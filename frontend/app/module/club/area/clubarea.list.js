(function () {
    "use strict";

    angular.module('app.clubarea.list', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('club.area', {
                    abstract: true,
                    template: '<ui-view autoscroll="false"/>',
                    resolve: {
                        rhyBounds: function (GIS, club) {
                            return GIS.getRhyBounds(club.rhy.officialCode);
                        }
                    }
                })
                .state('club.area.list', {
                    url: '/area?{areaId:[0-9]{1,8}}',
                    templateUrl: 'club/area/club-areas.html',
                    controller: 'ClubAreaListController',
                    controllerAs: '$ctrl',
                    bindToController: true,
                    wideLayout: true,
                    reloadOnSearch: false,
                    params: {
                        areaId: {
                            value: null
                        }
                    },
                    resolve: {
                        selectedAreaId: function ($stateParams) {
                            return _.parseInt($stateParams.areaId);
                        },
                        preloadedHuntingYears: function (ClubAreaListService, clubId) {
                            return ClubAreaListService.listHuntingYears(clubId);
                        },
                        preselectedArea: function (ClubAreas, selectedAreaId, clubId) {
                            return selectedAreaId ? ClubAreas.get({clubId: clubId, id: selectedAreaId}).$promise : null;
                        },
                        showDeactive: function (preselectedArea) {
                            return preselectedArea && !preselectedArea.active;
                        },
                        huntingYear: function (preselectedArea) {
                            return preselectedArea ? preselectedArea.huntingYear : null;
                        }
                    }
                });
        })

        .factory('ClubAreas', function ($resource) {
            return $resource('api/v1/club/:clubId/area/:id', {"clubId": "@clubId", "id": "@id"}, {
                query: {method: 'GET', params: {year: "@year"}, isArray: true},
                get: {method: 'GET'},
                update: {method: 'PUT'},
                huntingYears: {
                    method: 'GET',
                    url: 'api/v1/club/:clubId/area/huntingyears',
                    isArray: true
                },
                getFeatures: {
                    method: 'GET',
                    url: 'api/v1/club/:clubId/area/:id/features'
                },
                updateFeatures: {
                    method: 'PUT',
                    url: 'api/v1/club/:clubId/area/:id/features',
                    transformRequest: function (req, headers) {
                        delete req.clubId;
                        delete req.id;

                        return angular.toJson(req);
                    }
                },
                combinedFeatures: {
                    method: 'GET',
                    url: 'api/v1/club/:clubId/area/:id/combinedFeatures'
                },
                copy: {
                    method: 'POST',
                    url: 'api/v1/club/:clubId/area/:id/copy'
                }
            });
        })

        .component('rClubAreaCategorySelection', {
            templateUrl: 'club/area/r-club-area-category-selection.html',
            bindings: {
                activeTabIndex: '<'
            },
            controller: function ($state, $stateParams) {
                var $ctrl = this;

                var clubId = $stateParams.id;

                $ctrl.transitionToAreaList = function () {
                    $state.go('club.area.list', {clubId: clubId});
                };

                $ctrl.transitionToAreaProposals = function () {
                    $state.go('club.area.proposals', {clubId: clubId});
                };
            }
        })

        .service('ClubAreaListService', function ($filter, ClubAreas, HuntingYearService) {
            this.listHuntingYears = function (clubId) {
                return ClubAreas.huntingYears({clubId: clubId}).$promise.then(function (result) {
                    return _.map(result, HuntingYearService.toObj);
                });
            };

            this.list = function (clubId, huntingYear, activeOnly) {
                return ClubAreas.query({
                    clubId: clubId,
                    activeOnly: activeOnly,
                    year: huntingYear || HuntingYearService.getCurrent()

                }).$promise.then(function (areas) {
                    var i18n = $filter('rI18nNameFilter');

                    return _.sortBy(areas, function (area) {
                        var name = i18n(area);
                        return name ? name.toLowerCase() : null;
                    });
                });
            };

            this.selectActiveArea = function (areas, selectedAreaId) {
                var area = _.find(areas, 'id', selectedAreaId);
                return area || _.chain(areas).filter('active', true).first().value();
            };
        })

        .controller('ClubAreaListController', function ($location, $scope,
                                                        ClubAreas, ClubAreaFormSidebar, ClubAreaListService,
                                                        club, clubId, selectedAreaId, huntingYear, showDeactive,
                                                        rhyBounds, preloadedHuntingYears) {
            var $ctrl = this;

            $ctrl.areas = [];
            $ctrl.selectedAreaId = selectedAreaId;
            $ctrl.canCreateArea = club.canEdit;

            $ctrl.huntingYears = preloadedHuntingYears;
            $ctrl.selectedYear = huntingYear;

            $ctrl.rhyBounds = rhyBounds;
            $ctrl.showDeactive = showDeactive;

            $ctrl.$onInit = function () {
                $ctrl.reloadAreas();
            };

            $ctrl.createArea = function () {
                ClubAreaFormSidebar.addClubArea(clubId).then(function (area) {
                    $scope.$emit('areaChanged', area);
                });
            };

            $ctrl.reloadAreas = function () {
                ClubAreaListService.list(clubId, $ctrl.selectedYear, !$ctrl.showDeactive).then(function (areas) {
                    $ctrl.areas = areas;
                    focusSelectedArea();
                });
            };

            $scope.$on('areaChanged', function (e, area) {
                $ctrl.selectedAreaId = area.id;
                $ctrl.selectedYear = area.huntingYear;

                $ctrl.reloadAreas();

                if (!_.some($ctrl.huntingYears, 'year', $ctrl.selectedYear)) {
                    ClubAreaListService.listHuntingYears(clubId).then(function (result) {
                        $ctrl.huntingYears = result;
                    });
                }
            });

            $ctrl.onAreaSelect = function (area) {
                if (area && $ctrl.selectedAreaId !== area.id) {
                    showArea(area);
                }
            };

            function focusSelectedArea() {
                var selectedArea = ClubAreaListService.selectActiveArea($ctrl.areas, $ctrl.selectedAreaId);

                if (selectedArea) {
                    showArea(selectedArea);
                }
            }

            function showArea(area) {
                area.isOpen = true;

                $ctrl.selectedYear = area.huntingYear;
                $ctrl.selectedAreaId = area.id;

                $location.search({areaId: area.id});

                ClubAreas.combinedFeatures({clubId: clubId, id: area.id}).$promise.then(function (featureCollection) {
                    $ctrl.featureCollection = featureCollection;
                });
            }
        })

        .component('clubAreaList', {
            templateUrl: 'club/area/area-list.html',
            bindings: {
                areas: '<',
                canEdit: '<',
                onAreaSelect: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.selectArea = function ($event, area) {
                    $event.preventDefault();
                    $ctrl.onAreaSelect({'area': area});
                };

                $ctrl.getAreaToggleClasses = function (area) {
                    return {
                        'glyphicon': true,
                        'glyphicon-chevron-down': area.isOpen,
                        'glyphicon-chevron-right': !area.isOpen
                    };
                };
            }
        })

        .component('clubAreaListDetails', {
            templateUrl: 'club/area/area-details.html',
            bindings: {
                area: '<'
            },
            controller: function ($state, ActiveRoleService) {
                var $ctrl = this;

                function isContactPerson() {
                    return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();
                }

                $ctrl.canEditAreaGeometry = function () {
                    return isContactPerson() && $ctrl.area
                        && $ctrl.area.sourceType !== 'EXTERNAL';
                };

                $ctrl.editAreaGeometry = function () {
                    $state.go('club.map', {
                        areaId: $ctrl.area.id
                    });
                };
            }
        })

        .component('clubAreaFunctionDropdown', {
            templateUrl: 'club/area/area-functions.html',
            bindings: {
                area: '<'
            },
            controller: function ($q, $scope, $window, FormPostService, ActiveRoleService,
                                  ClubAreaFormSidebar, ClubAreaCopyModal, ClubAreaImportModal, AreaPrintModal) {
                var $ctrl = this;

                $ctrl.isContactPerson = function () {
                    return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();
                };

                function notifyAreaChange(area) {
                    $scope.$emit('areaChanged', area);
                }

                $ctrl.editArea = function () {
                    ClubAreaFormSidebar.editClubArea($ctrl.area).then(notifyAreaChange);
                };

                $ctrl.copyArea = function () {
                    ClubAreaCopyModal.copyClubArea($ctrl.area).then(notifyAreaChange);
                };

                $ctrl.importArea = function () {
                    ClubAreaImportModal.importArea($ctrl.area).then(notifyAreaChange);
                };

                $ctrl.printArea = function () {
                    AreaPrintModal.printArea('/api/v1/club/' + $ctrl.area.clubId + '/area/' + $ctrl.area.id + '/print');
                };

                $ctrl.exportExcel = function (type) {
                    FormPostService.submitFormUsingBlankTarget(exportBaseUri() + '/excel/' + type, {});
                };

                $ctrl.exportGeoJson = function () {
                    FormPostService.submitFormUsingBlankTarget(exportBaseUri() + '/zip', {});
                };

                $ctrl.exportGarmin = function () {
                    FormPostService.submitFormUsingBlankTarget(exportBaseUri() + '/garmin', {});
                };

                $ctrl.exportArea = function () {
                    $window.open(exportBaseUri() + '/zip');
                };

                function exportBaseUri() {
                    return '/api/v1/club/' + $ctrl.area.clubId + '/area/' + $ctrl.area.id;
                }

                $ctrl.isLocalArea = function () {
                    return $ctrl.area && $ctrl.area.sourceType !== 'EXTERNAL';
                };

                $ctrl.isAreaWithGeometry = function () {
                    return $ctrl.area && $ctrl.area.zoneId;
                };

                $ctrl.isLocalAreaWithGeometry = function () {
                    return $ctrl.isAreaWithGeometry() && $ctrl.isLocalArea();
                };
            }
        })

        .service('ClubAreaFormSidebar', function (FormSidebarService, GameDiaryParameters, HuntingYearService,
                                                  ClubAreas) {
            var formSidebar = FormSidebarService.create({
                templateUrl: 'club/area/area-form.html',
                controller: 'ClubAreaFormSidebarController',
                largeDialog: false,
                resolve: {
                    diaryParameters: function () {
                        return GameDiaryParameters.query().$promise;
                    }
                }
            }, ClubAreas, function (parameters) {
                return {area: _.constant(parameters.area)};
            });

            this.addClubArea = function (clubId) {
                return formSidebar.show({
                    area: {
                        clubId: clubId,
                        huntingYear: HuntingYearService.getCurrent()
                    }
                });
            };

            this.editClubArea = function (area) {
                return formSidebar.show({
                    id: area.id,
                    area: ClubAreas.get({clubId: area.clubId, id: area.id}).$promise
                });
            };
        })

        .controller('ClubAreaFormSidebarController', function (HuntingYearService, $scope, area, diaryParameters) {
            $scope.area = area;
            $scope.parameters = diaryParameters;
            $scope.getCategoryName = diaryParameters.$getCategoryName;
            $scope.getGameName = diaryParameters.$getGameName;

            var currentHuntingYear = HuntingYearService.getCurrent();
            var nextHuntingYear = currentHuntingYear + 1;
            var areaHuntingYear = area.huntingYear;

            $scope.huntingYears = _([currentHuntingYear, nextHuntingYear, areaHuntingYear])
                .filter()
                .uniq()
                .map(HuntingYearService.toObj)
                .value();

            $scope.save = function () {
                $scope.$close($scope.area);
            };

            $scope.cancel = $scope.$dismiss;

            $scope.activate = function () {
                $scope.area.active = true;
                $scope.$close($scope.area);
            };

            $scope.deactivate = function () {
                $scope.area.active = false;
                $scope.$close($scope.area);
            };
        })

        .service('AreaPrintModal', function ($uibModal, FormPostService) {
            this.printArea = function (url) {
                return $uibModal.open({
                    controller: ModalController,
                    templateUrl: 'club/area/area-print.html',
                    controllerAs: '$ctrl',
                    bindToController: true,
                    resolve: {
                        url: _.constant(url)
                    }
                });
            };

            function ModalController($uibModalInstance, url) {
                var $ctrl = this;

                $ctrl.request = {
                    paperSize: 'A4',
                    paperDpi: '300',
                    paperOrientation: 'PORTRAIT'
                };

                $ctrl.save = function () {
                    $uibModalInstance.close();
                    FormPostService.submitFormUsingBlankTarget(url, $ctrl.request);
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss();
                };
            }
        })

        .service('ClubAreaCopyModal', function ($uibModal, ClubAreas, HuntingYearService) {
            this.copyClubArea = function (area) {
                return $uibModal.open({
                    templateUrl: 'club/area/area-copy.html',
                    controller: ModalController,
                    controllerAs: '$ctrl',
                    bindToController: true,
                    resolve: {
                        area: _.constant(area)
                    }
                }).result.then(function (res) {
                    return ClubAreas.copy({clubId: area.clubId, id: res.id}, res).$promise;
                });
            };

            function ModalController($uibModalInstance, area) {
                var $ctrl = this;

                $ctrl.huntingYears = HuntingYearService.currentAndNextObj();
                $ctrl.area = area;
                $ctrl.areaCopyData = {
                    id: area.id,
                    huntingYear: area.huntingYear,
                    copyGroups: true
                };

                $ctrl.save = function () {
                    $uibModalInstance.close($ctrl.areaCopyData);
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss();
                };
            }
        })

        .service('ClubAreaImportModal', function ($q, $filter, $translate, $uibModal, dialogs, NotificationService) {
            this.importArea = function (area) {
                return confirmAreaImport(area).then(function () {
                    return $uibModal.open({
                        templateUrl: 'club/area/area-import.html',
                        size: 'sm',
                        controller: ModalController,
                        controllerAs: '$ctrl',
                        bindToController: true,
                        resolve: {
                            clubId: _.constant(area.clubId),
                            areaId: _.constant(area.id)
                        }
                    }).result.then(function () {
                        NotificationService.showMessage('club.area.import.success', 'success');
                        return area;
                    });
                });
            };

            function confirmAreaImport(area) {
                if (area.sourceType === 'EXTERNAL' || !area.zoneId) {
                    return $q.when(true);
                }

                var i18nFilter = $filter('rI18nNameFilter');
                var dialogTitle = $translate.instant('club.area.import.confirmTitle');
                var dialogMessage = $translate.instant('club.area.import.confirmBody', {
                    areaName: i18nFilter(area)
                });

                return dialogs.confirm(dialogTitle, dialogMessage).result;
            }

            function ModalController($uibModalInstance, clubId, areaId) {
                var $ctrl = this;

                $ctrl.url = '/api/v1/club/' + clubId + '/area/' + areaId + '/import';
                $ctrl.uploadButtonVisible = true;
                $ctrl.uploadInProgress = false;

                $ctrl.onUpload = function (files) {
                    $ctrl.uploadButtonVisible = false;
                    $ctrl.uploadInProgress = true;
                };

                $ctrl.onSuccess = function (response) {
                    $uibModalInstance.close();
                    NotificationService.showDefaultSuccess();
                };

                $ctrl.onError = function (response) {
                    $uibModalInstance.dismiss();
                    NotificationService.showDefaultFailure();
                };

                $ctrl.close = function () {
                    $uibModalInstance.dismiss();
                };
            }
        });

})();

