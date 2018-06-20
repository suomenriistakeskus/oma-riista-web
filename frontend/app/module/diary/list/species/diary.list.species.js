'use strict';

angular.module('app.diary.list.species', [])
    .component('diaryListSpeciesSelection', {
        templateUrl: 'diary/list/species/species-selection.html',
        bindings: {
            state: '<',
            parameters: '<',
            onChange: '&'
        },
        controller: function (DiaryListSpeciesService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.getGameName = $ctrl.parameters.$getGameName;
                $ctrl.getCategoryName = $ctrl.parameters.$getCategoryName;
            };

            $ctrl.deselectAllSpecies = function () {
                DiaryListSpeciesService.deselectAllSpecies();
                $ctrl.onChange();
            };

            $ctrl.selectAllSpecies = function () {
                DiaryListSpeciesService.selectAllSpecies();
                $ctrl.onChange();
            };

            $ctrl.speciesAddedToSelection = function () {
                if ($ctrl.lastSelectedSpeciesCode || $ctrl.lastSelectedSpeciesCode === 0) {
                    var speciesCode = $ctrl.lastSelectedSpeciesCode;
                    delete $ctrl.lastSelectedSpeciesCode;
                    DiaryListSpeciesService.speciesAddedToSelection($ctrl.parameters, speciesCode);
                    $ctrl.onChange();
                }
            };

            $ctrl.removeSpeciesFromSelection = function (gameSpeciesCode) {
                DiaryListSpeciesService.removeSpeciesFromSelection($ctrl.parameters, gameSpeciesCode);
                $ctrl.onChange();
            };
        }
    })

    .service('DiaryListSpeciesService', function (DiaryListViewState) {
        var self = this;

        var isSpeciesSelected = function (gameSpeciesCode) {
            return _.some(DiaryListViewState.selectedSpecies, function (selectedSpecies) {
                return selectedSpecies.code === gameSpeciesCode;
            });
        };

        var moveSpeciesBetweenArrays = function (gameSpeciesCode, fromArray, toArray) {
            var arrayOfRemoved = _.remove(fromArray, function (species) {
                return species.code === gameSpeciesCode;
            });

            if (arrayOfRemoved.length === 1) {
                toArray.push(arrayOfRemoved[0]);
            }
        };

        var sortSpeciesArray = function (parameters, speciesArray) {
            return _.sortBy(speciesArray, function (species) {
                return parameters.$getGameName(species.code);
            });
        };

        var constructSpeciesArrayFromDiaryEntries = function (parameters, diaryEntries) {
            var allSpecies = parameters.species;

            if (!allSpecies || allSpecies.length < 1) {
                return [];
            }

            var speciesCodeToEntryCount = _.countBy(_.map(diaryEntries, 'gameSpeciesCode'));

            return sortSpeciesArray(parameters, _.filter(allSpecies, function (species) {
                var count = speciesCodeToEntryCount[species.code];

                if (count) {
                    // Add 'count' field as a side-effect.
                    species.count = count;
                }

                return count;
            }));
        };

        this.updateAllSpecies = function (parameters, diaryEntries, selectAll) {
            DiaryListViewState.allSpecies = constructSpeciesArrayFromDiaryEntries(parameters, diaryEntries);

            if (selectAll) {
                self.selectAllSpecies();
            } else {
                var speciesPartition = _.partition(DiaryListViewState.allSpecies, function (diaryEntrySpecies) {
                    return isSpeciesSelected(diaryEntrySpecies.code);
                });

                DiaryListViewState.selectedSpecies = speciesPartition[0];
                DiaryListViewState.unselectedSpecies = speciesPartition[1];
            }
        };

        this.deselectAllSpecies = function () {
            DiaryListViewState.unselectedSpecies = angular.copy(DiaryListViewState.allSpecies);
            DiaryListViewState.selectedSpecies = [];
        };

        this.selectAllSpecies = function () {
            DiaryListViewState.selectedSpecies = angular.copy(DiaryListViewState.allSpecies);
            DiaryListViewState.unselectedSpecies = [];
        };

        this.speciesAddedToSelection = function (parameters, speciesCode) {
            moveSpeciesBetweenArrays(speciesCode, DiaryListViewState.unselectedSpecies, DiaryListViewState.selectedSpecies);
            DiaryListViewState.selectedSpecies = sortSpeciesArray(parameters, DiaryListViewState.selectedSpecies);
        };

        this.removeSpeciesFromSelection = function (parameters, gameSpeciesCode) {
            moveSpeciesBetweenArrays(gameSpeciesCode, DiaryListViewState.selectedSpecies, DiaryListViewState.unselectedSpecies);
            DiaryListViewState.unselectedSpecies = sortSpeciesArray(parameters, DiaryListViewState.unselectedSpecies);
        };

        this.filterDiaryEntriesBySpeciesSelection = function (allEntries) {
            return _.filter(allEntries, function (entry) {
                return isSpeciesSelected(entry.gameSpeciesCode);
            });
        };
    });
