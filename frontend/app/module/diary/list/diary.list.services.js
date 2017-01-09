'use strict';

angular.module('app.diary.list.services', ['ngResource'])

    .factory('DiaryListViewState', function (HuntingYearService) {
        return {
            showHarvest: true,
            showObservation: true,
            showSrvaEvent: true,
            beginDate: HuntingYearService.getBeginDateStr(),
            endDate: HuntingYearService.getEndDateStr(),
            lastSelectedSpeciesCode: null,
            allSpecies: [],
            selectedSpecies: [],
            unselectedSpecies: [],
            selectedDiaryEntry: null
        };
    })

    .service('DiaryListService', function (MapDefaults, MapState, GIS,
                                           DiaryListViewState, DiaryEntryService) {
        var maxBounds = MapDefaults.getBoundsOfFinland();

        this.getEntryBounds = function (diaryEntryList) {
            var geoLocations = _.map(diaryEntryList, 'geoLocation');
            return GIS.getBoundsFromGeolocations(geoLocations, maxBounds);
        };

        this.groupDiaryEntriesByHuntingDay = function (diaryEntryList) {
            var grouped = _.groupBy(diaryEntryList, function (diaryEntry) {
                return moment(diaryEntry.pointOfTime).format('YYYYMMDD');
            });

            return _(grouped).pairs().map(function (pair) {
                var entryList = _.sortByOrder(pair[1], 'pointOfTime', false);

                var countTotalSpecimenAmount = function (list, filterMethod) {
                    return _(list).filter(_.method(filterMethod)).sum(function (e) {
                        // totalSpecimenAmount is null, at least with some types of observations
                        return e.totalSpecimenAmount || 1;
                    });
                };

                return {
                    entries: entryList,
                    pointOfTime: _.first(entryList).pointOfTime,
                    totalHarvestSpecimenCount: countTotalSpecimenAmount(entryList, 'isHarvest'),
                    totalObservationSpecimenCount: countTotalSpecimenAmount(entryList, 'isObservation'),
                    totalSrvaSpecimenCount: countTotalSpecimenAmount(entryList, 'isSrva'),
                    accordionOpen: false
                };
            }).sortByOrder('pointOfTime', false).value();
        };

        this.showSidebar = function () {
            var currentSidebar = null;

            return function (diaryEntry) {
                var state = DiaryListViewState;

                if (currentSidebar !== null) {
                    if (state.selectedDiaryEntry && state.selectedDiaryEntry.id === diaryEntry.id) {
                        return;
                    }

                    currentSidebar.dismiss('ignore');
                    currentSidebar = null;
                }

                MapState.updateMapCenter(diaryEntry.geoLocation);

                state.selectedDiaryEntry = diaryEntry;

                var modalInstance = DiaryEntryService.showSidebar(diaryEntry);

                modalInstance.opened.then(function () {
                    currentSidebar = modalInstance;
                });

                modalInstance.result.finally(function (err) {
                    currentSidebar = null;
                });

                modalInstance.result.then(function () {
                    state.selectedDiaryEntry = null;
                }, function (err) {
                    if (err !== 'ignore') {
                        state.selectedDiaryEntry = null;
                    }
                });
            };
        };
    })

    .service('DiaryListMarkerService', function (Markers) {
        var markerDefaults = {
            draggable: false,
            icon: {
                type: 'awesomeMarker',
                prefix: 'fa', // font-awesome
                icon: 'crosshairs'
            },
            groupOption: {
                // Options to pass for leaflet.markercluster plugin

                //disableClusteringAtZoom: 13,
                showCoverageOnHover: true
            },
            group: 'DiaryEntries'
        };

        this.createMarkersForDiaryEntryList = function (diaryEntryList, showHandler) {
            var clickHandler = function (markerId) {
                var parts = markerId.split(':');

                if (parts.length === 2) {
                    var diaryEntry = _.findWhere(diaryEntryList, {
                        'type': parts[0],
                        'id': _.parseInt(parts[1])
                    });

                    if (diaryEntry) {
                        showHandler(diaryEntry);
                    }
                }
            };

            var getMarkerColor = function (diaryEntry) {
                if (diaryEntry.isHarvest()) {
                    if (diaryEntry.stateAcceptedToHarvestPermit) {
                        return Markers.getColorForHarvestReportState(diaryEntry.stateAcceptedToHarvestPermit);
                    }
                    if (diaryEntry.harvestReportRequired) {
                        return Markers.getColorForHarvestReportState(diaryEntry.harvestReportState);
                    }
                }

                if (diaryEntry.isSrva()) {
                    if (diaryEntry.eventType === 'TRAFFIC_ACCIDENT' && diaryEntry.eventResult === 'ACCIDENT_SITE_NOT_FOUND') {
                        return 'orange';
                    }
                }

                return 'green';
            };

            var getMarkerIconName = function (diaryEntry) {
                if (diaryEntry.isSrva()) {
                    return 'exclamation-triangle';
                } else if (diaryEntry.isObservation()) {
                    return 'binoculars';
                } else {
                    return 'crosshairs';
                }
            };

            var getMarkerIcon = function (diaryEntry) {
                return {
                    icon: getMarkerIconName(diaryEntry),
                    markerColor: getMarkerColor(diaryEntry)
                };
            };

            var createMarkerData = function (diaryEntry) {
                return [{
                    id: diaryEntry.type + ':' + diaryEntry.id,
                    etrsCoordinates: diaryEntry.geoLocation,
                    icon: getMarkerIcon(diaryEntry),
                    clickHandler: clickHandler
                }];
            };

            return Markers.transformToLeafletMarkerData(diaryEntryList, markerDefaults, createMarkerData);
        };
    })

    .service('DiaryListSpeciesService', function (DiaryListViewState) {
        var self = this;

        var isSpeciesSelected = function (gameSpeciesCode) {
            return _.some(DiaryListViewState.selectedSpecies, function (selectedSpecies) {
                return selectedSpecies.code === gameSpeciesCode;
            });
        };

        var moveSpeciesBetweenArrays = function (gameSpeciesCode, fromArray, toArray) {
            var arrayOfRemoved = _.remove(fromArray, function (species) {
                return species.code === gameSpeciesCode;
            });

            if (arrayOfRemoved.length === 1) {
                toArray.push(arrayOfRemoved[0]);
            }
        };

        var sortSpeciesArray = function (parameters, speciesArray) {
            return _.sortBy(speciesArray, function (species) {
                return parameters.$getGameName(species.code);
            });
        };

        var constructSpeciesArrayFromDiaryEntries = function (parameters, diaryEntries) {
            var allSpecies = parameters.species;

            if (!allSpecies || allSpecies.length < 1) {
                return [];
            }

            var speciesCodeToEntryCount = _.countBy(_.map(diaryEntries, 'gameSpeciesCode'));

            return sortSpeciesArray(parameters, _.filter(allSpecies, function (species) {
                var count = speciesCodeToEntryCount[species.code];

                if (count) {
                    // Add 'count' field as a side-effect.
                    species.count = count;
                }

                return count;
            }));
        };

        this.updateAllSpecies = function (parameters, diaryEntries, selectAll) {
            DiaryListViewState.allSpecies = constructSpeciesArrayFromDiaryEntries(parameters, diaryEntries);

            if (selectAll) {
                self.selectAllSpecies();
            } else {
                var speciesPartition = _.partition(DiaryListViewState.allSpecies, function (diaryEntrySpecies) {
                    return isSpeciesSelected(diaryEntrySpecies.code);
                });

                DiaryListViewState.selectedSpecies = speciesPartition[0];
                DiaryListViewState.unselectedSpecies = speciesPartition[1];
            }
        };

        this.deselectAllSpecies = function () {
            DiaryListViewState.unselectedSpecies = angular.copy(DiaryListViewState.allSpecies);
            DiaryListViewState.selectedSpecies = [];
        };

        this.selectAllSpecies = function () {
            DiaryListViewState.selectedSpecies = angular.copy(DiaryListViewState.allSpecies);
            DiaryListViewState.unselectedSpecies = [];
        };

        this.speciesAddedToSelection = function (parameters, speciesCode) {
            moveSpeciesBetweenArrays(speciesCode, DiaryListViewState.unselectedSpecies, DiaryListViewState.selectedSpecies);
            DiaryListViewState.selectedSpecies = sortSpeciesArray(parameters, DiaryListViewState.selectedSpecies);
        };

        this.removeSpeciesFromSelection = function (parameters, gameSpeciesCode) {
            moveSpeciesBetweenArrays(gameSpeciesCode, DiaryListViewState.selectedSpecies, DiaryListViewState.unselectedSpecies);
            DiaryListViewState.unselectedSpecies = sortSpeciesArray(parameters, DiaryListViewState.unselectedSpecies);
        };

        this.filterDiaryEntriesBySpeciesSelection = function (allEntries) {
            return _.filter(allEntries, function (entry) {
                return isSpeciesSelected(entry.gameSpeciesCode);
            });
        };
    });
