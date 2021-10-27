'use strict';

angular.module('app.rhy.reports', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy.reports', {
                url: '/reports?huntingYear&species&permitId',
                wideLayout: true,
                controllerAs: '$ctrl',
                templateUrl: 'harvestpermit/moosepermit/reports/luke-reports.html',
                resolve: {
                    huntingYears: function (Rhys, LukeSpeciesFilter, orgId) {
                        return Rhys.moosePermitHuntingYears({id: orgId}).$promise.then(function (years) {
                            return LukeSpeciesFilter.filter(years);
                        });
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    },
                    permits: function ($q, Rhys, selectedYearAndSpecies, orgId) {
                        if (!selectedYearAndSpecies.species || !selectedYearAndSpecies.huntingYear) {
                            return $q.when([]);
                        }

                        return Rhys.listMoosePermits({
                            id: orgId,
                            year: selectedYearAndSpecies.huntingYear,
                            species: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    permitId: function ($stateParams, MoosePermitSelection) {
                        var permitId = _.parseInt($stateParams.permitId);
                        if (_.isFinite(permitId)) {
                            MoosePermitSelection.updateSelectedPermitId($stateParams);
                            return permitId;
                        }
                        return null;
                    },
                    clubId: _.constant(null),
                    lukeReportParams: function (MoosePermits, permitId, selectedYearAndSpecies, ActiveRoleService) {
                        return permitId
                            ? MoosePermits.lukeReportParams({
                                permitId: permitId,
                                species: selectedYearAndSpecies.species,
                                activeOccupationId: ActiveRoleService.getActiveOccupationId()
                            }).$promise
                            : null;
                    }
                },
                controller: 'MoosePermitReportsFilterController'
            });

    })
;
