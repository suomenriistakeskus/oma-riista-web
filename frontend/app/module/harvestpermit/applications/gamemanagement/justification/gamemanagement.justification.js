'use strict';

angular.module('app.harvestpermit.application.gamemanagement.justification', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.gamemanagement.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/gamemanagement/justification/justification.html',
                controller: 'GameManagementPermitWizardJustificationController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    justification: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'methods'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.gamemanagement.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/gamemanagement/justification/justification.html',
                controller: 'GameManagementPermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    justification: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'methods'
                        };
                    }
                }
            });
    })

    .controller('GameManagementPermitWizardJustificationController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                               UnsavedChangesConfirmationService, Helpers,
                                                                               GameManagementPermitApplication, applicationId,
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
            return GameManagementPermitApplication
                .updateJustification({id: applicationId}, $ctrl.justification)
                .$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }
    });
