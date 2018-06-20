'use strict';

angular.module('app.harvestpermit.decision.attachments', [])
    .service('PermitDecisionAttachmentService', function (PermitDecision, FormPostService, $http, $translate, dialogs) {
        var self = this;

        this.loadAttachments = function (decisionId) {
            return PermitDecision.getAttachments({id: decisionId}).$promise.then(function (attachmentList) {
                for (var i = 0; i < attachmentList.length; i++) {
                    var attachment = attachmentList[i];
                    attachment.hasOrdering = _.isFinite(attachment.orderingNumber);
                }
                return attachmentList;
            });
        };

        this.downloadAttachment = function (decisionId, attachmentId) {
            FormPostService.submitFormUsingBlankTarget(self.getAttachmentUri(decisionId, attachmentId));
        };

        this.getAttachmentUri = function (decisionId, attachmentId) {
            return '/api/v1/decision/' + decisionId + '/attachment' + '/' + attachmentId;
        };

        this.deleteAttachment = function (decisionId, attachmentId) {
            return confirmDelete().then(function () {
                return $http.delete(self.getAttachmentUri(decisionId, attachmentId));
            });
        };

        function confirmDelete() {
            var dialogTitle = $translate.instant('global.dialog.confirmation.title');
            var dialogMessage = $translate.instant('global.dialog.confirmation.text');
            return dialogs.confirm(dialogTitle, dialogMessage).result;
        }
    })
    .service('PermitDecisionAttachmentsModal', function ($uibModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/attachments/attachments.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    decisionAttachments: function (PermitDecisionAttachmentService) {
                        return PermitDecisionAttachmentService.loadAttachments(decisionId);
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $scope, $timeout,
                                 PermitDecision, PermitDecisionAttachmentService,
                                 decisionId, decisionAttachments) {
            var $ctrl = this;

            $ctrl.dropzone = null;

            $ctrl.$onInit = function () {
                $ctrl.activeTabIndex = 0;
                $ctrl.decisionAttachments = decisionAttachments;
                $ctrl.attachmentDescription = '';
                $ctrl.attachmentChanged = false;

                $ctrl.dropzoneConfig = {
                    autoProcessQueue: false,
                    addRemoveLinks: true,
                    maxFiles: 1,
                    maxFilesize: 50, // MiB
                    uploadMultiple: false,
                    url: PermitDecisionAttachmentService.getAttachmentUri(decisionId, ''),
                    paramName: 'file'
                };

                $ctrl.dropzoneEventHandlers = {
                    addedfile: function (file) {
                        $timeout(function () {
                            // trigger digest cycle
                            $ctrl.errors = {};
                        });
                    },
                    success: function (file) {
                        $ctrl.dropzone.removeFile(file);

                        $timeout(function () {
                            $ctrl.errors = {};
                            $ctrl.attachmentDescription = '';
                            reloadAttachments();
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

                $ctrl.errors = {
                    incompatibleFileType: false
                };
            };

            $ctrl.save = function () {
                var ordering = _.chain($ctrl.decisionAttachments).filter('hasOrdering').map('id').value();

                PermitDecision.updateAttachmentOrder({id: decisionId}, ordering).$promise.finally(function () {
                    $uibModalInstance.close();
                });
            };

            // UPLOAD

            $ctrl.uploadDisabled = function (form) {
                return form.$invalid || !$ctrl.dropzone || $ctrl.dropzone.getAcceptedFiles().length < 1;
            };

            $ctrl.uploadAttachment = function () {
                $ctrl.dropzone.processQueue();
            };

            $ctrl.downloadAttachment = function (id) {
                PermitDecisionAttachmentService.downloadAttachment(decisionId, id);
            };

            $ctrl.deleteAttachment = function (id) {
                PermitDecisionAttachmentService.deleteAttachment(decisionId, id).then(function () {
                    reloadAttachments();
                });
            };

            $ctrl.addDefaultMooseAttachment = function () {
                PermitDecision.addDefaultMooseAttachment({id: decisionId}).$promise.then(function () {
                    reloadAttachments();
                });
            };

            // ORDER

            $ctrl.toggleDecisionNumber = function (attachment) {
                attachment.hasOrdering = !attachment.hasOrdering;
            };

            $ctrl.moveAttachment = function (id, delta) {
                moveArrayElement($ctrl.decisionAttachments, id, delta);
                $ctrl.attachmentChanged = true;
            };

            function moveArrayElement(arr, id, delta) {
                var ndx = _.findIndex(arr, 'id', id);

                if (ndx !== -1) {
                    var tmp = arr.splice(ndx, 1);
                    arr.splice(ndx + delta, 0, tmp[0]);
                    return true;
                }

                return false;
            }

            function reloadAttachments() {
                PermitDecisionAttachmentService.loadAttachments(decisionId).then(function (res) {
                    $ctrl.decisionAttachments = res;
                    $ctrl.attachmentChanged = false;
                    $ctrl.activeTabIndex = 0;
                });
            }
        }
    });
