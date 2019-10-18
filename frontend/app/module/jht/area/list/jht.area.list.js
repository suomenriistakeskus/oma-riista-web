'use strict';

angular.module('app.jht.area.list', [])
    .component('moderatorAreaList', {
        templateUrl: 'jht/area/list/area-list.html',
        bindings: {
            areaList: '<',
            reload: '&'
        },
        controller: function ($state, ModeratorAreaModal, MapPdfModal, NotificationService) {
            var $ctrl = this;

            $ctrl.toggle = function (a) {
                a.isCollapsed = !a.isCollapsed;
            };

            $ctrl.editMap = function (area) {
                $state.go('jht.areamap', {areaId: area.id});
            };

            $ctrl.edit = function (area) {
                var modalPromise = ModeratorAreaModal.open(area);

                NotificationService.handleModalPromise(modalPromise).then(function () {
                    $ctrl.reload();
                });
            };

            $ctrl.printArea = function (area) {
                MapPdfModal.printArea('/api/v1/moderator/area/' + area.id + '/print');
            };

            $ctrl.canPrintArea = function (area) {
                return _.get(area, 'size.all.total') > 0;
            };
        }
    })

    .component('moderatorAreaFunctions', {
        templateUrl: 'jht/area/list/area-functions.html',
        bindings: {
            area: '<'
        },
        controller: function ($state, ModeratorAreas, TranslatedBlockUI, FetchAndSaveBlob) {
            var $ctrl = this;

            $ctrl.delete = function () {
                ModeratorAreas.delete({id: $ctrl.area.id}).$promise.then(function () {
                    $state.reload();
                });
            };

            $ctrl.exportExcel = function (type) {
                var url = exportBaseUri() + '/excel/' + type;

                TranslatedBlockUI.start("club.area.excelExportMessage");

                FetchAndSaveBlob.get(url).finally(TranslatedBlockUI.stop);
            };

            function exportBaseUri() {
                return '/api/v1/moderator/area/' + $ctrl.area.id;
            }

            $ctrl.isAreaWithGeometry = function () {
                return _.get($ctrl.area, 'size.all.total') > 0;
            };
        }
    })

    .component('moderatorAreaListInfo', {
        templateUrl: 'jht/area/list/area-list-info.html',
        bindings: {
            area: '<'
        }
    })

    .service('ModeratorAreaModal', function ($uibModal, ModeratorAreas) {
        this.open = function (area) {
            return $uibModal.open({
                templateUrl: 'jht/area/list/area-form.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    areaId: _.constant(area.id),
                    rkaList: function (Areas) {
                        return Areas.query().$promise;
                    },
                    area: function () {
                        return area.id ? ModeratorAreas.get({id: area.id}).$promise : area;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, rkaList, area, areaId) {
            var $ctrl = this;

            $ctrl.rkaList = rkaList;
            $ctrl.isUpdate = !!areaId;
            $ctrl.areaName = area.name;
            $ctrl.rkaCode = area.rkaCode;
            $ctrl.year = area.year;
            $ctrl.maxYear = new Date().getFullYear() + 1;

            $ctrl.save = function (form) {
                if (form.$invalid) {
                    return;
                }

                var saveMethod = areaId ? ModeratorAreas.update : ModeratorAreas.save;

                saveMethod({
                    id: areaId,
                    year: $ctrl.year,
                    rkaCode: $ctrl.rkaCode,
                    name: $ctrl.areaName
                }).$promise.then(function () {
                    $uibModalInstance.close();
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
