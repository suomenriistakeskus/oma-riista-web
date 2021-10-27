'use strict';

angular.module('app.harvestreport.search', [])

    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.harvestreport.list', {
                url: '/list',
                templateUrl: 'harvestreport/search/harvestreport-moderator-search.html',
                controller: 'ModeratorHarvestReportSearchController',
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: false,
                resolve: {
                    reportCategories: function (HarvestReportFieldsAndSeasons) {
                        return HarvestReportFieldsAndSeasons.valids();
                    },
                    areas: function (Areas) {
                        return Areas.query().$promise;
                    },
                    harvestReportLocalityResolver: function (HarvestReportLocalityResolver) {
                        return HarvestReportLocalityResolver.get();
                    }
                }
            });
    })

    .service('HarvestReportSearch', function ($http) {
        function _findPage(url, searchParams, pager) {
            return $http({
                method: 'POST',
                url: url,
                params: pager,
                data: searchParams
            });
        }

        this.findPageForAdmin = function (searchParams, pager) {
            return _findPage('api/v1/harvestreport/admin/search', searchParams, pager);
        };
    })

    .service('HarvestReportSearchSidebar', function (HarvestReportSearchHarvestSidebar) {
        this.createSidebar = function () {
            var currentSidebar = null;
            var selectedDiaryEntry = null;

            return function (diaryEntry) {
                if (currentSidebar !== null) {
                    if (selectedDiaryEntry && selectedDiaryEntry.id === diaryEntry.id) {
                        return;
                    }

                    currentSidebar.dismiss('ignore');
                    currentSidebar = null;
                }

                selectedDiaryEntry = diaryEntry;

                var modalInstance = HarvestReportSearchHarvestSidebar.showSidebar(diaryEntry);

                modalInstance.opened.then(function () {
                    currentSidebar = modalInstance;
                });

                modalInstance.result.finally(function (err) {
                    currentSidebar = null;
                });

                modalInstance.result.then(function () {
                    selectedDiaryEntry = null;
                }, function (err) {
                    if (err !== 'ignore') {
                        selectedDiaryEntry = null;
                    }
                });

                return modalInstance.result;
            };
        };
    })

    .service('HarvestReportSearchHarvestSidebar', function ($q, offCanvasStack) {
        this.showSidebar = function (harvest) {
            return offCanvasStack.open({
                controller: 'DiaryEntrySidebarController',
                templateUrl: 'diary/sidebar/diary-sidebar.html',
                largeDialog: false,
                resolve: {
                    entry: _.constant(harvest),
                    computedFields: _.constant(null)
                }
            });
        };
    })

    .service('HarvestReportSearchMarkers', function ($translate, Helpers, WGS84, Markers) {
        var markerDefaults = {
            draggable: false,
            icon: {
                type: 'awesomeMarker',
                prefix: 'fa', // font-awesome
                icon: 'crosshairs'
            },
            compileMessage: true,
            groupOption: {
                // Options to pass for leaflet.markercluster plugin

                //disableClusteringAtZoom: 13,
                showCoverageOnHover: true
            },
            group: 'HarvestReports',
            popupOptions: {
                // Options to pass for Leaflet popup message (L.popup)

                //keepInView: true,
                maxWidth: 400
            }
        };

        this.createMarkers = function (harvestList, clickHandler, getHarvestState) {
            return _.map(harvestList, function (h) {
                var latlng = WGS84.fromETRS(h.geoLocation.latitude, h.geoLocation.longitude);

                return angular.merge({}, markerDefaults, {
                    id: h.id,
                    lat: latlng.lat,
                    lng: latlng.lng,
                    clickHandler: clickHandler,
                    icon: {
                        markerColor: Markers.getColorForHarvestReportState(getHarvestState(h))
                    }
                });
            });
        };
    })

    .service('ModeratorSearchData', function (HuntingYearService) {
        var huntingYear = HuntingYearService.getCurrent();

        var initialSearch = {
            searchOnInit: false,
            currentPage: 0,
            beginDate: HuntingYearService.getBeginDateStr(huntingYear),
            endDate: null,
            states: {
                'SENT_FOR_APPROVAL': true,
                'REJECTED': false,
                'APPROVED': false
            },
            person: null,
            selectedReportCategory: null,
            harvestArea: null,
            area: null,
            rhy: null,
            permitNumber: null,
            textSearch: null
        };

        var currentSearch = angular.copy(initialSearch);

        this.clear = function () {
            currentSearch = angular.copy(initialSearch);
        };

        this.get = function () {
            return currentSearch;
        };
    })

    .controller('ModeratorHarvestReportSearchController', function ($state, $translate, HuntingYearService,
                                                                    Helpers, FormPostService, FetchAndSaveBlob, ModeratorSearchData,
                                                                    MapDefaults, MapState, MapBounds,
                                                                    Harvest, DiaryEntryService, PersonSearchModal,
                                                                    HarvestReportSearchSidebar, HarvestChangeHistoryModal,
                                                                    HarvestReportSearch, HarvestReportSearchMarkers,
                                                                    reportCategories, areas, harvestReportLocalityResolver,
                                                                    GameSpeciesCodes) {
        var $ctrl = this;

        $ctrl.getHarvestAreaName = harvestReportLocalityResolver.getHarvestAreaName;
        $ctrl.getAreaName = harvestReportLocalityResolver.getAreaName;
        $ctrl.getRhyName = harvestReportLocalityResolver.getRhyName;
        $ctrl.showSidebar = HarvestReportSearchSidebar.createSidebar();

        $ctrl.$onInit = function () {
            $ctrl.results = {
                content: [],
                hasNext: false
            };
            $ctrl.markers = [];
            $ctrl.reportCategories = reportCategories;
            $ctrl.areas = areas;
            $ctrl.harvestAreas = [];
            $ctrl.searchData = ModeratorSearchData.get();
            $ctrl.activeTabIndex = 0;

            $ctrl.mapDefaults = MapDefaults.create({
                dragging: true,
                minZoom: 5
            });

            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapBounds = MapBounds.getBoundsOfFinland();

            updateRhyList($ctrl.searchData.area);
            updateHarvestAreaList();

            if ($ctrl.searchData.searchOnInit) {
                $ctrl.search();
            }
        };

        function createSearchParams() {
            var selectedReportCategory = $ctrl.searchData.selectedReportCategory || {};

            return {
                seasonId: idOrNull(selectedReportCategory.season),
                gameSpeciesCode: selectedReportCategory.species ? selectedReportCategory.species.code : null,
                harvestAreaId: idOrNull($ctrl.searchData.harvestArea),
                areaId: idOrNull($ctrl.searchData.area),
                rhyId: idOrNull($ctrl.searchData.rhy),
                personId: idOrNull($ctrl.searchData.person),
                states: getStates(),
                text: $ctrl.searchData.textSearch,
                permitNumber: $ctrl.searchData.permitNumber,
                beginDate: Helpers.dateToString($ctrl.searchData.beginDate),
                endDate: Helpers.dateToString($ctrl.searchData.endDate)
            };
        }

        function getStates() {
            return _.chain($ctrl.searchData.states).map(function (value, key) {
                return value === true ? key : null;
            }).filter().value();
        }

        function idOrNull(v) {
            return v ? v.id : null;
        }

        $ctrl.canSearch = function () {
            return getStates().length > 0;
        };

        $ctrl.search = function () {
            $ctrl.activeTabIndex = 0;
            $ctrl.loadPage(0);
        };

        $ctrl.loadPage = function (page) {
            $ctrl.searchData.searchOnInit = true;
            $ctrl.searchData.currentPage = page;

            var pageRequest = {
                page: page,
                size: 200
            };

            var searchParams = createSearchParams();

            HarvestReportSearch.findPageForAdmin(searchParams, pageRequest).then(function (response) {
                $ctrl.results = response.data;

                $ctrl.results.content = _.map($ctrl.results.content, function (harvest) {
                    var harvestReportDate = moment(harvest.harvestReportDate, "YYYY-MM-DD[T]HH:mm:ss.SSS");
                    harvest.harvestReportDeltaHours = moment().diff(harvestReportDate, 'hours');

                    return new Harvest(harvest);
                });

                $ctrl.results = response.data;

                $ctrl.markers = HarvestReportSearchMarkers.createMarkers($ctrl.results.content, markerClickHandler, harvestToState);
                $ctrl.mapBounds = MapBounds.getBounds($ctrl.markers, _.identity, MapBounds.getBoundsOfFinland());
            });
        };

        function markerClickHandler(markerId) {
            $ctrl.showSidebar(_.find($ctrl.results.content, {id: markerId}));
        }

        function harvestToState(harvest) {
            return _.get(harvest, 'harvestReportState');
        }

        $ctrl.export = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/harvestreport/admin/search/excel', {
                json: angular.toJson(createSearchParams())
            });
        };

        $ctrl.findPerson = function () {
            PersonSearchModal.searchPerson(true, true).then(function (personInfo) {
                $ctrl.searchData.person = personInfo;
            });
        };

        $ctrl.clearPerson = function () {
            $ctrl.searchData.person = null;
        };

        function updateHarvestAreaList() {
            $ctrl.harvestAreas = _.get($ctrl.searchData.selectedReportCategory, 'harvestAreas', []);
        }

        $ctrl.onReportCategoryChanged = function () {
            var selectedReportCategory = $ctrl.searchData.selectedReportCategory;

            if (selectedReportCategory && selectedReportCategory.season) {
                var season = selectedReportCategory.season;

                $ctrl.searchData.beginDate = season.beginDate;
                $ctrl.searchData.endDate = season.endDate2 ? season.endDate2 : season.endDate;
            } else {
                $ctrl.searchData.beginDate = HuntingYearService.getBeginDateStr();
                $ctrl.searchData.endDate = null;
            }

            $ctrl.searchData.harvestArea = null;
            updateHarvestAreaList();
        };

        function updateRhyList(area) {
            if (area) {
                $ctrl.rhys = _.chain(areas).filter({id: area.id}).map('subOrganisations').filter().flatten().value();
            } else {
                $ctrl.rhys = _.chain(areas).map('subOrganisations').filter().flatten().value();
            }
        }

        $ctrl.onRkaChanged = function () {
            $ctrl.searchData.rhy = null;
            updateRhyList($ctrl.searchData.area);
        };

        $ctrl.createHarvestReportOnBehalf = function () {
            DiaryEntryService.createHarvest();
        };

        $ctrl.toggleHarvest = function (harvest) {
            harvest.toggle = !harvest.toggle;
        };

        $ctrl.harvestReportStateChanged = function (harvest, state) {
            harvest.harvestReportState = state;
            harvest.rev = harvest.rev + 1;
        };

        $ctrl.isAcceptedCarnivore = function (harvest) {
            return harvest.harvestReportState === 'APPROVED' &&
                GameSpeciesCodes.isCarnivoreSpecies(harvest.gameSpeciesCode);
        };

        $ctrl.exportCertificate = function (harvest) {
            FetchAndSaveBlob.post('/api/v1/harvest/certificate/' + harvest.id + '/print/pdf');
        };

        $ctrl.editHarvest = function (harvest) {
            MapState.updateMapCenter(harvest.geoLocation);
            DiaryEntryService.edit(harvest);
        };

        $ctrl.showHistory = function (harvest) {
            HarvestChangeHistoryModal.showModal(harvest.id);
        };
    });
