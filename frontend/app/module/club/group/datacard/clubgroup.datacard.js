(function () {
    'use strict';

    angular.module('app.clubgroup.datacard', [])
        .factory('MooseDataCardImports', function ($resource) {
            return $resource('api/v1/moosedatacard/:id', {'id': '@id'}, {
                listForGroup: {
                    method: 'GET',
                    isArray: true,
                    url: 'api/v1/moosedatacard/listforgroup/:groupId',
                    params: {
                        groupId: '@groupId'
                    }
                }
            });
        })

        .component('groupMooseDataCardImportList', {
            templateUrl: 'club/group/datacard/moose-data-card-imports.html',
            bindings: {
                mooseDataCardImports: '<'
            },
            controller: function ($state, ActiveRoleService, RevokeMooseDataCardModal) {
                var $ctrl = this;

                $ctrl.notRevoked = [];
                $ctrl.revoked = [];
                $ctrl.isModerator = ActiveRoleService.isModerator();

                var groupMooseDataCardImports = function (all) {
                    $ctrl.notRevoked = _.filter(all, {'revocationTimestamp': null});
                    $ctrl.revoked = _.filter(all, 'revocationTimestamp');
                };

                groupMooseDataCardImports($ctrl.mooseDataCardImports);

                $ctrl.toggleImportMessages = function (object) {
                    object.showMessages = !object.showMessages;
                };

                $ctrl.revokeImport = function (mooseDataCardImport) {
                    RevokeMooseDataCardModal.showModal(mooseDataCardImport).then(function () {
                        $state.reload();
                    });
                };

                $ctrl.getPdfImportFileUrl = function (mooseDataCardImportId) {
                    return getUrlPrefix(mooseDataCardImportId) + '/pdf';
                };
                $ctrl.getXmlImportFileUrl = function (mooseDataCardImportId) {
                    return getUrlPrefix(mooseDataCardImportId) + '/xml';
                };

                function getUrlPrefix(mooseDataCardImportId) {
                    return '/api/v1/moosedatacard/' + mooseDataCardImportId;
                }
            }
        })

        .service('RevokeMooseDataCardModal', function ($uibModal, MooseDataCardImports) {
            this.showModal = function (mooseDataCardImport) {
                return $uibModal.open({
                    templateUrl: 'club/group/datacard/revoke-moose-data-card-import.html',
                    controllerAs: '$ctrl',
                    controller: ModalController,
                    resolve: {
                        mooseDataCardImport: _.constant(mooseDataCardImport)
                    }
                }).result;
            };

            function ModalController($uibModalInstance, mooseDataCardImport) {
                var $ctrl = this;

                $ctrl.mooseDataCardImport = mooseDataCardImport;

                $ctrl.revoke = function () {
                    MooseDataCardImports.delete({
                        id: $ctrl.mooseDataCardImport.id
                    }).$promise.then(function () {
                        $uibModalInstance.close();
                    });
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        });
})();
