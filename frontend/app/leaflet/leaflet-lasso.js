(function () {
    "use strict";

    if (typeof L === 'undefined') {
        throw 'L.Lasso: Leaflet.js is required';
    }

    L.Lasso = L.FeatureGroup.extend({
        initialize: function (options) {
            this.options = _.defaults({}, this.defaultOptions(), options || {});
            this.creating = false;
            this.eventHandlers = [];
            this.containerPoints = [];
            this.mouseButton = null;
        },

        defaultOptions: function () {
            return {
                simplifyThreshold: 2,
                strokeColour: 'red',
                strokeWidth: 2,
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
            this.map.off('contextmenu', L.DomEvent.stopPropagation);
            this.map.off('mousemove', this.eventHandlers.mouseMove);
            this.map.off('mouseup', this.eventHandlers.mouseUp);
            this.map.getContainer().removeEventListener('mouseleave', this.eventHandlers.mouseLeave);

            map.dragging.enable();
        },

        clearAll: function clearAll() {
            if (this.polyLine) {
                this.map.removeLayer(this.polyLine);
                this.polyLine = null;
            }
        },

        createPath: function () {
            var simplified = L.LineUtil.simplify(this.containerPoints, this.options.simplifyThreshold);

            this.map.fire("lasso", {
                latLngs: this.convertPointsToLatLngs(simplified),
                bounds: this.polyLine.getBounds(),
                mouseButton: this.mouseButton
            });

            this.clearAll();
        },

        events: {
            mouseDown: function (event) {
                event = event.originalEvent;

                var point = this.map.mouseEventToContainerPoint(event);

                this.creating = true;
                this.mouseButton = (event.which || event.button) < 2 ? 'left' : 'right';
                this.containerPoints = [];

                var latLng = this.map.containerPointToLatLng(point);

                this.polyLine = L.polyline([latLng], {
                    color: this.getOption('strokeColour'),
                    'stroke-width': this.getOption('strokeWidth')
                }).addTo(this.map);
            },
            mouseMove: function (event) {
                event = event.originalEvent;
                var point = this.map.mouseEventToContainerPoint(event);

                if (this.creating) {
                    this.containerPoints.push(point);

                    var latLng = this.map.containerPointToLatLng(point);
                    this.polyLine.addLatLng(latLng);
                }
            },
            mouseUp: function () {
                if (this.creating) {
                    this.creating = false;
                    this.createPath();
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
                mouseUp: this.events.mouseUp.bind(this),
                mouseLeave: this.events.mouseLeave.bind(this)
            };

            this.map.on('mousedown', this.eventHandlers.mouseDown);
            this.map.on('contextmenu', L.DomEvent.stopPropagation);
            this.map.on('mousemove', this.eventHandlers.mouseMove);
            this.map.on('mouseup', this.eventHandlers.mouseUp);
            this.map.getContainer().addEventListener('mouseleave', this.eventHandlers.mouseLeave);
        },

        convertPointsToLatLngs: function convertPointsToLatLngs(points) {
            return _.map(points, function (point) {
                return this.map.containerPointToLatLng(point);
            }, this);
        }
    });

    L.lasso = function (options) {
        return new L.Lasso(options);
    };

})();
