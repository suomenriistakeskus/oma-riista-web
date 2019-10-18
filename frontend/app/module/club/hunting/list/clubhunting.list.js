'use strict';

angular.module('app.clubhunting.list', [])

    .component('clubHuntingFilters', {
        templateUrl: 'club/hunting/list/filters.html',
        bindings: {
            onFilterChange: '&'
        },
        controller: function ($q, $timeout, $state,
                              NotificationService,
                              HuntingYearService,
                              ClubHuntingViewData,
                              ClubGroupService,
                              ClubTodoService) {
            var $ctrl = this;

            $ctrl.huntingData = ClubHuntingViewData.get();
            $ctrl.beginDate = null;
            $ctrl.endDate = null;
            $ctrl.onlyRejected = false;

            $ctrl.$onInit = function () {
                var huntingYear;

                if ($ctrl.huntingData.huntingYear) {
                    huntingYear = $ctrl.huntingData.huntingYear;
                    $ctrl.beginDate = HuntingYearService.getBeginDateStr(huntingYear);
                    $ctrl.endDate = HuntingYearService.getEndDateStr(huntingYear);
                } else {
                    huntingYear = HuntingYearService.getCurrent();
                }

                ClubTodoService.showTodo(ClubHuntingViewData.getHuntingClubId(), huntingYear);

                // This timeout was added to fix leaflet-directive behaviour. Otherwise changes are not visible.
                $timeout(function () {
                    filterContent();
                });
            };

            function filterContent() {
                $ctrl.mooseGroupSelected = ClubHuntingViewData.isMooseGroupSelected();

                $ctrl.onFilterChange({
                    filter: {
                        beginDate: $ctrl.beginDate,
                        endDate: $ctrl.endDate,
                        onlyRejected: $ctrl.onlyRejected
                    }
                });
            }

            $ctrl.onGroupChanged = function (huntingGroup) {
                if (huntingGroup && huntingGroup.id) {
                    $ctrl.onlyRejected = false;
                    ClubHuntingViewData.changeGroup(huntingGroup.id).then(filterContent);
                }
            };

            $ctrl.onSpeciesChanged = function (species) {
                if (species) {
                    $ctrl.onlyRejected = false;
                    ClubHuntingViewData.changeSpecies(species.code).then(filterContent);
                }
            };

            $ctrl.onHuntingYearChanged = function (huntingYear) {
                if (huntingYear) {
                    $ctrl.onlyRejected = false;
                    $ctrl.beginDate = HuntingYearService.getBeginDateStr(huntingYear);
                    $ctrl.endDate = HuntingYearService.getEndDateStr(huntingYear);

                    ClubHuntingViewData.changeHuntingYear(huntingYear).then(filterContent);
                    ClubTodoService.showTodo(ClubHuntingViewData.getHuntingClubId(), huntingYear);
                }
            };

            $ctrl.onDateChange = function () {
                filterContent();
            };

            $ctrl.onOnlyRejectedChange = function () {
                filterContent();
            };

            $ctrl.isEditPermitPossible = ClubHuntingViewData.isEditPermitPossible;

            $ctrl.editHuntingGroup = function () {
                var huntingGroup = $ctrl.huntingData.huntingGroup;

                if (huntingGroup) {
                    var modalPromise = ClubGroupService.editGroup(huntingGroup);

                    NotificationService.handleModalPromise(modalPromise).then(function (result) {
                        $state.reload();
                    });
                }
            };
        }
    })

    .component('clubHuntingStatistics', {
        templateUrl: 'club/hunting/list/statistics.html',
        bindings: {
            diary: '<',
            showObservation: '<'
        },
        controller: function (ObservationFieldRequirements) {
            var $ctrl = this;

            $ctrl.mooseAmountFields = ObservationFieldRequirements.getAllMooseAmountFields();

            $ctrl.$onChanges = function (c) {
                if (c.diary) {
                    $ctrl.harvestStats = _.reduce(c.diary.currentValue, collectStats, {});
                } else {
                    $ctrl.harvestStats = null;
                }
            };

            function collectStats(stats, entry) {
                if (entry.isPermitBasedMooselike()) {
                    if (entry.isHarvest() && entry.huntingDayId) {
                        _.forEach(entry.specimens, function (specimen) {
                            if (specimen.gender && specimen.age) {
                                var statsKey = entry.type + '_' + specimen.gender + '_' + specimen.age;
                                stats[statsKey] = (stats[statsKey] || 0) + 1;
                            }
                        });
                    }
                    // Count observations only of selected species.
                    // Since observations are shown only for moose, count only moose observations
                    if (entry.isMoose() && entry.isObservation() && entry.huntingDayId) {
                        _.forEach($ctrl.mooseAmountFields, function (statsKey) {
                            stats[statsKey] = (stats[statsKey] || 0) + (entry[statsKey] || 0);
                        });
                    }
                }

                return stats;
            }
        }
    })

    .component('clubHuntingList', {
        templateUrl: 'club/hunting/list/days.html',
        bindings: {
            huntingDays: '<',
            onSelectEntry: '&',
            onCreateHuntingDay: '&',
            onEditHuntingDay: '&'
        },
        controller: function ($scope, ClubHuntingViewData) {
            var $ctrl = this;

            $scope.$on('huntingDaySelected', function (event, selected) {
                var selectedDay;
                if (selected.id) {
                    selectedDay = _.find($ctrl.huntingDays, {
                        id: selected.id
                    });
                } else {
                    selectedDay = _.find($ctrl.huntingDays, {
                        startDate: selected.startDate
                    });
                }

                if (selectedDay) {
                    selectedDay.isOpen = true;
                }
            });

            $ctrl.createHuntingDay = function ($event, huntingDay) {
                $event.stopPropagation();
                $ctrl.onCreateHuntingDay({'startDateAsString': huntingDay.startDate});
            };

            $ctrl.editHuntingDay = function ($event, huntingDay) {
                $event.stopPropagation();
                $ctrl.onEditHuntingDay({'id': huntingDay.id});
            };

            $ctrl.getDayToggleClasses = function (day) {
                return {
                    'glyphicon': true,
                    'glyphicon-chevron-down': day.isOpen,
                    'glyphicon-chevron-right': !day.isOpen
                };
            };

            $ctrl.onEntryClick = function (entry, day) {
                $ctrl.onSelectEntry({
                    diaryEntry: entry,
                    huntingDay: day
                });
            };

            $ctrl.showHuntingDayAsterisk = function (day) {
                return !(day.id && _.every(day.gameEntries, 'huntingDayId'));
            };

            $ctrl.getSpecimenType = function (specimen) {
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

            $ctrl.isMooseGroupSelected = function () {
                return ClubHuntingViewData.isMooseGroupSelected();
            };
        }
    });
