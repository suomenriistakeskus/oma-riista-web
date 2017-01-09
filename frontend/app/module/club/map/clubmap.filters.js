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

        return function (areaSize) {
            return _.isNumber(areaSize) ? numberFilter(areaSize / 10000, 2) + ' ha' : null;
        };
    })
    .filter('featureAreaSize', function ($filter) {
        var prettyAreaSize = $filter('prettyAreaSize');
        var propertyGetter = _.property('properties.size');

        return function (feature) {
            var areaSize = propertyGetter(feature);
            return _.isNumber(areaSize) ? prettyAreaSize(areaSize) : null;
        };
    });
