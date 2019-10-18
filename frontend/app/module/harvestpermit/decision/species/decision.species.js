'use strict';

angular.module('app.harvestpermit.decision.species', [])
    .factory('PermitDecisionSpecies', function ($resource) {
        var apiPrefix = 'api/v1/decision/:decisionId';

        return $resource(apiPrefix, {id: '@id', decisionId: '@decisionId'}, {
            getSpecies: {method: 'GET', url: apiPrefix + '/species', isArray: true},
            updateSpecies: {method: 'POST', url: apiPrefix + '/species', isArray: true},
            getForbiddenMethods: {method: 'GET', url: apiPrefix + '/methods/:id'},
            updateForbiddenMethods: {method: 'POST', url: apiPrefix + '/methods/:id'}
        });
    })

    .service('PermitDecisionSpeciesAmountModal', function ($uibModal) {
        this.open = function (decisionId, gameSpeciesCode, permitTypeCode) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/species/species-amount.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    gameSpeciesCode: _.constant(gameSpeciesCode),
                    permitTypeCode: _.constant(permitTypeCode),
                    speciesAmountList: function ($filter, PermitDecisionSpecies) {
                        return PermitDecisionSpecies.getSpecies({
                            decisionId: decisionId

                        }).$promise.then(function (speciesAmountList) {
                            var dateFilter = $filter('date');

                            return _.chain(speciesAmountList)
                                .filter({gameSpeciesCode: gameSpeciesCode})
                                .map(function (spa) {
                                    spa.year = dateFilter(spa.beginDate, 'yyyy');
                                    return spa;
                                })
                                .sortBy('year')
                                .value();
                        });
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, Helpers, HuntingYearService, NotificationService,
                                 GameSpeciesCodes, PermitDecisionSpecies,
                                 decisionId, gameSpeciesCode, speciesAmountList, permitTypeCode) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.gameSpeciesCode = gameSpeciesCode;
                $ctrl.speciesAmountList = speciesAmountList;
                $ctrl.restrictionsInUse = permitTypeCode === '100' && GameSpeciesCodes.isMooselike(gameSpeciesCode);

                if (!$ctrl.restrictionsInUse) {
                    _.forEach($ctrl.speciesAmountList, function (spa) {
                        spa.restrictionType = null;
                        spa.restrictionAmount = null;
                    });
                }
            };

            $ctrl.save = function () {
                var dtoList = _.map($ctrl.speciesAmountList, function (spa) {
                    var hasRestriction = !!spa.restrictionType && spa.restrictionAmount > 0;

                    return {
                        id: spa.id,
                        gameSpeciesCode: spa.gameSpeciesCode,
                        beginDate: spa.beginDate,
                        endDate: spa.endDate,
                        amount: spa.amount,
                        beginDate2: spa.beginDate2,
                        endDate2: spa.endDate2,
                        restrictionType: hasRestriction ? spa.restrictionType : null,
                        restrictionAmount: hasRestriction ? spa.restrictionAmount : null
                    };
                });

                PermitDecisionSpecies.updateSpecies({decisionId: decisionId}, {list: dtoList}).$promise.then(function () {
                    $uibModalInstance.close();

                }, function () {
                    NotificationService.showDefaultFailure();
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.isValid = function (form) {
                return form.$valid && _.every($ctrl.speciesAmountList, function (spa) {
                    return checkIntervalsDoNotOverlap(spa.beginDate2, spa.endDate) &&
                        checkPeriodLessThanYear(spa.beginDate, spa.endDate, spa.endDate2);
                });
            };

            $ctrl.showOverlapError = function (spa) {
                return !checkIntervalsDoNotOverlap(spa.beginDate2, spa.endDate);
            };

            $ctrl.showDurationError = function (spa) {
                return !checkPeriodLessThanYear(spa.beginDate, spa.endDate, spa.endDate2);
            };

            function checkPeriodLessThanYear(beginDate, endDate, endDate2) {
                beginDate = Helpers.toMoment(beginDate, 'YYYY-MM-DD');
                endDate = Helpers.toMoment(endDate, 'YYYY-MM-DD');
                endDate2 = Helpers.toMoment(endDate2, 'YYYY-MM-DD');

                if (beginDate && beginDate.isValid()) {
                    var diff;

                    if (endDate2 && endDate2.isValid()) {
                        diff = endDate2.diff(beginDate, 'days', true);
                    } else if (endDate && endDate.isValid()) {
                        diff = endDate.diff(beginDate, 'days', true);
                    } else {
                        return true;
                    }

                    return diff <= 365;
                }

                return true;
            }

            function checkIntervalsDoNotOverlap(beginDate2, endDate) {
                endDate = Helpers.toMoment(endDate, 'YYYY-MM-DD');
                beginDate2 = Helpers.toMoment(beginDate2, 'YYYY-MM-DD');

                if (!beginDate2 || !endDate || !endDate.isValid() || !beginDate2.isValid()) {
                    return true;
                }

                return beginDate2.isAfter(endDate);
            }
        }
    })

    .service('PermitDecisionSpeciesMethodModal', function ($uibModal) {
        this.open = function (decisionId, gameSpeciesCode) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/species/species-method.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    gameSpeciesCode: _.constant(gameSpeciesCode),
                    dto: function (PermitDecisionSpecies) {
                        return PermitDecisionSpecies.getForbiddenMethods({
                            decisionId: decisionId,
                            id: gameSpeciesCode
                        }).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, PermitDecisionSpecies, PermitDecisionForbiddenMethodType,
                                 decisionId, gameSpeciesCode, dto) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.gameSpeciesCode = gameSpeciesCode;
                $ctrl.methodList = _.map(PermitDecisionForbiddenMethodType, function (method) {
                    return {
                        type: method,
                        selected: _.includes(dto.forbiddenMethodTypes, method)
                    };
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.isValid = function (form) {
                return form.$valid;
            };

            $ctrl.save = function () {
                PermitDecisionSpecies.updateForbiddenMethods({decisionId: decisionId, id: gameSpeciesCode}, {
                    forbiddenMethodTypes: _.chain($ctrl.methodList).map(function (m) {
                        return m.selected ? m.type : null;
                    }).filter().value()
                }).$promise.then(function () {
                    $uibModalInstance.close();
                });
            };
        }
    });

