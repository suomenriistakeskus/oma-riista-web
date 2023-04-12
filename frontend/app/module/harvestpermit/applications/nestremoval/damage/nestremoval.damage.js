'use strict';

angular.module('app.harvestpermit.application.nestremoval.damage', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.nestremoval.damage', {
                url: '/damage',
                templateUrl: 'harvestpermit/applications/wizard/damage/damage.html',
                controller: 'DerogationPermitWizardDamageController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesDamageList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesDamage({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'population'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.nestremoval.damage', {
                url: '/damage',
                templateUrl: 'harvestpermit/applications/wizard/damage/damage.html',
                controller: 'DerogationPermitWizardDamageController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesDamageList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getSpeciesDamage({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'population'
                        };
                    }
                }
            });
    });
