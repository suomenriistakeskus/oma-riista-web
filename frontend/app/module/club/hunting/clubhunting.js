'use strict';

angular.module('app.clubhunting', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.hunting', {
                url: '/hunting',
                templateUrl: 'club/hunting/hunting.html',
                controller: 'ClubHuntingLayoutController',
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    rhyBounds: function (MapBounds, club) {
                        return MapBounds.getRhyBounds(club.rhy.officialCode);
                    },
                    huntingGroups: function (ClubGroups, club) {
                        return ClubGroups.query({clubId: club.id}).$promise;
                    },
                    huntingData: function (ClubHuntingViewData, club, huntingGroups) {
                        return ClubHuntingViewData.initialize(club, huntingGroups);
                    }
                }
            });
    })

    .service('ClubHuntingActiveEntry', function ($q, Harvest, Observation) {
        var selectedItem = null;

        this.isItemSelected = function () {
            return selectedItem !== null;
        };

        this.clearSelectedItem = function () {
            var copy = selectedItem;
            selectedItem = null;
            return copy;
        };

        this.createHarvest = function (clubId, groupId, spec) {
            selectedItem = {
                actionName: 'create',
                clubId: clubId,
                groupId: groupId,
                diaryEntry: Harvest.createTransient(spec)
            };
        };

        this.createObservation = function (clubId, groupId, spec) {
            selectedItem = {
                actionName: 'create',
                clubId: clubId,
                groupId: groupId,
                diaryEntry: Observation.createTransient(spec)
            };
        };

        this.createObservationForMooseHarvest = function (clubId, groupId, harvest) {
            selectedItem = {
                actionName: 'create',
                clubId: clubId,
                groupId: groupId,
                diaryEntry: Observation.createObservationForMooseHarvest(harvest)
            };
        };

        this.editDiaryEntry = function (clubId, groupId, diaryEntry) {
            selectedItem = {
                actionName: 'edit',
                clubId: clubId,
                groupId: groupId,
                diaryEntry: diaryEntry
            };
        };

        this.fixGeoLocationOfDiaryEntry = function (clubId, groupId, diaryEntry) {
            selectedItem = {
                actionName: 'fixGeoLocation',
                clubId: clubId,
                groupId: groupId,
                diaryEntry: diaryEntry
            };
        };

        this.acceptDiaryEntry = function (clubId, groupId, diaryEntry) {
            selectedItem = {
                actionName: 'accept',
                clubId: clubId,
                groupId: groupId,
                diaryEntry: diaryEntry
            };
        };

        this.reloadSelectedItem = function () {
            var diaryEntry = selectedItem ? selectedItem.diaryEntry : null;

            if (!diaryEntry) {
                return $q.reject();
            }

            if (!diaryEntry.id) {
                return $q.when(selectedItem);
            }

            var repository = diaryEntry.isHarvest() ? Harvest : diaryEntry.isObservation ? Observation : null;

            return repository.get({
                id: diaryEntry.id
            }).$promise.then(function (reloadedDiaryEntry) {
                selectedItem.diaryEntry = reloadedDiaryEntry;
                return selectedItem;
            });
        };
    })

    .controller('ClubHuntingLayoutController', function ($scope, $state, $translate, $timeout, Helpers, FormPostService,
                                                         ActiveRoleService, NotificationService, dialogs, MapState,
                                                         ClubHuntingViewData,
                                                         ClubHuntingActiveEntry,
                                                         ClubHuntingEntryShowService,
                                                         ClubHuntingDayService,
                                                         huntingData, rhyBounds) {
        var $ctrl = this;

        // State parameters
        $ctrl.huntingData = huntingData;
        $ctrl.rhyBounds = rhyBounds;
        $ctrl.forceBoundsCalculation = !ClubHuntingActiveEntry.isItemSelected();

        // View state
        $ctrl.isModerator = ActiveRoleService.isModerator();
        $ctrl.mooseGroupSelected = false;
        $ctrl.activeTabIndex = 0;

        // Filter view component output variables
        $ctrl.filteredHuntingDays = [];
        $ctrl.filteredDiary = [];

        $ctrl.isAddHarvestVisible = ClubHuntingViewData.isAddHarvestVisible;
        $ctrl.isAddObservationVisible = ClubHuntingViewData.isAddObservationVisible;
        $ctrl.isCreateHuntingDayVisible = ClubHuntingViewData.isCreateHuntingDayVisible;

        $ctrl.addHarvest = function () {
            var clubId = ClubHuntingViewData.getHuntingClubId();
            var groupId = ClubHuntingViewData.getHuntingGroupId();
            var gameSpeciesCode = ClubHuntingViewData.getSelectedSpeciesCode();

            if (groupId && gameSpeciesCode) {
                ClubHuntingActiveEntry.createHarvest(clubId, groupId, {
                    gameSpeciesCode: gameSpeciesCode
                });
                $state.go('club.hunting.add');
            }
        };

        $ctrl.addObservation = function () {
            var clubId = ClubHuntingViewData.getHuntingClubId();
            var groupId = ClubHuntingViewData.getHuntingGroupId();
            var gameSpeciesCode = ClubHuntingViewData.getSelectedSpeciesCode();

            if (groupId) {
                ClubHuntingActiveEntry.createObservation(clubId, groupId, {
                    gameSpeciesCode: gameSpeciesCode
                });
                $state.go('club.hunting.add');
            }
        };

        $ctrl.createHuntingDay = function (startDateAsString) {
            var clubId = ClubHuntingViewData.getHuntingClubId();
            var groupId = ClubHuntingViewData.getHuntingGroupId();
            var resultPromise = ClubHuntingDayService.createHuntingDay(clubId, groupId,
                startDateAsString || Helpers.dateToString(new Date()));

            NotificationService.handleModalPromise(resultPromise).then(function () {
                $state.reload();
            });
        };

        $ctrl.editHuntingDay = function (huntingDayId) {
            var clubId = ClubHuntingViewData.getHuntingClubId();
            var groupId = ClubHuntingViewData.getHuntingGroupId();
            var resultPromise = ClubHuntingDayService.editHuntingDay(clubId, groupId, huntingDayId);

            NotificationService.handleModalPromise(resultPromise).then(function () {
                $state.reload();
            }, function (err) {
                if (err === 'delete') {
                    $state.reload();
                }
            });
        };

        $ctrl.selectEntry = function (diaryEntry, huntingDay) {
            var t = moment(diaryEntry.pointOfTime, 'YYYY-MM-DD[T]HH:mm');
            var entryDate = Helpers.dateToString(t, 'YYYY-MM-DD');

            if (diaryEntry.huntingDayId) {
                huntingDay = ClubHuntingViewData.findHuntingDay({id: diaryEntry.huntingDayId});
            }

            if (!huntingDay) {
                huntingDay = ClubHuntingViewData.findHuntingDay({startDate: entryDate});
            }

            if (!huntingDay) {
                huntingDay = ClubHuntingViewData.findHuntingDay({endDate: entryDate});
            }

            if (huntingDay) {
                $ctrl.activeTabIndex = 1;
                $scope.$broadcast('huntingDaySelected', huntingDay);
            }

            MapState.updateMapCenter(diaryEntry.geoLocation);

            var clubId = ClubHuntingViewData.getHuntingClubId();
            var groupId = ClubHuntingViewData.getHuntingGroupId();

            ClubHuntingEntryShowService.showDiaryEntry(clubId, groupId, diaryEntry).then(function () {
                $state.reload();
            });
        };

        $ctrl.selectEntryById = function (type, id) {
            var diaryEntry = ClubHuntingViewData.find(type, id);

            if (diaryEntry) {
                $ctrl.selectEntry(diaryEntry, null);
            }
        };

        $ctrl.onFilterChange = function (filter) {
            $ctrl.filteredHuntingDays = ClubHuntingViewData.filterHuntingDays(filter.beginDate, filter.endDate);
            $ctrl.filteredDiary = ClubHuntingViewData.filterDiary(filter.beginDate, filter.endDate, filter.onlyRejected);
            $ctrl.mooseGroupSelected = ClubHuntingViewData.isMooseGroupSelected();

            // This will trigger sidebar open for previously selected entry
            var selectedItem = ClubHuntingActiveEntry.clearSelectedItem();

            if (selectedItem && selectedItem.diaryEntry) {
                var diaryEntry = selectedItem.diaryEntry;
                var clubId = selectedItem.clubId;
                var groupId = selectedItem.groupId;
                var actionName = selectedItem.actionName;
                var acceptOrCreate = actionName === 'accept' || actionName === 'create';

                MapState.updateMapCenter(diaryEntry.geoLocation);
                ClubHuntingActiveEntry.clearSelectedItem();

                if (acceptOrCreate && diaryEntry.isHarvest() && diaryEntry.isMoose()) {
                    showSuggestCreateMooseObservationDialog().then(null, function (result) {
                        if (result === 'no') {
                            ClubHuntingActiveEntry.createObservationForMooseHarvest(clubId, groupId, diaryEntry);
                            $state.go('club.hunting.add');
                        }
                    });
                }

                if (diaryEntry.huntingDayId) {
                    $ctrl.activeTabIndex = 1;

                    $timeout(function () {
                        // Timeout is required so that diary list component has received updated diary
                        // before trying to focus on selected hunting day.
                        $scope.$broadcast('huntingDaySelected', {id: diaryEntry.huntingDayId});
                    });
                }

                ClubHuntingEntryShowService.showDiaryEntry(clubId, groupId, diaryEntry).then(function () {
                    $state.reload();
                });
            } else {
                $ctrl.forceBoundsCalculation = true;
            }
        };

        function showSuggestCreateMooseObservationDialog() {
            var dialogTitle = $translate.instant('club.hunting.suggestMooseObservation.title');
            var dialogMessage = $translate.instant('club.hunting.suggestMooseObservation.message');
            var dialog = dialogs.confirm(dialogTitle, dialogMessage);

            return dialog.result;
        }

        $ctrl.exportToExcel = function () {
            var clubId = ClubHuntingViewData.getHuntingClubId();
            var huntingYear = ClubHuntingViewData.getSelectedHuntingYear();
            var gameSpeciesCode = ClubHuntingViewData.getSelectedSpeciesCode();

            if (clubId && huntingYear && gameSpeciesCode) {
                var formSubmitAction = '/api/v1/club/' + clubId + '/huntingdata';

                FormPostService.submitFormUsingBlankTarget(formSubmitAction, {
                    huntingYear: huntingYear,
                    gameSpeciesCode: gameSpeciesCode
                });
            }
        };
    });
