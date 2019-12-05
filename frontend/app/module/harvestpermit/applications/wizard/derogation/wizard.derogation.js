'use strict';

angular.module('app.harvestpermit.application.wizard.derogation', ['app.metadata', 'app.harvestpermit.area'])

    .factory('DerogationPermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/derogation/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {id: '@id'}, {
            // Area
            getArea: getMethod('/area'),
            updateArea: postMethod('/area'),
            listAvailableClubs: getMethod('/area/clubs', true),
            addAreaAttachment: postMethod('/area-attachment'),

            // Damage
            getSpeciesDamage: getMethod('/damage', true),
            updateSpeciesDamage: postMethod('/damage', true),

            // Population
            getSpeciesPopulation: getMethod('/population', true),
            updateSpeciesPopulation: postMethod('/population', true),

            // Attachments
            listAttachments: getMethod('/attachment', true),
            updateAttachmentAdditionalInfo: postMethod('/attachment', true),
        });
    });
