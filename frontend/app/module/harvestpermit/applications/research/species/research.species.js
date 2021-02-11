'use strict';

angular.module('app.harvestpermit.application.research.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.research.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/research/species/species.html',
                controller: 'ResearchPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmountList: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.research.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/research/species/species.html',
                controller: 'ResearchPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmountList: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
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

    .controller('ResearchPermitWizardSpeciesController', function ($scope, ResearchPermitApplication,
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
            return ResearchPermitApplication.saveSpeciesAmounts({id: applicationId}, {list: $ctrl.speciesAmountList}).$promise;
        };

        function invalid(form) {
            return form.$invalid || _.isEmpty($ctrl.speciesAmountList) || duplicateSpeciesCodes();
        }

        function duplicateSpeciesCodes() {
            var uniqueCodes = _.chain($ctrl.speciesAmountList).map('gameSpeciesCode').uniq().value();
            return uniqueCodes.length !== $ctrl.speciesAmountList.length;
        }

        function buildAvailableSpecies() {
            var selectedSpeciesCodes = _.map($ctrl.speciesAmountList, function (spa) {
                return spa.gameSpeciesCode;
            });

            return _.chain(Species.getAllPermitSpecies())
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
