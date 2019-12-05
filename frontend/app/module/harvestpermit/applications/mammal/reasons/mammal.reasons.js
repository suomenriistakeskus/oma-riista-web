'use strict';

angular.module('app.harvestpermit.application.mammal.reasons', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mammal.reasons', {
                url: '/reasons',
                templateUrl: 'harvestpermit/applications/mammal/reasons/reasons.html',
                controller: 'MammalPermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    permitCause: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.getCurrentPermitCause({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'map',
                            next: 'period'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.mammal.reasons', {
                url: '/reasons',
                templateUrl: 'harvestpermit/applications/mammal/reasons/reasons.html',
                controller: 'MammalPermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    permitCause: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.getCurrentPermitCause({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'map',
                            next: 'period'
                        };
                    }
                }
            });
    })

    .controller('MammalPermitWizardJustificationController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                       UnsavedChangesConfirmationService,
                                                                       MammalPermitApplication, applicationId,
                                                                       states, wizard, permitCause) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.reasons = permitCause.reasons;
            $scope.$watch('deviationForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, wizard.exit);
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
            return MammalPermitApplication.updatePermitCause({
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
