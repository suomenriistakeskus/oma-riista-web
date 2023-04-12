'use strict';

angular.module('app.harvestpermit.application.lawsectionten.partridge.population', ['app.metadata'])

    .controller('PartridgePopulationController', function ($scope, LawSectionTenPermitApplication, ApplicationWizardNavigationHelper,
                                                           UnsavedChangesConfirmationService,
                                                           wizard, speciesPopulation, applicationId, states) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPopulation = speciesPopulation;
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
            return LawSectionTenPermitApplication.updateSpeciesPopulation({id: applicationId}, $ctrl.speciesPopulation).$promise;
        };
    })

    .component('partridgeApplicationSummaryPopulation', {
        templateUrl: 'harvestpermit/applications/lawsectionten/partridge/summary-population.html',
        bindings: {
            application: '<'
        }
    });

