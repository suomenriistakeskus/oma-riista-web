'use strict';

angular.module('app.harvestpermit.application.gamemanagement.methods', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.gamemanagement.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/gamemanagement/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'justification',
                            next: 'attachments'
                        };
                    },
                    updateResource: function (GameManagementPermitApplication) {
                        return GameManagementPermitApplication;
                    }
                }
            })
            .state('jht.decision.application.wizard.gamemanagement.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/gamemanagement/methods/methods.html',
                controller: 'DerogationPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'justification',
                            next: 'attachments'
                        };
                    },
                    updateResource: function (GameManagementPermitApplication) {
                        return GameManagementPermitApplication;
                    }
                }
            });
    })
;
