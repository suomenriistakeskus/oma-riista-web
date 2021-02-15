'use strict';

angular.module('app.jht.nominationdecision.document', [])

    .constant('NominationDecisionSection', {
        PROPOSAL: 'PROPOSAL',
        PROCESSING: 'PROCESSING',
        DECISION: 'DECISION',
        DECISION_REASONING: 'DECISION_REASONING',
        LEGAL_ADVICE: 'LEGAL_ADVICE',
        APPEAL: 'APPEAL',
        ADDITIONAL_INFO: 'ADDITIONAL_INFO',
        DELIVERY: 'DELIVERY',
        ATTACHMENTS: 'ATTACHMENTS'
    })
    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.nominationdecision.document', {
                url: '/document',
                templateUrl: 'jht/nominationdecision/document/document.html',
                controller: 'NominationDecisionDocumentController',
                controllerAs: '$ctrl',
                abstract: true,
                hideFooter: false,
                resolve: {
                    decision: function (NominationDecision, decisionId) {
                        return NominationDecision.get({id: decisionId}).$promise;
                    },
                    reference: function (NominationDecision, decisionId) {
                        return NominationDecision.getReference({id: decisionId}).$promise.then(function (res) {
                            return res.reference || null;
                        });
                    }
                }
            })
            .state('jht.nominationdecision.document.general', {
                url: '/general',
                templateUrl: 'jht/nominationdecision/document/document.general.html',
                controllerAs: '$ctrl',
                controller: function (NominationDecisionUtils,
                                      NominationDecisionDocumentSettingsModal,
                                      RefreshNominationStateService,
                                      decision) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.decision = decision;
                    };

                    $ctrl.isHandler = function () {
                        return NominationDecisionUtils.isHandler($ctrl.decision);
                    };

                    $ctrl.editDocumentSettings = function () {
                        NominationDecisionDocumentSettingsModal.open($ctrl.decision.id).then(function () {
                            RefreshNominationStateService.refresh();
                        });
                    };
                }
            })

            .state('jht.nominationdecision.document.proposal', {
                url: '/proposal',
                templateUrl: 'jht/nominationdecision/document/document-section.html',
                controllerAs: '$ctrl',
                controller: 'NominationDecisionDocumentSectionController',
                resolve: {
                    sectionId: function (NominationDecisionSection) {
                        return NominationDecisionSection.PROPOSAL;
                    }
                }
            })

            .state('jht.nominationdecision.document.decision', {
                url: '/decision',
                templateUrl: 'jht/nominationdecision/document/document-section.html',
                controllerAs: '$ctrl',
                controller: 'NominationDecisionDocumentSectionController',
                resolve: {
                    sectionId: function (NominationDecisionSection) {
                        return NominationDecisionSection.DECISION;
                    }
                }
            })
            .state('jht.nominationdecision.document.decision-reasoning', {
                url: '/decision-reasoning',
                templateUrl: 'jht/nominationdecision/document/document-section.html',
                controllerAs: '$ctrl',
                controller: 'NominationDecisionDocumentSectionController',
                resolve: {
                    sectionId: function (NominationDecisionSection) {
                        return NominationDecisionSection.DECISION_REASONING;
                    }
                }
            })
            .state('jht.nominationdecision.document.legal-advice', {
                url: '/legal-advice',
                templateUrl: 'jht/nominationdecision/document/document-section.html',
                controllerAs: '$ctrl',
                controller: 'NominationDecisionDocumentSectionController',
                resolve: {
                    sectionId: function (NominationDecisionSection) {
                        return NominationDecisionSection.LEGAL_ADVICE;
                    }
                }
            })
            .state('jht.nominationdecision.document.appeal', {
                url: '/appeal',
                templateUrl: 'jht/nominationdecision/document/document-section.html',
                controllerAs: '$ctrl',
                controller: 'NominationDecisionDocumentSectionController',
                resolve: {
                    sectionId: function (NominationDecisionSection) {
                        return NominationDecisionSection.APPEAL;
                    }
                }
            });
    })

    .controller('NominationDecisionDocumentSectionController', function (NominationDecisionUtils, NotificationService,
                                                                         NominationDecision, NominationDecisionSection,
                                                                         RefreshNominationStateService,
                                                                         decision, reference, decisionId, sectionId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.sectionId = sectionId;
            $ctrl.decision = decision;
            $ctrl.reference = reference;
            $ctrl.sectionContent = NominationDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
        };

        $ctrl.canEditDecision = function () {
            return NominationDecisionUtils.canEditContent(decision, NominationDecisionSection.DECISION);
        };

        $ctrl.regenerateApplicationSection = function () {
            NominationDecision.generateAndPersistText({
                id: decisionId,
                sectionId: sectionId
            }).$promise.then(function () {
                NotificationService.showDefaultSuccess();
                RefreshNominationStateService.refresh();

            }, function () {
                NotificationService.showDefaultFailure();
            });
        };
    })

    .service('RefreshNominationStateService', function ($state) {
        this.refresh = function () {
            $state.reload('jht.nominationdecision.document');
        };
    })

    .service('NominationDecisionUtils', function (LocalStorageService, NominationDecision, NominationDecisionSection) {
        var self = this;

        self.isHandler = function (decision) {
            return decision && decision.userIsHandler;
        };

        self.canEditContent = function (decision, sectionId) {
            return self.isHandler(decision) && decision.status === 'DRAFT' &&
                decision.completeStatus[documentFieldForSection(sectionId)] === false;
        };

        self.getSectionContent = function (decision, sectionId) {
            var doc = decision.document;
            return doc[documentFieldForSection(sectionId)];
        };

        self.canBeCompleted = function (decision, sectionId) {

            var isSwedish = decision.locale === 'sv_FI';
            var sectionTextLength = _.size(decision.document[documentFieldForSection(sectionId)]) || 0;

            return sectionTextLength >= getMinimumLengthToCompleteSection(sectionId, isSwedish);
        };

        var notBlankRequired = [
            NominationDecisionSection.DECISION,
            NominationDecisionSection.DECISION_REASONING,
            NominationDecisionSection.APPEAL,
        ];

        function getMinimumLengthToCompleteSection(sectionId, isSwedish) {
            if (_.includes(notBlankRequired, sectionId)) {
                return 1;
            }

            // Certain sections contain automatically generated data and need
            // more specific values for checking whether needed data has been filled in by handler.
            switch (sectionId) {
                case NominationDecisionSection.LEGAL_ADVICE:
                    return 65;
                case NominationDecisionSection.ADDITIONAL_INFO:
                    return (isSwedish ? 170 : 150);
                case NominationDecisionSection.DELIVERY:
                    return (isSwedish ? 24 : 18);
            }

            return 0;
        }

        function documentFieldForSection(sectionId){
            return _.camelCase(sectionId);
        }

        self.reloadSectionContent = function (sectionId, decisionId) {
            return NominationDecision.getDocument({id: decisionId}).$promise.then(function (doc) {
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
                self.allFieldsComplete(decision);
        };

        self.canUnlockDecision = function (decision) {
            return self.isHandler(decision)
                && (decision.status === 'LOCKED' || decision.status === 'PUBLISHED');
        };


        self.allFieldsComplete = function (decision) {
            return _.every(_.values(NominationDecisionSection), function (sectionId) {
                return decision.completeStatus[documentFieldForSection(sectionId)];
            });
        };

        self.isSectionComplete = function(decision, sectionId){
            return _.get(decision.completeStatus, documentFieldForSection(sectionId), false);
        };

        self.toggleComplete = function (decision, sectionId, value) {
            var completeStatus = decision.completeStatus;
            var documentField = documentFieldForSection(sectionId);

            NominationDecision.updateCompleteStatus({id: decision.id}, {
                sectionId: sectionId,
                complete: value
            }).$promise.then(function () {
                completeStatus[documentField] = value;
            });
        };
    })

    .controller('NominationDecisionDocumentController',
        function (NominationDecisionSection, NominationDecision,
                  NominationDecisionUtils, RefreshNominationStateService,
                  NominationDecisionChangeReferenceModal,
                  $state, FormPostService, NotificationService, TranslatedBlockUI, ReasonAsker,
                  decisionId, decision, reference) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.previewEnabled = decision.status !== 'DRAFT';
                $ctrl.sectionIds = _.values(NominationDecisionSection);
                $ctrl.decision = decision;
                $ctrl.reference = reference;
            };

            $ctrl.printDraftPdf = function () {
                FormPostService.submitFormUsingBlankTarget('/api/v1/nominationdecision/' + decisionId + '/print/pdf');
            };

            $ctrl.isHandler = function () {
                return NominationDecisionUtils.isHandler(decision);
            };

            $ctrl.canLockDecision = function () {
                return NominationDecisionUtils.canLockDecision(decision);
            };

            $ctrl.canUnlockDecision = function () {
                return NominationDecisionUtils.canUnlockDecision(decision);
            };

            $ctrl.lockDecision = function () {
                TranslatedBlockUI.start('global.block.wait');

                NominationDecision.lock({id: decisionId}).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $state.go('jht.nominationdecision.revisions', {decisionId: decisionId});

                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            };

            $ctrl.unlockDecision = function () {
                ReasonAsker.openModal({
                    titleKey: 'decision.unlockConfirm.title',
                    messageKey: 'decision.unlockConfirm.message'
                }).then(function (reason) {
                    NominationDecision.unlock({id: decisionId, unlockReason: reason}).$promise.then(function () {
                        NotificationService.showDefaultSuccess();
                        RefreshNominationStateService.refresh();
                    });
                });
            };

            $ctrl.changeReference = function () {
                NominationDecisionChangeReferenceModal.open($ctrl.decision).then(function () {
                    RefreshNominationStateService.refresh();
                });
            };

            $ctrl.previewUrl = function () {
                var activeSectionId = convertStateNameToSectionId() || '';

                return '/api/v1/nominationdecision/' + decisionId + '/print/html?sectionId=' + activeSectionId;
            };

            function convertStateNameToSectionId() {
                var currentStateName = $state.current.name;

                return _.chain(_.values(NominationDecisionSection)).filter(function (sectionId) {
                    return _.endsWith(currentStateName, _.kebabCase(sectionId));
                }).head().value();
            }

        })

    .component('nominationDecisionDocumentNav', {
        templateUrl: 'jht/nominationdecision/document/document-nav.html',
        bindings: {
            decision: '<'
        },
        controller: function ($state, NominationDecisionSection, NominationDecisionUtils) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionIds = _.values(NominationDecisionSection);
            };

            $ctrl.focus = function (sectionId) {
                $state.go($ctrl.getStateName(sectionId));
            };

            $ctrl.isComplete = function (sectionId) {
                return NominationDecisionUtils.isSectionComplete($ctrl.decision, sectionId);
            };

            $ctrl.getStateName = function (sectionId) {
                return 'jht.nominationdecision.document.' + _.kebabCase(sectionId);
            };
        }
    })

    .component('nominationDecisionSectionHeader', {
        templateUrl: 'jht/nominationdecision/document/section-header.html',
        bindings: {
            decision: '<',
            sectionId: '<',
            denyComplete: '&'
        },
        controllerAs: '$ctrl'
    })
    .component('nominationDecisionSectionHeaderButtons', {
        templateUrl: 'jht/nominationdecision/document/section-header-buttons.html',
        bindings: {
            decision: '<',
            sectionId: '<',
            denyComplete: '&'
        },
        controllerAs: '$ctrl',
        controller: function (NominationDecisionUtils) {
            var $ctrl = this;

            $ctrl.$onInit = function() {
                $ctrl.fieldValue = $ctrl.isComplete();
            };

            $ctrl.isComplete = function () {
                return NominationDecisionUtils.isSectionComplete($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.toggleComplete = function () {
                NominationDecisionUtils.toggleComplete($ctrl.decision, $ctrl.sectionId, $ctrl.fieldValue);
            };

            $ctrl.canBeCompleted = function () {
                return !$ctrl.denyComplete() &&
                    NominationDecisionUtils.canBeCompleted($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.isHandler = function () {
                return NominationDecisionUtils.isHandler($ctrl.decision);
            };
        }
    });
