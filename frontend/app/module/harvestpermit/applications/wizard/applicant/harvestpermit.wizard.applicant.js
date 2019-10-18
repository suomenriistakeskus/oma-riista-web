'use strict';

angular.module('app.harvestpermit.application.wizard.applicant', ['app.metadata'])
    .component('harvestPermitDerogationApplicationApplicant', {
        templateUrl: 'harvestpermit/applications/wizard/applicant/applicant-derogation.html',
        bindings: {
            permitHolder: '<',
            contactPerson: '<',
            isValid: '&'
        }
    })
;
