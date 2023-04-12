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
                requestLinks: function (PermitDecision, decisionId) {
                    return PermitDecision.listInformationRequestLinks({id: decisionId}).$promise;
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
            controller: function ($state, PermitDecision, NotificationService, ActiveRoleService, ModeratorPrivileges,
                                  FormPostService, GameSpeciesCodes, PermitDecisionAdditionalAttachmentsModal,
                                  PermitDecisionAnnualRenewalModal,
                                  PermitDecisionSendInformationRequestLinkModal,
                                  PermitDecisionDeactivateInformationRequestLinkModal, PermitTypes,
                                  decisionId, decision, revisions, requestLinks, decisionSpeciesAmounts,
                                  diaryParameters, decisionAttachments) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.decision = decision;
                    $ctrl.revisions = _.orderBy(revisions, ['id'], ['desc']);
                    $ctrl.permits = decision.permits || [];
                    $ctrl.isAnnualPermit = decision.permitTypeCode === PermitTypes.ANNUAL_UNPROTECTED_BIRD;
                    $ctrl.requestLinks = _.orderBy(requestLinks, ['id'], ['desc']);

                    $ctrl.mooselikeSpeciesCodes = _.chain(decisionSpeciesAmounts)
                        .map('gameSpeciesCode')
                        .filter(GameSpeciesCodes.isPermitBasedMooselike)
                        .value();
                    $ctrl.showHarvestReports =
                        (decision.harvestPermitCategory === 'BIRD' || decision.harvestPermitCategory === 'MAMMAL') &&
                        !$ctrl.isAnnualPermit && // Harvest reports for annual permits in separate dialog
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

                $ctrl.openRenewalDialog = function () {
                    PermitDecisionAnnualRenewalModal.open($ctrl.decision).then(function () {
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
                $ctrl.openInformationRequestLinkSendingModal = function () {
                    PermitDecisionSendInformationRequestLinkModal.open(decision).then(function () {
                        $state.reload();
                    });
                };
                $ctrl.openInformationRequestLinkDeactivationModal = function (link) {
                    PermitDecisionDeactivateInformationRequestLinkModal.open(link, decisionId).then(function () {
                        $state.reload();
                    });
                };
                $ctrl.hasInformationRequestLinkHandlingPermission = function () {
                    return ActiveRoleService.isPrivilegedModerator(ModeratorPrivileges.informationRequestLinkHandler);
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
    })
    .service('PermitDecisionSendInformationRequestLinkModal', function ($uibModal) {
        this.open = function (decision) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/revisions/send-information-request-link.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decision: decision,
                }
            }).result;
        };

        function ModalController($uibModalInstance, $scope, $translate, $timeout,
                                 PermitDecision, PermitDecisionAttachmentService,
                                 decision) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.fields = {
                    recipientName: null,
                    recipientEmail: null,
                    linkType: null,
                    title: $translate.instant('harvestpermit.wizard.summary.permitType.' + decision.permitTypeCode),
                    description: null
                };
                $ctrl.decision = decision;

                $ctrl.types = [
                    'APPLICATION',
                    'DECISION',
                    'APPLICATION_AND_DECISION'
                ];
            };

            $ctrl.selectLinkType = function (type) {
                $ctrl.fields.linkType = type;
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.canSubmit = function () {
                return $ctrl.fields.recipientName &&
                    $ctrl.fields.recipientEmail;
            };

            $ctrl.submit = function () {
                PermitDecision.createInformationRequestLink({id: decision.id}, $ctrl.fields).$promise.then(function () {
                    $uibModalInstance.close();
                }, function (err) {
                    $uibModalInstance.dismiss(err);
                });
            };
        }
    })
    .service('PermitDecisionDeactivateInformationRequestLinkModal', function ($uibModal) {
        this.open = function (link, decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/revisions/deactivate-information-request-link.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    linkId: _.constant(link.id),
                }
            }).result;
        };

        function ModalController($uibModalInstance, $scope, $timeout,
                                 PermitDecision, PermitDecisionAttachmentService,
                                 decisionId, linkId) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.fields = {
                    recipientName: null,
                    recipientEmail: null,
                    description: null
                };
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.deactivate = function () {
                PermitDecision.deleteInformationRequestLink({
                    id: decisionId,
                    linkId: linkId
                }, $ctrl.fields).$promise.then(function () {
                    $uibModalInstance.close();
                }, function (err) {
                    $uibModalInstance.dismiss(err);
                });
            };
        }
    })
    .service('PermitDecisionAnnualRenewalModal', function ($uibModal) {
        this.open = function (decision) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/revisions/annual-renewal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decision.id),
                    existingPermits: _.constant(decision.permits),
                    isRenewable: function (PermitDecisionRenewal) {
                        return PermitDecisionRenewal.isRenewable({id: decision.id}).$promise.then(function (value) {
                            return value.value;
                        });
                    },
                }
            }).result;
        };

        function ModalController($uibModalInstance, $scope, $timeout, $translate, AnnualPermitPdfUrl,
                                 ConfirmationDialogService, FormPostService, NotificationService, PermitDecision,
                                 PermitDecisionRenewal, decisionId, existingPermits, isRenewable) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.decisionId = decisionId;
                $ctrl.existingPermits = existingPermits;
                $ctrl.isRenewable = isRenewable;
                $ctrl.permitYear = _.chain(existingPermits)
                    .map(function (p) {
                        return _.split(p.permitNumber, '-', 1)[0];
                    })
                    .map(_.parseInt)
                    .max()
                    .value() + 1;
                $ctrl.createdPermit = null;
            };

            $ctrl.close = function () {
                if (!!$ctrl.createdPermit) {
                    $uibModalInstance.close($ctrl.createdPermit);
                } else {
                    $uibModalInstance.dismiss('cancel');
                }
            };

            $ctrl.renewPermit = function () {
                var title = $translate.instant('decision.annualRenewal.createForNextYear');
                var body = $translate.instant('decision.annualRenewal.confirmation');
                ConfirmationDialogService.showConfirmationDialogWithPrimaryAccept(title, body).then(function () {
                    PermitDecisionRenewal.createPermitForHuntingYear({
                        id: decisionId,
                        permitYear: $ctrl.permitYear
                    }, {}).$promise.then(function (permit) {
                        NotificationService.showMessage('decision.annualRenewal.permitCreated', 'success');
                        $ctrl.createdPermit = permit;
                        reloadPermits();
                    }, NotificationService.showDefaultFailure);
                });
            };

            $ctrl.printPdf = function (permit) {
                FormPostService.submitFormUsingBlankTarget(AnnualPermitPdfUrl.getRenewalPdf(permit.id));
            };

            $ctrl.printHarvestReportForms = function (permit) {
                FormPostService.submitFormUsingBlankTarget(AnnualPermitPdfUrl.getHarvestReportPdf(permit.id));
            };

            function reloadPermits() {
                PermitDecision.get({id: $ctrl.decisionId}).$promise.then(function (decision) {
                    $ctrl.existingPermits = decision.permits;
                });
                PermitDecisionRenewal.isRenewable({id: $ctrl.decisionId}).$promise.then(function (value) {
                    $ctrl.isRenewable = value.value;
                });
            }
        }
    });
