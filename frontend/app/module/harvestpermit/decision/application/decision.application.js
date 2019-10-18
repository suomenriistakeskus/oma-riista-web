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
            .state('jht.decision.application.wizard', {
                url: '/wizard',
                template: '<div ui-view autoscroll="false"></div>',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    applicationBasicDetails: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.overview', {
                url: '/summary',
                templateUrl: 'harvestpermit/decision/application/application.html',
                resolve: {
                    decision: function (PermitDecision, decisionId) {
                        return PermitDecision.get({id: decisionId}).$promise;
                    },
                    category: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise.then(function (app) {
                            return app.harvestPermitCategory;
                        });
                    },
                    applicationSummary: function (HarvestPermitApplicationSummaryService,
                                                  category, applicationId) {
                        return HarvestPermitApplicationSummaryService.getApplicationSummary(applicationId, category);
                    },
                    permitArea: function (MooselikePermitApplication, applicationId, category) {
                        if (category === 'MOOSELIKE' || category === 'MOOSELIKE_NEW') {
                            return MooselikePermitApplication.getArea({id: applicationId}).$promise;
                        }
                        return null;
                    },
                    mooselikeAmendment: function (HarvestPermitAmendmentApplications, applicationId, category) {
                        if (category === 'MOOSELIKE_NEW') {
                            return HarvestPermitAmendmentApplications.get({id: applicationId}).$promise;
                        }

                        return null;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, HarvestPermitApplications,
                                      PermitDecision, NotificationService, FetchAndSaveBlob,
                                      PermitDecisionActionListModal, PermitDecisionActionReadonlyListModal,
                                      HarvestPermitAmendmentApplications, PermitDecisionAppealSettingsModal,
                                      HarvestPermitWizardSelectorService,
                                      decisionId, applicationId, category,
                                      applicationSummary, decision, permitArea, mooselikeAmendment) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.applicationSummary = applicationSummary;
                        $ctrl.decision = decision;
                        $ctrl.permitArea = permitArea;
                        $ctrl.mooselikeAmendment = mooselikeAmendment;
                        $ctrl.baseUri = '/api/v1/harvestpermit/application/' + decision.applicationId;
                    };

                    $ctrl.isLateApplication = function () {
                        if (category === 'MOOSELIKE' && $ctrl.applicationSummary.submitDate) {
                            var submitDate = moment($ctrl.applicationSummary.submitDate, 'YYYY-MM-DD');

                            if (submitDate.isValid()) {
                                var month = submitDate.month() + 1; // zero indexed
                                return month > 4; // Submitted after 30.4
                            }
                        }
                        return false;
                    };

                    $ctrl.showActions = function () {
                        if ($ctrl.decision.userIsHandler && $ctrl.decision.status === 'DRAFT') {
                            PermitDecisionActionListModal.open(decisionId);
                        } else {
                            PermitDecisionActionReadonlyListModal.open(decisionId);
                        }
                    };

                    $ctrl.canEditApplication = function () {
                        var applicationStatus = $ctrl.applicationSummary.status;

                        return $ctrl.decision.userIsHandler && $ctrl.decision.status === 'DRAFT' && applicationStatus !== 'DRAFT';
                    };

                    $ctrl.resolveUnifiedStatus = function () {
                        if (applicationSummary.status === 'AMENDING') {
                            return applicationSummary.status;
                        }

                        if (applicationSummary.status === 'ACTIVE' && !decision.handler) {
                            return 'ACTIVE';
                        }

                        return decision.status;
                    };

                    $ctrl.editApplication = function () {
                        HarvestPermitApplications.startAmending({id: applicationId}).$promise.then(function () {
                            if (category === 'MOOSELIKE_NEW') {
                                HarvestPermitAmendmentApplications.get({id: applicationId}).$promise.then(function (a) {
                                    return $state.go('permitmanagement.amendment', {
                                        permitId: a.originalPermitId,
                                        applicationId: applicationId,
                                        decisionId: decisionId
                                    });
                                });
                            } else {
                                var wizardName = HarvestPermitWizardSelectorService.getWizardName(category);
                                $state.go('jht.decision.application.wizard.' + wizardName + '.applicant', {
                                    decisionId: decisionId
                                });
                            }

                        });
                    };

                    $ctrl.loadOriginalZip = function () {
                        FetchAndSaveBlob.post($ctrl.baseUri + '/archive');
                    };

                    $ctrl.loadApplicationPdf = function () {
                        FetchAndSaveBlob.post($ctrl.baseUri + '/print/pdf');
                    };

                    $ctrl.editAppealSettings = function () {
                        PermitDecisionAppealSettingsModal.open(decisionId).then(function () {
                            $state.reload();
                        });
                    };
                }
            });
    });
