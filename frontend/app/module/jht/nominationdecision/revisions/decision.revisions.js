'use strict';

angular.module('app.jht.nominationdecision.revisions', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.nominationdecision.revisions', {
            url: '/revisions',
            templateUrl: 'jht/nominationdecision/revisions/revisions.html',
            controllerAs: '$ctrl',
            resolve: {
                decision: function (NominationDecision, decisionId) {
                    return NominationDecision.get({id: decisionId}).$promise;
                },
                revisions: function (NominationDecision, decisionId) {
                    return NominationDecision.getRevisions({id: decisionId}).$promise;
                },
                decisionAttachments: function (NominationDecision, decisionId) {
                    return NominationDecision.getAttachments({id: decisionId}).$promise;
                }

            },
            controller: function ($state, NominationDecision, NotificationService,
                                  FormPostService, NominationDecisionAdditionalAttachmentsModal,
                                  decisionId, decision, revisions, decisionAttachments) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.decision = decision;
                    $ctrl.revisions = _.orderBy(revisions, ['id'], ['desc']);
                    $ctrl.contactPersonReceivers = [];
                    $ctrl.otherReceivers = [];
                    $ctrl.activeRevision = null;

                    $ctrl.attachments = decisionAttachments;
                    $ctrl.attachmentDownloadUrl = '/api/v1/nominationdecision/' + decisionId + '/attachment/';

                };

                $ctrl.onActiveRevisionChanged = function (revision) {
                    $ctrl.activeRevision = revision;
                    $ctrl.contactPersonReceivers = filterReceivers($ctrl.activeRevision, 'CONTACT_PERSON');
                    $ctrl.otherReceivers = filterReceivers($ctrl.activeRevision, 'OTHER');
                    $ctrl.allowEditOfAdditionalAttachments =
                        $ctrl.activeRevision.latest === true &&
                        $ctrl.decision.status === 'PUBLISHED';
                };

                $ctrl.openAttachmentEditor = function () {
                    NominationDecisionAdditionalAttachmentsModal.open(decisionId).then(function () {
                        $state.reload();
                    });
                };

                function filterReceivers(revision, type) {
                    return _.filter(revision.receivers, _.matchesProperty('receiverType', type));
                }

                $ctrl.togglePosted = function (posted) {
                    var m = posted ? NominationDecision.updatePosted : NominationDecision.updateNotPosted;
                    m({id: decisionId, revisionId: $ctrl.activeRevision.id}).$promise.then(function (r) {
                        $ctrl.activeRevision.posted = r.posted;
                        $ctrl.activeRevision.postedByMailDate = r.postedByMailDate;
                        $ctrl.activeRevision.postedByMailUsername = r.postedByMailUsername;
                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                };

                $ctrl.downloadPdf = function () {
                    if ($ctrl.activeRevision) {
                        FormPostService.submitFormUsingBlankTarget(
                            '/api/v1/nominationdecision/' + decisionId + '/revisions/' + $ctrl.activeRevision.id + '/pdf');
                    }
                };
            }
        });
    })
    .service('NominationDecisionAdditionalAttachmentsModal', function ($uibModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/revisions/additional-attachments.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    additionalAttachments: function (NominationDecisionAttachmentService) {
                        return NominationDecisionAttachmentService.loadAdditionalAttachments(decisionId);
                    }
                }
            }).result;
        };

        // TODO: Refactor into common controller if this approach is feasible for end users
        function ModalController($uibModalInstance, $timeout, NominationDecisionAttachmentService,
                                 decisionId, additionalAttachments) {
            var $ctrl = this;

            $ctrl.dropzone = null;

            $ctrl.$onInit = function () {
                $ctrl.activeTabIndex = 0;
                $ctrl.decisionAttachments = additionalAttachments;
                $ctrl.attachmentDescription = '';
                $ctrl.attachmentChanged = false;

                $ctrl.dropzoneConfig = {
                    autoProcessQueue: false,
                    addRemoveLinks: true,
                    maxFiles: 1,
                    maxFilesize: 50, // MiB
                    uploadMultiple: false,
                    url: NominationDecisionAttachmentService.getAdditionalAttachmentUri(decisionId, ''),
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
                $uibModalInstance.close();
            };

            // UPLOAD

            $ctrl.uploadDisabled = function (form) {
                return form.$invalid || !$ctrl.dropzone || $ctrl.dropzone.getAcceptedFiles().length < 1;
            };

            $ctrl.uploadAttachment = function () {
                $ctrl.dropzone.processQueue();
                $timeout(function () {
                    reloadAttachments();
                });
            };

            $ctrl.downloadAttachment = function (id) {
                NominationDecisionAttachmentService.downloadAttachment(decisionId, id);
            };

            $ctrl.deleteAttachment = function (id) {
                NominationDecisionAttachmentService.deleteAttachment(decisionId, id).then(function () {
                    reloadAttachments();
                });
            };

            function reloadAttachments() {
                NominationDecisionAttachmentService.loadAdditionalAttachments(decisionId).then(function (res) {
                    $ctrl.decisionAttachments = res;
                    $ctrl.attachmentChanged = false;
                    $ctrl.activeTabIndex = 0;
                });
            }
        }
    });

