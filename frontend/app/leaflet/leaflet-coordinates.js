L.Control.Coordinates = L.Control.extend({
    options: {
        position: 'bottomleft'
    },

    onAdd: function (map) {
        this._crs = new L.Proj.CRS('EPSG:3067');
        this._container = L.DomUtil.create('div', 'leaflet-control-coordinates');
        L.DomEvent.disableClickPropagation(this._container);
        map.on('mousemove', _.throttle(this._onMouseMove, 100), this);
        this._container.innerHTML = '';
        return this._container;
    },

    onRemove: function (map) {
        map.off('mousemove', this._onMouseMove)
    },

    _onMouseMove: function (e) {
        var projectedPoint = this._crs.projection.project(e.latlng);

        if (_.isFinite(projectedPoint.x) && _.isFinite(projectedPoint.y)) {
            var lat = Math.round(projectedPoint.x);
            var lng = Math.round(projectedPoint.y);

            // P 6826358 I 314766 (ETRS-TM35FIN)
            this._container.innerHTML = 'P ' + lng + ' I ' + lat + ' (ETRS-TM35FIN)';
        }
    }
});

L.Map.mergeOptions({
    positionControl: true
});

L.Map.addInitHook(function () {
    if (this.options.positionControl) {
        this.positionControl = new L.Control.Coordinates();
        this.addControl(this.positionControl);
    }
});

L.control.coordinates = function (options) {
    return new L.Control.Coordinates(options);
};
