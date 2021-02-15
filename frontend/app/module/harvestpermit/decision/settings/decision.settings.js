'use strict';

angular.module('app.harvestpermit.decision.settings', [])
    .service('PermitDecisionDocumentSettingsModal', function ($uibModal, NotificationService, PermitDecision) {
        this.open = function (decisionId, permitTypeCode) {
            var modalPromise = $uibModal.open({
                templateUrl: 'harvestpermit/decision/settings/document-settings.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    permitTypeCode: _.constant(permitTypeCode),
                    data: function () {
                        return PermitDecision.getDocumentSettings({id: decisionId}).$promise;
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise);
        };

        function ModalController($uibModalInstance, decisionId, permitTypeCode, data, PermitTypeCode) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.locale = data.locale;
                $ctrl.decisionType = data.decisionType;
                $ctrl.permitTypeCode = permitTypeCode;
            };

            $ctrl.save = function () {

                PermitDecision.updateDocumentSettings({id: decisionId}, {
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

            $ctrl.isCancelRenewalAvailable = function () {
                return PermitTypeCode.isRenewalPermitType($ctrl.permitTypeCode);
            };
        }
    })
    .service('PermitDecisionPublishSettingsModal', function ($uibModal, NotificationService, PermitDecision) {
        this.open = function (decisionId) {
            var modalPromise = $uibModal.open({
                templateUrl: 'harvestpermit/decision/settings/publish-settings.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    data: function () {
                        return PermitDecision.getPublishSettings({id: decisionId}).$promise;
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise);
        };

        function ModalController($uibModalInstance, Helpers, AppealStatus, decisionId, data) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.publishDate = data.publishDate;
                $ctrl.publishTime = data.publishTime || '08:00';
                $ctrl.appealStatus = data.appealStatus;
                $ctrl.appealStatusOptions = AppealStatus;
                $ctrl.appealStatusRequired = !!data.appealStatus;
            };

            $ctrl.save = function () {
                PermitDecision.updatePublishSettings({id: decisionId}, {
                    decisionId: decisionId,
                    publishDate: $ctrl.publishDate,
                    publishTime: $ctrl.publishTime,
                    appealStatus: $ctrl.appealStatus

                }).$promise.then(function () {
                    $uibModalInstance.close();
                }, function (err) {
                    $uibModalInstance.dismiss(err);
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.showPublishDateInPastWarning = function () {
                if ($ctrl.publishDate && $ctrl.publishTime) {
                    var parseDateAndTime = Helpers.parseDateAndTime($ctrl.publishDate, $ctrl.publishTime);
                    return parseDateAndTime.isValid() && parseDateAndTime.isSameOrBefore();
                }

                return false;
            };
        }
    })
    .service('PermitDecisionAppealSettingsModal', function ($uibModal, NotificationService, PermitDecision) {
        this.open = function (decisionId) {
            var modalPromise = $uibModal.open({
                templateUrl: 'harvestpermit/decision/settings/appeal-settings.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    data: function () {
                        return PermitDecision.getAppealSettings({id: decisionId}).$promise;
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
                PermitDecision.updateAppealSettings({id: decisionId}, {
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
