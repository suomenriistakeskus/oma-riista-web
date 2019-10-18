'use strict';

angular.module('app.common.map.directives', [])

    .directive('eventBroadcast', function (MapState) {
        // Extend leaflet directive to register baselayer change event globally
        return {
            restrict: "A",
            priority: 1,
            scope: false,
            replace: false,
            require: 'leaflet',

            link: function link(scope, element, attrs, controller) {
                controller.getMap().then(function (map) {
                    map.on('baselayerchange', function (leafletEvent) {
                        MapState.setSelectedLayerName(leafletEvent.name);
                    });

                    map.on('overlayadd', function (leafletEvent) {
                        MapState.setOverlayEnabled(leafletEvent.name);
                    });

                    map.on('overlayremove', function (leafletEvent) {
                        MapState.setOverlayDisabled(leafletEvent.name);
                    });

                    map.on('unload', function () {
                        map.off('overlayadd overlayremove baselayerchange');
                    });
                });
            }
        };
    })

    .directive('leafletVectorLayer', function ($parse, leafletBoundsHelpers) {
        return {
            require: '^leaflet',
            restrict: 'A',
            scope: false,
            replace: false,
            link: function ($scope, element, attrs, leafletCtrl) {
                var vectorLayerAttr = $parse(attrs.leafletVectorLayer);
                var leafletVectorGrid = null;

                leafletCtrl.getMap().then(function (map) {
                    $scope.$watch(vectorLayerAttr, function (props) {
                        if (leafletVectorGrid) {
                            map.removeLayer(leafletVectorGrid);
                            leafletVectorGrid = null;
                        }

                        if (props && props.url) {
                            leafletVectorGrid = createVectorLayer(props.url, props.bounds).addTo(map);
                        }
                    });
                });

                function createVectorLayer(url, bounds) {
                    return L.vectorGrid.protobuf(url, {
                        minZoom: 6,
                        pane: 'overlayPane',
                        rendererFactory: L.canvas.tile,
                        fetchOptions: {
                            credentials: 'include'
                        },
                        bounds: bounds ? leafletBoundsHelpers.createLeafletBounds(bounds) : null,
                        vectorTileLayerStyles: {
                            all: {
                                fill: true,
                                fillColor: 'green',
                                fillOpacity: 0.25,
                                weight: 1,
                                color: 'black'
                            }
                        }
                    });
                }
            }
        };
    })

    .component('rHuntingAreaAsVectorLayer', {
        template: '<div leaflet-vector-layer="$ctrl.vectorLayer"></div>',
        require: {
            leaflet: '^^leaflet'
        },
        bindings: {
            area: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                if ($ctrl.area && $ctrl.area.bounds && $ctrl.area.areaId) {
                    var bounds = $ctrl.area.bounds;
                    var vectorLayerTemplate = _.template('/api/v1/vector/hunting-club-area/<%= id %>/{z}/{x}/{y}');

                    $ctrl.vectorLayer = {
                        url: vectorLayerTemplate({id: $ctrl.area.areaId}),
                        bounds: {
                            southWest: {lat: bounds.minLat, lng: bounds.minLng},
                            northEast: {lat: bounds.maxLat, lng: bounds.maxLng}
                        }
                    };
                } else {
                    $ctrl.vectorLayer = null;
                }
            };
        }
    })

    .directive('rGeolocationKeepMarkerCenter', function (MapUtil) {
        return {
            scope: false,
            replace: false,
            require: ['rGeolocationMarker', 'rGeolocationCenter'],

            link: function (scope, element, attrs, ctrls) {
                var markerController = ctrls[0];
                var centerController = ctrls[1];

                scope.rGeolocationKeepMarkerCenterValue = markerController.getLocation();

                scope.$watch('rGeolocationKeepMarkerCenterValue', function (geoLocation) {
                    if (MapUtil.isValidGeoLocation(geoLocation)) {
                        centerController.updateCenter(geoLocation);
                    }
                }, true);
            }
        };
    })

    .directive('rGeolocationCenter',
        function ($timeout, $parse, leafletHelpers, leafletMapEvents, MapUtil, MapState, WGS84) {
            function geoLocationToLatLng(geoLocation, currentZoom) {
                var latlng = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);
                latlng.zoom = MapUtil.limitDefaultZoom(geoLocation.zoom || currentZoom);

                return latlng;
            }

            return {
                restrict: 'A',
                require: ['rGeolocationCenter', 'leaflet'],
                scope: false,
                replace: false,
                controller: function () {
                    var $ctrl = this;

                    $ctrl.updateCenter = function (geoLocation, zoom) {
                        if (!MapUtil.isValidGeoLocation(geoLocation)) {
                            return;
                        }

                        var actualZoom = zoom || MapState.getZoom() || $ctrl.map.getZoom();
                        $ctrl.updateLatLng(geoLocationToLatLng(geoLocation, actualZoom));
                    };

                    $ctrl.updateLatLng = function (latlng) {
                        if (!MapUtil.isValidLatLng(latlng)) {
                            return;
                        }

                        MapState.setMapCenterLatLng(latlng);

                        if (!leafletHelpers.isSameCenterOnMap(latlng, $ctrl.map)) {
                            $ctrl.map.setView([latlng.lat, latlng.lng], latlng.zoom);

                            $timeout(function () {
                                // This fixes map size in dialogs
                                $ctrl.map.invalidateSize({reset: true});
                            });
                        }
                    };
                },
                link: function ($scope, element, attrs, ctrls) {
                    var $ctrl = ctrls[0];
                    var leafletCtrl = ctrls[1];

                    leafletCtrl.getMap().then(function (map) {
                        $ctrl.map = map;

                        var geoLocationValue = $parse(attrs.rGeolocationCenter);
                        var defaultZoomGetter = $parse(attrs.defaultZoom);

                        $scope.$watch(geoLocationValue, function (geoLocation) {
                            var defaultZoom = $scope.$eval(defaultZoomGetter);
                            $ctrl.updateCenter(geoLocation, defaultZoom);
                        });

                        map.on('moveend', function () {
                            MapState.setMapCenterLatLng({
                                lat: map.getCenter().lat,
                                lng: map.getCenter().lng,
                                zoom: map.getZoom()
                            });
                        });
                    });
                }
            };
        }
    )

    .directive('rGeolocationMarker',
        function ($parse, leafletMarkersHelpers, versionUrlPrefix, MapUtil, WGS84, GIS) {
            var location = {};

            function updateCoordinates(lat, lng) {
                location.latitude = lat;
                location.longitude = lng;
            }

            return {
                restrict: "A",
                scope: false,
                replace: false,
                require: 'leaflet',
                controller: function () {
                    this.getLocation = function () {
                        return location;
                    };
                },

                link: function (scope, element, attrs, mapController) {
                    var geoLocationGetter = $parse(attrs.rGeolocationMarker);
                    var geoLocationEditableGetter = $parse(attrs.rGeolocationEditable);
                    var rGeolocationMarkerForceFinland = $parse(attrs.rGeolocationMarkerForceFinland)();

                    function _assignGeolocationToScope(lat, lng) {
                        if (lat && lng) {
                            geoLocationGetter.assign(scope, {
                                latitude: lat,
                                longitude: lng,
                                accuracy: 0,
                                source: 'MANUAL'
                            });
                            updateCoordinates(lat, lng);
                        }
                    }

                    function _resolveGeolocationAndAssign(latlng) {
                        var geoLocation = WGS84.toETRS(latlng.lat, latlng.lng);
                        var lat = Math.round(geoLocation.lat);
                        var lng = Math.round(geoLocation.lng);

                        if (rGeolocationMarkerForceFinland) {
                            var ok = function () {
                                _assignGeolocationToScope(lat, lng);
                            };
                            var nok = function () {
                                _assignGeolocationToScope(null, null);
                            };
                            GIS.getRhyForGeoLocation({latitude: lat, longitude: lng}).then(ok, nok);
                        } else {
                            _assignGeolocationToScope(lat, lng);
                        }
                    }

                    function _createMarker(markerData) {
                        var leafletMarker = leafletMarkersHelpers.createMarker(markerData);

                        // Add event handler to update Geolocation after moving the marker
                        leafletMarker.on('dragend', function (e) {
                            _resolveGeolocationAndAssign(leafletMarker.getLatLng());
                            scope.$digest();
                        });

                        return leafletMarker;
                    }

                    function _addMarker(map, geoLocation, draggable) {
                        var markerData = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);
                        markerData.draggable = draggable;
                        markerData.icon = {
                            type: 'icon',
                            icon: new L.Icon.Default({
                                iconUrl: versionUrlPrefix + '/css/images/marker-icon.png',
                                iconRetinaUrl: versionUrlPrefix + '/css/images/marker-icon-2x.png',
                                shadowUrl: versionUrlPrefix + '/css/images/marker-shadow.png',
                                iconSize: [25, 41],
                                iconAnchor: [12, 41],
                                popupAnchor: [1, -34],
                                shadowSize: [41, 41]
                            })
                        };

                        scope.leafletMarker = _createMarker(markerData);

                        map.addLayer(scope.leafletMarker);
                        // accuracy circle
                        if (geoLocation.accuracy) {
                            var accuracyCircleSettings = {weight: 2, opacity: 1, fillOpacity: 0.4};
                            scope.leafletCircle = L.circle(markerData, geoLocation.accuracy, accuracyCircleSettings);
                            scope.leafletCircle.addTo(map);
                        }
                    }

                    function _removeMarker(map) {
                        if (scope.leafletMarker) {
                            leafletMarkersHelpers.deleteMarker(scope.leafletMarker, map, null);
                        }
                        if (scope.leafletCircle) {
                            map.removeLayer(scope.leafletCircle);
                        }
                    }

                    function _replaceMarker(map, geoLocation, markerEditable) {
                        _removeMarker(map);
                        _addMarker(map, geoLocation, markerEditable);
                    }

                    mapController.getMap().then(function (map) {
                        var mapClickHandler = function (leafletEvent) {
                            _resolveGeolocationAndAssign(leafletEvent.latlng);
                        };

                        scope.$watchGroup([geoLocationGetter, geoLocationEditableGetter], function (vals) {
                            var geoLocation = vals[0],
                                editable = vals[1];

                            // Add event handler to set geoLocation selected position
                            if (editable) {
                                map.on('click', mapClickHandler);
                            } else {
                                map.off('click', mapClickHandler);
                            }

                            if (MapUtil.isValidGeoLocation(geoLocation)) {
                                _replaceMarker(map, geoLocation, editable);
                                updateCoordinates(geoLocation.latitude, geoLocation.longitude);
                            } else {
                                _removeMarker(map);
                            }
                        });
                    });
                }
            };
        }
    )

    .directive('rGeolocationInput', function ($parse, WGS84) {
        function _withinBounds(latlng) {
            return latlng.lat >= 59 && latlng.lat <= 71 &&
                latlng.lng >= 19 && latlng.lng <= 32;
        }

        function _round(latlng) {
            latlng.lat = Math.round(latlng.lat);
            latlng.lng = Math.round(latlng.lng);
            return latlng;
        }

        function _filterFloat(value) {
            return (/^(\-|\+)?([0-9]+(\.[0-9]+)?)$/.test(value)) ? parseFloat(value) : null;
        }

        function _parseInput(inputFields) {
            var result = {
                lat: _filterFloat(inputFields.lat),
                lng: _filterFloat(inputFields.lng)
            };
            return isFinite(result.lat) && isFinite(result.lng) ? result : null;
        }

        function _toWGS84(latlng, inputCrs) {
            if (inputCrs === 'ETRS-TM35FIN') {
                return WGS84.fromETRS(latlng.lat, latlng.lng);
            } else if (inputCrs === 'KKJ') {
                return WGS84.fromKKJ(latlng.lat, latlng.lng);
            } else {
                return latlng;
            }
        }

        function _toInputFieldValues(geoLocation, crs) {
            if (!geoLocation) {
                return {};

            } else if (crs === 'ETRS-TM35FIN') {
                return {
                    lat: geoLocation.latitude,
                    lng: geoLocation.longitude
                };

            } else {
                var latlng = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);

                if (crs === 'KKJ') {
                    return _round(WGS84.toKKJ(latlng.lat, latlng.lng));
                } else {
                    return latlng;
                }
            }
        }

        function _updateGeoLocation(geoLocation, inputFields, crs) {
            var latlng = _toWGS84(inputFields, crs);

            if (_withinBounds(latlng)) {
                var etrs = _round(WGS84.toETRS(latlng.lat, latlng.lng));
                geoLocation.latitude = etrs.lat;
                geoLocation.longitude = etrs.lng;
                geoLocation.source = 'MANUAL';
                return angular.copy(geoLocation);
            }
            return null;
        }

        return {
            restrict: 'A',
            templateUrl: 'common/map/geolocation_input.html',
            scope: {
                geoLocation: '=rGeolocationInput'
            },
            controllerAs: '$ctrl',
            controller: function ($scope) {
                var $ctrl = this;
                $ctrl.coordinateSystems = ['ETRS-TM35FIN', 'WGS84', 'KKJ'];
                $ctrl.coordinateSystem = 'ETRS-TM35FIN';
                $ctrl.coordinatesInput = {lat: null, lng: null};

                // External model update
                $scope.$watch('geoLocation', function (geoLocation) {
                    $ctrl.coordinatesInput = _toInputFieldValues(geoLocation, $ctrl.coordinateSystem);
                });

                // Input CRS option change
                $ctrl.coordinateSystemChanged = function () {
                    $ctrl.coordinatesInput = _toInputFieldValues($scope.geoLocation, $ctrl.coordinateSystem);
                };

                $ctrl.setCoordinates = function () {
                    var inputFields = _parseInput($ctrl.coordinatesInput);

                    if (inputFields) {
                        var newLocation = _updateGeoLocation($scope.geoLocation || {}, inputFields, $ctrl.coordinateSystem);
                        if (newLocation) {
                            $scope.geoLocation = newLocation;
                        }
                    }
                };
            }
        };
    })

    .component('markerEditorMap', {
        templateUrl: 'common/map/marker-editor-map.html',
        transclude: true,
        bindings: {
            geolocation: '=',
            editable: '<'
        },
        controller: function (MapDefaults, MapState) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
                $ctrl.mapDefaults = MapDefaults.create();
                $ctrl.mapCenter = MapState.toGeoLocationOrDefault($ctrl.geolocation);
            };
        }
    });
