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
            controller: 'OtherwiseDeceasedController',
            resolve: {
                results: function (OtherwiseDeceasedService) {
                    return OtherwiseDeceasedService.getResults(new Date().getFullYear().toString());
                }
            }
        });
    })
    .controller('OtherwiseDeceasedController', function ($filter, $translate, ActiveRoleService, GIS, FetchAndSaveBlob,
                                                         OtherwiseDeceasedService, OtherwiseDeceasedApi, OtherwiseDeceasedEditModal,
                                                         results) {
        var $ctrl = this;
        var RESULTS_PER_PAGE = 10;

        $ctrl.$onInit = function () {
            $ctrl.resultsForYear = results; // All brief results for the year
            $ctrl.filteredResults = [];     // All results with filters applied
            $ctrl.pagedResults = [];        // Paged filtered results

            $ctrl.speciesFilters = OtherwiseDeceasedService.getSpeciesCodes();
            $ctrl.filter = { species: '' }; // For default 'All' species selection at init
            applyFilter(); // Don't show rejected items at init.

            $ctrl.pageInfo = {
                hasNext: true,
                pageable: { page: 0 }
            };
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

        $ctrl.toggleSort = function (sortBy, defaultOrder, translator, i18nPrefix) {
            $ctrl.sortByTranslator = _.isNil(translator) ? null : $filter(translator);
            $ctrl.sortByi18nPrefix = i18nPrefix;

            if ($ctrl.sortBy !== sortBy) {
                $ctrl.sortBy = sortBy;
                $ctrl.sortByOrder = defaultOrder;
            } else {
                $ctrl.sortByOrder = $ctrl.sortByOrder === 'asc' ? 'desc' : 'asc';
            }
            applyFilter();
            selectFistPage();
        };

        $ctrl.selectPage = function (page) {
            $ctrl.pageInfo.pageable.page = page;
            $ctrl.pageInfo.hasNext = $ctrl.filteredResults.length > (page + 1) * RESULTS_PER_PAGE;
            $ctrl.pagedResults = _.slice($ctrl.filteredResults, page * RESULTS_PER_PAGE, (page + 1) * RESULTS_PER_PAGE);
        };

        $ctrl.hasResults = function () {
            return !_.isEmpty($ctrl.filteredResults);
        };

        $ctrl.getDetails = function (item) {
            return OtherwiseDeceasedApi.getDetails({itemId: item.id}).$promise;
        };

        $ctrl.add = function () {
            OtherwiseDeceasedEditModal.open({ /* empty */ }).then(reloadResults);
        };

        $ctrl.exportToExcel = function () {
            var filters = {
                gameSpeciesCode: !!Number($ctrl.filter.species) ? Number($ctrl.filter.species) : null,
                beginDate: $ctrl.filter.beginDate,
                endDate: $ctrl.filter.endDate,
                rkaOfficialCode: $ctrl.filter.rka,
                rhyOfficialCode: $ctrl.filter.rhy,
                cause: !!$ctrl.filter.cause ? $ctrl.filter.cause : null,
                showRejected: $ctrl.filter.showRejected
            };
            FetchAndSaveBlob.post('api/v1/deceased/excel', filters);
        };

        $ctrl.updateResults = function () {
            reloadResults();
        };

        $ctrl.onYearChange = function () {
            reloadResults();
            $ctrl.filteredResults = [];
            $ctrl.pageInfo.pageable.page = 0;
        };

        $ctrl.onFilterChange = function () {
            applyFilter();
            selectFistPage();
        };

        function applyFilter() {
            var filter = {};

            if (!!$ctrl.filter.species) {
                filter.gameSpeciesCode = $ctrl.filter.species;
            }

            if (!!$ctrl.filter.cause) {
                filter.cause = $ctrl.filter.cause;
            }

            if (!!$ctrl.filter.rka) {
                filter.rka = {};
                filter.rka.officialCode = $ctrl.filter.rka;
            }

            if (!!$ctrl.filter.rhy) {
                filter.rhy = {};
                filter.rhy.officialCode = $ctrl.filter.rhy;
            }

            filter.rejected = !!$ctrl.filter.showRejected;

            $ctrl.filteredResults = _.orderBy(_.filter(_.filter($ctrl.resultsForYear, filter), dateFilter), sortTranslatedValues, [$ctrl.sortByOrder]);
        }

        function sortTranslatedValues(item) {
            var sortValue = _.get(item, $ctrl.sortBy);
            sortValue = _.isNil($ctrl.sortByi18nPrefix) ? sortValue : $ctrl.sortByi18nPrefix + sortValue;
            sortValue = _.isNil($ctrl.sortByTranslator) ? sortValue : $ctrl.sortByTranslator(sortValue);
            return [sortValue];
        }

        function dateFilter(item) {
            var beginTime = moment($ctrl.filter.beginDate).toDate();
            var endTime = moment($ctrl.filter.endDate).toDate()
                .setHours(23, 59, 59, 999);
            var pointOfTime = moment(item.pointOfTime).toDate();

            return !(pointOfTime < beginTime || pointOfTime > endTime);
        }

        function reloadResults() {
            OtherwiseDeceasedService.getResults($ctrl.filter.year).then(
                function(briefResults) {
                    $ctrl.resultsForYear = briefResults;
                    applyFilter();
                    selectCurrentPage();
                });
        }

        function selectCurrentPage() {
            $ctrl.selectPage($ctrl.pageInfo.pageable.page);
        }

        function selectFistPage() {
            $ctrl.selectPage(0);
        }

    });
