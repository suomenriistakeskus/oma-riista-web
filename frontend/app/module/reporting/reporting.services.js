'use strict';

angular.module('app.reporting.services', [])
    .factory('Areas', function ($resource, $http, CacheFactory) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        return $resource('api/v1/contactsearch/areas', {}, {
            query: {
                method: 'GET',
                isArray: true,
                cache: CacheFactory.get('areasContactSearchCache'),
                transformResponse: appendTransform($http.defaults.transformResponse, function(data, headersGetter, status) {
                    if (status === 200) {
                        // sort areas and rhys by name, also make sure that all area subOrganisations are of RHY type
                        _.each(data, function (area) {
                            area.subOrganisations = _.sortBy(area.subOrganisations, 'name').filter(function (v) {
                                return v.organisationType === 'RHY';
                            });
                        });
                        return _.sortBy(data, 'name');
                    } else {
                        return data;
                    }
                })
            }
        });
    })
    .factory('Htas', function ($resource, $http, CacheFactory) {
        return $resource('api/v1/contactsearch/htas', {}, {
            query: {
                method: 'GET',
                isArray: true,
                cache: CacheFactory.get('htasContactSearchCache')
            }
        });
    });
