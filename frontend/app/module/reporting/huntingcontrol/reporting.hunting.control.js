'use strict';

angular.module('app.reporting.huntingcontrol', [])
    .config(function ($stateProvider) {
        $stateProvider.state('reporting.huntingcontrol', {
            url: '/huntingControlMap',
            wideLayout: true,
            templateUrl: 'reporting/huntingcontrol/reporting-hunting-control-map.html',
            controller: 'HuntingControlEventMapController',
            controllerAs: '$ctrl',
            resolve: {
                tabs: function (Rhys) {
                    return Rhys.searchParamOrganisations({id: null}).$promise.then(function (data) {
                        return _.filter(data, function (value) {
                            return value.type !== 'HTA';
                        });
                    });
                }
            }
        });
    })

    .service('HuntingControlEventMapService', function ($http) {
        this.search = function (params) {
            return $http({
                method: 'POST',
                url: '/api/v1/moderator/huntingcontrol/search',
                data: params
            }).then(function (res) {
                return res.data;
            });
        };
    })

    .controller('HuntingControlEventMapController', function ($state, $q, Helpers, FormPostService,
                                                              MapDefaults, MapState, MapBounds,
                                                              DiaryListService,
                                                              HuntingControlEventMapMarkers,
                                                              HuntingControlEventSidebar,
                                                              HuntingControlEventStatus,
                                                              HuntingControlEventTypes,
                                                              HuntingControlCooperationTypes,
                                                              HuntingControlEventMapService,
                                                              tabs
    ) {
        var $ctrl = this;

        $ctrl.mapState = MapState.get();
        $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
        $ctrl.mapDefaults = MapDefaults.create();
        $ctrl.tabs = tabs;

        $ctrl.orgChanged = function (type, code) {
            $ctrl.searchParams.organisationFilter.type = type;
            $ctrl.searchParams.organisationFilter.code = code;
        };

        $ctrl.searchParams = {
            dateRange: {
                beginDate: new Date(new Date().getFullYear(), 0, 1),
                endDate: null
            },
            statuses: [
                {
                    value: HuntingControlEventStatus.ACCEPTED_SUBSIDIZED,
                    name: 'rhy.huntingControlEvent.eventStatus.ACCEPTED_SUBSIDIZED',
                    isChecked: true
                },
                {
                    value: HuntingControlEventStatus.ACCEPTED,
                    name: 'rhy.huntingControlEvent.eventStatus.ACCEPTED',
                    isChecked: true
                },
                {
                    value: HuntingControlEventStatus.PROPOSED,
                    name: 'rhy.huntingControlEvent.eventStatus.PROPOSED',
                    isChecked: false
                },
                {
                    value: HuntingControlEventStatus.REJECTED,
                    name: 'rhy.huntingControlEvent.eventStatus.REJECTED',
                    isChecked: false
                }
            ],
            types: _.map(HuntingControlEventTypes, function (type) {
                return {value: type, name: 'rhy.huntingControlEvent.eventType.' + type, isChecked: true};
            }),
            cooperationTypes: _.map(HuntingControlCooperationTypes, function (type) {
                return {value: type, name: 'rhy.huntingControlEvent.cooperationType.' + type, isChecked: true};
            }),
            organisationFilter: {
                type: null,
                code: null
            }
        };

        $ctrl.$onInit = function () {
            $ctrl.search();
        };

        function createRequest() {
            var getCheckedValues = function (arr) {
                return _.chain(arr).filter('isChecked').map('value').value();
            };
            return {
                beginDate: Helpers.dateToString($ctrl.searchParams.dateRange.beginDate),
                endDate: Helpers.dateToString($ctrl.searchParams.dateRange.endDate),
                statuses: getCheckedValues($ctrl.searchParams.statuses),
                types: getCheckedValues($ctrl.searchParams.types),
                cooperationTypes: getCheckedValues($ctrl.searchParams.cooperationTypes),
                orgType: $ctrl.searchParams.organisationFilter.type,
                orgCode: $ctrl.searchParams.organisationFilter.code,
            };
        }

        $ctrl.search = function () {
            $ctrl.markers = [];

            HuntingControlEventMapService.search(createRequest()).then(function (huntingControlEvents) {
                $ctrl.markers = HuntingControlEventMapMarkers.createMapMarkers(huntingControlEvents, function (huntingControlEvent) {
                    HuntingControlEventSidebar.showSidebar(huntingControlEvent);
                });
                resolveMapBounds().then(function (defaultBounds) {
                    var entryBounds = DiaryListService.getEntryBounds(huntingControlEvents, defaultBounds);
                    MapState.updateMapBounds(entryBounds, defaultBounds, true);
                });
            });
        };

        function resolveMapBounds() {
            return $q.when(MapBounds.getBoundsOfFinland());
        }

        $ctrl.exportExcel = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/moderator/huntingcontrol/search/excel', {
                json: angular.toJson(createRequest())
            });
        };
    })

    .service('HuntingControlEventMapMarkers', function (Markers) {
        this.createMapMarkers = function (huntingControlEventList, showHandler) {
            var clickHandler = function (markerId) {
                var huntingControlEvent = _.find(huntingControlEventList, {
                    'id': _.parseInt(markerId)
                });

                if (huntingControlEvent) {
                    showHandler(huntingControlEvent);
                }
            };

            var getMarkerColor = function (huntingControlEvent) {
                var status = huntingControlEvent.status;
                if (status === 'REJECTED') {
                    return 'red';
                } else if (status === 'ACCEPTED_SUBSIDIZED' || status === 'ACCEPTED') {
                    return 'green';
                } else if (status === 'PROPOSED') {
                    return 'orange';
                }
                return 'red';
            };

            var getMarkerIconName = function (huntingControlEvent) {
                var status = huntingControlEvent.status;
                if (status === 'REJECTED') {
                    return 'remove';
                } else if (status === 'ACCEPTED_SUBSIDIZED' || status === 'ACCEPTED') {
                    return 'check';
                } else if (status === 'PROPOSED') {
                    return 'exclamation-triangle';
                }
                return 'remove';
            };

            var getMarkerIcon = function (huntingControlEvent) {
                return {
                    icon: getMarkerIconName(huntingControlEvent),
                    markerColor: getMarkerColor(huntingControlEvent)
                };
            };

            var createMarkerData = function (huntingControlEvent) {
                return [{
                    id: huntingControlEvent.id,
                    etrsCoordinates: huntingControlEvent.geoLocation,
                    icon: getMarkerIcon(huntingControlEvent),
                    clickHandler: clickHandler
                }];
            };
            var markerDefaults = {
                draggable: false,
                icon: {
                    type: 'awesomeMarker',
                    prefix: 'fa',
                    icon: 'check'
                },
                groupOption: {showCoverageOnHover: true},
                group: 'HuntingControlEvents'
            };
            return Markers.transformToLeafletMarkerData(huntingControlEventList, markerDefaults, createMarkerData);
        };

    })

    .service('HuntingControlEventSidebar', function (FormSidebarService) {
        var modalOptions = {
            controller: 'HuntingControlEventSidebarController',
            templateUrl: 'reporting/huntingcontrol/reporting-hunting-control-sidebar.html',
            largeDialog: false,
            resolve: {}
        };

        function parametersToResolve(parameters) {
            return {huntingControlEvent: _.constant(parameters.huntingControlEvent)};
        }

        var formSidebar = FormSidebarService.create(modalOptions, null, parametersToResolve);
        this.showSidebar = function (huntingControlEvent) {
            formSidebar.show({
                id: huntingControlEvent.id,
                huntingControlEvent: huntingControlEvent,
            });
        };
    })

    .controller('HuntingControlEventSidebarController', function ($scope, huntingControlEvent) {
        $scope.event = huntingControlEvent;

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    })
;
