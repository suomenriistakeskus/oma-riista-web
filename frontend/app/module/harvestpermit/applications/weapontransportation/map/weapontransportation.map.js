'use strict';

angular.module('app.harvestpermit.application.weapontransportation.map', ['app.metadata', 'app.harvestpermit.area'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.weapontransportation.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/weapontransportation/map/map.html',
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
                            previous: 'reason',
                            next: 'mapdetails'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.weapontransportation.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/weapontransportation/map/map.html',
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
                            previous: 'reason',
                            next: 'mapdetails'
                        };
                    }
                }
            })
            .state('profile.permitwizard.weapontransportation.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/weapontransportation/map/mapdetails.html',
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
                            next: 'justification'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.weapontransportation.mapdetails', {
                url: '/mapdetails',
                templateUrl: 'harvestpermit/applications/weapontransportation/map/mapdetails.html',
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
                            next: 'justification'
                        };
                    }
                }
            });
    });
