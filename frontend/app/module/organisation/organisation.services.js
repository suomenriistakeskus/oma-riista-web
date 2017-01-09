'use strict';

angular.module('app.organisation.services', [])
    .factory('Organisations', function ($resource) {
        return $resource('api/v1/organisation/:id', {"id": "@id"});
    });
