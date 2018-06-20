'use strict';

angular.module('app.clubhunting.map', [])

    .service('ClubHuntingViewMarkers', function (Markers) {
        var markerClassFn = function (cluster) {
            var unaccepted = _.find(cluster.getAllChildMarkers(), function (marker) {
                var isAccepted = marker.options.isAccepted;
                return _.isBoolean(isAccepted) && !isAccepted;
            });

            return unaccepted ? 'unaccepted' : 'accepted';
        };

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
                showCoverageOnHover: true,
                iconCreateFunction: Markers.iconCreateFunction(markerClassFn)
            },
            group: 'Harvests'
        };

        var getMarkerIconName = function (diaryEntry) {
            return diaryEntry.isObservation() ? 'binoculars' : 'crosshairs';
        };

        this.createMarkers = function (entryList, clickHandler, $scope) {
            function createMarkerData(entry) {
                var isAccepted = _.isNumber(entry.huntingDayId);

                return [{
                    id: entry.type + ':' + entry.id,
                    etrsCoordinates: entry.geoLocation,
                    isAccepted: isAccepted,
                    icon: {
                        icon: getMarkerIconName(entry),
                        markerColor: isAccepted ? 'green' : 'red'
                    },
                    getMessageScope: function () {
                        return $scope;
                    },
                    clickHandler: clickHandler ? clickHandler : _.noop // sanity check
                }];
            }

            return Markers.transformToLeafletMarkerData(entryList, markerDefaults, createMarkerData);
        };
    })

    .component('clubHuntingMap', {
        templateUrl: 'club/hunting/map/map.html',
        bindings: {
            diary: '<',
            huntingArea: '<',
            defaultBounds: '<',
            forceBoundsCalculation: '<',
            onMarkerClick: '&'
        },
        controller: function (leafletData, MapState, MapDefaults, MapBounds, Markers, ClubHuntingViewMarkers) {
            var $ctrl = this;

            $ctrl.mapState = MapState.get();
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapDefaults = MapDefaults.create();

            // Markers
            $ctrl.markers = [];
            $ctrl.markerBounds = null;

            // Club area
            $ctrl.vectorLayer = null;
            $ctrl.mapBounds = null;

            $ctrl.$onInit = function () {
                MapState.updateMapBounds(null, $ctrl.defaultBounds, false);
            };

            $ctrl.$onChanges = function (c) {
                if (c.diary) {
                    setDiary(c.diary.currentValue);
                }

                if (c.huntingArea) {
                    updateMap(c.huntingArea.currentValue);
                }

                var combinedBounds = MapBounds.combineBoundsArray([$ctrl.mapBounds, $ctrl.markerBounds]);
                MapState.updateMapBounds(combinedBounds, $ctrl.defaultBounds, $ctrl.forceBoundsCalculation);
            };

            function markerClickHandler(markerId) {
                var parts = markerId.split(':');

                if (parts.length === 2) {
                    $ctrl.onMarkerClick({
                        type: parts[0],
                        id: _.parseInt(parts[1])
                    });
                }
            }

            function setDiary(diary) {
                if (diary) {
                    $ctrl.markers = ClubHuntingViewMarkers.createMarkers(diary, markerClickHandler, $ctrl);
                    $ctrl.markerBounds = _.size($ctrl.markers) > 0 ? Markers.getMarkerBounds($ctrl.markers) : null;
                }
            }

            function updateMap(huntingArea) {
                if (huntingArea && huntingArea.bounds && huntingArea.areaId) {
                    var bounds = huntingArea.bounds;
                    var vectorLayerTemplate = _.template('/api/v1/vector/hunting-club-area/<%= id %>/{z}/{x}/{y}');

                    $ctrl.vectorLayer = {
                        url: vectorLayerTemplate({id: huntingArea.areaId}),
                        bounds: {
                            southWest: {lat: bounds.minLat, lng: bounds.minLng},
                            northEast: {lat: bounds.maxLat, lng: bounds.maxLng}
                        }
                    };
                    $ctrl.mapBounds = $ctrl.vectorLayer.bounds;

                } else {
                    $ctrl.vectorLayer = null;
                    $ctrl.mapBounds = null;
                }
            }
        }
    });
