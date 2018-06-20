'use strict';

angular.module('app.clubmap.directives', [])
    .directive('mapEditorToolbar', function () {
        function controller() {
            var ctrl = this;

            ctrl.currentTool = 'move';

            ctrl.useTool = function (toolName) {
                ctrl.editorApi.useTool(toolName);
                ctrl.currentTool = toolName;
            };

            ctrl.isActiveTool = function (name) {
                return name && _.startsWith(ctrl.currentTool, name);
            };

            ctrl.toolStyle = function (name, activeStyle, inactiveStyle) {
                var isActive = ctrl.isActiveTool(name);
                return _.zipObject([activeStyle, inactiveStyle], [isActive, !isActive]);
            };

            ctrl.buttonStyle = function (name) {
                return ctrl.toolStyle(name, 'btn-primary', 'btn-default');
            };
        }

        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'club/map/edit_toolbar.html',
            bindToController: true,
            controllerAs: 'toolbarCtrl',
            scope: {
                editorApi: '=api',
                close: '&',
                save: '&'
            },
            controller: controller
        };
    })
    .service('PropertyIdentifierService', function () {
        var simplePattern = new RegExp("^\\d{14}$");
        var formattedPattern = new RegExp("^(\\d{1,3})-(\\d{1,3})-(\\d{1,4})-(\\d{1,4})$");

        this.parseFromString = function (value) {
            if (simplePattern.test(value)) {
                return value;

            } else {
                var parts = formattedPattern.exec(value);

                if (parts && parts.length > 4) {
                    var padThree = _.partial(_.padLeft, _, 3, '0');
                    var padFour = _.partial(_.padLeft, _, 4, '0');

                    return [
                        padThree(parts[1]),
                        padThree(parts[2]),
                        padFour(parts[3]),
                        padFour(parts[4])
                    ].join('');
                }
            }

            return undefined;
        };
    })
    .directive('propertyIdentifierList', function (PropertyIdentifierService) {
        return {
            restrict: 'A',
            priority: 100,
            require: 'ngModel',
            link: function (scope, element, attr, ctrl) {
                function _parseValues(propertyList) {
                    var list = _.chain(propertyList).map(PropertyIdentifierService.parseFromString).compact().value();
                    return list.length !== propertyList.length ? undefined : _.uniq(list);
                }

                var parse = function (viewValue) {
                    // If the viewValue is invalid (say required but empty) it will be `undefined`
                    if (angular.isUndefined(viewValue)) {
                        return;
                    }

                    var list = [];

                    if (viewValue) {
                        angular.forEach(viewValue.split(/[\s,;]+/), function (value) {
                            if (value) {
                                list.push(value.trim());
                            }
                        });
                    }

                    return _parseValues(list);
                };

                ctrl.$parsers.push(parse);
                ctrl.$formatters.push(function (value) {
                    if (angular.isArray(value)) {
                        return value.join('\n');
                    }

                    return undefined;
                });

                // Override the standard $isEmpty because an empty array means the input is empty.
                ctrl.$isEmpty = function (value) {
                    return !value || !value.length;
                };
            }
        };
    })

    .directive('rGeojsonEditor', function ($window, $timeout, $translate,
                                           UnsavedChangesConfirmationService,
                                           MHAreaService,
                                           GeoJsonEditorExcludedFeatures,
                                           GeoJsonEditorFeatures) {
        return {
            restrict: "E",
            scope: {
                api: '=editorApi',
                geojson: '=editorGeojson',
                callbacks: '=editorCallbacks'
            },
            replace: false,
            require: '^leaflet',
            link: function ($scope, element, attrs, leafletCtrl) {
                var featureList = $scope.geojson && _.isArray($scope.geojson.features) ? $scope.geojson.features : [];

                /*jshint latedef: nofunc */
                var excludedFeatures = GeoJsonEditorExcludedFeatures.create(featureList),
                    leafletFeatureGroup = createGeoJSONLayer(featureList),
                    featuresApi = GeoJsonEditorFeatures.create(leafletFeatureGroup, excludedFeatures, $scope.callbacks.onFeatureSelect),
                    selectedTool = 'move',
                    lassoTool = L.lasso(),
                    polyLassoTool = L.polyLasso(),
                    marqueeTool = L.marquee(),
                    legend = L.control.simpleLegend({
                        legend: {
                            'green': $translate.instant('club.area.map.legend.realEstate'),
                            'magenta': $translate.instant('club.area.map.legend.metsahallitus'),
                            'orange': $translate.instant('club.area.map.legend.changed'),
                            'blue': $translate.instant('club.area.map.legend.selected')
                        }
                    });

                function enableDirtyFlag() {
                    UnsavedChangesConfirmationService.setChanges(true);
                }

                function onMapClick(leafletEvent) {
                    if (selectedTool === 'palsta.add.single') {
                        $scope.callbacks.add(leafletEvent.latlng).then(addFeatures);
                    }
                }

                function onMarqueeEvent(event) {
                    var successCallback = (selectedTool === 'palsta.add.marquee') ? addFeatures : removeFeatures;
                    $scope.callbacks.marquee(event.bounds).then(successCallback);
                }

                function onLassoEvent(event) {
                    var clipPolygon = _.map(event.latLngs, L.GeoJSON.latLngToCoords);

                    if (selectedTool === 'exclude.lasso.add' || selectedTool === 'exclude.polyLasso.add') {
                        enableDirtyFlag();
                        if (event.mouseButton === 'right') {
                            excludedFeatures.removePolygon(clipPolygon);
                        } else {
                            excludedFeatures.addPolygon(clipPolygon, featuresApi.getSelectedLayers(), event.bounds);
                        }
                    } else if (selectedTool === 'exclude.lasso.remove' || selectedTool === 'exclude.polyLasso.remove') {
                        enableDirtyFlag();
                        excludedFeatures.removePolygon(clipPolygon);
                    }
                }

                leafletCtrl.getMap().then(function (map) {
                    leafletFeatureGroup.addTo(map);
                    excludedFeatures.addTo(map);
                    legend.addTo(map);

                    map.on('click', onMapClick);
                    map.on('marquee', onMarqueeEvent);
                    map.on('lasso', onLassoEvent);
                });

                function onFeatureMouseOver(feature) {
                    if (selectedTool === 'palsta.add.single' || selectedTool === 'move') {
                        $scope.$apply(function () {
                            featuresApi.setHighlight(feature);
                        });
                    }
                }

                function onFeatureMouseOut(feature) {
                    if (selectedTool === 'palsta.add.single' || selectedTool === 'move') {
                        $scope.$apply(function () {
                            featuresApi.removeHighlight(feature);
                        });
                    }
                }

                function onFeatureClick(feature, layer, event) {
                    L.DomEvent.stopPropagation(event);

                    if (selectedTool === 'palsta.add.single') {
                        featuresApi.removeLayer(feature, layer);

                    } else if (selectedTool === 'move' || _.startsWith(selectedTool, 'exclude.lasso')) {
                        featuresApi.selectFeature(feature);
                    }
                }

                function createGeoJSONLayer(featureList) {
                    return L.geoJSON(featureList, {
                        onEachFeature: function (feature, layer) {
                            layer.on({
                                'mouseover': _.partial(onFeatureMouseOver, feature),
                                'mouseout': _.partial(onFeatureMouseOut, feature),
                                'click': _.partial(onFeatureClick, feature, layer)
                            });
                        },
                        style: function (feature) {
                            var isChanged = _.get(feature, 'properties.changed', false);
                            var mhArea = _.startsWith(feature.id, 'mh-');
                            var fillColor = isChanged ? 'orange' :
                                mhArea ? 'magenta' : 'green';

                            return {
                                fillColor: fillColor,
                                color: "black",
                                weight: 1,
                                opacity: 1,
                                fillOpacity: isChanged ? 0.6 : 0.3
                            };
                        }
                    });
                }

                function addFeatures(geojson) {
                    excludedFeatures.bringToBack();
                    featuresApi.clearSelection();
                    featuresApi.addGeoJSON(geojson);
                    enableDirtyFlag();
                }

                function removeFeatures(geojson) {
                    featuresApi.removeGeoJSON(geojson);
                    enableDirtyFlag();
                }

                function addOrRemoveLayer(map, layer, toolName) {
                    if (selectedTool && selectedTool.indexOf(toolName) !== -1) {
                        layer.addTo(map);
                    } else if (map.hasLayer(layer)) {
                        map.removeLayer(layer);
                    }
                }

                function changeTool(toolName, map) {
                    featuresApi.clearSelection();
                    addOrRemoveLayer(map, marqueeTool, 'marquee');
                    addOrRemoveLayer(map, polyLassoTool, 'polyLasso');
                    addOrRemoveLayer(map, lassoTool, 'lasso');

                    if ('palsta.add.single' === toolName || 'move' === toolName) {
                        map.dragging.enable();
                    } else {
                        map.dragging.disable();
                    }
                }

                // Public API
                $scope.api = {
                    ready: true,
                    features: featuresApi,
                    addFeatures: addFeatures,
                    removeFeature: function (featureId) {
                        featuresApi.removeFeatureById(featureId);
                        enableDirtyFlag();
                    },
                    useTool: function (toolName) {
                        if (toolName && selectedTool !== toolName) {
                            selectedTool = toolName;

                            leafletCtrl.getMap().then(function (map) {
                                changeTool(toolName, map);
                            });
                        }
                    },
                    zoom: function (featureId) {
                        featuresApi.eachLayerById(featureId, function (feature, layer) {
                            leafletCtrl.getMap().then(function (map) {
                                var bounds = layer.getBounds();

                                if (bounds && bounds.getNorthEast()) {
                                    map.fitBounds(bounds, {maxZoom: 16});
                                }
                            });
                        });
                    }
                };
            }
        };
    });
