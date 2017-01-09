'use strict';

angular.module('app.map.directives', [])

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
                });
            }
        };
    })

    .directive('rGeolocationCenter',
        function ($timeout, $parse, leafletHelpers, leafletMapEvents, WGS84) {
            var defaultCenter = {lat: 63, lng: 25, zoom: 4};
            var defaultZoom = 12;
            var minimumZoom = 6;

            function _isValidGeoLocation(geoLocation) {
                return geoLocation &&
                    isFinite(geoLocation.latitude) &&
                    isFinite(geoLocation.longitude);
            }

            function _updateLeafletCenter(map, latlng) {
                if (!leafletHelpers.isSameCenterOnMap(latlng, map)) {
                    map.setView([latlng.lat, latlng.lng], latlng.zoom);

                    $timeout(function () {
                        // This fixes map size in dialogs
                        map.invalidateSize({reset: true});
                    });
                }
            }

            function _onGeolocationChanged(map, geoLocation) {
                if (_isValidGeoLocation(geoLocation)) {
                    var latlng = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude);

                    if (geoLocation.zoom) {
                        latlng.zoom = geoLocation.zoom;
                    } else if (map.getZoom() === defaultCenter.zoom) {
                        latlng.zoom = minimumZoom;
                    } else if (map.getZoom() < minimumZoom) {
                        latlng.zoom = defaultZoom;
                    }

                    _updateLeafletCenter(map, latlng);

                } else {
                    _updateLeafletCenter(map, defaultCenter);
                }
            }

            var mapHolder;

            return {
                restrict: 'A',
                scope: false,
                replace: false,
                controller: function () {
                    this.updateCenter = function (geoLocation) {
                        if (geoLocation && mapHolder) {
                            var center = angular.copy(geoLocation);
                            center.zoom = mapHolder.getZoom();
                            _onGeolocationChanged(mapHolder, center);
                        }
                    };
                },
                require: 'leaflet',
                link: function (scope, element, attrs, controller) {
                    var geoLocationValue = $parse(attrs.rGeolocationCenter);

                    controller.getMap().then(function (map) {
                        mapHolder = map;
                        scope.$watch(geoLocationValue, function (geoLocation) {
                            _onGeolocationChanged(map, geoLocation);
                        });
                    });
                }
            };
        }
    )

    .directive('rGeolocationMarker',
        function ($parse, leafletMarkersHelpers, WGS84, GIS) {
            function _isValidGeoLocation(geoLocation) {
                return geoLocation &&
                    isFinite(geoLocation.latitude) &&
                    isFinite(geoLocation.longitude);
            }

            var location = {};

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
                            location.latitude = lat;
                            location.longitude = lng;
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

                            if (_isValidGeoLocation(geoLocation)) {
                                _replaceMarker(map, geoLocation, editable);
                            } else {
                                _removeMarker(map);
                            }
                        });
                    });
                }
            };
        }
    )

    .directive('rGeolocationKeepMarkerCenter', function () {
        return {
            scope: false,
            replace: false,
            require: ['rGeolocationMarker', 'rGeolocationCenter'],

            link: function (scope, element, attrs, ctrls) {
                var markerController = ctrls[0];
                var centerController = ctrls[1];
                scope.rGeolocationKeepMarkerCenterValue = markerController.getLocation();
                scope.$watchCollection('rGeolocationKeepMarkerCenterValue', function (newVal) {
                    if (newVal) {
                        centerController.updateCenter(newVal);
                    }
                });
            }
        };
    })

    .directive('rGeolocationInput',
        function ($parse, WGS84) {
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
                templateUrl: 'common/geolocation_input.html',
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
        });
