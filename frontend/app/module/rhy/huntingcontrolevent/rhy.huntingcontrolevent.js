"use strict";

angular.module('app.rhy.huntingcontrolevent.state', [])
    .config(function ($stateProvider) {

        function safeYear(year) {
            return _.isNil(year) ? new Date().getFullYear() : year;
        }

        $stateProvider
            .state('rhy.huntingcontrolevent', {
                abstract: true,
                template: '<ui-view autoscroll="false"/>',
                resolve: {
                    rhy: function (Rhys, rhyId) {
                        return Rhys.getPublicInfo({id: rhyId}).$promise;
                    },
                    rhyBounds: function (MapBounds, rhy) {
                        return MapBounds.getRhyBounds(rhy.officialCode);
                    },
                    rhyGeoJson: function (GIS, rhy) {
                        return GIS.getInvertedRhyGeoJSON(rhy.officialCode, rhy.id, { name: rhy.nameFI});
                    }
                }
            })
            .state('rhy.huntingcontrolevent.coordinator', {
                url: '/huntingcontrolevent/rhy',
                templateUrl: 'rhy/huntingcontrolevent/list.html',
                controller: 'HuntingControlEventListController',
                controllerAs: '$ctrl',
                resolve: {
                    availableYears: function (HuntingControlEvents, rhyId) {
                        return HuntingControlEvents.listYears({rhyId: rhyId}).$promise;
                    },
                    refreshEvents: function (HuntingControlEvents) {
                        return function (rhyId, year) {
                            return HuntingControlEvents.list({rhyId: rhyId, year: safeYear(year)}).$promise;
                        };
                    },
                    events: function (HuntingControlEvents, rhyId, availableYears, refreshEvents) {
                        return refreshEvents(rhyId, _.last(availableYears));
                    }
                }
            })
            .state('rhy.huntingcontrolevent.gamewarden', {
                url: '/huntingcontrolevent/gamewarden',
                templateUrl: 'rhy/huntingcontrolevent/list.html',
                controller: 'HuntingControlEventListController',
                controllerAs: '$ctrl',
                resolve: {
                    availableYears: function(HuntingControlEvents, rhyId) {
                        return HuntingControlEvents.listMyYears({rhyId: rhyId}).$promise;
                    },
                    refreshEvents: function (HuntingControlEvents) {
                        return function (rhyId, year) {
                            return HuntingControlEvents.listMy({ rhyId: rhyId, year: safeYear(year)}).$promise;
                        };
                    },
                    events: function (HuntingControlEvents, rhyId, availableYears, refreshEvents) {
                        return refreshEvents(rhyId, _.last(availableYears));
                    }
                }
            });
    })

    .factory('HuntingControlEvents', function ($resource) {
        return $resource('api/v1/riistanhoitoyhdistys/:rhyId/huntingcontrolevents/:year', {rhyId: '@rhyId', year: '@calendarYear'}, {
            list: {
                method: 'GET',
                isArray: true
            },
            update: {
                method: 'PUT',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id'
            },
            delete: {
                method: 'DELETE',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id'
            },
            deleteAttachment: {
                method: 'DELETE',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/attachment/:id'
            },
            listAttachments: {
                method: 'GET',
                isArray: true,
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id/attachments'
            },
            listInspectors: {
                method: 'GET',
                isArray: false,
                params: {rhyId: '@rhyId', date: '@date'},
                url: 'api/v1/riistanhoitoyhdistys/:rhyId/huntingcontrolevents/inspectors?date=:date'
            },
            listYears: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:rhyId/huntingcontrolevents/years',
                isArray: true
            },
            listMy: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:rhyId/huntingcontrolevents/my/:year',
                isArray: true
            },
            listMyYears: {
                method: 'GET',
                url: 'api/v1/riistanhoitoyhdistys/:rhyId/huntingcontrolevents/my/years',
                isArray: true
            },
            accept: {
                method: 'PUT',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id/accept'
            },
            acceptSubsidized: {
                method: 'PUT',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id/acceptsubsidized'
            },
            reject: {
                method: 'PUT',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id/reject'
            }
        });
    })
    .component('rHuntingControllerEventBrief', {
        templateUrl: 'rhy/huntingcontrolevent/brief.html',
        bindings: {
            event: '<'
        }
    })
    .component('rHuntingControllerEventStatus', {
        templateUrl: 'rhy/huntingcontrolevent/status.html',
        bindings: {
            event: '<'
        }
    })
    .constant('HuntingControlCooperationTypes', [
        'POLIISI',
        'RAJAVARTIOSTO',
        'MH',
        'OMA'
    ])
    .constant('WolfTerritory', [
        true,
        false
    ])
    .constant('HuntingControlEventTypes', [
        'MOOSELIKE_HUNTING_CONTROL',
        'LARGE_CARNIVORE_HUNTING_CONTROL',
        'GROUSE_HUNTING_CONTROL',
        'WATERFOWL_HUNTING_CONTROL',
        'DOG_DISCIPLINE_CONTROL',
        'OTHER'
    ])
    .constant('HuntingControlEventStatus', {
        PROPOSED: 'PROPOSED',
        REJECTED: 'REJECTED',
        ACCEPTED: 'ACCEPTED',
        ACCEPTED_SUBSIDIZED: 'ACCEPTED_SUBSIDIZED'
    })
    .constant('HuntingControlEventTypeFilter', {
        NOT_AVAILABLE: 'NOT_AVAILABLE'
    })
    .constant('HuntingControlEventSubsidizedFilter', {
        NOT_SUBSIDIZED: 'ACCEPTED',
        SUBSIDIZED: 'ACCEPTED_SUBSIDIZED'
    })
    .service('HuntingControlEventStatusFilter', function (HuntingControlEventStatus) {
        var self = this;

        var ACCEPTED_OR_SUBSIDIZED = 'ACCEPTED_OR_SUBSIDIZED';
        var PROPOSED = 'PROPOSED';
        var REJECTED = 'REJECTED';

        _.assign(self, {
            'ACCEPTED_OR_SUBSIDIZED': ACCEPTED_OR_SUBSIDIZED,
            'PROPOSED': PROPOSED,
            'REJECTED': REJECTED
        });

        self.asEventStatus = function (statusFilter) {
            switch (statusFilter) {
                case self.PROPOSED:
                    return HuntingControlEventStatus.PROPOSED;
                case self.REJECTED:
                    return HuntingControlEventStatus.REJECTED;
                default:
                    console.error('Cannot convert', statusFilter, 'to event HuntingControlEventStatus');
                    return null;
            }
        };

        self.isAcceptedOrSubsidized = function (eventStatus) {
            return eventStatus === HuntingControlEventStatus.ACCEPTED || eventStatus === HuntingControlEventStatus.ACCEPTED_SUBSIDIZED;
        };

    });
