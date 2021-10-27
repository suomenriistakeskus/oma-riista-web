'use strict';

angular.module('app.rhy.harvestregistry', [])
    .config(function ($stateProvider) {
        $stateProvider.state('rhy.harvestregistry', {
            url: '/harvestregistry',
            templateUrl: 'rhy/harvestregistry/rhy-harvestregistry-search.html',
            controller: 'RhyHarvestRegistrySearchController',
            controllerAs: '$ctrl',
            resolve: {
                dateRange: function (HuntingYearService) {
                    var huntingYear = HuntingYearService.getCurrent();
                    var begin = HuntingYearService.getBeginDateStr(huntingYear);
                    var end = HuntingYearService.getEndDateStr(huntingYear);
                    return [begin, end];
                }
            }
        });
    })

    .controller('RhyHarvestRegistrySearchController', function (HarvestRegistrySpecies, PersonSearchService,
                                                                HarvestRegistry, FetchAndSaveBlob,
                                                                dateRange, orgId) {
            var $ctrl = this;
            var FIELDS = {
                COMMON: 'COMMON',
                COMMON_WITH_SHOOTER: 'COMMON_WITH_SHOOTER'
            };

            $ctrl.$onInit = function () {
                $ctrl.searchReason = 'POPULATION';
                $ctrl.speciesOptions = HarvestRegistrySpecies.buildSpecies();
                $ctrl.filters = {
                    selectedSpeciesCode: null,
                    beginDate: dateRange[0],
                    endDate: dateRange[1],
                    shooterHunterNumber: null
                };
                $ctrl.searching = false;
                $ctrl.filtersValid = true;
                $ctrl.slice = null;
                $ctrl.includedFields = FIELDS.COMMON;
            };

            function buildSearchParameters(pageNumber) {
                var searchReason = $ctrl.searchReason;
                var selectedSpeciesCode = $ctrl.filters.selectedSpeciesCode;

                var hunterNumber = searchReason === 'HUNTING_CONTROL'
                    ? $ctrl.filters.shooterHunterNumber || null
                    : null;

                return {
                    page: pageNumber,
                    pageSize: 30,
                    searchReason: searchReason,
                    beginDate: $ctrl.filters.beginDate,
                    endDate: $ctrl.filters.endDate,
                    species: selectedSpeciesCode,
                    shooterHunterNumber: hunterNumber,
                    rhyId: orgId
                };
            }

            $ctrl.doSearch = function (pageNumber) {
                var searchReason = $ctrl.searchReason;
                HarvestRegistry.queryRhy(buildSearchParameters(pageNumber)).$promise
                    .then(function (data) {
                        $ctrl.includedFields = searchReason === 'HUNTING_CONTROL'
                            ? FIELDS.COMMON_WITH_SHOOTER
                            : FIELDS.COMMON;
                        $ctrl.slice = data;
                    });
            };

            $ctrl.filtersChanged = function (form) {
                $ctrl.filtersValid = !form.$invalid;
            };

            $ctrl.exportExcel = function () {
                if ($ctrl.filtersValid) {
                    // Use page number 0 for excel, omitted in backend
                    FetchAndSaveBlob.post('/api/v1/harvestregistry/rhy/excel', buildSearchParameters(0));
                }
            };

        }
    )
    .controller('RhyHarvestRegistryLocationModalController', function($uibModalInstance,
                                                                      MapState, MapDefaults, MapUtil, location) {
        var $ctrl = this;

        $ctrl.$onInit = function() {
            $ctrl.location = location;
            $ctrl.mapState = MapState.get();
            $ctrl.mapDefaults = MapDefaults.create();
            $ctrl.mapEvents = [];

            MapState.updateMapCenter(location, 6);
        };

        $ctrl.close = function () {
            $uibModalInstance.dismiss('close');
        };
    });
