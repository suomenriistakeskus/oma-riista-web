'use strict';

angular.module('app.jht.harvestregistry', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.harvestregistry', {
            url: '/harvestregistry',
            templateUrl: 'jht/harvestregistry/harvestregistry.search.html',
            controller: 'HarvestRegistrySearchController',
            controllerAs: '$ctrl',
            resolve: {
                dateRange: function (HuntingYearService) {
                    var huntingYear = HuntingYearService.getCurrent();
                    var begin = HuntingYearService.getBeginDateStr(huntingYear);
                    var end = HuntingYearService.getEndDateStr(huntingYear);
                    return [begin, end];
                },
                areas: function (OrganisationsByArea) {
                    return OrganisationsByArea.queryAll().$promise;
                },
                municipalities: function (Municipalities) {
                    return Municipalities.query().$promise;
                }
            }
        });
    })
    .controller('HarvestRegistrySearchController', function (Species, TranslatedSpecies, PersonSearchService,
                                                             HarvestRegistry, FetchAndSaveBlob,
                                                             dateRange, areas, municipalities) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.speciesOptions = buildSpecies();
                $ctrl.filters = {
                    selectedSpeciesCode: null,
                    beginDate: dateRange[0],
                    endDate: dateRange[1],
                    shooterHunterNumber: null,
                    municipalityCode: null
                };
                $ctrl.areas = areas;
                $ctrl.municipalities = municipalities;
                $ctrl.selectedMunicipality = null;
                $ctrl.selectedRka = null;
                $ctrl.selectedRhy = null;
                $ctrl.uiSelectModelShooter = null;
                $ctrl.uiSelectModelMunicipality = null;
                $ctrl.personSearchResult = [];
                $ctrl.searching = false;
                $ctrl.filtersValid = true;
                $ctrl.slice = null;
            };

        $ctrl.doSearch = function (pageNumber) {
            var selectedSpeciesCode = $ctrl.filters.selectedSpeciesCode;

                HarvestRegistry.query({
                    page: pageNumber,
                    pageSize: 30,
                    beginDate: $ctrl.filters.beginDate,
                    endDate: $ctrl.filters.endDate,
                    allSpecies: !selectedSpeciesCode,
                    species: selectedSpeciesCode ? [selectedSpeciesCode] : [],
                    shooterHunterNumber: $ctrl.filters.shooterHunterNumber,
                    municipalityCode: $ctrl.selectedMunicipality ? $ctrl.selectedMunicipality.officialCode : null,
                    rkaCode: $ctrl.selectedRka ? $ctrl.selectedRka.officialCode : null,
                    rhyCode: $ctrl.selectedRhy ? $ctrl.selectedRhy.officialCode : null
                }).$promise.then(function (data) {
                    $ctrl.slice = data;
                });
            };

            $ctrl.doSearchPerson = function (searchParam) {
                $ctrl.searching = true;
                return PersonSearchService.findByHunterNumberOrPersonName(searchParam)
                    .then(function (result) {
                        $ctrl.personSearchResult = result.data;
                        return result.data;
                    })
                    .finally(function () {
                        $ctrl.searching = false;
                    });
            };

            $ctrl.clearShooter = function () {
                $ctrl.uiSelectModelShooter = null;
                $ctrl.filters.shooterHunterNumber = null;
            };

        $ctrl.clearMunicipality = function () {
            $ctrl.uiSelectModelMunicipality = null;
            $ctrl.selectedMunicipality = null;
        };

        $ctrl.onSelectShooter = function (person) {
                $ctrl.filters.shooterHunterNumber = person.hunterNumber;
            };

        $ctrl.onSelectMunicipality = function (municipality) {
            $ctrl.selectedMunicipality = municipality;
        };

        $ctrl.searchMunicipalities = function (term) {
            return _.filter($ctrl.municipalities, function (m) {
                return m.officialCode === term ||
                    _.startsWith(_.toLower(m.name.finnish), _.toLower(term)) ||
                    _.startsWith(_.toLower(m.name.swedish), _.toLower(term));
            });
        };

        $ctrl.filtersChanged = function (form) {
            $ctrl.filtersValid = !form.$invalid;
        };

        $ctrl.exportExcel = function () {
            if ($ctrl.filtersValid) {
                FetchAndSaveBlob.post('/api/v1/harvestregistry/excel', {
                        page: 0,
                        pageSize: 1,
                        beginDate: $ctrl.filters.beginDate,
                        endDate: $ctrl.filters.endDate,
                        allSpecies: !$ctrl.filters.selectedSpeciesCode,
                        species: $ctrl.filters.selectedSpeciesCode ? [$ctrl.filters.selectedSpeciesCode] : [],
                        shooterHunterNumber: $ctrl.filters.shooterHunterNumber,
                        rkaCode: $ctrl.selectedRka ? $ctrl.selectedRka.officialCode : null,
                        rhyCode: $ctrl.selectedRhy ? $ctrl.selectedRhy.officialCode : null
                });
            }
        };

            function buildSpecies() {
                return _.chain(Species.getSpeciesMapping())
                    .map(function (species) {
                        return TranslatedSpecies.translateSpecies(species);
                    })
                    .sortBy('name')
                    .value();
            }
        }
    )
    .component('jhtHarvestRegistryTable', {
        templateUrl: 'jht/harvestregistry/harvestregistry-table.html',
        bindings: {
            items: '<',
            areas: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.resolveRkaName = function (item) {
                var rka = resolveRka(item);
                return rka ? rka.name : null;
            };

            $ctrl.resolveRhyName = function (item) {
                var rka = resolveRka(item);

                return rka ? resolveRhy(rka, item).name : null;
            };

            function resolveRka(item) {
                return findByOfficialCode($ctrl.areas, item.rkaCode);
            }

            function resolveRhy(rka, item) {
                return findByOfficialCode(rka.subOrganisations, item.rhyCode);
            }

            function findByOfficialCode(coll, code) {
                return _.find(coll, function (organisation) {
                    return organisation.officialCode === code;
                });
            }
        }
    });
