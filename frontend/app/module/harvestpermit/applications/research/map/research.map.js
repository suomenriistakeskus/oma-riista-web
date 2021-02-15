'use strict';

angular.module('app.harvestpermit.application.research.map', ['app.metadata', 'app.harvestpermit.area'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.research.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/research/map/map.html',
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
            .state('jht.decision.application.wizard.research.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/research/map/map.html',
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
            .state('profile.permitwizard.research.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/wizard/area/mapdetails.html',
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
                            next: 'period'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.research.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/wizard/area/mapdetails.html',
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
                            next: 'period'
                        };
                    }
                }
            });
    });
