'use strict';

angular.module('app.harvestpermit.application.carnivore.map', ['app.metadata', 'app.harvestpermit.area'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.carnivore.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/wizard/area/map.html',
                controller: 'DerogationPermitWizardMapController',
                controllerAs: '$ctrl',
                resolve: {
                    areaInfo: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getArea({id: applicationId}).$promise;
                    },
                    attachmentList: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'justification',
                            next: 'attachments'
                        };
                    }
                }
            }).state('jht.decision.application.wizard.carnivore.map', {
            url: '/map',
            templateUrl: 'harvestpermit/applications/wizard/area/map.html',
            controller: 'DerogationPermitWizardMapController',
            controllerAs: '$ctrl',
            resolve: {
                areaInfo: function (DerogationPermitApplication, applicationId) {
                    return DerogationPermitApplication.getArea({id: applicationId}).$promise;
                },
                attachmentList: function (HarvestPermitApplications, applicationId) {
                    return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                },
                states: function () {
                    return {
                        previous: 'justification',
                        next: 'attachments'
                    };
                }
            }
        });
    });
