'use strict';

angular.module('app.jht.otherwisedeceased-attachments', [])
    .component('rModalAttachments', {
        templateUrl: 'jht/otherwisedeceased/otherwisedeceased-attachments.html',
        bindings: {
            url: '@',
            attachments: '<'
        },
        controller: function ($timeout, $translate, dialogs, OtherwiseDeceasedService, AttachmentService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.dropzone = AttachmentService.getDropzone();
                $ctrl.dropzoneConfig = AttachmentService.getDefaultConfig($ctrl.url);
                $ctrl.dropzoneEventHandlers = AttachmentService.getDefaultHandlers();
            };

            $ctrl.downloadAttachment = function (id) {
                OtherwiseDeceasedService.downloadAttachment(id);
            };

            $ctrl.deleteAttachment = function (id) {
                var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                var dialogMessage = $translate.instant('jht.otherwiseDeceased.deleteAttachmentConfirmation');

                dialogs.confirm(dialogTitle, dialogMessage).result.then(
                    function () {
                        OtherwiseDeceasedService.deleteAttachment(id);
                        $ctrl.attachments = _.filter($ctrl.attachments, function (a) { return a.id !== id; });
                    });
            };
        }
    })
    .service('AttachmentService', function ($q, $timeout, $translate, FetchAndSaveBlob, NotificationService) {
        var self = this;
        self.data = {dropzone: {}};
        self.dto = null;

        self.getDropzone = function () {
            return self.data;
        };

        self.getDefaultConfig = function (url) {
            return {
                autoProcessQueue: false,
                addRemoveLinks: true,
                maxFiles: 10,
                maxFilesize: 50, // MiB
                uploadMultiple: true,
                parallelUploads: 10,
                url: url,
                paramName: function () {
                    return "files";
                },
                dictFileTooBig: $translate.instant('jht.otherwiseDeceased.dropzoneError.fileTooBig', {o: '{{', c: '}}'}),
                dictMaxFilesExceeded: $translate.instant('jht.otherwiseDeceased.dropzoneError.maxFilesExceeded', {o: '{{', c: '}}'}),
                dictRemoveFile: $translate.instant('global.button.delete')
            };
        };

        self.downloadAttachment = function (url) {
            return FetchAndSaveBlob.get(url);
        };

        self.hasAttachments = function () {
            return _.isFunction(self.data.dropzone.getQueuedFiles)
                && self.data.dropzone.getQueuedFiles().length > 0;
        };

        self.sendAttachments = function (dto) {
            self.dto = dto;

            return $q(function (resolve) {
                self.resolve = resolve;
                self.data.dropzone.processQueue();
            });
        };

        self.getDefaultHandlers = function () {
            return {
                addedfile: function (file) {
                },
                error: function (file, response, xhr) {
                    self.data.dropzone.removeFile(file);
                    NotificationService.showMessage(response, "error", {translateMessage: false});
                },
                sendingmultiple: function (file, xhr, formData) {
                    formData.append('dto', JSON.stringify(self.dto));
                },
                successmultiple: function (file, savedItem) {
                    self.data.dropzone.removeFile(file);
                    self.resolve(savedItem);
                }
            };
        };
    });