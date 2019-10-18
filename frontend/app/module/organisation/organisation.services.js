'use strict';

angular.module('app.organisation.services', [])
    .factory('Organisations', function ($resource) {
        return $resource('api/v1/organisation/:id', {"id": "@id"});
    })
    .factory('OrganisationsByArea', function ($resource, $http, CacheFactory) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        function doTransform(data, headersGetter, status) {
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
        }

        return $resource('api/v1/organisation/rka', {}, {
            queryAll: {
                method: 'GET',
                isArray: true,
                url: '/api/v1/organisation/rka/list-all',
                cache: CacheFactory.get('areasCacheAll'),
                transformResponse: appendTransform($http.defaults.transformResponse, doTransform)
            },
            queryActive: {
                method: 'GET',
                isArray: true,
                url: '/api/v1/organisation/rka/list-active',
                cache: CacheFactory.get('areasCacheActive'),
                transformResponse: appendTransform($http.defaults.transformResponse, doTransform)
            }
        });
    });
