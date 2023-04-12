'use strict';

angular.module('app.harvestpermit.application.dogunleash.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.dogunleash.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/dogunleash/attachments/attachments.html',
                controller: 'DerogationPermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                hideFooter: true,
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
                            previous: 'eventdetails',
                            next: 'summary'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogunleash.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/dogunleash/attachments/attachments.html',
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
                            previous: 'eventdetails',
                            next: 'summary'
                        };
                    }
                }
            });
    });
