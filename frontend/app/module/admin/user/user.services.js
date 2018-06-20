'use strict';

angular.module('app.admin.user.services', [])
    .factory('Users', function ($resource) {
        return $resource('api/v1/admin/users/:id', { "id": "@id" }, {
            query: { method: 'GET', params: { type: "page", roles: '@roles'}},
            get: { method: 'GET' },
            update: { method: 'PUT' },
            privileges: {method: 'GET', url: 'api/v1/admin/users/privileges', isArray: true}
        });
    });
