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
                                  rhyBounds, featureCollection, ClubMapZoneProcessingModal) {
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
                        return a.size.status !== 'PROCESSING' ? $q.resolve() : $q.reject();
                    });
                }

                function saveFeaturesGeoJSON(geoJson) {
                    return ClubAreas.saveFeatures({id: huntingClubAreaId}, geoJson).$promise.then(function () {
                        showProcessingModal();
                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                }

                function showProcessingModal() {
                    ClubMapZoneProcessingModal.open(huntingClubAreaId).then(function () {
                        UnsavedChangesConfirmationService.setChanges(false);
                        NotificationService.showDefaultSuccess();
                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                }
            }
        });
    })
    .service('ClubMapZoneProcessingModal', function ($uibModal, $q, $interval, ClubAreas) {
        this.open = function (areaId) {
            var modalInstance = $uibModal.open({
                templateUrl: 'club/map/processing.html',
                size: 'md',
                resolve: {
                    areaId: _.constant(areaId)
                },
                controllerAs: '$ctrl',
                controller: ModalController
            });
            return modalInstance.result;
        };

        function checkStatus(areaId) {
            return ClubAreas.getZoneStatus({id: areaId}).$promise.then(function (res) {
                if (res.status === 'PENDING' || res.status === 'PROCESSING') {
                    return $q.reject(res.status);
                }

                return res.status === 'READY' ? $q.when(true) : $q.when(false);
            });
        }

        function ModalController($uibModalInstance, areaId) {
            var $ctrl = this;
            $ctrl.status = 'PENDING';
            $ctrl.progress = 0;
            $ctrl.progressBarWidth = 7;

            var intervalPromise = $interval(updateProgress, 1000);

            updateProgress();

            function updateProgress() {
                checkStatus(areaId).then(function (success) {
                    $interval.cancel(intervalPromise);

                    if (success) {
                        $uibModalInstance.close();
                    } else {
                        $uibModalInstance.dismiss();
                    }

                }, function (status) {
                    $ctrl.status = status;

                    if (status === 'PROCESSING') {
                        $ctrl.progressBarWidth = 7 + Math.round(93.0 *
                            (1.0 - 1.0 / Math.exp(++$ctrl.progress / 100.0)));
                    }
                });
            }

        }
    });
