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
            getFullDetails: getMethod('/full'),

            // application types
            listTypes: getMethod('/types', true),
            findType: getMethod('/findtype'),

            // Search for moderator
            search: postMethod('/search', true),
            assignedApplications: postMethod('/assigned/applications', true),
            assignedDecisions: postMethod('/assigned/decisions', true),
            postalQueue: postMethod('/search/postalqueue', true),
            listYears: getMethod('/years', true),
            listHandlers: getMethod('/handlers', true),
            listPartnerClubs: getMethod('/partner/club', true),

            // Validate and send
            validate: postMethod('/validate'),
            sendApplication: postMethod('/send'),
            updateAdditionalData: postMethod('/additional'),
            startAmending: postMethod('/amend/start'),
            stopAmending: postMethod('/amend/stop'),

            // Species amounts
            listSpeciesAmounts: getMethod('/species', true),
            saveSpeciesAmounts: postMethod('/species', true),

            // Permit holder
            listAvailablePermitHolders: getMethod('/permit-holder', true),
            searchPermitHolder: getMethod('/permit-holder-search'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Shooter count
            getShooterCounts: getMethod('/shooters'),
            updateShooterCounts: postMethod('/shooters'),

            // Attachments
            getAttachments: getMethod('/attachment', true),
            importMh: postMethod('/mh'),

            // Permit area
            getArea: getMethod('/area'),
            getAreaStatus: getMethod('/area/status'),
            setAreaReady: postMethod('/area/ready'),
            setAreaIncomplete: postMethod('/area/incomplete'),
            getBounds: getMethod('/area/bounds'),
            getGeometry: getMethod('/area/geometry'),

            // Conflicts & fragments
            listConflicts: getMethod('/conflicts', true),
            listPairwiseConflicts: getMethod('/conflicts/:otherId', true),
            getGeometryFragmentInfo: postMethod('/area/fragmentinfo')
        });
    })
    .factory('HarvestPermitApplicationAreaPartners', function ($resource) {
        var apiPrefix = '/api/v1/harvestpermit/application/:applicationId/partner/:partnerId';

        return $resource(apiPrefix, {'applicationId': '@applicationId', 'partnerId': '@partnerId'}, {
            listAvailable: {
                method: 'GET',
                url: apiPrefix + '/available',
                isArray: true
            }
        });
    });
