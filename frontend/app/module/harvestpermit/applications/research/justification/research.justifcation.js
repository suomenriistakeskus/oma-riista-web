'use strict';

angular.module('app.harvestpermit.application.research.justification', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.research.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/research/justification/justification.html',
                controller: 'ResearchPermitWizardJustificationController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    justification: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'methods',
                            next: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.research.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/research/justification/justification.html',
                controller: 'ResearchPermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    justification: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'methods',
                            next: 'attachments'
                        };
                    }
                }
            });
    })

    .controller('ResearchPermitWizardJustificationController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                         UnsavedChangesConfirmationService, Helpers,
                                                                         ResearchPermitApplication, applicationId,
                                                                         states, wizard, justification) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.justification = justification || {};

            $scope.$watch('justificationForm.$pristine', function (newVal, oldVal) {
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
            return ResearchPermitApplication
                .updateJustification({id: applicationId}, $ctrl.justification)
                .$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }
    });
