'use strict';

angular.module('app.harvestpermit.application.weapontransportation.reason', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.weapontransportation.reason', {
                url: '/reason',
                templateUrl: 'harvestpermit/applications/weapontransportation/reason/reason.html',
                controller: 'WeaponTransportationWizardReasonController',
                controllerAs: '$ctrl',
                resolve: {
                    reason: function (applicationId, WeaponTransportationPermitApplication) {
                        return WeaponTransportationPermitApplication.getReason({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.weapontransportation.reason', {
                url: '/reason',
                templateUrl: 'harvestpermit/applications/weapontransportation/reason/reason.html',
                controller: 'WeaponTransportationWizardReasonController',
                controllerAs: '$ctrl',
                resolve: {
                    reason: function (applicationId, WeaponTransportationPermitApplication) {
                        return WeaponTransportationPermitApplication.getReason({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            });
    })

    .controller('WeaponTransportationWizardReasonController', function ($state, $scope,
                                                                        wizard, applicationId,
                                                                        UnsavedChangesConfirmationService,
                                                                        WeaponTransportationPermitApplication,
                                                                        ApplicationWizardNavigationHelper, Helpers,
                                                                        reason, states) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.reason = reason || {};
            $ctrl.reason.reasonType = $ctrl.reason.reasonType || 'POROMIES';

            $scope.$watch('weaponTransportationReasonForm.$pristine', function (newVal, oldVal) {
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

        $ctrl.isPeriodTooLong = function () {
            var b = $ctrl.reason.beginDate ? Helpers.toMoment($ctrl.reason.beginDate, 'YYYY-MM-DD') : null;
            var e = $ctrl.reason.endDate ? Helpers.toMoment($ctrl.reason.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e) {
                return false;
            }
            var maxEnd = b.clone().add(5, 'year').subtract(1, 'day');
            return e.isAfter(maxEnd);
        };

        $ctrl.isEndDateBeforeBeginDate = function () {
            var b = $ctrl.reason.beginDate ? Helpers.toMoment($ctrl.reason.beginDate, 'YYYY-MM-DD') : null;
            var e = $ctrl.reason.endDate ? Helpers.toMoment($ctrl.reason.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e) {
                return false;
            }
            return e.isBefore(b);
        };

        $ctrl.isNextDisabled = function (form) {
            return $ctrl.isEndDateBeforeBeginDate() || $ctrl.isPeriodTooLong() || form.$invalid;
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            if ($ctrl.reason.reasonType === 'POROMIES') {
                $ctrl.reason.reasonDescription = null;
            }
            return WeaponTransportationPermitApplication
                .updateReason({id: applicationId}, $ctrl.reason)
                .$promise;
        };
    });
