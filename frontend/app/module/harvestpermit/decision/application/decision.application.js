'use strict';

angular.module('app.harvestpermit.decision.application', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.decision.application', {
                url: '/application',
                template: '<div ui-view autoscroll="false"></div>',
                abstract: true,
                resolve: {
                    applicationId: function (decisionId, PermitDecision) {
                        return PermitDecision.getApplication({id: decisionId}).$promise.then(function (res) {
                            return res.id;
                        });
                    }
                }
            })
            .state('jht.decision.application.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/decision/application/application.html',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.getFullDetails({id: applicationId}).$promise;
                    },
                    decision: function (PermitDecision, decisionId) {
                        return PermitDecision.get({id: decisionId}).$promise;
                    },
                    permitArea: function (HarvestPermitApplications, application) {
                        return HarvestPermitApplications.getArea({
                            id: application.id
                        }).$promise;
                    },
                    diaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, HarvestPermitApplications, FormPostService,
                                      PermitDecision, NotificationService,
                                      decisionId, application, decision, permitArea, diaryParameters) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.application = application;
                        $ctrl.decision = decision;
                        $ctrl.permitArea = permitArea;
                        $ctrl.diaryParameters = diaryParameters;
                        $ctrl.baseUri = '/api/v1/harvestpermit/application/' + application.id;
                    };

                    $ctrl.isLateApplication = function () {
                        if ($ctrl.application.submitDate) {
                            var submitDate = moment($ctrl.application.submitDate, 'YYYY-MM-DD');

                            if (submitDate.isValid()) {
                                var month = submitDate.month() + 1; // zero indexed
                                return month > 4; // Submitted after 30.4
                            }
                        }
                        return false;
                    };


                    $ctrl.canEditApplication = function () {
                        return $ctrl.decision.userIsHandler &&
                            $ctrl.decision.status !== 'LOCKED' &&
                            $ctrl.decision.status !== 'PUBLISHED';
                    };

                    $ctrl.resolveUnifiedStatus = function () {
                        if (application.status === 'AMENDING') {
                            return 'AMENDING';
                        }
                        if (application.status === 'ACTIVE' && !decision.handler) {
                            return 'ACTIVE';
                        }
                        return decision.status;
                    };

                    $ctrl.editApplication = function () {
                        HarvestPermitApplications.startAmending({id: application.id}).$promise.then(function () {
                            $state.go('jht.decision.application.wizard.applicant', {
                                decisionId: decisionId
                            });
                        });
                    };

                    $ctrl.loadOriginalZip = function () {
                        FormPostService.submitFormUsingBlankTarget($ctrl.baseUri + '/archive');
                    };

                    $ctrl.loadApplicationPdf = function () {
                        FormPostService.submitFormUsingBlankTarget($ctrl.baseUri + '/print/pdf');
                    };

                    $ctrl.assign = function () {
                        PermitDecision.assign({id: decisionId}).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            $state.reload();
                        });
                    };
                }
            })

            // WIZARD

            .state('jht.decision.application.wizard', {
                url: '/wizard',
                templateUrl: 'harvestpermit/applications/wizard/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    applicationBasicDetails: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    },
                    wizard: function ($state, decisionId) {
                        return {
                            isAmending: _.constant(true),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('jht.decision.application.summary');
                            },
                            goto: function (childState, params) {
                                $state.go('jht.decision.application.wizard.' + childState, _.extend({
                                    decisionId: decisionId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/wizard/applicant.html',
                controller: 'HarvestPermitWizardApplicantController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/wizard/species.html',
                controller: 'HarvestPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    gameDiaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    },
                    speciesAmounts: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.listSpeciesAmounts({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.partners', {
                url: '/partners',
                templateUrl: 'harvestpermit/applications/wizard/partners.html',
                controller: 'HarvestPermitWizardPartnersController',
                controllerAs: '$ctrl',
                resolve: {
                    partners: function (HarvestPermitApplicationAreaPartners, applicationId) {
                        return HarvestPermitApplicationAreaPartners.query({
                            applicationId: applicationId
                        }).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/wizard/map.html',
                controller: 'HarvestPermitWizardMapController',
                controllerAs: '$ctrl',
                resolve: {
                    permitArea: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getArea({
                            id: applicationId
                        }).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/wizard/attachments.html',
                controller: 'HarvestPermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                resolve: {
                    attachments: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    shooterCounts: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getShooterCounts({id: applicationId}).$promise;
                    },
                    freeHunting: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getArea({
                            id: applicationId
                        }).$promise.then(function (res) {
                            return res.freeHunting;
                        });
                    }
                }
            })
            .state('jht.decision.application.wizard.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/wizard/summary.html',
                controller: 'HarvestPermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.getFullDetails({id: applicationId}).$promise;
                    },
                    isLate: _.constant(false),
                    permitArea: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getArea({
                            id: applicationId
                        }).$promise;
                    },
                    diaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    }
                }
            });
    });
