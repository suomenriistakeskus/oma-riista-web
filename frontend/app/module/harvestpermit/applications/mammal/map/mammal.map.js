'use strict';

angular.module('app.harvestpermit.application.mammal.map', ['app.metadata', 'app.harvestpermit.area'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mammal.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/wizard/area/map.html',
                controller: 'DerogationPermitWizardMapController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    areaInfo: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getArea({id: applicationId}).$promise;
                    },
                    attachmentList: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'species',
                            next: 'mapdetails'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.mammal.map', {
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
                            previous: 'species',
                            next: 'mapdetails'
                        };
                    }
                }
            })
            .state('profile.permitwizard.mammal.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/wizard/area/mapdetails.html',
                controller: 'DerogationPermitWizardMapDetailsController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    areaInfo: function (DerogationPermitApplication, applicationId) {
                        return DerogationPermitApplication.getArea({id: applicationId}).$promise;
                    },
                    attachmentList: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'map',
                            next: 'reasons'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.mammal.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/wizard/area/map.html',
                controller: 'DerogationPermitWizardMapDetailsController',
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
                            previous: 'map',
                            next: 'reasons'
                        };
                    }
                }
            });
    });
