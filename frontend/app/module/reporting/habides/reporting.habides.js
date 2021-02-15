'use strict';

angular.module('app.reporting.habides', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting.habides', {
                url: '/habides',
                templateUrl: 'reporting/habides/layout.html',
                controllerAs: '$ctrl',
                controller: function (Species, TranslatedSpecies, FetchAndSaveBlob, HuntingYearService) {

                    var $ctrl = this;

                    $ctrl.years = _.range(new Date().getFullYear(), 2018); // List years from current year to 2019
                    $ctrl.birdSpecies = _
                        .chain(Species.getBirdPermitSpecies())
                        .map(TranslatedSpecies.translateSpecies)
                        .sortBy('name')
                        .value();

                    $ctrl.selectedBird = {
                        species: $ctrl.birdSpecies[0],
                        year: $ctrl.years[0],
                    };

                    $ctrl.birdError = {};

                    $ctrl.exportBirdsToXml = function () {
                        $ctrl.birdError = {};

                        var params = {
                            filename: "habides_" + $ctrl.selectedBird.year + "_" + $ctrl.selectedBird.species.name + ".xml",
                            year: $ctrl.selectedBird.year,
                            speciesCode: $ctrl.selectedBird.species.code
                        };

                        FetchAndSaveBlob.post('api/v1/habides/export', params)
                            .catch(function (res) {
                                $ctrl.birdError = res.data;
                            });
                    };

                    $ctrl.huntingYears = _.reverse(HuntingYearService.createHuntingYearChoices(2019));
                    $ctrl.mammalSpecies = _
                        .chain(Species.getMammalPermitSpecies())
                        .map(TranslatedSpecies.translateSpecies)
                        .sortBy('name')
                        .value();

                    $ctrl.selectedMammal = {
                        species: $ctrl.mammalSpecies[0],
                        huntingYear: $ctrl.huntingYears[0],
                    };

                    $ctrl.mammalError = {};

                    $ctrl.exportMammalsToXml = function () {
                        $ctrl.mammalError = {};

                        var params = {
                            filename: "habides_" + $ctrl.selectedMammal.huntingYear.name + "_" + $ctrl.selectedMammal.species.name + ".xml",
                            year: $ctrl.selectedMammal.huntingYear.year,
                            speciesCode: $ctrl.selectedMammal.species.code
                        };

                        FetchAndSaveBlob.post('api/v1/habides/export', params)
                            .catch(function (res) {
                                $ctrl.mammalError = res.data;
                            });
                    };
                }
            });
    });
