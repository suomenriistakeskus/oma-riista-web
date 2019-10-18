'use strict';

angular.module('app.mapeditor.core', [])

    .component('leafletMapEditor', {
        bindings: {
            editor: '<'
        },
        require: {
            leafletCtrl: '^leaflet'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.leafletCtrl.getMap().then(function (map) {
                    $ctrl.editor.setup(map);
                });
            };
        }
    })

    .service('GeoJsonEditor', function ($translate, GIS,
                                        GeoJsonEditorSelection,
                                        GeoJsonEditorMML,
                                        GeoJsonEditorMetsahallitus,
                                        GeoJsonEditorDraw,
                                        GeoJsonEditorExcludedZone,
                                        UnsavedChangesConfirmationService) {
        function enableDirtyFlag() {
            UnsavedChangesConfirmationService.setChanges(true);
        }

        function focusLayer(map, layer) {
            var bounds = layer.getBounds();

            if (bounds && bounds.getNorthEast()) {
                map.fitBounds(bounds, {maxZoom: 16});
            }
        }

        function findLayersByProp(featureGroup, propName, propValue) {
            var propGetter = _.property(propName);
            var result = [];

            featureGroup.eachLayer(function (layer) {
                if (propValue === propGetter(layer)) {
                    result.push(layer);
                }
            });

            return result;
        }

        function findLayersById(featureGroup, featureId) {
            return findLayersByProp(featureGroup, 'feature.id', featureId);
        }

        function findLayersByNumber(featureGroup, featureNumber) {
            return findLayersByProp(featureGroup, 'feature.properties.number', featureNumber);
        }

        function createGeoJSONLayer(featureList, eventHandlers) {
            return L.geoJSON(featureList, {
                onEachFeature: function (feature, layer) {
                    layer.on({
                        'mouseover': _.partial(eventHandlers.featureMouseOver, feature),
                        'mouseout': _.partial(eventHandlers.featureMouseOut, feature),
                        'click': _.partial(eventHandlers.featureClick, feature, layer)
                    });
                },
                style: function (feature) {
                    var fillColor;

                    if (GeoJsonEditorMML.isChangedFeature(feature)) {
                        fillColor = 'orange';
                    } else if (GeoJsonEditorMetsahallitus.isMooseFeature(feature)) {
                        fillColor = 'magenta';
                    } else {
                        fillColor = 'green';
                    }

                    return {
                        fillColor: fillColor,
                        color: "black",
                        weight: 1,
                        opacity: 1,
                        fillOpacity: GeoJsonEditorMML.isChangedFeature(feature) ? 0.6 : 0.3
                    };
                }
            });
        }

        function Editor(featureList, scopeApply) {
            this.legend = L.control.simpleLegend({
                legend: {
                    'green': $translate.instant('global.map.editor.legend.realEstate'),
                    'magenta': $translate.instant('global.map.editor.legend.metsahallitus'),
                    'orange': $translate.instant('global.map.editor.legend.changed'),
                    'blue': $translate.instant('global.map.editor.legend.selected')
                }
            });

            // Tools
            this.selectedTool = 'move';
            this.lassoTool = L.lasso();
            this.polyLassoTool = L.polyLasso();
            this.marqueeTool = L.marquee();
            this.addOrRemoveTool = function (layer, toolName) {
                if (this.selectedTool && this.selectedTool.indexOf(toolName) !== -1) {
                    layer.addTo(this._map);
                } else if (this._map.hasLayer(layer)) {
                    this._map.removeLayer(layer);
                }
            };

            var self = this;

            this.eventHandlers = {
                mapClick: function (leafletEvent) {
                    if (self.selectedTool === 'palsta.add.single') {
                        GIS.getPropertyByCoordinates(leafletEvent.latlng).then(function (response) {
                            self.addGeoJSONFeatures(response.data);
                        });
                    }
                },
                marqueeEvent: function (event) {
                    GIS.getPropertyByBounds(event.bounds).then(function (response) {
                        var successCallback = (self.selectedTool === 'palsta.add.marquee')
                            ? self.addGeoJSONFeatures
                            : self.removeGeoJSONFeatures;
                        successCallback = _.bind(successCallback, self);

                        successCallback(response.data);
                    });
                },
                lassoEvent: function (event) {
                    var clipPolygon = L.GeoJSON.latLngsToCoords(event.latLngs);

                    if (!clipPolygon || clipPolygon.length < 3) {
                        return;
                    }

                    if (self.selectedTool === 'exclude.lasso.add' ||
                        self.selectedTool === 'exclude.polyLasso.add') {
                        enableDirtyFlag();

                        var selectedLayers = self.selection.getSelectedLayers();
                        var sourceLayers;

                        if (_.isEmpty(selectedLayers)) {
                            sourceLayers = self.leafletFeatureGroup.getLayers();
                            Array.prototype.push.apply(sourceLayers, self.draw.getLayers());

                        } else {
                            sourceLayers = selectedLayers;
                        }

                        var bboxPolygons = GeoJsonEditorExcludedZone.getLayerPolygonsInsideBounds(sourceLayers, event.bounds);

                        self.excluded.addPolygon(clipPolygon, bboxPolygons);
                    }

                    if (self.selectedTool === 'exclude.lasso.remove' ||
                        self.selectedTool === 'exclude.polyLasso.remove') {
                        enableDirtyFlag();

                        self.excluded.removePolygon(clipPolygon);
                    }
                },
                featureMouseOver: function (feature) {
                    if (self.selectedTool === 'palsta.add.single' || self.selectedTool === 'move') {
                        scopeApply(function () {
                            self.selection.setHighlight(feature);
                        });
                    }
                },
                featureMouseOut: function (feature) {
                    if (self.selectedTool === 'palsta.add.single' || self.selectedTool === 'move') {
                        scopeApply(function () {
                            self.selection.removeHighlight(feature);
                        });
                    }
                },
                featureClick: function (feature, layer, event) {
                    L.DomEvent.stopPropagation(event);

                    if (self.selectedTool === 'move') {
                        self.selection.selectFeature(feature);

                    } else if (self.selectedTool === 'palsta.add.single') {
                        if (GeoJsonEditorMML.isMmlFeature(feature)) {
                            self.mml.removeLayer(feature, layer);
                        }
                    }
                }
            };

            // Must be remove excluded feature from list first
            var excludedFeature = GeoJsonEditorExcludedZone.findAndRemoveExcludedFeature(featureList);
            var otherFeatures = GeoJsonEditorDraw.findAndRemoveOtherFeatures(featureList);

            // Sub-modules
            this.leafletFeatureGroup = createGeoJSONLayer(featureList, this.eventHandlers);
            this.selection = GeoJsonEditorSelection.create(this.leafletFeatureGroup);
            this.mml = GeoJsonEditorMML.create(this.leafletFeatureGroup);
            this.excluded = GeoJsonEditorExcludedZone.create(excludedFeature);
            this.draw = GeoJsonEditorDraw.create(otherFeatures);
        }

        var proto = Editor.prototype;

        proto.setup = function (map) {
            this._map = map;
            this.leafletFeatureGroup.addTo(map);
            this.draw.addTo(map);
            this.excluded.addTo(map);
            this.legend.addTo(map);

            map.on('click', this.eventHandlers.mapClick);
            map.on('marquee', this.eventHandlers.marqueeEvent);
            map.on('lasso', this.eventHandlers.lassoEvent);
        };

        proto.toGeoJSON = function () {
            var mmlGeoJson = this.mml.toGeoJSON();
            var excludedGeoJson = this.excluded.toGeoJSON();
            var otherGeoJson = this.draw.toGeoJSON();

            mmlGeoJson.features.push(excludedGeoJson);

            Array.prototype.push.apply(mmlGeoJson.features, otherGeoJson.features);

            return mmlGeoJson;
        };

        proto.addGeoJSONFeatures = function (geojson) {
            this.excluded.bringToBack();
            this.selection.clearSelection();
            this.mml.addGeoJSON(geojson);
            enableDirtyFlag();
        };

        proto.removeGeoJSONFeatures = function (geojson) {
            this.mml.removeGeoJSON(geojson);
            enableDirtyFlag();
        };

        proto.removeFeatureById = function (featureId) {
            var mmlLayers = findLayersById(this.leafletFeatureGroup, featureId);
            var drawLayers = findLayersById(this.draw, featureId);

            for (var i = 0; i < mmlLayers.length; i++) {
                this.leafletFeatureGroup.removeLayer(mmlLayers[i]);
            }

            for (var j = 0; j < drawLayers.length; j++) {
                this.draw.removeLayer(drawLayers[j]);
            }

            this.mml.updateCachedFeatures();
            this.draw.updateCachedFeatures();

            enableDirtyFlag();
        };

        proto.removeMooseArea = function (area) {
            var layers = findLayersByNumber(this.leafletFeatureGroup, area.number);

            for (var i = 0; i < layers.length; i++) {
                this.leafletFeatureGroup.removeLayer(layers[i]);
            }

            enableDirtyFlag();
        };

        proto.zoom = function (featureId) {
            var mmlLayers = findLayersById(this.leafletFeatureGroup, featureId);
            var drawLayers = findLayersById(this.draw, featureId);

            if (mmlLayers.length > 0) {
                focusLayer(this._map, mmlLayers[0]);
            }

            if (drawLayers.length > 0) {
                focusLayer(this._map, drawLayers[0]);
            }
        };

        proto.useTool = function (toolName) {
            if (this.selectedTool === toolName) {
                return;
            }
            this.selectedTool = toolName;
            this.selection.clearSelection();

            this.addOrRemoveTool(this.marqueeTool, 'marquee');
            this.addOrRemoveTool(this.polyLassoTool, 'polyLasso');
            this.addOrRemoveTool(this.lassoTool, 'lasso');

            if (toolName === 'draw.create') {
                this.draw.enableCreate();
            } else if (toolName === 'draw.edit') {
                this.draw.enableEdit();
            } else {
                this.draw.disableAll();
            }

            if (toolName === 'move' ||
                toolName === 'palsta.add.single') {
                this._map.dragging.enable();
            } else {
                this._map.dragging.disable();
            }
        };

        this.create = function (geoJSON, scopeApply) {
            var featureList = geoJSON && _.isArray(geoJSON.features) ? geoJSON.features : [];

            return new Editor(featureList, scopeApply);
        };
    });
