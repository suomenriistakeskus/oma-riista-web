'use strict';

angular.module('app.harvestpermit.decision.document.decisionreasoning', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.decision-reasoning', {
            url: '/decision-reasoning',
            templateUrl: 'harvestpermit/decision/document/decision-reasoning/document.decision-reasoning.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionSection, decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.DECISION_REASONING;
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
