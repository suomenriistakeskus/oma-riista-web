'use strict';

angular.module('app.account.area.list', [])
    .component('accountAreaList', {
        templateUrl: 'account/area/list/area-list.html',
        bindings: {
            areaList: '<'
        },
        controller: function ($state, AccountAreaModal, MapPdfModal, NotificationService) {
            var $ctrl = this;

            $ctrl.editMap = function (area) {
                $state.go('profile.areamap', {areaId: area.id});
            };

            $ctrl.edit = function (area) {
                AccountAreaModal.open(area).then(function () {
                    NotificationService.showDefaultSuccess();
                    $state.reload();

                }, function () {
                    NotificationService.showDefaultFailure();
                });
            };

            $ctrl.printArea = function (area) {
                MapPdfModal.printArea('/api/v1/account/area/' + area.id + '/print');
            };
        }
    })

    .component('accountAreaFunctions', {
        templateUrl: 'account/area/list/area-functions.html',
        bindings: {
            area: '<'
        },
        controller: function ($state, AccountAreas, TranslatedBlockUI, FetchAndSaveBlob) {
            var $ctrl = this;

            $ctrl.delete = function () {
                AccountAreas.delete({id: $ctrl.area.id}).$promise.then(function () {
                    $state.reload();
                });
            };

            $ctrl.copy = function () {
                AccountAreas.copy({id: $ctrl.area.id}).$promise.then(function () {
                    $state.reload();
                });
            };

            $ctrl.exportExcel = function (type) {
                var url = exportBaseUri() + '/excel/' + type;

                TranslatedBlockUI.start("club.area.excelExportMessage");

                FetchAndSaveBlob.get(url).finally(TranslatedBlockUI.stop);
            };

            $ctrl.exportGeoJson = function () {
                var url = exportBaseUri() + '/zip';

                FetchAndSaveBlob.post(url, 'arraybuffer');
            };

            $ctrl.exportGarmin = function () {
                FetchAndSaveBlob.post(exportBaseUri() + '/garmin');
            };

            function exportBaseUri() {
                return '/api/v1/account/area/' + $ctrl.area.id;
            }

            $ctrl.isAreaWithGeometry = function () {
                return $ctrl.area && $ctrl.area.zoneId && !!$ctrl.area.size;
            };
        }
    })

    .component('accountAreaListInfo', {
        templateUrl: 'account/area/list/area-list-info.html',
        bindings: {
            area: '<'
        },
        controller: function () {

        }
    })

    .service('AccountAreaModal', function ($uibModal, AccountAreas) {
        this.open = function (area) {
            return $uibModal.open({
                templateUrl: 'account/area/list/area-form.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    areaId: _.constant(area.id),
                    area: function () {
                        return area.id ? AccountAreas.get({id: area.id}).$promise : area;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, area, areaId) {
            var $ctrl = this;

            $ctrl.isUpdate = !!areaId;
            $ctrl.areaName = area.name;

            $ctrl.save = function (form) {
                if (form.$valid) {
                    var saveMethod = areaId ? AccountAreas.update : AccountAreas.save;

                    saveMethod({id: areaId, name: $ctrl.areaName}).$promise.then(function () {
                        $uibModalInstance.close();
                    });
                }
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss();
            };
        }
    });
