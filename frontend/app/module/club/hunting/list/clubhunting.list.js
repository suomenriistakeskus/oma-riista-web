'use strict';

angular.module('app.clubhunting.list', [])
    .run(function ($rootScope, ClubHuntingPersistentState) {
        // Reset on login and logout
        _.forEach(['loginRequired', 'loginCancelled'], function(eventName) {
            $rootScope.$on('event:auth-' + eventName, function () {
                ClubHuntingPersistentState.clear();
            });
        });
    })

    .directive('clubHuntingFilters', function () {
        return {
            restrict: 'A',
            scope: {
                onChange: '&onChange'
            },
            templateUrl: 'club/hunting/list/filters.html',
            controller: 'ClubHuntingEntryFiltersController',
            controllerAs: 'ctrl',
            bindToController: true
        };
    })

    .directive('clubHuntingList', function () {
        return {
            restrict: 'A',
            scope: {
                huntingDays: '=clubHuntingList',
                onSelectEntry: '&selectEntry',
                onCreateHuntingDay: "&createDay",
                onEditHuntingDay: "&editDay"
            },
            templateUrl: 'club/hunting/list/days.html',
            controller: 'ClubHuntingEntryListController',
            controllerAs: 'ctrl',
            bindToController: true
        };
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

    .service('ClubHuntingViewData', function ($q, ActiveRoleService, GIS, Helpers, HuntingYearService,
                                              ClubHuntingPersistentState, GameSpeciesCodes,
                                              ClubGroupDiary, ClubGroups) {
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

            var existingHuntingDates = _(huntingDays).pluck('startDate').map(formatDate).value();
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
                    .sortByOrder('pointOfTime', false)
                    .value();

                huntingDay.gameEntries = diary;
                huntingDay.canEdit = groupStatus.canEditHuntingDay;
                huntingDay.totalHarvestSpecimenCount = _(diary).filter(_.method('isHarvest')).sum('totalSpecimenAmount');
                huntingDay.totalObservationSpecimenCount = _(diary).filter(_.method('isObservation')).sum('totalSpecimenAmount');
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

            var groupParams = {
                id: groupId,
                clubId: clubId
            };

            var huntingDaysPromise = ClubGroups.huntingDays(groupParams).$promise;
            var huntingAreaPromise = ClubGroups.huntingArea(groupParams).$promise;
            var statusPromise = ClubGroups.status(groupParams).$promise;
            var diaryPromise = ClubGroupDiary.list(groupParams).$promise;
            var rejectedPromise = ClubGroups.listRejected(groupParams);

            var promiseArray = [statusPromise, diaryPromise, huntingDaysPromise, huntingAreaPromise, rejectedPromise];

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
                    data.species = _.find(data.speciesWithinSelectedYear, 'code', speciesCode);
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
                        data.huntingGroup = _.find(data.huntingGroups, 'id', groupId);
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
                    return _(groups).map('species').uniq('code').value();
                })
                .value();

            angular.extend(data, {
                club: club,
                huntingYears: _(huntingGroups).map('huntingYear').uniq().value()
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

        this.filterDiary = function (beginDate, endDate, showOnlyRejected) {
            return _.filter(data.diaryForGroup, function (entry) {
                if (showOnlyRejected) {
                    return !entry.huntingDayId && isEntryRejected(entry) && Helpers.dateWithinRange(entry.pointOfTime, beginDate, endDate);
                }
                return !isEntryRejected(entry) && Helpers.dateWithinRange(entry.pointOfTime, beginDate, endDate);
            });
        };

        this.filterHuntingDays = function (beginDate, endDate) {
            return _.filter(data.huntingDays, function (huntingDay) {
                return Helpers.dateWithinRange(huntingDay.startDate, beginDate, endDate);
            });
        };

        this.find = function (type, id) {
            return _.find(data.diaryForGroup, function (entry) {
                return entry.id === id && entry.type === type;
            });
        };

        this.findHuntingDay = function (where) {
            return _.isObject(where) ? _.findWhere(data.huntingDays, where) : null;
        };

        this.getHuntingAreaBounds = function () {
            var bbox = _.get(data, 'huntingArea.features[0].bbox');
            return GIS.getBoundsFromGeoJsonBbox(bbox);
        };

        this.isMooseGroupSelected = function () {
            return data.species && GameSpeciesCodes.isMoose(data.species.code) && data.huntingGroup;
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

        this.isExportVisible = function () {
            return _.get(data.groupStatus, 'canExportData', false);
        };

        this.isEditPermitPossible = function () {
            return _.get(data.groupStatus, 'canEditPermit', false);
        };
    })

    .controller('ClubHuntingEntryFiltersController', function ($q, $timeout, $state,
                                                               NotificationService,
                                                               HuntingYearService,
                                                               ClubHunting,
                                                               ClubHuntingViewData,
                                                               ClubGroupService,
                                                               ClubTodoService,
                                                               ObservationFieldRequirements) {

        var ctrl = this;
        var observationStatKeys = ObservationFieldRequirements.getAllMooseAmountFields();

        this.huntingData = ClubHuntingViewData.get();
        this.beginDate = null;
        this.endDate = null;
        this.onlyRejected = false;

        function initialize() {
            var huntingYear;

            if (ctrl.huntingData.huntingYear) {
                huntingYear = ctrl.huntingData.huntingYear;
                ctrl.beginDate = HuntingYearService.getBeginDateStr(huntingYear);
                ctrl.endDate = HuntingYearService.getEndDateStr(huntingYear);
            } else {
                huntingYear = HuntingYearService.getCurrent();
            }

            ctrl.mooseGroupSelected = ClubHuntingViewData.isMooseGroupSelected();
            ClubTodoService.showTodo(ctrl.huntingData.club.id, huntingYear);

            ctrl.filterContent();
        }

        function calculateStatistics(entryList) {
            function collectStats(stats, entry) {
                stats.entryCount = (stats.entryCount || 0) + 1;

                if (entry.isMoose() || entry.isPermitBasedDeer()) {
                    if (entry.isHarvest() && entry.huntingDayId) {
                        _.each(entry.specimens, function (specimen) {
                            if (specimen.gender && specimen.age) {
                                var statsKey = entry.type + '_' + specimen.gender + '_' + specimen.age;
                                stats[statsKey] = (stats[statsKey] || 0) + 1;
                            }
                        });
                    }
                    // Count observations only of selected species.
                    // Since observations are shown only for moose, count only moose observations
                    if (entry.isMoose() && entry.isObservation() && entry.huntingDayId) {
                        _.each(observationStatKeys, function (statsKey) {
                            stats[statsKey] = (stats[statsKey] || 0) + (entry[statsKey] || 0);
                        });
                    }
                }

                return stats;
            }

            return _.reduce(entryList, collectStats, {});
        }

        this.filterContent = function () {
            var huntingDays = ClubHuntingViewData.filterHuntingDays(ctrl.beginDate, ctrl.endDate);
            var diary = ClubHuntingViewData.filterDiary(ctrl.beginDate, ctrl.endDate, ctrl.onlyRejected);
            var stats = calculateStatistics(diary);

            ctrl.mooseGroupSelected = ClubHuntingViewData.isMooseGroupSelected();

            ctrl.onChange({
                huntingDays: huntingDays,
                diary: diary,
                stats: stats
            });
        };

        this.onGroupChanged = function (huntingGroup) {
            if (huntingGroup && huntingGroup.id) {
                ctrl.onlyRejected = false;
                ClubHuntingViewData.changeGroup(huntingGroup.id).then(ctrl.filterContent);
            }
        };

        this.onSpeciesChanged = function (species) {
            if (species) {
                ctrl.onlyRejected = false;
                ClubHuntingViewData.changeSpecies(species.code).then(ctrl.filterContent);
            }
        };

        this.onHuntingYearChanged = function (huntingYear) {
            if (huntingYear) {
                ctrl.onlyRejected = false;
                ctrl.beginDate = HuntingYearService.getBeginDateStr(huntingYear);
                ctrl.endDate = HuntingYearService.getEndDateStr(huntingYear);

                ClubHuntingViewData.changeHuntingYear(huntingYear).then(ctrl.filterContent);
                ClubTodoService.showTodo(ctrl.huntingData.club.id, huntingYear);
            }
        };

        this.onDateChange = function () {
            ctrl.filterContent();
        };

        this.onOnlyRejectedChange = function () {
            ctrl.filterContent();
        };

        this.isEditPermitPossible = ClubHuntingViewData.isEditPermitPossible;

        this.editHuntingGroup = function () {
            var huntingGroup = ctrl.huntingData.huntingGroup;

            if (huntingGroup) {
                var modalPromise = ClubGroupService.editGroup(huntingGroup);

                NotificationService.handleModalPromise(modalPromise).then(function (result) {
                    $state.reload();
                });
            }
        };

        // This timeout was added to fix leaflet-directive behaviour. Otherwise changes are not visible.
        $timeout(function () {
            initialize();
        });
    })

    .controller('ClubHuntingEntryListController', function ($scope, ClubHuntingViewData) {
        var ctrl = this;

        $scope.$on('huntingDaySelected', function (event, selected) {
            var selectedDay;
            if (selected.id) {
                selectedDay = _.find(ctrl.huntingDays, 'id', selected.id);
            } else {
                selectedDay = _.find(ctrl.huntingDays, 'startDate', selected.startDate);
            }

            if (selectedDay) {
                selectedDay.isOpen = true;
            }
        });

        this.createHuntingDay = function ($event, huntingDay) {
            $event.stopPropagation();
            this.onCreateHuntingDay({'startDateAsString': huntingDay.startDate});
        };

        this.editHuntingDay = function ($event, huntingDay) {
            $event.stopPropagation();
            this.onEditHuntingDay({'id': huntingDay.id});
        };

        this.getDayToggleClasses = function (day) {
            return {
                'glyphicon': true,
                'glyphicon-chevron-down': day.isOpen,
                'glyphicon-chevron-right': !day.isOpen
            };
        };

        this.onEntryClick = function (entry, day) {
            ctrl.onSelectEntry({
                diaryEntry: entry,
                huntingDay: day
            });
        };

        this.showHuntingDayAsterisk = function (day) {
            return !(day.id && _.every(day.gameEntries, 'huntingDayId'));
        };

        this.getSpecimenType = function (specimen) {
            var str = null;

            if (specimen) {
                if (_.isString(specimen.gender)) {
                    str = specimen.gender;
                }

                if (_.isString(specimen.age)) {
                    str = (str ? str + '_' : '') + specimen.age;
                }
            }

            return str || 'UNKNOWN';
        };

        this.isMooseGroupSelected = function () {
            return ClubHuntingViewData.isMooseGroupSelected();
        };
    });
