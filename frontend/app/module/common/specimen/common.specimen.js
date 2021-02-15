'use strict';

angular.module('app.common.specimen', [])
    .component('rShowEntrySpecimens', {
        templateUrl: 'common/specimen/show-entry-specimens.html',
        bindings: {
            entry: '<'
        }
    })
    .component('rShowHarvestSpecimens', {
        templateUrl: 'common/specimen/show-harvest-specimens.html',
        bindings: {
            entry: '<'
        },
        controller: function (AuthenticationService, GameSpeciesCodes) {
            var $ctrl = this;

            $ctrl.isNotNil = function (value) {
                return !_.isNil(value);
            };

            $ctrl.$onInit = function () {
                var speciesCode = $ctrl.entry.gameSpeciesCode;

                // TODO Remove this when deer pilot 2020 is over.
                var isDeerPilotUser = AuthenticationService.isDeerPilotUser();

                $ctrl.speciesHasExtendedFields = !!speciesCode
                    && GameSpeciesCodes.isSpeciesHavingExtendedFields(speciesCode, isDeerPilotUser);
            };
        }
    })
    .component('rShowObservationSpecimens', {
        templateUrl: 'common/specimen/show-observation-specimens.html',
        bindings: {
            entry: '<'
        },
        controller: function (ObservationFieldsMetadata) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var e = $ctrl.entry;

                $ctrl.showMooselikeObservationAmounts = $ctrl.entry.isMooselike()
                    && $ctrl.entry.isObservationWithinHunting();

                $ctrl.showCalfAmount = $ctrl.entry.isMoose()
                    || !_.isNil($ctrl.entry.mooselikeCalfAmount);

                ObservationFieldsMetadata
                    .forSpecies({gameSpeciesCode: e.gameSpeciesCode}).$promise
                    .then(function (metadata) {
                        var fieldRequirements = metadata.getFieldRequirements(e.observationCategory, e.observationType);

                        $ctrl.isGenderVisible = fieldRequirements.isFieldLegal('gender');
                        $ctrl.isAgeVisible = fieldRequirements.isFieldLegal('age');
                        $ctrl.isWidthOfPawVisible = fieldRequirements.isFieldLegal('widthOfPaw');
                        $ctrl.isLengthOfPawVisible = fieldRequirements.isFieldLegal('lengthOfPaw');
                        $ctrl.isStateVisible = fieldRequirements.isFieldLegal('state');
                        $ctrl.isMarkingVisible = fieldRequirements.isFieldLegal('marking');
                    });
            };
        }
    })
    .component('rShowSrvaSpecimens', {
        templateUrl: 'common/specimen/show-srva-specimens.html',
        bindings: {
            entry: '<'
        }
    });
