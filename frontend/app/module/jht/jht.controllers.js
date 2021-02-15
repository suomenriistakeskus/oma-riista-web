'use strict';

angular.module('app.jht.controllers', ['app.admin.users'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('jht', {
                abstract: true,
                templateUrl: 'jht/layout.html',
                url: '/jht',
                controllerAs: '$ctrl',

                controller: function (ModeratorPrivileges, AvailableRoleService, ActiveRoleService, renewalTodos) {
                    var $ctrl = this;
                    $ctrl.$onInit = function () {
                        $ctrl.myRenewalTodos = renewalTodos.myDecisionsToRenew;
                    };

                    $ctrl.isAuthorizedForHarvestRegistry = ActiveRoleService.isAdmin() ||
                        AvailableRoleService.hasPrivilege(ModeratorPrivileges.harvestRegistry);
                },
                resolve: {
                    renewalTodos: function (ModeratorTodos) {
                        return ModeratorTodos.get().$promise;
                    }
                }
            })
            .state('jht.home', {
                url: '/home',
                templateUrl: 'jht/dashboard.html',
                controllerAs: '$ctrl',

                controller: function ($state, PersonAddFromVtjModal, ModeratorPrivileges,
                                      AvailableRoleService, ActiveRoleService) {
                    var $ctrl = this;

                    $ctrl.addPersonFromVtj = function () {
                        PersonAddFromVtjModal.search().then(function (personInfo) {
                            $state.go('profile.account', {id: personInfo.id});
                        });
                    };

                    $ctrl.isAuthorizedForBulkMessages = function () {
                        return ActiveRoleService.isAdmin() ||
                            AvailableRoleService.hasPrivilege(ModeratorPrivileges.bulkMessagePrivilege);
                    };
                }
            })
            .state('jht.applications', {
                url: '/applications?tab',
                reloadOnSearch: false,
                params: {
                    tab: null
                },
                templateUrl: 'jht/applications.html',
                resolve: {
                    handlers: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.listHandlers().$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function (handlers, $location, renewalTodos) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.handlers = handlers;
                        $ctrl.showTab('filter');
                        $ctrl.myRenewalTodos = renewalTodos.myDecisionsToRenew;
                        $ctrl.decisionsToRenewExist = renewalTodos.decisionsToRenew;
                    };

                    $ctrl.showTab = function (tab) {
                        $ctrl.tab = tab;
                        $location.search({tab: tab});
                    };
                }
            })
            .state('jht.nomination', {
                url: '/jht?occupationType&rhyCode',
                templateUrl: 'occupation/nomination/list.html',
                controller: 'OccupationNominationListController',
                controllerAs: '$ctrl',
                params: {
                    areaCode: {
                        value: null
                    },
                    rhyCode: {
                        value: null
                    },
                    occupationType: {
                        value: null
                    }
                },
                resolve: {
                    activeRhy: _.constant(null),
                    searchParams: function (OccupationNominationService, activeRhy, $stateParams) {
                        var searchParameters = OccupationNominationService.createSearchParameters(activeRhy);

                        // Process email link selection
                        if ($stateParams.rhyCode && $stateParams.occupationType) {
                            searchParameters.nominationStatus = 'ESITETTY';
                            searchParameters.rhyCode = $stateParams.rhyCode;
                            searchParameters.occupationType = $stateParams.occupationType;
                        }

                        return searchParameters;
                    },
                    resultList: function (OccupationNominationService, searchParams) {
                        return searchParams ? OccupationNominationService.search(searchParams) : [];
                    }
                }
            })
            .state('jht.harvestreport', {
                abstract: true,
                url: '/harvestreport',
                template: '<ui-view autoscroll="false"/>'
            })

            .state('jht.harvestreport.create', {
                url: '/createHarvestReport',
                wideLayout: true,
                templateUrl: 'diary/harvest/edit-harvest.html',
                controller: 'OpenDiaryEntryFormController',
                resolve: {
                    entry: function (Harvest) {
                        return Harvest.createTransient({});
                    }
                }
            })

            .state('jht.harvestreport.edit', {
                url: '/editHarvestReport?entryId',
                wideLayout: true,
                templateUrl: 'diary/harvest/edit-harvest.html',
                controller: 'OpenDiaryEntryFormController',
                resolve: {
                    entry: function ($stateParams, MapState, Harvest) {
                        return Harvest.get({id: $stateParams.entryId}).$promise.then(function (harvest) {
                            var zoom = MapState.getZoom();

                            if (zoom) {
                                harvest.geoLocation.zoom = zoom;
                            }

                            if ($stateParams.copy === 'true') {
                                return harvest.createCopyForModeratorMassInsertion();
                            }

                            return harvest;
                        });
                    }
                }
            })

            .state('jht.harvestpermits', {
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
                    },
                    rhyId: _.constant(null)
                }
            });
    })
    .service('PersonAddFromVtjModal', function ($q, $uibModal, PersonSearchService) {
        this.search = function () {
            return $uibModal.open({
                size: 'lg',
                templateUrl: 'jht/addperson.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true
            }).result;
        };

        function ModalController($uibModalInstance) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.person = null;
                $ctrl.notFound = false;
                $ctrl.showSearch = false;
            };

            var found = function (response) {
                $ctrl.person = response.data;
                $ctrl.notFound = false;
            };

            var notFound = function () {
                $ctrl.person = null;
                $ctrl.notFound = true;
            };

            $ctrl.onSsnChange = function (ssn) {
                $ctrl.person = null;
                $ctrl.notFound = false;

                if (ssn) {
                    PersonSearchService.findBySSN(ssn).then(found, notFound);
                }
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.person);
            };
        }
    });
