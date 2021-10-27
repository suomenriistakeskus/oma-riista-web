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
        controller: function ($state, $location, ModeratorAreas, ModeratorAreaCopyModal, TranslatedBlockUI, FetchAndSaveBlob,
                              ModeratorAreaImportAreaModal, ModeratorAreaAddAreasModal) {
            var $ctrl = this;

            $ctrl.delete = function () {
                ModeratorAreas.delete({id: $ctrl.area.id}).$promise.then(function () {
                    $state.reload();
                });
            };

            $ctrl.copyArea = function () {
                ModeratorAreaCopyModal.copyModeratorArea($ctrl.area).then(function () {
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

            function exportBaseUri() {
                return '/api/v1/moderator/area/' + $ctrl.area.id;
            }

            $ctrl.isAreaWithGeometry = function () {
                return _.get($ctrl.area, 'size.all.total') > 0;
            };

            $ctrl.importArea = function () {
                ModeratorAreaImportAreaModal.importArea($ctrl.area).then(function () {
                    $state.reload();
                });
            };

            $ctrl.addAreas = function () {
                ModeratorAreaAddAreasModal.addAreas($ctrl.area).then(function (area) {
                    $location.path('/jht/areamap/' + area.id);
                });
            };

        }
    })

    .service('ModeratorAreaCopyModal', function ($uibModal, ModeratorAreas) {
        this.copyModeratorArea = function (area) {
            return $uibModal.open({
                templateUrl: 'jht/area/list/area-copy.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    area: _.constant(area)
                }
            }).result.then(function (res) {
                return ModeratorAreas.copy({id: res.id}, res).$promise;
            });
        };

        function ModalController($uibModalInstance, area) {
            var $ctrl = this;

            $ctrl.areaCopyData = {
                id: area.id,
                year: area.year
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.areaCopyData);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss();
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
    })

    .service('ModeratorAreaImportAreaModal', function ($q, $translate, $uibModal, dialogs, NotificationService) {
            this.importArea = function (area) {
                return confirmAreaImport(area).then(function () {
                    return $uibModal.open({
                        templateUrl: 'jht/area/list/area-import.html',
                        size: 'md',
                        controller: ModalController,
                        controllerAs: '$ctrl',
                        bindToController: true,
                        resolve: {
                            areaId: _.constant(area.id)
                        }
                    }).result.then(function (area) {
                        NotificationService.showMessage('moderator.area.import.success', 'success');
                        return area;
                    });
                });
            };

            function confirmAreaImport(area) {
                if (!area.zoneId) {
                    return $q.when(true);
                }

                var dialogTitle = $translate.instant('moderator.area.import.confirmTitle');
                var dialogMessage = $translate.instant('moderator.area.import.confirmBody', {
                    areaName: area.name
                });

                return dialogs.confirm(dialogTitle, dialogMessage).result;
            }

            function ModalController($uibModalInstance, ModeratorAreas, areaId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.externalId = null;
                    $ctrl.selectedArea = null;
                };

                $ctrl.search = function () {
                    $ctrl.selectedArea = null;
                    ModeratorAreas.findByExternalId({externalId: $ctrl.externalId}).$promise.then(
                        function (area) {
                            $ctrl.selectedArea = area;
                        });
                };

                $ctrl.close = function () {
                    $uibModalInstance.dismiss();
                };

                $ctrl.removeSelection = function () {
                    $ctrl.selectedArea = null;
                };

                $ctrl.doImport = function () {
                    return ModeratorAreas.importArea({id: areaId}, $ctrl.selectedArea).$promise
                        .then(function (area) {
                            $uibModalInstance.close(area);
                        });
                };
            }
        })

    .service('ModeratorAreaAddAreasModal', function ($q, $translate, $uibModal, dialogs, NotificationService) {
        this.addAreas = function (area) {
            return $uibModal.open({
                templateUrl: 'jht/area/list/area-add.html',
                size: 'md',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    areaId: _.constant(area.id)
                }
            }).result.then(function (area) {
                NotificationService.showMessage('moderator.area.import.success', 'success');
                return area;
            });
        };

        function ModalController($uibModalInstance, ModeratorAreas, areaId) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.externalAreaIdList = [];
                $ctrl.areaList = [];
                $ctrl.invalidNumberCount = 0;
            };

            $ctrl.findAreas = function () {
                var list = [];

                angular.forEach($ctrl.externalAreaIdList.split(/[\s,;]+/), function (value) {
                    if (value) {
                        list.push(value.trim());
                    }
                });

                ModeratorAreas.findByExternalIds(_.uniq(list)).$promise.then(function (response) {
                    $ctrl.areaList = response;
                    indexList($ctrl.areaList);
                    $ctrl.invalidNumberCount = countInvalidNumbers($ctrl.areaList);
                });
            };

            $ctrl.close = function () {
                $uibModalInstance.dismiss();
            };

            $ctrl.remove = function (key) {
                $ctrl.areaList.splice(key, 1);
                indexList($ctrl.areaList);
                $ctrl.invalidNumberCount = countInvalidNumbers($ctrl.areaList);
            };

            $ctrl.edit = function () {
                $ctrl.areaList = [];
            };

            $ctrl.doAdd = function () {
                _.forEach($ctrl.areaList, function (area) {
                    delete area.key;
                });
                return ModeratorAreas.addAreasOutline({id: areaId}, $ctrl.areaList).$promise
                    .then(function (area) {
                        $uibModalInstance.close(area);
                    });
            };

            function indexList(areaList) {
                var counter = 0;
                _.forEach(areaList, function (area) {
                    area.key = counter;
                    counter++;
                });
            }

            function countInvalidNumbers(areaList) {
                return _.filter(areaList, function (area) {
                    return !area.areaId;
                }).length;
            }
        }
    });
