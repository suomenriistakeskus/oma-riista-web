window.RiistaWidget = (function() {
    function _bounds() {
        var southWest = {lat: 55, lng: 11};
        var northEast = {lat: 73, lng: 50};
        return L.latLngBounds(southWest, northEast);
    }

    L.CRS.EPSG3067 = new L.Proj.CRS('EPSG:3067',
        '+proj=utm +zone=35 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs',
        {
            origin: [-548576, 6291456],
            bounds: L.bounds([-548576, 6291456], [1548576, 8388608]),
            resolutions: [
                8192, 4096, 2048, 1024, 512, 256,
                128, 64, 32, 16, 8, 4, 2, 1, 0.5,
                0.25, 0.125, 0.0625, 0.03125, 0.015625
            ]
        }
    );
    // Temporary fix for L.Scale control.
    // see https://github.com/kartena/Proj4Leaflet/issues/109
    L.CRS.EPSG3067.distance = L.CRS.Earth.distance;
    L.CRS.EPSG3067.R = 6378137;

    L.TileLayer.Riista = L.TileLayer.extend({
        options: {
            tms: true,
            attribution : '&copy; <a href="http://www.maanmittauslaitos.fi/avoindata_lisenssi_versio1_20120501"' +
            'target=new>Maanmittauslaitos</a>',
            bounds: _bounds()
        },

        initialize: function (url, options) {
            L.TileLayer.prototype.initialize.call(this, url, options);
        }
    });

    var RiistaWidget = function(opts) {
        this.opts = opts || {};
        var lang = opts.lang || 'fi';

        if (lang == 'sv') {
            this.i18n = {
                'huntingYear': 'Jaktår',
                'UNKNOWN': 'Okänd',
                'MALE': 'Handjur',
                'FEMALE': 'Hondjur'
            };
        } else {
            this.i18n = {
                'huntingYear': 'Metsästysvuosi',
                'UNKNOWN': 'Tuntematon',
                'MALE': 'Uros',
                'FEMALE': 'Naaras'
            };
        }
        this.tableTemplate = Handlebars.templates['wolf-table.tmpl.' + lang + '.html'];
        this.popupTemplate = Handlebars.templates['wolf-popup.tmpl.' + lang + '.html'];
        $("#" + opts.yearSelectTitleId).text(this.i18n.huntingYear);

        this.years = null;
        this.selectedYear = null;
    };

    RiistaWidget.prototype.initYears = function(years) {
        this.years = years;

        var combo = $("#" + this.opts.yearSelectId);
        $.each(this.years, function (i, el) {
            var optionBeginTag = el.current ? '<option selected>': '<option>';
            combo.append(optionBeginTag + el.text + '</option>');
        });

        var changeListener = this.onYearChange();
        changeListener(); // trigger change to force data be feched
        combo.change(changeListener);
    };

    RiistaWidget.prototype.onYearChange = function () {
        var self = this;
        var id = "#" + this.opts.yearSelectId;
        return function () {
            $(id + ' > option:selected').each(function (i, el) {
                var selectedText = $(el).text();
                var found = $.grep(self.years, function (y) {
                    return y.text === selectedText;
                });
                if (found.length === 1) {
                    self.selectedYear = found[0].year;
                    self.fetchData();
                } else {
                    console.log("Invalid state, years:", self.years, " selected:", selectedText, " found:", found);
                }
            });
        };
    };

    RiistaWidget.prototype.fetchData = function () {
        var self = this;
        this.opts.fetchData(self.selectedYear).done(function(data) {
            self.init(data);
        });
    };

    RiistaWidget.prototype.init = function(geojson) {
        this.table = $("#" + this.opts.tableId);
        if (this.map) {
            this.map.remove();
        }
        this.map = this.createMap(this.opts.mapId);
        this.geojson = this.processData(geojson);
        this.layer = this.createGeoJsonLayer();

        this.renderTable();

        var self = this;

        this.map.on('viewreset', function () {
            self.renderTable();
        });

        this.map.on('dragend', function () {
            self.renderTable();
        });

        this.map.on('zoomend', function () {
            self.renderTable();
        });
    };

    RiistaWidget.prototype.processData = function(data) {
        data.crs = {"type": "name", "properties": {"name": "urn:ogc:def:crs:EPSG::3067"}};

        for (var ndx = 0; ndx < data.features.length; ndx++) {
            var feature = data.features[ndx];
            var props = feature.properties;

            props.id = ndx + 1;
            props.age = props.age ? this.i18n[props.age] : '';
            props.gender = props.gender ? this.i18n[props.gender] : '';
            props.luke_status = props.luke_status || 'PENDING';
            props.luke_status = this.i18n[props.luke_status];
            props.longitude = feature.geometry.coordinates[0];
            props.latitude = feature.geometry.coordinates[1];
        }

        return data;
    };

    RiistaWidget.prototype.renderTable = function() {
        var self = this;
        var template = this.tableTemplate;
        var tableContent = template({
            "features": this.getVisibleFeatures()
        });

        this.table.html(tableContent).on("click", function(e) {
            var row = $(e.target).parents("tr:first");

            if (row) {
                var ndx = row.attr('data-ndx') - 1;
                var feature = self.geojson.features[ndx];
                var zoom = self.map.getZoom();
                self.map.setView(feature.properties.latlng, zoom + 1);
            }
        });
    };

    RiistaWidget.prototype.getVisibleFeatures = function() {
        if (this.map.getZoom() <= 3) {
            return this.geojson.features;
        }

        var bounds = this.map.getBounds();
        var inBounds = [];

        this.layer.eachLayer(function(marker) {
            if (bounds.contains(marker.getLatLng())) {
                inBounds.push(marker.feature);
            }
        });

        return inBounds;
    };

    RiistaWidget.prototype.createMap = function(mapId) {
        var map = new L.map(mapId, {
            crs: L.CRS.EPSG3857,
            continuousWorld: true,
            worldCopyJump: false,
            minZoom: 5,
            maxZoom: 10,
            zoomAnimation: true,
            zoomControl: false
        });

        L.control.zoom({position: 'bottomleft'}).addTo(map);
        map.addLayer(new L.TileLayer.Riista("https://kartta.riista.fi/tms/1.0.0/maasto_mobile/EPSG_3857/{z}/{x}/{y}.png", {
            minZoom: 5,
            maxZoom: 17
        })).setView([65.01275, 25.46815], 5);

        return map;
    };

    RiistaWidget.prototype.createGeoJsonLayer = function() {
        var self = this;
        var popupOpts = {
            minWidth: 600,
            maxWidth: 800,
            keepInView: false,
            closeButton: true
        };
        var geoJsonLayer = L.Proj.geoJson(self.geojson, {
            onEachFeature: function (feature, layer) {
                layer.bindPopup(self.popupTemplate(feature.properties), popupOpts);
                feature.properties.latlng = layer.getLatLng();
            }
        });

        this.map.addLayer(new L.MarkerClusterGroup({
            animate: false,
            spiderfyOnMaxZoom: false,
            disableClusteringAtZoom: 10,
            showCoverageOnHover: false
        }).addLayer(geoJsonLayer));

        return geoJsonLayer;
    };

    return RiistaWidget;
})();
