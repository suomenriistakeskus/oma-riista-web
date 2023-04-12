'use strict';

angular.module('app.rhy.huntingcontrolevent.attachments', [])
    .component('rHuntingControllerEventAttachments', {
        templateUrl: 'rhy/huntingcontrolevent/attachments.html',
        bindings: {
            attachments: '<',
            canDelete: '<',
            onDelete: '&'
        },
        controller: function ($translate, dialogs, FetchAndSaveBlob, HuntingControlEvents) {
            var $ctrl = this;

            $ctrl.downloadAttachment = function(id) {
                FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/huntingcontrolevents/attachment/' + id);
            };

            $ctrl.removeAttachment = function(id) {
                var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                var dialogMessage = $translate.instant('global.dialog.confirmation.text');

                dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                    HuntingControlEvents.deleteAttachment({id: id}).$promise
                        .then(function () {
                            $ctrl.attachmentsDeleted = true;
                            $ctrl.onDelete();
                        });
                });
            };
        }});