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
                    nestRemovalPermitUsages: function (NestRemovalPermitUsage, permitId) {
                        return NestRemovalPermitUsage.list({id: permitId}).$promise;
                    },
                    permitUsages: function (PermitUsage, permitId) {
                        return PermitUsage.list({id: permitId}).$promise;
                    },
                    isMooselikePermit: function (permit) {
                        return permit.permitTypeCode === '100';
                    },
                    isNestRemovalPermit: function (permit) {
                        return permit.permitTypeCode === '615';
                    },
                    isNonHarvestPermit: function (permit, PermitTypes) {
                        return _.includes(
                            [PermitTypes.DEPORTATION, PermitTypes.RESEARCH, PermitTypes.IMPORTING, PermitTypes.GAME_MANAGEMENT],
                            permit.permitTypeCode);
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
            })
            .state('permitmanagement.nestremoval', {
                url: '/nestremoval',
                templateUrl: 'harvestpermit/management/nestremoval/nestremoval-usage.html',
                controller: 'HarvestPermitNestRemovalUsageController',
                controllerAs: '$ctrl',
                resolve: {
                    permitId: function (permitId) {
                        return permitId;
                    },
                    permitUsages: function (NestRemovalPermitUsage, permitId) {
                        return NestRemovalPermitUsage.list({id: permitId}).$promise;
                    }
                }
            })
            .state('permitmanagement.permitusage', {
                url: '/permitusage',
                templateUrl: 'harvestpermit/management/permitusage/permit-usage.html',
                controller: 'PermitUsageController',
                controllerAs: '$ctrl',
                resolve: {
                    permitId: function (permitId) {
                        return permitId;
                    },
                    permitUsages: function (PermitUsage, permitId) {
                        return PermitUsage.list({id: permitId}).$promise;
                    }
                }
            });
    })

    .controller('HarvestPermitDashboardController', function ($state, $location, AnnualPermitPdfUrl, FormPostService,
                                                              NotificationService,
                                                              EditHarvestPermitContactPersonsModal,
                                                              HarvestPermitPdfUrl, HarvestPermitAttachmentUrl,
                                                              HarvestPermitPeriodEditModal,
                                                              MoosePermitLeadersService, ActiveRoleService,
                                                              PermitEndOfHuntingReportModal,
                                                              HarvestPermitAmendmentApplicationService,
                                                              permit, isMooselikePermit, isNestRemovalPermit,
                                                              attachmentList, duePayment,
                                                              getGameSpeciesName, selectedGameSpeciesCode,
                                                              partiallyPaidInvoiceCount, nestRemovalPermitUsages,
                                                              PermitTypeCode, isNonHarvestPermit, permitUsages) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.isModerator = ActiveRoleService.isModerator();
            $ctrl.permit = permit;
            $ctrl.attachmentList = attachmentList;
            $ctrl.duePayment = duePayment;
            $ctrl.isMooselikePermit = isMooselikePermit;
            $ctrl.isNestRemovalPermit = isNestRemovalPermit;
            $ctrl.isNonHarvestPermit = isNonHarvestPermit;
            $ctrl.hasOnlySpecimenGranted = hasOnlySpecimenGranted(permit);
            $ctrl.huntingYear = _.parseInt(permit.permitNumber.substring(0, 4));
            $ctrl.getGameSpeciesName = getGameSpeciesName;
            $ctrl.selectedGameSpeciesCode = selectedGameSpeciesCode;
            $ctrl.partiallyPaidInvoiceCount = partiallyPaidInvoiceCount;
            $ctrl.nestRemovalPermitUsageLastModifier =
                nestRemovalPermitUsages && nestRemovalPermitUsages.length > 0 ? nestRemovalPermitUsages[0].lastModifier : null;
            $ctrl.permitUsageLastModifier =
                permitUsages && permitUsages.length > 0 ? permitUsages[0].lastModifier : null;
            $ctrl.paymentStarted = false;
            $ctrl.canEditPeriods = !permit.harvestReportState &&
                PermitTypeCode.isRenewalPermitType(permit.permitTypeCode) &&
                $ctrl.isModerator;
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

        $ctrl.isAnnualRenewalPermit = function () {
            return PermitTypeCode.isRenewalPermitType($ctrl.permit.permitTypeCode);
        };

        $ctrl.downloadAnnualPermitPdf = function () {
            FormPostService.submitFormUsingBlankTarget(AnnualPermitPdfUrl.getRenewalPdf(permit.id));
        };

        $ctrl.downloadAttachment = function (a) {
            FormPostService.submitFormUsingBlankTarget(HarvestPermitAttachmentUrl.get(permit.id, a.id));
        };

        $ctrl.startPayment = function () {
            $ctrl.paymentStarted = true;

            $state.go('permitmanagement.payment.confirmation', {
                permitId: permit.id,
                invoiceId: duePayment.id
            });
        };

        $ctrl.editContactPersons = function () {
            EditHarvestPermitContactPersonsModal.showModal($ctrl.permit.id).finally(reloadState);
        };

        $ctrl.hasSpeciesAmounts = function () {
            return PermitTypeCode.hasSpeciesAmounts($ctrl.permit.permitTypeCode);
        };

        $ctrl.editPeriods = function () {
            HarvestPermitPeriodEditModal.open($ctrl.permit).then($state.reload);
        };

        function hasOnlySpecimenGranted(permit) {
            var specimenGranted = !!_.find(permit.speciesAmounts, function (spa) {
                return spa.amount && spa.amount > 0;
            });
            return specimenGranted && _.every(permit.speciesAmounts, function (spa) {
                return spa.eggAmount === null && spa.constructionAmount === null && spa.nestAmount === null;
            });
        }
    })
    .service('HarvestPermitPeriodEditModal', function ($uibModal) {
        this.open = function (permit) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/management/annual-renewal-periods.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    permit: _.constant(permit)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $scope, $timeout, $translate,
                                 ConfirmationDialogService, HarvestPermits, NotificationService,
                                 PermitDecisionRenewal, permit) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.permit = permit;
                $ctrl.speciesAmounts = angular.copy(permit.speciesAmounts);
            };

            $ctrl.close = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.savePeriods = function () {
                var periods = _.chain($ctrl.speciesAmounts)
                    .map(function (spa) {
                        return {
                            speciesCode: spa.gameSpecies.code,
                            beginDate: spa.beginDate,
                            endDate: spa.endDate,
                            beginDate2: spa.beginDate2,
                            endDate2: spa.endDate2
                        };
                    })
                    .value();

                HarvestPermits.moderatePeriods({permitId: permit.id, periods: periods}).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $uibModalInstance.close();
                });
            };
        }
    });
