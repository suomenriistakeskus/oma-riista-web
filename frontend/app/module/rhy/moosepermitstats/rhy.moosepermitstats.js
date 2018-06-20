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
                        var nextHuntingYear = currentHuntingYear + 1;
                        return _.range(2016, nextHuntingYear + 1);
                    },
                    availableSpecies: function (MooselikeSpecies) {
                        return MooselikeSpecies.getPermitBased();
                    },
                    tabs: function (Rhys, orgId) {
                        return Rhys.searchParamOrganisations({id: orgId}).$promise;
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

            $ctrl.formData = {
                id: orgId,
                year: null,
                species: null,
                orgType: null,
                orgCode: null
            };
            $ctrl.statistics = null;

            $ctrl.selectHuntingYearAndSpeciesCode = function (huntingYear, speciesCode) {
                $ctrl.formData.year = huntingYear;
                $ctrl.formData.species = speciesCode;
                loadStatistics();
            };

            $ctrl.orgChanged = function (type, code) {
                $ctrl.formData.orgType = type;
                $ctrl.formData.orgCode = code;
                loadStatistics();
            };

            function loadStatistics() {
                $ctrl.statistics = null;

                if ($ctrl.formData.year && $ctrl.formData.species && $ctrl.formData.orgType) {
                    TranslatedBlockUI.start("global.block.wait");

                    return Rhys.moosepermitStatistics($ctrl.formData).$promise.then(function (result) {
                        $ctrl.statistics = result;
                    }).finally(TranslatedBlockUI.stop);
                }
            }

            $ctrl.exportToExcel = function () {
                var url = '/api/v1/riistanhoitoyhdistys/' + orgId + '/moosepermit/statistics/excel';
                FormPostService.submitFormUsingBlankTarget(url, $ctrl.formData);
            };

            $ctrl.canNavigateToPermit = function () {
                return $ctrl.formData.orgCode === rhy.officialCode && $ctrl.formData.orgType === 'RHY';
            };

            $ctrl.navigateToPermit = function (permitId) {
                $state.go('rhy.moosepermit.show', {
                    permitId: permitId,
                    rhyId: orgId,
                    huntingYear: $ctrl.formData.year,
                    species: $ctrl.formData.species
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
