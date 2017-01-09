'use strict';

angular.module('app.adminharvest.services', [])
    .factory('HarvestAdmin', function ($resource) {
        return $resource('api/v1/harvestreport/admin', null, {
            query: {method: 'GET'}
        });
    });
