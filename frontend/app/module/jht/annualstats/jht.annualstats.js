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
                        availableYears: function (AnnualStatisticsAvailableYears) {
                            return AnnualStatisticsAvailableYears.get();
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

                                if (angular.isString(RhyAnnualStatisticsStates[activeTab])) {
                                    return activeTab;
                                }
                            }

                            return 0;
                        },
                        allAnnualStats: function (RhyAnnualStatisticsProgress, year) {
                            return RhyAnnualStatisticsProgress.get({calendarYear: year}).$promise;
                        }
                    }
                });
        })

        .factory('RhyAnnualStatisticsProgress', function ($resource) {
            var params = {
                calendarYear: '@calendarYear'
            };

            return $resource('/api/v1/riistanhoitoyhdistys/annualstatistics/year/:calendarYear/progress', params, {
                get: {method: 'GET', isArray: true}
            });
        })

        .controller('AnnualStatisticsDashboardController', function ($location, $state, RhyAnnualStatisticsStates,
                                                                     activeTab, allAnnualStats, availableYears, year) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.activeTab = activeTab;
                $ctrl.year = year;
                $ctrl.availableYears = availableYears;

                var stateCountsObj = _.countBy(allAnnualStats, 'annualStatsState');
                $ctrl.stateCounts = _.map(RhyAnnualStatisticsStates, function (state) {
                    return stateCountsObj[state] || 0;
                });

                $ctrl.activeState = RhyAnnualStatisticsStates[activeTab];

                $ctrl.groupedStats = _.groupBy(allAnnualStats, 'annualStatsState');
                $ctrl.filteredStats = $ctrl.groupedStats[$ctrl.activeState];

                $ctrl.isCopyEmailsButtonActive = $ctrl.stateCounts[0] + $ctrl.stateCounts[1] > 0;

                $location.search({year: year, activeTab: activeTab});
            };

            $ctrl.onSelectedYearChanged = function () {
                $state.go($state.current, {year: $ctrl.year, activeTab: $ctrl.activeTab}, {reload: true});
            };

            var getStatesUnlockedForCoordinator = function () {
                return _.slice(RhyAnnualStatisticsStates, 0, 2);
            };

            $ctrl.getEntriesNotCompletedByCoordinator = function () {
                return _(getStatesUnlockedForCoordinator())
                    .map(_.propertyOf($ctrl.groupedStats))
                    .flatten()
                    .value();
            };
        })

       .component('jhtAnnualStatisticsTabSelection', {
            templateUrl: 'jht/annualstats/tab-selection.html',
            bindings: {
                tabCounts: '<',
                activeTabIndex: '<',
                year: '<'
            },
            controller: function ($state) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.notCreatedCount = $ctrl.tabCounts[0];
                    $ctrl.inProgressCount = $ctrl.tabCounts[1];
                    $ctrl.underInspectionCount = $ctrl.tabCounts[2];
                    $ctrl.approvedCount = $ctrl.tabCounts[3];
                };

                $ctrl.selectTab = function (index) {
                    if ($ctrl.activeTabIndex !== index) {

                        var stateParams = {
                            year: $ctrl.year,
                            activeTab: index
                        };

                        $state.go($state.$current, stateParams, {reload: true});
                    }
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
            controller: function ($state) {
                var $ctrl = this;

                $ctrl.isAnnualStatisticsCreated = function (annualStatsEntry) {
                    return _.isFinite(annualStatsEntry.annualStatsId);
                };

                $ctrl.isReadyForTransitioningToNextState = function (entry) {
                    return $ctrl.activeState === 'IN_PROGRESS' && entry.readyForInspection ||
                           $ctrl.activeState === 'UNDER_INSPECTION' && entry.completeForApproval;
                };

                $ctrl.openAnnualStatistics = function (rhyId) {
                    $state.go('rhy.annualstats', {id: rhyId, year: $ctrl.year});
                };
            }
        });

})();
