'use strict';

angular.module('app.harvestpermit.application.deportation.methods', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.deportation.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/deportation/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    forbiddenMethods: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'damage'
                        };
                    },
                    updateResource: function (DeportationPermitApplication) {
                        return DeportationPermitApplication;
                    }
                }
            })
            .state('jht.decision.application.wizard.deportation.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/deportation/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'damage'
                        };
                    },
                    updateResource: function (DeportationPermitApplication) {
                        return DeportationPermitApplication;
                    }
                }
            });
    })
;
