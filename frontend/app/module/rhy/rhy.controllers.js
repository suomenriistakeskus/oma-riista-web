'use strict';

angular.module('app.rhy.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy', {
                abstract: true,
                templateUrl: 'rhy/layout.html',
                url: '/rhy/{id:[0-9]{1,8}}',
                resolve: {
                    orgId: function ($stateParams) {
                        return _.parseInt($stateParams.id);
                    },
                    rhyId: function (orgId) {
                        return orgId;
                    },
                    // FIXME when shooting test pilot is over, this can be removed
                    showPilotShootingTestMenu: function (Rhys, orgId) {
                        return Rhys.getPublicInfo({id: orgId}).$promise.then(function (rhy) {
                            return ['602', '459', '666', '161', '163', '106', '108', '110', '112', '114', '472', '101', '105', '113', '211', '121'].indexOf(rhy.officialCode) !== -1;
                        });
                    }
                },
                controllerAs: '$ctrl',
                controller: function (ActiveRoleService, showPilotShootingTestMenu) {
                    this.coordinatorView = ActiveRoleService.isCoordinator() || ActiveRoleService.isModerator();
                    this.shootingTestOfficialView = ActiveRoleService.isShootingTestOfficial();

                    this.showPilotShootingTestMenu = showPilotShootingTestMenu;
                }
            })
            .state('rhy.show', {
                url: '/show',
                templateUrl: 'rhy/show.html',
                controller: 'RhyShowController',
                onEnter: function (SrvaEventMapSearchParametersService,
                                   SrvaEventListSearchParametersService) {
                    // Make sure search parameters are not persisted when moderator changes RHY
                    SrvaEventMapSearchParametersService.pop();
                    SrvaEventListSearchParametersService.pop();
                },
                resolve: {
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    }
                }
            })
            .state('rhy.occupations', {
                url: '/occupations',
                templateUrl: 'occupation/list.html',
                controller: 'OccupationListController',
                controllerAs: '$ctrl',
                resolve: {
                    allOccupations: function (Occupations, orgId) {
                        return Occupations.query({orgId: orgId}).$promise;
                    },
                    onlyBoard: function () {
                        return false;
                    },
                    occupationTypes: function (OccupationTypes, orgId) {
                        return OccupationTypes.query({orgId: orgId}).$promise;
                    }
                }
            })
            .state('rhy.announcements', {
                url: '/announcements',
                controllerAs: '$ctrl',
                templateUrl: 'rhy/announcements.html',
                resolve: {
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    }
                },
                controller: function ($state, $stateParams, rhy) {
                    this.rhy = rhy;
                }
            })
            .state('rhy.nomination', {
                url: '/jht',
                templateUrl: 'occupation/nomination/list.html',
                controller: 'OccupationNominationListController',
                controllerAs: '$ctrl',
                resolve: {
                    activeRhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    },
                    searchParams: function (OccupationNominationService, activeRhy) {
                        return OccupationNominationService.createSearchParameters(activeRhy);
                    },
                    resultList: function (OccupationNominationService, searchParams) {
                        return searchParams ? OccupationNominationService.search(searchParams) : [];
                    }
                }
            })
            .state('rhy.events', {
                url: '/events',
                templateUrl: 'event/event_list.html',
                controller: 'EventListController'
            })
            .state('rhy.locations', {
                url: '/locations',
                templateUrl: 'event/venue_list.html',
                controller: 'VenueListController'
            })
            .state('rhy.permits', {
                url: '/permits',
                templateUrl: 'harvestpermit/search/search-permits.html',
                controller: 'PermitSearchController',
                controllerAs: '$ctrl',
                resolve: {
                    species: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise.then(function (params) {
                            return params.species;
                        });
                    },
                    permitTypes: function (HarvestPermits) {
                        return HarvestPermits.permitTypes().$promise;
                    },
                    areas: function (Areas) {
                        return Areas.query().$promise;
                    }
                }
            })
            .state('rhy.srva', {
                abstract: true,
                url: '/srva',
                template: '<div ui-view autoscroll="false"></div>',
                resolve: {
                    parameters: function (GameDiarySrvaParameters) {
                        return GameDiarySrvaParameters.query().$promise;
                    }
                }
            })
            .state('rhy.srva.list', {
                url: '/list',
                templateUrl: 'srva/srva-events.html',
                controller: 'SrvaEventListController',
                controllerAs: '$ctrl',
                resolve: {
                    initialRhy: function (Rhys, orgId) {
                        return Rhys.getPublicInfo({id: orgId}).$promise;
                    }
                }
            })
            .state('rhy.srva.map', {
                url: '/map',
                wideLayout: true,
                templateUrl: 'srva/srva-map.html',
                controller: 'SrvaEventMapController',
                controllerAs: '$ctrl',
                resolve: {
                    moderatorView: _.constant(false),
                    initialRhy: function (Rhys, orgId) {
                        return Rhys.getPublicInfo({id: orgId}).$promise;
                    },
                    tabs: function (Rhys, orgId) {
                        return Rhys.searchParamOrganisations({id: orgId}).$promise;
                    }
                }
            })
            .state('rhy.moosepermit', {
                url: '/moosepermit?huntingYear&species',
                wideLayout: true,
                params: {
                    huntingYear: null,
                    species: null
                },
                resolve: {
                    initialState: _.constant('rhy.moosepermit'),
                    huntingYears: function (Rhys, orgId) {
                        return Rhys.moosePermitHuntingYears({id: orgId}).$promise;
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    }
                },
                views: {
                    '@rhy': {
                        templateUrl: 'harvestpermit/moosepermit/layout.html'
                    },
                    'left@rhy.moosepermit': {
                        templateUrl: 'harvestpermit/moosepermit/list.html',
                        controller: 'MoosePermitListController',
                        controllerAs: '$ctrl',
                        resolve: {
                            permits: function (Rhys, selectedYearAndSpecies, orgId) {
                                var year = selectedYearAndSpecies.huntingYear;
                                var species = selectedYearAndSpecies.species;
                                if (!species) {
                                    return _.constant([]);
                                }
                                return Rhys.listMoosePermits({id: orgId}, {year: year, species: species}).$promise;
                            }
                        }
                    }
                }
            })
            .state('rhy.moosepermit.show', {
                url: '/{permitId:[0-9]{1,8}}/show',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/show.html',
                controller: 'MoosePermitShowController',
                controllerAs: '$ctrl',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (MoosePermits, permitId, selectedYearAndSpecies) {
                        return MoosePermits.get({
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
            .state('rhy.moosepermit.lukereports', {
                url: '/{permitId:[0-9]{1,8}}/luke-reports',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/luke/luke-reports.html',
                controller: 'MoosePermitLukeReportsController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    clubId: _.constant(null),
                    lukeReportParams: function (MoosePermits, permitId) {
                        return MoosePermits.lukeReportParams({permitId: permitId}).$promise;
                    }
                }
            })
            .state('rhy.moosepermit.map', {
                url: '/{permitId:[0-9]{1,8}}/map',
                templateUrl: 'harvestpermit/moosepermit/map/permit-map.html',
                controller: 'MoosePermitMapController',
                controllerAs: 'ctrl',
                wideLayout: true,
                resolve: {
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    },
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
                    mapBounds: function (MapBounds, featureCollection, rhy) {
                        var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        return bounds || MapBounds.getRhyBounds(rhy.officialCode);
                    }
                }
            })
            .state('rhy.moosepermit.rhystats', {
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
            })
            .state('rhy.moosepermitapplications', {
                url: '/moosepermitapplications?applicationId&tab&year&species',
                templateUrl: 'rhy/applications/rhy-application-layout.html',
                controllerAs: '$ctrl',
                controller: 'RhyApplicationsController',
                wideLayout: true,
                reloadOnSearch: false,
                params: {
                    tab: null,
                    species: null,
                    year: null,
                    applicationId: null
                },
                resolve: {
                    diaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    },
                    availableSpecies: function (MooselikeSpecies) {
                        return MooselikeSpecies.getPermitBased();
                    },
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    },
                    selectedRhyOfficialCode: function (rhy) {
                        return rhy.officialCode;
                    }
                }
            });
    })

    .controller('RhyShowController',
        function ($scope, $state, $uibModal, rhy, NotificationService, MapDefaults, MapBounds, MapState, GIS) {
            $scope.data = rhy;
            $scope.mapDefaults = MapDefaults.create();
            $scope.center = {};

            MapBounds.getRhyBounds(rhy.officialCode).then(function (bounds) {
                $scope.bounds = bounds;
            });

            GIS.getRhyGeom(rhy.officialCode).then(function (response) {
                var geojson = {
                    type: "FeatureCollection",
                    features: [
                        {
                            type: "Feature",
                            id: rhy.id,
                            properties: {name: rhy.nameFI},
                            geometry: {
                                type: "MultiPolygon",
                                coordinates: response.data.coordinates
                            }
                        }
                    ]
                };
                $scope.geojson = {
                    data: geojson,
                    style: {fillColor: "green", weight: 2, opacity: 0, color: 'none', fillOpacity: 0.6}
                };
            });


            $scope.editRHY = function () {
                $uibModal.open({
                    templateUrl: 'rhy/edit_rhy.html',
                    controller: 'RHYEditController',
                    resolve: {
                        rhy: function (Rhys) {
                            return Rhys.get({id: rhy.id}).$promise;
                        }
                    }
                }).result.then(function () {
                    NotificationService.showDefaultSuccess();

                    $state.reload();
                });
            };
        })
    .controller('RHYEditController',
        function ($scope, $uibModalInstance, Rhys, rhy) {
            $scope.rhy = rhy;

            var originalAddress = rhy.hasOwnAddress ? rhy.address : null;
            if (!rhy.hasOwnAddress) {
                // address is from coordinator, address might not be valid, so set it null to have valid form
                rhy.address = null;
            }
            $scope.$watch('rhy.hasOwnAddress', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    rhy.address = newValue ? originalAddress : null;
                }
            });

            var originalEmail = rhy.hasOwnEmail ? rhy.email : null;
            $scope.$watch('rhy.hasOwnEmail', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    rhy.email = newValue ? originalEmail : null;
                }
            });

            var originalPhoneNumber = rhy.hasOwnPhoneNumber ? rhy.phoneNumber : null;
            $scope.$watch('rhy.hasOwnPhoneNumber', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    rhy.phoneNumber = newValue ? originalPhoneNumber : null;
                }
            });

            $scope.save = function (form) {
                var promise = Rhys.update(rhy).$promise;

                promise.then(function () {
                    $uibModalInstance.close();
                }, function () {
                    $scope.error = "ERROR";
                });
            };
        })
    .controller('ValidRhyEmailController', function ($scope) {
        var validDomain = '@rhy.riista.fi';
        var getUser = function (email) {
            return email.substring(0, email.indexOf('@'));
        };
        var getDomain = function (email) {
            return email.substring(email.indexOf('@'));
        };
        var startsWith = function (str, toStartWith) {
            return str.indexOf(toStartWith) === 0;
        };
        var isValidDomain = function (email) {
            return startsWith(validDomain, getDomain(email));
        };
        $scope.getRhyEmail = function (email) {
            if (isValidDomain(email)) {
                return [getUser(email) + validDomain];
            }
            if (email.indexOf('@') !== -1) {
                return [''];
            }
            return [email + validDomain];
        };
    })
    .component('organisationSelectByRhy', {
        templateUrl: 'rhy/organisation-select-by-rhy.html',
        bindings: {
            tabs: '<',
            organisationChanged: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.selectTab(_.first($ctrl.tabs));
            };

            $ctrl.selectedOrgCode = null;
            $ctrl.selectedTab = null;

            $ctrl.orgChanged = function () {
                $ctrl.organisationChanged({'type': $ctrl.selectedTab.type, 'code': $ctrl.selectedOrgCode});
            };

            $ctrl.selectTab = function (tab) {
                $ctrl.selectedTab = tab;
                var code = _.get(_.find(tab.organisations, 'selected'), 'officialCode');
                $ctrl.selectedOrgCode = code;
                $ctrl.organisationChanged({'type': $ctrl.selectedTab.type, 'code': code});
            };
        }
    })
;
