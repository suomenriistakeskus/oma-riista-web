'use strict';

angular.module('app.clubhunting.data', [])

    .run(function ($rootScope, ClubHuntingPersistentState) {
        // Reset on login and logout
        _.forEach(['loginRequired', 'loginCancelled'], function (eventName) {
            $rootScope.$on('event:auth-' + eventName, function () {
                ClubHuntingPersistentState.clear();
            });
        });
    })

    .service('ClubHuntingPersistentState', function (LocalStorageService) {
        // Load value from LocalStorage only once to avoid issues with multiple browser windows
        var keyNames = ['selectedGroupId', 'selectedSpeciesCode', 'selectedHuntingYear'];
        var values = _(keyNames)
            .map(LocalStorageService.getKey)
            .map(_.parseInt)
            .map(function (value) {
                return _.isFinite(value) ? value : null;
            })
            .filter()
            .value();
        var state = _.zipObject(keyNames, values);

        function store(key, value) {
            state[key] = value;
            LocalStorageService.setKey(key, value);
        }

        function load(key) {
            return state[key];
        }

        // Create accessor methods
        var self = this;
        _.forEach(keyNames, function (key) {
            var methodName = key.charAt(0).toUpperCase() + key.substring(1);
            self['get' + methodName] = _.partial(load, key);
            self['set' + methodName] = _.partial(store, key);
        });

        this.clear = function () {
            state = {};
            _.forEach(keyNames, function (key) {
                LocalStorageService.setKey(key, null);
            });
        };
    })

    .service('ClubHuntingViewData', function ($q, ActiveRoleService, ClubGroupDiary, ClubGroupDiaryEntryRepository,
                                              ClubGroups, ClubHuntingPersistentState, GameSpeciesCodes, GIS, Helpers,
                                              HuntingYearService) {
        // Expose only necessary data items.
        var data = {
            club: null,
            groupStatus: null,
            huntingYears: [],
            huntingYear: null,
            speciesWithinSelectedYear: [],
            species: null,
            huntingGroups: null,
            huntingGroup: null,
            huntingDays: [],
            huntingArea: null,
            rejected: {},
            diaryForGroup: [],
            fromMooseDataCard: false,
            huntingFinished: false
        };

        // Game species grouped by selected hunting year are cached into this within initialization
        // for improving performance.
        var speciesGroupedByYear = null;

        // All hunting groups are cached in internal variable.
        var allHuntingGroups = null;

        this.get = function () {
            return data;
        };

        function isEntryRejected(entry) {
            return _.has(data.rejected, entry.type) && _.includes(data.rejected[entry.type], entry.id);
        }

        function linkGameEntriesToHuntingDays(huntingDays, diaryForGroup, groupStatus) {
            var formatDate = function (date) {
                return moment(date).format('YYYY-MM-DD');
            };

            var diaryWithHuntingDay = _.groupBy(diaryForGroup, 'huntingDayId');
            var diaryWithoutHuntingDay = _.chain(diaryForGroup)
                .filter(_.negate(isEntryRejected))
                .filter(_.negate(_.property('huntingDayId')))
                .groupBy(function (diaryEntry) {
                    return formatDate(diaryEntry.pointOfTime);
                })
                .value();

            var existingHuntingDates = _(huntingDays).map('startDate').map(formatDate).value();
            var allHuntingDates = _.keys(diaryWithoutHuntingDay);
            var missingHuntingDays = _.difference(allHuntingDates, existingHuntingDates);

            var phantomHuntingDates = _.map(missingHuntingDays, function (key) {
                return {
                    id: null,
                    startDate: key,
                    endDate: key,
                    gameEntries: []
                };
            });

            // Concat in place
            huntingDays.push.apply(huntingDays, phantomHuntingDates);

            _.forEach(huntingDays, function (huntingDay) {
                var byStartDate, byEndDate, byHuntingDayId;

                var startKey = formatDate(huntingDay.startDate);
                var endKey = formatDate(huntingDay.endDate);

                byStartDate = diaryWithoutHuntingDay[startKey];

                if (endKey !== startKey) {
                    byEndDate = diaryWithoutHuntingDay[endKey];
                }

                if (huntingDay.id) {
                    byHuntingDayId = diaryWithHuntingDay[huntingDay.id];
                }

                var diary = _.chain([byStartDate, byEndDate, byHuntingDayId])
                    .filter()
                    .flatten()
                    .orderBy('pointOfTime', 'desc')
                    .value();

                huntingDay.gameEntries = diary;
                huntingDay.canEdit = groupStatus.canEditHuntingDay;
                huntingDay.totalHarvestSpecimenCount = _(diary).filter(_.method('isHarvest')).sumBy('totalSpecimenAmount');
                huntingDay.totalObservationSpecimenCount = _(diary).filter(_.method('isObservation')).sumBy('totalSpecimenAmount');
            });
        }

        function loadData() {
            data.groupStatus = null;
            data.diaryForGroup = [];
            data.huntingDays = [];
            data.rejected = {};

            var clubId = _.get(data, 'club.id');
            var groupId = _.get(data, 'huntingGroup.id');

            if (!clubId || !groupId) {
                return $q.when(data);
            }

            var groupParams = {id: groupId};
            var clubAndGroupId = {
                id: groupId,
                clubId: clubId
            };

            var huntingDaysPromise = ClubGroupDiary.huntingDays(groupParams).$promise;
            var huntingAreaPromise = ClubGroups.huntingArea(clubAndGroupId).$promise;
            var statusPromise = ClubGroups.status(clubAndGroupId).$promise;
            var diaryEntriesPromise = ClubGroupDiaryEntryRepository.list(groupParams).$promise;
            var rejectedPromise = ClubGroupDiary.listRejected(groupParams);

            var promiseArray = [statusPromise, diaryEntriesPromise, huntingDaysPromise, huntingAreaPromise, rejectedPromise];

            return $q.all(promiseArray).then(function (promises) {
                data.groupStatus = promises[0];
                data.diaryForGroup = promises[1];
                data.huntingDays = promises[2];
                data.huntingArea = promises[3];
                data.rejected = promises[4];

                linkGameEntriesToHuntingDays(data.huntingDays, data.diaryForGroup, data.groupStatus);

                return data;
            });
        }

        function selectHuntingYearSpeciesAndGroup(huntingYear, speciesCode, groupId) {
            if (!huntingYear) {
                huntingYear = HuntingYearService.getCurrent();
            }

            data.speciesWithinSelectedYear = _.get(speciesGroupedByYear, huntingYear, []);
            data.huntingYear = _.size(data.speciesWithinSelectedYear) > 0 ? huntingYear : null;
            data.species = null;
            data.huntingGroup = null;

            if (data.huntingYear) {
                if (speciesCode) {
                    data.species = _.find(data.speciesWithinSelectedYear, {
                        code: speciesCode
                    });
                }

                if (!data.species) {
                    data.species = data.speciesWithinSelectedYear[0];
                }

                speciesCode = data.species.code;
                data.huntingGroups = _.filter(allHuntingGroups, function (group) {
                    return group.huntingYear === huntingYear && group.species.code === speciesCode;
                });

                if (_.size(data.huntingGroups) > 0) {
                    if (groupId) {
                        data.huntingGroup = _.find(data.huntingGroups, {
                            id: groupId
                        });
                    }

                    if (!data.huntingGroup) {
                        data.huntingGroup = data.huntingGroups[0];
                    }
                }
            }

            ClubHuntingPersistentState.setSelectedHuntingYear(data.huntingYear);
            ClubHuntingPersistentState.setSelectedSpeciesCode(data.species ? data.species.code : null);

            if (data.huntingGroup) {
                ClubHuntingPersistentState.setSelectedGroupId(data.huntingGroup.id);

                data.huntingFinished = data.huntingGroup.huntingFinished;
                data.fromMooseDataCard = data.huntingGroup.fromMooseDataCard;
            } else {
                ClubHuntingPersistentState.setSelectedGroupId(null);

                data.huntingFinished = false;
                data.fromMooseDataCard = false;
            }
        }

        this.initialize = function (club, huntingGroups) {
            allHuntingGroups = huntingGroups;

            speciesGroupedByYear = _(huntingGroups)
                .groupBy('huntingYear')
                .mapValues(function (groups) {
                    return _(groups).map('species').uniqBy('code').value();
                })
                .value();

            angular.extend(data, {
                club: club,
                huntingYears: _(huntingGroups).map('huntingYear').uniq().sort().value()
            });

            var storedHuntingYear = ClubHuntingPersistentState.getSelectedHuntingYear();
            var storedSpeciesCode = ClubHuntingPersistentState.getSelectedSpeciesCode();
            var storedGroupId = ClubHuntingPersistentState.getSelectedGroupId();

            selectHuntingYearSpeciesAndGroup(storedHuntingYear, storedSpeciesCode, storedGroupId);

            return loadData();
        };

        this.changeHuntingYear = function (huntingYear) {
            selectHuntingYearSpeciesAndGroup(huntingYear, null, null);
            return loadData();
        };

        this.changeSpecies = function (speciesCode) {
            selectHuntingYearSpeciesAndGroup(data.huntingYear, speciesCode, null);
            return loadData();
        };

        this.changeGroup = function (groupId) {
            selectHuntingYearSpeciesAndGroup(data.huntingYear, data.species.code, groupId);
            return loadData();
        };

        this.filterDiary = function (beginDate, endDate, showOnlyRejected, showHarvests, showObservations) {
            return _.filter(data.diaryForGroup, function (entry) {
                if (entry.isHarvest() && !showHarvests || entry.isObservation() && !showObservations){
                    return false;
                }

                if (!Helpers.dateWithinRange(entry.pointOfTime, beginDate, endDate)) {
                    return false;
                }

                var isLinkedWithHuntingDay = !_.isNil(entry.huntingDayId);

                if (showOnlyRejected) {
                    return isEntryRejected(entry) && !isLinkedWithHuntingDay;
                }

                var isProposedWithinDeerHuntingObservation = entry.isObservationWithinDeerHunting() && !isLinkedWithHuntingDay;

                return !(isEntryRejected(entry) || isProposedWithinDeerHuntingObservation);
            });
        };

        this.filterHuntingDays = function (beginDate, endDate) {
            return _.filter(data.huntingDays, function (huntingDay) {
                return Helpers.dateWithinRange(huntingDay.startDate, beginDate, endDate);
            });
        };

        this.getSelectedHuntingYear = function () {
            return data.huntingYear;
        };

        this.getSelectedSpeciesCode = function () {
            return _.get(data, 'species.code');
        };

        this.getHuntingGroupId = function () {
            return _.get(data, 'huntingGroup.id');
        };

        this.getHuntingClubId = function () {
            return _.get(data, 'club.id');
        };

        this.find = function (type, id) {
            return _.find(data.diaryForGroup, function (entry) {
                return entry.id === id && entry.type === type;
            });
        };

        this.findHuntingDay = function (where) {
            return _.isObject(where) ? _.find(data.huntingDays, where) : null;
        };

        this.isMooseGroupSelected = function () {
            return !!(data.species && GameSpeciesCodes.isMoose(data.species.code) && data.huntingGroup);
        };

        this.isWhiteTailedDeerGroupSelected = function () {
            return !!(data.species && GameSpeciesCodes.isWhiteTailedDeer(data.species.code) && data.huntingGroup);
        };

        this.isAddHarvestVisible = function () {
            return _.get(data.groupStatus, 'canCreateHarvest', false);
        };

        this.isAddObservationVisible = function () {
            return _.get(data.groupStatus, 'canCreateObservation', false);
        };

        this.isCreateHuntingDayVisible = function () {
            return _.get(data.groupStatus, 'canCreateHuntingDay', false);
        };

        this.isEditPermitPossible = function () {
            return _.get(data.groupStatus, 'canEditPermit', false);
        };

        this.isObservationsViable = function () {
            return this.isMooseGroupSelected() || this.isWhiteTailedDeerGroupSelected();
        };
    });
