'use strict';

angular.module('app.harvestpermit.application.bird.methods', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/bird/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'damage'
                        };
                    },
                    updateResource: function (BirdPermitApplication) {
                        return BirdPermitApplication;
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/bird/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'damage'
                        };
                    },
                    updateResource: function (BirdPermitApplication) {
                        return BirdPermitApplication;
                    }
                }
            });
    })
;
