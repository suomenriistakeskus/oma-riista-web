'use strict';

angular.module('app.harvestpermit.decision.rkarecipient', [])
    .factory('DecisionRkaRecipient', function ($resource) {
        var apiPrefix = 'api/v1/decision/rkarecipient/:id';

        return $resource(apiPrefix, {id: '@id'}, {
            update: {method: 'PUT'},
            delete: {method: 'DELETE'},
            listByRka: {method: 'GET', url: apiPrefix + '/rka/:rkaId', isArray: true}
        });
    })
    .controller('DecisionRkaRecipientListControler', function ($state, NotificationService,
                                                               DecisionRkaRecipient, DecisionRkaRecipientModal,
                                                               rkaId, recipients) {
        var $ctrl = this;
        $ctrl.$onInit = function () {
            $ctrl.rkaId = rkaId;
            $ctrl.recipients = recipients;
        };

        function reload() {
            NotificationService.showDefaultSuccess();
            return $state.reload();
        }

        function failure(error) {
            if (error !== 'cancel' && error !== 'escape key press') {
                NotificationService.showDefaultFailure();
            }
        }

        function doEdit(recipient) {
            DecisionRkaRecipientModal.open(recipient).then(function (recipient) {
                var method = !recipient.id ? DecisionRkaRecipient.save : DecisionRkaRecipient.update;
                return method(recipient).$promise;
            }).then(reload, failure);
        }

        $ctrl.add = function () {
            doEdit({rkaId: $ctrl.rkaId});
        };

        $ctrl.edit = function (recipient) {
            doEdit(recipient);
        };
        $ctrl.remove = function (recipient) {
            DecisionRkaRecipient.delete(recipient).$promise.then(reload, failure);
        };

    })

    .service('DecisionRkaRecipientModal', function ($uibModal) {
        this.open = function (recipient) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/rkarecipient/edit-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    recipient: _.constant(recipient || {})
                }
            }).result;
        };

        function ModalController($uibModalInstance, recipient) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.recipient = recipient;
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.recipient);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
;

