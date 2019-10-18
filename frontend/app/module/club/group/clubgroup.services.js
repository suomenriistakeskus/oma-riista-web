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
            'availablePermits': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/permits',
                isArray: true
            },
            'permitSpeciesAmount': {
                method: 'GET',
                url: 'api/v1/club/:clubId/group/:id/permit-species-amount'
            },
            'copy': {
                method: 'POST',
                url: 'api/v1/club/:clubId/group/:id/copy'
            }
        });
    })

    .factory('ClubGroupDiaryEntryRepository', function ($resource, DiaryEntryRepositoryFactory) {
        var repository = $resource('api/v1/club/group/:id', {"id": "@id"}, {
            'list': {
                method: 'GET',
                url: 'api/v1/club/group/:id/diary',
                isArray: true
            }
        });

        // Add methods
        return DiaryEntryRepositoryFactory.decorateRepository(repository);
    })

    .factory('ClubGroupDiary', function ($resource) {
        return $resource('api/v1/club/group/:id', {"id": "@id"}, {
            'huntingDays': {
                method: 'GET',
                url: 'api/v1/club/group/:id/huntingdays',
                isArray: true
            },
            'listRejected': {
                method: 'GET',
                url: 'api/v1/club/group/:id/rejected'
            },
            'rejectEntry': {
                method: 'POST',
                url: 'api/v1/club/group/:id/rejectentry'
            },
            'editHarvestLocation': {
                method: 'POST',
                url: 'api/v1/club/group/harvest/:harvestId/editlocation'
            },
            'editObservationLocation': {
                method: 'POST',
                url: 'api/v1/club/group/observation/:observationId/editlocation'
            }
        });
    })

    .service('ClubGroupAreas', function ($q, ClubAreas) {
        this.loadGroupAreaOptions = function (huntingGroup) {
            var clubId = huntingGroup.clubId;
            var huntingYear = huntingGroup.huntingYear;

            return clubId && huntingYear ? ClubAreas.query({
                clubId: clubId,
                year: huntingYear,
                activeOnly: true,
                includeEmpty: false
            }).$promise : $q.when([]);
        };

        this.loadGroupArea = function (huntingGroup) {
            return huntingGroup.huntingAreaId
                ? ClubAreas.get({id: huntingGroup.huntingAreaId}).$promise
                : $q.when(null);
        };
    })

    .service('ClubGroupPermits', function ($q, ClubGroups) {
        this.loadGroupPermits = function (huntingGroup) {
            var clubId = huntingGroup.clubId;
            var gameSpeciesCode = huntingGroup.gameSpeciesCode;
            var huntingYear = huntingGroup.huntingYear;

            return clubId && gameSpeciesCode && huntingYear ? ClubGroups.availablePermits({
                clubId: clubId,
                gameSpeciesCode: gameSpeciesCode,
                huntingYear: huntingYear
            }).$promise : $q.when([]);
        };
    })

    .service('ClubGroupService', function ($q, $uibModal, HuntingYearService, ClubGroups, GameDiaryParameters, GameSpeciesCodes) {
        function showModal(clubGroup, mooseDataCardGroupExistsForCurrentYear) {
            return GameDiaryParameters.query().$promise.then(function (parameters) {
                var availableSpecies = _.filter(parameters.species, function (s) {
                    return GameSpeciesCodes.isPermitBasedMooselike(s.code);
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

                            _.forEach(availableSpecies, function (species) {
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

            var huntingYearOptions = _.map(groupsByYear, function (groups, huntingYearStr) {
                var opt = HuntingYearService.toObj(parseInt(huntingYearStr));
                opt.groups = groups;
                return opt;
            });

            return {
                values: _.orderBy(huntingYearOptions, 'year', 'desc'),
                selected: resolveOptionOrDefault(huntingYearOptions, 'year',
                    possiblySelectedYear, HuntingYearService.getCurrent())
            };
        };

        function resolveOptionOrDefault(options, keyName, selectedValue, defaultValue) {
            if (selectedValue && _.some(options, [keyName, selectedValue])) {
                return selectedValue;
            }

            if (defaultValue && _.some(options, [keyName, defaultValue])) {
                return defaultValue;
            }

            return options.length > 0 ? _.get(options[0], keyName) : null;
        }

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
