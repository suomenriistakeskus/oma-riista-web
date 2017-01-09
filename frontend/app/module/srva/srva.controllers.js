'use strict';

angular.module('app.srva.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('srva', {
                abstract: true,
                url: '/srva/{id:[0-9]{1,8}}',
                templateUrl: 'srva/layout.html',
                resolve: {
                    parameters: function (GameDiarySrvaParameters) {
                        return GameDiarySrvaParameters.query().$promise;
                    },
                    orgId: function ($stateParams) {
                        return $stateParams.id;
                    }
                }
            })
            .state('srva.list', {
                url: '/list',
                templateUrl: 'srva/srva-events.html',
                controller: 'SrvaEventListController',
                controllerAs: '$ctrl'
            })
            .state('srva.map', {
                url: '/map',
                wideLayout: true,
                templateUrl: 'srva/srva-map.html',
                controller: 'SrvaEventMapController',
                controllerAs: '$ctrl',
                resolve: {
                    areas: function (Areas) {
                        return Areas.query().$promise;
                    }
                }
            })
        ;
    })

    .controller('SrvaEventMapController', function ($state, SrvaEventMapSearchService, MapDefaults, DiaryEntryUrl,
                                                    Helpers, MapState, Markers, DiaryListService,
                                                    DiaryEntryRepositoryFactory, DiaryListMarkerService,
                                                    SrvaEventMapSearchParametersService, FormPostService,
                                                    areas, parameters, orgId) {
        var $ctrl = this;

        $ctrl.areas = areas;
        $ctrl.srvaSpecies = parameters.species;
        $ctrl.getGameName = parameters.$getGameName;
        $ctrl.getGameNameWithAmount = parameters.$getGameNameWithAmount;

        $ctrl.mapState = MapState.get();
        $ctrl.mapState.center = {};
        $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
        $ctrl.mapDefaults = MapDefaults.create();

        $ctrl.showEntry = DiaryListService.showSidebar();

        $ctrl.searchParams = [];

        $ctrl.updateRhys = function (area) {
            $ctrl.searchParams.rhy = null;
            $ctrl.rhys = _($ctrl.areas).map(function (a) {
                if (!area || a === area) {
                    return a.subOrganisations;
                }
            }).flatten().compact().value();
        };

        $ctrl.searchParams.states = SrvaEventMapSearchParametersService.getStates();
        $ctrl.searchParams.eventNames = SrvaEventMapSearchParametersService.getEventNames();
        $ctrl.searchParams.dateRange = SrvaEventMapSearchParametersService.getDateRange();
        $ctrl.searchParams.gameSpeciesCode = SrvaEventMapSearchParametersService.getGameSpeciesCode();
        $ctrl.searchParams.rka = SrvaEventMapSearchParametersService.getRka();
        $ctrl.updateRhys($ctrl.searchParams.rka);
        $ctrl.searchParams.rhy = SrvaEventMapSearchParametersService.getRhy();

        // Use currently selected rhy as default at the first time
        if (_.isUndefined($ctrl.searchParams.rhy)) {
            $ctrl.searchParams.rhy =
                _.find(_.flatten(_.pluck($ctrl.areas, 'subOrganisations')), {'id': _.parseInt(orgId)});
        }

        var getEnabled = function (data) {
            return _.pluck(_.filter(data, {'isChecked': true}), 'name');
        };

        var saveSearchParams = function () {
            SrvaEventMapSearchParametersService.saveStates($ctrl.searchParams.states);
            SrvaEventMapSearchParametersService.saveEventNames($ctrl.searchParams.eventNames);
            SrvaEventMapSearchParametersService.saveDateRange($ctrl.searchParams.dateRange);
            SrvaEventMapSearchParametersService.saveGameSpeciesCode($ctrl.searchParams.gameSpeciesCode);
            SrvaEventMapSearchParametersService.saveRhy($ctrl.searchParams.rhy);
            SrvaEventMapSearchParametersService.saveRka($ctrl.searchParams.rka);
        };

        function getSearchParams() {
            return {
                currentRhyId: orgId,
                states: getEnabled($ctrl.searchParams.states),
                eventNames: getEnabled($ctrl.searchParams.eventNames),
                beginDate: Helpers.dateToString($ctrl.searchParams.dateRange.beginDate),
                endDate: Helpers.dateToString($ctrl.searchParams.dateRange.endDate),
                gameSpeciesCode: $ctrl.searchParams.gameSpeciesCode,
                rhyId: _.get($ctrl.searchParams, 'rhy.id', null),
                rkaId: _.get($ctrl.searchParams, 'rka.id', null)
            };
        }

        $ctrl.search = function () {
            saveSearchParams();
            SrvaEventMapSearchService.search(getSearchParams()).$promise.then(
                function (accidents) {
                    $ctrl.mapState.viewBounds = DiaryListService.getEntryBounds(accidents);
                    $ctrl.markers = DiaryListMarkerService.createMarkersForDiaryEntryList(accidents, function (srvaEvent) {
                        $ctrl.showEntry(srvaEvent);
                    });
                }
            );
        };

        $ctrl.exportSrva = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/srva/search/excel', {json: angular.toJson(getSearchParams())});
        };

        $ctrl.search();
    })
    .controller('SrvaEventListController', function ($state, SrvaEventChangeStateService, SrvaEventListSearchService,
                                                     SrvaEventListSearchParametersService, MapDefaults, DiaryEntryUrl,
                                                     Helpers, SrvaEventState, NotificationService, FormPostService,
                                                     parameters, orgId) {
        var $ctrl = this;

        $ctrl.srvaSpecies = parameters.species;
        $ctrl.getGameName = parameters.$getGameName;
        $ctrl.getGameNameWithAmount = parameters.$getGameNameWithAmount;

        $ctrl.mapDefaults = MapDefaults.create();
        $ctrl.getImageUrl = DiaryEntryUrl.getUrl;

        $ctrl.searchParams = [];
        $ctrl.searchParams.states = SrvaEventListSearchParametersService.getStates();
        $ctrl.searchParams.eventNames = SrvaEventListSearchParametersService.getEventNames();
        $ctrl.searchParams.dateRange = SrvaEventListSearchParametersService.getDateRange();
        $ctrl.searchParams.gameSpeciesCode = SrvaEventListSearchParametersService.getGameSpeciesCode();

        $ctrl.pager = {
            page: 1,
            pageSize: 10,
            total: 0,
            sort: 'pointOfTime,DESC'
        };

        var saveSearchParams = function () {
            SrvaEventListSearchParametersService.saveStates($ctrl.searchParams.states);
            SrvaEventListSearchParametersService.saveEventNames($ctrl.searchParams.eventNames);
            SrvaEventListSearchParametersService.saveDateRange($ctrl.searchParams.dateRange);
            SrvaEventListSearchParametersService.saveGameSpeciesCode($ctrl.searchParams.gameSpeciesCode);
        };

        var getEnabled = function (data) {
            return _.pluck(_.filter(data, {'isChecked': true}), 'name');
        };

        function getSearchParams() {
            var searchParams = {
                states: getEnabled($ctrl.searchParams.states),
                eventNames: getEnabled($ctrl.searchParams.eventNames),
                beginDate: Helpers.dateToString($ctrl.searchParams.dateRange.beginDate),
                endDate: Helpers.dateToString($ctrl.searchParams.dateRange.endDate),
                gameSpeciesCode: $ctrl.searchParams.gameSpeciesCode,
                rhyId: orgId,
                currentRhyId: orgId
            };
            return searchParams;
        }

        $ctrl.search = function (resetPager) {
            saveSearchParams();
            if (resetPager) {
                $ctrl.pager.page = 1;
            }

            var pageRequest = {
                page: $ctrl.pager.page - 1,
                size: $ctrl.pager.pageSize,
                sort: $ctrl.pager.sort
            };

            SrvaEventListSearchService.searchPage(getSearchParams(), pageRequest).then(function (response) {
                $ctrl.srvaEvents = response.data.content;
                $ctrl.pager.total = response.data.total;
            });
        };

        $ctrl.exportSrva = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/srva/search/excel', {json: angular.toJson(getSearchParams())});
        };

        var changeState = function (entry, newState) {
            SrvaEventChangeStateService.changeState({
                id: entry.id,
                rev: entry.rev,
                newState: newState
            }).$promise
                .then(function (answer) {
                    entry.state = answer.state;
                    NotificationService.showDefaultSuccess();
                }, NotificationService.showDefaultFailure)
                .finally(function () {
                    $state.reload();
                });
        };

        $ctrl.acceptSrvaEvent = function (entry) {
            changeState(entry, SrvaEventState.approved);
        };

        $ctrl.rejectSrvaEvent = function (entry) {
            changeState(entry, SrvaEventState.rejected);
        };

        $ctrl.edit = function (entry) {
            $state.go('profile.diary.editSrva', {id: 'me', entryId: entry.id});
        };

        $ctrl.search();
    })
;

