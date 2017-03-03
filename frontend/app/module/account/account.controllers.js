'use strict';

angular.module('app.account.controllers', ['ui.router', 'app.account.services'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile', {
                abstract: true,
                templateUrl: 'account/layout.html',
                url: '/profile/{id:[0-9a-zA-Z]{1,8}}',
                controller: function ($scope, $state, $stateParams) {
                    $scope.moderatorViewCurrentSelectedProfile = function () {
                        $state.go('profile.account', {id: $stateParams.id});
                    };
                }
            })
            .state('profile.clubconfig', {
                url: '/clubconfig',
                templateUrl: 'club/config.html',
                controller: 'ContactShareController',
                resolve: {
                    profile: function (AccountService, $stateParams) {
                        return AccountService.loadAccount($stateParams.id);
                    },
                    clubOccupations: function (profile) {
                        return profile.clubOccupations;
                    }
                }
            })
            // moderator states for viewing persons permits and harvest reports
            .state('profile.harvestreport', {
                abstract: true,
                templateUrl: 'account/layout_harvestreport.html',
                url: '/harvestreport'
            })
            .state('profile.harvestreport.todo', {
                url: '/todo',
                templateUrl: 'harvestreport/list.html',
                controller: 'HarvestReportListToDoController',
                resolve: {
                    fieldsAndSeasons: function (HarvestReportFieldsAndSeasons) {
                        return HarvestReportFieldsAndSeasons.valids();
                    },
                    todoHarvest: function (DiaryEntries, $stateParams) {
                        return DiaryEntries.todo({personId: $stateParams.id}).$promise;
                    },
                    parameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    },
                    harvestReportLocalityResolver: function (HarvestReportLocalityResolver) {
                        return HarvestReportLocalityResolver.get();
                    }
                }
            })
            .state('profile.harvestreport.permits', {
                url: '/permits',
                templateUrl: 'harvestpermit/list.html',
                controller: 'PermitListController',
                resolve: {
                    permits: function ($stateParams, HarvestPermits) {
                        return HarvestPermits.query({personId: $stateParams.id}).$promise;
                    }
                }
            })
            .state('profile.harvestreport.permit', {
                url: '/permit/{id:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/show.html',
                controller: 'PermitShowController',
                resolve: {
                    permit: function ($stateParams, HarvestPermits) {
                        return HarvestPermits.get({permitId: $stateParams.id}).$promise;
                    }
                }
            })
            .state('profile.harvestreport.listdone', {
                url: '/harvestreports',
                templateUrl: 'harvestreport/listdone.html',
                controller: 'HarvestReportListDoneController',
                resolve: {
                    harvestReports: function ($stateParams, HarvestReports) {
                        return HarvestReports.query({personId: $stateParams.id}).$promise;
                    },
                    harvestReportLocalityResolver: function (HarvestReportLocalityResolver) {
                        return HarvestReportLocalityResolver.get();
                    }
                }
            })
            .state('profile.harvestreport.moosepermit', {
                url: '/moosepermit?huntingYear&species',
                wideLayout: true,
                params: {
                    huntingYear: null,
                    species: null
                },
                resolve: {
                    initialState: _.constant('profile.harvestreport.moosepermit'),
                    huntingYears: function ($stateParams, HarvestPermits) {
                        return HarvestPermits.moosePermitHuntingYears({personId: $stateParams.id}).$promise;
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    }
                },
                views: {
                    '@profile.harvestreport': {
                        templateUrl: 'harvestpermit/moosepermit/layout.html'
                    },
                    'left@profile.harvestreport.moosepermit': {
                        templateUrl: 'harvestpermit/moosepermit/list.html',
                        controller: 'MoosePermitListController',
                        controllerAs: '$ctrl',
                        resolve: {
                            permits: function ($stateParams, HarvestPermits, selectedYearAndSpecies) {
                                if (!selectedYearAndSpecies.species) {
                                    return _.constant([]);
                                }
                                return HarvestPermits.listMoosePermits({}, {
                                    year: selectedYearAndSpecies.huntingYear,
                                    species: selectedYearAndSpecies.species,
                                    personId: $stateParams.id
                                }).$promise.then(function (permits) {
                                    return _.sortByAll(permits, ['permitNumber', 'id']);
                                });
                            }
                        }
                    }
                }
            })
            .state('profile.harvestreport.moosepermit.show', {
                url: '/{permitId:[0-9]{1,8}}/show',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/show.html',
                controller: 'MoosePermitShowController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.moosePermit({permitId: permitId, species: selectedYearAndSpecies.species}).$promise;
                    },
                    todos: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.listTodos({permitId: permitId, speciesCode: selectedYearAndSpecies.species}).$promise;
                    },
                    edit: _.constant(false)
                }
            })
            .state('profile.harvestreport.moosepermit.edit', {
                url: '/{permitId:[0-9]{1,8}}/edit',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/show.html',
                controller: 'MoosePermitShowController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.moosePermit({permitId: permitId, species: selectedYearAndSpecies.species}).$promise;
                    },
                    todos: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.listTodos({permitId: permitId, speciesCode: selectedYearAndSpecies.species}).$promise;
                    },
                    edit: _.constant(true)
                }
            })
            .state('profile.harvestreport.moosepermit.lukereports', {
                url: '/{permitId:[0-9]{1,8}}/luke-reports',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/luke-reports.html',
                controller: 'MoosePermitLukeReportsController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    clubId: _.constant(null),
                    lukeReportParams: function (HarvestPermits, permitId) {
                        return HarvestPermits.lukeReportParams({permitId: permitId}).$promise;
                    }
                }
            })
            .state('profile.harvestreport.moosepermit.map', {
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
                    featureCollection: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.permitMapFeatures({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    mapBounds: function (GIS, HarvestPermits, featureCollection, permitId) {
                        var bounds = GIS.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        var findBoundsFromPermitRhy = function () {
                            return HarvestPermits.moosePermitRhyCode({permitId: permitId}).$promise.then(function (rhy) {
                                return GIS.getRhyBounds(rhy.officialCode);
                            });
                        };
                        return bounds || findBoundsFromPermitRhy();
                    }
                }
            })
            .state('profile.harvestreport.moosepermit.rhystats', {
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
                    statistics: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        var params = {permitId: permitId, speciesCode: selectedYearAndSpecies.species};
                        return HarvestPermits.moosePermitRhyStats(params).$promise;
                    }
                }
            });
    })

    .controller('AccountClubRegisterController', function ($q, $http, $filter) {
        var ctrl = this;

        ctrl.selectedOrganisation = null;
        ctrl.nameQuery = '';
        ctrl.codeQuery = '';
        ctrl.warningClubAlreadyActive = false;
        ctrl.existingContactPersonName = '';

        var i18nFilter = $filter('rI18nNameFilter');

        this.searchResultTitle = function (item) {
            if (!item) {
                return '?';
            }

            return i18nFilter(item) +
                (item.contactPersonName ? ' - ' + item.contactPersonName : '') +
                ((item.hasActiveContactPerson) ? ' (*)' : '');
        };

        function search(url, queryString) {
            if (!_.isString(queryString) || _.isEmpty(queryString)) {
                return $q.when([]);
            }

            ctrl.selectedOrganisation = null;
            ctrl.warningClubAlreadyActive = false;
            ctrl.existingContactPersonName = '';

            return $http.get(url, {params: {queryString: queryString}}).then(function (response) {
                return response.data;
            });
        }

        this.searchByName = function ($viewValue) {
            ctrl.codeQuery = '';
            return search('/api/v1/club/lh/findByName', $viewValue);
        };

        this.searchByCode = function ($viewValue) {
            ctrl.nameQuery = '';
            return search('/api/v1/club/lh/findByCode', $viewValue);
        };

        this.onSelectSearchResult = function ($item, $model, $label) {
            ctrl.selectedOrganisation = $item;
            ctrl.codeQuery = '';
            ctrl.nameQuery = '';

            if ($item) {
                ctrl.warningClubAlreadyActive = $item.hasActiveContactPerson;
                ctrl.existingContactPersonName = $item.contactPersonName;
            } else {
                ctrl.warningClubAlreadyActive = false;
                ctrl.existingContactPersonName = '';
            }
        };

        this.canSave = function () {
            return ctrl.selectedOrganisation && !this.selectedOrganisation.hasActiveContactPerson;
        };

        this.save = function () {
            $http.post('/api/v1/club/lh/register', ctrl.selectedOrganisation).then(function (response) {
                if (response.data.result === 'success') {
                    ctrl.$close(response.data);
                } else if (response.data.result === 'exists') {
                    ctrl.warningClubAlreadyActive = true;
                    ctrl.existingContactPersonName = response.data.contactPersonName;
                }
            });
        };

        this.cancel = function () {
            ctrl.$dismiss('cancel');
        };
    })

    .controller('AccountClubCreateController', function (Clubs, MapDefaults, NotificationService) {
        var ctrl = this;

        ctrl.club = {
            geoLocation: null
        };

        ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
        ctrl.mapDefaults = MapDefaults.create();

        ctrl.canSave = function () {
            return ctrl.club.geoLocation;
        };

        ctrl.save = function () {
            Clubs.save(ctrl.club).$promise.then(
                function (response) {
                    ctrl.$close(response.data);
                }, function () {
                    NotificationService.showDefaultFailure();
                });
        };


        ctrl.cancel = function () {
            ctrl.$dismiss('cancel');
        };
    })
;
