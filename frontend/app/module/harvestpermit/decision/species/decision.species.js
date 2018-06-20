'use strict';

angular.module('app.harvestpermit.decision.species', [])
    .factory('PermitDecisionSpecies', function ($resource) {
        var apiPrefix = 'api/v1/decision/:decisionId';

        return $resource(apiPrefix, {id: '@id', decisionId: '@decisionId'}, {
            getSpecies: {method: 'GET', url: apiPrefix + '/species', isArray: true},
            updateSpecies: {method: 'POST', url: apiPrefix + '/species', isArray: true},
            deleteSpecies: {method: 'DELETE', url: apiPrefix + '/species/:id'}
        });
    })

    .service('PermitDecisionSpeciesModal', function ($uibModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/species/species.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    decisionSpeciesAmounts: function (PermitDecisionSpecies) {
                        return PermitDecisionSpecies.getSpecies({decisionId: decisionId}).$promise;
                    },
                    diaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate, dialogs,
                                 PermitDecisionSpecies, PermitDecisionSpeciesAmountModal,
                                 decisionId, decisionSpeciesAmounts, diaryParameters) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.decisionSpeciesAmounts = decisionSpeciesAmounts;
                $ctrl.availableSpecies = filterAvailableSpecies(decisionSpeciesAmounts);
                $ctrl.gameSpeciesCode = null;
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };

            $ctrl.addSpeciesAmount = function () {
                if ($ctrl.gameSpeciesCode) {
                    $ctrl.edit({
                        gameSpeciesCode: $ctrl.gameSpeciesCode
                    });
                }
            };

            $ctrl.setSpeciesCode = function (speciesCode) {
                $ctrl.gameSpeciesCode = speciesCode;
            };

            $ctrl.getSpeciesName = function (gameSpeciesCode) {
                return diaryParameters
                    ? diaryParameters.$getGameName(gameSpeciesCode, null)
                    : gameSpeciesCode;
            };

            $ctrl.edit = function (spa) {
                var gameSpeciesName = $ctrl.getSpeciesName(spa.gameSpeciesCode);

                PermitDecisionSpeciesAmountModal.open(spa, gameSpeciesName).then(function (result) {
                    PermitDecisionSpecies.updateSpecies({decisionId: decisionId}, result).$promise.then(function () {
                        reloadSpecies();
                    });
                });
            };

            $ctrl.delete = function (spa) {
                confirmDelete().then(function () {
                    PermitDecisionSpecies.deleteSpecies({decisionId: decisionId, id: spa.id}).$promise.then(function () {
                        reloadSpecies();
                    });
                });
            };

            function reloadSpecies() {
                PermitDecisionSpecies.getSpecies({decisionId: decisionId}).$promise.then(function (res) {
                    $ctrl.decisionSpeciesAmounts = res;
                    $ctrl.availableSpecies = filterAvailableSpecies(res);
                });
            }

            function filterAvailableSpecies(input) {
                var selectedSpeciesCodes = _.pluck(input, 'gameSpeciesCode');

                return _.filter(diaryParameters.species, function (s) {
                    return !_.includes(selectedSpeciesCodes, s.code);
                });
            }

            function confirmDelete() {
                var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                var dialogMessage = $translate.instant('global.dialog.confirmation.text');
                return dialogs.confirm(dialogTitle, dialogMessage).result;
            }
        }
    })

    .service('PermitDecisionSpeciesAmountModal', function ($uibModal) {
        this.open = function (spa, gameSpeciesName) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/species/species-amount.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    spa: _.constant(spa),
                    gameSpeciesName: _.constant(gameSpeciesName)
                }
            }).result;
        };

        function ModalController($uibModalInstance, Helpers, HuntingYearService, GameSpeciesCodes,
                                 spa, gameSpeciesName) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var huntingYear = HuntingYearService.getCurrent();

                var defaults = {
                    beginDate: HuntingYearService.getBeginDateStr(huntingYear),
                    endDate: HuntingYearService.getEndDateStr(huntingYear),
                    beginDate2: null,
                    endDate2: null,
                    restrictionType: null,
                    restrictionAmount: null,
                    gameSpeciesCode: null,
                    amount: 0,
                    applicationAmount: 0
                };

                _.assign($ctrl, defaults, spa || {});

                if (!$ctrl.isRestrictionEnabled()) {
                    $ctrl.restrictionType = null;
                    $ctrl.restrictionAmount = null;
                }

                $ctrl.maxAmount = $ctrl.applicationAmount > 0 ? $ctrl.applicationAmount : 9999;
                $ctrl.modalTitle = gameSpeciesName + ' - muokkaa kiintiötä';
            };

            $ctrl.save = function () {
                $uibModalInstance.close(createSpeciesAmount());
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.isValid = function (form) {
                return form.$valid && checkIntervalsDoNotOverlap($ctrl.beginDate2, $ctrl.endDate) &&
                    checkPeriodLessThanYear($ctrl.beginDate, $ctrl.endDate, $ctrl.endDate2);
            };

            $ctrl.isRestrictionEnabled = function () {
                return $ctrl.gameSpeciesCode && GameSpeciesCodes.isMooselike($ctrl.gameSpeciesCode);
            };

            $ctrl.showOverlapError = function () {
                return !checkIntervalsDoNotOverlap($ctrl.beginDate2, $ctrl.endDate);
            };

            $ctrl.showDurationError = function () {
                return !checkPeriodLessThanYear($ctrl.beginDate, $ctrl.endDate, $ctrl.endDate2);
            };

            function checkPeriodLessThanYear(beginDate, endDate, endDate2) {
                beginDate = Helpers.toMoment($ctrl.beginDate, 'YYYY-MM-DD');
                endDate = Helpers.toMoment($ctrl.endDate, 'YYYY-MM-DD');
                endDate2 = Helpers.toMoment($ctrl.endDate2, 'YYYY-MM-DD');

                if (beginDate && beginDate.isValid()) {
                    var diff;

                    if (endDate2 && endDate2.isValid()) {
                        diff = endDate2.diff(beginDate, 'days', true);
                    } else if (endDate && endDate.isValid()) {
                        diff = endDate.diff(beginDate, 'days', true);
                    } else {
                        return true;
                    }

                    return diff < 365;
                }

                return true;
            }

            function checkIntervalsDoNotOverlap(beginDate2, endDate) {
                endDate = Helpers.toMoment($ctrl.endDate, 'YYYY-MM-DD');
                beginDate2 = Helpers.toMoment($ctrl.beginDate2, 'YYYY-MM-DD');

                if (!beginDate2 || !endDate || !endDate.isValid() || !beginDate2.isValid()) {
                    return true;
                }

                return beginDate2.isAfter(endDate);
            }

            function createSpeciesAmount() {
                var hasRestriction = !!$ctrl.restrictionType && $ctrl.restrictionAmount > 0;

                return {
                    gameSpeciesCode: $ctrl.gameSpeciesCode,
                    beginDate: $ctrl.beginDate,
                    endDate: $ctrl.endDate,
                    amount: $ctrl.amount,
                    beginDate2: $ctrl.beginDate2,
                    endDate2: $ctrl.endDate2,
                    restrictionType: hasRestriction ? $ctrl.restrictionType : null,
                    restrictionAmount: hasRestriction ? $ctrl.restrictionAmount : null
                };
            }
        }
    });

