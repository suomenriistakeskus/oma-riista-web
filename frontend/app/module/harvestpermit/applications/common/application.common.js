'use strict';

angular.module('app.harvestpermit.application.common', ['app.metadata'])
    .component('harvestPermitPermitApplicationWizardApplicantType', {
        templateUrl: 'harvestpermit/applications/common/application-applicant-type.html',
        bindings: {
            subtype: '<'
        }
    })

    .component('harvestPermitApplicationSummaryApplicant', {
        templateUrl: 'harvestpermit/applications/common/summary-applicant.html',
        bindings: {
            application: '<'
        }
    })

    .component('harvestPermitApplicationDecisionDelivery', {
        templateUrl: 'harvestpermit/applications/common/summary-decision-delivery.html',
        bindings: {
            application: '<'
        }
    })

    .component('harvestPermitApplicationSummaryDeliveryAddress', {
        templateUrl: 'harvestpermit/applications/common/summary-delivery-address.html',
        bindings: {
            application: '<'
        }
    })

    .component('harvestPermitApplicationSummaryAttachments', {
        templateUrl: 'harvestpermit/applications/common/summary-attachments.html',
        bindings: {
            attachments: '<',
            baseUri: '<'
        },
        controller: function (FetchAndSaveBlob) {
            var $ctrl = this;

            $ctrl.downloadAttachment = function (id) {
                FetchAndSaveBlob.post($ctrl.baseUri + '/' + id);
            };
        }
    })
;
