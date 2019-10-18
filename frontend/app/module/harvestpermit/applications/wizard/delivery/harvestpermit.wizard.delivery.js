'use strict';

angular.module('app.harvestpermit.application.wizard.delivery', ['app.metadata'])
    .service('DecisionDeliveryAddressModal', function ($uibModal) {
        this.open = function (deliveryAddress) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/applications/wizard/delivery/delivery-address-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    deliveryAddress: _.cloneDeep(deliveryAddress)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate, dialogs, deliveryAddress) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.deliveryAddress = deliveryAddress;
            };

            $ctrl.close = function () {
                $uibModalInstance.close($ctrl.deliveryAddress);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
;
