'use strict';

angular.module('app.harvestpermit.application.lawsectionten.europeanbeaver', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider

            // BASE

            .state('profile.permitwizard.europeanbeaver', {
                url: '/europeanbeaver',
                templateUrl: 'harvestpermit/applications/lawsectionten/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    wizard: function ($state, applicationId, applicationBasicDetails) {
                        return {
                            isAmending: _.constant(applicationBasicDetails.status === 'AMENDING'),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('profile.permits');
                            },
                            goto: function (childState, params) {
                                $state.go('profile.permitwizard.europeanbeaver.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver', {
                url: '/europeanbeaver',
                templateUrl: 'harvestpermit/applications/lawsectionten/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    wizard: function ($state, applicationId, applicationBasicDetails) {
                        return {
                            isAmending: _.constant(applicationBasicDetails.status === 'AMENDING'),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('jht.decision.application.overview');
                            },
                            goto: function (childState, params) {
                                $state.go('jht.decision.application.wizard.europeanbeaver.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })

            // APPLICANT

            .state('profile.permitwizard.europeanbeaver.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/lawsectionten/applicant/applicant.html',
                controller: 'LawSectionTenPermitWizardApplicantController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/lawsectionten/applicant/applicant.html',
                controller: 'LawSectionTenPermitWizardApplicantController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            })

            // SPECIES

            .state('profile.permitwizard.europeanbeaver.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/lawsectionten/species/species.html',
                controller: 'LawSectionTenPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    harvestPermitCategory: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise.then(function (application) {
                            return application.harvestPermitCategory;
                        });
                    },
                    speciesAmount: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/lawsectionten/species/species.html',
                controller: 'LawSectionTenPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    harvestPermitCategory: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise.then(function (application) {
                            return application.harvestPermitCategory;
                        });
                    },
                    speciesAmount: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })

            // MAP

            .state('profile.permitwizard.europeanbeaver.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/lawsectionten/map/map.html',
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
            .state('jht.decision.application.wizard.europeanbeaver.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/lawsectionten/map/map.html',
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
            .state('profile.permitwizard.europeanbeaver.mapdetails', {
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
                            next: 'period'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver.mapdetails', {
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

            // PERIOD

            .state('profile.permitwizard.europeanbeaver.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/lawsectionten/period/period.html',
                controller: 'LawSectionTenPermitWizardPeriodController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesPeriod: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'population'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/lawsectionten/period/period.html',
                controller: 'LawSectionTenPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriod: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'population'
                        };
                    }
                }
            })




            // POPULATION

            .state('profile.permitwizard.europeanbeaver.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/lawsectionten/europeanbeaver/population.html',
                controller: 'EuropeanBeaverPopulationController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesPopulation: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver.population', {
                url: '/population',
                templateUrl: 'harvestpermit/applications/lawsectionten/europeanbeaver/population.html',
                controller: 'EuropeanBeaverPopulationController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPopulation: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesPopulation({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'period',
                            next: 'attachments'
                        };
                    }
                }
            })

            // ATTACHMENTS

            .state('profile.permitwizard.europeanbeaver.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/lawsectionten/attachments/attachments.html',
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
                            previous: 'population',
                            next: 'summary'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/lawsectionten/attachments/attachments.html',
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
                            previous: 'population',
                            next: 'summary'
                        };
                    }
                }
            })

            // SUMMARY

            .state('profile.permitwizard.europeanbeaver.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/lawsectionten/summary/summary.html',
                controller: 'LawSectionTenPermitWizardSummaryController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    application: function (applicationId, LawSectionTenPermitApplication) {
                        return LawSectionTenPermitApplication.getFullDetails({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.europeanbeaver.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/lawsectionten/summary/summary.html',
                controller: 'LawSectionTenPermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, LawSectionTenPermitApplication) {
                        return LawSectionTenPermitApplication.getFullDetails({id: applicationId}).$promise;
                    }
                }
            });
    })
;
