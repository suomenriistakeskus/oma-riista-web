'use strict';

angular.module('app.account.harvestregistry', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.harvestRegistry', {
                url: '/harvestregistry',
                templateUrl: 'account/harvestregistry/harvestregistry.html',
                controller: 'AccountHarvestRegistryController',
                controllerAs: '$ctrl',
                resolve: {
                    personId: function ($stateParams, ActiveRoleService) {
                        return ActiveRoleService.isModerator() ? $stateParams.id : 'me';
                    },
                    dateRange: function (HuntingYearService) {
                        var huntingYear = HuntingYearService.getCurrent();
                        var begin = HuntingYearService.getBeginDateStr(huntingYear);
                        var end = HuntingYearService.getEndDateStr(huntingYear);
                        return [begin, end];
                    }
                }
            });
    })
    .controller('AccountHarvestRegistryController', function ($state, HarvestRegistry, SpeciesNameService,
                                                              Species, TranslatedSpecies,
                                                              personId, dateRange) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.searchBegin = dateRange[0];
            $ctrl.searchEnd = dateRange[1];
            $ctrl.allSpecies = true;
            $ctrl.species = [];
            $ctrl.slice = null;
            $ctrl.unselectedSpecies = [];
            $ctrl.selectedSpecies = [];
            $ctrl.filtersValid = true;
            buildAvailableSpecies();
            $ctrl.searchPage(0);
        };

        $ctrl.translateSpeciesName = function (speciesCode) {
            return SpeciesNameService.translateSpeciesCode(speciesCode);
        };

        $ctrl.addSpecies = function (species) {
            if (species) {
                $ctrl.selectedSpecies.push(species);
                buildAvailableSpecies();
                $ctrl.searchPage(0);

            }
        };

        $ctrl.removeSpeciesFromSelection = function (code) {
            if (code) {
                $ctrl.selectedSpecies = _.filter($ctrl.selectedSpecies, function (speciesCode) {
                    return speciesCode !== code;
                });
                buildAvailableSpecies();
                $ctrl.searchPage(0);
            }
        };

        $ctrl.clearSpecies = function () {
            $ctrl.selectedSpecies = [];
            buildAvailableSpecies();
            $ctrl.searchPage(0);
        };

        $ctrl.filtersChanged = function (form) {
            $ctrl.filtersValid = !form.$invalid;
            $ctrl.searchPage(0);
        };

        $ctrl.searchPage = function (page) {
            if ($ctrl.filtersValid) {
                HarvestRegistry.list({personId: personId}, {
                    page: page,
                    pageSize: 10,
                    beginDate: $ctrl.searchBegin,
                    endDate: $ctrl.searchEnd,
                    allSpecies: $ctrl.allSpecies,
                    species: $ctrl.selectedSpecies
                }).$promise.then(function (data) {
                    $ctrl.slice = data;
                });
            }
        };

        function buildAvailableSpecies() {
            $ctrl.unselectedSpecies = _.chain(Species.getSpeciesMapping())
                .filter(function (species) {
                    return !_.includes($ctrl.selectedSpecies, species.code);
                })
                .map(function (species) {
                    return TranslatedSpecies.translateSpecies(species);
                })
                .sortBy('name')
                .value();
        }
    })
    .component('accountHarvestRegistryItem', {
        templateUrl: 'account/harvestregistry/harvestregistry-item.html',
        bindings: {
            item: '<'
        }
    });
