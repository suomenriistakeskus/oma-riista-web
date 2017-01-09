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

    .service('SrvaEventChangeStateService', function ($resource) {
        return $resource('/api/v1/srva/changestate/:id', {"id": "@id"}, {
            'changeState': {
                method: 'PUT',
                params: {rev: '@rev', newState: '@newState'}
            }
        });
    })
    .service('SrvaEventListSearchService', function ($http, SrvaOtherSpeciesService) {
        this.searchPage = function (searchParams, pager) {
            return $http({
                method: 'POST',
                url: 'api/v1/srva/searchPage',
                params: pager,
                data: searchParams,
                transformResponse: function (data, headers, status) {
                    var result = angular.fromJson(data);

                    if (status >= 400) {
                        return result;
                    }

                    SrvaOtherSpeciesService.replaceNullsWithOtherSpeciesCodeInEntries(result.content);
                    return result;
                }
            });
        };

        return this;
    })
    .service('SrvaEventMapSearchService', function ($resource, DiaryEntryRepositoryFactory) {
        var r = $resource('api/v1/srva/search', {}, {
            'search': {
                method: 'POST',
                isArray: true
            }
        });
        return DiaryEntryRepositoryFactory.decorateRepository(r);
    })
    .service('SrvaEventListSearchParametersService', function (SrvaEventState, SrvaEventName) {
        var _states = [
            {name: SrvaEventState.unfinished, 'isChecked': true},
            {name: SrvaEventState.approved, 'isChecked': false},
            {name: SrvaEventState.rejected, 'isChecked': false}
        ];

        var _eventNames = [
            {name: SrvaEventName.accident, 'isChecked': true},
            {name: SrvaEventName.deportation, 'isChecked': true},
            {name: SrvaEventName.injuredAnimal, 'isChecked': true}
        ];

        var _dateRange = {
            beginDate: null,
            endDate: null
        };

        var _gameSpeciesCode = null;

        this.saveStates = function (states) {
            _states = states;
        };

        this.getStates = function () {
            return _states;
        };

        this.saveEventNames = function (eventNames) {
            _eventNames = eventNames;
        };

        this.getEventNames = function () {
            return _eventNames;
        };

        this.saveDateRange = function (dateRange) {
            _dateRange = dateRange;
        };

        this.getDateRange = function () {
            return _dateRange;
        };

        this.saveGameSpeciesCode = function (gameSpeciesCode) {
            _gameSpeciesCode = gameSpeciesCode;
        };

        this.getGameSpeciesCode = function () {
            return _gameSpeciesCode;
        };
    })
    .service('SrvaEventMapSearchParametersService', function (SrvaEventState, SrvaEventName) {
        var _states = [
            {name: SrvaEventState.unfinished, 'isChecked': true},
            {name: SrvaEventState.approved, 'isChecked': true},
        ];

        var _eventNames = [
            {name: SrvaEventName.accident, 'isChecked': true},
            {name: SrvaEventName.deportation, 'isChecked': true},
            {name: SrvaEventName.injuredAnimal, 'isChecked': true}
        ];

        var _dateRange = {
            beginDate: new Date(new Date().getFullYear(), 0, 1),
            endDate: null
        };

        var _gameSpeciesCode = null;
        // not defined is used to determine if value has been ever set.
        var _rhy;
        var _rka = null;

        this.saveStates = function (states) {
            _states = states;
        };

        this.getStates = function () {
            return _states;
        };

        this.saveEventNames = function (eventNames) {
            _eventNames = eventNames;
        };

        this.getEventNames = function () {
            return _eventNames;
        };

        this.saveDateRange = function (dateRange) {
            _dateRange = dateRange;
        };

        this.getDateRange = function () {
            return _dateRange;
        };

        this.saveGameSpeciesCode = function (gameSpeciesCode) {
            _gameSpeciesCode = gameSpeciesCode;
        };

        this.getGameSpeciesCode = function () {
            return _gameSpeciesCode;
        };

        this.saveRhy = function (rhy) {
            _rhy = rhy;
        };

        this.getRhy = function () {
            return _rhy;
        };
        this.saveRka = function (rka) {
            _rka = rka;
        };

        this.getRka = function () {
            return _rka;
        };
    })
    .service('SrvaSelectedSubpageService', function (SrvaEventState, SrvaEventName) {
        var _selected = 'events';

        this.getSelected = function () {
            return _selected;
        };

        this.setSelected = function(selected) {
            _selected = selected;
        };
    })
;
