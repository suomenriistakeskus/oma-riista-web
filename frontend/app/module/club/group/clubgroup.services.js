'use strict';

angular.module('app.clubgroup.services', [])

    .factory('ClubGroups', function ($resource) {
        return $resource('api/v1/club/:clubId/group/:id', {'clubId': '@clubId', 'id': '@id'}, {
            'query': {method: 'GET', isArray: true},
            'get': {method: 'GET'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'},
            'huntingYears': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/huntingyears',
                isArray: true
            },
            'huntingArea': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/huntingArea'
            },
            'status': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/status'
            },
            'huntingDays': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/huntingdays',
                isArray: true
            },
            'availablePermits': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/permits',
                isArray: true
            },
            'permitSpeciesAmount': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/permit-species-amount'
            },
            'rejectEntry': {
                method: 'POST',
                url: 'api/v1/club/:clubId/group/:id/rejectentry'
            },
            'listRejected': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/rejected'
            },
            'copy': {
                method: 'POST',
                url: 'api/v1/club/:clubId/group/:id/copy'
            }
        });
    })

    .factory('ClubGroupDiary', function ($resource, DiaryEntryRepositoryFactory) {
        var repository = $resource('api/v1/club/:clubId/group/:id', {"clubId": "@clubId", "id": "@id"}, {
            'list': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/diary',
                isArray: true
            }
        });

        // Add methods
        return DiaryEntryRepositoryFactory.decorateRepository(repository);
    })

    .service('ClubGroupAreas', function ($q, ClubAreas) {
        this.loadGroupAreaOptions = function (huntingGroup) {
            var params = {
                clubId: huntingGroup.clubId,
                year: huntingGroup.huntingYear,
                activeOnly: true
            };

            return params.clubId && params.year
                ? ClubAreas.query(params).$promise
                : $q.when([]);
        };

        this.loadGroupArea = function (huntingGroup) {
            return huntingGroup.huntingAreaId && huntingGroup.clubId
                ? ClubAreas.get({id: huntingGroup.huntingAreaId, clubId: huntingGroup.clubId}).$promise
                : $q.when(null);
        };
    })

    .service('ClubGroupPermits', function ($q, ClubGroups) {
        this.loadGroupPermits = function (huntingGroup) {
            return ClubGroups.availablePermits({
                clubId: huntingGroup.clubId,
                gameSpeciesCode: huntingGroup.gameSpeciesCode,
                huntingYear: huntingGroup.huntingYear
            }).$promise;
        };
    })

    .service('ClubGroupService', function ($q, $uibModal, HuntingYearService, ClubGroups, GameDiaryParameters, GameSpeciesCodes) {
        function showModal(clubGroup, mooseDataCardGroupExistsForCurrentYear) {
            return GameDiaryParameters.query().$promise.then(function (parameters) {
                var availableSpecies = _.filter(parameters.species, function (s) {
                    return GameSpeciesCodes.isMoose(s.code) || GameSpeciesCodes.isPermitBasedDeer(s.code);
                });

                var modalInstance = $uibModal.open({
                    templateUrl: 'club/group/form.html',
                    resolve: {
                        group: function () {
                            if (!clubGroup.huntingYear) {
                                var currentYear = HuntingYearService.getCurrent();
                                var speciesCode = clubGroup.gameSpeciesCode;
                                var defaultSelectedHuntingYear = currentYear;

                                if (speciesCode && GameSpeciesCodes.isMoose(speciesCode) && mooseDataCardGroupExistsForCurrentYear) {
                                    defaultSelectedHuntingYear++;
                                }

                                clubGroup.huntingYear = defaultSelectedHuntingYear;
                            }
                            return clubGroup;
                        },
                        parameters: function () {
                            return parameters;
                        },
                        areas: function (ClubGroupAreas) {
                            return ClubGroupAreas.loadGroupAreaOptions(clubGroup);
                        },
                        permits: function (ClubGroupPermits) {
                            return ClubGroupPermits.loadGroupPermits(clubGroup);
                        },
                        availableSpecies: function () {
                            return availableSpecies;
                        },
                        availableHuntingYearsBySpeciesCode: function () {
                            var result = {};

                            var yearsObj = HuntingYearService.currentAndNextObj();
                            var groupDoesNotHaveId = !(_.get(clubGroup, 'id'));

                            _.forEach(availableSpecies, function(species) {
                                var currentYearNotIncludedInSelection = groupDoesNotHaveId &&
                                    mooseDataCardGroupExistsForCurrentYear &&
                                    GameSpeciesCodes.isMoose(species.code);

                                result[species.code] = currentYearNotIncludedInSelection ? yearsObj.slice(1) : yearsObj;
                            });

                            return result;
                        }
                    },
                    controller: 'ClubGroupFormController',
                    controllerAs: '$ctrl'
                });

                return modalInstance.result.then(function (clubGroup) {
                    delete clubGroup.species;

                    var saveMethod = clubGroup.id ? ClubGroups.update : ClubGroups.save;

                    return saveMethod({clubId: clubGroup.clubId}, clubGroup).$promise;
                });
            });
        }

        this.createGroup = function (clubId, mooseDataCardGroupExistsForCurrentYear) {
            return showModal({
                clubId: clubId,
                gameSpeciesCode: GameSpeciesCodes.MOOSE
            }, mooseDataCardGroupExistsForCurrentYear);
        };

        this.editGroup = function (huntingClubGroup) {
            var params = {clubId: huntingClubGroup.clubId, id: huntingClubGroup.id};

            return ClubGroups.get(params).$promise.then(function (reloaded) {
                return showModal(reloaded);
            });
        };

        this.delete = function (huntingClubGroup) {
            var params = {clubId: huntingClubGroup.clubId, id: huntingClubGroup.id};
            return ClubGroups.delete(params).$promise;
        };

        this.groupsToYearSelection = function (groups, possiblySelectedYear) {
            var groupsByYear = _.groupBy(groups, 'huntingYear');
            var years = [];
            _.forOwn(groupsByYear, function (val, key) {
                var y = HuntingYearService.toObj(parseInt(key));
                y.groups = val;
                years.push(y);
            });
            years = _.sortBy(years, function (year) {
                return -1 * year.year;
            });
            var selected = parseInt(possiblySelectedYear) || HuntingYearService.getCurrent();
            if (years && years.length === 1) {
                selected = years[0].year;
            }
            return {
                selected: selected,
                values: years || []
            };
        };

        this.copy = function (clubGroup) {
            var groupCopy = angular.copy(clubGroup);
            var modalInstance = $uibModal.open({
                templateUrl: 'club/group/copy.html',
                resolve: {
                    group: _.constant(groupCopy),
                    areas: function (ClubGroupAreas) {
                        return ClubGroupAreas.loadGroupAreaOptions(groupCopy);
                    }
                },
                controller: 'ClubGroupCopyController'
            });

            return modalInstance.result.then(function () {
                var params = {clubId: clubGroup.clubId, id: clubGroup.id};
                var data = {huntingAreaId: groupCopy.huntingAreaId, huntingYear: groupCopy.huntingYear};
                return ClubGroups.copy(params, data).$promise;
            });
        };
    });
