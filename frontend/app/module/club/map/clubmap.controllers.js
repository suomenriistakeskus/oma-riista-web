'use strict';

angular.module('app.clubmap.controllers', ['app.map.services'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.map', {
                url: '/areamap/{areaId:[0-9]{1,8}}',
                templateUrl: 'club/map/edit.html',
                controller: 'ClubAreaEditMapController',
                wideLayout: true,
                resolve: {
                    rhyBounds: function (GIS, club) {
                        return GIS.getRhyBounds(club.rhy.officialCode);
                    },
                    areaId: function ($stateParams) {
                        return $stateParams.areaId;
                    },
                    area: function (ClubAreas, clubId, areaId) {
                        return ClubAreas.get({clubId: clubId, id: areaId}).$promise;
                    },
                    featureCollection: function (ClubAreas, clubId, areaId) {
                        return ClubAreas.getFeatures({clubId: clubId, id: areaId}).$promise;
                    },
                    mh: function (MHAreaService, featureCollection, area) {
                        return MHAreaService.create(featureCollection, area);
                    }
                }
            });
    })

    .controller('ClubAreaEditMapController',
        function ($scope, $state, $log, $filter,
                  NotificationService, TranslatedBlockUI, UnsavedChangesConfirmationService,
                  GIS, WGS84, MapDefaults, MapState, MHAreaService, ClubAreas,
                  clubId, areaId,
                  rhyBounds, mh, featureCollection) {
            angular.extend($scope, {
                areaId: areaId,
                mapEvents: MapDefaults.getMapBroadcastEvents(),
                mapDefaults: MapDefaults.create(),
                mapState: MapState.get(),
                mh: mh,
                userInput: {
                    mooseAreaSearchQuery: null,
                    propertyList: []
                },
                okPropertyCount: null,
                invalidPropertyList: null,
                pager: {
                    currentPage: 1,
                    pageSize: 15,
                    total: 0
                },
                editor: {
                    api: {
                        ready: false
                    },
                    geojson: featureCollection,
                    callbacks: {
                        'add': function (latlng) {
                            // Add area geometry by clicking map
                            return GIS.getPropertyByCoordinates(WGS84.toETRS(latlng.lat, latlng.lng)).then(_.property('data'));
                        },
                        'marquee': function (bounds) {
                            return GIS.getPropertyByBounds(bounds).then(_.property('data'));
                        },
                        'onFeatureSelect': function (feature) {
                        }
                    }
                }
            });

            $scope.palstaFeatureList = function () {
                return $scope.editor.api.ready ? $scope.editor.api.features.palstaFeatureList() : [];
            };

            var areaBounds = GIS.getBoundsFromGeoJsonBbox(featureCollection.bbox);

            MapState.updateMapBounds(areaBounds, rhyBounds, false);

            $scope.close = function () {
                $state.go('club.area.list', {areaId: areaId});
            };

            $scope.selectFeature = function (feature) {
                $scope.editor.api.zoom(feature.id);
                $scope.editor.api.features.selectFeature(feature);
            };

            $scope.saveFeatures = function () {
                var geoJson = $scope.editor.api.features.toGeoJSON();
                geoJson.id = areaId;
                geoJson.clubId = clubId;

                var onSuccess = function () {
                    UnsavedChangesConfirmationService.setChanges(false);
                    NotificationService.showDefaultSuccess();
                };

                TranslatedBlockUI.start("global.block.wait");

                ClubAreas.updateFeatures(geoJson).$promise
                    .then(onSuccess, NotificationService.showDefaultFailure)
                    .finally(function () {
                        TranslatedBlockUI.stop();
                    });
            };

            $scope.getActiveFeature = function () {
                return $scope.editor.api.ready ? $scope.editor.api.features.getActiveFeature() : null;
            };

            $scope.focusMooseArea = function (area) {
                $scope.editor.api.zoom(area.id);
            };

            $scope.onSelectMooseArea = function (area) {
                UnsavedChangesConfirmationService.setChanges(true);

                // De-select
                $scope.userInput.mooseAreaSearchQuery = null;

                // Show geometry
                GIS.getMetsahallitusHirviById(area.gid).then(function (response) {
                    $scope.editor.api.features.removeMooseArea(area);
                    $scope.editor.api.addFeatures(response.data);

                    var mooseArea = mh.addSelectedArea(area);
                    $scope.focusMooseArea(mooseArea);
                });
            };

            $scope.removeMooseArea = function (area) {
                UnsavedChangesConfirmationService.setChanges(true);
                mh.removeSelectedArea(area);
                $scope.editor.api.features.removeMooseArea(area);
            };

            $scope.updateChangedFeature = function (feature) {
                $scope.editor.api.features.updateChangedFeature(feature).catch(NotificationService.showDefaultFailure);
            };

            // Add area geometry using property identifier
            $scope.addPropertiesAsText = function () {
                var api = $scope.editor.api;

                var propertyListCopy = $scope.userInput.propertyList;
                $scope.userInput.propertyList = [];
                $scope.okPropertyCount = 0;
                $scope.invalidPropertyList = [];

                var handleResponse = function (propertyIdentifier) {
                    return function (response) {
                        if (response.data.features.length) {
                            api.addFeatures(response.data);
                            $scope.okPropertyCount++;
                        } else {
                            $scope.invalidPropertyList.push(propertyIdentifier);
                        }
                    };
                };

                for (var i = 0; i < propertyListCopy.length; i++) {
                    var value = propertyListCopy[i];
                    GIS.getPropertyPolygonByCode(value).then(handleResponse(value), NotificationService.showDefaultFailure);
                }
            };
        }
    );

