'use strict';

angular.module('app.harvestpermit.application.wizard.derogation.attachments', ['app.metadata'])

    .controller('DerogationPermitWizardAttachmentsController', function ($q, $http, $translate, dialogs,
                                                                         DerogationPermitApplication,
                                                                         wizard, applicationId, attachmentList, states) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.attachmentList = attachmentList;
            $ctrl.attachmentBaseUri = '/api/v1/harvestpermit/application/' + applicationId + '/attachment';
            $ctrl.attachmentIdToRemove = [];
        };

        $ctrl.exit = function () {
            save().then(function () {
                wizard.exit();
            });
        };

        $ctrl.previous = function () {
            save().then(function () {
                wizard.goto(states.previous);
            });
        };

        $ctrl.previousDisabled = function (form) {
            return form.$invalid;
        };

        $ctrl.next = function () {
            save().then(function () {
                wizard.goto(states.next);
            });
        };

        $ctrl.nextDisabled = function (form) {
            return form.$invalid;
        };

        $ctrl.attachmentUploadComplete = function (fileName, response) {
            $ctrl.attachmentList.push({
                id: response.id,
                name: fileName,
                additionalInfo: ''
            });
        };

        $ctrl.removeAttachment = function (attachment) {
            var modalTitle = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.title');
            var modalBody = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.body');

            dialogs.confirm(modalTitle, modalBody).result.then(function () {
                var indexToRemove = _.findIndex($ctrl.attachmentList, ['id', attachment.id]);

                if (indexToRemove >= 0) {
                    $ctrl.attachmentList.splice(indexToRemove, 1);
                    $ctrl.attachmentIdToRemove.push(attachment.id);
                }
            });
        };

        function save() {
            var requestList = _.map($ctrl.attachmentList, function (a) {
                return {
                    id: a.id,
                    additionalInfo: a.additionalInfo
                };
            });

            return DerogationPermitApplication.updateAttachmentAdditionalInfo({id: applicationId}, {
                list: requestList

            }).$promise.then(function () {
                var promiseArray = _.map($ctrl.attachmentIdToRemove, function (id) {
                    return $http.delete($ctrl.attachmentBaseUri + '/' + id);
                });

                return $q.all(promiseArray);
            });
        }
    })


    .component('derogationApplicationSummaryAttachments', {
        templateUrl: 'harvestpermit/applications/wizard/derogation/attachments/summary-attachments.html',
        bindings: {
            attachments: '<',
            baseUri: '<'
        },
        controller: function ($http, $translate, dialogs, FormPostService) {
            var $ctrl = this;
            $ctrl.downloadAttachment = function (id) {
                FormPostService.submitFormUsingBlankTarget($ctrl.baseUri + '/' + id);
            };
        }
    });
