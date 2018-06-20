'use strict';

angular.module('app.srva.services', [])
    .constant('SrvaEventName', {
        accident: 'ACCIDENT',
        deportation: 'DEPORTATION',
        injuredAnimal: 'INJURED_ANIMAL'
    })

    .constant('SrvaEventState', {
        unfinished: 'UNFINISHED',
        approved: 'APPROVED',
        rejected: 'REJECTED'
    })

    .factory('SrvaEventChangeStateService', function ($resource) {
        return $resource('/api/v1/srva/changestate/:id', {"id": "@id"}, {
            'changeState': {
                method: 'PUT',
                params: {rev: '@rev', newState: '@newState'}
            }
        });
    })

    .factory('SrvaEventMapSearchService', function ($resource, DiaryEntryRepositoryFactory) {
        return DiaryEntryRepositoryFactory.decorateRepository($resource('api/v1/srva/search', {}, {
            'search': {
                method: 'POST',
                isArray: true
            }
        }));
    })

    .service('SrvaEventListSearchService', function ($http, SrvaOtherSpeciesService) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        this.searchPage = function (searchParams, pager) {
            return $http({
                method: 'POST',
                url: 'api/v1/srva/searchPage',
                params: pager,
                data: searchParams,
                transformResponse: appendTransform($http.defaults.transformResponse, function (data, headersGetter, status) {
                    if (status === 200 && angular.isObject(data)) {
                        SrvaOtherSpeciesService.replaceNullsWithOtherSpeciesCodeInEntries(data.content);
                        return data;
                    } else {
                        return data || {};
                    }
                })
            });
        };

        return this;
    })

    .service('SrvaEventListSearchParametersService', function (Helpers, SrvaEventState, SrvaEventName) {
        this.createEmpty = function () {
            return {
                gameSpeciesCode: null,
                dateRange: {
                    beginDate: null,
                    endDate: null
                },
                states: [
                    {name: SrvaEventState.unfinished, isChecked: true},
                    {name: SrvaEventState.approved, isChecked: false},
                    {name: SrvaEventState.rejected, isChecked: false}
                ],
                eventNames: [
                    {name: SrvaEventName.accident, isChecked: true},
                    {name: SrvaEventName.deportation, isChecked: true},
                    {name: SrvaEventName.injuredAnimal, isChecked: true}
                ]
            };
        };

        var _state = null;

        this.push = function (state) {
            _state = state;
        };

        this.pop = function () {
            var tmp = _state;
            _state = null;
            return tmp;
        };

        this.createRequest = function (searchParams, currentRhyId) {
            return {
                currentRhyId: currentRhyId,
                gameSpeciesCode: searchParams.gameSpeciesCode,
                rhyCode: searchParams.rhyCode,
                beginDate: Helpers.dateToString(searchParams.dateRange.beginDate),
                endDate: Helpers.dateToString(searchParams.dateRange.endDate),
                states: filterEnabledChoices(searchParams.states),
                eventNames: filterEnabledChoices(searchParams.eventNames)
            };
        };

        function filterEnabledChoices(choices) {
            return _.pluck(_.filter(choices, 'isChecked', true), 'name');
        }
    })

    .service('SrvaEventMapSearchParametersService', function (Helpers, SrvaEventState, SrvaEventName) {
        this.createEmpty = function (initialRhyCode) {
            return {
                gameSpeciesCode: null,
                rkaCode: null,
                rhyCode: initialRhyCode,
                htaCode: null,
                dateRange: {
                    beginDate: new Date(new Date().getFullYear(), 0, 1),
                    endDate: null
                },
                states: [
                    {name: SrvaEventState.unfinished, isChecked: true},
                    {name: SrvaEventState.approved, isChecked: true}
                ],
                eventNames: [
                    {name: SrvaEventName.accident, isChecked: true},
                    {name: SrvaEventName.deportation, isChecked: true},
                    {name: SrvaEventName.injuredAnimal, isChecked: true}
                ]
            };
        };

        var _state = null;

        this.push = function (state) {
            _state = state;
        };

        this.pop = function () {
            var tmp = _state;
            _state = null;
            return tmp;
        };

        this.createRequest = function (searchParams, moderatorView, currentRhyId) {
            return {
                moderatorView: moderatorView,
                currentRhyId: currentRhyId,
                gameSpeciesCode: searchParams.gameSpeciesCode,
                rkaCode: searchParams.rkaCode,
                rhyCode: searchParams.rhyCode,
                htaCode: searchParams.htaCode,
                beginDate: Helpers.dateToString(searchParams.dateRange.beginDate),
                endDate: Helpers.dateToString(searchParams.dateRange.endDate),
                states: filterEnabledChoices(searchParams.states),
                eventNames: filterEnabledChoices(searchParams.eventNames)
            };
        };

        function filterEnabledChoices(choices) {
            return _.pluck(_.filter(choices, 'isChecked', true), 'name');
        }
    });
