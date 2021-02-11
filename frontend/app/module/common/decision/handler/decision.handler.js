'use strict';

angular.module('app.common.decision.handler', [])
    .component('assignDecisionHandler', {
        templateUrl: 'common/decision/handler/assign-handler.html',
        bindings: {
            decision: '<',
            assign: '&',
            unassign: '&'
        },
        controllerAs: '$ctrl',
        controller: function ($state, NotificationService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.lockedAssignment = $ctrl.decision.status !== 'DRAFT';
            };

            $ctrl.assignDecision = function () {
                $ctrl.assign().$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $state.reload();
                });
            };

            $ctrl.unassignDecision = function () {
                $ctrl.unassign().$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $state.reload();
                });
            };
        }
    });
