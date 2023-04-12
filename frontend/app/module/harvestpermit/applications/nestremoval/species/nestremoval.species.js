'use strict';

angular.module('app.harvestpermit.application.nestremoval.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.nestremoval.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/nestremoval/species/species.html',
                controller: 'NestRemovalPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesAmountList: function (NestRemovalPermitApplication, applicationId) {
                        return NestRemovalPermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.nestremoval.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/nestremoval/species/species.html',
                controller: 'NestRemovalPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmountList: function (NestRemovalPermitApplication, applicationId) {
                        return NestRemovalPermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
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

    .controller('NestRemovalPermitWizardSpeciesController', function ($scope, NestRemovalPermitApplication,
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
                gameSpeciesCode: speciesCode
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
                if (spa.nestAmount === 0) {
                    spa.nestAmount = null;
                }
                if (spa.eggAmount === 0) {
                    spa.eggAmount = null;
                }
                if (spa.constructionAmount === 0) {
                    spa.constructionAmount = null;
                }
            });
            return NestRemovalPermitApplication.saveSpeciesAmounts({id: applicationId}, {list: $ctrl.speciesAmountList}).$promise;
        };

        function invalid(form) {
            var speciesWithAmount = _.filter($ctrl.speciesAmountList, function (spa) {
                return (spa.nestAmount && spa.nestAmount > 0) ||
                    (spa.eggAmount && spa.eggAmount > 0) ||
                    (spa.constructionAmount && spa.constructionAmount > 0);
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

            return _.chain(Species.getNestRemovalPermitSpecies())
                .filter(function (species) {
                    return !_.includes(selectedSpeciesCodes, species.code);
                })
                .map(function (species) {
                    return TranslatedSpecies.translateSpecies(species);
                })
                .sortBy('name')
                .value();
        }

        $ctrl.isBirdSpecies = function(speciesCode) {
            return Species.isBirdPermitSpecies(speciesCode);
        };

    });
