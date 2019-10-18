'use strict';

angular.module('app.mapeditor.selection', [])
    .service('GeoJsonEditorSelection', function () {
        function Service(leafletFeatureGroup) {
            this.leafletFeatureGroup = leafletFeatureGroup;
            this.selectedLayers = [];
            this.selectedFeature = null;
            this.highlightedFeature = null;

            // Private methods

            this.findLayersWithSamePropertyNumber = function (lookupFeature) {
                var propGet = _.property('properties.number');
                var lookupPropertyValue = propGet(lookupFeature);

                var allLayers = this.leafletFeatureGroup.getLayers();
                return _.filter(allLayers, function (layer) {
                    return propGet(layer.feature) === lookupPropertyValue;
                });
            };

            this.setLayerStyles = function (layers, style) {
                _.forEach(layers, _.method('setStyle', style));
            };

            this.resetLayerStyles = function (layers) {
                _.forEach(layers, _.bind(this.leafletFeatureGroup.resetStyle, this.leafletFeatureGroup));
            };
        }

        var proto = Service.prototype;

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
            return this.selectedLayers;
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
        };

        this.create = function (leafletFeatureGroup) {
            return new Service(leafletFeatureGroup);
        };
    });
