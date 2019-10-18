'use strict';

angular.module('app.harvestpermit.application.bird.population', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/bird/population/population.html',
                controller: 'BirdPermitWizardPopulationController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPopulationList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/bird/population/population.html',
                controller: 'BirdPermitWizardPopulationController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPopulationList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('BirdPermitWizardPopulationController', function ($scope, BirdPermitApplication, ApplicationWizardNavigationHelper,
                                                                  UnsavedChangesConfirmationService,
                                                                  wizard, speciesPopulationList, applicationId) {
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
            wizard.goto('damage');
        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto('attachments');
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
            return BirdPermitApplication.updateSpeciesPopulation({id: applicationId}, {list: $ctrl.speciesPopulationList}).$promise;
        };
    });
