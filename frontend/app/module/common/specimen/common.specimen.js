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
        controller: function () {
            var $ctrl = this;

            $ctrl.isAloneVisible = function () {
                var e = $ctrl.entry;
                return e.isHarvest() && e.isAlonePossible() && _.isBoolean(e.specimens[0].alone);
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

                $ctrl.showMooselikeObservationAmounts = $ctrl.entry.isMooselike() && $ctrl.entry.withinMooseHunting;

                ObservationFieldsMetadata
                    .forSpecies({gameSpeciesCode: e.gameSpeciesCode}).$promise
                    .then(function (metadata) {
                        var fieldRequirements = metadata.getFieldRequirements(e.withinMooseHunting, e.observationType);

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
