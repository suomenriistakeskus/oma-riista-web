'use strict';

angular.module('app.harvestpermit.decision.document', [])

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

    .constant('PermitDecisionSectionList', [
        'application', 'applicationReasoning', 'processing', 'decision', 'restriction',
        'decisionReasoning', 'execution', 'legalAdvice', 'notificationObligation', 'appeal', 'additionalInfo',
        'delivery', 'payment', 'administrativeCourt', 'attachments'
    ])

    .service('RefreshDecisionStateService', function ($state) {
        this.refresh = function () {
            $state.reload('jht.decision.document');
        };
    })

    .service('PermitDecisionUtils', function (LocalStorageService, PermitDecision, PermitDecisionSectionList) {
        this.isHandler = function (decision) {
            return decision && decision.userIsHandler;
        };

        this.canEditContent = function (decision, sectionId) {
            if (sectionId === 'decisionExtra') {
                sectionId = 'decision';
            } else if (sectionId === 'restrictionExtra') {
                sectionId = 'restriction';
            }

            return this.isHandler(decision) && decision.status === 'DRAFT' &&
                decision.completeStatus[sectionId] === false;
        };

        this.getSectionContent = function (decision, sectionId) {
            var doc = decision.document;
            if (sectionId === 'restriction') {
                return _([doc.restriction, doc.restrictionExtra]).filter().join('\n');
            } else if (sectionId === 'decision') {
                return _([doc.decision, doc.decisionExtra]).filter().join('\n');
            }
            return doc[sectionId];
        };

        this.canBeCompleted = function (decision, sectionId) {
            if (this.sectionIsNotApplicable(decision, sectionId)) {
                return true;
            }

            var isSwedish = decision.locale === 'sv_FI';
            var byMail = decision.deliveryByMail;

            var realSectionKey = sectionId === 'restriction' ? 'restrictionExtra' : sectionId;
            var sectionTextLength = _.size(decision.document[realSectionKey]) || 0;

            return sectionTextLength >= getMinimumLengthToCompleteSection(sectionId, isSwedish, byMail);
        };

        var notBlankRequired = ['decision', 'decisionReasoning', 'restriction', 'execution', 'appeal',
            'administrativeCourt'
        ];

        function getMinimumLengthToCompleteSection(sectionId, isSwedish, byMail) {
            if (_.includes(notBlankRequired, sectionId)) {
                return 1;
            }

            switch (sectionId) {
                case 'legalAdvice':
                    return (isSwedish ? 67 : 66);
                case 'additionalInfo':
                    return (isSwedish ? (byMail ? 288 : 302) : (byMail ? 265 : 282));
                case 'delivery':
                    return (isSwedish ? 24 : 18);
            }

            return 0;
        }

        this.sectionIsNotApplicable = function (decision, sectionId) {
            var amendmentPermit = decision.permitTypeCode === '190';
            var notPermitDecision = decision.decisionType !== 'HARVEST_PERMIT';

            switch (sectionId) {
                case 'restriction':
                    return amendmentPermit || notPermitDecision;
                case 'execution':
                    return notPermitDecision;
            }

            return false;
        };

        this.reloadSectionContent = function (sectionId, decisionId) {
            return PermitDecision.getDocument({id: decisionId}).$promise.then(function (doc) {
                return doc[sectionId] || '';
            });
        };

        this.getReferenceContent = function (reference, sectionId) {
            return reference && reference.document
                ? (reference.document[sectionId] || '')
                : null;
        };

        this.canLockDecision = function (decision) {
            return this.isHandler(decision) &&
                decision.applicationStatus !== 'AMENDING' &&
                this.allEditableFieldsComplete(decision);
        };

        this.canUnlockDecision = function (decision) {
            return this.isHandler(decision) && (decision.status === 'LOCKED' ||
                decision.status === 'PUBLISHED');
        };

        this.hideSectionForRejected = function (decision, sectionId) {
            return decision.grantStatus === 'REJECTED' &&
                (sectionId === 'restriction' ||
                    sectionId === 'execution');
        };

        this.allEditableFieldsComplete = function (decision) {
            var self = this;

            return _.every(PermitDecisionSectionList, function (sectionId) {
                return sectionId === 'application' ||
                    self.hideSectionForRejected(decision, sectionId) ||
                    decision.completeStatus[sectionId];
            });
        };

        this.toggleComplete = function (decision, sectionId) {
            var completeStatus = decision.completeStatus;
            var completeValue = completeStatus[sectionId];

            PermitDecision.updateCompleteStatus({id: decision.id}, {
                sectionId: sectionId,
                complete: completeValue
            }).$promise.then(function () {
                completeStatus[sectionId] = completeValue;
            });
        };
    })

    .controller('PermitDecisionDocumentController',
        function (PermitDecisionSectionList, PermitDecision,
                  PermitDecisionUtils, RefreshDecisionStateService,
                  PermitDecisionChangeReferenceModal,
                  PermitDecisionPublishSettingsModal,
                  $state, FormPostService, NotificationService, TranslatedBlockUI, ReasonAsker,
                  dialogs, decisionId, decision, reference) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.previewEnabled = decision.status !== 'DRAFT';
                $ctrl.sectionIds = PermitDecisionSectionList;
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
                    titleKey: 'harvestpermit.decision.unlockConfirm.title',
                    messageKey: 'harvestpermit.decision.unlockConfirm.message'
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

                return _.chain(PermitDecisionSectionList).filter(function (sectionId) {
                    return _.endsWith(currentStateName, _.kebabCase(sectionId));
                }).head().value();
            }

        })

    .component('permitDecisionDocumentNav', {
        templateUrl: 'harvestpermit/decision/document/document-nav.html',
        bindings: {
            completeStatus: '<',
            publishDate: '<',
            grantStatus: '<'
        },
        controller: function ($state, PermitDecisionSectionList) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionIds = PermitDecisionSectionList;
            };

            $ctrl.focus = function (sectionId) {
                $state.go($ctrl.getStateName(sectionId));
            };

            $ctrl.isComplete = function (sectionId) {
                return sectionId === 'application' || _.get($ctrl.completeStatus, sectionId, false);
            };

            $ctrl.isSectionVisible = function (sectionId) {
                return !($ctrl.grantStatus === 'REJECTED' && (sectionId === 'restriction' || sectionId === 'execution'));
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
        controllerAs: '$ctrl',
        controller: function (PermitDecisionUtils) {
            var $ctrl = this;

            $ctrl.isComplete = function () {
                return _.get($ctrl.decision.completeStatus, $ctrl.sectionId, false);
            };

            $ctrl.toggleComplete = function () {
                PermitDecisionUtils.toggleComplete($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.canBeCompleted = function () {
                return !$ctrl.denyComplete() &&
                    PermitDecisionUtils.canBeCompleted($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.isHandler = function () {
                return PermitDecisionUtils.isHandler($ctrl.decision);
            };
        }
    });
