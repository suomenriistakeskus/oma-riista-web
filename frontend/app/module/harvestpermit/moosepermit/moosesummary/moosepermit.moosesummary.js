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

    .service('MooseHuntingSummaryService', function (MooseHuntingSummaryAreaType, MooseHuntingSummary,
                                                     MooseHuntingSummaryPopulationGrowthTrend,
                                                     FormSidebarService, GameDiaryParameters,
                                                     GameSpeciesCodes) {

        var modalOptions = {
            controller: 'MooseHuntingSummaryFormController',
            templateUrl: 'harvestpermit/moosepermit/moosesummary/moose-hunting-summary.html',
            largeDialog: true,
            resolve: {
                areaTypes: function () {
                    return _.values(MooseHuntingSummaryAreaType);
                },
                species: function () {
                    var transformSpecies = function (parameters, gameSpeciesCode, key) {
                        var retrievedSpecies = _.find(parameters.species, function (species) {
                            return species.code === gameSpeciesCode;
                        });
                        return {
                            key: key,
                            gameSpeciesCode: gameSpeciesCode,
                            name: retrievedSpecies.name || {}
                        };
                    };

                    return GameDiaryParameters.query().$promise.then(function (parameters) {
                        var mooselikeSpecies = [];
                        var deerCodes = {
                            fallowDeer: GameSpeciesCodes.FALLOW_DEER,
                            roeDeer: GameSpeciesCodes.ROE_DEER,
                            whiteTailedDeer: GameSpeciesCodes.WHITE_TAILED_DEER,
                            wildForestReindeer: GameSpeciesCodes.WILD_FOREST_REINDEER
                        };
                        _.forOwn(deerCodes, function (gameSpeciesCode, key) {
                            mooselikeSpecies.push(transformSpecies(parameters, gameSpeciesCode, key));
                        });

                        var wildBoarCode = {wildBoar: GameSpeciesCodes.WILD_BOAR};
                        var wildBoar;
                        _.forOwn(wildBoarCode, function (gameSpeciesCode, key) {
                            wildBoar = transformSpecies(parameters, gameSpeciesCode, key);
                        });

                        return {
                            mooselike: mooselikeSpecies,
                            wildBoar: wildBoar
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

    .controller('MooseHuntingSummaryFormController', function ($scope, MooseHuntingSummary, Helpers, areaTypes,
                                                               mooseHuntingSummary, species, speciesAmount,
                                                               populationGrowthTrends) {

        $scope.huntingSummary = mooseHuntingSummary;
        $scope.areaTypes = areaTypes;
        $scope.populationGrowthTrends = populationGrowthTrends;
        $scope.markedUnfinished = false;

        $scope.speciesAmount = speciesAmount;
        $scope.mooselikeSpecies = species.mooselike;
        $scope.wildBoar = species.wildBoar;
        var ALL_SPECIES = _.union($scope.mooselikeSpecies, [$scope.wildBoar]);

        var deerFlyDateFields = ['dateOfFirstDeerFlySeen', 'dateOfLastDeerFlySeen'];
        var otherDeerFlyFields = [
            'numberOfAdultMoosesHavingFlies', 'numberOfYoungMoosesHavingFlies', 'trendOfDeerFlyPopulationGrowth'
        ];

        var mandatoryViewStateDateFields = ['huntingEndDate'];
        var otherMandatoryFields = ['huntingAreaType'];

        var viewStateDateFields = mandatoryViewStateDateFields.concat(deerFlyDateFields).concat([
            'mooseHeatBeginDate', 'mooseHeatEndDate', 'mooseFawnBeginDate', 'mooseFawnEndDate'
        ]);

        var deadMooseCountFields = ['numberOfDrownedMooses', 'numberOfMoosesKilledByBear',
            'numberOfMoosesKilledByWolf', 'numberOfMoosesKilledInTrafficAccident',
            'numberOfMoosesKilledByPoaching', 'numberOfMoosesKilledInRutFight',
            'numberOfStarvedMooses', 'numberOfMoosesDeceasedByOtherReason'
        ];

        var getAppearanceKey = function (species) {
            return species.key + 'Appearance';
        };

        var propertyPathForSpeciesAppeared = function (species) {
            return getAppearanceKey(species) + '.appeared';
        };

        function createAppearances() {
            otherMandatoryFields = ['huntingAreaType'];
            $scope.mooselikeSpeciesAppearances = [];
            // initialize missing appearances to empty object
            _.forEach(ALL_SPECIES, function (species) {
                var key = getAppearanceKey(species);
                $scope.huntingSummary[key] = $scope.huntingSummary[key] || {};
            });
            // create mooselike apprearances
            _.forEach($scope.mooselikeSpecies, function (species) {
                var mooselikeAppearance = $scope.huntingSummary[getAppearanceKey(species)];
                $scope.mooselikeSpeciesAppearances.push(mooselikeAppearance);
                otherMandatoryFields.push(propertyPathForSpeciesAppeared(species));
            });
            // create wild boar appearance
            $scope.wildBoarAppearance = $scope.huntingSummary[getAppearanceKey($scope.wildBoar)];
            otherMandatoryFields.push(propertyPathForSpeciesAppeared($scope.wildBoar));
        }
        createAppearances();

        $scope.viewState = {};
        _.forEach(viewStateDateFields, function (fieldName) {
            $scope.viewState[fieldName] = $scope.huntingSummary[fieldName];
        });

        $scope.showPermitAreaSize = function () {
            var summary = $scope.huntingSummary;
            var totalHuntingArea = summary.totalHuntingArea;
            return !_.isFinite(totalHuntingArea) || totalHuntingArea > summary.permitAreaSize;
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
            return $scope.huntingSummary.totalHuntingArea || $scope.huntingSummary.permitAreaSize;
        };

        $scope.getMaxForRemainingPopulationInEffectiveArea = function () {
            return $scope.huntingSummary.remainingPopulationInTotalArea || 9999;
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

        $scope.isValid = function (form) {
            return form.$valid;
        };

        var allRequiredFieldsPopulated = function () {
            var summary = $scope.huntingSummary;

            var getProperties = function (object, propertyPaths) {
                return _.map(propertyPaths, _.propertyOf(object));
            };

            var isDefined = function (value) {
                return value !== undefined && value !== null;
            };

            var allDefined = function (collection) {
                return _.every(collection, isDefined);
            };

            var isHuntingAreaAndRemainingPopulationDefined = function () {
                var totalAreaDefined = isDefined(summary.totalHuntingArea);

                return totalAreaDefined && isDefined(summary.remainingPopulationInTotalArea) ||
                    isDefined(summary.remainingPopulationInEffectiveArea) && (
                        isDefined(summary.effectiveHuntingArea) ||
                        totalAreaDefined && isDefined(summary.effectiveHuntingAreaPercentage)
                    );
            };

            var conditionallyMandatoryFieldsSatisfied = _.chain(ALL_SPECIES)
                .filter(function (species) {
                    return _.property(propertyPathForSpeciesAppeared(species))(summary);
                })
                .map(function (species) {
                    var keys = [
                        getAppearanceKey(species) + '.trendOfPopulationGrowth',
                        getAppearanceKey(species) + '.estimatedAmountOfSpecimens'
                    ];
                    if (species === $scope.wildBoar) {
                        keys.push(getAppearanceKey(species) + '.estimatedAmountOfSowWithPiglets');
                    }
                    return keys;
                })
                .flatten()
                .map(_.propertyOf(summary))
                .every(isDefined)
                .value();

            return allDefined(getProperties($scope.viewState, mandatoryViewStateDateFields)) &&
                allDefined(getProperties(summary, otherMandatoryFields)) &&
                isHuntingAreaAndRemainingPopulationDefined() &&
                conditionallyMandatoryFieldsSatisfied;
        };

        $scope.isValidForFinalSubmit = function (form) {
            return $scope.isValid(form) && allRequiredFieldsPopulated();
        };

        var prepareForSubmit = function (form, checkFormValidFn) {
            var summary = $scope.huntingSummary;

            $scope.$broadcast('show-errors-check-validity');

            if (!checkFormValidFn(form)) {
                return;
            }

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
            prepareForSubmit(form, $scope.isValid);
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
        bindings: {
            appearance: '<',
            species: '<',
            populationGrowthTrends: '<',
            showEstimateOfSowWithPiglets: '<',
            index: '<'
        }
    })
;
