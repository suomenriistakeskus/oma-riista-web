'use strict';

angular.module('app.clubpermit.controllers', [])
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
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (ClubPermits, clubId, permitId, selectedYearAndSpecies) {
                        return ClubPermits.get({clubId: clubId, permitId: permitId, species: selectedYearAndSpecies.species}).$promise;
                    },
                    todos: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.listTodos({permitId: permitId, speciesCode: selectedYearAndSpecies.species}).$promise;
                    },
                    edit: _.constant(false)
                }
            })
            .state('club.permit.edit', {
                url: '/{permitId:[0-9]{1,8}}/edit',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/show.html',
                controller: 'MoosePermitShowController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (ClubPermits, clubId, permitId, selectedYearAndSpecies) {
                        return ClubPermits.get({clubId: clubId, permitId: permitId, species: selectedYearAndSpecies.species}).$promise;
                    },
                    todos: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.listTodos({permitId: permitId, speciesCode: selectedYearAndSpecies.species}).$promise;
                    },
                    edit: _.constant(true)
                }
            })
            .state('club.permit.lukereports', {
                url: '/{permitId:[0-9]{1,8}}/luke-reports',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/luke-reports.html',
                controller: 'MoosePermitLukeReportsController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    lukeReportParams: function (HarvestPermits, clubId, permitId) {
                        return HarvestPermits.lukeReportParams({clubId: clubId, permitId: permitId}).$promise;
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
                    harvests: function (MoosePermitHarvest, clubId, permitId, selectedYearAndSpecies) {
                        return MoosePermitHarvest.query({
                            clubId: clubId,
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    featureCollection: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.permitMapFeatures({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    mapBounds: function (GIS, club, featureCollection) {
                        var bounds = GIS.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        return bounds || GIS.getRhyBounds(club.rhy.officialCode);
                    }
                }
            });
    });
