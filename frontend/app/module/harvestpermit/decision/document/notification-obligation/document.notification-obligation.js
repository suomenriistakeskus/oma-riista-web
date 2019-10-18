'use strict';

angular.module('app.harvestpermit.decision.document.notificationobligation', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.notification-obligation', {
            url: '/notification-obligation',
            templateUrl: 'harvestpermit/decision/document/notification-obligation/document.notification-obligation.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionDocumentEditModal, decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = 'notificationObligation';
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
