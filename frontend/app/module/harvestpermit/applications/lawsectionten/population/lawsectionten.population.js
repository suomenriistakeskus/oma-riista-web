'use strict';

angular.module('app.harvestpermit.application.lawsectionten.population', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.lawsectionten.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/lawsectionten/population/population.html',
                controller: 'DerogationPermitWizardPopulationController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPopulationList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.lawsectionten.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/lawsectionten/population/population.html',
                controller: 'DerogationPermitWizardPopulationController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPopulationList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'attachments'
                        };
                    }
                }
            });
    })
;
