'use strict';

angular.module('app.common.map.services', [])
    .service('MapUtil', function (leafletData, $timeout) {
        this.isValidLatLng = function (latlng) {
            return _.isObject(latlng) &&
                isFinite(latlng.lat) &&
                isFinite(latlng.lng) &&
                latlng.lat > 0 &&
                latlng.lng > 0;
        };

        this.isValidGeoLocation = function (geoLocation) {
            return _.isObject(geoLocation) &&
                isFinite(geoLocation.latitude) &&
                isFinite(geoLocation.longitude) &&
                geoLocation.latitude > 0 &&
                geoLocation.longitude > 0;
        };

        this.limitDefaultZoom = function (currentZoom) {
            var defaultZoom = 11;
            var minZoom = 5;
            var maxZoom = 16;

            if (!isFinite(currentZoom)) {
                return defaultZoom;
            }

            if (currentZoom < minZoom) {
                return minZoom;
            }

            if (currentZoom > maxZoom) {
                return maxZoom;
            }

            return currentZoom;
        };

        this.getDefaultGeoLocation = function () {
            return {
                latitude: 7150000,
                longitude: 570000,
                zoom: 6
            };
        };

        // Map container may not be able to resolve map content size in creation time if nested inside e.g. bootstrap modal
        // Solution is to invalidate map size (force recalculation of container) and recalculate bounding rect
        this.forceRefreshMapArea = function (mapId, bounds) {
            leafletData.getMap(mapId).then(function(map) {
                $timeout(function () {
                    map.invalidateSize();
                    //create bounds
                    leafletData.getMap(mapId).then(function (map) {
                        var bbox = L.latLngBounds(bounds.toLeafletBounds());
                        map.fitBounds(bbox);
                    });
                });
            });
        };
    })
    .service('MapState', function (WGS84, MapUtil, LocalStorageService) {
        var state = {
            layerName: LocalStorageService.getKey('selectedLayerName'),
            overlayNames: loadOverlayNames(),
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

        this.getSelectedOverlayNames = function () {
            return state.overlayNames;
        };

        this.setSelectedLayerName = function (name) {
            state.layerName = name;
            LocalStorageService.setKey('selectedLayerName', name);
        };

        this.setOverlayEnabled = function (overlayName) {
            state.overlayNames = state.overlayNames || [];
            state.overlayNames = _.pull(state.overlayNames, overlayName);
            state.overlayNames.push(overlayName);
            storeOverlayNames();
        };

        this.setOverlayDisabled = function (overlayName) {
            state.overlayNames = state.overlayNames || [];
            state.overlayNames = _.pull(state.overlayNames, overlayName);
            storeOverlayNames();
        };

        function storeOverlayNames() {
            LocalStorageService.setKey('selectedOverlayNames', JSON.stringify(state.overlayNames));
        }

        function loadOverlayNames() {
            try {
                var value = JSON.parse(LocalStorageService.getKey('selectedOverlayNames'));
                return _.isArray(value) ? value : null;
            } catch (e) {
                return null;
            }
        }

        this.getZoom = function () {
            return state.center ? state.center.zoom : null;
        };

        this.setMapCenterLatLng = function (latlng) {
            if (MapUtil.isValidLatLng(latlng)) {
                state.center = angular.copy(latlng);
            }
        };

        this.updateMapCenter = function (geoLocation, zoom) {
            var latlng = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);

            if (angular.isObject(state.center)) {
                state.center.lat = latlng.lat;
                state.center.lng = latlng.lng;
            } else {
                state.center = angular.copy(latlng);
            }

            state.center.zoom = MapUtil.limitDefaultZoom(zoom || state.center.zoom);
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

                if (state.center) {
                    state.center = {};
                }
            }
        };

        this.toGeoLocationOrDefault = function (defaultGeoLocation) {
            if (MapUtil.isValidLatLng(state.center)) {
                var c = WGS84.toETRS(state.center.lat, state.center.lng);

                return {
                    latitude: c.lat,
                    longitude: c.lng,
                    zoom: state.center.zoom
                };
            }

            // fallback to default
            if (MapUtil.isValidGeoLocation(defaultGeoLocation)) {
                return defaultGeoLocation;
            }

            return MapUtil.getDefaultGeoLocation();
        };
    })

    .service('WGS84', function () {
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

    .service('SelectedMapLayers', function (MapState) {
        this.activateSelectedBaseLayer = function (layers) {
            var selectedLayerName = MapState.getSelectedLayerName();
            var selectedLayer = _.find(layers, function (layer) {
                return layer.name === selectedLayerName;
            });

            _.forEach(layers, function (layer) {
                layer.top = false;
            });

            if (selectedLayer) {
                selectedLayer.top = true;
            }
        };

        this.activateSelectedOverlays = function (layers) {
            var selectedOverlayNames = MapState.getSelectedOverlayNames();

            _.forEach(layers, function (overlay) {
                overlay.visible = _.includes(selectedOverlayNames, overlay.name);
            });
        };
    })

    .service('BaseMapLayers', function (MapBounds, $translate) {
        var maxBounds = MapBounds.getBoundsOfMmlBasemap();

        var mmlUrlTemplate = _.template('https://kartta.riista.fi/tms/1.0.0/<%= layer %>/EPSG_3857/{z}/{x}/{y}.png');
        var mmlUrls = {
            terrain: mmlUrlTemplate({layer: 'maasto_mobile'}),
            background: mmlUrlTemplate({layer: 'tausta_mobile'}),
            aerial: mmlUrlTemplate({layer: 'orto_mobile'}),
            empty: ''
        };

        this.createLayer = function (type) {
            return {
                name: $translate.instant('global.map.layer.' + type),
                url: mmlUrls[type],
                type: 'xyz',
                layerOptions: {
                    type: 'xyz',
                    format: 'image/png',
                    minZoom: 0,
                    maxZoom: 16,
                    maxNativeZoom: 16,
                    detectRetina: false,
                    continuousWorld: true,
                    worldCopyJump: false,
                    tms: true,
                    bounds: L.latLngBounds(maxBounds.southWest, maxBounds.northEast),
                    attribution: '&copy;<a href="http://www.maanmittauslaitos.fi" target="_blank">Maanmittauslaitos</a>'
                }
            };
        };
    })

    .service('VectorMapLayers', function ($translate, MapBounds) {
        var maxBounds = MapBounds.getBoundsOfMmlBasemap();
        var vectorLayerTemplate = _.template('https://kartta.riista.fi/vector/<%= layer %>/{z}/{x}/{y}');

        this.createLayer = function (vectorGridLayerName, style) {
            var vectorTileLayerStyles = {};
            vectorTileLayerStyles[vectorGridLayerName] = style;

            var layer = L.vectorGrid.protobuf(vectorLayerTemplate({layer: vectorGridLayerName}), {
                interactive: true,
                pane: 'overlayPane',
                updateWhenZooming: true,
                keepBuffer: 10,
                maxZoom: 16,
                rendererFactory: L.canvas.tile,
                bounds: L.latLngBounds(maxBounds.southWest, maxBounds.northEast),
                vectorTileLayerStyles: vectorTileLayerStyles
            });

            var popup = L.popup({
                closeButton: false,
                closeOnClick: true
            });

            layer.on('mouseover', function (e) {
                var map = e.target._map;
                var name = _.get(e.layer.properties, 'KOHDE_NIMI', '');

                if (!_.isEmpty(name)) {
                    L.DomEvent.stopPropagation(e);
                    popup.setContent(name).setLatLng(e.latlng).openOn(map);
                }
            });

            return {
                name: $translate.instant('global.map.overlay.' + vectorGridLayerName),
                type: 'custom',
                layer: layer
            };
        };

        this.createRandomColourFunction = function (kohdeCount) {
            return function (feature) {
                var kohdeId = _.get(feature, 'KOHDE_ID', 0);

                // Multiply by small prime to alternate colour for bordering polygons
                var tmp = 97 * kohdeId / kohdeCount;

                return {
                    fill: true,
                    fillColor: 'hsl(' + Math.round(tmp * 256) % 256 + ',100%,40%)',
                    fillOpacity: 0.25,
                    weight: 0.75,
                    color: 'black'
                };
            };
        };
    })

    .service('MapDefaults', function (BaseMapLayers, VectorMapLayers, SelectedMapLayers) {
        var mapBaseLayers = {
            terrain: BaseMapLayers.createLayer('terrain'),
            background: BaseMapLayers.createLayer('background'),
            aerial: BaseMapLayers.createLayer('aerial'),
            empty: BaseMapLayers.createLayer('empty')
        };

        var mapOverlays = {
            // Style definition contains hard-coded counts of known distinct layer features
            hirvi: VectorMapLayers.createLayer('hirvi', VectorMapLayers.createRandomColourFunction(348)),
            pienriista: VectorMapLayers.createLayer('pienriista', VectorMapLayers.createRandomColourFunction(123)),
            metsahallitus: VectorMapLayers.createLayer('metsahallitus', {
                fill: true,
                fillColor: 'blue',
                fillOpacity: 0.25,
                weight: 0.75,
                color: 'black'
            }),
            riistakolmiot: VectorMapLayers.createLayer('riistakolmiot', {
                fill: true,
                fillColor: 'red',
                fillOpacity: 0.25,
                weight: 0.75,
                color: 'black'
            }),
            rhy: VectorMapLayers.createLayer('rhy', {
                fill: false,
                weight: 5.0,
                color: 'blue'
            })
        };

        this.create = function (cfg) {
            var hideOverlays = _.get(cfg, 'hideOverlays', false);
            var scrollWheelZoom = _.get(cfg, 'scrollWheelZoom', true);
            var doubleClickZoom = _.get(cfg, 'doubleClickZoom', true);

            if (hideOverlays) {
                _.forEach(mapOverlays, function (overlay) {
                    overlay.visible = false;
                });
            } else {
                SelectedMapLayers.activateSelectedOverlays(mapOverlays);
            }

            SelectedMapLayers.activateSelectedBaseLayer(mapBaseLayers);

            var defaults = angular.extend({}, {
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
                    baselayers: mapBaseLayers,
                    overlays: mapOverlays
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
                fadeAnimation: false,
                scrollWheelZoom: scrollWheelZoom,
                doubleClickZoom: doubleClickZoom,
                inertia: true,
                minZoom: 5,
                attributionControl: true,
                zoomsliderControl: false,
                zoomControlPosition: 'topleft',

                map: {
                    crs: L.CRS.EPSG3857
                }
            });

            if (cfg && cfg.fullscreen) {
                defaults.controls.custom = new L.Control.Fullscreen({
                    pseudoFullscreen: true,
                    position: 'topright',
                    title: {
                        'false': 'Kokoruutu',
                        'true': 'Pois kokoruudusta'
                    }
                });
            }
            return defaults;
        };

        this.getGeoJsonOptions = function (overwrites) {
            return angular.extend({}, {
                fillColor: 'green',
                weight: 1,
                opacity: 1,
                color: 'black',
                fillOpacity: 0.3
            }, overwrites || {});
        };

        this.getMapBroadcastEvents = function (mapEvents) {
            // By default, most (if not all) Leaflet map events are not needed to be propagated into Angular scope(s)
            var broadcastEvents = {
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

            if (angular.isArray(mapEvents)) {
                _.forEach(mapEvents, function (e) {
                    broadcastEvents.map.enable.push(e);
                });
            }

            return broadcastEvents;
        };
    })

    .service('GIS', function ($http, MapBounds, WGS84) {
        var self = this;

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

        this.getMunicipalityForGeoLocation = function (geoLocation) {
            return $http.get('/api/v1/gis/municipality', {
                params: {
                    latitude: geoLocation.latitude,
                    longitude: geoLocation.longitude
                }
            });
        };

        this.getRhyGeom = function (officialCode) {
            return $http.get('/api/v1/gis/rhy/geom', {params: {officialCode: officialCode}});
        };

        this.getInvertedRhyGeoJSON = function (officialCode, featureId, props) {
            return self.getRhyGeom(officialCode).then(function (response) {
                return {
                    type: "FeatureCollection",
                    features: [
                        {
                            type: "Feature",
                            id: featureId,
                            properties: props,
                            geometry: {
                                type: "MultiPolygon",
                                coordinates: [[[[-180, -180], [180, 0], [180, 180], [0, 180], [-180, -180]],
                                    response.data.coordinates[0][0]]]
                            }
                        }
                    ]
                };
            });
        };

        this.getRhyCenter = function (officialCode) {
            return MapBounds.getRhyBounds(officialCode).then(function (rhyBounds) {
                var center = {zoom: 8}; // hand-wavy zoom level default to show most of rhy area
                var sw = WGS84.toETRS(rhyBounds.southWest.lat, rhyBounds.southWest.lng);
                var ne = WGS84.toETRS(rhyBounds.northEast.lat, rhyBounds.northEast.lng);
                center.latitude = sw.lat + (ne.lat - sw.lat) / 2;
                center.longitude = sw.lng + (ne.lng - sw.lng) / 2;
                return center;
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

        this.getMetsahallitusHirviById = function (id) {
            var params = {id: id};
            return $http.get('/api/v1/gis/mh/hirvi/id', {params: params});
        };

        this.listMetsahallitusHirviByYear = function (year) {
            var params = {year: year};
            return $http.get('/api/v1/gis/mh/hirvi', {params: params});
        };
    })

    .service('PolygonService', function ($http) {
        var self = this;

        function geometryToPolygons(data) {
            if (!data) {
                return [];
            }

            switch (data.type) {
                case 'Polygon':
                    // Keep exterior rings only
                    return [_.head(data.coordinates)];

                case 'MultiPolygon':
                    return _(data.coordinates).map(_.head).value();

                case 'GeometryCollection':
                    return _.chain(data.geometries)
                        .map(geometryToPolygons)
                        .flatten()
                        .value();

                default:
                    return [];
            }
        }

        this.geometryToPolygons = geometryToPolygons;

        function resultToMultiPolygon(result, defaultValue) {
            return result && typeof result[0][0] === 'number' ? [result] : (result || defaultValue);
        }

        function isPointInsidePolygon(polygon, point) {
            var oddNodes = false, edgeCounter = 1,
                current = polygon[0], next = polygon[1],
                pointX = point[0], pointY = point[1];

            do {
                var currentX = current[0];
                var currentY = current[1];
                var nextX = next[0];
                var nextY = next[1];

                if ((currentY < pointY && nextY >= pointY ||
                    nextY < pointY && currentY >= pointY) &&
                    (currentX <= pointX || nextX <= pointX)) {
                    /* jshint ignore:start */
                    oddNodes ^= (currentX + (pointY - currentY) /
                        (nextY - currentY) * (nextX - currentX) < pointX);
                    /* jshint ignore:end */
                }

                edgeCounter++;
                current = next;
                next = edgeCounter < polygon.length ? polygon[edgeCounter] : polygon[0];
            } while (current !== polygon[0]);

            return !!oddNodes;
        }

        function isPolygonInsidePolygon(a, b) {
            return _.every(a, function (point) {
                return isPointInsidePolygon(b, point);
            });
        }

        this.difference = function (clipPolygon, bboxPolygons) {
            return _(bboxPolygons).map(function (sourcePolygon) {
                // Clipping requires intersection -> clip region inside -> no action
                if (isPolygonInsidePolygon(clipPolygon, sourcePolygon)) {
                    return [sourcePolygon];
                }

                // Clipping requires intersection -> clip region outside -> remove region
                if (isPolygonInsidePolygon(sourcePolygon, clipPolygon)) {
                    return null;
                }

                return resultToMultiPolygon(greinerHormann.diff(sourcePolygon, clipPolygon), [sourcePolygon]);
            }).filter().flatten().value();
        };

        this.intersection = function (clipPolygon, bboxPolygons) {
            return _(bboxPolygons).map(function (sourcePolygon) {
                return resultToMultiPolygon(greinerHormann.intersection(sourcePolygon, clipPolygon));
            }).filter().flatten().value();
        };

        this.joinPolygons = function (inputPolygons) {
            var requestData = {
                type: 'FeatureCollection',
                features: _.map(inputPolygons, function (p) {
                    return L.GeoJSON.asFeature({
                        type: 'Polygon',
                        coordinates: [p]
                    });
                })
            };

            return $http.post('/api/v1/gis/polygonUnion', requestData).then(function (response) {
                return self.geometryToPolygons(response.data);
            });
        };
    })

    .service('MapPdfModal', function ($uibModal, FormPostService) {
        var self = this;

        this.printArea = function (url) {
            self.showModal().then(function (pdfParameters) {
                FormPostService.submitFormUsingBlankTarget(url, pdfParameters);
            });
        };

        this.showModal = function () {
            var modalInstance = $uibModal.open({
                controller: ModalController,
                templateUrl: 'common/map/map-pdf.html',
                controllerAs: '$ctrl',
                bindToController: true
            });

            return modalInstance.result;
        };

        function ModalController($uibModalInstance) {
            var $ctrl = this;

            $ctrl.request = {
                paperSize: 'A4',
                paperOrientation: 'PORTRAIT',
                layer: 'MAASTOKARTTA',
                overlay: 'NONE'
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.request);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss();
            };
        }
    })

    .service('Markers', function (WGS84, MapBounds) {
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

        this.getMarkerBounds = function (markers, defaultBounds) {
            var markerToLatLng = function (marker) {
                return {
                    lat: marker.lat,
                    lng: marker.lng
                };
            };

            return MapBounds.getBounds(markers, markerToLatLng, defaultBounds);
        };
    });
