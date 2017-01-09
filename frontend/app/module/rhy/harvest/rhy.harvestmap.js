(function () {
    "use strict";

    angular.module('app.rhy.harvestmap', [])
        .factory('RhyHarvestRepository', RhyHarvestRepository)
        .service('RhyHarvestMapService', RhyHarvestMapService)
        .controller('RhyHarvestMapController', RhyHarvestMapController)
        .controller('RhyHarvestMapShowController', RhyHarvestMapShowController)
        .config(configureState);

    function configureState($stateProvider) {
        $stateProvider.state('rhy.harvestmap', {
            url: '/harvestmap',
            templateUrl: 'rhy/harvest/harvestmap.html',
            controller: 'RhyHarvestMapController',
            controllerAs: '$ctrl',
            bindToController: true,
            wideLayout: true,
            resolve: {
                availableSpecies: function (MooselikeSpecies) {
                    return MooselikeSpecies.getPermitBased();
                },
                rhy: function (Rhys, orgId) {
                    return Rhys.get({id: orgId}).$promise;
                },
                rhyBounds: function (rhy, GIS) {
                    return GIS.getRhyBounds(rhy.officialCode);
                },
                rhyGeometry: function (rhy, GIS) {
                    return GIS.getRhyGeom(rhy.officialCode).then(function (response) {
                        return response.data;
                    });
                },
                interval: function (HuntingYearService) {
                    var now = new Date();
                    return {
                        begin: HuntingYearService.getBeginDateStr(now),
                        end: HuntingYearService.getEndDateStr(now)
                    };
                }
            }
        });
    }

    function RhyHarvestRepository(DiaryEntryRepositoryFactory, $resource) {
        return DiaryEntryRepositoryFactory.decorateRepository($resource(
            'api/v1/riistanhoitoyhdistys/:rhyId/harvest', {
                "rhyId": "@rhyId",
                "speciesCode": "@speciesCode",
                "filterByAreaGeometry": "@filterByAreaGeometry"
            }
        ));
    }

    function RhyHarvestMapService(Markers, GameDiaryParameters, FormSidebarService) {
        var formSidebar = createFormSidebar();

        this.createMarkers = function (entryList, $scope) {
            var markerDefaults = {
                draggable: false,
                icon: {
                    type: 'awesomeMarker',
                    prefix: 'fa',
                    icon: 'crosshairs'
                },
                groupOption: {
                    showCoverageOnHover: true,
                    iconCreateFunction: Markers.iconCreateFunction(_.constant('accepted'))
                },
                group: 'DiaryEntries'
            };

            function markerClickHandler(markerId) {
                var parts = markerId.split(':');

                if (parts.length === 2) {
                    var diaryEntry = _.findWhere(entryList, {
                        'type': parts[0],
                        'id': _.parseInt(parts[1])
                    });

                    if (diaryEntry) {
                        formSidebar.show({
                            id: diaryEntry.id,
                            diaryEntry: diaryEntry
                        });
                    }
                }
            }

            function createMarkerData(entry) {
                return [{
                    id: entry.type + ':' + entry.id,
                    etrsCoordinates: entry.geoLocation,
                    icon: {
                        icon: 'crosshairs',
                        markerColor: 'green'
                    },
                    getMessageScope: _.constant($scope),
                    clickHandler: markerClickHandler
                }];
            }

            return Markers.transformToLeafletMarkerData(entryList, markerDefaults, createMarkerData);
        };

        this.createRhyFeatures = function (rhy, rhyGeometry) {
            return {
                data: {
                    type: "FeatureCollection",
                    features: [{
                        type: "Feature",
                        id: rhy.id,
                        properties: {name: rhy.nameFI},
                        geometry: {
                            type: "MultiPolygon",
                            coordinates: [[
                                [[-180, -180], [180, 0], [180, 180], [0, 180], [-180, -180]],
                                rhyGeometry.coordinates[0][0]]]
                        }
                    }]
                },
                style: {
                    fillColor: "#A080B0",
                    weight: 2,
                    opacity: 0,
                    color: 'none',
                    fillOpacity: 0.45
                }
            };
        };

        function createFormSidebar() {
            var modalOptions = {
                controller: 'RhyHarvestMapShowController',
                templateUrl: 'rhy/harvest/show.html',
                largeDialog: false,
                resolve: {
                    parameters: _.constant(GameDiaryParameters.query().$promise)
                }
            };

            function parametersToResolve(parameters) {
                return {
                    diaryEntry: _.constant(parameters.diaryEntry)
                };
            }

            return FormSidebarService.create(modalOptions, null, parametersToResolve);
        }
    }

    function RhyHarvestMapShowController($scope, DiaryEntryService, parameters, diaryEntry) {
        $scope.diaryEntry = diaryEntry;
        $scope.getGameNameWithAmount = parameters.$getGameNameWithAmount;
        $scope.getUrl = DiaryEntryService.getUrl;

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    }

    function RhyHarvestMapController($scope, MapState, MapDefaults, Markers,
                                     RhyHarvestMapService, RhyHarvestRepository, TranslatedBlockUI,
                                     availableSpecies, rhy, rhyGeometry, rhyBounds, orgId, interval) {
        var $ctrl = this;

        $ctrl.availableSpecies = availableSpecies;
        $ctrl.selectedSpeciesCode = null;
        $ctrl.filterByAreaGeometry = true;
        $ctrl.beginDate = interval.begin;
        $ctrl.endDate = interval.end;

        $ctrl.onFilterCriteriaChanged = function () {
            if ($ctrl.beginDate && $ctrl.endDate) {
                fetchHarvests();
            }
        };

        $ctrl.mapState = MapState.get();
        $ctrl.mapDefaults = MapDefaults.create();
        $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();

        function fetchHarvests() {
            TranslatedBlockUI.start("global.block.wait");
            RhyHarvestRepository.query({
                rhyId: orgId,
                speciesCode: $ctrl.selectedSpeciesCode,
                filterByAreaGeometry: $ctrl.filterByAreaGeometry,
                begin: $ctrl.beginDate,
                end: $ctrl.endDate
            }).$promise
                .then(updateMap)
                .finally(TranslatedBlockUI.stop);
        }

        function updateMap(harvests) {
            $ctrl.markers = RhyHarvestMapService.createMarkers(harvests, $ctrl);

            $ctrl.mapFeatures = RhyHarvestMapService.createRhyFeatures(rhy, rhyGeometry);

            var markerBounds = Markers.getMarkerBounds($ctrl.markers, rhyBounds);
            MapState.updateMapBounds(markerBounds, rhyBounds, true);
            MapState.get().center = {};
        }

        $ctrl.selectSpeciesCode = function (speciesCode) {
            $ctrl.selectedSpeciesCode = speciesCode;
            $ctrl.onFilterCriteriaChanged();
        };
    }
})();
