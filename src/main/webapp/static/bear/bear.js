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

        initialize: function (options) {
            L.setOptions(this, options);
            var url = "https://kartta.riista.fi/tms/1.0.0/maasto_kiint/EPSG_3067/{z}/{x}/{y}.png";
            L.TileLayer.prototype.initialize.call(this, url, options);
        }
    });

    var RiistaWidget = function(opts) {
        this.opts = opts || {};
        var lang = opts.lang || 'fi';

        if (lang == 'sv') {
            this.i18n = {
                'huntingYear': 'Jaktår',
                'areaWest': 'Med stöd av kvot jagade på det västra renskötselområdet',
                'areaEast': 'Med stöd av kvot jagade på det östra renskötselområdet',
                'permit207': 'Erhållna björnar med dispens i stamvårdande syfte',
                'permit202': 'Erhållna björnar med dispens baserat på skadegrund',
                'permitText207': 'Dispens i stamvårdande syfte',
                'permitText202': 'Skadebaserad dispens',
                'UNKNOWN': 'Okänd',
                'ADULT': 'Vuxen',
                'YOUNG': 'Under 1 år',
                'MALE': 'Handjur',
                'FEMALE': 'Hondjur'
            };
        } else {
            this.i18n = {
                'huntingYear': 'Metsästysvuosi',
                'areaWest': 'Läntisellä poronhoitoalueella kiintiön perusteella metsästetyt',
                'areaEast': 'Itäisellä poronhoitoalueella kiintiön perusteella metsästetyt',
                'permit207': 'Kannanhoidollisilla poikkeusluvilla saadut karhut',
                'permit202': 'Vahinkoperusteisilla poikkeusluvilla saadut karhut',
                'permitText207': 'Kannanhoidollinen poikkeuslupa',
                'permitText202': 'Vahinkoperusteinen poikkeuslupa',
                'UNKNOWN': 'Tuntematon',
                'ADULT': 'Aikuinen',
                'YOUNG': 'Alle 1 v',
                'MALE': 'Uros',
                'FEMALE': 'Naaras'
            };
        }
        this.tableTemplate = Handlebars.templates['bear-table.tmpl.' + lang + '.html'];
        this.popupTemplate = Handlebars.templates['bear-popup.tmpl.' + lang + '.html'];
        $("#" + opts.yearSelectTitleId).text(this.i18n.huntingYear);

        this.tableWest = $("#" + this.opts.tableWestId);
        this.tableEast = $("#" + this.opts.tableEastId);
        this.tablePermit207 = $("#" + this.opts.tablePermit207);
        this.tablePermit202 = $("#" + this.opts.tablePermit202);

        this.areaConfig = {
            west: {title: this.i18n.areaWest, table: this.tableWest, filter: this.areaFilter('Läntinen'), showQuota: true, quota: this.findQuota('Läntinen')},
            east: {title: this.i18n.areaEast, table: this.tableEast, filter: this.areaFilter('Itäinen'), showQuota: true, quota: this.findQuota('Itäinen')},
            permit207: {title: this.i18n.permit207, table: this.tablePermit207, filter: this.permitFilter("207"), showQuota: false, quota: _.partial(_.noop)},
            permit202: {title: this.i18n.permit202, table: this.tablePermit202, filter: this.permitFilter("202"), showQuota: false, quota: _.partial(_.noop)}
        };

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

    RiistaWidget.prototype.areaFilter = function (areaFiPrefix) {
        return _.partial(_.filter, _, function(a) {
            if (!areaFiPrefix) {
                return !a.properties.area_fi;
            }
            return _.startsWith(a.properties.area_fi, areaFiPrefix);
        });
    };

    RiistaWidget.prototype.permitFilter = function (permitTypeCode) {
        return _.partial(_.filter, _, function(a) {
            return a.properties.permit_type_code === permitTypeCode;
        });
    };

    RiistaWidget.prototype.findQuota = function (areaFiPrefix) {
        var self = this;
        return function () {
            return _.find(self.geojson.properties.usedQuotas, function(quota) {
                return _.startsWith(quota.area_fi, areaFiPrefix);
            })
        };
    };

    RiistaWidget.prototype.processData = function(data) {
        data.crs = {"type": "name", "properties": {"name": "urn:ogc:def:crs:EPSG::3067"}};

        var self = this;

        var processFeature = function (accumulator, feature) {
            var props = feature.properties;

            props.id = accumulator.index();
            props.order = accumulator.order();
            props.age = props.age ? self.i18n[props.age] : '';
            props.gender = props.gender ? self.i18n[props.gender] : '';
            props.longitude = feature.geometry.coordinates[0];
            props.latitude = feature.geometry.coordinates[1];
            props.permit = props.permit_type_code ? self.i18n['permitText' + props.permit_type_code] : '';
            return accumulator.advance();
        };

        var acc = function () {
            var _index = 0;
            var _order = 1;
            return {
                index: function () {
                    return _index;
                },
                order: function () {
                    return _order;
                },
                advance: function () {
                    _index++;
                    _order++;
                    return this;
                },
                reset: function () {
                    _order = 1;
                    return this;
                }
            };
        }();
        _.reduce(this.areaConfig.west.filter(data.features), processFeature, acc);
        _.reduce(this.areaConfig.east.filter(data.features), processFeature, acc.reset());
        _.reduce(this.areaConfig.permit207.filter(data.features), processFeature, acc.reset());
        _.reduce(this.areaConfig.permit202.filter(data.features), processFeature, acc.reset());

        return data;
    };

    RiistaWidget.prototype.renderTable = function() {
        var self = this;
        var createTable = function (conf) {
            var quota = conf.quota();
            var tableContent = self.tableTemplate({
                "title": conf.title,
                "showQuota": conf.showQuota,
                "remainingQuota": quota ? quota.remaining : '',
                "features": conf.filter(self.getVisibleFeatures())
            });

            conf.table.html(tableContent).on("click", function(e) {
                var row = $(e.target).parents("tr:first");
                if (row && row.attr('data-ndx')) {
                    var index = parseInt(row.attr('data-ndx'));
                    var feature = _.find(self.geojson.features, function (f) {
                        return f.properties.id === index;
                    });
                    var zoom = self.map.getZoom();
                    self.map.setView(feature.properties.latlng, zoom + 1);
                }
            });
        };
        createTable(this.areaConfig.west);
        createTable(this.areaConfig.east);
        createTable(this.areaConfig.permit202);
        createTable(this.areaConfig.permit207);
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
            crs: L.CRS.EPSG3067,
            continuousWorld: true,
            worldCopyJump: false,
            minZoom: 2,
            maxZoom: 10,
            zoomAnimation: true,
            zoomControl: false
        });

        L.control.zoom({position: 'bottomleft'}).addTo(map);
        map.addLayer(new L.TileLayer.Riista({})).setView([65.01275, 25.46815], 2);

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
            disableClusteringAtZoom: 5,
            showCoverageOnHover: false
        }).addLayer(geoJsonLayer));

        return geoJsonLayer;
    };

    return RiistaWidget;
})();
