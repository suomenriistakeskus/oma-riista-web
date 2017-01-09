'use strict';

angular.module('app.reporting.club.dashboard', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting.club', {
                url: '/club?rkaId&includePermitHolders',
                template: '<reporting-club-statistics statistics="clubStatistics"></reporting-club-statistics>',
                controller: 'ReportingClubDashboardController',
                params: {
                    rkaId: null,
                    includePermitHolders: 'false'
                },
                resolve: {
                    clubStatistics: function ($stateParams, ReportingClubStatisticsService) {
                        return ReportingClubStatisticsService.loadStatistics({
                            rkaId: $stateParams.rkaId,
                            includePermitHolders: $stateParams.includePermitHolders
                        });
                    }
                }
            });
    })

    .controller('ReportingClubDashboardController', function ($scope, clubStatistics) {
        $scope.clubStatistics = clubStatistics;
    })

    .service('ReportingClubStatisticsService', function ($http) {
        this.loadStatistics = function (params) {
            function stripPrefix(name) {
                var c = name ? name.indexOf(',') : null;
                return c > 0 ? name.substring(c + 1) : name;
            }

            function stripSuffix(name) {
                var c = name ? name.lastIndexOf(' ') : null;
                return c > 0 ? name.substring(0, c + 1) : name;
            }

            function replaceNames(row, f) {
                row.organisation.nameFI = f(row.organisation.nameFI);
                row.organisation.nameSV = f(row.organisation.nameSV);
                return row;
            }

            return $http({
                url: '/api/v1/club/moderator/huntingclubmetrics',
                params: params
            }).then(function (response) {
                return _.map(response.data, function (row) {
                    if (row.organisation.nameFI.indexOf(', ') !== -1) {
                        return replaceNames(row, stripPrefix);
                    }
                    if (row.organisation.nameFI.indexOf(', ') === -1) {
                        return replaceNames(row, stripSuffix);
                    }
                    return row;
                });
            });
        };
    })

    .directive('reportingClubStatistics', function () {
        return {
            restrict: 'E',
            templateUrl: 'reporting/club/club-statistics.html',
            scope: {
                statistics: '='
            },
            bindToController: true,
            controllerAs: 'ctrl',
            link: function (scope, element, attrs) {
                angular.element(element).addClass('reporting-club-statistics');
            },
            controller: function ($state, $stateParams, $location, $anchorScroll) {
                var self = this;

                function sumRowCounts(row) {
                    return _(row).omit(['countAll', 'rka']).sum();
                }

                this.printClubRegistrationRatio = function (row) {
                    var sum = sumRowCounts(row);
                    return sum + ' / ' + row.countAll + ' seuraa';
                };

                this.printClubRegistrationPercentage = function (row) {
                    var sum = sumRowCounts(row);
                    return row.countAll > 0 ? 100.0 * sum / row.countAll : 0;
                };

                this.transformValue = function (row, value) {
                    return value;
                };

                this.getRowMax = function (row) {
                    return row.countAll;
                };

                this.hideBarLabel = function (row, value) {
                    return row.countAll < 1 || Math.round(100.0 * value / row.countAll) < 3;
                };

                this.isOrganisationLinkClickable = function (row) {
                    return row.organisation.organisationType === 'RKA';
                };

                this.isBackLinkVisible = function () {
                    return self.statistics && self.statistics.length > 2 &&
                        self.statistics[0].organisation.organisationType === null &&
                        self.statistics[1].organisation.organisationType === 'RHY';
                };

                this.showRka = function (organisation) {
                    return switchRka(organisation.id);
                };

                this.showFinland = function () {
                    return switchRka(null);
                };

                this.showPermitHolders = function () {
                    return $stateParams.includePermitHolders === 'true';
                };

                this.toggleShowPermitHolders = function () {
                    return $state.go($state.current, {
                        rkaId: $stateParams.rkaId,
                        includePermitHolders: !self.showPermitHolders()
                    }).then(scrollToTop);
                };

                function switchRka(rkaId) {
                    return $state.go($state.current, {
                        rkaId: rkaId,
                        includePermitHolders: $stateParams.includePermitHolders
                    }).then(scrollToTop);
                }

                function scrollToTop() {
                    $location.hash('scrollToTop');
                    $anchorScroll();
                }
            }
        };
    });
