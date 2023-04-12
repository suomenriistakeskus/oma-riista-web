'use strict';

angular.module('app.harvestpermit.decision.document', [])

    .constant('PermitDecisionSection', {
        APPLICATION: 'APPLICATION',
        APPLICATION_REASONING: 'APPLICATION_REASONING',
        PROCESSING: 'PROCESSING',
        DECISION: 'DECISION',
        DECISION_EXTRA: 'DECISION_EXTRA',
        RESTRICTION: 'RESTRICTION',
        RESTRICTION_EXTRA: 'RESTRICTION_EXTRA',
        DECISION_REASONING: 'DECISION_REASONING',
        EXECUTION: 'EXECUTION',
        LEGAL_ADVICE: 'LEGAL_ADVICE',
        NOTIFICATION_OBLIGATION: 'NOTIFICATION_OBLIGATION',
        APPEAL: 'APPEAL',
        ADDITIONAL_INFO: 'ADDITIONAL_INFO',
        DELIVERY: 'DELIVERY',
        PAYMENT: 'PAYMENT',
        ADMINISTRATIVE_COURT: 'ADMINISTRATIVE_COURT',
        ATTACHMENTS: 'ATTACHMENTS'
    })

    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document', {
            url: '/document',
            templateUrl: 'harvestpermit/decision/document/document.html',
            controller: 'PermitDecisionDocumentController',
            controllerAs: '$ctrl',
            abstract: true,
            hideFooter: false,
            resolve: {
                decision: function (PermitDecision, decisionId) {
                    return PermitDecision.get({id: decisionId}).$promise;
                },
                reference: function (PermitDecision, decisionId) {
                    return PermitDecision.getReference({id: decisionId}).$promise.then(function (res) {
                        return !!res.reference ? res.reference : null;
                    });
                }
            }
        });
    })

    .service('RefreshDecisionStateService', function ($state) {
        this.refresh = function () {
            $state.reload('jht.decision.document');
        };
    })

    .service('PermitDecisionUtils', function (LocalStorageService, PermitDecision, PermitDecisionSection) {
        var self = this;

        self.isHandler = function (decision) {
            return decision && decision.userIsHandler;
        };

        self.canEditContent = function (decision, sectionId) {
            if (sectionId === PermitDecisionSection.DECISION_EXTRA) {
                sectionId = PermitDecisionSection.DECISION;
            } else if (sectionId === PermitDecisionSection.RESTRICTION_EXTRA) {
                sectionId = PermitDecisionSection.RESTRICTION;
            }

            return self.isHandler(decision) && decision.status === 'DRAFT' &&
                decision.completeStatus[documentFieldForSection(sectionId)] === false;
        };

        self.getSectionContent = function (decision, sectionId) {
            var doc = decision.document;
            if (sectionId === PermitDecisionSection.RESTRICTION) {
                return _([doc.restriction, doc.restrictionExtra]).filter().join('\n');
            } else if (sectionId === PermitDecisionSection.DECISION) {
                return _([doc.decision, doc.decisionExtra]).filter().join('\n');
            }
            return doc[documentFieldForSection(sectionId)];
        };

        self.canBeCompleted = function (decision, sectionId) {
            if (self.sectionIsNotApplicable(decision, sectionId)) {
                return true;
            }

            var isSwedish = decision.locale === 'sv_FI';
            var byMail = decision.deliveryByMail;

            var realSectionKey = sectionId === PermitDecisionSection.RESTRICTION
                ? PermitDecisionSection.RESTRICTION_EXTRA
                : sectionId;

            var documentField = documentFieldForSection(realSectionKey);
            var sectionTextLength = _.size(decision.document[documentField]) || 0;

            return sectionTextLength >= getMinimumLengthToCompleteSection(sectionId, isSwedish, byMail);
        };

        var notBlankRequired = [PermitDecisionSection.DECISION, PermitDecisionSection.DECISION_REASONING,
            PermitDecisionSection.RESTRICTION, PermitDecisionSection.EXECUTION, PermitDecisionSection.APPEAL,
            PermitDecisionSection.ADMINISTRATIVE_COURT
        ];

        function getMinimumLengthToCompleteSection(sectionId, isSwedish, byMail) {
            if (_.includes(notBlankRequired, sectionId)) {
                return 1;
            }

            switch (sectionId) {
                case PermitDecisionSection.LEGAL_ADVICE:
                    return (isSwedish ? 67 : 66);
                case PermitDecisionSection.ADDITIONAL_INFO:
                    return (isSwedish ? (byMail ? 288 : 302) : (byMail ? 265 : 282));
            }

            return 0;
        }

        function documentFieldForSection(sectionId){
            return _.camelCase(sectionId);
        }

        self.sectionIsNotApplicable = function (decision, sectionId) {
            var amendmentPermit = decision.permitTypeCode === '190';
            var notPermitDecision = decision.decisionType !== 'HARVEST_PERMIT';
            var disabilityPermit = decision.permitTypeCode === '710';

            switch (sectionId) {
                case PermitDecisionSection.RESTRICTION:
                    return amendmentPermit || notPermitDecision;
                case PermitDecisionSection.EXECUTION:
                    return notPermitDecision;
                case PermitDecisionSection.DELIVERY:
                    return disabilityPermit;
            }

            return false;
        };

        self.reloadSectionContent = function (sectionId, decisionId) {
            return PermitDecision.getDocument({id: decisionId}).$promise.then(function (doc) {
                return doc[documentFieldForSection(sectionId)] || '';
            });
        };

        self.getReferenceContent = function (reference, sectionId) {
            return reference && reference.document
                ? (reference.document[documentFieldForSection(sectionId)] || '')
                : null;
        };

        self.canLockDecision = function (decision) {
            return self.isHandler(decision) &&
                decision.applicationStatus !== 'AMENDING' &&
                self.allEditableFieldsComplete(decision);
        };

        self.canUnlockDecision = function (decision) {
            return this.isHandler(decision) && (decision.status === 'LOCKED' ||
                decision.status === 'PUBLISHED');
        };

        self.hideSectionForRejected = function (decision, sectionId) {
            return decision.grantStatus === 'REJECTED' &&
                (sectionId === PermitDecisionSection.RESTRICTION ||
                    sectionId === PermitDecisionSection.EXECUTION);
        };

        self.allEditableFieldsComplete = function (decision) {
            return _.every(PermitDecisionSection, function (sectionId) {
                return self.hideSectionForRejected(decision, sectionId)
                    || _.endsWith(sectionId, "_EXTRA")
                    || self.hideDeliveryForDisability(decision.permitTypeCode, sectionId)
                    || self.isSectionComplete(decision, sectionId);
            });
        };

        self.isSectionComplete = function(decision, sectionId){
            return sectionId === PermitDecisionSection.APPLICATION ||
                _.get(decision.completeStatus, documentFieldForSection(sectionId), false);
        };

        self.toggleComplete = function (decision, sectionId, value) {
            var completeStatus = decision.completeStatus;
            var documentField = documentFieldForSection(sectionId);

            PermitDecision.updateCompleteStatus({id: decision.id}, {
                sectionId: sectionId,
                complete: value
            }).$promise.then(function () {
                completeStatus[documentField] = value;
            });
        };

        self.updateGrantStatus = function(decision, grantStatus) {
            PermitDecision.updateGrantStatus({id: decision.id}, {
                grantStatus: grantStatus
            }).$promise.then(function () {
                decision.grantStatus = grantStatus;
            });
        };

        this.hideDeliveryForDisability = function (permitTypeCode, sectionId) {
            // 710 = disability permit
            return permitTypeCode === '710' && sectionId === PermitDecisionSection.DELIVERY;
        };
    })

    .controller('PermitDecisionDocumentController',
        function (PermitDecisionSection, PermitDecision,
                  PermitDecisionUtils, RefreshDecisionStateService,
                  PermitDecisionChangeReferenceModal,
                  PermitDecisionPublishSettingsModal,
                  $state, FormPostService, NotificationService, TranslatedBlockUI, ReasonAsker,
                  dialogs, decisionId, decision, reference) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.previewEnabled = decision.status !== 'DRAFT';
                $ctrl.sectionIds = _.values(PermitDecisionSection);
                $ctrl.decision = decision;
                $ctrl.reference = reference;
            };

            $ctrl.printDraftPdf = function () {
                FormPostService.submitFormUsingBlankTarget('/api/v1/decision/' + decisionId + '/print/pdf');
            };

            $ctrl.isSectionVisible = function (sectionId) {
                return !PermitDecisionUtils.hideSectionForRejected(decision, sectionId);
            };

            $ctrl.isHandler = function () {
                return PermitDecisionUtils.isHandler(decision);
            };

            $ctrl.canLockDecision = function () {
                return PermitDecisionUtils.canLockDecision(decision);
            };

            $ctrl.canUnlockDecision = function () {
                return PermitDecisionUtils.canUnlockDecision(decision);
            };

            $ctrl.lockDecision = function () {
                PermitDecisionPublishSettingsModal.open(decisionId).then(function () {
                    lockDecision();
                });
            };

            $ctrl.unlockDecision = function () {
                ReasonAsker.openModal({
                    titleKey: 'decision.unlockConfirm.title',
                    messageKey: 'decision.unlockConfirm.message'
                }).then(function (reason) {
                    PermitDecision.unlock({id: decisionId, unlockReason: reason}).$promise.then(function () {
                        NotificationService.showDefaultSuccess();
                        RefreshDecisionStateService.refresh();
                    });
                });
            };

            function lockDecision() {
                TranslatedBlockUI.start('global.block.wait');

                PermitDecision.lock({id: decisionId}).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $state.go('jht.decision.revisions', {decisionId: decisionId});

                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            }

            $ctrl.changeReference = function () {
                PermitDecisionChangeReferenceModal.open($ctrl.decision).then(function () {
                    RefreshDecisionStateService.refresh();
                });
            };

            $ctrl.previewUrl = function () {
                var activeSectionId = convertStateNameToSectionId() || '';

                return '/api/v1/decision/' + decisionId + '/print/html?sectionId=' + activeSectionId;
            };

            function convertStateNameToSectionId() {
                var currentStateName = $state.current.name;

                return _.chain(_.values(PermitDecisionSection)).filter(function (sectionId) {
                    return _.endsWith(currentStateName, _.kebabCase(sectionId));
                }).head().value();
            }

        })

    .component('permitDecisionDocumentNav', {
        templateUrl: 'harvestpermit/decision/document/document-nav.html',
        bindings: {
            decision: '<',
            publishDate: '<',
            grantStatus: '<'
        },
        controller: function ($state, PermitDecisionSection, PermitDecisionUtils) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionIds = _.filter(_.values(PermitDecisionSection), function (value) {
                    // No separate navigation for section extra fields
                    return !_.endsWith(value, "_EXTRA");
                });
            };

            $ctrl.focus = function (sectionId) {
                $state.go($ctrl.getStateName(sectionId));
            };

            $ctrl.isComplete = function (sectionId) {
                return PermitDecisionUtils.isSectionComplete($ctrl.decision, sectionId);
            };

            $ctrl.isSectionVisible = function (sectionId) {
                return !PermitDecisionUtils.hideSectionForRejected($ctrl.decision, sectionId) &&
                    !PermitDecisionUtils.hideDeliveryForDisability($ctrl.decision.permitTypeCode, sectionId);
            };

            $ctrl.getStateName = function (sectionId) {
                return 'jht.decision.document.' + _.kebabCase(sectionId);
            };
        }
    })

    .component('permitDecisionSectionHeader', {
        templateUrl: 'harvestpermit/decision/document/section-header.html',
        bindings: {
            decision: '<',
            sectionId: '<',
            denyComplete: '&'
        },
        controllerAs: '$ctrl'
    })

    .component('permitDecisionSectionHeaderButtons', {
        templateUrl: 'harvestpermit/decision/document/section-header-buttons.html',
        bindings: {
            decision: '<',
            sectionId: '<',
            denyComplete: '&'
        },
        controllerAs: '$ctrl',
        controller: function (PermitDecisionUtils, PermitDecisionSection) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.approved = $ctrl.decision.grantStatus === 'UNCHANGED';
                $ctrl.fieldValue = $ctrl.isComplete();
            };

            $ctrl.isComplete = function () {
                return PermitDecisionUtils.isSectionComplete($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.toggleComplete = function () {
                PermitDecisionUtils.toggleComplete($ctrl.decision, $ctrl.sectionId, $ctrl.fieldValue);
            };

            $ctrl.canBeCompleted = function () {
                return !$ctrl.denyComplete() &&
                    PermitDecisionUtils.canBeCompleted($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.isHandler = function () {
                return PermitDecisionUtils.isHandler($ctrl.decision);
            };

            $ctrl.showButtons = function () {
                return $ctrl.isHandler() && $ctrl.sectionId !== PermitDecisionSection.APPLICATION;
            };

        }
    })

    .component('permitDecisionSectionHeaderWithGrantStatus', {
        templateUrl: 'harvestpermit/decision/document/decision-section-header-with-grant-status.html',
        bindings: {
            decision: '<',
            sectionId: '<',
            denyComplete: '&'
        },
        controllerAs: '$ctrl',
        controller: function (PermitDecisionUtils) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.approved = $ctrl.decision.grantStatus === 'UNCHANGED';
            };

            $ctrl.isHandler = function () {
                return PermitDecisionUtils.isHandler($ctrl.decision);
            };

            $ctrl.isComplete = function () {
                return PermitDecisionUtils.isSectionComplete($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.toggleGrantStatus = function () {
                var status = $ctrl.approved ? 'UNCHANGED' : 'REJECTED';
                PermitDecisionUtils.updateGrantStatus($ctrl.decision, status);
            };
        }
    });
