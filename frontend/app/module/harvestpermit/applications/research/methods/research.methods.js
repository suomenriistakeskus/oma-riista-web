'use strict';

angular.module('app.harvestpermit.application.research.methods', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.research.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/research/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'justification'
                        };
                    },
                    updateResource: function (ResearchPermitApplication) {
                        return ResearchPermitApplication;
                    }
                }
            })
            .state('jht.decision.application.wizard.research.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/research/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'justification'
                        };
                    },
                    updateResource: function (ResearchPermitApplication) {
                        return ResearchPermitApplication;
                    }
                }
            });
    });
