'use strict';

angular.module('app.diary.specimen', [])
    .service('DiaryEntrySpecimenFormService', function (offCanvasStack) {
        var self = this;

        this.MAX_SPECIMEN_AMOUNT = 999;
        this.MAX_VISIBLE_AMOUNT = 25;

        this.getMaxSpecimenCountForHarvest = function (gameDiaryParameters, gameSpeciesCode) {
            return gameDiaryParameters.isMultipleSpecimensAllowedForHarvestSpecies(gameSpeciesCode) ? self.MAX_SPECIMEN_AMOUNT : 1;
        };

        this.getMaxSpecimenCountForObservation = function () {
            return self.MAX_SPECIMEN_AMOUNT;
        };

        this.initAmountAndSpecimens = function (entry) {
            // initialize invalid amount to 1.
            if (!_.isFinite(entry.totalSpecimenAmount)) {
                entry.totalSpecimenAmount = 1;
            }

            if (!entry.specimens) {
                entry.specimens = [];
            }

            self.setSpecimenCount(entry, entry.totalSpecimenAmount);
        };

        this.setSpecimenCount = function (entry, count) {
            if (count > 0) {
                var limit = Math.min(entry.totalSpecimenAmount, self.MAX_VISIBLE_AMOUNT);

                if (!_.isArray(entry.specimens)) {
                    entry.specimens = [];
                }
                var arrSize = entry.specimens.length;

                if (count > arrSize) {
                    for (var i = arrSize; i < limit; i++) {
                        self.addNewSpecimen(entry, 0);
                    }
                } else {
                    entry.specimens.length = count;
                }
            }
        };

        this.addNewSpecimen = function (entry, index) {
            var specimen = {};

            if (index) {
                entry.specimens.splice(index, 0, specimen);
            } else {
                entry.specimens.push(specimen);
            }
        };

        this.removeSpecimen = function (entry, index) {
            if (entry.totalSpecimenAmount > 1) {
                if (index >= 0) {
                    entry.specimens.splice(index, 1);
                }
                entry.totalSpecimenAmount--;
            }
        };

        this.editSpecimen = function (entry, parameters, availableFields, observationFieldRequirements) {
            var templateUrl;

            if (entry.isHarvest()) {
                templateUrl = 'diary/specimen/harvest-specimen.html';
            } else if (entry.isObservation()) {
                templateUrl = 'diary/specimen/observation-specimen.html';
            } else if (entry.isSrva()) {
                templateUrl = 'diary/specimen/srva-specimen.html';
            }

            return offCanvasStack.open({
                templateUrl: templateUrl,
                largeDialog: true,
                resolve: {
                    entry: _.constant(entry),
                    parameters: _.constant(parameters),
                    availableFields: _.constant(availableFields || {}),
                    observationFieldRequirements: _.constant(observationFieldRequirements || null)
                },
                controller: 'DiaryEntrySpecimenModalController'
            }).result;
        };
    })

    .controller('DiaryEntrySpecimenModalController', function ($scope, DiaryEntrySpecimenFormService, availableFields,
                                                               entry, parameters, observationFieldRequirements) {

        $scope.entry = entry;
        $scope.maxVisibleSpecimens = DiaryEntrySpecimenFormService.MAX_VISIBLE_AMOUNT;

        function isMultipleSpecimensAllowedForDiaryEntry() {
            return !$scope.entry.isHarvest() || parameters.isMultipleSpecimensAllowedForHarvestSpecies($scope.entry.gameSpeciesCode);
        }

        $scope.getAvailableGameGenders = function () {
            return parameters.genders;
        };

        $scope.getAvailableGameAges = function () {
            // Use observationFieldRequirements to resolve age values, if available;
            // otherwise default to using general game diary 'parameters' object.
            return observationFieldRequirements ? observationFieldRequirements.getAvailableGameAges() : parameters.ages;
        };

        $scope.getAvailableGameStates = function () {
            return observationFieldRequirements ? observationFieldRequirements.getAvailableGameStates() : [];
        };

        $scope.getAvailableGameMarkings = function () {
            return observationFieldRequirements ? observationFieldRequirements.getAvailableGameMarkings() : [];
        };

        $scope.getWidthOfPawOptions = function () {
            return observationFieldRequirements ? observationFieldRequirements.getWidthOfPawOptions() : [];
        };

        $scope.getLengthOfPawOptions = function () {
            return observationFieldRequirements ? observationFieldRequirements.getLengthOfPawOptions() : [];
        };

        var isFieldRequired = function (fieldName) {
            return availableFields[fieldName] === true;
        };

        var isFieldVisible = function (fieldName) {
            return availableFields[fieldName] === false;
        };

        $scope.isAddSpecimenButtonHidden = function () {
            return !entry.canEdit || entry.specimens.length > 0 && !isMultipleSpecimensAllowedForDiaryEntry();
        };

        $scope.addSpecimen = function (index) {
            if (entry.totalSpecimenAmount < $scope.maxVisibleSpecimens && isMultipleSpecimensAllowedForDiaryEntry()) {
                DiaryEntrySpecimenFormService.addNewSpecimen(entry, index);
                entry.totalSpecimenAmount++;
            }
        };

        $scope.genderVisible = isFieldVisible('gender');
        $scope.ageVisible = isFieldVisible('age');
        $scope.stateVisible = isFieldVisible('state');
        $scope.markingVisible = isFieldVisible('marking');
        $scope.widthOfPawVisible = isFieldVisible('widthOfPaw');
        $scope.lengthOfPawVisible = isFieldVisible('lengthOfPaw');

        $scope.genderRequired = isFieldRequired('gender');
        $scope.ageRequired = isFieldRequired('gender');
        $scope.stateRequired = isFieldRequired('state');
        $scope.markingRequired = isFieldRequired('marking');
        $scope.widthOfPawRequired = isFieldRequired('widthOfPaw');
        $scope.lengthOfPawRequired = isFieldRequired('lengthOfPaw');
    });
