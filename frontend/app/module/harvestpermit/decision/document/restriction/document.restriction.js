'use strict';

angular.module('app.harvestpermit.decision.document.restriction', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.restriction', {
            url: '/restriction',
            templateUrl: 'harvestpermit/decision/document/restriction/document.restriction.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionDocumentEditModal, decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = 'restriction';
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
