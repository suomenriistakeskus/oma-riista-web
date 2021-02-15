'use strict';

angular.module('app.harvestpermit.decision.document.payment', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.payment', {
            url: '/payment',
            templateUrl: 'harvestpermit/decision/document/payment/document.payment.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionUtils, PermitDecisionPaymentModal, NotificationService,
                                  PermitDecision, PermitDecisionSection, RefreshDecisionStateService,
                                  decision, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.PAYMENT;
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = PermitDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {
                    PermitDecisionPaymentModal.open($ctrl.decision.paymentAmount, $ctrl.decision.id).then(function (amount) {
                        PermitDecision.updatePayment({
                            id: decisionId,
                            paymentAmount: amount
                        }).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            RefreshDecisionStateService.refresh();
                        }, function () {
                            NotificationService.showDefaultFailure();
                        });
                    });
                };

                $ctrl.canEditContent = function () {
                    return PermitDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };
            }
        });
    })

    .service('PermitDecisionPaymentModal', function ($uibModal) {
        this.open = function (currentAmount, decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/payment/select-payment-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    amount: _.constant(currentAmount),
                    paymentOptions: function (PermitDecision) {
                        return PermitDecision.getPaymentOptions({id:decisionId}).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, amount, paymentOptions) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.amount = amount;
                $ctrl.amounts = paymentOptions;
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.amount);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
