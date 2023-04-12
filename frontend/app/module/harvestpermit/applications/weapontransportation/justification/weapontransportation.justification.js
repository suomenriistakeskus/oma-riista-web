'use strict';

angular.module('app.harvestpermit.application.weapontransportation.justification', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.weapontransportation.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/weapontransportation/justification/justification.html',
                controller: 'WeaponTransportationWizardJustificationController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    justification: function (applicationId, WeaponTransportationPermitApplication) {
                        return WeaponTransportationPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.weapontransportation.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/weapontransportation/justification/justification.html',
                controller: 'WeaponTransportationWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    justification: function (applicationId, WeaponTransportationPermitApplication) {
                        return WeaponTransportationPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'attachments'
                        };
                    }
                }
            });
    })

    .controller('WeaponTransportationWizardJustificationController', function ($state, $scope,
                                                                               wizard, applicationId,
                                                                               UnsavedChangesConfirmationService,
                                                                               WeaponTransportationPermitApplication,
                                                                               ApplicationWizardNavigationHelper,
                                                                               justification, states,
                                                                               WeaponTransportationVehicleTypes, WeaponTransportationWeaponTypes) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.vehicleTypes = WeaponTransportationVehicleTypes;
            $ctrl.weaponTypes = WeaponTransportationWeaponTypes;
            $ctrl.justification = justification || {};

            if (!$ctrl.justification.transportedWeapons || $ctrl.justification.transportedWeapons.length === 0) {
                $ctrl.justification.transportedWeapons = [{key: 0}];
            } else {
                _.each($ctrl.justification.transportedWeapons, function (weapon, index) {
                    weapon.key = index;
                });
            }

            if (!$ctrl.justification.vehicles || $ctrl.justification.vehicles.length === 0) {
                $ctrl.justification.vehicles = [{key: 0}];
            } else {
                _.each($ctrl.justification.vehicles, function (vehicle, index) {
                    vehicle.key = index;
                });
            }

            $scope.$watch('weaponTransportationJustificationForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.exit(form.$invalid, $ctrl.save, wizard.exit);
        };

        $ctrl.previous = function (form) {
            ApplicationWizardNavigationHelper.previous(form.$invalid, $ctrl.save, $ctrl.doGotoPrevious);
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

        $ctrl.isNextDisabled = function (form) {
            return form.$invalid;
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            var justification = angular.copy($ctrl.justification);

            _.each(justification.transportedWeapons, function (weapon) {
                if (weapon.type !== 'MUU') {
                    weapon.description = null;
                }

                delete weapon.key;
            });

            _.each(justification.vehicles, function (vehicle) {
                if (vehicle.type !== 'MUU') {
                    vehicle.description = null;
                }
                delete vehicle.key;
            });

            return WeaponTransportationPermitApplication
                .updateJustification({id: applicationId}, justification)
                .$promise;
        };

        $ctrl.addWeapon = function () {
            var lastKey = _.maxBy($ctrl.justification.transportedWeapons, function (weapon) {
                return weapon.key;
            }).key;
            $ctrl.justification.transportedWeapons.push({key: lastKey + 1});
        };

        $ctrl.removeWeapon = function (key) {
            var idx = _.findIndex($ctrl.justification.transportedWeapons, function (weapon) {
                return weapon.key === key;
            });
            if (idx !== -1) {
                $ctrl.justification.transportedWeapons.splice(idx, 1);
            }
        };

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
    })
    .constant('WeaponTransportationVehicleTypes', ['AUTO', 'MOOTTORIKELKKA', 'MONKIJA', 'MUU'])
    .constant('WeaponTransportationWeaponTypes', ['KIVAARI', 'HAULIKKO', 'METSASTYSJOUSI', 'MUU']);
