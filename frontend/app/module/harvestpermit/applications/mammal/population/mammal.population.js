'use strict';

angular.module('app.harvestpermit.application.mammal.population', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mammal.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/mammal/population/population.html',
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
            .state('jht.decision.application.wizard.mammal.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/mammal/population/population.html',
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
