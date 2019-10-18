'use strict';

angular.module('app.reporting.habides', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting.habides', {
                url: '/habides',
                templateUrl: 'reporting/habides/layout.html',
                controllerAs: '$ctrl',
                controller: function (Species, TranslatedSpecies, FetchAndSaveBlob) {

                    var $ctrl = this;

                    $ctrl.years = _.range(new Date().getFullYear(), 2018); // List years from current year to 2019
                    $ctrl.species = _
                        .chain(Species.getBirdPermitSpecies())
                        .map(TranslatedSpecies.translateSpecies)
                        .sortBy('name')
                        .value();

                    $ctrl.selected = {
                        species: $ctrl.species[0],
                        year: $ctrl.years[0],
                    };

                    $ctrl.exportBirdsToXml = function () {
                        var params = {
                            filename: "habides_" + $ctrl.selected.year + "_" + $ctrl.selected.species.name + ".xml",
                            year: $ctrl.selected.year,
                            speciesCode: $ctrl.selected.species.code
                        };
                        FetchAndSaveBlob.post('api/v1/habides/birds/export', params);
                    };
                }
            });
    });
