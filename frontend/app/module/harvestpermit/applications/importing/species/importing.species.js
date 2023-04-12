'use strict';

angular.module('app.harvestpermit.application.importing.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.importing.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/importing/species/species.html',
                controller: 'ImportingPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesAmountList: function (ImportingPermitApplication, applicationId) {
                        return ImportingPermitApplication.getSpeciesAmounts({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'period'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.importing.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/importing/species/species.html',
                controller: 'ImportingPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmountList: function (ImportingPermitApplication, applicationId) {
                        return ImportingPermitApplication.getSpeciesAmounts({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'period'
                        };
                    }
                }
            });
    })

    .controller('ImportingPermitWizardSpeciesController', function ($scope, ImportingPermitApplication,
                                                                      Species, TranslatedSpecies, GameSpeciesCodes,
                                                                      UnsavedChangesConfirmationService,
                                                                      ApplicationWizardNavigationHelper,
                                                                      states, wizard, applicationId, speciesAmountList) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesAmountList = speciesAmountList;
            $ctrl.availableSpeciesOptions = buildAvailableSpecies();
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

        $ctrl.addSpecies = function (speciesCode) {
            if (!speciesCode) {
                return;
            }
            UnsavedChangesConfirmationService.setChanges(true);
            $ctrl.selectedSpeciesCode = null;
            $ctrl.speciesAmountList.push({
                gameSpeciesCode: speciesCode,
                subSpeciesName: null
            });
            $ctrl.availableSpeciesOptions = buildAvailableSpecies();
        };

        $ctrl.removeSpecies = function (speciesAmount) {
            UnsavedChangesConfirmationService.setChanges(true);
            $ctrl.speciesAmountList = _.filter($ctrl.speciesAmountList, function (a) {
                return a !== speciesAmount;
            });
            $ctrl.availableSpeciesOptions = buildAvailableSpecies();
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            // Send only applied amounts
            $ctrl.speciesAmountList.forEach(function (spa) {
                if (spa.specimenAmount === 0) {
                    spa.specimenAmount = null;
                }
                if (spa.eggAmount === 0) {
                    spa.eggAmount = null;
                }
            });
            return ImportingPermitApplication.saveSpeciesAmounts({id: applicationId}, {list: $ctrl.speciesAmountList}).$promise;
        };

        function invalid(form) {
            var speciesWithAmount = _.filter($ctrl.speciesAmountList, function (spa) {
                return (spa.specimenAmount && spa.specimenAmount > 0) ||
                    (spa.eggAmount && spa.eggAmount > 0);
            });
            var allSpeciesHaveAtLeastOneAmount = speciesWithAmount.length === $ctrl.speciesAmountList.length;
            return form.$invalid || _.isEmpty($ctrl.speciesAmountList) || duplicateSpeciesCodes() || !allSpeciesHaveAtLeastOneAmount;
        }

        function duplicateSpeciesCodes() {
            var uniqueCodes = _.chain($ctrl.speciesAmountList).map('gameSpeciesCode').uniq().value();
            return uniqueCodes.length !== $ctrl.speciesAmountList.length;
        }

        function buildAvailableSpecies() {
            var selectedSpeciesCodes = _.map($ctrl.speciesAmountList, function (spa) {
                return spa.gameSpeciesCode;
            });

            return _.chain(Species.getPermitSpeciesWithoutAlienSpecies())
                .filter(function (species) {
                    return !_.includes(selectedSpeciesCodes, species.code);
                })
                .map(function (species) {
                    return TranslatedSpecies.translateSpecies(species);
                })
                .sortBy('name')
                .value();
        }

    });
