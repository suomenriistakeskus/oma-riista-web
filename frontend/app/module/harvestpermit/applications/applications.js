'use strict';

angular.module('app.harvestpermit.application', [])
    .factory('HarvestPermitApplications', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {id: '@id'}, {
            listMyApplicationsAndDecisions: getMethod('/myApplicationsAndDecisions', true),

            // application types
            listTypes: getMethod('/types', true),
            findType: getMethod('/findtype'),

            // Search for moderator
            search: postMethod('/search'),
            assignedApplications: postMethod('/assigned/applications'),
            assignedDecisions: postMethod('/assigned/decisions'),
            postalQueue: postMethod('/search/postalqueue', true),
            annualRenewals: postMethod('/search/annualrenewals', true),
            listYears: getMethod('/years', true),
            listHandlers: getMethod('/handlers', true),
            listSpecies: postMethod('/species', true),

            // Wizard
            createAmendmentApplication: postMethod('/amendment'),
            updateAdditionalData: postMethod('/additional'),
            getAttachments: getMethod('/attachment', true),

            // Validate and send
            send: postMethod('/send'),
            validate: postMethod('/validate'),

            // Amend
            startAmending: postMethod('/amend/start'),
            stopAmending: postMethod('/amend/stop')
        });
    })
    .factory('ModeratorTodos', function ($resource) {
        var apiPrefix = 'api/v1/moderator/todo';
        return $resource(apiPrefix, {}, {});
    })
    .constant('ApplicationVehicleTypes', ['AUTO', 'MOOTTORIKELKKA', 'MONKIJA', 'MUU']);
