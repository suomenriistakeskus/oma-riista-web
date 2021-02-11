'use strict';

angular.module('app.harvestpermit.application.bird.map', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/bird/map/map.html',
                controller: 'BirdPermitWizardMapController',
                controllerAs: '$ctrl',
                resolve: {
                    protectedAreaInfo: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentProtectedArea({id: applicationId}).$promise;
                    }
                }
            }).state('jht.decision.application.wizard.bird.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/bird/map/map.html',
                controller: 'BirdPermitWizardMapController',
                controllerAs: '$ctrl',
                resolve: {
                    protectedAreaInfo: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentProtectedArea({id: applicationId}).$promise;
                    }
                }
            })
            .state('profile.permitwizard.bird.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/wizard/area/mapdetails.html',
                controller: 'DerogationPermitWizardMapDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    areaInfo: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getArea({id: applicationId}).$promise;
                    },
                    attachmentList: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'map',
                            next: 'cause'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/wizard/area/mapdetails.html',
                controller: 'DerogationPermitWizardMapDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    areaInfo: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getArea({id: applicationId}).$promise;
                    },
                    attachmentList: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'map',
                            next: 'cause'
                        };
                    }
                }
            });
    })

    .controller('BirdPermitWizardMapController', function ($scope, $http, $translate, dialogs,
                                                           ProtectedAreaTypes, BirdPermitApplication,
                                                           UnsavedChangesConfirmationService, ApplicationWizardNavigationHelper,
                                                           wizard, applicationId, protectedAreaInfo) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.protectedAreaInfo = protectedAreaInfo;
            $ctrl.availableProtectedAreaTypes = ProtectedAreaTypes;
            $scope.$watch('protectedAreaForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.exit(invalid(form), $ctrl.save, function () {
                wizard.exit();
            });
        };

        $ctrl.previous = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, $ctrl.doGotoPrevious);
        };

        $ctrl.doGotoPrevious = function () {
                wizard.goto('species');

        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto('mapdetails');
            });
        };

        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        function invalid(form) {
            return form.$invalid
                || !validLocation($ctrl.protectedAreaInfo.geoLocation);
        }

        function validLocation(l) {
            return _.isObject(l) && _.isFinite(l.longitude) && _.isFinite(l.latitude);
        }

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return BirdPermitApplication.updateProtectedArea({
                id: applicationId
            }, $ctrl.protectedAreaInfo).$promise;
        };
    })

    .component('birdApplicationLocationEditor', {
        templateUrl: 'harvestpermit/applications/bird/map/location.html',
        bindings: {
            protectedArea: '<'
        },
        controller: function ($state, $scope, MapDefaults, MapUtil, MapState, GIS) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.mapState = MapState.get();
                $ctrl.mapDefaults = MapDefaults.create({scrollWheelZoom: false});
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);

                var geoLocation;

                if ($ctrl.protectedArea && $ctrl.protectedArea.geoLocation) {
                    geoLocation = $ctrl.protectedArea.geoLocation;
                    updateRhy(geoLocation.longitude, geoLocation.latitude);
                } else {
                    geoLocation = null;
                }

                MapState.updateMapCenter(geoLocation
                    ? angular.copy(geoLocation)
                    : MapUtil.getDefaultGeoLocation(), 6);

                $scope.$watchGroup([
                    '$ctrl.protectedArea.geoLocation.longitude',
                    '$ctrl.protectedArea.geoLocation.latitude'
                ], function (newValues, oldValues) {
                    if (!(newValues[0] === oldValues[0] && newValues[1] === oldValues[1])) {
                        updateRhy(newValues[0], newValues[1]);
                    }
                });
            };

            function updateRhy(longitude, latitude) {
                if (_.isFinite(longitude) && _.isFinite(latitude)) {
                    GIS.getRhyForGeoLocation({
                        latitude: latitude,
                        longitude: longitude
                    }).then(function (res) {
                        $ctrl.rhy = res.data;
                    });
                }
            }
        }
    });
