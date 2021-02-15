'use strict';

angular.module('app.harvestpermit.decision.document.execution', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.execution', {
            url: '/execution',
            templateUrl: 'harvestpermit/decision/document/execution/document.execution.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionSection, decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.EXECUTION;
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
