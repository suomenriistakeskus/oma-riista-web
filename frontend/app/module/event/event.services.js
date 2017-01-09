'use strict';

angular.module('app.event.services', ['ngResource'])
    .factory('EventTypes', function ($http) {
        return $http.get('api/v1/organisation/eventtypes');
    })
    .factory('Events', function ($resource) {
        return $resource('api/v1/organisation/:orgId/events/:id', {"orgId": "@orgId", "id": "@id"}, {
            'query': { method: 'GET', isArray: true},
            'get': { method: 'GET'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    })
    .factory('Venues', function ($resource) {
        return $resource('api/v1/organisation/:orgId/venues/:id', {"orgId": "@orgId", "id": "@id"}, {
            'query': { method: 'GET', isArray: true},
            'get': { method: 'GET'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'},
            'attach': {
                url: 'api/v1/organisation/:orgId/attachvenue/:id',
                method: 'PUT',
                params: {"orgId": "@orgId", "id": "@id"}
            },
            'detach': {
                url: 'api/v1/organisation/:orgId/detachvenue/:id',
                method: 'DELETE',
                params: {"orgId": "@orgId", "id": "@id"}
            }
        });
    })
    .factory('VenueSearchApi', function ($resource) {
        return $resource('api/v1/search/venue', {}, {
            'search': { method: 'GET'}
        });
    })
    .service('VenueSearchByName', function (VenueSearchApi) {
        var search = function (params, cb) {
            VenueSearchApi.search(params).$promise.then(cb);
        };
        return {
            search: _.debounce(search, 500),
            searchImmediately: search
        };
    });