'use strict';

angular.module('app.mapeditor.metsahallitus', [])
    .factory('GeoJsonEditorMetsahallitus', function () {
        var PREFIX_MOOSE = 'mh-hirvi-';

        function isMooseFeature(feature) {
            return _.isObject(feature) && _.startsWith(feature.id, PREFIX_MOOSE);
        }

        function parseSelectedAreas(featureList) {
            return _(featureList)
                .filter(isMooseFeature)
                .map(function (feature) {
                    return {
                        id: feature.id,
                        number: feature.properties.number,
                        name: feature.properties.name,
                        size: feature.properties.size,
                        year: feature.properties.year
                    };
                })
                .value();
        }

        function Service(hirviAreaList, featureCollection, metsahallitusYear) {
            this.metsahallitusYear = metsahallitusYear;
            this.areaList = hirviAreaList;
            this.selectedAreaList = parseSelectedAreas(featureCollection.features);
        }

        var proto = Service.prototype;

        proto.getSelectedAreaList = function () {
            return this.selectedAreaList;
        };

        proto.addSelectedArea = function (a) {
            this.removeSelectedArea(a);
            var area = {
                id: PREFIX_MOOSE + a.gid,
                number: a.number,
                name: a.name,
                size: a.size,
                year: a.year
            };
            this.selectedAreaList.unshift(area);
            return area;
        };

        proto.removeSelectedArea = function (area) {
            _.remove(this.selectedAreaList, function (a) {
                return area.number === a.number;
            });
        };

        proto.isEmpty = function () {
            return _.size(this.selectedAreaList) === 0;
        };

        proto.isUpToDate = function (area) {
            return this.metsahallitusYear === area.year;
        };

        proto.hasChangedFeatures = function () {
            var self = this;
            return _.some(this.selectedAreaList, function (area) {
                return self.metsahallitusYear !== area.year;
            });
        };

        proto.filterMooseAreaList = function (searchQuery) {
            if (!searchQuery || _.isEmpty(searchQuery)) {
                return this.areaList;
            }

            var searchRegex = new RegExp('.*' + searchQuery + '.*', 'i');

            return _.filter(this.areaList, function (value) {
                var number = _.get(value, 'number');
                var name = _.get(value, 'name');

                return (number && searchRegex.test(number)) || (name && searchRegex.test(name));
            });
        };

        proto.findByCode = function (code) {
            return _(this.areaList).filter(function (a) {
                return a.number === code;
            }).head();
        };

        return {
            isMooseFeature: isMooseFeature,
            create: function (hirviAreaList, featureCollection, metsahallitusYear) {
                return new Service(hirviAreaList, featureCollection, metsahallitusYear);
            }
        };
    });
