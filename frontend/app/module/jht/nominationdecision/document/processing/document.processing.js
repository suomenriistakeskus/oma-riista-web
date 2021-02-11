'use strict';

angular.module('app.jht.nominationdecision.document.processing', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.nominationdecision.document.processing', {
            url: '/processing',
            templateUrl: 'jht/nominationdecision/document/processing/document.processing.html',
            controllerAs: '$ctrl',
            controller: function ($state, NominationDecisionUtils, NominationDecisionActionListModal,
                                  NotificationService, NominationDecisionSection,
                                  RefreshNominationStateService, decision, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = NominationDecisionSection.PROCESSING;
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = NominationDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {
                    NominationDecisionActionListModal.open(decisionId).then(
                        RefreshNominationStateService.refresh,
                        RefreshNominationStateService.refresh); // Refresh on error too
                };

                $ctrl.canEditContent = function () {
                    return NominationDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };
            }
        });
    });
