'use strict';

angular.module('app.harvestpermit.decision.document.attachments', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.attachments', {
            url: '/attachments',
            templateUrl: 'harvestpermit/decision/document/attachments/document.attachments.html',
            controllerAs: '$ctrl',
            controller: function ($state, PermitDecisionUtils, PermitDecisionAttachmentsModal,
                                  PermitDecisionSection, decision, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.ATTACHMENTS;
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = PermitDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {
                    var showDefaultMooseAttachmentButton = $ctrl.decision.permitTypeCode === '100';

                    PermitDecisionAttachmentsModal.open(decisionId, showDefaultMooseAttachmentButton).then(function () {
                        $state.reload();
                    });
                };

                $ctrl.canEditContent = function () {
                    return PermitDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };

                $ctrl.denyComplete = function () {
                    return $ctrl.decision.permitTypeCode === '100' &&
                        $ctrl.decision.decisionType === 'HARVEST_PERMIT' &&
                        $ctrl.decision.grantStatus !== 'REJECTED' &&
                        _.isEmpty($ctrl.sectionContent);
                };
            }
        });
    });
