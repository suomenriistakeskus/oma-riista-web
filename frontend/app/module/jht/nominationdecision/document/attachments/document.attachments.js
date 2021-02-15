'use strict';

angular.module('app.jht.nominationdecision.document.attachments', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.nominationdecision.document.attachments', {
            url: '/attachments',
            templateUrl: 'jht/nominationdecision/document/attachments/document.attachments.html',
            controllerAs: '$ctrl',
            controller: function ($state, NominationDecisionUtils, NominationDecisionAttachmentsModal,
                                  NominationDecisionSection, decision, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = NominationDecisionSection.ATTACHMENTS;
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = NominationDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {

                    NominationDecisionAttachmentsModal.open(decisionId).then(function () {
                        $state.reload();
                    });
                };

                $ctrl.canEditContent = function () {
                    return NominationDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };

                $ctrl.denyComplete = function () {
                    return false;
                };
            }
        });
    });
