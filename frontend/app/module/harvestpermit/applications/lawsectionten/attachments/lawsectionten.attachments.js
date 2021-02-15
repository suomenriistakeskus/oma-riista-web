'use strict';

angular.module('app.harvestpermit.application.lawsectionten.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.lawsectionten.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/lawsectionten/attachments/attachments.html',
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
            .state('jht.decision.application.wizard.lawsectionten.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/lawsectionten/attachments/attachments.html',
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
    })

;
