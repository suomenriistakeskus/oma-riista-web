'use strict';

angular.module('app.harvestpermit.decision.document.appeal', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.appeal', {
            url: '/appeal',
            templateUrl: 'harvestpermit/decision/document/appeal/document.appeal.html',
            controllerAs: '$ctrl',
            controller: function (decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = 'appeal';
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                };
            }
        });
    });
