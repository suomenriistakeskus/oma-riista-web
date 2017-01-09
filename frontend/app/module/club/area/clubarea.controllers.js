(function () {
    "use strict";

    angular.module('app.clubarea.controllers', [])
        .controller('ClubAreaListController', ClubAreaListController)
        .controller('ClubAreaFormController', ClubAreaFormController)
        .controller('ClubAreaCopyController', ClubAreaCopyController)
        .controller('ClubAreaMapImportModalController', ClubAreaMapImportModalController)
        .directive('clubAreaListDetails', ClubAreaListDetailsDirective)
        .config(configureState);

    function configureState($stateProvider) {
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
                templateUrl: 'club/area/list.html',
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
                    preselectedArea: function ($stateParams, ClubAreas, clubId) {
                        var areaId = $stateParams.areaId;
                        return areaId ? ClubAreas.get({clubId: clubId, id: areaId}).$promise : null;
                    },
                    areas: function (ClubAreaListService, clubId, preselectedArea) {
                        var huntingYear = preselectedArea ? preselectedArea.huntingYear : null;
                        var activeOnly = !preselectedArea || preselectedArea.active;
                        return ClubAreaListService.list(clubId, huntingYear, activeOnly);
                    },
                    area: function (preselectedArea, areas, ClubAreaListService) {
                        return preselectedArea ? preselectedArea : ClubAreaListService.firstActiveArea(areas);
                    },
                    huntingYears: function (ClubAreaListService, clubId) {
                        return ClubAreaListService.listHuntingYears(clubId);
                    },
                    featureCollection: function (ClubAreas, clubId, area) {
                        return area ? ClubAreas.combinedFeatures({clubId: clubId, id: area.id}).$promise : null;
                    }
                }
            });
    }

    function ClubAreaListController($scope, $location, ClubAreas, ClubAreaListService, HuntingYearService,
                                    clubId, areas, huntingYears, rhyBounds, club, area, featureCollection) {
        var $ctrl = this;

        $ctrl.club = club;
        $ctrl.areas = areas;
        $ctrl.huntingYears = huntingYears;
        $ctrl.rhyBounds = rhyBounds;

        $ctrl.showDeactive = false;
        $ctrl.selectedYear = HuntingYearService.getCurrent();
        $ctrl.selectedArea = null;

        initController();

        $ctrl.addArea = function () {
            ClubAreaListService.addClubArea(clubId).then(onSaveSuccessful);
        };

        $ctrl.showArea = function ($event, area) {
            $event.preventDefault();
            showSelectedArea(area);
            loadGeometry(area);
        };

        $ctrl.editArea = function ($event, area) {
            $event.preventDefault();
            ClubAreaListService.editClubArea(area).then(onSaveSuccessful);
        };

        $ctrl.copyArea = function ($event, area) {
            $event.preventDefault();
            ClubAreaListService.copyClubArea(area).then(onSaveSuccessful);
        };

        $ctrl.onHuntingYearChange = function () {
            reloadAreas();
        };

        $ctrl.onShowDeActiveChange = function () {
            reloadAreas();
        };

        $ctrl.reloadAreas = reloadAreas;

        function reloadAreas() {
            ClubAreaListService.list(clubId, $ctrl.selectedYear, !$ctrl.showDeactive).then(updateAreaList);
        }

        function reloadHuntingYears() {
            ClubAreaListService.listHuntingYears(clubId).then(function (result) {
                $ctrl.huntingYears = result;
            });
        }

        function loadGeometry(area) {
            ClubAreas.combinedFeatures({clubId: clubId, id: area.id}).$promise.then(showFeatureCollection);
        }

        function onSaveSuccessful(result) {
            $ctrl.selectedYear = result.huntingYear;
            $ctrl.selectedArea = result;

            reloadAreas();

            // reload huntingYears if area year does not exist in option list
            if (!_.some($ctrl.huntingYears, 'year', $ctrl.selectedYear)) {
                reloadHuntingYears();
            }
        }

        function showSelectedArea(area) {
            $ctrl.selectedYear = area.huntingYear;
            $ctrl.selectedArea = area;

            $location.search({areaId: area.id});
        }

        function showFeatureCollection(featureCollection) {
            $ctrl.featureCollection = featureCollection;
        }

        function chooseActiveArea(areas) {
            var activeArea = null;

            if ($ctrl.selectedArea) {
                // If area is already selected, then make sure list contains it...
                activeArea = _.find(areas, 'id', $ctrl.selectedArea.id);
            }

            // ... or pick first active option
            return activeArea ? activeArea : ClubAreaListService.firstActiveArea(areas);
        }

        function updateAreaList(areas) {
            $ctrl.areas = areas;
            $ctrl.selectedArea = chooseActiveArea(areas);

            if ($ctrl.selectedArea) {
                showSelectedArea($ctrl.selectedArea);
                loadGeometry($ctrl.selectedArea);
            }
        }

        function initController() {
            if (area) {
                showSelectedArea(area);
                showFeatureCollection(featureCollection);
                $ctrl.showDeactive = !area.active;
            }
        }
    }

    function ClubAreaListDetailsDirective() {
        return {
            restrict: 'E',
            templateUrl: 'club/area/list-details.html',
            scope: {
                selectedArea: '=',
                reloadAreas: '&'
            },
            controllerAs: '$ctrl',
            bindToController: true,
            controller: ClubAreaListDetailsController
        };
    }

    function ClubAreaListDetailsController($q,
                                           $state,
                                           $translate,
                                           $filter,
                                           dialogs,
                                           ClubHuntingAreaPrintService,
                                           ClubAreaListService,
                                           ActiveRoleService) {
        var $ctrl = this;

        $ctrl.isContactPerson = function () {
            return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator() || ActiveRoleService.isAdmin();
        };

        $ctrl.editAreaGeometry = function () {
            $state.go('club.map', {
                areaId: $ctrl.selectedArea.id
            });
        };

        $ctrl.printArea = function () {
            ClubHuntingAreaPrintService.showModalDialog($ctrl.selectedArea);
        };

        function confirmAreaImport() {
            if (!$ctrl.isLocalAreaWithGeometry()) {
                return $q.when(true);
            }

            var i18nFilter = $filter('rI18nNameFilter');
            var dialogTitle = $translate.instant('club.area.import.confirmTitle');
            var dialogMessage = $translate.instant('club.area.import.confirmBody', {
                areaName: i18nFilter($ctrl.selectedArea)
            });

            return dialogs.confirm(dialogTitle, dialogMessage).result;
        }

        $ctrl.importArea = function () {
            return confirmAreaImport().then(function () {
                ClubAreaListService.importArea($ctrl.selectedArea).then($ctrl.reloadAreas);
            });
        };

        $ctrl.exportExcel = function (type) {
            ClubAreaListService.exportExcel($ctrl.selectedArea, type);
        };

        $ctrl.exportGeoJson = function () {
            ClubAreaListService.exportGeoJson($ctrl.selectedArea);
        };

        $ctrl.exportGarmin = function () {
            ClubAreaListService.exportGarmin($ctrl.selectedArea);
        };

        $ctrl.exportArea = function () {
            ClubAreaListService.exportArea($ctrl.selectedArea);
        };

        $ctrl.isLocalArea = function () {
            return $ctrl.selectedArea && $ctrl.selectedArea.sourceType !== 'EXTERNAL';
        };

        $ctrl.isAreaWithGeometry = function () {
            return $ctrl.selectedArea && $ctrl.selectedArea.zoneId;
        };

        $ctrl.isLocalAreaWithGeometry = function () {
            return $ctrl.isAreaWithGeometry() && $ctrl.isLocalArea();
        };
    }

    function ClubAreaFormController(HuntingYearService,
                                    $scope, area, diaryParameters) {
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
    }

    function ClubAreaCopyController(area, HuntingYearService) {
        var $ctrl = this;

        $ctrl.huntingYears = HuntingYearService.currentAndNextObj();
        $ctrl.area = area;
        $ctrl.areaCopyData = {
            id: area.id,
            huntingYear: area.huntingYear,
            copyGroups: true
        };

        $ctrl.save = function () {
            $ctrl.$close($ctrl.areaCopyData);
        };

        $ctrl.cancel = function () {
            $ctrl.$dismiss();
        };
    }

    function ClubAreaMapImportModalController($uibModalInstance,
                                              NotificationService,
                                              clubId, areaId) {
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
})();

