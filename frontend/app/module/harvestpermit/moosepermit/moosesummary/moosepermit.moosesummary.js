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
            findByClubIdAndPermitId: {
                method: 'GET',
                url: 'api/v1/club/:clubId/moosesummary/permit/:permitId',
                params: {
                    clubId: '@clubId',
                    permitId: '@permitId'
                }
            },
            update: {method: 'PUT'},
            markUnfinished: {
                method: 'POST',
                url: 'api/v1/club/:clubId/moosesummary/:id/markunfinished',
                params: {clubId: '@clubId', id: '@id'}
            }
        });
    })

    .service('MooseHuntingSummaryService', function ($q, $translate, $uibModal, NotificationService,
                                                     GameDiaryParameters,
                                                     GameSpeciesCodes, MooseHuntingSummary,
                                                     MooseHuntingSummaryAreaType,
                                                     MooseHuntingSummaryPopulationGrowthTrend) {
        this.editHuntingSummary = function (clubId, permitId, speciesAmount) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/moosepermit/moosesummary/moose-hunting-summary.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: 'MooseHuntingSummaryFormController',
                resolve: {
                    mooseHuntingSummary: function () {
                        return MooseHuntingSummary.findByClubIdAndPermitId({
                            clubId: clubId,
                            permitId: permitId
                        }).$promise;
                    },
                    speciesAmount: _.constant(speciesAmount),
                    areaTypes: function () {
                        return _.values(MooseHuntingSummaryAreaType);
                    },
                    appearedSpecies: function () {
                        return GameDiaryParameters.query().$promise.then(function (parameters) {
                            return getAppearedSpecies(parameters);
                        });
                    },
                    populationGrowthTrends: function () {
                        return _.values(MooseHuntingSummaryPopulationGrowthTrend);
                    }
                }
            });

            modalInstance.rendered.then(function () {
                var nodeList = document.querySelectorAll('.modal');

                for (var i = 0; i < nodeList.length; i++) {
                    nodeList[i].scrollTop = 0;
                }
            });

            return modalInstance.result.then(function (summary) {
                var saveMethod = summary.id ? MooseHuntingSummary.update : MooseHuntingSummary.save;

                return saveMethod(summary).$promise.then(function (result) {
                    NotificationService.showDefaultSuccess();
                    return result;

                }, function (err) {
                    NotificationService.showDefaultFailure();
                    return $q.reject(err);
                });
            });
        };

        function constructSpecies(diaryParameters, gameSpeciesCode, localisationKey) {
            var retrievedSpecies = _.find(diaryParameters.species, function (species) {
                return species.code === gameSpeciesCode;
            });

            return {
                key: localisationKey,
                gameSpeciesCode: gameSpeciesCode,
                name: retrievedSpecies.name || {}
            };
        }

        function getAppearedSpecies(diaryParameters) {
            return {
                mooselike: [
                    constructSpecies(diaryParameters, GameSpeciesCodes.FALLOW_DEER, 'fallowDeer'),
                    constructSpecies(diaryParameters, GameSpeciesCodes.ROE_DEER, 'roeDeer'),
                    constructSpecies(diaryParameters, GameSpeciesCodes.WHITE_TAILED_DEER, 'whiteTailedDeer'),
                    constructSpecies(diaryParameters, GameSpeciesCodes.WILD_FOREST_REINDEER, 'wildForestReindeer')
                ],
                wildBoar: constructSpecies(diaryParameters, GameSpeciesCodes.WILD_BOAR, 'wildBoar'),
                beaver: {
                    key: 'beaver',
                    gameSpeciesCode: null,
                    name: {
                        fi: $translate.instant('club.hunting.mooseHuntingSummary.beaver')
                    }
                }
            };
        }
    })

    .controller('MooseHuntingSummaryFormController', function ($uibModalInstance, $scope,
                                                               Helpers, MooseHuntingSummary,
                                                               appearedSpecies, areaTypes, mooseHuntingSummary,
                                                               populationGrowthTrends, speciesAmount) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            var year = Helpers.toMoment(speciesAmount.beginDate, 'YYYY-MM-DD').year();
            $ctrl.show2017Fields = year && year >= 2017;

            $ctrl.huntingSummary = mooseHuntingSummary;
            $ctrl.areaTypes = areaTypes;
            $ctrl.populationGrowthTrends = populationGrowthTrends;
            $ctrl.markedUnfinished = false;
            $ctrl.speciesAmount = speciesAmount;
            $ctrl.mooselikeSpecies = appearedSpecies.mooselike;
            $ctrl.wildBoar = appearedSpecies.wildBoar;
            $ctrl.beaver = appearedSpecies.beaver;

            var nonMooselike = [appearedSpecies.wildBoar];

            if ($ctrl.show2017Fields) {
                nonMooselike.push(appearedSpecies.beaver);
            }

            $ctrl.otherSpecies = _.union(appearedSpecies.mooselike, nonMooselike);

            $ctrl.deerFlyDateFields = ['dateOfFirstDeerFlySeen', 'dateOfLastDeerFlySeen'];
            $ctrl.otherDeerFlyFields = [
                'numberOfAdultMoosesHavingFlies', 'numberOfYoungMoosesHavingFlies', 'trendOfDeerFlyPopulationGrowth'
            ];

            $ctrl.viewStateDateFields = $ctrl.deerFlyDateFields.concat([
                'mooseHeatBeginDate', 'mooseHeatEndDate', 'mooseFawnBeginDate', 'mooseFawnEndDate', 'huntingEndDate'
            ]);

            $ctrl.deadMooseCountFields = ['numberOfDrownedMooses', 'numberOfMoosesKilledByBear',
                'numberOfMoosesKilledByWolf', 'numberOfMoosesKilledInTrafficAccident',
                'numberOfMoosesKilledByPoaching', 'numberOfMoosesKilledInRutFight',
                'numberOfStarvedMooses', 'numberOfMoosesDeceasedByOtherReason'
            ];

            createAppearances();

            _.forEach($ctrl.viewStateDateFields, function (fieldName) {
                $ctrl[fieldName] = $ctrl.huntingSummary[fieldName];
            });
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.submit = function (form) {
            prepareForSubmit(form, $ctrl.isValidExceptMissingFields);

            $uibModalInstance.close($ctrl.huntingSummary);
        };

        $ctrl.doFinalSubmit = function (form) {
            prepareForSubmit(form, $ctrl.isValidForFinalSubmit);

            $ctrl.huntingSummary.huntingFinished = true;

            $uibModalInstance.close($ctrl.huntingSummary);
        };

        $ctrl.markUnfinished = function () {
            var summary = $ctrl.huntingSummary;

            MooseHuntingSummary.markUnfinished({
                id: summary.id,
                clubId: summary.clubId
            }).$promise.then(function (mooseHuntingSummary) {
                $ctrl.huntingSummary = mooseHuntingSummary;
                $ctrl.markedUnfinished = true;
                createAppearances();
            });
        };

        $ctrl.showAlertForPermitAreaSize = function (form) {
            var summary = $ctrl.huntingSummary;
            var totalHuntingArea = parseInt(form.totalHuntingArea.$viewValue, 10);
            var effectiveHuntingArea = parseInt(form.effectiveHuntingArea.$viewValue, 10);

            return !_.isFinite(totalHuntingArea) && !_.isFinite(effectiveHuntingArea) ||
                totalHuntingArea > summary.permitAreaSize ||
                effectiveHuntingArea > summary.permitAreaSize;
        };

        $ctrl.getTotalNumberOfDeadMooses = function () {
            return _($ctrl.deadMooseCountFields)
                .map(function (fieldName) {
                    return $ctrl.huntingSummary[fieldName];
                })
                .sum();
        };

        $ctrl.isTotalHuntingAreaRequired = function () {
            return !_.isFinite($ctrl.huntingSummary.effectiveHuntingArea);
        };

        $ctrl.isEffectiveHuntingAreaRequired = function () {
            return !_.isFinite($ctrl.huntingSummary.effectiveHuntingAreaPercentage) && !_.isFinite($ctrl.huntingSummary.totalHuntingArea);
        };

        $ctrl.isRemainingPopulationForTotalAreaRequired = function () {
            var summary = $ctrl.huntingSummary;

            return !_.isFinite(summary.effectiveHuntingArea) && !_.isFinite(summary.effectiveHuntingAreaPercentage) ||
                !_.isFinite(summary.remainingPopulationInEffectiveArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                (_.isFinite(summary.effectiveHuntingArea) || _.isFinite(summary.effectiveHuntingAreaPercentage));
        };

        $ctrl.isRemainingPopulationForEffectiveAreaRequired = function () {
            var summary = $ctrl.huntingSummary;

            return !_.isFinite(summary.totalHuntingArea) ||
                !_.isFinite(summary.remainingPopulationInTotalArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                (_.isFinite(summary.effectiveHuntingArea) || _.isFinite(summary.effectiveHuntingAreaPercentage));
        };

        $ctrl.getMaxForEffectiveHuntingArea = function () {
            var totalHuntingArea = $ctrl.huntingSummary.totalHuntingArea;
            return _.isFinite(totalHuntingArea) ? totalHuntingArea : $ctrl.huntingSummary.permitAreaSize;
        };

        $ctrl.getMaxForRemainingPopulationInEffectiveArea = function () {
            var remainingPopulationInTotalArea = $ctrl.huntingSummary.remainingPopulationInTotalArea;
            return _.isFinite(remainingPopulationInTotalArea) ? remainingPopulationInTotalArea : 9999;
        };

        $ctrl.isHuntingFinished = function () {
            return !!$ctrl.huntingSummary.huntingFinished;
        };

        $ctrl.isHuntingFinishedOrLocked = function () {
            return $ctrl.isHuntingFinished() || $ctrl.huntingSummary.locked;
        };

        $ctrl.isHuntingFinishedAndNotLocked = function () {
            return $ctrl.isHuntingFinished() && !$ctrl.huntingSummary.locked;
        };

        $ctrl.isValidExceptMissingFields = function (form) {
            var errors = form.$error;

            if (_.isEmpty(errors)) {
                return true;
            }

            return _.every(_.keys(errors), function (key) {
                return key === 'required';
            });
        };

        $ctrl.isValidForFinalSubmit = function (form) {
            return form.$valid && isHuntingAreaAndRemainingPopulationDefined();
        };

        function isHuntingAreaAndRemainingPopulationDefined() {
            var summary = $ctrl.huntingSummary;

            var isDefined = function (value) {
                return value !== undefined && value !== null;
            };

            var totalAreaDefined = isDefined(summary.totalHuntingArea);

            return totalAreaDefined && isDefined(summary.remainingPopulationInTotalArea) ||
                isDefined(summary.remainingPopulationInEffectiveArea) && (
                    isDefined(summary.effectiveHuntingArea) ||
                    totalAreaDefined && isDefined(summary.effectiveHuntingAreaPercentage)
                );
        }

        function getAppearanceKey(species) {
            return species.key + 'Appearance';
        }

        function createAppearances() {
            _.forEach($ctrl.otherSpecies, function (species) {
                var key = getAppearanceKey(species);

                if (!$ctrl.huntingSummary[key]) {
                    $ctrl.huntingSummary[key] = {};
                }
            });

            $ctrl.mooselikeSpeciesAppearances = _.map($ctrl.mooselikeSpecies, function (species) {
                return $ctrl.huntingSummary[getAppearanceKey(species)];
            });

            $ctrl.wildBoarAppearance = $ctrl.huntingSummary[getAppearanceKey($ctrl.wildBoar)];
            $ctrl.beaverAppearance = $ctrl.huntingSummary[getAppearanceKey($ctrl.beaver)];
        }

        function prepareForSubmit(form, checkFormValidFn) {
            var summary = $ctrl.huntingSummary;

            $scope.$broadcast('show-errors-check-validity');

            if (!checkFormValidFn(form)) {
                return;
            }

            // Nullify appearance fields when appeared is set to false.
            _.forEach($ctrl.otherSpecies, function (species) {
                var appearance = $ctrl.huntingSummary[getAppearanceKey(species)];

                if (!appearance.appeared) {
                    _.forOwn(appearance, function (value, key) {
                        if (value !== null && key !== 'appeared') {
                            appearance[key] = null;
                        }
                    });
                }
            });

            // Convert date fields to correct format.
            _.forEach($ctrl.viewStateDateFields, function (fieldName) {
                summary[fieldName] = Helpers.dateToString($ctrl[fieldName]);
            });

            // Delete irrelevant deer fly fields, if not appeared.
            if (summary.deerFliesAppeared === false) {
                _.forEach($ctrl.deerFlyDateFields.concat($ctrl.otherDeerFlyFields), function (fieldName) {
                    delete summary[fieldName];
                });
            }
        }
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
