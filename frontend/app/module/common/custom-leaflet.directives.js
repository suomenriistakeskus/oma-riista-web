'use strict';

// Used https://raw.githubusercontent.com/tombatossals/angular-leaflet-directive/master/src/directives/markers.js as a basis
// and applied some performance optimizations:
// * bulk additions/removals of Leaflet markers to/from marker cluster
// * better handling of popup messages
// * "group" was made a mandatory attribute of markerData
// * AngularJS watches removed from individual markers
angular.module('app.custom-leaflet.directives', ['ui-leaflet'])
    .directive('markerCluster', function ($compile, $log, $rootScope, $q, $translate, leafletData, leafletHelpers, leafletMarkersHelpers, leafletMarkerEvents) {
        return {
            restrict: "E",
            scope: {
                markers: '=leafletMarkers'
            },
            replace: false,
            require: ['^leaflet', '^?layers'],

            link: function (scope, element, attrs, controllers) {
                var mapController = controllers[0],
                    leafletScope  = mapController.getLeafletScope(),
                    MarkerClusterPlugin = leafletHelpers.MarkerClusterPlugin,
                    isDefined = leafletHelpers.isDefined,
                    isString = leafletHelpers.isString,
                    createMarker = leafletMarkersHelpers.createMarker,
                    groups = {};

                var _clearMarkerGroups = function () {
                    if (isDefined(groups)) {
                        _.forEach(_.keys(groups), function (groupName) {
                            groups[groupName].clearLayers();
                            delete groups[groupName];
                        });
                    }
                };

                var _removeMarkerFromLayers = function (marker, map, layers) {
                    // There is no easy way to know if a marker is added to a layer, so we search for it
                    // if there are overlays
                    var overlayKeys = _.keys(layers.overlays);

                    for (var i=0; i < overlayKeys.length; i++) {
                        var key = overlayKeys[i];

                        if (layers.overlays[key] instanceof L.LayerGroup || layers.overlays[key] instanceof L.FeatureGroup) {
                            if (layers.overlays[key].hasLayer(marker)) {
                                layers.overlays[key].removeLayer(marker);
                                return;
                            }
                        }
                    }
                };

                var _addMarkerToGroup = function (marker, groupName, groupOptions, map) {
                    if (!isString(groupName)) {
                        $log.error('[Custom marker-cluster directive] The marker group you have specified is invalid.');
                        return;
                    }

                    if (!MarkerClusterPlugin.isLoaded()) {
                        $log.error("[Custom marker-cluster directive] The MarkerCluster plugin is not loaded.");
                        return;
                    }
                    if (!isDefined(groups[groupName])) {
                        groups[groupName] = new L.MarkerClusterGroup(groupOptions);
                        map.addLayer(groups[groupName]);
                    }
                };

                var _handleOpenPopup = function (marker, markerData) {
                    var popup = marker.getPopup(),
                        // The marker may have angular templates to compile.
                        compileMessage = isDefined(markerData.compileMessage) ? markerData.compileMessage : true,
                        // The marker may provide a scope returning function used to compile the message;
                        // $rootScope is used by default.
                        markerScope = angular.isFunction(markerData.getMessageScope) ? markerData.getMessageScope() : $rootScope;

                    if (isDefined(popup)) {
                        var updatePopup = function(popup) {
                            popup._updateLayout();
                            popup._updatePosition();
                        };

                        if (angular.isFunction(markerData.message)) {
                            markerData.message = markerData.message();
                        }
                        popup.setContent(markerData.message);

                        if (compileMessage) {
                            $compile(popup._contentNode)(markerScope);
                            // In case of an ng-include, we need to update the content after template load
                            if (isDefined(popup._contentNode) && popup._contentNode.innerHTML.indexOf("ngInclude") > -1) {
                                var unregister = markerScope.$on('$includeContentLoaded', function () {
                                    updatePopup(popup);
                                    unregister();
                                });
                            } else {
                                updatePopup(popup);
                            }
                        }
                    }
                    if (leafletHelpers.LabelPlugin.isLoaded() && isDefined(markerData.label) && isDefined(markerData.label.options) && markerData.label.options.noHide === true) {
                        if (compileMessage) {
                            $compile(marker.label._container)(markerScope);
                        }
                        marker.showLabel();
                    }
                };

                var _listenMarkerEvents = function (marker, markerData, leafletScope) {
                    if (markerData.message) {
                        marker.on("popupopen", function (/* event */) {
                            _handleOpenPopup(marker, markerData);
                        });

                        marker.on("popupclose", function (/* event */) {
                        });
                    } else if (markerData.clickHandler) {
                        marker.on("click", function (/* event */) {
                            markerData.clickHandler(markerData.id);
                        });
                    }

                    if (markerData.dragHandler) {
                        marker.on("dragend", function (event) {
                            markerData.dragHandler(markerData.id, event.target.getLatLng());
                        });
                    }
                };

                mapController.getMap().then(function (map) {
                    var leafletMarkers = {},
                        getLayers;

                    // If the layers attribute is used, we must wait until the layers are created
                    if (isDefined(controllers[1])) {
                        getLayers = controllers[1].getLayers;
                    } else {
                        getLayers = function () {
                            var deferred = $q.defer();
                            deferred.resolve();
                            return deferred.promise;
                        };
                    }

                    getLayers().then(function (layers) {
                        leafletData.setMarkers(leafletMarkers, attrs.id);
                        scope.$watch('markers', function (newMarkers) {

                            var groupedMarkers = {};
                            var groupName;

                            _clearMarkerGroups();
                            if (isDefined(layers) && isDefined(layers.overlays)) {
                                // Delete markers from layers
                                _.forEach(leafletMarkers, function (marker) {
                                    _removeMarkerFromLayers(marker, map, layers);
                                });
                            }
                            leafletMarkers = {};

                            // Add new markers
                            _.forOwn(newMarkers, function (markerData, newName) {
                                if (newName.search("-") !== -1) {
                                    $log.error('[Custom marker-cluster directive] The marker can\'t use a "-" on his key name: "' + newName + '".');
                                    return;
                                }

                                if (!isDefined(markerData) || !isDefined(markerData.group)) {
                                    $log.error('[Custom marker-cluster directive] marker group not specified for the marker ' + newName + '.');
                                    return;
                                }

                                var pathToMarker = leafletHelpers.getObjectDotPath([newName]);
                                var marker = createMarker(markerData);
                                groupName = markerData.group;

                                if (!isDefined(marker)) {
                                    $log.error('[Custom marker-cluster directive] Received invalid data on the marker ' + newName + '.');
                                    return;
                                }
                                leafletMarkers[newName] = marker;

                                // Bind popup
                                if (isDefined(markerData.message)) {
                                    marker.bindPopup(markerData.message, markerData.popupOptions);
                                }

                                if (isDefined(markerData.tooltipMessage)) {
                                    marker.bindTooltip(markerData.tooltipMessage, markerData.tooltipOptions);
                                }

                                var groupOptions = isDefined(markerData.groupOption) ? markerData.groupOption : null;
                                _addMarkerToGroup(marker, groupName, groupOptions, map);

                                // Show label if plugin defined
                                if (leafletHelpers.LabelPlugin.isLoaded() && isDefined(markerData.label) && isDefined(markerData.label.message)) {
                                    marker.bindLabel(markerData.label.message, markerData.label.options);
                                }

                                // Check if the marker should be added to a layer
                                if (isDefined(markerData) && isDefined(markerData.layer)) {
                                    if (!isString(markerData.layer)) {
                                        $log.error('[Custom marker-cluster directive] A layername must be a string');
                                        return;
                                    }
                                    if (!isDefined(layers)) {
                                        $log.error('[Custom marker-cluster directive] You must add layers to the directive if the markers are going to use this functionality.');
                                        return;
                                    }

                                    if (!isDefined(layers.overlays) || !isDefined(layers.overlays[markerData.layer])) {
                                        $log.error('[Custom marker-cluster directive] A marker can only be added to a layer of type "group"');
                                        return;
                                    }
                                    var layerGroup = layers.overlays[markerData.layer];
                                    if (!(layerGroup instanceof L.LayerGroup || layerGroup instanceof L.FeatureGroup)) {
                                        $log.error('[Custom marker-cluster directive] Adding a marker to an overlay needs a overlay of the type "group" or "featureGroup"');
                                        return;
                                    }

                                    // The marker goes to a correct layer group, so first of all we add it
                                    layerGroup.addLayer(marker);

                                    // The marker is automatically added to the map depending on the visibility
                                    // of the layer, so we only have to open the popup if the marker is in the map
                                    if (map.hasLayer(marker) && markerData.focus === true) {
                                       _handleOpenPopup(marker, markerData);
                                    }
                                }

                                if (!isDefined(groupedMarkers[groupName])) {
                                    groupedMarkers[groupName] = [];
                                }
                                groupedMarkers[groupName].push(marker);

                                _listenMarkerEvents(marker, markerData, scope);
                                leafletMarkerEvents.bindEvents(attrs.id, marker, pathToMarker, markerData, leafletScope, null);
                            });

                            _.forEach(_.keys(groupedMarkers), function (groupName) {
                                groups[groupName].addLayers(groupedMarkers[groupName]);
                            });

                        }, false);
                    });
                });
            }
        };
    });
