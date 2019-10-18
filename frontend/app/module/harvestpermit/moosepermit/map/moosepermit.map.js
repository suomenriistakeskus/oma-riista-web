'use strict';

angular.module('app.moosepermit.map', [])
    .factory('MoosePermitHarvest', function (DiaryEntryRepositoryFactory, $resource) {
        var repository = $resource('api/v1/moosepermit/:permitId/harvest', {"permitId": "@permitId"});

        DiaryEntryRepositoryFactory.decorateRepository(repository);

        return repository;
    })
    .service('MoosePermitMapService', function (Markers, GameDiaryParameters, FormSidebarService) {
        this.createPalette = function (featureCollection) {
            var features = _.get(featureCollection, 'features', []);
            var zArray = _.map(features, 'id');

            var palette = _.chain(zArray)
                .map(function (value, index) {
                    var hue = Math.round(index * 256 / zArray.length) % 256;
                    return [value, 'hsl(' + hue + ',100%,40%)'];
                })
                .fromPairs()
                .value();

            return {
                resolve: function (feature) {
                    return palette[feature.id] || 'green';
                }
            };
        };

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
            group: 'Harvests'
        };

        this.createMarkers = function (entryList, clickHandler, $scope) {
            function createMarkerData(entry) {
                return [{
                    id: entry.type + ':' + entry.id,
                    etrsCoordinates: entry.geoLocation,
                    icon: {
                        icon: 'crosshairs',
                        markerColor: 'green'
                    },
                    getMessageScope: _.constant($scope),
                    clickHandler: clickHandler
                }];
            }

            return Markers.transformToLeafletMarkerData(entryList, markerDefaults, createMarkerData);
        };

        var modalOptions = {
            controller: 'MoosePermitMapShowHarvestController',
            templateUrl: 'harvestpermit/moosepermit/map/show.html',
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

        var formSidebar = FormSidebarService.create(modalOptions, null, parametersToResolve);

        this.showDiaryEntry = function (diaryEntry) {
            return formSidebar.show({
                id: diaryEntry.id,
                diaryEntry: diaryEntry
            });
        };
    })
    .controller('MoosePermitMapController', function ($scope, $q, leafletData, $translate,
                                                      MapDefaults, MapState, MoosePermitMapService,
                                                      mapBounds, harvests, featureCollection, goBackFn) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.harvests = harvests;
            $ctrl.markers = [];
            $ctrl.mapState = MapState.get();
            $ctrl.mapDefaults = MapDefaults.create();
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.goBack = goBackFn;
            $ctrl.showGoBack = _.isFunction(goBackFn);

            MapState.updateMapBounds(mapBounds, mapBounds, true);

            var markerClickHandler = function (markerId) {
                var parts = markerId.split(':');

                if (parts.length === 2) {
                    var diaryEntry = _.find(harvests, {
                        'type': parts[0],
                        'id': _.parseInt(parts[1])
                    });

                    if (diaryEntry) {
                        MoosePermitMapService.showDiaryEntry(diaryEntry);
                    }
                }
            };

            $ctrl.markers = MoosePermitMapService.createMarkers(harvests, markerClickHandler, $scope);

            leafletData.getMap('club-permit-map').then(function (map) {
                var userLanguage = $translate.use() === 'sv' ? 'sv' : 'fi';
                var clubNameProperty = 'properties.clubName.' + userLanguage;
                var geoJsonLayer = createGeoJsonLayer(featureCollection, clubNameProperty);

                L.control.geoJsonLayerControl(geoJsonLayer, {
                    textToggleAll: $translate.instant('global.map.toggleLayers'),
                    layerToLegendTitle: function (layer) {
                        return _.get(layer.feature, clubNameProperty);
                    }
                }).addTo(map);
            });
        };

        function createGeoJsonLayer(featureCollection, clubNameProperty) {
            featureCollection.features = _.sortBy(featureCollection.features, clubNameProperty);
            var palette = MoosePermitMapService.createPalette(featureCollection);

            return L.geoJson(featureCollection, {
                style: function (feature) {
                    return {
                        fillColor: palette.resolve(feature),
                        weight: 1,
                        opacity: 1,
                        color: 'black',
                        fillOpacity: 0.5
                    };
                },
                onEachFeature: function (feature, layer) {
                    var clubName = _.get(feature, clubNameProperty);
                    layer.bindPopup(clubName);
                }
            });
        }
    })
    .controller('MoosePermitMapShowHarvestController', function ($scope,
                                                                 DiaryImageService,
                                                                 parameters,
                                                                 diaryEntry) {
        $scope.diaryEntry = diaryEntry;
        $scope.getGameNameWithAmount = parameters.$getGameNameWithAmount;
        $scope.getUrl = DiaryImageService.getUrl;

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    });
