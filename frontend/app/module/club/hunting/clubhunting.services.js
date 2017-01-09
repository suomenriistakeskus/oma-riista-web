'use strict';

angular.module('app.clubhunting.services', [])
    .service('ClubHunting', function (Markers) {
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
    });
