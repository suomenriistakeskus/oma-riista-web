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
                    initialDiaryEntries: function (DiaryEntries, Helpers, parameters, DiaryListViewState, SrvaService) {
                        if (!parameters) {
                            return [];
                        }

                        return DiaryEntries.mine({
                            beginDate: Helpers.dateToString(DiaryListViewState.beginDate),
                            endDate: Helpers.dateToString(DiaryListViewState.endDate),
                            srvaEvents: SrvaService.isEnableSrva()
                        }).$promise;
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

    .controller('DiaryListController', function ($scope, $state, $uibModal,
                                                 MapDefaults, MapState, WGS84, GIS, Markers, Helpers,
                                                 DiaryListService,
                                                 DiaryListMarkerService,
                                                 DiaryListSpeciesService,
                                                 DiaryEntryService,
                                                 DiaryEntries,
                                                 SrvaService,
                                                 viewState,
                                                 parameters,
                                                 initialDiaryEntries,
                                                 SrvaOtherSpeciesService) {
        $scope.state = viewState;
        $scope.mapState = MapState.get();
        $scope.allEntries = initialDiaryEntries;
        $scope.getGameName = parameters.$getGameName;
        $scope.getCategoryName = parameters.$getCategoryName;

        $scope.mapEvents = MapDefaults.getMapBroadcastEvents();
        $scope.mapDefaults = MapDefaults.create();

        $scope.enableSrva = SrvaService.isEnableSrva();
        if($scope.enableSrva) {
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

            return _.first($scope.groupedEntries);
        }

        function _expandActiveHuntingDayGroup() {
            var activeHuntingDayGroup = _getActiveHuntingDayGroup();

            if (activeHuntingDayGroup) {
                _.each($scope.groupedEntries, function (group) {
                    group.accordionOpen = false;
                });
                activeHuntingDayGroup.accordionOpen = true;
            }
        }

        // Species filtering

        var filterDiaryEntriesBySpeciesSelection = function () {
            if ($scope.state.selectedDiaryEntry !== null) {
                // Make sure active harvest is not filtered out
                DiaryListSpeciesService.speciesAddedToSelection(parameters, $scope.state.selectedDiaryEntry.gameSpeciesCode);
            }
            var filteredEntries = DiaryListSpeciesService.filterDiaryEntriesBySpeciesSelection($scope.allEntries);

            filteredEntries = _.filter(filteredEntries, function (entry) {
                return ($scope.state.showHarvest && entry.isHarvest()) ||
                    ($scope.state.showObservation && entry.isObservation()) ||
                    ($scope.state.showSrvaEvent && entry.isSrva());
            });

            $scope.mapState.viewBounds = DiaryListService.getEntryBounds(filteredEntries);

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

            if ($scope.state.selectedDiaryEntry !== null) {
                // Open sidebar dialog for active harvest
                $scope.showEntry($scope.state.selectedDiaryEntry);
            }
        };

        if ($scope.state.selectedDiaryEntry !== null) {
            // Initially zoom to selected harvest after create or update
            MapState.updateMapCenter($scope.state.selectedDiaryEntry.geoLocation);
        }

        $scope.deselectAllSpecies = function () {
            DiaryListSpeciesService.deselectAllSpecies();
            filterDiaryEntriesBySpeciesSelection();
        };

        $scope.selectAllSpecies = function () {
            DiaryListSpeciesService.selectAllSpecies();
            filterDiaryEntriesBySpeciesSelection();
        };

        $scope.speciesAddedToSelection = function () {
            if ($scope.state.lastSelectedSpeciesCode || $scope.state.lastSelectedSpeciesCode === 0) {
                var speciesCode = $scope.state.lastSelectedSpeciesCode;
                delete $scope.state.lastSelectedSpeciesCode;
                DiaryListSpeciesService.speciesAddedToSelection(parameters, speciesCode);
                filterDiaryEntriesBySpeciesSelection();
            }
        };

        $scope.removeSpeciesFromSelection = function (gameSpeciesCode) {
            DiaryListSpeciesService.removeSpeciesFromSelection(parameters, gameSpeciesCode);
            filterDiaryEntriesBySpeciesSelection();
        };

        // Actions

        $scope.showEntry = DiaryListService.showSidebar();

        $scope.addHarvest = function () {
            $state.go('profile.diary.addHarvest');
        };

        $scope.addObservation = function () {
            $state.go('profile.diary.addObservation');
        };

        $scope.addSrva = function () {
            $state.go('profile.diary.addSrva');
        };

        $scope.search = function () {
            var params = {
                beginDate: Helpers.dateToString($scope.state.beginDate),
                endDate: Helpers.dateToString($scope.state.endDate),
                reportedForOthers:  $scope.state.reportedForOthers || false,
                srvaEvents: $scope.state.showSrvaEvent || false
            };

            DiaryEntries.mine(params).$promise.then(function (diaryEntries) {
                $scope.allEntries = diaryEntries;

                DiaryListSpeciesService.updateAllSpecies(parameters, diaryEntries, true);
                filterDiaryEntriesBySpeciesSelection();
            });
        };

        var selectAll = $scope.state.allSpecies.length === 0;
        DiaryListSpeciesService.updateAllSpecies(parameters, $scope.allEntries, selectAll);

        filterDiaryEntriesBySpeciesSelection();
    });
