'use strict';

angular.module('app.harvestpermit.decision.document.decisionreasoning', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.decision-reasoning', {
            url: '/decision-reasoning',
            templateUrl: 'harvestpermit/decision/document/decision-reasoning/document.decision-reasoning.html',
            controllerAs: '$ctrl',
            controller: function (decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = 'decisionReasoning';
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
