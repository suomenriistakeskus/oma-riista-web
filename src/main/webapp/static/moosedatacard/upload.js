window.UploadWidget = (function () {
    try { Dropzone }
    catch (error) {
        throw new Error('Dropzone.js not loaded.');
    }

    Dropzone.autoDiscover = false;

    var UploadWidget = function (opts) {
        var self = this,
            acceptedFileExtensions = [".xml", ".pdf"],
            maxFilesize = 10, // MiB
            dropzoneConfig,
            errorCodes;

        this.opts = opts || {};

        this.successfulUploads = [];
        this.failedUploads = [];

        this.successfulUploadsTemplate = Handlebars.templates["successful-uploads.tmpl.html"];
        this.failedUploadsTemplate = Handlebars.templates["failed-uploads.tmpl.html"];

        this.successfulUploadsElement = $("#" + this.opts.successfulUploadsId);
        this.failedUploadsElement = $("#" + this.opts.failedUploadsId);

        this.onMaxFilesExceededState = false;

        errorCodes = {
            duplicate: "duplicate-file-to-upload",
            fileTooBig: "file-too-big",
            unacceptedExtension: "unaccepted-extension"
        };

        dropzoneConfig = {
            url: this.opts.url,

            // Prevents Dropzone from uploading dropped files immediately
            autoProcessQueue: false,

            uploadMultiple: true,
            maxFiles: 2,
            acceptedFiles: acceptedFileExtensions.join(", "),
            maxFilesize: maxFilesize,
            addRemoveLinks: true,

            dictFileTooBig: errorCodes.fileTooBig,
            dictInvalidFileType: errorCodes.unacceptedExtension,
            dictRemoveFile: "Poista tiedosto",

            init: function () {
                var dropzone = this;

                // nameFn function takes filename and its last '.' index as parameter.
                function getFilenameTransformation(filename, nameFn, returnValueIfNoDotPresent) {
                    if (!_.isString(filename)) {
                        return null;
                    }

                    var lastIndexOfDot = filename.lastIndexOf('.');
                    return lastIndexOfDot >= 0 ? nameFn(filename, lastIndexOfDot) : returnValueIfNoDotPresent;
                }

                function getFileBasename(filename) {
                    return getFilenameTransformation(filename, function (name, lastIndexOfDot) {
                        return filename.substring(0, lastIndexOfDot);
                    }, filename);
                }

                function getFileExtension(filename) {
                    return getFilenameTransformation(filename, function (name, lastIndexOfDot) {
                        return filename.substring(lastIndexOfDot + 1);
                    }, null);
                }

                dropzone.options.accept = function (file, done) {
                    var basename, counterpart,
                        queuedFiles = dropzone.getQueuedFiles();

                    if (queuedFiles.length > 0 && _.map(queuedFiles, "name").indexOf(file.name) >= 0) {
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
                        if (getFileExtension(counterpart.name) === "xml") {
                            dropzone.processFiles([counterpart, file]);
                        } else {
                            dropzone.processFiles([file, counterpart]);
                        }
                    }
                };

                function storeUploadResult(xmlFileName, messages, addToCollection, removeFromCollection) {
                    var findUpload = function (upload) {
                        return upload.filename === xmlFileName;
                    };

                    _.remove(removeFromCollection, findUpload);

                    var existingUpload = _.find(addToCollection, findUpload);

                    if (existingUpload) {
                        existingUpload.messages = messages;
                    } else {
                        addToCollection.push({ filename: xmlFileName, messages: messages });
                    }
                }

                function uploadSuccessful(xmlFileName, messages) {
                    storeUploadResult(xmlFileName, messages, self.successfulUploads, self.failedUploads);
                    self.renderLists();
                }

                function uploadFailed(xmlFileName, messages) {
                    storeUploadResult(xmlFileName, messages, self.failedUploads, self.successfulUploads);
                    self.renderLists();
                }

                function notifyError(msg) {
                    $.notify({
                        message: msg,
                    }, {
                        type: "danger"
                    });
                }

                // Event handlers

                this.on("maxfilesexceeded", function (file) {
                    if (!self.onMaxFilesExceededState) {
                        self.onMaxFilesExceededState = true;
                        notifyError("Voit lisätä vain kaksi tiedostoa kerrallaan. Ylimääräiset poistetaan automaattisesti.");
                    }
                    dropzone.removeFile(file);
                });

                this.on("error", function (file, response, xhr) {
                    var msg,
                        doRemoveFile = true;

                    switch (response) {
                    case errorCodes.duplicate:
                        break;
                    case errorCodes.fileTooBig:
                        msg = "Tiedosto on liian iso (" +
                            (Math.round(file.size / 1024 / 10.24) / 100) +
                            " MiB). Maksimikoko: " +
                            maxFilesize +
                            " MiB.";
                        notifyError(msg);
                        break;
                    case errorCodes.unacceptedExtension:
                        msg = "Hirvitietokortin sisäänluennassa kelpuutetaan vain pdf- ja xml-tiedostot. Yritit lähettää tiedoston, jonka pääte on: " +
                            getFileExtension(file.name);
                        notifyError(msg);
                        break;
                    default:
                        doRemoveFile = false;
                    }

                    if (doRemoveFile) {
                        dropzone.removeFile(file);
                    }
                });

                this.on("errormultiple", function (files, response, xhr) {
                    var xmlFileName;

                    if (files.length === 2) {
                        xmlFileName = getFileExtension(files[0].name) === "xml" ? files[0].name : files[1].name;
                        uploadFailed(xmlFileName, response.messages);
                    }
                });

                this.on("successmultiple", function (files, response, e) {
                    var xmlFileName;

                    if (files.length === 2) {
                        xmlFileName = getFileExtension(files[0].name) === "xml" ? files[0].name : files[1].name;
                        uploadSuccessful(xmlFileName, response.messages);
                    }
                });

                this.on("completemultiple", function (files) {
                    // Delay removal of completed files from dropzone for sleeker user experience.
                    window.setTimeout(function () {
                        var acceptedFilenames,
                            acceptedFiles = dropzone.getAcceptedFiles();

                        if (acceptedFiles.length > 0) {
                            acceptedFilenames = _.map(acceptedFiles, "name");

                            _.forEach(files, function (file) {
                                if (acceptedFilenames.indexOf(file.name) >= 0) {
                                    dropzone.removeFile(file);
                                }
                            });
                        }

                        self.onMaxFilesExceededState = false;
                    }, 900);
                });
            }
        };

        dropzoneConfig.paramName = function (fileIndex) {
            switch (fileIndex) {
            case 0:
                return "xmlFile";
            case 1:
                return "pdfFile";
            default:
                return "unknownFile";
            }
        };

        dropzoneConfig.previewTemplate =
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

        new Dropzone("#" + this.opts.dropzoneId, dropzoneConfig);
    };

    UploadWidget.prototype.renderLists = function () {
        this.successfulUploadsElement.html(this.successfulUploadsTemplate({
            "successfulUploads": this.successfulUploads
        }));

        this.failedUploadsElement.html(this.failedUploadsTemplate({
            "failedUploads": this.failedUploads
        }));
    };

    UploadWidget.prototype.init = function () {
        this.renderLists();
    };

    return UploadWidget;
})();
