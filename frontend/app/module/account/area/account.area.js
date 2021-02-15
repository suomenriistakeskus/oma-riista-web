'use strict';

angular.module('app.account.area', [])
    .factory('AccountAreas', function ($resource) {
        var apiPrefix = 'api/v1/account/area/:id';

        return $resource(apiPrefix, {"id": "@id"}, {
            update: {method: 'PUT'},
            copy: {url: apiPrefix + '/copy', method: 'POST'},
            listPageForPerson: {
                url: 'api/v1/account/area/page/:personId',
                method: 'GET',
                isArray: false
            },
            listForPerson: {
                url: 'api/v1/account/area/list/:personId',
                method: 'GET',
                isArray: true
            },
            getFeatures: {
                method: 'GET',
                url: apiPrefix + '/features'
            },
            saveFeatures: {
                method: 'PUT',
                url: apiPrefix + '/features'
            },
            combinedFeatures: {
                method: 'GET',
                url: apiPrefix + '/combinedFeatures'
            },
            findByExternalId: {
                method: 'GET',
                url: 'api/v1/account/area/by-external-id/:areaExternalId',
                isArray: false
            }
        });
    })

    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.areas', {
                url: '/areas',
                abstract: true,
                templateUrl: 'account/area/account-areas.html',
                resolve: {
                    personId: function ($stateParams, ActiveRoleService) {
                        return ActiveRoleService.isModerator() ? $stateParams.id : 'me';
                    }
                }
            })
            .state('profile.areas.personal', {
                url: '/personal',
                templateUrl: 'account/area/account-areas-personal.html',
                resolve: {
                    areaList: function (AccountAreas, personId) {
                        return AccountAreas.listPageForPerson({personId: personId, page: 0, size: 10}).$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, ActiveRoleService, AccountAreas, AccountAreaModal,
                                      personId, areaList) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.areaList = areaList;
                        $ctrl.moderatorView = ActiveRoleService.isModerator();
                    };

                    $ctrl.loadPage = function (page) {
                        AccountAreas.listPageForPerson({
                            personId: personId,
                            page: page,
                            size: 10
                        }).$promise.then(function (res) {
                            $ctrl.areaList = res;
                        });
                    };

                    $ctrl.createArea = function () {
                        AccountAreaModal.open({}).then(function () {
                            $state.reload();
                        });
                    };

                }

            });
    })
    .component('personalAreaListMap', {
        templateUrl: 'account/area/list/area-list-map.html',
        bindings: {
            area: '<',
            areaType: '<'
        },
        controller: function ($filter, $translate, MapDefaults, MapBounds) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.mapDefaults = MapDefaults.create();
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
                $ctrl.mapId = 'map-' + $ctrl.area.id;
                $ctrl.mapBounds = $ctrl.area.bounds
                    ? convertAreaBounds($ctrl.area.bounds)
                    : MapBounds.getBoundsOfMmlBasemap();

                var vectorLayerTemplate = _.template('/api/v1/vector/' + $ctrl.areaType + '/<%= id %>/{z}/{x}/{y}');

                $ctrl.vectorLayer = {
                    url: vectorLayerTemplate({id: $ctrl.area.id}),
                    bounds: $ctrl.mapBounds
                };
            };

            function convertAreaBounds(bounds) {
                return {
                    southWest: {lat: bounds.minLat, lng: bounds.minLng},
                    northEast: {lat: bounds.maxLat, lng: bounds.maxLng}
                };
            }
        }
    })
;
