'use strict';

angular.module('app.account.area.map', [])
    .config(function ($stateProvider) {
        $stateProvider.state('profile.areamap', {
            url: '/areamap/{areaId:[0-9]{1,8}}',
            templateUrl: 'account/area/map/area-map.html',
            wideLayout: true,
            resolve: {
                areaId: function ($stateParams) {
                    return _.parseInt($stateParams.areaId);
                },
                area: function (AccountAreas, areaId) {
                    return AccountAreas.get({id: areaId}).$promise;
                },
                featureCollection: function (AccountAreas, areaId) {
                    return AccountAreas.getFeatures({id: areaId}).$promise;
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
                                  MapBounds, AccountAreas,
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
                    $state.go('profile.areas.personal', {});
                };

                $ctrl.saveFeatures = function (geoJSON) {
                    return saveFeaturesGeoJSON(geoJSON);
                };

                function saveFeaturesGeoJSON(geoJson) {
                    return AccountAreas.saveFeatures({id: areaId}, geoJson).$promise.then(function () {
                        UnsavedChangesConfirmationService.setChanges(false);
                        NotificationService.showDefaultSuccess();

                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                }
            }
        });
    });
