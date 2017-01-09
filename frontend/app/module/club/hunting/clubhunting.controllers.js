'use strict';

angular.module('app.clubhunting.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.hunting', {
                url: '/hunting',
                templateUrl: 'club/hunting/hunting.html',
                controller: 'ClubHuntingMapController',
                wideLayout: true,
                resolve: {
                    rhyBounds: function (GIS, club) {
                        return GIS.getRhyBounds(club.rhy.officialCode);
                    },
                    huntingGroups: function (ClubGroups, club) {
                        return ClubGroups.query({clubId: club.id}).$promise;
                    },
                    huntingData: function (ClubHuntingViewData, club, huntingGroups) {
                        return ClubHuntingViewData.initialize(club, huntingGroups);
                    },
                    mooseAmountFields: function (ObservationFieldRequirements) {
                        return ObservationFieldRequirements.getAllMooseAmountFields();
                    }
                }
            });
    })

    .controller('ClubHuntingMapController', function ($scope, $state, $q, $timeout,
                                                      NotificationService, Helpers, FormPostService,
                                                      ActiveRoleService,
                                                      ClubGroups, ClubGroupDiary,
                                                      ClubHunting,
                                                      ClubHuntingViewData,
                                                      ClubHuntingEntryService,
                                                      ClubHuntingDayService,
                                                      GIS, WGS84, MapState, MapDefaults, Markers,
                                                      club, huntingData, mooseAmountFields, rhyBounds) {
        $scope.club = club;
        $scope.huntingData = huntingData;
        $scope.isModerator = ActiveRoleService.isModerator();
        $scope.filteredHuntingDays = [];
        $scope.harvestStats = null;
        $scope.mooseGroupSelected = ClubHuntingViewData.isMooseGroupSelected();
        $scope.mooseAmountFields = mooseAmountFields;
        $scope.uiState = {
            activeTabIndex: 0
        };

        $scope.mapState = MapState.get();
        $scope.mapEvents = MapDefaults.getMapBroadcastEvents();
        $scope.mapDefaults = MapDefaults.create();
        $scope.markers = [];
        $scope.mapGeoJSON = null;

        MapState.updateMapBounds(null, rhyBounds, false);

        function getSelectedSpeciesCode() {
            return _.get(huntingData, 'species.code');
        }

        function getHuntingGroupId() {
            return _.get(huntingData, 'huntingGroup.id');
        }

        function getHuntingClubId() {
            return _.get(huntingData, 'club.id');
        }

        $scope.addHarvest = function () {
            var groupId = getHuntingGroupId();
            var gameSpeciesCode = getSelectedSpeciesCode();

            if (groupId && gameSpeciesCode) {
                ClubHuntingEntryService.createHarvest(club.id, groupId, gameSpeciesCode);
            }
        };

        $scope.addObservation = function () {
            var groupId = getHuntingGroupId();
            var gameSpeciesCode = getSelectedSpeciesCode();

            if (groupId) {
                ClubHuntingEntryService.createObservation(club.id, groupId, gameSpeciesCode);
            }
        };

        $scope.isAddHarvestVisible = ClubHuntingViewData.isAddHarvestVisible;
        $scope.isAddObservationVisible = ClubHuntingViewData.isAddObservationVisible;
        $scope.isCreateHuntingDayVisible = ClubHuntingViewData.isCreateHuntingDayVisible;
        $scope.isExportVisible = ClubHuntingViewData.isExportVisible;

        $scope.createHuntingDay = function (startDateAsString) {
            var resultPromise = ClubHuntingDayService.createHuntingDay(club.id, getHuntingGroupId(),
                startDateAsString || Helpers.dateToString(new Date()));

            NotificationService.handleModalPromise(resultPromise).then(function () {
                $state.reload();
            });
        };

        $scope.editHuntingDay = function (huntingDayId) {
            var resultPromise = ClubHuntingDayService.editHuntingDay(club.id, getHuntingGroupId(), huntingDayId);

            NotificationService.handleModalPromise(resultPromise).then(function () {
                $state.reload();
            }, function (err) {
                if (err === 'delete') {
                    $state.reload();
                }
            });
        };

        function showSidebar(diaryEntry) {
            MapState.updateMapCenter(diaryEntry.geoLocation);

            ClubHuntingEntryService.showDiaryEntry(club.id, getHuntingGroupId(), diaryEntry).then(function () {
                $state.reload();
            });
        }

        function selectHuntingDay(huntingDay) {
            $scope.uiState.activeTabIndex = 1;
            $scope.$broadcast('huntingDaySelected', huntingDay);
        }

        function onEntrySelect(diaryEntry, huntingDay) {
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
                selectHuntingDay(huntingDay);
            }

            showSidebar(diaryEntry);
        }

        $scope.selectEntry = function (diaryEntry, huntingDay) {
            onEntrySelect(diaryEntry, huntingDay);
        };

        var markerClickHandler = function (markerId) {
            var parts = markerId.split(':');

            if (parts.length === 2) {
                var diaryEntry = ClubHuntingViewData.find(parts[0], _.parseInt(parts[1]));
                onEntrySelect(diaryEntry, null);
            }
        };

        function combineBounds(boundsArray) {
            return _.reduce(boundsArray, function (acc, bounds) {
                if (!acc) {
                    return bounds;
                } else {
                    return bounds ? combineBounds(acc, bounds) : acc;
                }
            }, null);

            function combineBounds(a, b) {
                var la = toLeafletLatLngBounds(a),
                    lb = toLeafletLatLngBounds(b),
                    c = la.extend(lb);

                return {
                    northEast: c.getNorthEast(),
                    southWest: c.getSouthWest()
                };
            }

            function toLeafletLatLngBounds(bounds) {
                return L.latLngBounds(
                    L.latLng(bounds.southWest.lat, bounds.southWest.lng),
                    L.latLng(bounds.northEast.lat, bounds.northEast.lng));
            }
        }

        $scope.onFilterChange = function (huntingDays, diary, harvestStats) {
            $scope.filteredHuntingDays = huntingDays;
            $scope.harvestStats = harvestStats;
            $scope.mooseGroupSelected = ClubHuntingViewData.isMooseGroupSelected();
            $scope.markers = ClubHunting.createMarkers(diary, markerClickHandler, $scope);
            $scope.mapGeoJSON = huntingData.huntingArea ? {
                data: huntingData.huntingArea,
                onEachFeature: _.noop,
                style: MapDefaults.getGeoJsonOptions()
            } : null;

            var huntingAreaBounds = ClubHuntingViewData.getHuntingAreaBounds();
            var markerBounds = _.size($scope.markers) > 0 ? Markers.getMarkerBounds($scope.markers) : null;
            var combinedBounds = combineBounds([huntingAreaBounds, markerBounds]);
            var forceBoundsCalculation = !ClubHuntingEntryService.isItemSelected();

            MapState.updateMapBounds(combinedBounds, rhyBounds, forceBoundsCalculation);

            // This will trigger sidebar open for previously selected entry
            ClubHuntingEntryService.showSelectedDiaryEntry(function (selectedDiaryEntry) {
                if (selectedDiaryEntry) {
                    MapState.updateMapCenter(selectedDiaryEntry.geoLocation);

                    if (selectedDiaryEntry.huntingDayId) {
                        selectHuntingDay({id: selectedDiaryEntry.huntingDayId});
                    }
                }
            });
        };

        $scope.exportHuntingDays = function () {
            var clubId = getHuntingClubId();
            var groupId = getHuntingGroupId();

            var formSubmitAction = '/api/v1/club/' + clubId + '/group/' + groupId + '/export-hunting-days';

            FormPostService.submitFormUsingBlankTarget(formSubmitAction, {});
        };
    });
