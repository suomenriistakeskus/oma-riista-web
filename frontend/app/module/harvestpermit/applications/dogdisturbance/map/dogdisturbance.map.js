'use strict';

angular.module('app.harvestpermit.application.dogdisturbance.map', ['app.metadata', 'app.harvestpermit.area'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.dogdisturbance.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/dogdisturbance/map/map.html',
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
                            previous: 'applicant',
                            next: 'mapdetails'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogdisturbance.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/dogdisturbance/map/map.html',
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
                            previous: 'applicant',
                            next: 'mapdetails'
                        };
                    }
                }
            })
            .state('profile.permitwizard.dogdisturbance.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/dogdisturbance/map/mapdetails.html',
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
                            next: 'trainingdetails'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogdisturbance.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/dogdisturbance/map/mapdetails.html',
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
                            next: 'trainingdetails'
                        };
                    }
                }
            });
    });
