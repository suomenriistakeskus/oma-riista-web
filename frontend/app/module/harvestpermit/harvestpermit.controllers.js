'use strict';

angular.module('app.harvestpermit.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('harvestreport.main.permits', {
                url: '/permits',
                templateUrl: 'harvestpermit/list.html',
                controller: 'PermitListController',
                resolve: {
                    permits: function (HarvestPermits) {
                        return HarvestPermits.query().$promise;
                    }
                }
            })
            .state('harvestreport.main.permit', {
                url: '/permit/{id:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/show.html',
                controller: 'PermitShowController',
                resolve: {
                    permit: function (HarvestPermits, $stateParams) {
                        return HarvestPermits.get({id: $stateParams.id}).$promise;
                    }
                }
            })
            ////////////////////////////
            // Moose permits
            ////////////////////////////
            .state('harvestreport.main.moosepermit', {
                url: '/moosepermit?huntingYear&species',
                wideLayout: true,
                params: {
                    huntingYear: null,
                    species: null
                },
                resolve: {
                    initialState: _.constant('harvestreport.main.moosepermit'),
                    huntingYears: function (HarvestPermits) {
                        return HarvestPermits.moosePermitHuntingYears().$promise;
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    }
                },
                views: {
                    '@harvestreport.main': {
                        templateUrl: 'harvestpermit/moosepermit/layout.html'
                    },
                    'left@harvestreport.main.moosepermit': {
                        templateUrl: 'harvestpermit/moosepermit/list.html',
                        controller: 'MoosePermitListController',
                        controllerAs: '$ctrl',
                        resolve: {
                            permits: function (HarvestPermits, selectedYearAndSpecies) {
                                if (!selectedYearAndSpecies.species) {
                                    return _.constant([]);
                                }
                                return HarvestPermits.listMoosePermits({}, {
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
            .state('harvestreport.main.moosepermit.show', {
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
            .state('harvestreport.main.moosepermit.edit', {
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
            .state('harvestreport.main.moosepermit.lukereports', {
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
            .state('harvestreport.main.moosepermit.map', {
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
            });
    })

    .controller('PermitListController',
        function ($scope, permits) {
            var _validOrExpired = function (permit) {
                return _(permit.speciesAmounts).pluck('beginDate').any(function (date) {
                    return moment().isAfter(date) || moment().isSame(date);
                });
            };
            $scope.requiresAction = function (permit) {
                return permit.endOfHuntingReportRequired && _validOrExpired(permit);
            };
            // will sort those requiring action to be first, then by permit number
            $scope.permits = _(permits).sortBy('permitNumber').sortByOrder($scope.requiresAction, false).value();
        })

    .controller('PermitShowController',
        function ($scope, $state, $uibModal, $window, NotificationService, Helpers, PermitAcceptHarvest,
                  PermitEndOfHuntingReportService, DiaryEntryService, HarvestReportService,
                  HarvestReportFieldsAndSeasons, permit) {

            $scope.permit = permit;

            var onFailure = function (failure) {
                if (failure === 'error' || failure.status) {
                    NotificationService.showDefaultFailure();
                }
            };

            $scope.edit = function (permit) {
                $uibModal.open({
                    templateUrl: 'harvestpermit/edit_contact_persons_form.html',
                    resolve: {
                        permit: Helpers.wrapToFunction(angular.copy(permit))
                    },
                    controller: 'EditPermitContactPersonsController'
                }).result.then(function () {
                    $state.reload();
                }, onFailure);
            };

            $scope.exportHarvestReports = function (permit) {
                $window.open('/api/v1/harvestpermit/' + permit.id + '/export-reports');
            };

            $scope.reloadCallback = function () {
                // Should not pass arguments to reload()
                $state.reload();
            };

            $scope.changeState = function (harvest, newState) {
                PermitAcceptHarvest.accept(harvest.id, harvest.rev, newState)
                    .then(function () {
                        $state.reload();
                    }, onFailure);
            };

            $scope.createOrViewEndOfHuntingReport = function (permit) {
                PermitEndOfHuntingReportService.viewById(permit.id).finally(function () {
                    $state.reload();
                });
            };

            $scope.proposedHarvestsExist = function () {
                return !!_.find(permit.harvests, function (h) {
                    return h.stateAcceptedToHarvestPermit === 'PROPOSED';
                });
            };

            $scope.createHarvestForListPermit = function (permit) {
                DiaryEntryService.createHarvestForPermit(permit, 'none');
            };

            $scope.createHarvestReportForPermit = function (permit, species) {
                var harvest = {
                    gameSpeciesCode: species.code,
                    permitNumber: permit.permitNumber
                };
                HarvestReportFieldsAndSeasons.validsForAllSeasonsAndPermits()
                    .then(function (fieldsAndSeasons) {
                        var fs = _.find(fieldsAndSeasons, function (fs) {
                            return fs.season === null && fs.fields.species.code === species.code;
                        });
                        HarvestReportService.create(fs, harvest, false)
                            .then(function () {
                                $state.reload();
                            }, onFailure);
                    });
            };
        })

    .controller('EditPermitContactPersonsController',
        function ($scope, $uibModalInstance, $uibModal, Helpers, NotificationService, HarvestPermits, permit) {
            $scope.permit = permit;
            $scope.cancel = function () {
                $uibModalInstance.close();
            };

            var saveOk = function () {
                $uibModalInstance.close('ok');
                NotificationService.showDefaultSuccess();

            };
            $scope.save = function (permit) {
                _.each(permit.contactPersons, function (p) {
                    delete p.$isNew;
                });
                HarvestPermits.updateContactPersons({id: permit.id}, permit.contactPersons).$promise
                    .then(saveOk, NotificationService.showDefaultFailure);
            };

            $scope.removePerson = function (permit, person) {
                _.remove(permit.contactPersons, function (p) {
                    return p.id === person.id;
                });
                $scope.changes = true;
            };

            var findPerson = function (showSsnSearch, showPermitNumberSearch) {
                return $uibModal.open({
                    templateUrl: 'harvestreport/findperson.html',
                    resolve: {
                        showSsnSearch: Helpers.wrapToFunction(showSsnSearch),
                        showPermitNumberSearch: Helpers.wrapToFunction(showPermitNumberSearch)
                    },
                    controller: 'HarvestReportFindPersonController'
                }).result;
            };

            $scope.addNewPerson = function (permit) {
                var onFailure = function (failure) {
                    if (failure === 'error' || failure.status) {
                        NotificationService.showDefaultFailure();
                    }
                };
                var personWithHunterNumberDTOtoHarvestPermitContactPersonDTO = function (dto) {
                    delete dto.extendedName;
                    dto.canBeDeleted = true;
                };
                var cb = function (personInfo) {
                    $scope.changes = true;
                    var existing = _.find(permit.contactPersons, function (p) {
                        return p.id === personInfo.id;
                    });
                    if (existing) {
                        existing.$isNew = true;
                    } else {
                        personWithHunterNumberDTOtoHarvestPermitContactPersonDTO(personInfo);
                        personInfo.$isNew = true;
                        permit.contactPersons.push(personInfo);
                    }
                };
                findPerson(false, false).then(cb, onFailure);
            };
        })

    .controller('PermitViewReportController',
        function ($scope, HarvestReportService) {
            $scope.editHarvestReport = function (id, callback) {
                HarvestReportService.editById(id).then(callback, callback);
            };
            $scope.viewHarvestReport = function (id, callback) {
                HarvestReportService.showById(id).then(callback, callback);
            };
        })

    .controller('PermitEndOfHuntingReportEditController',
        function ($scope, $uibModalInstance, permit, report, HarvestReports, NotificationService, ActiveRoleService) {
            $scope.permit = permit;

            $scope.harvests = _.filter(permit.harvests, function (h) {
                return h.stateAcceptedToHarvestPermit === 'ACCEPTED';
            });
            $scope.report = report || {canEdit: true};

            $scope.viewState = {moderatorView: ActiveRoleService.isModerator()};

            var success = function () {
                NotificationService.showDefaultSuccess();
                $uibModalInstance.close();
            };
            $scope.save = function () {

                var method = permit.harvestsAsList ? HarvestReports.createForPermit : HarvestReports.createEndOfHuntForPermit;

                // send permit id and rev, make sure that when harvests are accepted/rejected the permit.rev is changed
                var params = {permitId: permit.id, permitRev: permit.rev};

                method(null, params).$promise.then(success, NotificationService.showDefaultFailure);
            };
            $scope.remove = function () {
                HarvestReports.delete({id: permit.endOfHuntingReport.id}).$promise
                    .then(success, NotificationService.showDefaultFailure);
                console.log('PermitEndOfHuntingReportEditController remove');
            };
        }
    )
;
