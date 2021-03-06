'use strict';

angular.module('app.harvestpermit.decision.document.applicationreasoning', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.application-reasoning', {
            url: '/application-reasoning',
            templateUrl: 'harvestpermit/decision/document/application-reasoning/document.application-reasoning.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionSection, decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.APPLICATION_REASONING;
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
