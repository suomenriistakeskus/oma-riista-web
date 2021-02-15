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
                    rhyId: function ($stateParams) {
                        return $stateParams.id;
                    },
                    initialRhy: function (Rhys, rhyId) {
                        return Rhys.getPublicInfo({id: rhyId}).$promise;
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
                    moderatorView: _.constant(false),
                    tabs: function (Rhys, rhyId) {
                        return Rhys.searchParamOrganisations({id: rhyId}).$promise;
                    }
                }
            })
        ;
    })
    .controller('SrvaCallringRotationController', function ($state, NotificationService, Rhys, Helpers, rhyId, rotation) {

        var $ctrl = this;
        $ctrl.rotationOptions = ['DAILY','WEEKLY', 'MONTHLY'];
        $ctrl.rotation = rotation;
        $ctrl.rotationInUse = !!$ctrl.rotation.srvaRotation;

        $ctrl.save = function () {
            var rotation = $ctrl.rotationInUse ? $ctrl.rotation : {srvaRotation: null, startDate: null};

            var promise = Rhys.updateSrvaRotation({id: rhyId}, rotation).$promise;

            promise.then(function () {
                NotificationService.showDefaultSuccess();

                $state.reload();
            }, function () {
                NotificationService.showDefaultFailure();
            });
        };

    })
    .controller('SrvaEventMapController', function ($state, $q, Helpers, FormPostService,
                                                    MapDefaults, MapState, MapBounds, Markers,
                                                    DiaryListService, DiaryEntryRepositoryFactory,
                                                    DiaryListMarkerService,
                                                    SrvaEventMapSearchService, SrvaEventMapSearchParametersService,
                                                    parameters, moderatorView, initialRhy, tabs) {
        var $ctrl = this;

        $ctrl.searchParams = {};
        $ctrl.moderatorView = moderatorView;
        $ctrl.srvaSpecies = parameters.species;

        $ctrl.getGameName = parameters.$getGameName;
        $ctrl.getGameNameWithAmount = parameters.$getGameNameWithAmount;
        $ctrl.showEntry = DiaryListService.showSidebar();

        $ctrl.mapState = MapState.get();
        $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
        $ctrl.mapDefaults = MapDefaults.create();

        $ctrl.tabs = tabs;

        $ctrl.$onInit = function () {
            $ctrl.searchParams = SrvaEventMapSearchParametersService.pop();

            if (!$ctrl.searchParams) {
                var initialRhyCode = _.get(initialRhy, 'officialCode');
                $ctrl.searchParams = SrvaEventMapSearchParametersService.createEmpty(initialRhyCode);
            }

            $ctrl.search();
        };

        $ctrl.orgChanged = function (type, code) {
            if (type === 'RKA') {
                $ctrl.searchParams.rkaCode = code;
                $ctrl.searchParams.rhyCode = null;
                $ctrl.searchParams.htaCode = null;
            }
            if (type === 'RHY') {
                $ctrl.searchParams.rkaCode = null;
                $ctrl.searchParams.rhyCode = code;
                $ctrl.searchParams.htaCode = null;
            }
            if (type === 'HTA') {
                $ctrl.searchParams.rkaCode = null;
                $ctrl.searchParams.rhyCode = null;
                $ctrl.searchParams.htaCode = code;
            }
        };

        $ctrl.search = function () {
            SrvaEventMapSearchParametersService.push($ctrl.searchParams);

            SrvaEventMapSearchService.search(createRequest()).$promise.then(function (accidents) {
                $ctrl.markers = DiaryListMarkerService.createMarkersForDiaryEntryList(accidents, function (srvaEvent) {
                    $ctrl.showEntry(srvaEvent);
                });

                resolveMapBounds().then(function (defaultBounds) {
                    var entryBounds = DiaryListService.getEntryBounds(accidents, defaultBounds);
                    MapState.updateMapBounds(entryBounds, defaultBounds, true);
                });
            });
        };

        function resolveMapBounds() {
            if ($ctrl.searchParams.rhyCode) {
                return MapBounds.getRhyBounds($ctrl.searchParams.rhyCode);
            } else {
                return $q.when(MapBounds.getBoundsOfFinland());
            }
        }

        $ctrl.exportSrva = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/srva/search/excel', {
                json: angular.toJson(createRequest())
            });
        };

        function createRequest() {
            return SrvaEventMapSearchParametersService.createRequest(
                $ctrl.searchParams, moderatorView, _.get(initialRhy, 'id'));
        }
    })

    .controller('SrvaEventListController', function ($state, SrvaEventChangeStateService, SrvaEventListSearchService,
                                                     SrvaEventListSearchParametersService, MapDefaults, DiaryImageService,
                                                     Helpers, SrvaEventState, NotificationService, FormPostService,
                                                     parameters, initialRhy) {
        var $ctrl = this;

        $ctrl.searchParams = {};
        $ctrl.srvaSpecies = parameters.species;
        $ctrl.getGameName = parameters.$getGameName;
        $ctrl.getImageUrl = DiaryImageService.getUrl;
        $ctrl.getGameNameWithAmount = parameters.$getGameNameWithAmount;
        $ctrl.mapDefaults = MapDefaults.create();

        $ctrl.$onInit = function () {
            $ctrl.searchParams = SrvaEventListSearchParametersService.pop();
            if (!$ctrl.searchParams) {
                $ctrl.searchParams = SrvaEventListSearchParametersService.createEmpty();
                $ctrl.searchParams.rhyCode = _.get(initialRhy, 'officialCode');
            }
            $ctrl.search();
        };

        $ctrl.search = function () {
            SrvaEventListSearchParametersService.push($ctrl.searchParams);
            $ctrl.loadPage(0);
        };

        $ctrl.loadPage = function (page) {
            var pageRequest = {
                page: page,
                size: 10
            };

            SrvaEventListSearchService.searchPage(createRequest(), pageRequest).then(function (response) {
                $ctrl.results = response.data;
            });
        };

        $ctrl.exportSrva = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/srva/search/excel', {
                json: angular.toJson(createRequest())
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

        function createRequest() {
            return SrvaEventListSearchParametersService.createRequest($ctrl.searchParams, _.get(initialRhy, 'id'));
        }

        function changeState(entry, newState) {
            SrvaEventChangeStateService.changeState({
                id: entry.id,
                rev: entry.rev,
                newState: newState
            }).$promise.then(function (answer) {
                entry.state = answer.state;
                NotificationService.showDefaultSuccess();

            }, function () {
                NotificationService.showDefaultFailure();

            }).finally(function () {
                $state.reload();
            });
        }
    });

