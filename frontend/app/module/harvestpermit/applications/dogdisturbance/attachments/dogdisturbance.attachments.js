'use strict';

angular.module('app.harvestpermit.application.dogdisturbance.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.dogdisturbance.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/dogdisturbance/attachments/attachments.html',
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
                            previous: 'testdetails',
                            next: 'summary'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogdisturbance.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/dogdisturbance/attachments/attachments.html',
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
                            previous: 'testdetails',
                            next: 'summary'
                        };
                    }
                }
            });
    });
