L.Marquee = L.Layer.extend({
    options: {
    },

    initialize: function(options) {
        L.Util.setOptions(this, options);
    },

    addTo: function(map) {
        map.addLayer(this);
        return this;
    },

    onAdd: function(map) {
        this.map = map;
        this.overlayPane = map.getPanes().overlayPane;
        this.mouseTarget = map;
        this._marquee = L.DomUtil.create("div", "leaflet-marquee", this.map._controlContainer);
        this._setupEventHandlers();

        map.dragging.disable();
    },

    onRemove: function(map) {
        this._removeEventHandlers();

        map.dragging.enable();
    },

    _render: function() {
        var pos = this.map.containerPointToLayerPoint(this._dragStart);
        L.DomUtil.setPosition(this._marquee, pos);

        this._marquee.style.width = this._width + "px";
        this._marquee.style.height = this._height + "px";
    },

    _setupEventHandlers: function() {
        L.DomEvent.on(this.mouseTarget, "mousedown", this._onMouseDown, this);
        this.map.on('viewreset', this._reset, this);
    },

    _removeEventHandlers: function() {
        L.DomEvent.off(this.mouseTarget, "mousedown", this._onMouseDown, this);
        this.map.off('viewreset', this._reset, this);
    },

    _reset: function () {
        if (this._dragStartLatLng) {
            this._dragStart = this.map.latLngToContainerPoint(this._dragStartLatLng);
        }

        if (this._dragStart) {
            this._render();
        }
    },

    _onMouseDown: function(event) {
        L.DomEvent.stopPropagation(event);

        this.overlayPane.appendChild(this._marquee);

        this._dragStart = event.containerPoint;
        this._dragStartLatLng = event.latlng;
        this._width = this._height = 1;
        this._render();

        L.DomEvent.off(this.mouseTarget, "mousedown", this._onMouseDown, this);
        L.DomEvent.on(this.mouseTarget, "mousemove", this._onMouseMove, this);
        L.DomEvent.on(this.mouseTarget, "mouseup", this._onMouseUp, this);
    },

    _onMouseMove: function(moveEvent) {
        this._width = (moveEvent.containerPoint.x - this._dragStart.x);
        this._height = (moveEvent.containerPoint.y - this._dragStart.y);
        this._width = Math.max(0, this._width);
        this._height = Math.max(0, this._height);

        var size = this.map.getSize();
        this._width = Math.min(size.x-this._dragStart.x, this._width);
        this._height = Math.min(size.y-this._dragStart.y, this._height);

        this._render();
    },

    _onMouseUp: function(upEvent) {
        L.DomEvent.stopPropagation(upEvent);

        this.overlayPane.removeChild(this._marquee);

        L.DomEvent.off(this.mouseTarget, "mouseup", this._onMouseUp, this);
        L.DomEvent.off(this.mouseTarget, "mousemove", this._onMouseMove, this);
        L.DomEvent.on(this.mouseTarget, "mousedown", this._onMouseDown, this);

        var bounds = L.latLngBounds([
            this.map.containerPointToLatLng(this._dragStart),
            this.map.containerPointToLatLng(upEvent.containerPoint)
        ]);

        this.map.fire("marquee", {
            bounds: bounds
        });
    }
});

L.marquee = function(options) {
    return new L.Marquee(options);
};
