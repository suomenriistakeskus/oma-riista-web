'use strict';

angular.module('app.club.gamestatistics', [])
    .factory('ClubGameStatisticsService', function ($resource) {
        return $resource('/api/v1/club/:clubId/gamestatistics', {clubId: '@clubId'}, {
            mooseStatistics: {
                method: 'GET',
                url: '/api/v1/club/:clubId/gamestatistics/moose'
            },
            deerStatistics: {
                method: 'GET',
                url: '/api/v1/club/:clubId/gamestatistics/deer'
            },
            deerCensusStatistics: {
                method: 'GET',
                url: '/api/v1/club/:clubId/gamestatistics/deercensus'
            }
        });
    })

    .service('GameStatisticsChartService', function ($window, $translate) {
        /**
         * Creates chart from GameStatisticsDTO to defined HTML element using C3.js
         *
         * @param chartId ID of the HTML element where the chart is created.
         * Example: '#exampleChart'
         *
         * @param gameStatistics GameStatisticsDTO containing 'timestamps' and 'datasets' properties.
         * Example: {
         *      timestamps: [2017-01-01, 2018-01-01, 2019-01-01],
         *      datasets: { mooseCount: [1,2,3], deerCount: [0,0,7] }
         *  }
         *
         * @param dataKeys Array of data keys. Defines which data from gameStatistics.datasets is displayed on chart.
         * Example: ['mooseCount', 'deerCount']
         *
         * @param dataLabels Optional labels for data. Example:
         *  { 'mooseCount': 'Hirvet', 'deerCount': 'Peurat' }
         */
        this.createChart = function (chartId, gameStatistics, dataKeys, dataLabels) {
            var c3ChartDataArrays = convertGameStatisticsToC3ChartData(gameStatistics, dataKeys);
            createC3Chart(chartId, c3ChartDataArrays, dataLabels);
        };

        /**
         * Example input:
         * dataKeys = ['mooseCount', 'deerCount']
         * gameStatistics = {
         *      timestamps: [2017-01-01, 2018-01-01, 2019-01-01],
         *      datasets: { mooseCount: [1,2,3], deerCount: [0,0,7] }
         *  }
         *
         * Example output:
         *  [
         *   ['timestamps', 2017-01-01, 2018-01-01, 2019-01-01],
         *   ['mooseCount', 200, 130, 90],
         *   ['deerCount', 300, 200, 160]
         *  ]
         */
        function convertGameStatisticsToC3ChartData(gameStatistics, dataKeys) {
            var c3ChartDataArrays = [];
            c3ChartDataArrays.push(createC3ChartDataArray('timestamps', gameStatistics));

            _.forEach(dataKeys, function(dataKey) {
                c3ChartDataArrays.push(createC3ChartDataArray(dataKey, gameStatistics.datasets));
            });
            return c3ChartDataArrays;
        }

        /**
         * Example input:
         * dataKey = 'mooseCount'
         * datsetValues = { mooseCount: [1,2,3], deerCount: [0,0,7] }
         *
         * Example output:
         * ["mooseCount", 1,2,3]
         */
        function createC3ChartDataArray(dataKey, datasetValues) {
            var arr = [dataKey];
            var values = datasetValues[dataKey];
            if (values) {
                arr = arr.concat(values);
            }
            return arr;
        }

        /**
         * Creates chart using C3.js
         * @param chartId ID of the HTML element where the chart is created
         * @param data Array of arrays. Example:
         *  [
         *   ['timestamps', 2017-01-01, 2018-01-01, 2019-01-01],
         *   ['mooseCount', 200, 130, 90],
         *   ['deerCount', 300, 200, 160]
         *  ]
         * @param names Optional labels for data. Example:
         *  { 'mooseCount': 'Hirvet', 'deerCount': 'Peurat' }
         */
        function createC3Chart (chartId, data, names) {
            names = names ? names : {};
            var noDataText = '';
            if (!data.length || data[0].length <= 1) {
                //Show "Ei tietoja" only if there are no timestamp values in data array
                noDataText = $translate.instant('club.gamestatistics.noData');
            }

            var chart = $window.c3.generate({
                bindto: chartId,
                data: {
                    x: 'timestamps',
                    columns: data,
                    xFormat: '%Y-%m-%d',
                    type: 'bar',
                    names: names,
                    empty: {
                        label: {
                            text: noDataText
                        }
                    }
                },
                color: {
                    pattern: ['#154493', '#c69b35', '#56c8ec', '#697179']
                },
                legend: {
                    show: (data.length > 2)
                },
                axis: {
                    x: {
                        type: 'timeseries',
                        tick: {
                            format: '%Y',
                            outer: false
                        }
                    },
                    y: {
                        default: [0, 10],
                        tick: {
                            format: $window.d3.format('d'),
                            outer: false
                        }
                    }
                },
                padding: {
                    top: 10,
                    bottom: 10
                },
                tooltip: {
                    show: false
                },
                bar: {
                    width: {
                        ratio: 0.2
                    }
                }
            });
        }
    })

    .controller('ClubGameStatisticsController',
        function ($scope, $state, $stateParams, $translate, NotificationService,
                  ClubGameStatisticsService, DeerCensusService, GameStatisticsChartService, club) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.club = club;
                $ctrl.isEditDeerCensusAllowed =  DeerCensusService.isEditDeerCensusAllowed();
                $ctrl.isViewDeerCensusButtonVisible = false;
                initDeerCensusNotificationState();
                $ctrl.showMooseStatistics();
            };

            function initDeerCensusNotificationState() {
                DeerCensusService.isCurrentYearDeerCensusNotificationVisible($ctrl.club.id).then(function(result) {
                    $ctrl.isCurrentYearDeerCensusNotificationVisible = result;
                    $ctrl.currentYear = new Date().getFullYear();
                    $ctrl.previousYear = new Date().getFullYear() - 1;
                });
            }

            $ctrl.openDeerCensusForm = function () {
                DeerCensusService.createDeerCensus($ctrl.club.id).finally(function () {
                    loadDeerCensusStatistics();
                    initDeerCensusNotificationState();
                });
            };
            $ctrl.openDeerCensusView = function () {
                DeerCensusService.viewDeerCensuses($ctrl.club.id).finally(function () {
                    loadDeerCensusStatistics();
                    initDeerCensusNotificationState();
                });
            };

            $ctrl.showMooseStatistics = function () {
                $ctrl.isMooseView = true;
                $ctrl.isDeerView = false;
                $ctrl.isDeerCensusView = false;

                loadMooseStatistics();
            };
            $ctrl.showDeerStatistics = function () {
                $ctrl.isMooseView = false;
                $ctrl.isDeerView = true;
                $ctrl.isDeerCensusView = false;

                loadDeerStatistics();
            };
            $ctrl.showDeerCensusStatistics= function () {
                $ctrl.isMooseView = false;
                $ctrl.isDeerView = false;
                $ctrl.isDeerCensusView = true;

                loadDeerCensusStatistics();
            };

            var dataLabels = {
                'roeDeers': $translate.instant('club.gamestatistics.roeDeers'),
                'whiteTailDeers': $translate.instant('club.gamestatistics.whiteTailDeers'),
                'wildForestReindeers': $translate.instant('club.gamestatistics.wildForestReindeers'),
                'fallowDeers': $translate.instant('club.gamestatistics.fallowDeers')};

            function loadMooseStatistics() {

                ClubGameStatisticsService.mooseStatistics({clubId: $ctrl.club.id}).$promise.then(function (gameStatistics) {

                    $ctrl.isMooseDataForMultiplePermitsWarningVisible = gameStatistics.containsAnnuallyCombinedData;

                    GameStatisticsChartService.createChart('#deadMoosesChart',
                        gameStatistics, ['totalDeadMooses']);

                    $ctrl.deadMoosesTableData = gameStatistics;

                    GameStatisticsChartService.createChart('#remainingMoosesInTotalAreaChart',
                        gameStatistics, ['remainingMoosesInTotalArea']);

                    GameStatisticsChartService.createChart('#remainingMoosesInEffectiveAreaChart',
                        gameStatistics, ['remainingMoosesInEffectiveArea']);

                    GameStatisticsChartService.createChart('#deersChart',
                        gameStatistics, ['roeDeers', 'whiteTailDeers', 'wildForestReindeers', 'fallowDeers'],
                        dataLabels);

                    GameStatisticsChartService.createChart('#beaverChart1',
                        gameStatistics, ['beaversAmountOfInhabitedWinterNests']);

                    GameStatisticsChartService.createChart('#beaverChart2',
                        gameStatistics, ['beaversHarvestAmount']);

                    GameStatisticsChartService.createChart('#beaverChart3',
                        gameStatistics, ['beaversAreaOfDamage']);

                    GameStatisticsChartService.createChart('#beaverChart4',
                        gameStatistics, ['beaversAreaOccupiedByWater']);

                    GameStatisticsChartService.createChart('#wildBoarChart1',
                        gameStatistics, ['wildBoarsEstimatedAmountOfSpecimens']);

                    GameStatisticsChartService.createChart('#wildBoarChart2',
                        gameStatistics, ['wildBoarsEstimatedAmountOfSowWithPiglets']);
                });
            }

            function loadDeerStatistics() {

                ClubGameStatisticsService.deerStatistics({clubId: $ctrl.club.id}).$promise.then(function (gameStatistics) {

                    GameStatisticsChartService.createChart('#deerChart1',
                        gameStatistics, ['whiteTailDeerRemainingPopulationInTotalArea']);

                    GameStatisticsChartService.createChart('#deerChart2',
                        gameStatistics, ['whiteTailDeerRemainingPopulationInEffectiveArea']);
                });
            }

            function loadDeerCensusStatistics() {

                ClubGameStatisticsService.deerCensusStatistics({clubId: $ctrl.club.id}).$promise.then(function (gameStatistics) {

                    GameStatisticsChartService.createChart('#deerCensusChart',
                        gameStatistics, ['whiteTailDeers', 'roeDeers', 'fallowDeers'],
                        dataLabels);

                    $ctrl.isViewDeerCensusButtonVisible = gameStatistics.timestamps.length;
                });
            }
        });
