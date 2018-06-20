'use strict';

angular.module('app.rhy.services', [])
    .factory('Rhys', function ($resource) {
        return $resource('api/v1/riistanhoitoyhdistys/:id', {id: '@id'}, {
            query: {method: 'GET', params: {type: 'page'}},
            get: {method: 'GET'},
            getPublicInfo: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:id/public'
            },
            update: {method: 'PUT'},
            clubContacts: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:id/contacts',
                isArray: true
            },
            clubLeaders: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:id/leaders/:year',
                params: {year: '@year'},
                isArray: true
            },
            moosePermitHuntingYears: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:id/moosepermit/huntingyears',
                isArray: true
            },
            listMoosePermits: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:id/moosepermit',
                params: {year: '@year', species: '@species'},
                isArray: true
            },
            searchParamOrganisations: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/searchparams/orgs',
                params: {id: '@id'},
                isArray: true
            },
            moosepermitStatistics: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:id/moosepermit/statistics',
                isArray: true
            }
        });
    });
