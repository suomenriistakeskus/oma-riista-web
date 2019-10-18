'use strict';

angular.module('app.jht.area.map', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.areamap', {
            url: '/areamap/{areaId:[0-9]{1,8}}',
            templateUrl: 'jht/area/map/area-map.html',
            wideLayout: true,
            resolve: {
                areaId: function ($stateParams) {
                    return _.parseInt($stateParams.areaId);
                },
                area: function (ModeratorAreas, areaId) {
                    return ModeratorAreas.get({id: areaId}).$promise;
                },
                featureCollection: function (ModeratorAreas, areaId) {
                    return ModeratorAreas.getFeatures({id: areaId}).$promise;
                },
                metsahallitusYear: function (area) {
                    return area.metsahallitusYear;
                },
                hirviAreaList: function (GIS, metsahallitusYear) {
                    return GIS.listMetsahallitusHirviByYear(metsahallitusYear).then(_.property('data'));
                }
            },
            controllerAs: '$ctrl',
            controller: function ($state, $q, $scope,
                                  NotificationService, UnsavedChangesConfirmationService,
                                  MapBounds, ModeratorAreas,
                                  GeoJsonEditor, GeoJsonEditorMetsahallitus,
                                  hirviAreaList, metsahallitusYear,
                                  area, areaId, featureCollection) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.featureCollection = featureCollection;
                    $ctrl.hirviAreaList = hirviAreaList;
                    $ctrl.metsahallitusYear = metsahallitusYear;
                    $ctrl.defaultBounds = MapBounds.getBoundsOfFinland();
                    $ctrl.area = area;
                };

                $ctrl.close = function () {
                    $state.go('jht.areas', {});
                };

                $ctrl.saveFeatures = function (geoJSON) {
                    return saveFeaturesGeoJSON(geoJSON);
                };

                function saveFeaturesGeoJSON(geoJson) {
                    return ModeratorAreas.saveFeatures({id: areaId}, geoJson).$promise.then(function () {
                        UnsavedChangesConfirmationService.setChanges(false);
                        NotificationService.showDefaultSuccess();

                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                }
            }
        });
    });
