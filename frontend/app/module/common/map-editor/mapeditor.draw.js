'use strict';

angular.module('app.mapeditor.draw', [])
    .factory('GeoJsonEditorDraw', function ($timeout) {
        var OTHER_FEATURE_ID_PREFIX = 'other-';

        function isDrawFeature(feature) {
            return _.isObject(feature) && _.isString(feature.id) && _.startsWith(feature.id, OTHER_FEATURE_ID_PREFIX);
        }

        var drawnLayerStyle = {
            fillColor: 'green',
            color: 'black',
            weight: 1,
            opacity: 1,
            fillOpacity: 0.3
        };

        function decorateDrawnLayer(layer) {
            var ts = +(new Date());
            var latLngs = layer.editing.latlngs[0][0];

            layer.feature = {
                type: 'Feature',
                id: 'other-' + ts,
                properties: {
                    size: L.GeometryUtil.geodesicArea(latLngs)
                },
                geometry: null
            };

            layer.setStyle(drawnLayerStyle);

            return layer;
        }

        var Service = L.GeoJSON.extend({
            initialize: function (geojson) {
                L.GeoJSON.prototype.initialize.call(this, geojson, {
                    style: drawnLayerStyle
                });

                this.cachedFeatures = _.map(this.getLayers(), 'feature');
            },

            updateCachedFeatures: function () {
                var self = this;

                $timeout(function () {
                    self.cachedFeatures = _.map(self.getLayers(), 'feature');
                });
            },

            getFeatureList: function () {
                return this.cachedFeatures;
            },

            isFeatureListEmpty: function () {
                return _.isEmpty(this.cachedFeatures);
            },

            onAdd: function (map) {
                L.GeoJSON.prototype.onAdd.call(this, map);

                this._map = map;

                var self = this;

                map.on(L.Draw.Event.CREATED, function (event) {
                    self.addLayer(decorateDrawnLayer(event.layer));
                    self.updateCachedFeatures();
                });

                map.on(L.Draw.Event.EDITED, function (event) {
                    self.updateCachedFeatures();
                });

                map.on(L.Draw.Event.DELETED, function (event) {
                    self.updateCachedFeatures();
                });

                map.on('unload', function () {
                    self.disableAll();
                });
            },

            enableCreate: function () {
                this.disableAll();

                var guideLayers = [];
                guideLayers = guideLayers.concat(this.getLayers());

                this.polygonDrawControl = new L.Draw.Polygon(this._map, {
                    allowIntersection: false,
                    repeatMode: true,
                    showArea: true,
                    shapeOptions: {
                        color: 'purple'
                    },
                    icon: new L.DivIcon({
                        iconSize: new L.Point(15, 15),
                        className: 'leaflet-div-icon leaflet-editing-icon'
                    }),
                    snapDistance: 20,
                    snapVertices: true,
                    guideLayers: guideLayers,
                    guidelineDistance: 20,
                    maxGuideLineLength: 4000
                });

                this.polygonDrawControl.enable();
                this.polygonDrawControl._snap_on_enabled();
            },

            enableEdit: function () {
                this.disableAll();

                var guideLayers = [];
                guideLayers = guideLayers.concat(this.getLayers());

                this.editControl = new L.EditToolbar.SnapEdit(this._map, {
                    featureGroup: this,
                    snapOptions: {
                        snapDistance: 20,
                        snapVertices: true,
                        guideLayers: guideLayers
                    },
                    poly: {
                        allowIntersection: false,
                        icon: new L.DivIcon({
                            iconSize: new L.Point(15, 15),
                            className: 'leaflet-div-icon leaflet-editing-icon'
                        })
                    },
                    selectedPathOptions: {
                        maintainColor: true
                    }
                });

                this.editControl.enable();
            },

            disableAll: function () {
                if (this.polygonDrawControl) {
                    this.polygonDrawControl.disable();
                    this.polygonDrawControl._snap_on_disabled();
                    this.polygonDrawControl = null;
                }

                if (this.editControl) {
                    this.editControl.disable();
                    this.editControl = null;
                }
            }
        });

        return {
            findAndRemoveOtherFeatures: function (featureList) {
                return _.remove(featureList, function (f) {
                    return isDrawFeature(f);
                });
            },
            create: function (featureList) {
                return new Service(featureList);
            }
        };
    });
