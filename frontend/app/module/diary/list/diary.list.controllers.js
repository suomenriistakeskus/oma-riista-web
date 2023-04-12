'use strict';

angular.module('app.diary.list.controllers', ['ngResource'])

    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.diary', {
                url: '/diary',
                templateUrl: 'diary/list/list.html',
                controller: 'DiaryListController',
                wideLayout: true,
                resolve: {
                    parameters: function ($stateParams, GameDiaryParameters, ActiveRoleService) {
                        if ($stateParams.id !== 'me' || ActiveRoleService.isModerator()) {
                            return null;
                        }

                        return GameDiaryParameters.query().$promise;
                    },
                    viewState: function (DiaryListViewState, Harvest, Observation, Srva) {
                        var diaryEntry = DiaryListViewState.selectedDiaryEntry;

                        if (diaryEntry === null) {
                            return DiaryListViewState;
                        }

                        var repository;

                        if (diaryEntry.isHarvest()) {
                            repository = Harvest;
                        } else if (diaryEntry.isSrva()) {
                            repository = Srva;
                        } else {
                            repository = Observation;
                        }

                        // Reload locally stored selected Harvest/Observation after update
                        return repository.get({id: diaryEntry.id}).$promise.then(function (entry) {
                            DiaryListViewState.selectedDiaryEntry = entry;
                            return DiaryListViewState;
                        });
                    }
                }
            });
    })

    .controller('DiaryListController', function ($scope, Helpers, AccountService, FormPostService,
                                                 MapDefaults, MapState, MapBounds, WGS84, GIS, Markers,
                                                 DiaryListService, DiaryListMarkerService, DiaryListSpeciesService,
                                                 DiaryEntries, DiaryEntryService, SrvaOtherSpeciesService,
                                                 AccountBeingUnregisteredNotifier, viewState, parameters) {
        $scope.state = viewState;
        $scope.parameters = parameters;
        $scope.mapState = MapState.get();
        $scope.allEntries = [];
        $scope.getGameName = parameters.$getGameName;
        $scope.getCategoryName = parameters.$getCategoryName;
        $scope.showEntry = DiaryListService.showSidebar();
        $scope.mapEvents = MapDefaults.getMapBroadcastEvents();
        $scope.mapDefaults = MapDefaults.create();

        $scope.enableSrva = AccountService.isSrvaFeatureEnabled();
        if ($scope.enableSrva) {
            SrvaOtherSpeciesService.addOtherSpecies(parameters.species);
        }

        function _getActiveHuntingDayGroup() {
            if (!$scope.groupedEntries || $scope.groupedEntries.length === 0) {
                return null;
            }

            if ($scope.state.selectedDiaryEntry !== null) {
                var selectedDiaryEntryDay = moment($scope.state.selectedDiaryEntry.pointOfTime).format('YYYYMMDD');

                return _.find($scope.groupedEntries, function (group) {
                    return moment(group.pointOfTime).format('YYYYMMDD') === selectedDiaryEntryDay;
                });
            }

            return _.head($scope.groupedEntries);
        }

        function _expandActiveHuntingDayGroup() {
            var activeHuntingDayGroup = _getActiveHuntingDayGroup();

            if (activeHuntingDayGroup) {
                _.forEach($scope.groupedEntries, function (group) {
                    group.accordionOpen = false;
                });
                activeHuntingDayGroup.accordionOpen = true;
            }
        }

        // Species filtering

        $scope.filterDiaryEntriesBySpeciesSelection = function () {
            var filteredEntries = DiaryListSpeciesService.filterDiaryEntriesBySpeciesSelection($scope.allEntries);

            filteredEntries = _.filter(filteredEntries, function (entry) {
                return ($scope.state.showHarvest && entry.isHarvest()) ||
                    ($scope.state.showObservation && entry.isObservation()) ||
                    ($scope.state.showSrvaEvent && entry.isSrva());
            });

            $scope.markers = DiaryListMarkerService.createMarkersForDiaryEntryList(filteredEntries, function (diaryEntry) {
                $scope.showEntry(diaryEntry);
                _expandActiveHuntingDayGroup();
            });
            $scope.groupedEntries = DiaryListService.groupDiaryEntriesByHuntingDay(filteredEntries);

            $scope.totalSpecimenAmountBySpecies = _.reduce(filteredEntries, function (total, entry) {
                var code = entry.gameSpeciesCode;
                var count = entry.totalSpecimenAmount;
                total[code] = (total[code] || 0) + count;
                return total;
            }, {});

            _expandActiveHuntingDayGroup();

            if (!$scope.state.selectedDiaryEntry) {
                var entryBounds = DiaryListService.getEntryBounds(filteredEntries);
                MapState.updateMapBounds(entryBounds, MapBounds.getBoundsOfFinland(), true);
            }
        };

        function openSelectedDiaryEntry() {
            var selectedDiaryEntry = $scope.state.selectedDiaryEntry;
            $scope.state.selectedDiaryEntry = null;

            if (selectedDiaryEntry !== null) {
                // Make sure active harvest is not filtered out
                DiaryListSpeciesService.speciesAddedToSelection(parameters, selectedDiaryEntry.gameSpeciesCode);

                // Initially zoom to selected harvest after create or update
                MapState.updateMapCenter(selectedDiaryEntry.geoLocation);

                // Open sidebar dialog for active harvest
                $scope.showEntry(selectedDiaryEntry);
            }
        }

        function searchBackend() {
            if (!viewState.beginDate || !viewState.endDate) {
                return;
            }

            DiaryEntries.search({
                beginDate: Helpers.dateToString(viewState.beginDate),
                endDate: Helpers.dateToString(viewState.endDate),
                includeHarvest: !!viewState.showHarvest,
                includeObservation: !!viewState.showObservation,
                includeSrva: !!viewState.showSrvaEvent,
                onlyReports: !!viewState.showHarvest && viewState.harvestReportFilter === 'report-only',
                onlyTodo: !!viewState.showHarvest && viewState.harvestReportFilter === 'todo-only',
                reportedForOthers: !!viewState.reportedForOthers

            }).$promise.then(function (diaryEntries) {
                $scope.allEntries = diaryEntries;

                DiaryListSpeciesService.updateAllSpecies(parameters, diaryEntries, true);

                openSelectedDiaryEntry();

                $scope.filterDiaryEntriesBySpeciesSelection();
            });
        }

        $scope.$watchGroup([
            'state.showHarvest', // 0
            'state.showObservation', // 1
            'state.showSrvaEvent', // 2
            'state.reportedForOthers', // 3
            'state.harvestReportFilter', // 4
            'state.beginDate', // 5
            'state.endDate', // 6
        ], function (newValues, oldValues) {
            // Data loaded from backend is up to date ?

            if (angular.equals(newValues[0], oldValues[0]) &&
                angular.equals(newValues[1], oldValues[1]) &&
                angular.equals(newValues[2], oldValues[2]) &&
                angular.equals(newValues[3], oldValues[3]) &&
                angular.equals(newValues[4], oldValues[4]) &&
                angular.equals(newValues[5], oldValues[5]) &&
                angular.equals(newValues[6], oldValues[6])) {
                return;
            }

            searchBackend();
        });

        // Actions

        $scope.addHarvest = function () {
            DiaryEntryService.createHarvest();
        };

        $scope.addObservation = function () {
            DiaryEntryService.createObservation();
        };

        $scope.addSrva = function () {
            DiaryEntryService.createSrvaEvent();
        };

        $scope.exportExcel = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/gamediary/excel', {});
        };

        $scope.goToStore = function () {
            Helpers.goToStore();
        };
        $scope.showStoreButton = function () {
            var system = Helpers.getMobileOperatingSystem();
            return system === 'android' || system === 'ios';
        };


        MapState.updateMapBounds(null, MapBounds.getBoundsOfFinland(), false);

        searchBackend();
        AccountBeingUnregisteredNotifier.notifyAccountUnregistration();
    });
