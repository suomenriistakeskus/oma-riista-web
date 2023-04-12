'use strict';

angular.module('app.harvestpermit.application.gamemanagement.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.gamemanagement.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/gamemanagement/species/species.html',
                controller: 'GameManagementPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesAmount: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'period'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.gamemanagement.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/gamemanagement/species/species.html',
                controller: 'GameManagementPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmount: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
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

    .controller('GameManagementPermitWizardSpeciesController', function ($scope, GameManagementPermitApplication,
                                                                         Species, TranslatedSpecies, GameSpeciesCodes,
                                                                         UnsavedChangesConfirmationService,
                                                                         ApplicationWizardNavigationHelper,
                                                                         states, wizard, applicationId, speciesAmount) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesAmount = speciesAmount || {};
            $ctrl.availableSpeciesOptions = getSpeciesList();
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

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return GameManagementPermitApplication.saveSpeciesAmount({id: applicationId}, $ctrl.speciesAmount).$promise;
        };

        function invalid(form) {
            return form.$invalid || !$ctrl.speciesAmount.gameSpeciesCode;
        }

        function getSpeciesList() {
            return _.chain(Species.getPermitSpeciesWithoutAlienSpecies())
                .map(TranslatedSpecies.translateSpecies)
                .sortBy('name')
                .value();
        }
    });
