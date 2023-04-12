'use strict';

angular.module('app.harvestpermit.application.nestremoval.population', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.nestremoval.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/nestremoval/population/population.html',
                controller: 'DerogationPermitWizardPopulationController',
                controllerAs: '$ctrl',
                hideFooter: true,
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
            .state('jht.decision.application.wizard.nestremoval.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/nestremoval/population/population.html',
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
    });
