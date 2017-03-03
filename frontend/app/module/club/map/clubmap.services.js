'use strict';

angular.module('app.clubmap.services', [])

    .factory('MHAreaService', function (GIS) {
        var PREFIX_MOOSE = 'mh-hirvi-';

        function isMooseFeature(feature) {
            return _.isObject(feature) && _.startsWith(feature.id, PREFIX_MOOSE);
        }

        function Service(featureCollection, huntingClubArea) {
            this.featureCollection = featureCollection;
            this.areaList = [];
            this.selectedAreaList = [];
            this.huntingClubArea = huntingClubArea;
        }

        var proto = Service.prototype;

        proto.init = function () {
            var self = this;

            self.selectedAreaList = _(this.featureCollection.features)
                .filter(function (feature) {
                    return _.startsWith(feature.id, PREFIX_MOOSE);
                }).map(function (feature) {
                    return {
                        id: feature.id,
                        number: feature.properties.number,
                        name: feature.properties.name,
                        size: feature.properties.size,
                        year: feature.properties.year
                    };
                }).value();

            var year = self.huntingClubArea.metsahallitusYear;

            return GIS.listMetsahallitusHirviByYear(year).then(function (response) {
                self.areaList = response.data || [];
                return self;
            });
        };

        proto.getSelectedAreaList = function () {
            return this.selectedAreaList;
        };

        proto.addSelectedArea = function (a) {
            this.removeSelectedArea(a);
            var area = {
                id: PREFIX_MOOSE + a.gid,
                number: a.number,
                name: a.name,
                size: a.size,
                year: a.year
            };
            this.selectedAreaList.unshift(area);
            return area;
        };

        proto.removeSelectedArea = function (area) {
            _.remove(this.selectedAreaList, function (a) {
                return area.number === a.number;
            });
        };

        proto.isEmptySelection = function () {
            return !this.selectedAreaList;
        };

        proto.filterMooseAreaList = function (searchQuery) {
            if (!searchQuery || _.isEmpty(searchQuery)) {
                return this.areaList;
            }

            var searchRegex = new RegExp('.*' + searchQuery + '.*', 'i');

            return _.filter(this.areaList, function (value) {
                var number = _.get(value, 'number');
                var name = _.get(value, 'name');

                return (number && searchRegex.test(number)) || (name && searchRegex.test(name));
            });
        };

        return {
            create: function (featureCollection, huntingClubArea) {
                return new Service(featureCollection, huntingClubArea).init();
            },

            isMooseFeature: isMooseFeature
        };
    })

    .service('PolygonService', function ($http) {
        var self = this;

        this.geometryToPolygons = function (data) {
            // Keep exterior rings only
            return !data ? []
                : data.type === 'Polygon' ? [_.first(data.coordinates)]
                : data.type === 'MultiPolygon' ? _(data.coordinates).map(_.first).value()
                : data.type === 'GeometryCollection' ? _(data.geometries).map(self.geometryToPolygons).flatten().value()
                : [];
        };

        this.findWithBoundsOverlap = function (layers, bounds) {
            return _(layers)
                .filter(function (layer) {
                    var layerBounds = layer.getBounds();
                    return layerBounds.getNorthEast() && bounds.intersects(layerBounds);
                })
                .map(_.property('feature.geometry'))
                .filter()
                .map(self.geometryToPolygons).flatten().filter().value();
        };

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

    .service('GeoJsonEditorFeatures', function ($q, $timeout, GIS, MHAreaService) {
        function Service(leafletFeatureGroup, excludedFeatures, onFeatureSelect) {
            this.leafletFeatureGroup = leafletFeatureGroup;
            this.excludedFeatures = excludedFeatures;
            this.selectedLayers = [];
            this.selectedFeature = null;
            this.highlightedFeature = null;
            this.cachedPalstaFeatures = [];
            this.onFeatureSelect = onFeatureSelect || _.noop;

            this.allFeaturesChain = function () {
                return _(this.leafletFeatureGroup.getLayers()).map('feature').filter();
            };

            this.originalChangedIds = this.allFeaturesChain()
                .filter('properties.changed', true)
                .map('id')
                .value();

            this.setLayerStyles = function (layers, style) {
                _.forEach(layers, _.method('setStyle', style));
            };

            this.resetLayerStyles = function (layers) {
                _.forEach(layers, _.bind(this.leafletFeatureGroup.resetStyle, this.leafletFeatureGroup));
            };

            this.findLayersWithSamePropertyNumber = function (lookupFeature) {
                var propGet = _.property('properties.number');
                var lookupPropertyValue = propGet(lookupFeature);

                var allLayers = this.leafletFeatureGroup.getLayers();
                return _.filter(allLayers, function (layer) {
                    return propGet(layer.feature) === lookupPropertyValue;
                });
            };

            var self = this;
            this.filterPalstaFeatures = function () {
                var patternNumeric = new RegExp('^[0-9]+');

                return self.allFeaturesChain()
                    .filter(function (feature) {
                        return _.isString(feature.id) && patternNumeric.test(feature.id);
                    })
                    .sortByAll(['properties.number','id'])
                    .value();
            };
            this.cachedPalstaFeatures = this.filterPalstaFeatures();
        }

        var proto = Service.prototype;

        proto.eachLayerById = function (featureId, callback) {
            this.leafletFeatureGroup.eachLayer(function (layer) {
                if (featureId === _.get(layer, 'feature.id')) {
                    callback(layer.feature, layer);
                }
            });
        };

        proto.updateCachedFeatures = function () {
            var self = this;
            $timeout(function () {
                self.cachedPalstaFeatures = self.filterPalstaFeatures();
            });
        };

        proto.palstaFeatureList = function () {
            return this.cachedPalstaFeatures;
        };

        proto.addGeoJSON = function (geojson) {
            var currentIds = [];

            this.leafletFeatureGroup.eachLayer(function (layer) {
                var feature = layer.feature;
                if (feature.id) {
                    currentIds.push(feature.id);
                }
            });

            // Do not import duplicates
            _.remove(geojson.features, function (f) {
                return currentIds.indexOf(f.id) !== -1;
            });

            if (_.size(geojson.features) === 1) {
                // Show details for added feature
                this.selectFeature(geojson.features[0]);
            }

            this.leafletFeatureGroup.addData(geojson);
            this.updateCachedFeatures();
        };

        proto.removeGeoJSON = function (geojson) {
            var self = this;
            var featureIds = _.map(geojson.features, function (f) {
                return f.id;
            });

            this.leafletFeatureGroup.eachLayer(function (layer) {
                var feature = layer.feature;

                if (feature.id && _.includes(featureIds, feature.id)) {
                    self.leafletFeatureGroup.removeLayer(layer);
                }
            });
            this.updateCachedFeatures();
        };

        proto.removeLayer = function (feature, layer) {
            if (MHAreaService.isMooseFeature(feature)) {
                // Do not remove MH geometry by clicking on map
                return;
            }

            this.leafletFeatureGroup.removeLayer(layer);
            this.updateCachedFeatures();
        };

        proto.removeFeatureById = function (featureId) {
            var self = this;
            this.eachLayerById(featureId, function (feature, layer) {
                self.leafletFeatureGroup.removeLayer(layer);
            });

            this.updateCachedFeatures();
        };

        proto.removeMooseArea = function (area) {
            var self = this;
            this.leafletFeatureGroup.eachLayer(function (layer) {
                if (area.number === _.get(layer, 'feature.properties.number')) {
                    self.leafletFeatureGroup.removeLayer(layer);
                }
            });
            this.updateCachedFeatures();
        };

        proto.toGeoJSON = function () {
            var self = this;
            var geoJson = this.leafletFeatureGroup.toGeoJSON();

            _.each(geoJson.features, function (f) {
                var isChanged = _.get(f, 'properties.changed', false);

                // Skip geometries and extra properties
                delete f.properties;
                delete f.geometry;

                // Feature was initially marked as changed but was replaced with fresh copy
                if (!isChanged && self.originalChangedIds.indexOf(f.id) !== -1) {
                    f.properties = {
                        fixed: true
                    };
                }
            });

            var excludedGeoJson = this.excludedFeatures.toGeoJSON();
            geoJson.features.push(excludedGeoJson);

            return geoJson;
        };

        proto.updateChangedFeature = function (feature) {
            var self = this;
            var oldId = feature.id;
            var replacementId = feature.properties.new_palsta_id;

            if (!replacementId || !oldId) {
                return $q.reject();
            }

            return GIS.getPropertyPolygonById(replacementId).then(function (response) {
                var geoJson = response.data;

                if (geoJson.features.length) {
                    self.clearSelection();
                    self.removeFeatureById(oldId);
                    self.addGeoJSON(geoJson);
                    return geoJson;
                } else {
                    return $q.reject();
                }
            });
        };

        proto.getActiveFeature = function () {
            return this.highlightedFeature || this.selectedFeature;
        };

        // HIGHLIGHT logic

        proto.setHighlight = function (feature) {
            this.highlightedFeature = feature;
            var layers = this.findLayersWithSamePropertyNumber(feature);
            layers = _.difference(layers, this.selectedLayers);
            this.setLayerStyles(layers, {fillOpacity: 0.5});
        };

        proto.removeHighlight = function (feature) {
            this.highlightedFeature = null;
            var layers = this.findLayersWithSamePropertyNumber(feature);
            layers = _.difference(layers, this.selectedLayers);
            this.resetLayerStyles(layers);
        };

        // SELECTION logic

        proto.getSelectedLayers = function () {
            return _.isEmpty(this.selectedLayers) ? this.leafletFeatureGroup.getLayers() : this.selectedLayers;
        };

        proto.clearSelection = function () {
            this.resetLayerStyles(this.selectedLayers);
            this.selectedLayers = [];
            this.selectedFeature = null;
        };

        proto.selectLayers = function (layers) {
            this.setLayerStyles(layers, {fillColor: "blue", fillOpacity: 0.5});
            this.selectedLayers = layers;
        };

        proto.selectFeature = function (feature) {
            if (this.selectedFeature === feature) {
                this.clearSelection();
                return;
            }
            this.clearSelection();
            this.selectedFeature = feature;
            var layers = this.findLayersWithSamePropertyNumber(feature);
            this.selectLayers(layers);
            this.onFeatureSelect(feature);
        };

        this.create = function (leafletFeatureGroup, excludedFeatures, onFeatureHiglight) {
            return new Service(leafletFeatureGroup, excludedFeatures, onFeatureHiglight);
        };
    })

    .service('GeoJsonEditorExcludedFeatures', function (PolygonService) {
        var ExcludedFeatures = L.FeatureGroup.extend({
            initialize: function (featureCollection) {
                L.FeatureGroup.prototype.initialize.call(this);

                this.excludedFeature = {
                    id: 'excluded',
                    type: 'Feature',
                    properties: {},
                    geometry: null
                };

                var removed = _.remove(featureCollection, function (f) {
                    return f.id === 'excluded';
                });

                var existingFeature = _.first(removed);

                if (existingFeature && existingFeature.geometry) {
                    this.excludedFeature.geometry = existingFeature.geometry;
                }
            },

            onAdd: function (map) {
                L.FeatureGroup.prototype.onAdd.call(this, map);
                this._updateLeafletLayer();
            },

            toGeoJSON: function () {
                return this.excludedFeature;
            },

            addPolygon: function (clipPolygon, layers, bounds) {
                if (!clipPolygon || clipPolygon.length < 3) {
                    return;
                }

                var bboxPolygons = PolygonService.findWithBoundsOverlap(layers, bounds);
                var clippedPolygons = PolygonService.intersection(clipPolygon, bboxPolygons);

                if (_.isEmpty(clippedPolygons)) {
                    return;
                }

                var existingPolygons = PolygonService.geometryToPolygons(this.excludedFeature.geometry);
                var existingAndClipped = existingPolygons.concat(clippedPolygons);
                PolygonService.joinPolygons(existingAndClipped).then(this._updatePolygons.bind(this));
            },

            removePolygon: function (clipPolygon) {
                var existingPolygons = PolygonService.geometryToPolygons(this.excludedFeature.geometry);
                var difference = PolygonService.difference(clipPolygon, existingPolygons);

                this._updatePolygons(difference);
            },

            _updatePolygons: function (coords) {
                if (_.isEmpty(coords)) {
                    this.excludedFeature.geometry = null;

                } else if (_.isNumber(coords[0][0])) {
                    this.excludedFeature.geometry = {
                        type: 'Polygon',
                        coordinates: [coords[0]]
                    };

                } else {
                    this.excludedFeature.geometry = {
                        type: 'MultiPolygon',
                        coordinates: _.map(coords, function (p) {
                            return [p];
                        })
                    };
                }

                this._updateLeafletLayer();
            },

            _updateLeafletLayer: function () {
                L.FeatureGroup.prototype.clearLayers.call(this);

                if (!this.excludedFeature.geometry) {
                    return;
                }

                var layer = L.geoJSON(this.excludedFeature, {
                    style: function (feature) {
                        return {
                            color: 'black',
                            fillColor: 'red',
                            fillOpacity: 0.5,
                            weight: 1
                        };
                    }
                });

                L.FeatureGroup.prototype.addLayer.call(this, layer);
                L.FeatureGroup.prototype.bringToBack.call(this);
            }
        });

        this.create = function (featureCollection) {
            return new ExcludedFeatures(featureCollection);
        };
    });
