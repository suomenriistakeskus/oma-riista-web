'use strict';

angular.module('app.harvestpermit.decision.revisions', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.revisions', {
            url: '/revisions',
            templateUrl: 'harvestpermit/decision/revisions/revisions.html',
            controllerAs: '$ctrl',
            resolve: {
                decision: function (PermitDecision, decisionId) {
                    return PermitDecision.get({id: decisionId}).$promise;
                },
                revisions: function (PermitDecision, decisionId) {
                    return PermitDecision.getRevisions({id: decisionId}).$promise;
                },
                decisionSpeciesAmounts: function (PermitDecisionSpecies, decisionId) {
                    return PermitDecisionSpecies.getSpecies({decisionId: decisionId}).$promise;
                },
                diaryParameters: function (GameDiaryParameters) {
                    return GameDiaryParameters.query().$promise;
                },
                decisionAttachments: function (PermitDecision, decisionId) {
                    return PermitDecision.getAttachments({id: decisionId}).$promise;
                }

            },
            controller: function ($state, PermitDecision, NotificationService,
                                  FormPostService, GameSpeciesCodes, PermitDecisionAdditionalAttachmentsModal,
                                  decisionId, decision, revisions, decisionSpeciesAmounts, diaryParameters, decisionAttachments) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.decision = decision;
                    $ctrl.revisions = _.orderBy(revisions, ['id'], ['desc']);
                    $ctrl.permits = decision.permits || [];
                    $ctrl.reversePermits = decision.permitTypeCode === '346'; // Reverse permits for annually renewed
                    $ctrl.mooselikeSpeciesCodes = _.chain(decisionSpeciesAmounts)
                        .map('gameSpeciesCode')
                        .filter(GameSpeciesCodes.isPermitBasedMooselike)
                        .value();
                    $ctrl.showHarvestReports =
                        (decision.harvestPermitCategory === 'BIRD' || decision.harvestPermitCategory === 'MAMMAL') &&
                        decision.status === 'PUBLISHED' &&
                        decision.decisionType === 'HARVEST_PERMIT' &&
                        decision.grantStatus !== 'REJECTED';
                    $ctrl.contactPersonReceivers = [];
                    $ctrl.otherReceivers = [];
                    $ctrl.activeRevision = null;

                    $ctrl.attachments = decisionAttachments;
                    $ctrl.attachmentDownloadUrl = '/api/v1/decision/' + decisionId + '/attachment/';
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
                    PermitDecisionAdditionalAttachmentsModal.open(decisionId).then(function () {
                        $state.reload();
                    });
                };

                function filterReceivers(revision, type) {
                    return _.filter(revision.receivers, _.matchesProperty('receiverType', type));
                }

                $ctrl.togglePosted = function (posted) {
                    var m = posted ? PermitDecision.updatePosted : PermitDecision.updateNotPosted;
                    m({id: decisionId, revisionId: $ctrl.activeRevision.id}).$promise.then(function (r) {
                        $ctrl.activeRevision.posted = r.posted;
                        $ctrl.activeRevision.postedByMailDate = r.postedByMailDate;
                        $ctrl.activeRevision.postedByMailUsername = r.postedByMailUsername;
                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                };

                $ctrl.getSpeciesName = function (gameSpeciesCode) {
                    return diaryParameters
                        ? diaryParameters.$getGameName(gameSpeciesCode, null)
                        : gameSpeciesCode;
                };

                $ctrl.downloadPdf = function () {
                    if ($ctrl.activeRevision) {
                        FormPostService.submitFormUsingBlankTarget(
                            '/api/v1/decision/' + decisionId + '/revisions/' + $ctrl.activeRevision.id + '/pdf');
                    }
                };

                // Allow invoice downloading only for published decisions since decision status can change
                // back to draft after invoice is generated
                var canDownloadDecisionInvoice = function () {
                    return $ctrl.decision.hasDecisionInvoice
                        && $ctrl.decision.status === 'PUBLISHED'
                        && $ctrl.activeRevision
                        && !!$ctrl.activeRevision.publishDate;
                };

                $ctrl.getProcessingInvoiceDownloadStatus = function () {
                    if (!canDownloadDecisionInvoice()) {
                        return 'NOT_AVAILABLE';
                    }

                    if (!$ctrl.activeRevision.postalByMail) {
                        return 'DISABLED_BY_ELECTRONIC_INVOICING';
                    }

                    return 'AVAILABLE';
                };

                $ctrl.downloadProcessingInvoice = function () {
                    window.open('/api/v1/decision/' + decisionId + '/invoice/processing');
                };

                $ctrl.downloadHarvestInvoice = function (gameSpeciesCode) {
                    window.open('/api/v1/decision/' + decisionId + '/invoice/harvest/' + gameSpeciesCode);
                };

                $ctrl.downloadPermitHarvestReport = function () {
                    window.open('/api/v1/decision/' + decisionId + '/permit-harvest-report');
                };

                $ctrl.moveToInvoices = function () {
                    $state.go('jht.invoice.search', {applicationNumber: decision.applicationNumber});
                };

                $ctrl.moveToOnlyPermit = function () {
                    if ($ctrl.permits.length === 1) {
                        $ctrl.moveToPermit($ctrl.permits[0].id);
                    }
                };

                $ctrl.moveToPermit = function (id) {
                    $state.go('permitmanagement.dashboard', {permitId: id});
                };
            }
        });
    })
    .component('permitDecisionRevisionStateIcon', {
        templateUrl: 'harvestpermit/decision/revisions/revision-state-icon.html',
        bindings: {
            iconType: '@',
            iconEnabled: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.iconClasses = {};
                $ctrl.iconClasses['fa-' + $ctrl.iconType] = true;
            };
        }
    })

    .service('PermitDecisionAdditionalAttachmentsModal', function ($uibModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/revisions/additional-attachments.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    additionalAttachments: function (PermitDecisionAttachmentService) {
                        return PermitDecisionAttachmentService.loadAdditionalAttachments(decisionId);
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $scope, $timeout,
                                 PermitDecision, PermitDecisionAttachmentService,
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
                    url: PermitDecisionAttachmentService.getAdditionalAttachmentUri(decisionId, ''),
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
                PermitDecisionAttachmentService.downloadAttachment(decisionId, id);
            };

            $ctrl.deleteAttachment = function (id) {
                PermitDecisionAttachmentService.deleteAttachment(decisionId, id).then(function () {
                    reloadAttachments();
                });
            };

            function reloadAttachments() {
                PermitDecisionAttachmentService.loadAdditionalAttachments(decisionId).then(function (res) {
                    $ctrl.decisionAttachments = res;
                    $ctrl.attachmentChanged = false;
                    $ctrl.activeTabIndex = 0;
                });
            }
        }
    });
