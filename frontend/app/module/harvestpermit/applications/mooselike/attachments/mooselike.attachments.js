'use strict';

angular.module('app.harvestpermit.application.mooselike.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mooselike.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/mooselike/attachments/attachments.html',
                controller: 'MooselikePermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    attachments: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    shooterCounts: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getShooterCounts({id: applicationId}).$promise;
                    },
                    freeHunting: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getArea({
                            id: applicationId
                        }).$promise.then(function (res) {
                            return res.freeHunting;
                        });
                    }
                }
            })

            .state('jht.decision.application.wizard.mooselike.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/mooselike/attachments/attachments.html',
                controller: 'MooselikePermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                resolve: {
                    attachments: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    shooterCounts: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getShooterCounts({id: applicationId}).$promise;
                    },
                    freeHunting: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getArea({
                            id: applicationId
                        }).$promise.then(function (res) {
                            return res.freeHunting;
                        });
                    }
                }
            });
    })

    .component('permitApplicationAttachmentList', {
        templateUrl: 'harvestpermit/applications/mooselike/attachments/attachment-list.html',
        bindings: {
            attachments: '<',
            attachmentType: '<',
            attachmentConfig: '<', // baseUri, canDownload, canDelete
            refresh: '&'
        },
        controller: function ($http, $translate, dialogs, FormPostService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.filteredAttachments = filterAttachmentList($ctrl.attachments, $ctrl.attachmentType);
            };

            $ctrl.$onChanges = function (c) {
                if (c.attachments) {
                    $ctrl.filteredAttachments = filterAttachmentList(c.attachments.currentValue, $ctrl.attachmentType);
                }
            };

            function filterAttachmentList(attachmentList, attachmentType) {
                if (angular.isString(attachmentType)) {
                    return _.filter(attachmentList, {
                        type: attachmentType
                    });
                }

                return attachmentList;
            }

            $ctrl.canDownload = function () {
                return _.get($ctrl.attachmentConfig, 'canDownload', false);
            };

            $ctrl.canDelete = function () {
                return _.get($ctrl.attachmentConfig, 'canDelete', false);
            };

            $ctrl.downloadAttachment = function (id) {
                if ($ctrl.canDownload()) {
                    FormPostService.submitFormUsingBlankTarget(getAttachmentUri(id));
                }
            };

            $ctrl.deleteAttachment = function (id) {
                var modalTitle = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.title');
                var modalBody = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.body');

                dialogs.confirm(modalTitle, modalBody).result.then(function () {
                    $http.delete(getAttachmentUri(id)).then(function () {
                        $ctrl.refresh();
                    });
                });
            };

            function getAttachmentUri(id) {
                return $ctrl.attachmentConfig.baseUri + '/' + id;
            }
        }
    })

    .component('permitApplicationAttachmentUpload', {
        templateUrl: 'harvestpermit/applications/mooselike/attachments/attachment-upload.html',
        bindings: {
            uri: '<',
            attachmentType: '<',
            done: '&'
        },
        controller: function ($timeout) {
            var $ctrl = this;

            $ctrl.dropzone = null;

            $ctrl.$onInit = function () {
                $ctrl.dropzoneConfig = {
                    autoProcessQueue: true,
                    maxFiles: 1,
                    maxFilesize: 50, // MiB
                    uploadMultiple: false,
                    url: $ctrl.uri,
                    paramName: 'file',
                    params: {
                        attachmentType: $ctrl.attachmentType
                    }
                };
                $ctrl.errors = {
                    incompatibleFileType: false
                };
            };

            $ctrl.dropzoneEventHandlers = {
                success: function (file, response, xhr) {
                    $ctrl.dropzone.removeFile(file);
                    $timeout(function () {
                        $ctrl.errors = {};
                    });

                    $ctrl.done({
                        fileName: file.name || file.upload.filename,
                        response: response
                    });
                },
                error: function (file, response, xhr) {
                    $ctrl.dropzone.removeFile(file);
                    $timeout(function () {
                        $ctrl.errors = {
                            incompatibleFileType: true
                        };
                    });
                }
            };
        }
    })

    .constant('MooselikePermitWizardAttachmentsToggleState', {})

    .controller('MooselikePermitWizardAttachmentsController', function (MooselikePermitApplication,
                                                                        MooselikePermitWizardAttachmentsToggleState,
                                                                        MooselikePermitWizardMetsahallitusModal,
                                                                        wizard, applicationId,
                                                                        attachments, shooterCounts, freeHunting) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.attachments = attachments;
            $ctrl.attachmentConfig = {
                baseUri: '/api/v1/harvestpermit/application/' + applicationId + '/attachment',
                canDownload: true,
                canDelete: true
            };
            $ctrl.shooterCounts = shooterCounts || {};
            $ctrl.freeHunting = freeHunting;
            $ctrl.toggle = MooselikePermitWizardAttachmentsToggleState;
        };

        $ctrl.attachmentUploadComplete = function () {
            $ctrl.refreshAttachments();
        };

        $ctrl.refreshAttachments = function () {
            storeShooterCounts().then(function () {
                wizard.reload();
            });
        };

        $ctrl.hasAttachmentWithType = function (type) {
            return _.some($ctrl.attachments, ['type', type]);
        };

        $ctrl.getAttachmentCount = function (type) {
            return _($ctrl.attachments).filter({
                type: type
            }).size();
        };

        $ctrl.attachmentListComplete = function () {
            return !$ctrl.freeHunting || $ctrl.hasAttachmentWithType('MH_AREA_PERMIT') && $ctrl.hasAttachmentWithType('SHOOTER_LIST');
        };

        $ctrl.shooterCountsValid = function () {
            if (!$ctrl.freeHunting) {
                return true;
            }

            var counts = $ctrl.shooterCounts;
            var a = counts.shooterOnlyClub;
            var b = counts.shooterOtherClubPassive;
            var c = counts.shooterOtherClubActive;

            return isValidCount(a) && isValidCount(b) && isValidCount(c) && (a + b) > 0;
        };

        function isValidCount(c) {
            return _.isFinite(c) && c >= 0;
        }

        function storeShooterCounts() {
            var requestBody = $ctrl.freeHunting ? $ctrl.shooterCounts : {};
            return MooselikePermitApplication.updateShooterCounts({id: applicationId}, requestBody).$promise;
        }

        $ctrl.importMh = function () {
            MooselikePermitWizardMetsahallitusModal.showImport(applicationId).then($ctrl.refreshAttachments);
        };

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            wizard.goto('map');
        };

        $ctrl.next = function () {
            storeShooterCounts().then(function () {
                wizard.goto('summary');
            });
        };
    });
