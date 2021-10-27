'use strict';

angular.module('app.harvestpermit.application.deportation.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.deportation.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/deportation/species/species.html',
                controller: 'DeportationPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmount: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.deportation.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/deportation/species/species.html',
                controller: 'DeportationPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmount: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
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

    .controller('DeportationPermitWizardSpeciesController', function ($scope, DeportationPermitApplication,
                                                                      Species, TranslatedSpecies, GameSpeciesCodes,
                                                                      UnsavedChangesConfirmationService,
                                                                      ApplicationWizardNavigationHelper,
                                                                      states, wizard, applicationId, speciesAmount) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesAmount = speciesAmount;
            $ctrl.availableSpeciesOptions = getSpeciesList();
            $ctrl.selectedSpeciesCode = null;
            $scope.$watch('speciesAmountForm.$pristine', function (newVal, oldVal) {
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

        $ctrl.addSpecies = function () {
            UnsavedChangesConfirmationService.setChanges(true);
            $ctrl.speciesAmount = {
                gameSpeciesCode: $ctrl.selectedSpeciesCode,
                amount: 0
            };
            $ctrl.selectedSpeciesCode = null;
        };

        $ctrl.removeSpecies = function () {
            UnsavedChangesConfirmationService.setChanges(true);
            $ctrl.speciesAmount = null;
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return DeportationPermitApplication.saveSpeciesAmount({id: applicationId}, $ctrl.speciesAmount).$promise;
        };

        function invalid(form) {
            return form.$invalid || !$ctrl.speciesAmount;
        }

        function getSpeciesList() {
            return _.chain(Species.getPermitSpeciesWithoutAlienSpecies())
                .map(function (species) {
                    return TranslatedSpecies.translateSpecies(species);
                })
                .sortBy('name')
                .value();
        }

    });
