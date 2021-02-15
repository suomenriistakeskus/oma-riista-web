(function () {
    "use strict";

    angular.module('app.jht.annualstats', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('jht.annualstats', {
                    url: '/annualstatistics?{year:2[0-9]{3}}&activeTab',
                    reloadOnSearch: false,
                    params: {
                        year: null,
                        activeTab: null
                    },
                    templateUrl: 'jht/annualstats/dashboard.html',
                    controller: 'AnnualStatisticsDashboardController',
                    controllerAs: '$ctrl',
                    wideLayout: false,
                    resolve: {
                        availableYears: function (JhtAnnualStatisticsYears) {
                            return JhtAnnualStatisticsYears.get();
                        },
                        year: function ($stateParams, availableYears) {
                            var yearParam = $stateParams.year;

                            if (angular.isString(yearParam)) {
                                var year = _.parseInt(yearParam);

                                if (_.includes(availableYears, year)) {
                                    return year;
                                }
                            }

                            return _.last(availableYears);
                        },
                        activeTab: function ($stateParams, RhyAnnualStatisticsStates) {
                            var activeTabParam = $stateParams.activeTab;

                            if (angular.isString(activeTabParam)) {
                                var activeTab = _.parseInt(activeTabParam);

                                if (!!RhyAnnualStatisticsStates.get(activeTab)) {
                                    return activeTab;
                                }
                            }

                            return 0;
                        },
                        allAnnualStats: function (RhyAnnualStatisticsJhtService, year) {
                            return RhyAnnualStatisticsJhtService.list(year);
                        }
                    }
                });
        })

        .factory('RhyAnnualStatisticsJht', function ($resource) {
            var defaultParams = {
                calendarYear: '@calendarYear'
            };

            return $resource('/api/v1/riistanhoitoyhdistys/annualstatistics/year/:calendarYear', defaultParams, {
                list: {
                    method: 'GET',
                    isArray: true
                },
                batchApprove: {
                    method: 'POST',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/year/:calendarYear/approve'
                }
            });
        })

        .service('RhyAnnualStatisticsJhtService', function (NotificationService, RhyAnnualStatisticsJht) {
            var self = this;

            self.list = function (year) {
                return RhyAnnualStatisticsJht.list({calendarYear: year}).$promise;
            };

            self.batchApprove = function (year, annualStatisticsIds) {
                return RhyAnnualStatisticsJht
                    .batchApprove({calendarYear: year}, annualStatisticsIds).$promise
                    .then(function (response) {
                        NotificationService.flashMessage('global.messages.success', 'success');
                        return response;
                    });
            };
        })

        .service('JhtAnnualStatisticsYears', function (AnnualStatisticsLastAvailableYear) {
            this.get = function () {
                return _.range(2017, AnnualStatisticsLastAvailableYear.LAST_YEAR + 1);

                // TODO uncomment after annual statistics locking logic is implemented.
                //return _.range(2017, new Date().getFullYear() + 1);
            };
        })

        .controller('AnnualStatisticsDashboardController', function ($location, $state, ExportSubsidyAllocationModal,
                                                                     RhyAnnualStatisticsJhtService,
                                                                     RhyAnnualStatisticsState,
                                                                     RhyAnnualStatisticsStates, activeTab,
                                                                     allAnnualStats, availableYears, year,
                                                                     FormPostService) {
            var $ctrl = this;


            var exportSendersUnderInspection = function () {
                var url = '/api/v1/riistanhoitoyhdistys/annualstatistics/year/' +
                    $ctrl.year + '/sendersexcel/underinspection';
                return FormPostService.submitFormUsingBlankTarget(url, {});
            };

            var exportSendersApproved = function () {
                var url = '/api/v1/riistanhoitoyhdistys/annualstatistics/year/' +
                    $ctrl.year + '/sendersexcel/approved';
                return FormPostService.submitFormUsingBlankTarget(url, {});
            };

            $ctrl.$onInit = function () {
                $ctrl.activeTab = activeTab;
                $ctrl.year = year;
                $ctrl.availableYears = availableYears;

                var annualStatisticsStates = RhyAnnualStatisticsStates.list();

                $ctrl.activeState = annualStatisticsStates[activeTab];
                $ctrl.groupedStats = _.groupBy(allAnnualStats, 'annualStatsState');
                $ctrl.displayedStatistics = $ctrl.groupedStats[$ctrl.activeState];

                var getStatisticsByState = function (state) {
                    return $ctrl.groupedStats[state] || [];
                };

                $ctrl.stateNameCountPairs = _.map(annualStatisticsStates, function (state) {
                    var count = getStatisticsByState(state).length;
                    return {name: state, count: count};
                });

                $ctrl.unsentStatistics = _(annualStatisticsStates)
                    .filter(function (state) {
                        return !RhyAnnualStatisticsStates.isCompletedByCoordinator(state);
                    })
                    .map(getStatisticsByState)
                    .flatten()
                    .value();

                $ctrl.isCopyEmailsButtonVisible = !RhyAnnualStatisticsStates.isCompletedByCoordinator($ctrl.activeState);
                $ctrl.isCopyEmailsButtonDisabled = $ctrl.unsentStatistics.length === 0;

                $ctrl.approvableStatistics = _(getStatisticsByState(RhyAnnualStatisticsState.UNDER_INSPECTION))
                    .filter('completeForApproval')
                    .value();

                $ctrl.isBatchApproveButtonVisible = $ctrl.activeState === RhyAnnualStatisticsState.UNDER_INSPECTION;
                $ctrl.isBatchApproveButtonDisabled = $ctrl.approvableStatistics.length === 0;

                $ctrl.isExportSubsidyAllocationButtonVisible =
                    ($ctrl.year > 2017 && $ctrl.year <= 2020) && $ctrl.activeState === RhyAnnualStatisticsState.APPROVED;

                // TODO Switch to outcommented logic.
                $ctrl.isExportSubsidyAllocationButtonDisabled = $ctrl.unsentStatistics.length > 0;
                    //$ctrl.stateNameCountPairs[RhyAnnualStatisticsState.APPROVED] > 0;

                $ctrl.isExportSendersButtonVisible =
                    $ctrl.activeState === RhyAnnualStatisticsState.UNDER_INSPECTION ||
                    $ctrl.activeState === RhyAnnualStatisticsState.APPROVED;
                var submittedEvents =
                    _(getStatisticsByState($ctrl.activeState))
                    .filter('submitEvent')
                    .value();
                $ctrl.isExportSendersButtonDisabled = submittedEvents.length === 0;
                $ctrl.exportSenders =
                    $ctrl.activeState === RhyAnnualStatisticsState.UNDER_INSPECTION ? exportSendersUnderInspection :
                        exportSendersApproved;

                $location.search({year: year, activeTab: activeTab});
            };

            var reload = function () {
                $state.go($state.current, {year: $ctrl.year, activeTab: $ctrl.activeTab}, {reload: true});
            };

            $ctrl.onSelectedYearChanged = function () {
                reload();
            };

            $ctrl.onActiveTabChanged = function (activeTab) {
                if ($ctrl.activeTab !== activeTab) {
                    $ctrl.activeTab = activeTab;
                    reload();
                }
            };

            $ctrl.batchApprove = function () {
                var annualStatisticsIds = _.map($ctrl.approvableStatistics, 'annualStatsId');
                RhyAnnualStatisticsJhtService.batchApprove($ctrl.year, annualStatisticsIds).then(reload);
            };

            $ctrl.openExportSubsidyAllocationDialog = function () {
                var subsidyYear = $ctrl.year + 1;
                ExportSubsidyAllocationModal.openDialog(subsidyYear);
            };
        })

        .service('ExportSubsidyAllocationModal', function ($q, $uibModal, FormPostService) {
            var self = this;

            self.openDialog = function (subsidyYear) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'jht/annualstats/export-subsidy-allocation.html',
                    resolve: {
                    },
                    controllerAs: '$ctrl',
                    controller: function ($uibModalInstance) {
                        var $ctrl = this;

                        $ctrl.data = {
                            totalSubsidyAmount: null,
                            subsidyYear: subsidyYear
                        };

                        $ctrl.export = function () {
                            $uibModalInstance.close($ctrl.data);
                        };

                        $ctrl.cancel = function () {
                            $uibModalInstance.dismiss('cancel');
                        };
                    }
                });

                return modalInstance.result.then(
                    function (exportInput) {
                        var url = '/api/v1/riistanhoitoyhdistys/subsidy/excel';
                        FormPostService.submitFormUsingBlankTarget(url, exportInput);
                    },
                    function (errReason) {
                        return $q.reject(errReason);
                    });
            };
        })

       .component('jhtAnnualStatisticsTabSelection', {
            templateUrl: 'jht/annualstats/tab-selection.html',
            bindings: {
                nameCountPairs: '<',
                activeTab: '<',
                onActiveTabChanged: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.selectTab = function (tabIndex) {
                    $ctrl.onActiveTabChanged({activeTab: tabIndex});
                };
            }
        })

        .component('jhtAnnualStatisticsTable', {
            templateUrl: 'jht/annualstats/stats-table.html',
            bindings: {
                annualStats: '<',
                year: '<',
                activeState: '<'
            },
            controller: function ($state, RhyAnnualStatisticsState) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.inProgressState = $ctrl.activeState === RhyAnnualStatisticsState.IN_PROGRESS;
                    $ctrl.underInspectionState = $ctrl.activeState === RhyAnnualStatisticsState.UNDER_INSPECTION;
                    $ctrl.approvedState = $ctrl.activeState === RhyAnnualStatisticsState.APPROVED;

                    $ctrl.isReadinessColumnShown = $ctrl.inProgressState || $ctrl.underInspectionState;
                    $ctrl.readinessColumnTitleKey =
                        'jht.annualStats.' + ($ctrl.inProgressState ? 'readyForInspection' : 'completeForApproval');

                    $ctrl.isSenderInfoShown = $ctrl.approvedState || $ctrl.underInspectionState;
                };

                $ctrl.isReadyForTransitioningToNextState = function (entry) {
                    return $ctrl.inProgressState && entry.readyForInspection ||
                           $ctrl.underInspectionState && entry.completeForApproval;
                };

                $ctrl.openAnnualStatistics = function (rhyId) {
                    $state.go('rhy.annualstats', {id: rhyId, year: $ctrl.year});
                };
            }
        });

})();
