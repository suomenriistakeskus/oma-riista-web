'use strict';

angular.module('app.harvestpermit.decision.document.text', [])

    .component('permitDecisionDocumentTextSection', {
        templateUrl: 'common/decision/document/text/text-section.html',
        controllerAs: '$ctrl',
        bindings: {
            sectionId: '<',
            decision: '<',
            reference: '<'
        },
        controller: function (PermitDecision, PermitDecisionUtils, PermitDecisionSection, DecisionDocumentEditTextModal,
                              NotificationService, RefreshDecisionStateService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionContent = PermitDecisionUtils.getSectionContent($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.canEditContent = function () {
                return PermitDecisionUtils.canEditContent($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.editSection = function () {
                PermitDecisionUtils.reloadSectionContent($ctrl.sectionId, $ctrl.decision.id).then(function (textContent) {
                    return editContent(textContent).then(storeTextContent);
                });
            };

            function editContent(textContent) {
                var referenceContent = PermitDecisionUtils.getReferenceContent($ctrl.reference, $ctrl.sectionId);

                return DecisionDocumentEditTextModal.open(
                    $ctrl.decision.id, $ctrl.sectionId, textContent, referenceContent, PermitDecision.generateText,
                    $ctrl.sectionId !== PermitDecisionSection.EXECUTION);
            }

            function storeTextContent(editedTextContent) {
                PermitDecision.updateDocument({id: $ctrl.decision.id}, {
                    sectionId: $ctrl.sectionId,
                    content: editedTextContent

                }).$promise.then(function () {
                    NotificationService.showDefaultSuccess();

                    RefreshDecisionStateService.refresh();

                }, function () {
                    NotificationService.showDefaultFailure();
                });
            }
        }
    });
