'use strict';

angular.module('app.harvestpermit.application.importing.justification', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.importing.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/importing/justification/justification.html',
                controller: 'ImportingPermitWizardJustificationController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    justification: function (ImportingPermitApplication, applicationId) {
                        return ImportingPermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.importing.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/importing/justification/justification.html',
                controller: 'ImportingPermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    justification: function (ImportingPermitApplication, applicationId) {
                        return ImportingPermitApplication.getJustification({id: applicationId}).$promise;
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

    .controller('ImportingPermitWizardJustificationController', function ($scope, ApplicationWizardNavigationHelper,
                                                                          UnsavedChangesConfirmationService,
                                                                          ImportingPermitApplication, applicationId,
                                                                          justification, states, wizard) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.data = justification;
            $scope.$watch('importingJustificationForm.$pristine', function (newVal, oldVal) {
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
            return ImportingPermitApplication.updateJustification({id: applicationId}, $ctrl.data).$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }
    });
