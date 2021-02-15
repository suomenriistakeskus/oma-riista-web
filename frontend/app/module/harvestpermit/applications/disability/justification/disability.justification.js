'use strict';

angular.module('app.harvestpermit.application.disability.justification', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.disability.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/disability/justification/justification.html',
                controller: 'DisabilityPermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    justification: function (DisabilityPermitApplication, applicationId) {
                        return DisabilityPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'basicinfo',
                            next: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.disability.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/disability/justification/justification.html',
                controller: 'DisabilityPermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    justification: function (DisabilityPermitApplication, applicationId) {
                        return DisabilityPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'basicinfo',
                            next: 'attachments'
                        };
                    }
                }
            });
    })

    .controller('DisabilityPermitWizardJustificationController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                           UnsavedChangesConfirmationService, Helpers,
                                                                           DisabilityPermitApplication, applicationId,
                                                                           states, wizard, justification,
                                                                           ApplicationVehicleTypes, HuntingTypes) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.huntingTypes = HuntingTypes;
            $ctrl.justification = justification || {};

            if (_.isEmpty($ctrl.justification.huntingTypeInfos)) {
                $ctrl.justification.huntingTypeInfos = [{key: 0}];
            } else {
                _.each($ctrl.justification.huntingTypeInfos, function (huntingTypeInfo, index) {
                    huntingTypeInfo.key = index;
                });
            }

            if (_.isEmpty($ctrl.justification.vehicles)) {
                $ctrl.justification.vehicles = [{key: 0}];
            } else {
                _.each($ctrl.justification.vehicles, function (vehicle, index) {
                    vehicle.key = index;
                });
            }

            $scope.$watch('disabilityJustificationForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.exit(invalid(form), $ctrl.save, wizard.exit);
        };

        $ctrl.previous = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, $ctrl.doGotoPrevious);
        };

        $ctrl.doGotoPrevious = function () {
            doGoto(states.previous);
        };

        function doGoto(state) {
            wizard.goto(state);
        }

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                doGoto(states.next);
            });
        };

        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            var justification = angular.copy($ctrl.justification);

            _.each(justification.vehicles, function (vehicle) {
                if (vehicle.type !== 'MUU') {
                    vehicle.description = null;
                }
                delete vehicle.key;
            });

            _.each(justification.huntingTypeInfos, function (huntingTypeInfo) {
                if (huntingTypeInfo.huntingType !== 'MUU') {
                    huntingTypeInfo.huntingTypeDescription = null;
                }
                delete huntingTypeInfo.key;
            });

            return DisabilityPermitApplication
                .updateJustification({id: applicationId}, justification)
                .$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }


        $ctrl.addVehicle = function () {
            var lastKey = _.maxBy($ctrl.justification.vehicles, function (vehicle) {
                return vehicle.key;
            }).key;
            $ctrl.justification.vehicles.push({key: lastKey + 1});
        };

        $ctrl.removeVehicle = function (key) {
            var idx = _.findIndex($ctrl.justification.vehicles, function (vehicle) {
                return vehicle.key === key;
            });
            if (idx !== -1) {
                $ctrl.justification.vehicles.splice(idx, 1);
            }
        };

        $ctrl.addHuntingTypeInfo = function () {
            var lastKey = _.maxBy($ctrl.justification.huntingTypeInfos, function (huntingTypeInfo) {
                return huntingTypeInfo.key;
            }).key;
            $ctrl.justification.huntingTypeInfos.push({key: lastKey + 1});
        };

        $ctrl.removeHuntingTypeInfo = function (key) {
            var idx = _.findIndex($ctrl.justification.huntingTypeInfos, function (huntingTypeInfo) {
                return huntingTypeInfo.key === key;
            });
            if (idx !== -1) {
                $ctrl.justification.huntingTypeInfos.splice(idx, 1);
            }
        };

    })
    .component('disabilityVehicles', {
        templateUrl: 'harvestpermit/applications/disability/justification/justification.vehicles.html',
        bindings: {
            vehicles: '<',
            addVehicle: '&',
            removeVehicle: '&'
        },
        controller: function (ApplicationVehicleTypes) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.vehicleTypes = ApplicationVehicleTypes;
            };
        }
    })
    .component('disabilityHuntingTypeInfos', {
        templateUrl: 'harvestpermit/applications/disability/justification/justification.huntingtypeinfos.html',
        bindings: {
            huntingTypeInfos: '<',
            addHuntingTypeInfo: '&',
            removeHuntingTypeInfo: '&'
        },
        controller: function (HuntingTypes) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.huntingTypes = HuntingTypes;
            };
        }
    })
    .constant('HuntingTypes', ['PIENRIISTA', 'HIRVIELAIMET', 'SUURPEDOT', 'MUU']);

