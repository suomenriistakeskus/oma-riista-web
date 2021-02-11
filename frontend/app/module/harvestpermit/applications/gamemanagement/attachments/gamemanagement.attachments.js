'use strict';

angular.module('app.harvestpermit.application.gamemanagement.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.gamemanagement.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/gamemanagement/attachments/attachments.html',
                controller: 'DerogationPermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                resolve: {
                    attachmentList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.listAttachments({
                            id: applicationId,
                            typeFilter: 'OTHER'
                        }).$promise.then(function (res) {
                            return _.sortBy(res, 'id');
                        });
                    },
                    states: function () {
                        return {
                            previous: 'methods',
                            next: 'summary'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.gamemanagement.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/gamemanagement/attachments/attachments.html',
                controller: 'DerogationPermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                resolve: {
                    attachmentList: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.listAttachments({
                            id: applicationId,
                            typeFilter: 'OTHER'
                        }).$promise.then(function (res) {
                            return _.sortBy(res, 'id');
                        });
                    },
                    states: function () {
                        return {
                            previous: 'methods',
                            next: 'summary'
                        };
                    }
                }
            });
    })

;
