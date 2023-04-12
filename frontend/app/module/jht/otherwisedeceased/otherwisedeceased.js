'use strict';

angular.module('app.jht.otherwisedeceased-main', [])
    .constant('OtherwiseDeceasedAges', [
        'ADULT',
        'YOUNG',
        'UNKNOWN'
    ])
    .constant('OtherwiseDeceasedGenders', [
        'FEMALE',
        'MALE',
        'UNKNOWN'
    ])
    .constant('OtherwiseDeceasedCauses', [
        'HIGHWAY_ACCIDENT',
        'RAILWAY_ACCIDENT',
        'SICKNESS_OR_STARVATION',
        'KILLED_BY_POLICES_ORDER',
        'NECESSITY',
        'ILLEGAL_KILLING',
        'UNDER_INVESTIGATION',
        'OTHER'
    ])
    .constant('OtherwiseDeceasedSources', [
        'RK',
        'RHY',
        'RVR',
        'POLICE',
        'RVL',
        'MH',
        'LUKE',
        'CITIZEN',
        'MEDIA',
        'OTHER'
    ])

    .config(function ($stateProvider) {
        $stateProvider.state('jht.otherwisedeceased', {
            url: '/otherwisedeceased',
            templateUrl: 'jht/otherwisedeceased/otherwisedeceased.html',
            controllerAs: '$ctrl',
            controller: 'OtherwiseDeceasedController'
        });
    })
    .controller('OtherwiseDeceasedController', function ($filter, $translate, ActiveRoleService, GIS, FetchAndSaveBlob,
                                                         OtherwiseDeceasedApi, OtherwiseDeceasedService,
                                                         OtherwiseDeceasedSearch, OtherwiseDeceasedEditModal) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.resultList = [];
            $ctrl.pageInfo = {
                hasNext: false,
                pageable: {
                    page: 0,
                    size: 10
                }
            };
            $ctrl.filter = defaultFilters();
            $ctrl.speciesFilters = OtherwiseDeceasedService.getSpeciesCodes();

            selectFistPage(); // Show only 1st page of items.

            $ctrl.sortBy = 'pointOfTime';
            $ctrl.sortByOrder = 'desc';
            $ctrl.sortByTranslator = null;
            $ctrl.sortByi18nPrefix = null;
        };

        $ctrl.setSpecies = function (species) {
            $ctrl.filter.species = species;
            $ctrl.onFilterChange();
        };

        $ctrl.isSortedBy = function (sortBy, order) {
            return $ctrl.sortBy === sortBy && $ctrl.sortByOrder === order;
        };

        $ctrl.selectPage = function (page) {
            $ctrl.pageInfo.pageable.page = page;
            reloadResults();
        };

        $ctrl.hasResults = function () {
            return !_.isEmpty($ctrl.filteredResults);
        };

        $ctrl.getDetails = function (item) {
            return OtherwiseDeceasedApi.getDetails({itemId: item.id}).$promise;
        };

        $ctrl.add = function () {
            OtherwiseDeceasedEditModal.open().then(reloadResults);
        };

        $ctrl.exportToExcel = function () {
            var filters = buildSearchParams();
            FetchAndSaveBlob.post('api/v1/deceased/excel', filters);
        };

        $ctrl.updateResults = function () {
            reloadResults();
        };

        $ctrl.onFilterChange = function () {
            if (dateRangeValid()) {
                selectFistPage();
            }
        };

        function dateRangeValid() {
            return $ctrl.filter.beginDate && $ctrl.filter.endDate;
        }

        function reloadResults() {
            OtherwiseDeceasedSearch.findPage(buildSearchParams(), $ctrl.pageInfo.pageable).then(
                function (response) {
                    var briefResults = response.data;
                    $ctrl.resultList = briefResults.content;
                    $ctrl.pageInfo.hasNext = briefResults.hasNext;
                    $ctrl.pageInfo.pageable.page = briefResults.pageable.page;
                });
        }

        function selectFistPage() {
            $ctrl.selectPage(0);
        }

        function defaultFilters() {
            var year = new Date().getFullYear().toString();
            return {
                beginDate: year + '-01-01',
                endDate: year + '-12-31',
                showRejected: false
            };
        }

        function buildSearchParams() {
            return {
                gameSpeciesCode: !!Number($ctrl.filter.species) ? Number($ctrl.filter.species) : null,
                beginDate: $ctrl.filter.beginDate,
                endDate: $ctrl.filter.endDate,
                rkaOfficialCode: $ctrl.filter.rka,
                rhyOfficialCode: $ctrl.filter.rhy,
                cause: !!$ctrl.filter.cause ? $ctrl.filter.cause : null,
                showRejected: $ctrl.filter.showRejected
            };
        }
    });
