'use strict';

angular.module('app.jht.moosedatacard', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.moosedatacard', {
            url: '/moosedatacard',
            templateUrl: 'jht/moosedatacard/moose-data-card.html',
            controller: 'MooseDataCardImportController',
            resolve: {}
        });
    })
    .controller('MooseDataCardImportController', function ($scope, $timeout, $translate, NotificationService) {
        var acceptedFileExtensions = ['.xml', '.pdf'];

        var errorCodes = {
            duplicate: "duplicate-file-to-upload",
            fileTooBig: "file-too-big",
            unacceptedExtension: "unaccepted-extension"
        };

        $scope.successfulUploads = [];
        $scope.failedUploads = [];
        $scope.dropzone = null;

        $scope.dropzoneConfig = {
            acceptedFiles: acceptedFileExtensions.join(', '),
            addRemoveLinks: true,
            autoProcessQueue: false,
            maxFiles: 2,
            maxFilesize: 50, // MiB
            uploadMultiple: true,
            url: '/api/v1/moosedatacard/import',

            dictFileTooBig: errorCodes.fileTooBig,
            dictInvalidFileType: errorCodes.unacceptedExtension,
            dictRemoveFile: $translate.instant('reporting.mooseDataCardUpload.removeUploadFile')
        };

        $scope.dropzoneConfig.previewTemplate =
            '<div class="dz-preview dz-file-preview">\n' +
            '  <div class="dz-image"><img data-dz-thumbnail /></div>\n' +
            '  <div class="dz-details">\n' +
            '    <div class="dz-filename"><span data-dz-name></span></div>\n' +
            '    <div class="dz-size"><span data-dz-size></span></div>\n' +
            '  </div>\n' +
            '  <div class="dz-progress"><span class="dz-upload" data-dz-uploadprogress></span></div>\n' +
            '  <div class="dz-error-message"><span data-dz-errormessage></span></div>\n' +
            '  <div class="dz-success-mark">\n' +
            '  </div>\n' +
            '  <div class="dz-error-mark">\n' +
            '  </div>\n' +
            '</div>\n';

        // nameFn function takes filename and its last '.' index as parameter.
        var getFilenameTransformation = function (filename, nameFn, returnValueIfNoDotPresent) {
            if (!angular.isString(filename)) {
                return null;
            }

            var lastIndexOfDot = filename.lastIndexOf('.');
            return lastIndexOfDot >= 0 ? nameFn(filename, lastIndexOfDot) : returnValueIfNoDotPresent;
        };

        var getFileBasename = function (filename) {
            return getFilenameTransformation(filename, function (name, lastIndexOfDot) {
                return filename.substring(0, lastIndexOfDot);
            }, filename);
        };

        var getFileExtension = function (filename) {
            return getFilenameTransformation(filename, function (name, lastIndexOfDot) {
                return filename.substring(lastIndexOfDot + 1);
            }, null);
        };

        $scope.dropzoneConfig.accept = function (file, done) {
            var basename, counterpart,
                dz = $scope.dropzone,
                queuedFiles = dz.getQueuedFiles();

            if (queuedFiles.length > 0 && _.pluck(queuedFiles, 'name').indexOf(file.name) >= 0) {
                done(errorCodes.duplicate);
                return;
            }

            done();

            // Custom queueing/processing logic needed to batch xml+pdf file pairs.
            basename = getFileBasename(file.name);
            counterpart = _.find(queuedFiles, function (queuedFile) {
                return basename === getFileBasename(queuedFile.name);
            });

            if (counterpart) {

                if (getFileExtension(counterpart.name) === 'xml') {
                    dz.processFiles([counterpart, file]);
                } else {
                    dz.processFiles([file, counterpart]);
                }
            }
        };

        $scope.dropzoneConfig.paramName = function (fileIndex) {
            switch (fileIndex) {
                case 0:
                    return 'xmlFile';
                case 1:
                    return 'pdfFile';
                default:
                    return 'unknownFile';
            }
        };

        var storeUploadResult = function (xmlFileName, messages, addToCollection, removeFromCollection) {
            var findUpload = function (upload) {
                return upload.filename === xmlFileName;
            };

            _.remove(removeFromCollection, findUpload);

            var existingUpload = _.find(addToCollection, findUpload);

            if (existingUpload) {
                existingUpload.messages = messages;
            } else {
                addToCollection.push({filename: xmlFileName, messages: messages});
            }
        };

        var uploadSuccessful = function (xmlFileName, messages) {
            storeUploadResult(xmlFileName, messages, $scope.successfulUploads, $scope.failedUploads);
        };

        var uploadFailed = function (xmlFileName, messages) {
            storeUploadResult(xmlFileName, messages, $scope.failedUploads, $scope.successfulUploads);
        };

        $scope.dropzoneEventHandlers = {
            maxfilesexceeded: function (file) {
                NotificationService.showMessage('reporting.mooseDataCardUpload.errorMessages.maxFilesExceeded', 'warn');
                $scope.dropzone.removeFile(file);
            },
            error: function (file, response, xhr) {
                var msg,
                    doRemoveFile = true;

                switch (response) {
                    case errorCodes.duplicate:
                        break;
                    case errorCodes.fileTooBig:
                        msg = $translate.instant('reporting.mooseDataCardUpload.errorMessages.fileTooBig', {
                            filesize: Math.round(file.size / 1024 / 10.24) / 100,
                            maxFilesize: $scope.dropzoneConfig.maxFilesize
                        });
                        NotificationService.showMessage(msg, 'warn');
                        break;
                    case errorCodes.unacceptedExtension:
                        msg = $translate.instant('reporting.mooseDataCardUpload.errorMessages.unacceptedFileExtension', {
                            extension: getFileExtension(file.name)
                        });
                        NotificationService.showMessage(msg, 'warn');
                        break;
                    default:
                        doRemoveFile = false;
                }

                if (doRemoveFile) {
                    $scope.dropzone.removeFile(file);
                }
            },
            errormultiple: function (files, response, xhr) {
                var xmlFileName;

                if (files.length === 2) {
                    xmlFileName = getFileExtension(files[0].name) === 'xml' ? files[0].name : files[1].name;
                    uploadFailed(xmlFileName, response.messages);
                }
            },
            successmultiple: function (files, response, e) {
                var xmlFileName;

                if (files.length === 2) {
                    xmlFileName = getFileExtension(files[0].name) === 'xml' ? files[0].name : files[1].name;
                    uploadSuccessful(xmlFileName, response.messages);
                }
            },
            completemultiple: function (files) {
                // Delay removal of completed files from drop-zone for sleeker user experience.
                $timeout(function () {
                    var acceptedFilenames,
                        dz = $scope.dropzone,
                        acceptedFiles = dz.getAcceptedFiles();

                    if (acceptedFiles.length > 0) {
                        acceptedFilenames = _.pluck(acceptedFiles, 'name');

                        _.forEach(files, function (file) {
                            if (acceptedFilenames.indexOf(file.name) >= 0) {
                                dz.removeFile(file);
                            }
                        });
                    }
                }, 900);
            }
        };
    });
