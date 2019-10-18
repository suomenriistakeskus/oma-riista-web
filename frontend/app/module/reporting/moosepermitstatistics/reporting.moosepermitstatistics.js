'use strict';

angular.module('app.reporting.moosepermitstatistics', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting.moosepermitstatistics', {
                url: '/moosepermitstatistics',
                templateUrl: 'reporting/moosepermitstatistics/layout.html',
                controllerAs: '$ctrl',
                controller: 'MoosePermitStatisticsController',
                wideLayout: true,
                resolve: {
                    activeRhy: function () {
                        return {};
                    },
                    availableSpecies: function (MooselikeSpecies) {
                        return MooselikeSpecies.getPermitBased();
                    },
                    huntingYears: function (HuntingYearService) {
                        var currentHuntingYear = HuntingYearService.getCurrent();
                        var nextHuntingYear = currentHuntingYear + 1;
                        return _.range(2016, nextHuntingYear + 1);
                    },
                    tabs: function (Rhys, ActiveRoleService) {
                        return Rhys.searchParamOrganisations().$promise.then(function (tabs) {
                            if (ActiveRoleService.isModerator()) {
                                tabs.unshift({
                                    type: 'RK',
                                    organisations: [{
                                        officialCode: "900",
                                        name: "Suomen riistakeskus, koko Suomi",
                                        selected: true
                                    }]
                                });
                            }

                            return tabs;
                        });
                    }
                }
            });
    })

    .component('moosePermitStatisticsModeratorFilters', {
        templateUrl: 'reporting/moosepermitstatistics/moderator-filters.html',
        bindings: {
            tabs: '<',
            huntingYears: '<',
            availableSpecies: '<',
            onFilterChange: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.reportTypeOptions = ['BY_PERMIT', 'BY_LOCATION'];
                $ctrl.groupByOptions = ['RHY_PERMIT', 'HTA_PERMIT', 'RKA', 'RHY', 'HTA', 'HTA_RHY'];
                $ctrl.reportType = _.head($ctrl.reportTypeOptions);
                $ctrl.groupBy = _.head($ctrl.groupByOptions);
                $ctrl.year = null;
                $ctrl.species = null;
            };

            $ctrl.selectHuntingYearAndSpeciesCode = function (huntingYear, speciesCode) {
                $ctrl.year = huntingYear;
                $ctrl.species = speciesCode;
                $ctrl.notifyChange();
            };

            $ctrl.onOrganisationSelect = function (type, code) {
                if (type === 'RK') {
                    $ctrl.groupBy = 'RKA';
                } else {
                    $ctrl.groupBy = 'RHY_PERMIT';
                }

                $ctrl.orgType = type;
                $ctrl.orgCode = code;
                $ctrl.notifyChange();
            };

            $ctrl.notifyChange = function () {
                $ctrl.onFilterChange({filters: createFilters()});
            };

            function createFilters() {
                var isValid = $ctrl.reportType && $ctrl.groupBy && $ctrl.year && $ctrl.species && $ctrl.orgType && $ctrl.orgCode;

                return isValid ? {
                    year: $ctrl.year,
                    species: $ctrl.species,
                    orgType: $ctrl.orgType,
                    orgCode: $ctrl.orgCode,
                    reportType: $ctrl.reportType,
                    groupBy: $ctrl.groupBy
                } : null;
            }
        }
    })

    .component('moosePermitStatisticsCoordinatorFilters', {
        templateUrl: 'reporting/moosepermitstatistics/coordinator-filters.html',
        bindings: {
            tabs: '<',
            huntingYears: '<',
            availableSpecies: '<',
            onFilterChange: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.year = null;
                $ctrl.species = null;
            };

            $ctrl.selectHuntingYearAndSpeciesCode = function (huntingYear, speciesCode) {
                $ctrl.year = huntingYear;
                $ctrl.species = speciesCode;
                notifyChange();
            };

            $ctrl.onOrganisationSelect = function (type, code) {
                $ctrl.orgType = type;
                $ctrl.orgCode = code;
                notifyChange();
            };

            function notifyChange() {
                $ctrl.onFilterChange({filters: createFilters()});
            }

            function createFilters() {
                var isValid = $ctrl.year && $ctrl.species && $ctrl.orgType && $ctrl.orgCode;

                return isValid ? {
                    year: $ctrl.year,
                    species: $ctrl.species,
                    orgType: $ctrl.orgType,
                    orgCode: $ctrl.orgCode,
                    reportType: 'BY_PERMIT',
                    groupBy: 'RHY_PERMIT'
                } : null;
            }
        }
    })


    .service('MoosePermitStatisticsService', function ($http, FormPostService) {
        this.fetch = function (filters) {
            return $http.get('/api/v1/moosepermit/statistics', {
                params: filters
            }).then(function (response) {
                return response.data;
            });
        };

        this.downloadExcel = function (filters) {
            FormPostService.submitFormUsingBlankTarget('/api/v1/moosepermit/statistics/excel', filters);
        };
    })

    .controller('MoosePermitStatisticsController', function ($state, $stateParams, $http, ActiveRoleService,
                                                             TranslatedBlockUI, MoosePermitStatisticsService,
                                                             activeRhy, availableSpecies, huntingYears, tabs) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.tabs = tabs;
            $ctrl.huntingYears = huntingYears;
            $ctrl.availableSpecies = availableSpecies;
            $ctrl.moderatorView = ActiveRoleService.isModerator();
        };

        $ctrl.tooManyResults = function () {
            if (!$ctrl.filters) {
                return false;
            }

            var groupBy = $ctrl.filters.groupBy;
            var orgType = $ctrl.filters.orgType;

            return _.includes(['RHY_PERMIT', 'HTA_PERMIT'], groupBy) && orgType === 'RK';
        };

        $ctrl.search = function () {
            TranslatedBlockUI.start("global.block.wait");

            MoosePermitStatisticsService.fetch($ctrl.filters).then(function (statistics) {
                $ctrl.statistics = statistics;

            }).finally(function () {
                TranslatedBlockUI.stop();
            });
        };

        var initialSearchDone = false;

        $ctrl.onFilterChange = function (filters) {
            $ctrl.filters = filters;
            $ctrl.statistics = null;

            if (filters && !initialSearchDone) {
                initialSearchDone = true;
                $ctrl.search();
            }
        };

        $ctrl.exportToExcel = function () {
            if ($ctrl.filters) {
                MoosePermitStatisticsService.downloadExcel($ctrl.filters);
            }
        };

        $ctrl.canNavigateToPermit = function () {
            return !!(activeRhy && activeRhy.officialCode
                && activeRhy.officialCode === $ctrl.filters.orgCode
                && $ctrl.filters.orgType === 'RHY');
        };

        $ctrl.navigateToPermit = function (permitId) {
            $state.go('rhy.moosepermit.table', {
                permitId: permitId,
                rhyId: activeRhy.id,
                huntingYear: $ctrl.filters.year,
                species: $ctrl.filters.species
            });
        };
    })

    .component('moosePermitStatisticsTable', {
        templateUrl: 'reporting/moosepermitstatistics/stats-table.html',
        bindings: {
            statistics: '<',
            canNavigateToPermit: '&',
            navigateToPermit: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.isSummaryRow = function (s) {
                return !s.rhy && !s.rka && !s.hta;
            };

            $ctrl.onRowClick = function (row) {
                $ctrl.navigateToPermit({permitId: row.permitId});
            };
        }
    })

    .component('moosePermitStatisticsSimpleTable', {
        templateUrl: 'reporting/moosepermitstatistics/simple-table.html',
        bindings: {
            statistics: '<',
            canNavigateToPermit: '&',
            navigateToPermit: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.isSummaryRow = function (s) {
                return !s.rhy && !s.rka && !s.hta;
            };

            $ctrl.onRowClick = function (row) {
                $ctrl.navigateToPermit({permitId: row.permitId});
            };
        }
    });
