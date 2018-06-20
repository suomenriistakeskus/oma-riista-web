'use strict';

angular.module('app.diary.list.services', ['ngResource'])

    .run(function ($rootScope, DiaryListViewState) {
        // Reset on login and logout
        _.forEach(['loginRequired', 'loginCancelled'], function (eventName) {
            $rootScope.$on('event:auth-' + eventName, function () {
                DiaryListViewState.selectedDiaryEntry = null;
            });
        });
    })

    .factory('DiaryEntries', function ($resource, $http,
                                       DiaryEntryType, DiaryEntryRepositoryFactory, SrvaOtherSpeciesService) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        var repository = $resource('api/v1/gamediary', {}, {
            search: {
                method: 'POST',
                isArray: true,
                transformResponse: appendTransform($http.defaults.transformResponse, function(data, headersGetter, status) {
                    if (status === 200 && angular.isArray(data)) {
                        return SrvaOtherSpeciesService.replaceNullsWithOtherSpeciesCodeInEntries(data);
                    } else {
                        return data || [];
                    }
                })
            }
        });

        return DiaryEntryRepositoryFactory.decorateRepository(repository);
    })

    .factory('DiaryListViewState', function (HuntingYearService) {
        return {
            showHarvest: true,
            showObservation: true,
            showSrvaEvent: true,
            onlyReports: false,
            onlyTodo: false,
            beginDate: HuntingYearService.getBeginDateStr(),
            endDate: HuntingYearService.getEndDateStr(),
            allSpecies: [],
            selectedSpecies: [],
            unselectedSpecies: [],
            selectedDiaryEntry: null
        };
    })

    .service('DiaryListService', function (MapDefaults, MapState, MapBounds, WGS84,
                                           DiaryListViewState, DiaryEntrySidebar) {
        this.getEntryBounds = function (diaryEntryList, defaultBounds) {
            var geoLocations = _.chain(diaryEntryList)
                .map('geoLocation')
                .filter()
                .filter(MapBounds.isGeoLocationInsideFinland)
                .value();

            var latLngFunc = function (geoLocation) {
                return WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);
            };

            return MapBounds.getBounds(geoLocations, latLngFunc, defaultBounds || MapBounds.getBoundsOfFinland());
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

                var modalInstance = DiaryEntrySidebar.showSidebar(diaryEntry);

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
    });
