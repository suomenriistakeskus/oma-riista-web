'use strict';

angular.module('app.club.reports', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.reports', {
                url: '/reports?huntingYear&species&permitId',
                wideLayout: true,
                controllerAs: '$ctrl',
                templateUrl: 'harvestpermit/moosepermit/reports/luke-reports.html',
                resolve: {
                    huntingYears: function (ClubPermits, LukeSpeciesFilter, clubId) {
                        return ClubPermits.huntingYears({clubId: clubId}).$promise.then(function (years) {
                            return LukeSpeciesFilter.filter(years);
                        });
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    },
                    permits: function ($q, ClubPermits, selectedYearAndSpecies, clubId) {
                        if (!selectedYearAndSpecies.species || !selectedYearAndSpecies.huntingYear) {
                            return $q.when([]);
                        }

                        return ClubPermits.query({
                            clubId: clubId,
                            year: selectedYearAndSpecies.huntingYear,
                            species: selectedYearAndSpecies.species
                        }).$promise.then(function (permits) {
                            return _.sortBy(permits, ['permitNumber', 'id']);
                        });
                    },
                    permitId: function ($stateParams, MoosePermitSelection) {
                        var permitId = _.parseInt($stateParams.permitId);
                        if (_.isFinite(permitId)){
                            MoosePermitSelection.updateSelectedPermitId($stateParams);
                            return permitId;
                        }
                        return null;
                    },
                    lukeReportParams: function (MoosePermits, clubId, permitId, selectedYearAndSpecies, ActiveRoleService) {
                        return permitId
                            ? MoosePermits.lukeReportParams({
                                clubId: clubId,
                                permitId: permitId,
                                species: selectedYearAndSpecies.species,
                                activeOccupationId: ActiveRoleService.getActiveOccupationId()
                            }).$promise
                            : null;
                    }
                },
                controller: 'MoosePermitReportsFilterController'
            });

    });
