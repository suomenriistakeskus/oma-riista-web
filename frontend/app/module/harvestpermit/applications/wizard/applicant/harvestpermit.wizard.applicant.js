'use strict';

angular.module('app.harvestpermit.application.wizard.applicant', ['app.metadata'])
    .component('harvestPermitApplicationApplicant', {
        templateUrl: 'harvestpermit/applications/wizard/applicant/applicant.html',
        bindings: {
            permitHolder: '<',
            contactPerson: '<',
            isValid: '&'
        }
    })
;
