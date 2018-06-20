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
                    permit: function (HarvestPermits, Harvest, permitId) {
                        return HarvestPermits.get({id: permitId}).$promise;
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
                    }
                },
                controllerAs: '$navCtrl',
                controller: function (isMooselikePermit, duePaymentsCount) {
                    var $navCtrl = this;

                    $navCtrl.$onInit = function () {
                        $navCtrl.isMooselikePermit = isMooselikePermit;
                        $navCtrl.duePaymentsCount = duePaymentsCount;
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
                        return gameSpeciesCode ? gameSpeciesCode : _.first(permit.gameSpeciesCodes);
                    },
                    speciesAmounts: function (HarvestPermits, permitId) {
                        return HarvestPermits.getSpeciesUsage({id: permitId}).$promise;
                    },
                    attachmentList: function (HarvestPermits, permitId) {
                        return HarvestPermits.getAttachmentList({id: permitId}).$promise;
                    },
                    duePayment: function (duePayments) {
                        return _.chain(duePayments)
                            .sortByAll(['dueDate'])
                            .first()
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
                    speciesAmounts: function (HarvestPermits, permitId) {
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
                                .sortByOrder(['pointOfTime'], ['desc'])
                                .value();
                        });
                    }
                }
            })
            .state('permitmanagement.map', {
                url: '/map?gameSpeciesCode&huntingYear',
                templateUrl: 'harvestpermit/moosepermit/map/permit-map.html',
                controller: 'MoosePermitMapController',
                controllerAs: 'ctrl',
                bindToController: true,
                wideLayout: true,
                params: {
                    gameSpeciesCode: null,
                    huntingYear: null
                },
                resolve: {
                    club: _.constant({rhy: {}}),
                    harvests: function (MoosePermitHarvest, permitId, $stateParams) {
                        return MoosePermitHarvest.query({
                            permitId: permitId,
                            huntingYear: $stateParams.huntingYear,
                            gameSpeciesCode: $stateParams.gameSpeciesCode
                        }).$promise;
                    },
                    featureCollection: function (MoosePermits, permitId, $stateParams) {
                        return MoosePermits.partnerAreaFeatures({
                            permitId: permitId,
                            huntingYear: $stateParams.huntingYear,
                            gameSpeciesCode: $stateParams.gameSpeciesCode
                        }).$promise;
                    },
                    mapBounds: function (MapBounds, club, featureCollection) {
                        var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        return bounds || MapBounds.getBoundsOfFinland();
                    }
                }
            });
    })
    .controller('HarvestPermitDashboardController', function ($state, $location, FormPostService,
                                                              EditHarvestPermitContactPersonsModal,
                                                              MoosePermitPdfUrl, MooseHarvestReportModal,
                                                              MoosePermitLeadersService,
                                                              PermitEndOfHuntingReportService,
                                                              permit, isMooselikePermit, attachmentList,
                                                              speciesAmounts, duePayment,
                                                              getGameSpeciesName, selectedGameSpeciesCode) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permit = permit;
            $ctrl.speciesAmounts = speciesAmounts;
            $ctrl.attachmentList = attachmentList;
            $ctrl.duePayment = duePayment;
            $ctrl.isMooselikePermit = isMooselikePermit;
            $ctrl.canEditMooseHarvestReport = isMooselikePermit && _.parseInt(permit.permitNumber.substring(0,4)) < 2018;
            $ctrl.getGameSpeciesName = getGameSpeciesName;
            $ctrl.selectedGameSpeciesCode = selectedGameSpeciesCode;
        };

        $ctrl.changeGameSpeciesCode = function (gameSpeciesCode) {
            $ctrl.selectedGameSpeciesCode = gameSpeciesCode;
            $location.search({gameSpeciesCode: gameSpeciesCode});
        };

        $ctrl.downloadPdf = function () {
            FormPostService.submitFormUsingBlankTarget(MoosePermitPdfUrl.get(permit.permitNumber));
        };

        $ctrl.downloadAttachment = function (a) {
            FormPostService.submitFormUsingBlankTarget('api/v1/harvestpermit/' + permit.id + '/attachment/' + a.id);
        };

        $ctrl.startPayment = function () {
            $state.go('permitmanagement.payment.confirmation', {
                permitId: permit.id,
                invoiceId: duePayment.id
            });
        };

        $ctrl.editContactPersons = function () {
            EditHarvestPermitContactPersonsModal.showModal($ctrl.permit.id).finally(function () {
                $state.reload();
            });
        };

        $ctrl.editAllocations = function (gameSpeciesCode) {
            $state.go('permitmanagement.allocation', {
                permitId: permit.id,
                gameSpeciesCode: gameSpeciesCode
            });
        };

        $ctrl.showHuntingGroupLeaders = function (gameSpeciesCode) {
            var huntingYear = _.parseInt(permit.permitNumber.substring(0, 4));

            MoosePermitLeadersService.showLeaders({
                id: permit.id,
                huntingYear: huntingYear,
                gameSpeciesCode: gameSpeciesCode
            });
        };

        $ctrl.showMap = function (gameSpeciesCode) {
            var huntingYear = _.parseInt(permit.permitNumber.substring(0, 4));
            $state.go('permitmanagement.map', {
                permitId: permit.id,
                gameSpeciesCode: gameSpeciesCode,
                huntingYear: huntingYear
            });
        };

        $ctrl.showTables= function (gameSpeciesCode) {
            $state.go('permitmanagement.tables', {
                permitId: permit.id,
                gameSpeciesCode: gameSpeciesCode
            });
        };

        $ctrl.showMooseHarvestReport = function (gameSpeciesCode) {
            MooseHarvestReportModal.openModal(permit.id, gameSpeciesCode).finally(function () {
                $state.reload();
            });
        };

        $ctrl.showEndOfHuntingReport = function () {
            PermitEndOfHuntingReportService.openModal(permit.id).finally(function () {
                $state.reload();
            });
        };
    });
