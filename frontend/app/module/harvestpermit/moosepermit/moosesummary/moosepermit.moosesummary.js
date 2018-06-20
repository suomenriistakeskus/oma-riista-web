'use strict';

angular.module('app.moosepermit.moosehuntingsummary', [])

    .constant('MooseHuntingSummaryAreaType', {
        summerPasture: 'SUMMER_PASTURE',
        winterPasture: 'WINTER_PASTURE',
        both: 'BOTH'
    })

    .constant('MooseHuntingSummaryPopulationGrowthTrend', {
        decreased: 'DECREASED',
        unchanged: 'UNCHANGED',
        increased: 'INCREASED'
    })

    .factory('MooseHuntingSummary', function ($resource) {
        return $resource('api/v1/club/:clubId/moosesummary/:id', {'clubId': '@clubId', 'id': '@id'}, {
            'findByClubIdAndPermitId': {
                method: 'GET',
                url: 'api/v1/club/:clubId/moosesummary/permit/:permitId',
                params: {
                    clubId: '@clubId',
                    permitId: '@permitId'
                }
            },
            'update': {method: 'PUT'},
            'markUnfinished': {
                method: 'POST',
                url: 'api/v1/club/:clubId/moosesummary/:id/markunfinished',
                params: {clubId: '@clubId', id: '@id'}
            }
        });
    })

    .service('MooseHuntingSummaryService', function ($translate, FormSidebarService, GameDiaryParameters,
                                                     GameSpeciesCodes, MooseHuntingSummary,
                                                     MooseHuntingSummaryAreaType,
                                                     MooseHuntingSummaryPopulationGrowthTrend) {

        var modalOptions = {
            controller: 'MooseHuntingSummaryFormController',
            templateUrl: 'harvestpermit/moosepermit/moosesummary/moose-hunting-summary.html',
            largeDialog: true,
            resolve: {
                areaTypes: function () {
                    return _.values(MooseHuntingSummaryAreaType);
                },
                appearedSpecies: function () {
                    var constructSpecies = function (parameters, gameSpeciesCode, localisationKey) {
                        var retrievedSpecies = _.find(parameters.species, function (species) {
                            return species.code === gameSpeciesCode;
                        });

                        return {
                            key: localisationKey,
                            gameSpeciesCode: gameSpeciesCode,
                            name: retrievedSpecies.name || {}
                        };
                    };

                    return GameDiaryParameters.query().$promise.then(function (parameters) {
                        var mooselike = [
                            constructSpecies(parameters, GameSpeciesCodes.FALLOW_DEER, 'fallowDeer'),
                            constructSpecies(parameters, GameSpeciesCodes.ROE_DEER, 'roeDeer'),
                            constructSpecies(parameters, GameSpeciesCodes.WHITE_TAILED_DEER, 'whiteTailedDeer'),
                            constructSpecies(parameters, GameSpeciesCodes.WILD_FOREST_REINDEER, 'wildForestReindeer')
                        ];

                        return {
                            mooselike: mooselike,
                            wildBoar: constructSpecies(parameters, GameSpeciesCodes.WILD_BOAR, 'wildBoar'),
                            beaver: {
                                key: 'beaver',
                                gameSpeciesCode: null,
                                name: {
                                    fi: $translate.instant('club.hunting.mooseHuntingSummary.beaver')
                                }
                            }
                        };
                    });
                },
                populationGrowthTrends: function () {
                    return _.values(MooseHuntingSummaryPopulationGrowthTrend);
                }
            }
        };

        function parametersToResolve(parameters) {
            return {
                mooseHuntingSummary: _.constant(parameters.mooseHuntingSummary),
                speciesAmount: _.constant(parameters.speciesAmount)
            };
        }

        var formSidebar = FormSidebarService.create(modalOptions, MooseHuntingSummary, parametersToResolve);

        this.editHuntingSummary = function (clubId, permitId, speciesAmount) {
            var params = {
                clubId: clubId,
                permitId: permitId
            };

            return MooseHuntingSummary.findByClubIdAndPermitId(params).$promise
                .then(function (summary) {
                    return formSidebar.show({id: summary.id, mooseHuntingSummary: summary, speciesAmount: speciesAmount});
                });
        };
    })

    .controller('MooseHuntingSummaryFormController', function ($scope, Helpers, MooseHuntingSummary, appearedSpecies,
                                                               areaTypes, mooseHuntingSummary, populationGrowthTrends,
                                                               speciesAmount) {

        var year = Helpers.toMoment(speciesAmount.beginDate, 'YYYY-MM-DD').year();
        $scope.show2017Fields = year && year >= 2017;

        $scope.huntingSummary = mooseHuntingSummary;
        $scope.areaTypes = areaTypes;
        $scope.populationGrowthTrends = populationGrowthTrends;
        $scope.markedUnfinished = false;
        $scope.speciesAmount = speciesAmount;
        $scope.mooselikeSpecies = appearedSpecies.mooselike;
        $scope.wildBoar = appearedSpecies.wildBoar;
        $scope.beaver = appearedSpecies.beaver;

        var nonMooselike = [appearedSpecies.wildBoar];
        if ($scope.show2017Fields) {
            nonMooselike.push(appearedSpecies.beaver);
        }
        var otherSpecies = _.union(appearedSpecies.mooselike, nonMooselike);

        var deerFlyDateFields = ['dateOfFirstDeerFlySeen', 'dateOfLastDeerFlySeen'];
        var otherDeerFlyFields = [
            'numberOfAdultMoosesHavingFlies', 'numberOfYoungMoosesHavingFlies', 'trendOfDeerFlyPopulationGrowth'
        ];

        var viewStateDateFields = deerFlyDateFields.concat([
            'mooseHeatBeginDate', 'mooseHeatEndDate', 'mooseFawnBeginDate', 'mooseFawnEndDate', 'huntingEndDate'
        ]);

        var deadMooseCountFields = ['numberOfDrownedMooses', 'numberOfMoosesKilledByBear',
            'numberOfMoosesKilledByWolf', 'numberOfMoosesKilledInTrafficAccident',
            'numberOfMoosesKilledByPoaching', 'numberOfMoosesKilledInRutFight',
            'numberOfStarvedMooses', 'numberOfMoosesDeceasedByOtherReason'
        ];

        var getAppearanceKey = function (species) {
            return species.key + 'Appearance';
        };

        function createAppearances() {

            _.forEach(otherSpecies, function (species) {
                var key = getAppearanceKey(species);

                if (!$scope.huntingSummary[key]) {
                    $scope.huntingSummary[key] = {};
                }
            });

            $scope.mooselikeSpeciesAppearances = _.map($scope.mooselikeSpecies, function (species) {
                return $scope.huntingSummary[getAppearanceKey(species)];
            });

            $scope.wildBoarAppearance = $scope.huntingSummary[getAppearanceKey($scope.wildBoar)];
            $scope.beaverAppearance = $scope.huntingSummary[getAppearanceKey($scope.beaver)];
        }
        createAppearances();

        $scope.viewState = {};
        _.forEach(viewStateDateFields, function (fieldName) {
            $scope.viewState[fieldName] = $scope.huntingSummary[fieldName];
        });

        $scope.showPermitAreaSize = function (form) {
            var summary = $scope.huntingSummary;
            var totalHuntingArea = parseInt(form.totalHuntingArea.$viewValue, 10);
            var effectiveHuntingArea = parseInt(form.effectiveHuntingArea.$viewValue, 10);

            return !_.isFinite(totalHuntingArea) && !_.isFinite(effectiveHuntingArea) ||
                totalHuntingArea > summary.permitAreaSize ||
                effectiveHuntingArea > summary.permitAreaSize;
        };

        $scope.getTotalNumberOfDeadMooses = function () {
            return _(deadMooseCountFields)
                .map(function (fieldName) {
                    return $scope.huntingSummary[fieldName];
                })
                .sum();
        };

        $scope.isTotalHuntingAreaRequired = function () {
            return !_.isFinite($scope.huntingSummary.effectiveHuntingArea);
        };

        $scope.isEffectiveHuntingAreaRequired = function () {
            return !_.isFinite($scope.huntingSummary.effectiveHuntingAreaPercentage) && !_.isFinite($scope.huntingSummary.totalHuntingArea);
        };

        $scope.isRemainingPopulationForTotalAreaRequired = function () {
            var summary = $scope.huntingSummary;

            return !_.isFinite(summary.effectiveHuntingArea) && !_.isFinite(summary.effectiveHuntingAreaPercentage) ||
                !_.isFinite(summary.remainingPopulationInEffectiveArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                (_.isFinite(summary.effectiveHuntingArea) || _.isFinite(summary.effectiveHuntingAreaPercentage));
        };

        $scope.isRemainingPopulationForEffectiveAreaRequired = function () {
            var summary = $scope.huntingSummary;

            return !_.isFinite(summary.totalHuntingArea) ||
                !_.isFinite(summary.remainingPopulationInTotalArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                (_.isFinite(summary.effectiveHuntingArea) || _.isFinite(summary.effectiveHuntingAreaPercentage));
        };

        $scope.getMaxForEffectiveHuntingArea = function () {
            var totalHuntingArea = $scope.huntingSummary.totalHuntingArea;
            return _.isFinite(totalHuntingArea) ? totalHuntingArea : $scope.huntingSummary.permitAreaSize;
        };

        $scope.getMaxForRemainingPopulationInEffectiveArea = function () {
            var remainingPopulationInTotalArea = $scope.huntingSummary.remainingPopulationInTotalArea;
            return _.isFinite(remainingPopulationInTotalArea) ? remainingPopulationInTotalArea : 9999;
        };

        $scope.isHuntingFinished = function () {
            return !!$scope.huntingSummary.huntingFinished;
        };

        $scope.isHuntingFinishedOrLocked = function () {
            return $scope.isHuntingFinished() || $scope.huntingSummary.locked;
        };

        $scope.isHuntingFinishedAndNotLocked = function () {
            return $scope.isHuntingFinished() && !$scope.huntingSummary.locked;
        };

        $scope.isValidExceptMissingFields = function (form) {
            var errors = form.$error;

            if (_.isEmpty(errors)) {
                return true;
            }

            return _.every(_.keys(errors), function (key) {
                return key === 'required';
            });
        };

        var isHuntingAreaAndRemainingPopulationDefined = function () {
            var summary = $scope.huntingSummary;

            var isDefined = function (value) {
                return value !== undefined && value !== null;
            };

            var totalAreaDefined = isDefined(summary.totalHuntingArea);

            return totalAreaDefined && isDefined(summary.remainingPopulationInTotalArea) ||
                isDefined(summary.remainingPopulationInEffectiveArea) && (
                    isDefined(summary.effectiveHuntingArea) ||
                    totalAreaDefined && isDefined(summary.effectiveHuntingAreaPercentage)
                );
        };

        $scope.isValidForFinalSubmit = function (form) {
            return form.$valid && isHuntingAreaAndRemainingPopulationDefined();
        };

        var prepareForSubmit = function (form, checkFormValidFn) {
            var summary = $scope.huntingSummary;

            $scope.$broadcast('show-errors-check-validity');

            if (!checkFormValidFn(form)) {
                return;
            }

            // Nullify appearance fields when appeared is set to false.
            _.forEach(otherSpecies, function (species) {
                var appearance = $scope.huntingSummary[getAppearanceKey(species)];

                if (!appearance.appeared) {
                    _.forOwn(appearance, function (value, key) {
                        if (value !== null && key !== 'appeared') {
                            appearance[key] = null;
                        }
                    });
                }
            });

            // Convert date fields to correct format.
            _.forEach(viewStateDateFields, function (fieldName) {
                summary[fieldName] = Helpers.dateToString($scope.viewState[fieldName]);
            });

            // Delete irrelevant deer fly fields, if not appeared.
            if (summary.deerFliesAppeared === false) {
                _.forEach(deerFlyDateFields.concat(otherDeerFlyFields), function (fieldName) {
                    delete summary[fieldName];
                });
            }
        };

        $scope.markUnfinished = function () {
            var summary = $scope.huntingSummary;
            var params = { id: summary.id, clubId: summary.clubId };

            MooseHuntingSummary.markUnfinished(params).$promise
                .then(function (mooseHuntingSummary) {
                    $scope.huntingSummary = mooseHuntingSummary;
                    $scope.markedUnfinished = true;
                    createAppearances();
                });
        };

        $scope.submit = function (form) {
            prepareForSubmit(form, $scope.isValidExceptMissingFields);
            $scope.$close($scope.huntingSummary);
        };

        $scope.doFinalSubmit = function (form) {
            prepareForSubmit(form, $scope.isValidForFinalSubmit);
            $scope.huntingSummary.huntingFinished = true;
            $scope.$close($scope.huntingSummary);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    })

    .component('rMooseSummaryAppearance', {
        templateUrl: 'harvestpermit/moosepermit/moosesummary/r-moose-summary-appearance.html',
        require: {
            parentForm: '^^form'
        },
        bindings: {
            appearance: '<',
            species: '<',
            populationGrowthTrends: '<',
            index: '<'
        },
        controller: function (GameSpeciesCodes) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var index = $ctrl.index;

                $ctrl.isBeaver = $ctrl.species.key === 'beaver';
                $ctrl.isWildBoar = GameSpeciesCodes.isWildBoar($ctrl.species.gameSpeciesCode);

                $ctrl.appearedInputId = 'appeared' + index;
                $ctrl.trendOfGrowthSelectId = 'trendOfGrowthSelect' + index;
                $ctrl.estimatedAmountOfSpecimensInputId = 'estimatedAmountOfSpecimens' + index;
                $ctrl.estimatedAmountOfSowWithPigletsInputId = 'estimatedAmountOfSowWithPiglets' + index;
                $ctrl.amountOfInhabitedWinterNestsInputId = 'amountOfInhabitedWinterNests' + index;
                $ctrl.beaverHarvestAmountInputId = 'beaverHarvestAmount' + index;
                $ctrl.areaOfDamageInputId = 'areaOfDamage' + index;
                $ctrl.areaOccupiedByWaterInputId = 'areaOccupiedByWater' + index;
                $ctrl.additionalBeaverInfoInputId = 'additionalBeaverInfo' + index;
            };
        }
    })
;
