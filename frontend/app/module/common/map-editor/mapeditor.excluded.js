'use strict';

angular.module('app.mapeditor.excluded', [])
    .factory('GeoJsonEditorExcludedZone', function (PolygonService) {
        var EXCLUDED_FEATURE_ID = 'excluded';

        function findAndRemoveExcludedFeature(featureList) {
            var removed = _.remove(featureList, function (f) {
                return f.id === EXCLUDED_FEATURE_ID;
            });

            return _.head(removed);
        }

        function getLayerPolygonsInsideBounds(layers, bounds) {
            return _(layers)
                .filter(function (layer) {
                    var layerBounds = layer.getBounds();
                    return layerBounds.getNorthEast() && bounds.intersects(layerBounds);
                })
                .map(function (layer) {
                    var geoJSON = layer.toGeoJSON();
                    return PolygonService.geometryToPolygons(geoJSON.geometry);
                })
                .flatten()
                .filter()
                .value();
        }

        function coordToGeometry(coords) {
            if (_.isEmpty(coords)) {
                return null;

            } else if (_.isNumber(coords[0][0])) {
                return {
                    type: 'Polygon',
                    coordinates: [coords[0]]
                };

            } else {
                return {
                    type: 'MultiPolygon',
                    coordinates: _.map(coords, function (p) {
                        return [p];
                    })
                };
            }
        }

        function createFeatureLayer(feature) {
            return L.geoJSON(feature, {
                style: function () {
                    return {
                        color: 'black',
                        fillColor: 'red',
                        fillOpacity: 0.5,
                        weight: 1
                    };
                }
            });
        }

        var ExcludedFeatureGroup = L.FeatureGroup.extend({
            initialize: function (existingFeature) {
                L.FeatureGroup.prototype.initialize.call(this);

                this.excludedFeature = {
                    type: 'Feature',
                    id: EXCLUDED_FEATURE_ID,
                    geometry: existingFeature ? existingFeature.geometry : null
                };
            },

            onAdd: function (map) {
                L.FeatureGroup.prototype.onAdd.call(this, map);
                this._updateLeafletLayer();
            },

            toGeoJSON: function () {
                return this.excludedFeature;
            },

            addPolygon: function (clipPolygon, bboxPolygons) {
                var clippedPolygons = PolygonService.intersection(clipPolygon, bboxPolygons);

                if (_.isEmpty(clippedPolygons)) {
                    return;
                }

                var existingPolygons = PolygonService.geometryToPolygons(this.excludedFeature.geometry);
                var allPolygons = existingPolygons.concat(clippedPolygons);

                var self = this;

                PolygonService.joinPolygons(allPolygons).then(function (union) {
                    self.excludedFeature.geometry = coordToGeometry(union);
                    self._updateLeafletLayer();
                });
            },

            removePolygon: function (clipPolygon) {
                var existingPolygons = PolygonService.geometryToPolygons(this.excludedFeature.geometry);
                var difference = PolygonService.difference(clipPolygon, existingPolygons);

                this.excludedFeature.geometry = coordToGeometry(difference);
                this._updateLeafletLayer();
            },

            _updateLeafletLayer: function () {
                L.FeatureGroup.prototype.clearLayers.call(this);

                if (!this.excludedFeature.geometry) {
                    return;
                }

                L.FeatureGroup.prototype.addLayer.call(this, createFeatureLayer(this.excludedFeature));
                L.FeatureGroup.prototype.bringToBack.call(this);
            }
        });

        return {
            findAndRemoveExcludedFeature: findAndRemoveExcludedFeature,
            getLayerPolygonsInsideBounds: getLayerPolygonsInsideBounds,
            create: function (excludedFeature) {
                return new ExcludedFeatureGroup(excludedFeature);
            }
        };
    });
