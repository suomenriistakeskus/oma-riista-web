'use strict';

angular.module('app.harvestpermit.decision.rkaauthority', [])
    .factory('DecisionRkaAuthority', function ($resource) {
        var apiPrefix = 'api/v1/decision/rkaauthority/:id';

        return $resource(apiPrefix, {id: '@id'}, {
            update: {method: 'PUT'},
            delete: {method: 'DELETE'},
            listByRka: {method: 'GET', url: apiPrefix + '/rka/:rkaId', isArray: true},
            listByDecision: {method: 'GET', url: apiPrefix + '/decision/:decisionId', isArray: true},
        });
    })
    .controller('DecisionRkaAuthorityListControler', function ($state, NotificationService,
                                                               DecisionRkaAuthority, DecisionRkaAuthorityModal,
                                                               rkaId, authorities) {
        var $ctrl = this;
        $ctrl.$onInit = function () {
            $ctrl.rkaId = rkaId;
            $ctrl.authorities = authorities;
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

        function doEdit(authority) {
            DecisionRkaAuthorityModal.open(authority).then(function (authority) {
                var method = !authority.id ? DecisionRkaAuthority.save : DecisionRkaAuthority.update;
                return method(authority).$promise;
            }).then(reload, failure);
        }

        $ctrl.add = function () {
            doEdit({rkaId: $ctrl.rkaId});
        };

        $ctrl.edit = function (authority) {
            doEdit(authority);
        };
        $ctrl.remove = function (authority) {
            DecisionRkaAuthority.delete(authority).$promise.then(reload, failure);
        };

    })

    .service('DecisionRkaAuthorityModal', function ($uibModal) {
        this.open = function (authority) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/rkaauthority/edit-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    authority: _.constant(authority || {})
                }
            }).result;
        };

        function ModalController($uibModalInstance, authority) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.authority = authority;
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.authority);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
;

