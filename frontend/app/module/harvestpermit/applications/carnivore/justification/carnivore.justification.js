'use strict';

angular.module('app.harvestpermit.application.carnivore.justification', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.carnivore.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/carnivore/justification/justification.html',
                controller: 'CarnivorePermitWizardJustificationController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    justification: function (CarnivorePermitApplication, applicationId) {
                        return CarnivorePermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'species',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.carnivore.justification', {
                url: '/justification',
                templateUrl: 'harvestpermit/applications/carnivore/justification/justification.html',
                controller: 'CarnivorePermitWizardJustificationController',
                controllerAs: '$ctrl',
                resolve: {
                    justification: function (CarnivorePermitApplication, applicationId) {
                        return CarnivorePermitApplication.getJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'species',
                            next: 'map'
                        };
                    }
                }
            });
    })

    .controller('CarnivorePermitWizardJustificationController', function ($scope, ApplicationWizardNavigationHelper,
                                                                          UnsavedChangesConfirmationService,
                                                                          CarnivorePermitApplication, applicationId,
                                                                          justification, states, wizard) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.data = justification;
            $scope.$watch('carnivoreJustificationForm.$pristine', function (newVal, oldVal) {
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
            return CarnivorePermitApplication.updateJustification({id: applicationId}, $ctrl.data).$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }
    });
