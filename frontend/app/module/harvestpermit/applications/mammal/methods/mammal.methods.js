'use strict';

angular.module('app.harvestpermit.application.mammal.methods', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mammal.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/mammal/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'damage'
                        };
                    },
                    updateResource: function (MammalPermitApplication) {
                        return MammalPermitApplication;
                    }
                }
            })
            .state('jht.decision.application.wizard.mammal.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/mammal/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'damage'
                        };
                    },
                    updateResource: function (MammalPermitApplication) {
                        return MammalPermitApplication;
                    }
                }
            });
    })
;
