'use strict';

angular.module('app.harvestpermit.application.wizard.derogation.population', ['app.metadata'])

    .controller('DerogationPermitWizardPopulationController', function ($scope, DerogationPermitApplication, ApplicationWizardNavigationHelper,
                                                                        UnsavedChangesConfirmationService,
                                                                        wizard, speciesPopulationList, applicationId, states) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPopulationList = speciesPopulationList;
            $scope.$watch('speciesPopulationForm.$pristine', function (newVal, oldVal) {
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
            wizard.goto(states.previous);
        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto(states.next);
            });
        };

        function invalid(form) {
            return form.$invalid;
        }

        $ctrl.nextDisabled = function (form) {
            return form.$invalid;
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return DerogationPermitApplication.updateSpeciesPopulation({id: applicationId}, {list: $ctrl.speciesPopulationList}).$promise;
        };
    });
