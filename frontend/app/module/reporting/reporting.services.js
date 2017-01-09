'use strict';

angular.module('app.reporting.services', [])
    .factory('OccupationContactSearch', function ($resource) {
        return $resource('api/v1/contactsearch/occupation', {}, {
            'search': {
                method: 'POST',
                isArray: true
            }
        });
    })
    .factory('RhyContactSearch', function ($resource) {
        return $resource('api/v1/contactsearch/rhy', {}, {
            'search': {
                method: 'POST',
                isArray: true
            }
        });
    })
    .factory('Areas', function ($resource, $http, CacheFactory) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        return $resource('api/v1/contactsearch/areas', {}, {
            'query': {
                method: 'GET',
                isArray: true,
                cache: CacheFactory.get('areasContactSearchCache'),
                transformResponse: appendTransform($http.defaults.transformResponse, function(areas, headersGetter, status) {
                    if (status === 200) {
                        // sort areas and rhys by name, also make sure that all area subOrganisations are of RHY type
                        _.each(areas, function (area) {
                            area.subOrganisations = _.sortBy(area.subOrganisations, 'name').filter(function (v) {
                                return v.organisationType === 'RHY';
                            });
                        });
                        return _.sortBy(areas, 'name');
                    } else {
                        return areas;
                    }
                })
            }
        });
    })
    .factory('Htas', function ($resource, $http, CacheFactory) {
        return $resource('api/v1/contactsearch/htas', {}, {
            'query': {
                method: 'GET',
                isArray: true,
                cache: CacheFactory.get('htasContactSearchCache')
            }
        });
    })
    .factory('HarvestReportLocalityResolver', function ($translate, Areas) {
        var _getHuntingArea = function (harvestReport) {
            if (harvestReport.harvestQuota && harvestReport.harvestQuota.harvestArea) {
                var ha = harvestReport.harvestQuota.harvestArea;

                return $translate.use() === 'sv' ? ha.nameSV : ha.nameFI;
            }

            return '-';
        };

        return {
            get: function () {
                return Areas.query().$promise.then(function (areas) {
                    var organisations = {};

                    _.each(areas, function (area) {
                        if (area && area.subOrganisations) {
                            _.each(area.subOrganisations, function (org) {
                                if (org && org.id) {
                                    organisations[org.id] = { 'name': org.name, 'areaName': area.name };
                                }
                            });
                        }
                    });

                    return {
                        getHuntingArea: function (harvestReport) {
                            return _getHuntingArea(harvestReport);
                        },
                        getAreaName: function (harvestReport) {
                            var rhy = organisations[harvestReport.rhyId];
                            return (rhy && rhy.areaName) ? rhy.areaName : '-';
                        },
                        getRhyName: function (harvestReport) {
                            var rhy = organisations[harvestReport.rhyId];
                            return (rhy && rhy.name) ? rhy.name : '-';
                        }
                    };
                });
            }
        };
    });
