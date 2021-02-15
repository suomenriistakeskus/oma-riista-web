'use strict';

angular.module('app.jht.nominationdecision.document.text', [])

    .component('nominationDecisionDocumentTextSection', {
        templateUrl: 'common/decision/document/text/text-section.html',
        controllerAs: '$ctrl',
        bindings: {
            sectionId: '<',
            decision: '<',
            reference: '<'
        },
        controller: function (NominationDecision, NominationDecisionUtils, DecisionDocumentEditTextModal,
                              NotificationService, RefreshNominationStateService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionContent = NominationDecisionUtils.getSectionContent($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.canEditContent = function () {
                return NominationDecisionUtils.canEditContent($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.editSection = function () {
                NominationDecisionUtils.reloadSectionContent($ctrl.sectionId, $ctrl.decision.id).then(function (textContent) {
                    return editContent(textContent).then(storeTextContent);
                });
            };

            function editContent(textContent) {
                var referenceContent = NominationDecisionUtils.getReferenceContent($ctrl.reference, $ctrl.sectionId);

                return DecisionDocumentEditTextModal.open(
                    $ctrl.decision.id, $ctrl.sectionId, textContent, referenceContent, NominationDecision.generateText, true);
            }

            function storeTextContent(editedTextContent) {
                NominationDecision.updateDocument({id: $ctrl.decision.id}, {
                    sectionId: $ctrl.sectionId,
                    content: editedTextContent

                }).$promise.then(function () {
                    NotificationService.showDefaultSuccess();

                    RefreshNominationStateService.refresh();

                }, function () {
                    NotificationService.showDefaultFailure();
                });
            }
        }
    });
