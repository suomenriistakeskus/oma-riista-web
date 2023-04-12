'use strict';

angular.module('app.harvestpermit.application.bird.damage', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.damage', {
                url: '/damage',
                templateUrl: 'harvestpermit/applications/bird/damage/damage.html',
                controller: 'DerogationPermitWizardDamageController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesDamageList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesDamage({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'methods',
                            next: 'population'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.damage', {
                url: '/damage',
                templateUrl: 'harvestpermit/applications/bird/damage/damage.html',
                controller: 'DerogationPermitWizardDamageController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesDamageList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesDamage({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'methods',
                            next: 'population'
                        };
                    }
                }
            });
    })
;
