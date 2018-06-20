'use strict';

angular.module('app.common.askreason', [])
    .service('ReasonAsker', function ($q, $uibModal) {
        this.openModal = function (params) {
            return $uibModal.open({
                templateUrl: 'common/askreason/askreason.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                resolve: {
                    params: _.constant(params)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate, params) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.reason = '';
                $ctrl.modalTitle = $translate.instant(params.titleKey);
                $ctrl.message = $translate.instant(params.messageKey);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.reason);
            };
        }
    });
