(function () {
    "use strict";

    angular.module('app.clubarea.services', ['ngResource'])
        .factory('ClubAreas', ClubAreas)
        .service('ClubAreaListService', ClubAreaListService);

    function ClubAreas($resource) {
        return $resource('api/v1/club/:clubId/area/:id', {"clubId": "@clubId", "id": "@id"}, {
            'query': {method: 'GET', params: {year: "@year"}, isArray: true},
            'get': {method: 'GET'},
            'update': {method: 'PUT'},
            'huntingYears': {
                method: 'GET',
                url: 'api/v1/club/:clubId/area/huntingyears',
                isArray: true
            },
            'getFeatures': {
                method: 'GET',
                url: 'api/v1/club/:clubId/area/:id/features'
            },
            'updateFeatures': {
                method: 'PUT',
                url: 'api/v1/club/:clubId/area/:id/features',
                transformRequest: function (req, headers) {
                    delete req.clubId;
                    delete req.id;

                    return angular.toJson(req);
                }
            },
            'combinedFeatures': {
                method: 'GET',
                url: 'api/v1/club/:clubId/area/:id/combinedFeatures'
            },
            'copy': {
                method: 'POST',
                url: 'api/v1/club/:clubId/area/:id/copy'
            }
        });
    }

    function ClubAreaListService($uibModal, $window, $filter,
                                 FormSidebarService, FormPostService,
                                 GameDiaryParameters, NotificationService,
                                 ClubAreas, HuntingYearService) {
        var formSidebar = createFormSidebar();

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
                area: angular.copy(area)
            });
        };

        this.list = function (clubId, huntingYear, activeOnly) {
            var params = {
                clubId: clubId,
                activeOnly: activeOnly,
                year: huntingYear || HuntingYearService.getCurrent()
            };

            return ClubAreas.query(params).$promise.then(sortAreaListByName);
        };

        this.listHuntingYears = function (clubId) {
            return ClubAreas.huntingYears({clubId: clubId}).$promise.then(function (result) {
                return _.map(result, HuntingYearService.toObj);
            });
        };

        this.firstActiveArea = function (areas) {
            return _.chain(areas).filter('active', true).first().value();
        };

        this.exportExcel = function (area, type) {
            var formSubmitAction = '/api/v1/club/' + area.clubId + '/area/' + area.id + '/excel/' + type;
            FormPostService.submitFormUsingBlankTarget(formSubmitAction, {});
        };

        this.exportGeoJson = function (area) {
            var formSubmitAction = '/api/v1/club/' + area.clubId + '/area/' + area.id + '/zip';
            FormPostService.submitFormUsingBlankTarget(formSubmitAction, {});
        };

        this.exportGarmin = function (area) {
            var formSubmitAction = '/api/v1/club/' + area.clubId + '/area/' + area.id + '/garmin';
            FormPostService.submitFormUsingBlankTarget(formSubmitAction, {});
        };

        this.copyClubArea = function (area) {
            return $uibModal.open({
                templateUrl: 'club/area/copy.html',
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    area: _.constant(area)
                },
                controller: 'ClubAreaCopyController'
            }).result.then(function (res) {
                return ClubAreas.copy({clubId: area.clubId, id: res.id}, res).$promise;
            });
        };

        this.importArea = function (area) {
            return $uibModal.open({
                templateUrl: 'club/area/import-geojson.html',
                size: 'sm',
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    clubId: _.constant(area.clubId),
                    areaId: _.constant(area.id)
                },
                controller: 'ClubAreaMapImportModalController'
            }).result.then(function() {
                NotificationService.showMessage('club.area.import.success', 'success');
            });
        };

        this.exportArea = function (area) {
            $window.open('api/v1/club/' + area.clubId + '/area/' + area.id + '/export');
        };

        function sortAreaListByName(areas) {
            var i18n = $filter('rI18nNameFilter');

            return _.sortBy(areas, function (area) {
                var name = i18n(area);
                return name ? name.toLowerCase() : null;
            });
        }

        function createFormSidebar() {
            var modalOptions = {
                controller: 'ClubAreaFormController',
                templateUrl: 'club/area/form.html',
                largeDialog: false,
                resolve: {
                    diaryParameters: _.constant(GameDiaryParameters.query().$promise)
                }
            };

            function parametersToResolve(parameters) {
                return {
                    area: _.constant(parameters.area)
                };
            }

            return FormSidebarService.create(modalOptions, ClubAreas, parametersToResolve);
        }
    }
})();
