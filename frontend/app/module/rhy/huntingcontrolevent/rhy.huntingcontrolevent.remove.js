'use strict';

angular.module('app.rhy.huntingcontrolevent.remove', [])
    .controller('HuntingControlEventRemoveController', function (event, $uibModalInstance, HuntingControlEvents) {

        var $ctrl = this;

        $ctrl.remove = function () {
            HuntingControlEvents.delete({id: event.id}).$promise
                .then(function() {
                    $uibModalInstance.close();
                }, function() {
                    $uibModalInstance.dismiss('error');
                });
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });