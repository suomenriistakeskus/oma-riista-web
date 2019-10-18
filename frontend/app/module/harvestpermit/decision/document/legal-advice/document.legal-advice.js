'use strict';

angular.module('app.harvestpermit.decision.document.legaladvice', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.legal-advice', {
            url: '/legal-advice',
            templateUrl: 'harvestpermit/decision/document/legal-advice/document.legal-advice.html',
            controllerAs: '$ctrl',
            controller: function ($state, PermitDecisionUtils, PermitDecisionDocumentEditModal,
                                  decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = 'legalAdvice';
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
