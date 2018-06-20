L.Control.SimpleLegend = L.Control.extend({
    options: {
        position: 'bottomleft',
        legend: {}
    },

    onAdd: function (map) {
        this._container = L.DomUtil.create('div', 'leaflet-simple-legend');

        var legend = this.options.legend;
        var labels = [];
        for (var key in legend) {
            if (legend.hasOwnProperty(key)) {
                var value = legend[key];
                labels.push('<li><span class="icon" style="background-color:' + key + '"></span>' + value + '</li>');
            }
        }
        this._container.innerHTML = '<ul>' + labels.join('') + '</ul>';
        L.DomEvent.disableClickPropagation(this._container);
        return this._container;
    }
});

L.control.simpleLegend = function (options) {
    return new L.Control.SimpleLegend(options);
};
