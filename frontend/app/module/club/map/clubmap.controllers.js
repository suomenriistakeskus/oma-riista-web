'use strict';

angular.module('app.clubmap.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider.state('club.map', {
            url: '/areamap/{areaId:[0-9]{1,8}}',
            templateUrl: 'club/map/edit.html',
            wideLayout: true,
            resolve: {
                rhyBounds: function (MapBounds, club) {
                    return MapBounds.getRhyBounds(club.rhy.officialCode);
                },
                huntingClubAreaId: function ($stateParams) {
                    return _.parseInt($stateParams.areaId);
                },
                huntingClubArea: function (ClubAreas, huntingClubAreaId) {
                    return ClubAreas.get({id: huntingClubAreaId}).$promise;
                },
                featureCollection: function (ClubAreas, huntingClubAreaId) {
                    return ClubAreas.getFeatures({id: huntingClubAreaId}).$promise;
                },
                hirviAreaList: function (GIS, huntingClubArea) {
                    return GIS.listMetsahallitusHirviByYear(huntingClubArea.metsahallitusYear).then(_.property('data'));
                }
            },
            controllerAs: '$ctrl',
            controller: function ($state, $q, $scope,
                                  NotificationService, UnsavedChangesConfirmationService,
                                  ClubAreas, GeoJsonEditor, GeoJsonEditorMetsahallitus,
                                  huntingClubAreaId, huntingClubArea, hirviAreaList,
                                  rhyBounds, featureCollection) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.featureCollection = featureCollection;
                    $ctrl.rhyBounds = rhyBounds;
                    $ctrl.huntingClubArea = huntingClubArea;
                    $ctrl.hirviAreaList = hirviAreaList;
                };

                $ctrl.close = function () {
                    $state.go('club.area.list', {areaId: huntingClubAreaId});
                };

                $ctrl.saveFeatures = function (geoJSON) {
                    return checkAreaCalculated().then(function () {
                        return saveFeaturesGeoJSON(geoJSON);

                    }, function () {
                        NotificationService.showMessage('club.area.messages.areaCalculationRunning', 'warn');
                    });
                };

                function checkAreaCalculated() {
                    return ClubAreas.get({id: huntingClubAreaId}).$promise.then(function (a) {
                        return !!a.size ? $q.resolve() : $q.reject();
                    });
                }

                function saveFeaturesGeoJSON(geoJson) {
                    return ClubAreas.saveFeatures({id: huntingClubAreaId}, geoJson).$promise.then(function () {
                        UnsavedChangesConfirmationService.setChanges(false);
                        NotificationService.showDefaultSuccess();

                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                }
            }
        });
    });
