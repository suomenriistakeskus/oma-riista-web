'use strict';

angular.module('app.harvestpermit.decision.document.application', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.application', {
            url: '/application',
            templateUrl: 'harvestpermit/decision/document/application/document.application.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionUtils, NotificationService,
                                  PermitDecision, RefreshDecisionStateService, decision, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = 'application';
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = PermitDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.canEditDecision = function () {
                    return PermitDecisionUtils.canEditContent(decision, 'decision');
                };

                $ctrl.regenerateApplicationSection = function () {
                    PermitDecision.generateAndPersistText({
                        id: decisionId,
                        sectionId: $ctrl.sectionId
                    }).$promise.then(function () {
                        NotificationService.showDefaultSuccess();
                        RefreshDecisionStateService.refresh();

                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                };
            }
        });
    });
