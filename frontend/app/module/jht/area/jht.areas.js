'use strict';

angular.module('app.jht.area', [])
    .factory('ModeratorAreas', function ($resource) {
        var apiPrefix = 'api/v1/moderator/area/:id';

        return $resource(apiPrefix, {"id": "@id"}, {
            update: {method: 'PUT'},
            search: {
                url: 'api/v1/moderator/area/search',
                method: 'POST'
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
            copy: {
                method: 'POST',
                url: apiPrefix + '/copy',
                params: {year: '@year'}
            }
        });
    })

    .config(function ($stateProvider) {
        $stateProvider.state('jht.areas', {
            url: '/areas',
            templateUrl: 'jht/area/jht-areas.html',
            resolve: {
                rkaList: function (Areas) {
                    return Areas.query().$promise;
                }
            },
            controllerAs: '$ctrl',
            controller: function ($state, ActiveRoleService, ModeratorAreas, ModeratorAreaModal, rkaList) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.rkaList = rkaList;
                    $ctrl.moderatorView = ActiveRoleService.isModerator();

                    var currentYear = new Date().getFullYear();
                    var beginYear = Math.max(currentYear - 10, 2019);
                    var endYear = currentYear + 1;
                    $ctrl.yearOptions = _.range(beginYear, endYear + 1);

                    $ctrl.loadPage(0);
                };

                $ctrl.clearFilter = function() {
                  $ctrl.year = null;
                  $ctrl.rkaCode = null;
                  $ctrl.searchText = null;
                  $ctrl.onFilterChange();
                };

                $ctrl.hasFilters = function () {
                    return !!$ctrl.year || !!$ctrl.rkaCode || !!$ctrl.searchText;
                };

                $ctrl.onFilterChange = function () {
                    $ctrl.loadPage(0);
                };

                $ctrl.loadPage = function (page) {
                    var request = {
                        page: page,
                        size: 10,
                        year: $ctrl.year,
                        rkaCode: $ctrl.rkaCode,
                        searchText: $ctrl.searchText
                    };

                    ModeratorAreas.search(request).$promise.then(function (res) {
                        _.each(res.content, function (value, index) {
                            value.isCollapsed = (index > 0);
                        });

                        $ctrl.areaList = res;
                    });
                };

                $ctrl.createArea = function () {
                    ModeratorAreaModal.open({}).then(function () {
                        $state.reload();
                    });
                };
            }
        });
    })

    .component('moderatorAreaListMap', {
        templateUrl: 'jht/area/list/area-list-map.html',
        bindings: {
            area: '<'
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

                var vectorLayerTemplate = _.template('/api/v1/vector/moderator-area/<%= id %>/{z}/{x}/{y}');

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
    });
