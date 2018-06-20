/*
 Copyright (c) 2010-2016, Vladimir Agafonkin
 Copyright (c) 2010-2011, CloudMade
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this list of
 conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice, this list
 of conditions and the following disclaimer in the documentation and/or other materials
 provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 modified from source: https://github.com/Leaflet/Leaflet/blob/master/src/control/Control.Layers.js
 */
L.Control.GeoJsonLayerControl = L.Control.extend({
    options: {
        textToggleAll: '',
        collapsed: true,
        position: 'topleft'
    },

    initialize: function (geoJsonLayer, options) {
        L.setOptions(this, options);

        this._layers = [];
        this._handlingClick = false;
        this._geoJsonLayer = geoJsonLayer;
        this._lastZIndex = 0;

        var self = this;
        geoJsonLayer.eachLayer(function (layer) {
            self.addLayer(layer);
        });
    },

    onAdd: function (map) {
        this._initLayout();
        this._map = map;

        for (var i = 0; i < this._layers.length; i++) {
            var layer = this._layers[i].layer;
            layer.zIndex = this._lastZIndex++;
            layer.on('add remove', this._onLayerChange, this);
            layer.on('add', this._updateSvgOrder, this);
            layer.addTo(map);
        }

        this._update();
        this._addSelectAllItem(this._container.querySelector('.leaflet-control-geojson-list'));
        this._orderVectorLayers();

        return this._container;
    },

    onRemove: function () {
        for (var i = 0; i < this._layers.length; i++) {
            this._layers[i].layer.off('add remove', this._onLayerChange, this);
            this._layers[i].layer.off('add', this._updateSvgOrder, this);
        }
    },

    _updateSvgOrder : function (layer) {
        if (layer._path) {
            layer._path.zIndex = layer.zIndex;
        }
    },

    _orderVectorLayers: function() {
        var overlayPane = this._map.getPane('overlayPane');
        var svg = overlayPane.querySelector('svg');

        if (!svg) {
            return;
        }

        var root = svg.firstChild,
            child = root.firstChild,
            next;

        while (child) {
            next = child.nextSibling;

            if (!next) {
                break;
            }

            var nextOrder = next.zIndex;
            var childOrder = child.zIndex;

            if (nextOrder < childOrder) {
                root.insertBefore(next, child);
                if (next === root.firstChild) {
                    continue;
                }
                child = next.previousSibling;
                continue;
            }
            child = next;
        }
    },

    _toggleAll: function (e) {
        var map = this._map;

        this._geoJsonLayer.eachLayer(function (l) {
            if (e.target.checked) {
                l.addTo(map);
            } else {
                l.removeFrom(map);
            }
        });
    },

    _addSelectAllItem: function (container) {
        var holder = document.createElement('div'),
            label = document.createElement('label'),
            input = document.createElement('input');
        input.type = 'checkbox';
        input.className = 'leaflet-control-geojson-selector';
        input.checked = true;

        L.DomEvent.on(input, 'click', this._toggleAll, this);
        var name = document.createElement('span');
        name.innerHTML = ' ' + this.options.textToggleAll;
        name.style = 'margin-left: 4px';
        label.appendChild(holder);
        holder.appendChild(input);
        holder.appendChild(name);

        L.DomUtil.create('div', 'leaflet-control-geojson-separator', holder);
        container.insertBefore(label, container.firstChild);

        return label;
    },

    addLayer: function (layer) {
        var name = this.options.layerToLegendTitle(layer);
        var layerColor = layer.options.fillColor;
        var layerIcon = '<span style="display:inline-block;width:12px;height:12px;margin:0 0 -2px 2px;background-color:' + layerColor +' "></span>';
        var nameHtml = layerIcon + ' ' + name;

        this._addLayer(layer, nameHtml, true);
        return (this._map) ? this._update() : this;
    },

    // @method removeLayer(layer: Layer): this
    // Remove the given layer from the control.
    removeLayer: function (layer) {
        layer.off('add remove', this._onLayerChange, this);

        var obj = this._getLayer(L.stamp(layer));
        if (obj) {
            this._layers.splice(this._layers.indexOf(obj), 1);
        }
        return (this._map) ? this._update() : this;
    },

    // @method expand(): this
    // Expand the control container if collapsed.
    expand: function () {
        L.DomUtil.addClass(this._container, 'leaflet-control-geojson-expanded');
        this._form.style.height = null;
        var acceptableHeight = this._map.getSize().y - (this._container.offsetTop + 120);
        if (acceptableHeight < this._form.clientHeight) {
            L.DomUtil.addClass(this._form, 'leaflet-control-geojson-scrollbar');
            this._form.style.height = acceptableHeight + 'px';
        } else {
            L.DomUtil.removeClass(this._form, 'leaflet-control-geojson-scrollbar');
        }
        return this;
    },

    // @method collapse(): this
    // Collapse the control container if expanded.
    collapse: function () {
        L.DomUtil.removeClass(this._container, 'leaflet-control-geojson-expanded');
        return this;
    },

    toggle: function () {
        if (L.DomUtil.hasClass(this._container, 'leaflet-control-geojson-expanded')) {
            this.collapse();
        } else {
            this.expand();
        }
    },

    _initLayout: function () {
        var className = 'leaflet-control-geojson',
            container = this._container = L.DomUtil.create('div', className);

        // makes this work on IE touch devices by stopping it from firing a mouseout event when the touch is released
        container.setAttribute('aria-haspopup', true);

        L.DomEvent.disableClickPropagation(container);
        L.DomEvent.disableScrollPropagation(container);

        var form = this._form = L.DomUtil.create('form', className + '-list');

        if (this.options.collapsed) {
            var link = L.DomUtil.create('a', className + '-toggle', container);
            link.href = '#';
            link.title = 'Layers';

            var icon = L.DomUtil.create('span', 'glyphicon glyphicon-tasks', link);

            L.DomEvent
                .on(link, 'click', L.DomEvent.stop)
                .on(link, 'click', this.toggle, this);

            // work around for Firefox Android issue https://github.com/Leaflet/Leaflet/issues/2033
            L.DomEvent.on(form, 'click', function () {
                setTimeout(L.bind(this._onInputClick, this), 0);
            }, this);

            this._map.on('click', this.collapse, this);
        } else {
            this.expand();
        }

        this._overlaysList = L.DomUtil.create('div', className + '-overlays', form);

        container.appendChild(form);
    },

    _getLayer: function (id) {
        for (var i = 0; i < this._layers.length; i++) {

            if (this._layers[i] && L.stamp(this._layers[i].layer) === id) {
                return this._layers[i];
            }
        }
    },

    _addLayer: function (layer, name) {
        if (this._map) {
            layer.on('add remove', this._onLayerChange, this);
        }

        this._layers.push({
            layer: layer,
            name: name
        });

        this._layers.sort(L.bind(function (a, b) {
            if (_.isFunction(this.options.sortFunction)) {
                return this.options.sortFunction(a, b);
            }
            return a.name < b.name ? -1 : (b.name < a.name ? 1 : 0);
        }, this));
    },

    _update: function () {
        if (!this._container) { return this; }

        L.DomUtil.empty(this._overlaysList);

        var i, obj;

        for (i = 0; i < this._layers.length; i++) {
            obj = this._layers[i];
            this._addItem(obj);
        }

        return this;
    },

    _onLayerChange: function (e) {
        if (!this._handlingClick) {
            this._update();
        }
    },

    _addItem: function (obj) {
        var label = document.createElement('label'),
            checked = this._map.hasLayer(obj.layer),
            input = document.createElement('input');

        input.type = 'checkbox';
        input.className = 'leaflet-control-geojson-selector';
        input.defaultChecked = checked;
        input.layerId = L.stamp(obj.layer);

        L.DomEvent.on(input, 'click', this._onInputClick, this);

        var name = document.createElement('span');
        name.innerHTML = ' ' + obj.name;

        var holder = document.createElement('div');

        label.appendChild(holder);
        holder.appendChild(input);
        holder.appendChild(name);

        var container = this._overlaysList;
        container.appendChild(label);

        return label;
    },

    _onInputClick: function () {
        var inputs = this._form.getElementsByTagName('input'),
            input, layer, hasLayer;
        var addedLayers = [],
            removedLayers = [];

        this._handlingClick = true;

        for (var i = inputs.length - 1; i >= 0; i--) {
            input = inputs[i];

            if (!input.layerId) {
                continue;
            }

            layer = this._getLayer(input.layerId).layer;
            hasLayer = this._map.hasLayer(layer);

            if (input.checked && !hasLayer) {
                addedLayers.push(layer);

            } else if (!input.checked && hasLayer) {
                removedLayers.push(layer);
            }
        }

        for (i = 0; i < removedLayers.length; i++) {
            this._map.removeLayer(removedLayers[i]);
        }
        for (i = 0; i < addedLayers.length; i++) {
            this._map.addLayer(addedLayers[i]);
        }

        this._handlingClick = false;

        this._refocusOnMap();
        this._orderVectorLayers();
    }
});

L.control.geoJsonLayerControl = function (geoJsonLayer, options) {
    return new L.Control.GeoJsonLayerControl(geoJsonLayer, options);
};
