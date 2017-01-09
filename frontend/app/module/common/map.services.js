'use strict';

angular.module('app.map.services', [])
    .service('MapState', function (WGS84, LocalStorageService) {
        var state = {
            layerName: LocalStorageService.getKey('selectedLayerName'),
            center: {},
            viewBounds: null
        };

        this.reset = function () {
            state.center = {};
            state.viewBounds = null;
        };

        this.get = function () {
            return state;
        };

        this.getSelectedLayerName = function () {
            return state.layerName;
        };

        this.getZoom = function () {
            if (state.center) {
                return state.center.zoom;
            }

            return null;
        };

        this.updateMapCenter = function (geoLocation, zoom) {
            var latlng = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);

            if (angular.isObject(state.center)) {
                state.center.lat = latlng.lat;
                state.center.lng = latlng.lng;
            } else {
                state.center = latlng;
            }

            if (isFinite(zoom)) {
                state.center.zoom = zoom;
            } else if (!state.center.zoom || state.center.zoom < 12) {
                state.center.zoom = 12;
            }
        };

        this.updateMapBounds = function (bounds, defaultBounds, forceUpdate) {
            if (state.viewBounds === null || state.center === null) {
                forceUpdate = true;
            }

            if (forceUpdate) {
                if (bounds) {
                    state.viewBounds = bounds;
                } else if (defaultBounds) {
                    state.viewBounds = defaultBounds;
                }
            }
        };

        this.toGeoLocation = function () {
            if (angular.isObject(state.center)) {
                var c = WGS84.toETRS(state.center.lat, state.center.lng);

                return {
                    latitude: c.lat,
                    longitude: c.lng,
                    zoom: state.center.zoom
                };
            }

            return null;
        };

        this.setSelectedLayerName = function (name) {
            state.layerName = name;
            LocalStorageService.setKey('selectedLayerName', name);
        };
    })

    .service('WGS84', function () {
        /* global proj4 */
        proj4.defs([
            // ETRS-TM35FIN
            ['EPSG:3067', '+proj=utm +zone=35 +ellps=GRS80 +units=m +no_defs'],
            // WGS84
            ['EPSG:4326', '+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs'],
            // KKJ
            ['EPSG:2393', '+proj=tmerc +lat_0=0 +lon_0=27 +k=1 +x_0=3500000 +y_0=0 +ellps=intl +towgs84=-96.0617,-82.4278,-121.7435,4.80107,0.34543,-1.37646,1.4964 +units=m +no_defs']
        ]);

        function toLatLng(xy) {
            return {lat: xy.y, lng: xy.x};
        }

        function convert(from, to, lat, lng) {
            return toLatLng(proj4(from, to, {x: lng, y: lat}));
        }

        function round(latlng) {
            return {
                lat: Math.round(latlng.lat),
                lng: Math.round(latlng.lng)
            };
        }

        this.toETRS = function (lat, lng) {
            return round(convert('EPSG:4326', 'EPSG:3067', lat, lng));
        };

        this.fromETRS = function (lat, lng) {
            return convert('EPSG:3067', 'EPSG:4326', lat, lng);
        };

        this.fromKKJ = function (lat, lng) {
            return convert('EPSG:2393', 'EPSG:4326', lat, lng);
        };

        this.toKKJ = function (lat, lng) {
            return convert('EPSG:4326', 'EPSG:2393', lat, lng);
        };
    })

    .factory('MapDefaults', function (MapState, leafletMapDefaults, WGS84, $translate) {
        L.CRS.EPSG3067 = new L.Proj.CRS('EPSG:3067',
            '+proj=utm +zone=35 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs',
            {
                tms: true,
                origin: [-548576, 6291456],
                bounds: L.bounds([-548576, 6291456], [1548576, 8388608]),
                resolutions: [
                    8192, 4096, 2048, 1024, 512, 256,
                    128, 64, 32, 16, 8, 4, 2, 1, 0.5,
                    0.25, 0.125, 0.0625, 0.03125, 0.015625
                ]
            }
        );
        // Temporary fix for L.Scale control.
        // see https://github.com/kartena/Proj4Leaflet/issues/109
        L.CRS.EPSG3067.distance = L.CRS.Earth.distance;
        L.CRS.EPSG3067.R = 6378137;

        var maxBounds = {
            southWest: WGS84.fromETRS(6291456, -548576),
            northEast: WGS84.fromETRS(8388608, 1548576)
        };

        var mmlUrlTemplate = _.template('https://kartta.riista.fi/tms/1.0.0/<%= layer %>/EPSG_3067/{z}/{x}/{y}.png');
        var mmlUrls = {
            terrain: mmlUrlTemplate({layer: 'maasto_kiint'}),
            background: mmlUrlTemplate({layer: 'tausta_kiint'}),
            aerial: mmlUrlTemplate({layer: 'orto_kiint'}),
            ortoVaara: mmlUrlTemplate({layer: 'orto_vaara_kiint'}),
            empty: ''
        };

        var mmlOptions = {
            type: 'xyz',
            format: 'image/png',
            minZoom: 3,
            maxZoom: 14,
            maxNativeZoom: 12,
            detectRetina: false,
            continuousWorld: true,
            worldCopyJump: false,
            tms: true,
            bounds: L.latLngBounds(maxBounds.southWest, maxBounds.northEast),
            attribution: '&copy;<a href="http://www.maanmittauslaitos.fi" target="_blank">Maanmittauslaitos</a>'
        };

        var createLayer = function (type) {
            return {
                name: $translate.instant('global.mapLayer.' + type),
                url: mmlUrls[type],
                type: 'xyz',
                layerOptions: angular.copy(mmlOptions)
            };
        };

        var _riistaMapDefaults = {
            controls: {
                scale: {
                    visible: true,
                    position: 'bottomleft',
                    maxWidth: 200,
                    imperial: false,
                    updateWhenIdle: false
                },
                layers: {
                    visible: true,
                    position: 'topleft',
                    collapsed: true
                }
            },
            mmlLayers: {
                baselayers: {
                    terrain: createLayer('terrain'),
                    background: createLayer('background'),
                    aerial: createLayer('aerial'),
                    empty: createLayer('empty')
                }
            },

            geojsonWatchOptions: {
                doWatch: true,
                isDeep: false,
                individual: {
                    doWatch: false,
                    isDeep: false
                }
            },

            // Disable the animation on double-click and other zooms.
            zoomAnimation: false,
            inertia: true,
            minZoom: 3,
            attributionControl: true,
            zoomsliderControl: false,
            zoomControlPosition: 'topleft'
        };

        function _updateSelectLayer(layers) {
            var selectedLayerName = MapState.getSelectedLayerName();
            var selectedLayer = _.find(layers.baselayers, function (layer) {
                return layer.name === selectedLayerName;
            });

            _.each(layers.baselayers, function (layer) {
                layer.top = false;
            });

            if (selectedLayer) {
                selectedLayer.top = true;
            }
        }

        var _create = function (overwrites) {
            var defaults = angular.extend({}, _riistaMapDefaults, overwrites);

            defaults.map = {
                crs: L.CRS.EPSG3067
            };

            _updateSelectLayer(defaults.mmlLayers);

            return defaults;
        };

        var boundsOfFinland = {
            southWest: WGS84.fromETRS(6603008, 60192),
            northEast: WGS84.fromETRS(7776256, 733984)
        };

        // By default, most (if not all) Leaflet map events are not needed to propgage into Angular scope(s)
        var defaultMapEvents = {
            map: {
                logic: 'emit',
                enable: []
            },
            marker: {
                logic: 'emit',
                enable: []
            },
            path: {
                logic: 'emit',
                enable: []
            }
        };

        var _defaultGeoJsonOptions = {
            fillColor: 'green',
            weight: 1,
            opacity: 1,
            color: 'black',
            fillOpacity: 0.3
        };

        return {
            create: function (overwrites) {
                return _create(overwrites || {});
            },
            getGeoJsonOptions: function (overwrites) {
                return angular.extend({}, _defaultGeoJsonOptions, overwrites || {});
            },
            getBoundsOfFinland: function () {
                return angular.copy(boundsOfFinland);
            },
            getMapBroadcastEvents: function (mapEvents) {
                var copy = angular.copy(defaultMapEvents);

                if (angular.isArray(mapEvents)) {
                    _.each(mapEvents, function (e) {
                        copy.map.enable.push(e);
                    });
                }

                return copy;
            }
        };
    })

    .service('GIS', function ($http, leafletBoundsHelpers, $rootScope, WGS84, MapDefaults) {
        this.getPropertyIdentifierForGeoLocation = function (geoLocation) {
            return $http.get('/api/v1/gis/kt', {
                params: {
                    latitude: geoLocation.latitude,
                    longitude: geoLocation.longitude
                }
            });
        };

        this.getRhyForGeoLocation = function (geoLocation) {
            return $http.get('/api/v1/gis/rhy', {
                params: {
                    latitude: geoLocation.latitude,
                    longitude: geoLocation.longitude
                }
            });
        };

        this.getRhyGeom = function (officialCode) {
            return $http.get('/api/v1/gis/rhy/geom', {params: {officialCode: officialCode}});
        };

        var getRhyBounds = function (officialCode) {
            return $http.get('/api/v1/gis/rhy/bounds', {params: {officialCode: officialCode}})
                .then(function (response) {
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
        this.getRhyBounds = getRhyBounds;

        this.getRhyCenter = function (officialCode) {
            return getRhyBounds(officialCode).then(function (rhyBounds) {
                var center = {zoom: 6}; // hand-wavy zoom level default to show most of rhy area
                var sw = WGS84.toETRS(rhyBounds.southWest.lat, rhyBounds.southWest.lng);
                var ne = WGS84.toETRS(rhyBounds.northEast.lat, rhyBounds.northEast.lng);
                center.latitude = sw.lat + (ne.lat - sw.lat) / 2;
                center.longitude = sw.lng + (ne.lng - sw.lng) / 2;
                return center;
            });
        };

        this.getRhyMembershipBoundsOrNull = function () {
            if ($rootScope.account && $rootScope.account.rhyMembership) {
                return getRhyBounds($rootScope.account.rhyMembership.officialCode);
            }
            return null;
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
            defaultBounds = defaultBounds || MapDefaults.getBoundsOfFinland();

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

        this.getBoundsFromGeolocations = function (geoLocations, defaultBounds) {
            defaultBounds = defaultBounds || MapDefaults.getBoundsOfFinland();

            var latLngFunc = function (geoLocation) {
                return WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);
            };

            return this.getBounds(geoLocations, latLngFunc, defaultBounds);
        };

        function _isValidBbox(bbox) {
            var isAbsPositive = function (a) {
                return _.isNumber(a) && Math.abs(a) > 0.001;
            };

            return _.isArray(bbox) && bbox.length === 4 && _.every(bbox, isAbsPositive);
        }

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
                zoom: defaultZoom || 6
            };
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

        this.getPropertyPolygonByCode = function (propertyIdentifier) {
            var params = {propertyIdentifier: propertyIdentifier};

            return $http.get('/api/v1/gis/property/identifier', {params: params});
        };

        this.getPropertyPolygonById = function (id) {
            var params = {id: id};
            return $http.get('/api/v1/gis/property/id', {params: params});
        };

        this.getPropertyByCoordinates = function (latlng) {
            var params = angular.copy(latlng);

            return $http.get('/api/v1/gis/property/point', {params: params});
        };

        this.getPropertyByBounds = function (bounds) {
            var sw = bounds.getSouthWest();
            var ne = bounds.getNorthEast();

            var params = {
                minLat: sw.lat,
                minLng: sw.lng,
                maxLat: ne.lat,
                maxLng: ne.lng
            };

            return $http.get('/api/v1/gis/property/bounds', {params: params});
        };

        this.getPropertiesDWithin = function (latlng, distance) {
            var params = angular.copy(latlng);
            params.distance = distance;

            return $http.get('/api/v1/gis/property/dwithin', {params: params});
        };

        this.getMetsahallitusHirviById = function (id) {
            var params = {id: id};
            return $http.get('/api/v1/gis/mh/hirvi/id', {params: params});
        };

        this.listMetsahallitusHirviByYear = function (year) {
            var params = {year: year};
            return $http.get('/api/v1/gis/mh/hirvi', {params: params});
        };
    })

    .service('Markers', function (WGS84, GIS) {
        // Transforms Javascript objects to Leaflet marker data.
        //
        // extractLeafletMarkerData function is expected to return object having
        // following structure:
        //
        // {
        //   id: <object id>
        //   etrsCoordinates: {
        //     latitude: <latitude in ETRS-TM35FIN coordinate system>
        //     longitude: <longitude in ETRS-TM35FIN coordinate system>
        //   },
        //   popupMessageFields: <either an array of objects or a function returning an array of objects (id passed as argument)>
        //   popupMessageButtons: <either an array of objects or a function returning an array of objects (id passed as argument)>
        //   getMessageScope: <function returning scope within which angular expressions are evaluated; defaults to $rootScope>
        // }
        //
        // popupMessageFields array must have the following form:
        // [
        //   { name: <message field name>, value: <message field value> },
        //   ...
        // ]
        //
        // popupMessageButtons array must have the following form:
        // [
        //   { name: <button name>, handlerExpr: <angular expression> },
        //   ...
        // ]
        //
        this.transformToLeafletMarkerData = function (objects, markerDefaults, extractLeafletMarkerData) {
            return _.flatten(_.map(objects, function (obj) {
                return _.map(extractLeafletMarkerData(obj), function (markerData) {
                    var id = markerData.id,
                        etrsLoc = markerData.etrsCoordinates,
                        wgs84Loc = WGS84.fromETRS(etrsLoc.latitude, etrsLoc.longitude),
                        messageFields = markerData.popupMessageFields,
                        messageButtons = markerData.popupMessageButtons,
                        clickHandler = markerData.clickHandler;

                    markerData = _.merge(angular.copy(markerDefaults), markerData, wgs84Loc);

                    if (markerData.message || messageFields || messageButtons) {
                        if (!markerData.message) {
                            markerData.message = function () {
                                if (angular.isFunction(messageFields)) {
                                    messageFields = messageFields(id);
                                }
                                if (angular.isFunction(messageButtons)) {
                                    messageButtons = messageButtons(id);
                                }
                                return formatMarkerPopupContent(messageFields, messageButtons);
                            };
                        }
                    } else if (clickHandler) {
                        // Noop (currently)
                    }
                    return markerData;

                });
            }));
        };

        function formatMarkerPopupContent(messageFields, buttons) {
            var popupContent = _.reduce(messageFields, function (accumulator, field) {
                var fieldName = field.name,
                    value = field.value,
                    localizationKey = fieldName ? 'global.marker_messages.' + fieldName : undefined,
                    hearderLine = fieldName ? '  <dt><span translate="' + localizationKey + '"></span>:</dt>\n' : '<dt/>\n',
                    valueLine = '  <dd>' + value + '</dd>\n';

                return accumulator + hearderLine + valueLine;
            }, '<dl class="r-leaflet-marker-popup-field">\n') + '</dl>\n';

            if (buttons && buttons.length > 0) {
                popupContent += _.reduce(buttons, function (accumulator, button) {
                    var localizationKey = 'global.marker_messages.' + button.name,
                        linkHtml = '<a ng-click="' + button.handlerExpr + '" class="btn btn-default">' +
                            '<span translate="' + localizationKey + '"></span>' +
                            '</a>\n';

                    return accumulator + linkHtml;
                }, '<div class="text-right">\n') + '</div>\n';
            }

            return popupContent;
        }

        this.getColorForHarvestReportState = function (state) {
            if (state === 'REJECTED') {
                return 'red';
            } else if (state === 'APPROVED' || state === 'ACCEPTED') {
                return 'green';
            } else if (state === 'SENT_FOR_APPROVAL') {
                return 'orange';
            } else if (state === 'PROPOSED') {
                return 'orange';
            }
            return 'red';
        };

        // markerClassFn is a string-returning function that takes
        // a L.MarkerClusterGroup object as a parameter.
        this.iconCreateFunction = function (markerClassFn) {
            return function (cluster) {
                return new L.DivIcon({
                    html: '<div><span>' + cluster.getChildCount() + '</span></div>',
                    className: 'marker-cluster marker-cluster-' + markerClassFn(cluster),
                    iconSize: new L.Point(40, 40)
                });
            };
        };

        this.getMarkerBounds = function(markers, defaultBounds) {
            var markerToLatLng = function (marker) {
                return {
                    lat: marker.lat,
                    lng: marker.lng
                };
            };

            return GIS.getBounds(markers, markerToLatLng, defaultBounds);
        };
    });
