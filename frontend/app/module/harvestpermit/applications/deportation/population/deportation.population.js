'use strict';

angular.module('app.harvestpermit.application.deportation.population', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.deportation.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/deportation/population/population.html',
                controller: 'DerogationPermitWizardPopulationController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPopulationList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'damage',
                            next: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.deportation.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/deportation/population/population.html',
                controller: 'DerogationPermitWizardPopulationController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPopulationList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'damage',
                            next: 'attachments'
                        };
                    }
                }
            });
    })
;
