'use strict';

angular.module('app.club.permits', [])
    .factory('ClubPermits', function ($resource) {
        return $resource('/api/v1/club/:clubId/permit', {'clubId': '@clubId'}, {
            query: {
                method: 'GET',
                params: {year: '@year', species: '@species'},
                isArray: true
            },
            get: {
                method: 'GET',
                url: '/api/v1/club/:clubId/permit/:permitId',
                params: {'clubId': '@clubId', 'permitId': '@permitId'}
            },
            huntingYears: {
                method: 'GET',
                url: '/api/v1/club/:clubId/permit/huntingyears',
                isArray: true
            },
            todos: {
                method: 'GET',
                url: '/api/v1/club/:clubId/permit/todo',
                params: {'clubId': '@clubId', 'year': '@year'}
            },
        });
    })

    .config(function ($stateProvider) {
        $stateProvider
            .state('club.permit', {
                url: '/permit?huntingYear&species',
                wideLayout: true,
                params: {
                    huntingYear: null,
                    species: null
                },
                resolve: {
                    initialState: _.constant('club.permit'),
                    huntingYears: function (ClubPermits, clubId) {
                        return ClubPermits.huntingYears({clubId: clubId}).$promise;
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    }
                },
                views: {
                    '@club': {
                        templateUrl: 'harvestpermit/moosepermit/layout.html'
                    },
                    'left@club.permit': {
                        templateUrl: 'harvestpermit/moosepermit/list.html',
                        controller: 'MoosePermitListController',
                        controllerAs: '$ctrl',
                        resolve: {
                            permits: function (ClubPermits, selectedYearAndSpecies, clubId) {
                                if (!selectedYearAndSpecies.species) {
                                    return _.constant([]);
                                }
                                return ClubPermits.query({clubId: clubId}, {
                                    year: selectedYearAndSpecies.huntingYear,
                                    species: selectedYearAndSpecies.species
                                }).$promise.then(function (permits) {
                                    return _.sortByAll(permits, ['permitNumber', 'id']);
                                });
                            }
                        }
                    }
                }
            })
            .state('club.permit.show', {
                url: '/{permitId:[0-9]{1,8}}/show',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/show.html',
                controller: 'MoosePermitShowController',
                controllerAs: '$ctrl',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (ClubPermits, clubId, permitId, selectedYearAndSpecies) {
                        return ClubPermits.get({
                            clubId: clubId,
                            permitId: permitId,
                            species: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    todos: function (MoosePermits, permitId, selectedYearAndSpecies) {
                        return MoosePermits.listTodos({
                            permitId: permitId,
                            speciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    }
                }
            })
            .state('club.permit.lukereports', {
                url: '/{permitId:[0-9]{1,8}}/luke-reports',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/luke/luke-reports.html',
                controller: 'MoosePermitLukeReportsController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    lukeReportParams: function (MoosePermits, clubId, permitId) {
                        return MoosePermits.lukeReportParams({clubId: clubId, permitId: permitId}).$promise;
                    }
                }
            })
            .state('club.permit.map', {
                url: '/{permitId:[0-9]{1,8}}/map',
                templateUrl: 'harvestpermit/moosepermit/map/permit-map.html',
                controller: 'MoosePermitMapController',
                controllerAs: 'ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    harvests: function (MoosePermitHarvest, permitId, selectedYearAndSpecies) {
                        return MoosePermitHarvest.query({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    featureCollection: function (MoosePermits, permitId, selectedYearAndSpecies) {
                        return MoosePermits.partnerAreaFeatures({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    mapBounds: function (MapBounds, club, featureCollection) {
                        var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        return bounds || MapBounds.getRhyBounds(club.rhy.officialCode);
                    }
                }
            })
            .state('club.permit.rhystats', {
                url: '/{permitId:[0-9]{1,8}}/rhy-stats',
                template: '<moose-permit-stats-table statistics="$ctrl.statistics"></moose-permit-stats-table>',
                controller: function (statistics) {
                    this.statistics = statistics;
                },
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    statistics: function (MoosePermits, permitId, selectedYearAndSpecies) {
                        var params = {permitId: permitId, speciesCode: selectedYearAndSpecies.species};
                        return MoosePermits.rhyStatistics(params).$promise;
                    }
                }
            });
    });
