'use strict';

angular.module('app.harvestpermit.application.mammal.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mammal.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/mammal/species/species.html',
                controller: 'MammalPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmountList: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.mammal.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/mammal/species/species.html',
                controller: 'MammalPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmountList: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
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

    .controller('MammalPermitWizardSpeciesController', function ($scope, MammalPermitApplication,
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
                amount: 0
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
            return MammalPermitApplication.saveSpeciesAmounts({id: applicationId}, {list: $ctrl.speciesAmountList}).$promise;
        };

        function invalid(form) {
            return form.$invalid || _.isEmpty($ctrl.speciesAmountList) ||
                duplicateSpeciesCodes() || (isMultipleSpeciesForbidden() && $ctrl.speciesAmountList.length > 1);
        }

        function duplicateSpeciesCodes() {
            var uniqueCodes = _.chain($ctrl.speciesAmountList).map('gameSpeciesCode').uniq().value();
            return uniqueCodes.length !== $ctrl.speciesAmountList.length;
        }

        function isMultipleSpeciesForbidden() {
            return !!_.find($ctrl.speciesAmountList, function (species) {
                return isMultipleSpeciesForbiddenFor(species.gameSpeciesCode);
            });
        }

        function buildAvailableSpecies() {

            // Current species selection does not allow other species to be added
            if (isMultipleSpeciesForbidden()) {
                return [];
            }

            var selectedSpeciesCodes = _.map($ctrl.speciesAmountList, function (spa) {
                return spa.gameSpeciesCode;
            });

            return _.chain(Species.getMammalPermitSpecies())
                .filter(function (species) {
                    return !isMultipleSpeciesForbiddenFor(species.code) || selectedSpeciesCodes.length === 0;
                })
                .filter(function (species) {
                    return !_.includes(selectedSpeciesCodes, species.code);
                })
                .map(function (species) {
                    return TranslatedSpecies.translateSpecies(species);
                })
                .sortBy('name')
                .value();

        }

        function isMultipleSpeciesForbiddenFor(speciesCode) {
            // Carnivore species and otter needs to be applied for separately
            return GameSpeciesCodes.isCarnivoreSpecies(speciesCode) ||
                GameSpeciesCodes.isOtter(speciesCode);
        }

    });
