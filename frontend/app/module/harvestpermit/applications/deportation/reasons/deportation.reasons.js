'use strict';

angular.module('app.harvestpermit.application.deportation.reasons', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.deportation.reasons', {
                url: '/reasons',
                templateUrl: 'harvestpermit/applications/deportation/reasons/reasons.html',
                controller: 'DeportationPermitWizardReasonsController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    permitCause: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getCurrentPermitCause({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'period'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.deportation.reasons', {
                url: '/reasons',
                templateUrl: 'harvestpermit/applications/deportation/reasons/reasons.html',
                controller: 'DeportationPermitWizardReasonsController',
                controllerAs: '$ctrl',
                resolve: {
                    permitCause: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getCurrentPermitCause({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'period'
                        };
                    }
                }
            });
    })

    .controller('DeportationPermitWizardReasonsController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                      UnsavedChangesConfirmationService,
                                                                      DeportationPermitApplication, applicationId,
                                                                      states, wizard, permitCause) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.reasons = permitCause.reasons;
            $scope.$watch('deportationReasonsForm.$pristine', function (newVal, oldVal) {
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
            return invalid(form) || isSomeCauseMissing();
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return DeportationPermitApplication.updatePermitCause({
                id: applicationId
            }, {reasons: $ctrl.reasons}).$promise;
        };

        function isSomeCauseMissing() {
            // Every legal section must have some reason selected
            return !!_.find($ctrl.reasons, function (reason) {
                return _.every(reason.lawSectionReasons, ['checked', false]);
            });
        }

        function invalid(form) {
            return form.$invalid;
        }
    });
