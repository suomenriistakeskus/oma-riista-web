(function () {
    "use strict";

    if (typeof L === 'undefined') {
        throw 'L.PolyLasso: Leaflet.js is required';
    }

    L.PolyLasso = L.FeatureGroup.extend({
        initialize: function (options) {
            this.options = _.defaults({}, this.defaultOptions(), options || {});
            this.creating = false;
            this.eventHandlers = [];
            this.lastSegment = null;
        },

        defaultOptions: function () {
            return {
                strokeColor: 'blue',
                activeStrokeColor: 'red',
                strokeWidth: 2,
                endTolerancePx: 20,
                width: '100%',
                height: '100%'
            };
        },

        getOption: function getOption(property) {
            return this.options[property] || this.defaultOptions()[property];
        },

        onAdd: function (map) {
            this.map = map;

            map.dragging.disable();
            this.attachEvents(map);
        },

        onRemove: function onRemove(map) {
            this.map.off('mousedown', this.eventHandlers.mouseDown);
            this.map.off('mouseup', L.DomEvent.stopPropagation);
            this.map.off('contextmenu', L.DomEvent.stopPropagation);
            this.map.off('mousemove', this.eventHandlers.mouseMove);
            this.map.getContainer().removeEventListener('mouseleave', this.eventHandlers.mouseLeave);

            map.dragging.enable();
        },

        clearAll: function clearAll() {
            if (this.polyLine) {
                this.map.removeLayer(this.polyLine);
                this.polyLine = null;
            }

            if (this.lastSegment) {
                this.map.removeLayer(this.lastSegment);
                this.lastSegment = null;
            }
        },

        createPath: function () {
            this.map.fire("lasso", {
                latLngs: this.polyLine.getLatLngs(),
                bounds: this.polyLine.getBounds()
            });

            this.clearAll();
        },

        events: {
            mouseDown: function (event) {
                event = event.originalEvent;

                var point = this.map.mouseEventToContainerPoint(event);
                this.fromLatLng = this.map.containerPointToLatLng(point);
                var mouseButton = (event.which || event.button) < 2 ? 'left' : 'right';

                if (!this.creating) {
                    this.creating = true;
                    this.firstPointLatLng = this.map.containerPointToLatLng(point);

                    this.polyLine = L.polyline([this.fromLatLng], {
                        'stroke-width': this.getOption('strokeWidth'),
                        color: this.getOption('strokeColor')
                    }).addTo(this.map);

                    this.lastSegment = L.polyline([this.fromLatLng], {
                        'stroke-width': this.getOption('strokeWidth'),
                        color: this.getOption('activeStrokeColor')
                    }).addTo(this.map);

                } else if (mouseButton === 'right') {
                    this.clearAll();
                    this.creating = false;

                } else {
                    this.polyLine.addLatLng(this.fromLatLng);
                    this.lastSegment.setLatLngs([this.fromLatLng]);

                    var distanceTo = this.map.latLngToContainerPoint(this.firstPointLatLng).distanceTo(point);
                    var tolerance = this.getOption('endTolerancePx');
                    var pointCount = this.polyLine.getLatLngs().length;

                    if (distanceTo < tolerance && pointCount > 3) {
                        this.creating = false;
                        this.createPath();
                    }
                }
            },
            mouseMove: function (event) {
                event = event.originalEvent;

                if (this.creating) {
                    var point = this.map.mouseEventToContainerPoint(event);
                    var toLatLng = this.map.containerPointToLatLng(point);
                    this.lastSegment.setLatLngs([this.fromLatLng, toLatLng]);

                    var tolerance = this.getOption('endTolerancePx');
                    var clickWillEndPath = this.map.latLngToContainerPoint(this.firstPointLatLng).distanceTo(point) < tolerance;
                    var strokeColor = clickWillEndPath ? this.getOption('activeStrokeColor') : this.getOption('strokeColor');

                    this.polyLine.setStyle({
                        color: strokeColor
                    });
                }
            },
            mouseLeave: function () {
                this.clearAll();
                this.creating = false;
            }
        },

        attachEvents: function (map) {
            this.eventHandlers = {
                mouseDown: this.events.mouseDown.bind(this),
                mouseMove: this.events.mouseMove.bind(this),
                mouseLeave: this.events.mouseLeave.bind(this)
            };

            this.map.on('mousedown', this.eventHandlers.mouseDown);
            this.map.on('mouseup', L.DomEvent.stopPropagation);
            this.map.on('contextmenu', L.DomEvent.stopPropagation);
            this.map.on('mousemove', this.eventHandlers.mouseMove);
            this.map.getContainer().addEventListener('mouseleave', this.eventHandlers.mouseLeave);
        }
    });

    L.polyLasso = function (options) {
        return new L.PolyLasso(options);
    };
})();
