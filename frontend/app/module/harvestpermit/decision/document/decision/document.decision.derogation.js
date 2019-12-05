'use strict';

angular.module('app.harvestpermit.decision.document.decision.derogation', [])
    .component('permitDecisionDerogationReasons', {
        bindings: {
            decisionId: '<',
            derogationLawSections: '<',
            canEditContent: '<'
        },
        templateUrl: 'harvestpermit/decision/document/decision/derogation-reasons.html',
        controller: function ($translate, PermitDecisionDerogationReasonsModal, RefreshDecisionStateService) {
            var $ctrl = this;

            $ctrl.updateDerogationReasons = function () {
                PermitDecisionDerogationReasonsModal.open($ctrl.decisionId).then(function () {
                    RefreshDecisionStateService.refresh();
                });
            };

            $ctrl.getSelectedReasons = function (lawSection) {
                return _.filter(lawSection.reasons, 'checked');
            };
        }
    })

    .service('PermitDecisionDerogationReasonsModal', function ($uibModal, NotificationService, PermitDecisionDerogation) {
        this.open = function (decisionId) {
            var modalPromise = $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/decision/derogation-reasons-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    derogationLawSections: function () {
                        return PermitDecisionDerogation.getReasons({id: decisionId}).$promise.then(function (data) {
                            return data.lawSections;
                        });
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise);
        };

        function ModalController($uibModalInstance, decisionId, derogationLawSections) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.derogationLawSections = derogationLawSections;
            };

            $ctrl.save = function () {
                PermitDecisionDerogation.updateReasons({id: decisionId}, {
                    lawSections: $ctrl.derogationLawSections
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

    .component('permitDecisionProtectedAreaTypes', {
        bindings: {
            decisionId: '<',
            protectedAreaTypes: '<',
            canEditContent: '<'
        },
        templateUrl: 'harvestpermit/decision/document/decision/protected-area-types.html',
        controller: function ($translate, PermitDecisionProtectedAreaTypesModal, RefreshDecisionStateService) {
            var $ctrl = this;

            $ctrl.updateProtectedAreaTypes = function () {
                PermitDecisionProtectedAreaTypesModal.open($ctrl.decisionId).then(function () {
                    RefreshDecisionStateService.refresh();
                });
            };
        }
    })

    .service('PermitDecisionProtectedAreaTypesModal', function ($uibModal, NotificationService, PermitDecisionDerogation) {
        this.open = function (decisionId) {
            var modalPromise = $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/decision/protected-area-types-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    data: function () {
                        return PermitDecisionDerogation.getProtectedAreaTypes({id: decisionId}).$promise;
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise);
        };

        function ModalController($uibModalInstance, decisionId, data) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.protectedAreaTypes = data.types;
            };

            $ctrl.save = function () {
                PermitDecisionDerogation.updateProtectedAreaTypes({id: decisionId}, {
                    types: $ctrl.protectedAreaTypes
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
