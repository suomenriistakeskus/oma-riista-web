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
        function (Rhys, TranslatedBlockUI, availableSpecies, huntingYears, orgId, tabs) {
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

            function loadStatistics() {
                $ctrl.statistics = null;

                if ($ctrl.selectedYear && $ctrl.selectedSpeciesCode && $ctrl.selectedTab) {
                    TranslatedBlockUI.start("global.block.wait");

                    return Rhys.moosepermitStatistics({
                        id: orgId,
                        year: $ctrl.selectedYear,
                        species: $ctrl.selectedSpeciesCode,
                        orgType: $ctrl.selectedTab.type,
                        orgCode: $ctrl.selectedOrgCode
                    }).$promise.then(function (result) {
                        $ctrl.statistics = result;
                    }).finally(TranslatedBlockUI.stop);
                }
            }

        });
