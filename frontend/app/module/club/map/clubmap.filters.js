'use strict';

angular.module('app.clubmap.filters', [])
    .filter('propertyFeaturesChanged', function () {
        return function (input) {
            if (_.isArray(input)) {
                return _.filter(input, 'properties.changed', true);
            }
            return input;
        };
    })
    .filter('prettyAreaSize', function ($filter) {
        var numberFilter = $filter('number');

        return function (areaSize, fractionSize) {
            fractionSize = angular.isNumber(fractionSize) ? fractionSize : 2;
            return _.isNumber(areaSize) ? numberFilter(areaSize / 10000, fractionSize) + ' ha' : null;
        };
    })
    .filter('featureAreaSize', function ($filter) {
        var prettyAreaSize = $filter('prettyAreaSize');
        var propertyGetter = _.property('properties.size');

        return function (feature) {
            var areaSize = propertyGetter(feature);
            return _.isNumber(areaSize) ? prettyAreaSize(areaSize) : null;
        };
    })
    .filter('featureAreaSizeDiff', function ($filter) {
        var prettyAreaSize = $filter('prettyAreaSize');
        var propertyGetter = _.property('properties.diff_area');

        return function (feature) {
            var areaSize = propertyGetter(feature);
            if (_.isNumber(areaSize)) {
                return areaSize < 0 ? prettyAreaSize(areaSize) : '+' + prettyAreaSize(areaSize);
            }
            return null;
        };
    });
