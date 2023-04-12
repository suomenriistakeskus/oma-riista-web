'use strict';

angular.module('app.rhy.services', [])
    .factory('Rhys', function ($resource) {
        var apiPrefix = '/api/v1/riistanhoitoyhdistys';
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
                isArray: true
            },
            searchParamOrganisations: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/searchparams/orgs',
                params: {id: '@id'},
                isArray: true
            }, getSrvaRotation:{
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:id/srva-rotation',
                params:{id: '@id'}
            }, updateSrvaRotation:{
                method: 'POST',
                url: 'api/v1/riistanhoitoyhdistys/:id/srva-rotation',
                params:{id: '@id'}
            },
            getTaxationReport: {
                method: 'GET',
                url: apiPrefix + '/taxation',
                params: {year: '@year', speciesCode: '@speciesCode', rhyId: '@rhyId', htaId: '@htaId'}
            },
            getMooseAreas: {
                method: 'GET',
                url: apiPrefix + '/taxation/moose_areas',
                params: { rhyId: '@rhyId'}
            },
            getTaxationReportYears: {
                method: 'GET',
                isArray: true,
                url: apiPrefix + '/taxation/years',
                params: { rhyId: '@rhyId'}
            },
            getExcelExport: {
                method: 'POST',
                url: apiPrefix + '/taxation/excel',
            },
            saveOrUpdateTaxationReport: {
                method: 'POST',
                url: apiPrefix + '/taxation'
            },
            downloadTaxationAttachment: {
                method: 'POST',
                url: apiPrefix + '/taxation'
            },
            deleteTaxationAttachment: {
                method: 'DELETE',
                url: apiPrefix + '/taxation/attachment/:id'
            },
        });
    });
