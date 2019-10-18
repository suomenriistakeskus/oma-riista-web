'use strict';

angular.module('app.harvestpermit.management', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('permitmanagement', {
                url: '/permitmanagement/{permitId:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/management/layout.html',
                abstract: true,
                resolve: {
                    permitId: function ($stateParams) {
                        return _.parseInt($stateParams.permitId);
                    },
                    permit: function ($state, HarvestPermits, permitId) {
                        return HarvestPermits.get({id: permitId}).$promise.then(function (p) {
                            if (p.permitTypeCode === '190') {
                                return $state.go('permitmanagement.dashboard', {permitId: p.originalPermitId});
                            }
                            return p;
                        });
                    },
                    isMooselikePermit: function (permit) {
                        return permit.permitTypeCode === '100';
                    },
                    getGameSpeciesName: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise.then(function (parameters) {
                            return parameters.$getGameName;
                        });
                    },
                    duePayments: function (PermitInvoice, permitId) {
                        return PermitInvoice.listDueByPermit({permitId: permitId}).$promise;
                    },
                    duePaymentsCount: function (duePayments) {
                        return _.size(duePayments);
                    },
                    partiallyPaidInvoiceCount: function (PermitInvoice, permitId) {
                        return PermitInvoice.listPaidByPermit({permitId: permitId}).$promise
                            .then(function (paidInvoices) {
                                return _.chain(paidInvoices)
                                    .filter(function (invoice) {
                                        return invoice.amount !== invoice.paidAmount;
                                    })
                                    .size()
                                    .value();
                            });
                    }
                },
                controllerAs: '$navCtrl',
                controller: function (isMooselikePermit, duePaymentsCount, partiallyPaidInvoiceCount) {
                    var $navCtrl = this;

                    $navCtrl.$onInit = function () {
                        $navCtrl.isMooselikePermit = isMooselikePermit;
                        $navCtrl.paymentAlertCount = duePaymentsCount + partiallyPaidInvoiceCount;
                    };
                }
            })
            .state('permitmanagement.dashboard', {
                url: '/dashboard?gameSpeciesCode',
                templateUrl: 'harvestpermit/management/dashboard.html',
                controller: 'HarvestPermitDashboardController',
                controllerAs: '$ctrl',
                params: {
                    gameSpeciesCode: null
                },
                resolve: {
                    selectedGameSpeciesCode: function (permit, $stateParams) {
                        permit.gameSpeciesCodes.sort();

                        var gameSpeciesCode = _.parseInt($stateParams.gameSpeciesCode);
                        return gameSpeciesCode ? gameSpeciesCode : _.head(permit.gameSpeciesCodes);
                    },
                    attachmentList: function (HarvestPermits, permitId) {
                        return HarvestPermits.getAttachmentList({id: permitId}).$promise;
                    },
                    duePayment: function (duePayments) {
                        return _.chain(duePayments)
                            .sortBy(['dueDate'])
                            .head()
                            .value();
                    }
                }
            })
            .state('permitmanagement.usage', {
                url: '/harvests',
                templateUrl: 'harvestpermit/management/usage/permit-usage.html',
                controller: 'HarvestPermitUsageController',
                controllerAs: '$ctrl',
                resolve: {
                    permitUsage: function (HarvestPermits, permitId) {
                        return HarvestPermits.getSpeciesUsage({id: permitId}).$promise;
                    },
                    harvestList: function (HarvestPermits, Harvest, permitId) {
                        return HarvestPermits.getHarvestList({id: permitId}).$promise.then(function (data) {
                            return _.chain(data)
                                .map(function (harvest) {
                                    var harvestReportDate = moment(harvest.harvestReportDate, "YYYY-MM-DD[T]HH:mm:ss.SSS");
                                    harvest.harvestReportDeltaHours = moment().diff(harvestReportDate, 'hours');

                                    return new Harvest(harvest);
                                })
                                .orderBy(['pointOfTime'], ['desc'])
                                .value();
                        });
                    }
                }
            })
            .state('permitmanagement.map', {
                url: '/map?gameSpeciesCode&huntingYear',
                templateUrl: 'harvestpermit/moosepermit/map/permit-map.html',
                controller: 'MoosePermitMapController',
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    club: _.constant({rhy: {}}),
                    huntingYear: function ($stateParams) {
                        return _.parseInt($stateParams.huntingYear);
                    },
                    gameSpeciesCode: function ($stateParams) {
                        return _.parseInt($stateParams.gameSpeciesCode);
                    },
                    harvests: function (MoosePermitHarvest, permitId, huntingYear, gameSpeciesCode) {
                        return MoosePermitHarvest.query({
                            permitId: permitId,
                            huntingYear: huntingYear,
                            gameSpeciesCode: gameSpeciesCode
                        }).$promise;
                    },
                    featureCollection: function (MoosePermits, permitId, huntingYear, gameSpeciesCode) {
                        return MoosePermits.partnerAreaFeatures({
                            permitId: permitId,
                            huntingYear: huntingYear,
                            gameSpeciesCode: gameSpeciesCode
                        }).$promise;
                    },
                    mapBounds: function (MapBounds, club, featureCollection) {
                        var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        return bounds || MapBounds.getBoundsOfFinland();
                    },
                    goBackFn: function ($state, permitId, gameSpeciesCode) {
                        return function () {
                            $state.go('permitmanagement.dashboard', {
                                permitId: permitId,
                                gameSpeciesCode: gameSpeciesCode
                            }, {reload: true});
                        };
                    }
                }
            })
            .state('permitmanagement.amendment', {
                url: '/amendment/{applicationId:[0-9]{1,8}}?decisionId',
                templateUrl: 'harvestpermit/applications/amendment/amendment.html',
                controller: 'HarvestPermitAmendmentController',
                controllerAs: '$ctrl',
                resolve: {
                    applicationId: function ($stateParams) {
                        return _.parseInt($stateParams.applicationId);
                    },
                    decisionId: function ($stateParams) {
                        return $stateParams.decisionId;
                    },
                    application: function (HarvestPermitAmendmentApplications, applicationId) {
                        return HarvestPermitAmendmentApplications.get({id: applicationId}).$promise;
                    },
                    attachments: function (HarvestPermitApplications, applicationId) {
                        return applicationId ? HarvestPermitApplications.getAttachments({id: applicationId}).$promise : [];
                    },
                    species: function (HarvestPermitAmendmentApplications, permitId) {
                        return HarvestPermitAmendmentApplications.listSpecies({id: permitId}).$promise;
                    },
                    partners: function (HarvestPermitAmendmentApplications, permitId) {
                        return HarvestPermitAmendmentApplications.listPartners({id: permitId}).$promise;
                    },
                    diaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    }
                }
            });
    })
    .controller('HarvestPermitDashboardController', function ($state, $location, FormPostService, NotificationService,
                                                              EditHarvestPermitContactPersonsModal,
                                                              HarvestPermitPdfUrl, HarvestPermitAttachmentUrl,
                                                              MoosePermitLeadersService, ActiveRoleService,
                                                              PermitEndOfHuntingReportModal,
                                                              HarvestPermitAmendmentApplicationService,
                                                              permit, isMooselikePermit, attachmentList, duePayment,
                                                              getGameSpeciesName, selectedGameSpeciesCode,
                                                              partiallyPaidInvoiceCount) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.isModerator = ActiveRoleService.isModerator();
            $ctrl.permit = permit;
            $ctrl.attachmentList = attachmentList;
            $ctrl.duePayment = duePayment;
            $ctrl.isMooselikePermit = isMooselikePermit;
            $ctrl.huntingYear = _.parseInt(permit.permitNumber.substring(0, 4));
            $ctrl.getGameSpeciesName = getGameSpeciesName;
            $ctrl.selectedGameSpeciesCode = selectedGameSpeciesCode;
            $ctrl.partiallyPaidInvoiceCount = partiallyPaidInvoiceCount;

            $ctrl.getSelectedSpeciesName = function () {
                return getGameSpeciesName($ctrl.selectedGameSpeciesCode);
            };
        };

        var reloadState = function () {
            $state.reload();
        };

        $ctrl.changeGameSpeciesCode = function (gameSpeciesCode) {
            $ctrl.selectedGameSpeciesCode = gameSpeciesCode;
            $location.search({gameSpeciesCode: gameSpeciesCode});
        };

        $ctrl.downloadPdf = function (permitNumber) {
            FormPostService.submitFormUsingBlankTarget(HarvestPermitPdfUrl.get(permitNumber));
        };

        $ctrl.downloadAttachment = function (a) {
            FormPostService.submitFormUsingBlankTarget(HarvestPermitAttachmentUrl.get(permit.id, a.id));
        };

        $ctrl.startPayment = function () {
            $state.go('permitmanagement.payment.confirmation', {
                permitId: permit.id,
                invoiceId: duePayment.id
            });
        };

        $ctrl.editContactPersons = function () {
            EditHarvestPermitContactPersonsModal.showModal($ctrl.permit.id).finally(reloadState);
        };

        $ctrl.editAllocations = function () {
            $state.go('permitmanagement.allocation', {
                permitId: permit.id,
                gameSpeciesCode: $ctrl.selectedGameSpeciesCode
            });
        };

        $ctrl.showHuntingGroupLeaders = function () {
            MoosePermitLeadersService.showLeaders({
                id: permit.id,
                huntingYear: $ctrl.huntingYear,
                gameSpeciesCode: $ctrl.selectedGameSpeciesCode
            });
        };

        $ctrl.showMap = function () {
            $state.go('permitmanagement.map', {
                permitId: permit.id,
                gameSpeciesCode: $ctrl.selectedGameSpeciesCode,
                huntingYear: $ctrl.huntingYear
            });
        };

        $ctrl.showUsage = function () {
            var currentState = $state.current;
            $state.go('permitmanagement.usage', {
                permitId: permit.id
            }).catch(function () {
                $state.go(currentState.name, currentState.params);
                NotificationService.showDefaultFailure();
            });
        };

        $ctrl.showTables = function () {
            $state.go('permitmanagement.tables', {
                permitId: permit.id,
                gameSpeciesCode: $ctrl.selectedGameSpeciesCode
            });
        };

        $ctrl.showEndOfHuntingReport = function () {
            PermitEndOfHuntingReportModal.openModal(permit.id).finally(reloadState);
        };

        $ctrl.endHuntingForMooselikePermit = function () {
            $state.go('permitmanagement.endofmooselikehunting', {
                permitId: permit.id,
                gameSpeciesCode: $ctrl.selectedGameSpeciesCode
            });
        };

        $ctrl.listAmendmentApplications = function () {
            HarvestPermitAmendmentApplicationService.openModal(permit);
        };
    });
