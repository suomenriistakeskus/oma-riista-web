'use strict';

angular.module('app.rhy.moosepermitstats', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy.moosepermitstats', {
                url: '/moosepermitstats',
                templateUrl: 'rhy/moosepermitstats/stats.html',
                controllerAs: '$ctrl',
                controller: 'MoosePermitStatsController',
                wideLayout: true,
                resolve: {
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    },
                    huntingYears: function (HuntingYearService) {
                        var currentHuntingYear = HuntingYearService.getCurrent();
                        return _.range(2016, currentHuntingYear + 1);
                    },
                    availableSpecies: function (MooselikeSpecies) {
                        return MooselikeSpecies.getPermitBased();
                    },
                    tabs: function (Rhys, orgId) {
                        return Rhys.moosepermitStatisticsOrganisations({id: orgId}).$promise;
                    }
                }
            })
        ;
    })
    .controller('MoosePermitStatsController',
        function ($state, $stateParams, Rhys, TranslatedBlockUI, FormPostService,
                  availableSpecies, rhy, huntingYears, orgId, tabs) {
            var $ctrl = this;

            $ctrl.huntingYears = huntingYears;
            $ctrl.availableSpecies = availableSpecies;
            $ctrl.tabs = tabs;

            $ctrl.selectedYear = null;
            $ctrl.selectedSpeciesCode = null;
            $ctrl.selectedOrgCode = null;
            $ctrl.selectedTab = null;
            $ctrl.statistics = null;

            $ctrl.selectHuntingYearAndSpeciesCode = function (huntingYear, speciesCode) {
                $ctrl.selectedYear = huntingYear;
                $ctrl.selectedSpeciesCode = speciesCode;
                loadStatistics();
            };

            $ctrl.isTabSelected = function (tab) {
                return tab === $ctrl.selectedTab;
            };

            $ctrl.selectTab = function (tab) {
                $ctrl.selectedTab = tab;
                $ctrl.orgChanged(_.get(_.find(tab.organisations, 'selected'), 'officialCode'));
            };

            $ctrl.orgChanged = function (code) {
                $ctrl.selectedOrgCode = code;
                loadStatistics();
            };

            // init
            $ctrl.selectTab(_.first(tabs));

            function params() {
                return {
                    id: orgId,
                    year: $ctrl.selectedYear,
                    species: $ctrl.selectedSpeciesCode,
                    orgType: $ctrl.selectedTab.type,
                    orgCode: $ctrl.selectedOrgCode
                };
            }

            function loadStatistics() {
                $ctrl.statistics = null;

                if ($ctrl.selectedYear && $ctrl.selectedSpeciesCode && $ctrl.selectedTab) {
                    TranslatedBlockUI.start("global.block.wait");

                    return Rhys.moosepermitStatistics(params()).$promise.then(function (result) {
                        $ctrl.statistics = result;
                    }).finally(TranslatedBlockUI.stop);
                }
            }

            $ctrl.exportToExcel = function () {
                var url = '/api/v1/riistanhoitoyhdistys/' + orgId + '/moosepermit/statistics/excel';
                FormPostService.submitFormUsingBlankTarget(url, params());
            };

            $ctrl.canNavigateToPermit = function () {
                return $ctrl.selectedOrgCode === rhy.officialCode && $ctrl.selectedTab.type === 'RHY';
            };

            $ctrl.navigateToPermit = function (permitId) {
                $state.go('rhy.moosepermit.show', {
                    permitId: permitId,
                    rhyId: orgId,
                    huntingYear: $ctrl.selectedYear,
                    species: $ctrl.selectedSpeciesCode
                });
            };
        })

    .component('moosePermitStatsTable', {
        templateUrl: 'rhy/moosepermitstats/stats-table.html',
        bindings: {
            statistics: '<',
            canNavigateToPermit: '&',
            navigateToPermit: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.onRowClick = function (row) {
                $ctrl.navigateToPermit({
                    permitId: row.permitId
                });
            };
        }
    });
