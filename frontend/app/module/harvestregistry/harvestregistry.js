'use strict';

angular.module('app.harvestregistry', [])
    .factory('HarvestRegistry', function ($resource) {
        var apiPrefix = 'api/v1/harvestregistry';

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {personId: '@personId'}, {
            query: postMethod('', false),
            queryRhy: postMethod('/rhy', false),
            list: postMethod('/:personId')
        });
    })
    .factory('Municipalities', function ($resource, $http, CacheFactory) {

        return $resource('api/v1/municipality/list', {}, {
            query: {
                method: 'GET',
                isArray: true,
                cache: CacheFactory.get('municipalityCache')
            }
        });
    });
