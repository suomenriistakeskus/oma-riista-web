'use strict';

angular.module('app.harvestpermit.application.research.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.research.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/wizard/derogation/attachments/attachments.html',
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
                            previous: 'justification',
                            next: 'summary'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.research.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/wizard/derogation/attachments/attachments.html',
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
                            previous: 'justification',
                            next: 'summary'
                        };
                    }
                }
            });
    });
