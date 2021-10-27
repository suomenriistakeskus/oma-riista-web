'use strict';

angular.module('app.reporting.jhtarchive', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting.jhtarchive', {
                url: '/jhtarchive',
                templateUrl: 'reporting/jhtarchive/layout.html',
                controllerAs: '$ctrl',
                resolve: {
                    years: _.constant(_.range(2016, new Date().getFullYear() + 1)), // end value is not included
                    permitTypes: function (HarvestPermits) {
                        return HarvestPermits.omaRiistaPermitTypes().$promise;
                    }
                },
                controller: function ($translate, FormPostService, FetchAndSaveBlob, SpeciesNameService, years, permitTypes) {

                    var $ctrl = this;

                    $ctrl.years = years;

                    $ctrl.$onInit = function () {
                        $ctrl.permitTypes = initPermitTypes(permitTypes);
                        $ctrl.selected = {
                            permitType: $ctrl.permitTypes[0],
                            species: null,
                            calendarYear: _.last($ctrl.years)
                        };
                    };

                    $ctrl.onPermitTypeChange = function () {
                        $ctrl.selected.species = null;
                    };

                    $ctrl.exportToExcel = function () {
                        var params = {
                            permitTypeCode: _.get($ctrl.selected, 'permitType.permitTypeCode', null),
                            speciesCode: _.get($ctrl.selected, 'species.speciesCode', null),
                            calendarYear: $ctrl.selected.calendarYear
                        };
                        FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/export-jhtarchive', params);
                    };

                    function initPermitTypes(permitTypes) {
                        // If "Permit type: All" is selected, then all available species should be selectable.
                        // TODO: Should there be an option for permit types that does NOT have species?
                        var allPermitTypes = {
                            permitTypeCode: null,
                            permitTypeName: $translate.instant('reporting.jhtArchive.option.all'),
                            species: _.chain(permitTypes) // All species from all permit types
                                .flatMap(function (i) {
                                    return i.speciesCodes;
                                })
                                .uniq()
                                .map(speciesCodeWithName)
                                .sortBy('speciesName')
                                .value()
                        };

                        return [allPermitTypes].concat(_
                            .chain(permitTypes)
                            .map(function (pt) {
                                return {
                                    permitTypeCode: pt.permitTypeCode,
                                    permitTypeName: $translate.instant('harvestpermit.wizard.summary.permitType.' + pt.permitTypeCode),
                                    species: _.chain(pt.speciesCodes)
                                        .map(speciesCodeWithName)
                                        .sortBy('speciesName')
                                        .value()
                                };

                            })
                            .sortBy('permitTypeName')
                            .value());
                    }

                    function speciesCodeWithName(speciesCode) {
                        return {
                            speciesCode: speciesCode,
                            speciesName: SpeciesNameService.translateSpeciesCode(speciesCode)
                        };
                    }
                }
            });
    });
