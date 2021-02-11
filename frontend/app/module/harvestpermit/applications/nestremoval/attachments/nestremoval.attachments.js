'use strict';

angular.module('app.harvestpermit.application.nestremoval.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.nestremoval.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/nestremoval/attachments/attachments.html',
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
                            previous: 'population',
                            next: 'summary'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.nestremoval.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/nestremoval/attachments/attachments.html',
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
                            previous: 'population',
                            next: 'summary'
                        };
                    }
                }
            });
    });
