'use strict';

angular.module('app.harvestpermit.management.endofhunting', [])

    .config(function ($stateProvider) {
        $stateProvider
            .state('permitmanagement.endofmooselikehunting', {
                url: '/endhunting/{gameSpeciesCode:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/management/endofhunting/end-mooselike-hunting.html',
                controller: 'EndHuntingForMooselikePermitController',
                controllerAs: '$ctrl',
                resolve: {
                    gameSpeciesCode: function ($stateParams) {
                        return _.parseInt($stateParams.gameSpeciesCode);
                    },
                    moosePermit: function (MoosePermits, permitId, gameSpeciesCode) {
                        return MoosePermits.get({
                            permitId: permitId,
                            species: gameSpeciesCode
                        }).$promise;
                    }
                }
            });
    })

    .controller('EndHuntingForMooselikePermitController', function (MoosePermitEndOfHuntingReport, NotificationService,
                                                                    $state, $translate, dialogs, ActiveRoleService,
                                                                    gameSpeciesCode, getGameSpeciesName, moosePermit) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.moosePermit = moosePermit;
            $ctrl.partners = moosePermit.partners;

            $ctrl.canEndMooselikeHunting = moosePermit.canModifyEndOfHunting
                && moosePermit.allPartnersFinishedHunting
                && moosePermit.amendmentPermitsMatchHarvests
                && !moosePermit.permitHolderFinishedHunting;

            var isModerator = ActiveRoleService.isModerator();

            $ctrl.canCancelEndHunting = moosePermit.canModifyEndOfHunting
                && moosePermit.permitHolderFinishedHunting
                && (!moosePermit.huntingFinishedByModeration || isModerator);

            $ctrl.isFinishHuntingByAdminOverrideVisible = ActiveRoleService.isAdmin() && !moosePermit.permitHolderFinishedHunting;
        };

        $ctrl.getGameSpeciesName = function () {
            return getGameSpeciesName(gameSpeciesCode);
        };

        $ctrl.goBack = function () {
            $state.go('permitmanagement.dashboard', {
                permitId: moosePermit.id,
                gameSpeciesCode: gameSpeciesCode
            }, {reload: true});
        };

        $ctrl.done = function () {
            NotificationService.showDefaultSuccess();
            $state.reload();
        };

        $ctrl.endHunting = function () {
            var dialogTitle = $translate.instant('global.dialog.confirmation.title');
            var dialogMessage = $translate.instant('global.dialog.confirmation.text');

            dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                MoosePermitEndOfHuntingReport.save({
                    permitId: moosePermit.id,
                    speciesCode: gameSpeciesCode

                }).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $ctrl.goBack();

                }, function () {
                    NotificationService.showDefaultFailure();
                });
            });
        };

        $ctrl.cancelEndOfHunting = function () {
            var dialogTitle = $translate.instant('global.dialog.confirmation.title');
            var dialogMessage = $translate.instant('global.dialog.confirmation.text');

            dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                MoosePermitEndOfHuntingReport.remove({
                    permitId: moosePermit.id,
                    speciesCode: gameSpeciesCode

                }).$promise.then($ctrl.done, function () {
                    NotificationService.showDefaultFailure();
                });
            });
        };

        $ctrl.finishHuntingByAdminOverride = function () {
            $state.go('permitmanagement.override', {permitId: moosePermit.id, speciesCode: gameSpeciesCode});
        };
    })

    .factory('PermitEndOfHuntingReport', function ($resource) {
        var prefix = 'api/v1/harvestreport/permit/:id';
        return $resource(prefix, {id: '@id'}, {
            changeState: {
                method: 'POST',
                url: prefix + '/state'
            },
            moderatorCreate: {
                method: 'POST',
                url: prefix + '/moderator'
            }
        });
    })

    .factory('MoosePermitEndOfHuntingReport', function ($resource) {
        var apiPrefix = '/api/v1/harvestreport/moosepermit/:permitId/:speciesCode';

        return $resource(apiPrefix, {permitId: "@permitId", speciesCode: "@speciesCode"}, {});
    })

    .service('PermitEndOfHuntingReportModal', function ($uibModal) {
        this.openModal = function (permitId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/management/endofhunting/end-of-hunting-report.html',
                resolve: {
                    report: function (PermitEndOfHuntingReport) {
                        return PermitEndOfHuntingReport.get({id: permitId}).$promise;
                    }
                },
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                size: 'lg'
            }).result;
        };

        function ModalController($uibModalInstance, NotificationService, PermitEndOfHuntingReport, report, ActiveRoleService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.report = report;
                $ctrl.hasHarvests = report.harvests && report.harvests.length;
                $ctrl.isModerator = ActiveRoleService.isModerator();
                $ctrl.showAdditionalComments = $ctrl.isModerator ||
                    ($ctrl.report.harvestReportState === 'APPROVED' &&
                    $ctrl.report.endOfHuntingReportComments);
            };

            $ctrl.create = function () {
                var promise;
                if (ActiveRoleService.isModerator()) {
                    promise = PermitEndOfHuntingReport
                        .moderatorCreate({id: report.permitId}, {endOfHuntingReportComments: $ctrl.report.endOfHuntingReportComments})
                        .$promise;
                } else {
                    promise = PermitEndOfHuntingReport.save({id: report.permitId}).$promise;
                }
                handleActionResultPromise(promise);
            };

            $ctrl.remove = function () {
                var promise = PermitEndOfHuntingReport.delete({id: report.permitId}).$promise;
                handleActionResultPromise(promise);
            };

            $ctrl.accept = function () {
                var bodyParams = {
                    to: 'APPROVED',
                    id: report.permitId,
                    rev: report.permitRev,
                    endOfHuntingReportComments: {
                        endOfHuntingReportComments: $ctrl.report.endOfHuntingReportComments
                    }
                };
                var promise = PermitEndOfHuntingReport.changeState({id: report.permitId}, bodyParams).$promise;
                handleActionResultPromise(promise);
            };

            function handleActionResultPromise($promise) {
                $promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $uibModalInstance.close();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            }
        }
    })

    .component('moosePermitHuntingProgress', {
        templateUrl: 'harvestpermit/management/endofhunting/moose-permit-hunting-progress.html',
        bindings: {
            permitAmount: '<',
            amendmentAmount: '<',
            harvestedAmount: '<',
            nonEdibleAmount: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.totalAmount = $ctrl.permitAmount + $ctrl.amendmentAmount;
                $ctrl.progressBarWidth = 100 * $ctrl.harvestedAmount / $ctrl.totalAmount;

                $ctrl.getCssClassForHarvestedAmount = function () {
                    return $ctrl.harvestedAmount <= $ctrl.totalAmount ? 'text-success' : 'text-danger';
                };
            };
        }
    })

    .component('moosePermitPriceBreakdown', {
        templateUrl: 'harvestpermit/management/endofhunting/moose-permit-price-breakdown.html',
        bindings: {
            totalPayment: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
            };
        }
    })

    .component('moosePermitPartnerTable', {
        templateUrl: 'harvestpermit/management/endofhunting/moose-permit-partner-table.html',
        bindings: {
            partners: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var pageSize = 20;

                $ctrl.pager = {
                    data: $ctrl.partners,
                    total: $ctrl.partners.length,
                    currentPage: 1,
                    pageSize: pageSize
                };

                var updatePager = function () {
                    var page = $ctrl.pager.currentPage - 1;
                    var begin = page * pageSize;
                    var end = begin + pageSize;

                    $ctrl.page = $ctrl.pager.data.slice(begin, end);
                };
                updatePager();
                $ctrl.updatePager = updatePager;
            };
        }
    })

    .factory('EndOfHuntingNestRemovalReport', function ($resource) {
        var prefix = 'api/v1/harvestreport/permit/nestremoval/:id';
        return $resource(prefix, {id: '@id'}, {
            moderatorCreate: {
                method: 'POST',
                url: prefix + '/moderator'
            }
        });
    })

    .service('EndOfHuntingNestRemovalReportModal', function ($uibModal) {
        this.openModal = function (permitId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/management/endofhunting/end-of-hunting-nest-removal-report.html',
                resolve: {
                    report: function (EndOfHuntingNestRemovalReport) {
                        return EndOfHuntingNestRemovalReport.get({id: permitId}).$promise;
                    }
                },
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                size: 'lg'
            }).result;
        };

        function ModalController($uibModalInstance, NotificationService, PermitEndOfHuntingReport,
                                 EndOfHuntingNestRemovalReport, ActiveRoleService, report) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.report = report;
                $ctrl.hasUsages = $ctrl.report.usages && $ctrl.report.usages.length;
                $ctrl.isModerator = ActiveRoleService.isModerator();
                $ctrl.showAdditionalComments = $ctrl.isModerator ||
                    ($ctrl.report.harvestReportState === 'APPROVED' &&
                        $ctrl.report.endOfHuntingReportComments);
            };

            $ctrl.create = function () {
                var promise;
                if (ActiveRoleService.isModerator()) {
                    promise = EndOfHuntingNestRemovalReport
                        .moderatorCreate({id: report.permitId}, {endOfHuntingReportComments: $ctrl.report.endOfHuntingReportComments})
                        .$promise;
                } else {
                    promise = EndOfHuntingNestRemovalReport.save({id: report.permitId}).$promise;
                }
                handleActionResultPromise(promise);
            };

            $ctrl.remove = function () {
                var promise = PermitEndOfHuntingReport.delete({id: report.permitId}).$promise;
                handleActionResultPromise(promise);
            };

            $ctrl.accept = function () {
                var bodyParams = {
                    to: 'APPROVED',
                    id: report.permitId,
                    rev: report.permitRev,
                    endOfHuntingReportComments: {
                        endOfHuntingReportComments: $ctrl.report.endOfHuntingReportComments
                    }
                };
                var promise = PermitEndOfHuntingReport.changeState({id: report.permitId}, bodyParams).$promise;
                handleActionResultPromise(promise);
            };

            function handleActionResultPromise($promise) {
                $promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $uibModalInstance.close();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            }
        }
    })

    .factory('EndOfPermitPeriodReport', function ($resource) {
        var prefix = 'api/v1/harvestreport/endofpermitperiod/permit/:id';
        return $resource(prefix, {id: '@id'}, {
            moderatorCreate: {
                method: 'POST',
                url: prefix + '/moderator'
            }
        });
    })

    .service('EndOfPermitPeriodReportModal', function ($uibModal) {
        this.openModal = function (permitId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/management/endofhunting/end-of-permit-period-report.html',
                resolve: {
                    report: function (EndOfPermitPeriodReport) {
                        return EndOfPermitPeriodReport.get({id: permitId}).$promise;
                    }
                },
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                size: 'lg'
            }).result;
        };

        function ModalController($uibModalInstance, NotificationService, PermitEndOfHuntingReport,
                                 EndOfPermitPeriodReport, ActiveRoleService, report) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.report = report;
                $ctrl.hasUsages = $ctrl.report.usages && $ctrl.report.usages.length;
                $ctrl.isModerator = ActiveRoleService.isModerator();
                $ctrl.showAdditionalComments = $ctrl.isModerator ||
                    ($ctrl.report.harvestReportState === 'APPROVED' &&
                        $ctrl.report.endOfHuntingReportComments);
            };

            $ctrl.create = function () {
                var promise;
                if (ActiveRoleService.isModerator()) {
                    promise = EndOfPermitPeriodReport
                        .moderatorCreate({id: report.permitId}, {endOfHuntingReportComments: $ctrl.report.endOfHuntingReportComments})
                        .$promise;
                } else {
                    promise = EndOfPermitPeriodReport.save({id: report.permitId}).$promise;
                }
                handleActionResultPromise(promise);
            };

            $ctrl.remove = function () {
                var promise = PermitEndOfHuntingReport.delete({id: report.permitId}).$promise;
                handleActionResultPromise(promise);
            };

            $ctrl.accept = function () {
                var bodyParams = {
                    to: 'APPROVED',
                    id: report.permitId,
                    rev: report.permitRev,
                    endOfHuntingReportComments: {
                        endOfHuntingReportComments: $ctrl.report.endOfHuntingReportComments
                    }
                };
                var promise = PermitEndOfHuntingReport.changeState({id: report.permitId}, bodyParams).$promise;
                handleActionResultPromise(promise);
            };

            function handleActionResultPromise($promise) {
                $promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $uibModalInstance.close();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            }
        }
    });
