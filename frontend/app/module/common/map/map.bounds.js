'use strict';

angular.module('app.common.map.bounds', [])
    .service('MapBounds', function ($http, leafletBoundsHelpers, WGS84) {
        var boundsOfFinland = {
            southWest: WGS84.fromETRS(6603008, 60192),
            northEast: WGS84.fromETRS(7776256, 733984)
        };

        var boundsOfMmlBasemap = {
            southWest: WGS84.fromETRS(6291456, -548576),
            northEast: WGS84.fromETRS(8388608, 1548576)
        };

        function _isValidBbox(bbox) {
            var isAbsPositive = function (a) {
                return _.isNumber(a) && Math.abs(a) > 0.001;
            };

            return _.isArray(bbox) && bbox.length === 4 && _.every(bbox, isAbsPositive);
        }

        this.getBoundsOfFinland = function () {
            return angular.copy(boundsOfFinland);
        };

        this.getBoundsOfMmlBasemap = function () {
            return angular.copy(boundsOfMmlBasemap);
        };

        this.isGeoLocationInsideFinland = function (g) {
            return g.latitude >= 6603008 && g.latitude <= 7776256
                && g.longitude >= 60192 && g.longitude <= 733984;
        };

        this.getRhyBounds = function (officialCode) {
            return $http.get('/api/v1/gis/rhy/bounds', {
                params: {
                    officialCode: officialCode
                }
            }).then(function (response) {
                var bounds = response.data;
                return {
                    southWest: {lat: bounds.minLat, lng: bounds.minLng},
                    northEast: {lat: bounds.maxLat, lng: bounds.maxLng},
                    toLeafletBounds: function () {
                        return leafletBoundsHelpers.createLeafletBounds(this);
                    }
                };
            });
        };

        this.getBoundsFromGeoJsonFeatureCollection = function (featureCollection) {
            var bboxArray = _.chain(featureCollection.features)
                .map('bbox')
                .filter(_isValidBbox)
                .value();

            if (_.size(bboxArray) < 1) {
                return null;
            }

            return _.reduce(bboxArray, function (acc, bbox) {
                acc.southWest.lng = acc.southWest.lng ? Math.min(acc.southWest.lng, bbox[0]) : bbox[0];
                acc.southWest.lat = acc.southWest.lat ? Math.min(acc.southWest.lat, bbox[1]) : bbox[1];
                acc.northEast.lng = acc.northEast.lng ? Math.max(acc.northEast.lng, bbox[2]) : bbox[2];
                acc.northEast.lat = acc.northEast.lat ? Math.max(acc.northEast.lat, bbox[3]) : bbox[3];

                return acc;
            }, {
                northEast: {},
                southWest: {}
            });
        };

        this.getBoundsFromGeoJsonBbox = function (bbox) {
            if (!_isValidBbox(bbox)) {
                return null;
            }

            var minLng = bbox[0];
            var minLat = bbox[1];
            var maxLng = bbox[2];
            var maxLat = bbox[3];

            return {
                northEast: {
                    lat: maxLat,
                    lng: maxLng
                },
                southWest: {
                    lat: minLat,
                    lng: minLng
                }
            };
        };

        this.getGeolocationFromGeoJsonBbox = function (bbox, defaultZoom) {
            if (!_isValidBbox(bbox)) {
                return {};
            }

            var sw = WGS84.toETRS(bbox[1], bbox[0]);
            var ne = WGS84.toETRS(bbox[3], bbox[2]);

            return {
                latitude: sw.lat + (ne.lat - sw.lat) / 2,
                longitude: sw.lng + (ne.lng - sw.lng) / 2,
                zoom: defaultZoom || 9
            };
        };

        this.combineBoundsArray = function (boundsArray) {
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
        };

        // Value selected approximately by visual inspection (subjective
        // matter) on what is the most pleasant zoom level that is produced
        // by a given bounding box edge length.
        var minBoundingBoxEdgeLen = 0.01;

        // The purpose of this function is by adjusting ordinate values to
        // ensure that Leaflet markers are not located too near to the
        // borders of the map and that the map is not zoomed in too deep.
        var applyMarginAndMinimumSeparation = function (minOrdinate, maxOrdinate) {
            var offset, diff;

            if (_.isNumber(minOrdinate) && _.isNumber(maxOrdinate)) {
                diff = maxOrdinate - minOrdinate;
                if (diff > 0) {
                    // Move both values 10% further away from each other.
                    offset = diff / 10;
                    maxOrdinate += offset;
                    minOrdinate -= offset;
                }

                diff = maxOrdinate - minOrdinate;
                if (diff < minBoundingBoxEdgeLen) {
                    // Apply minimum size for bounding box edge.
                    offset = (minBoundingBoxEdgeLen - diff) / 2;
                    maxOrdinate += offset;
                    minOrdinate -= offset;
                }
            }

            return {min: minOrdinate, max: maxOrdinate};
        };

        this.getBounds = function (objects, latLngFunc, defaultBounds) {
            defaultBounds = defaultBounds || boundsOfFinland;

            var bounds = _.reduce(objects, function (acc, object) {
                var latLng = latLngFunc(object);
                var lat = latLng.lat;
                var lng = latLng.lng;

                acc.minLat = acc.minLat ? Math.min(acc.minLat, lat) : lat;
                acc.minLng = acc.minLng ? Math.min(acc.minLng, lng) : lng;

                acc.maxLat = acc.maxLat ? Math.max(acc.maxLat, lat) : lat;
                acc.maxLng = acc.maxLng ? Math.max(acc.maxLng, lng) : lng;

                return acc;
            }, {});

            var latPoints = applyMarginAndMinimumSeparation(bounds.minLat, bounds.maxLat);
            var lngPoints = applyMarginAndMinimumSeparation(bounds.minLng, bounds.maxLng);

            var defaultMaxLat = defaultBounds.northEast ? defaultBounds.northEast.lat : undefined;
            var defaultMaxLng = defaultBounds.northEast ? defaultBounds.northEast.lng : undefined;

            var defaultMinLat = defaultBounds.southWest ? defaultBounds.southWest.lat : undefined;
            var defaultMinLng = defaultBounds.southWest ? defaultBounds.southWest.lng : undefined;

            return {
                northEast: {
                    lat: latPoints.max || defaultMaxLat,
                    lng: lngPoints.max || defaultMaxLng
                },
                southWest: {
                    lat: latPoints.min || defaultMinLat,
                    lng: lngPoints.min || defaultMinLng
                }
            };
        };
    });
