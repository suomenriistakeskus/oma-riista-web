'use strict';

angular.module('app.jht.nominationdecision.settings', [])
    .service('NominationDecisionDocumentSettingsModal', function ($uibModal, NotificationService, NominationDecision) {
        this.open = function (decisionId) {
            var modalPromise = $uibModal.open({
                templateUrl: 'jht/nominationdecision/settings/document-settings.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    data: function () {
                        return NominationDecision.getDocumentSettings({id: decisionId}).$promise;
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise);
        };

        function ModalController($uibModalInstance, decisionId, data) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.locale = data.locale;
            };

            $ctrl.save = function () {
                NominationDecision.updateDocumentSettings({id: decisionId}, {
                    decisionId: decisionId,
                    locale: $ctrl.locale,
                    decisionType: $ctrl.decisionType
                }).$promise.then(function () {
                    $uibModalInstance.close();
                }, function (err) {
                    $uibModalInstance.dismiss(err);
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })

    .service('NominationDecisionAppealSettingsModal', function ($uibModal, NotificationService, NominationDecision) {
        this.open = function (decisionId) {
            var modalPromise = $uibModal.open({
                templateUrl: 'jht/nominationdecision/settings/appeal-settings.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    data: function () {
                        return NominationDecision.getAppealSettings({id: decisionId}).$promise;
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise);
        };

        function ModalController($uibModalInstance, Helpers, AppealStatus, decisionId, data) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.appealInitiated = !!data.appealStatus;
                $ctrl.appealStatus = data.appealStatus || 'INITIATED';
                $ctrl.appealStatusOptions = AppealStatus;
            };

            $ctrl.save = function () {
                NominationDecision.updateAppealSettings({id: decisionId}, {
                    decisionId: decisionId,
                    appealStatus: $ctrl.appealInitiated ? $ctrl.appealStatus : null

                }).$promise.then(function () {
                    $uibModalInstance.close();
                }, function (err) {
                    $uibModalInstance.dismiss(err);
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
