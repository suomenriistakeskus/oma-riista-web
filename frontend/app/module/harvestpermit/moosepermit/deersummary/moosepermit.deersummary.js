'use strict';

angular.module('app.moosepermit.deerhuntingsummary', [])

    .factory('DeerHuntingSummary', function ($resource) {
        return $resource('api/v1/deersummary/:id', {id: "@id"}, {
            update: {method: 'PUT'},
            findByClubIdAndSpeciesAmountId: {
                method: 'GET',
                url: 'api/v1/deersummary/club/:clubId/speciesamount/:speciesAmountId',
                params: {
                    clubId: "@clubId",
                    speciesAmountId: "@speciesAmountId"
                }
            },
            getEditState: {
                method: 'GET',
                url: 'api/v1/deersummary/editstate/club/:clubId/speciesamount/:speciesAmountId',
                params: {
                    clubId: "@clubId",
                    speciesAmountId: "@speciesAmountId"
                }
            },
            markUnfinished: {
                method: 'POST',
                url: 'api/v1/deersummary/:id/markunfinished',
                params: {id: "@id"}
            }
        });
    })

    .service('DeerHuntingSummaryService', function ($q, $uibModal, NotificationService,
                                                    DeerHuntingSummary, GameDiaryParameters) {
        this.editHuntingSummary = function (clubId, speciesAmount) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/moosepermit/deersummary/deer-hunting-summary.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: 'DeerHuntingSummaryFormController',
                resolve: {
                    speciesAmount: _.constant(speciesAmount),
                    getGameSpeciesName: function () {
                        return GameDiaryParameters.query().$promise.then(function (parameters) {
                            return parameters.$getGameName;
                        });
                    },
                    deerHuntingSummary: function () {
                        return DeerHuntingSummary.findByClubIdAndSpeciesAmountId({
                            clubId: clubId,
                            speciesAmountId: speciesAmount.id
                        }).$promise;
                    },
                    formEditingEnabled: function () {
                        return DeerHuntingSummary.getEditState({
                            clubId: clubId,
                            speciesAmountId: speciesAmount.id
                        }).$promise.then(function (editState) {
                            return !editState.isLocked;
                        });
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
                var saveMethod = summary.id ? DeerHuntingSummary.update : DeerHuntingSummary.save;

                return saveMethod(summary).$promise.then(function (result) {
                    NotificationService.showDefaultSuccess();
                    return result;

                }, function (err) {
                    NotificationService.showDefaultFailure();
                    return $q.reject(err);
                });
            });
        };
    })

    .controller('DeerHuntingSummaryFormController', function ($uibModalInstance, $scope,
                                                              DeerHuntingSummary, Helpers, deerHuntingSummary,
                                                              speciesAmount, formEditingEnabled, getGameSpeciesName) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.summary = deerHuntingSummary;
            $ctrl.speciesAmount = speciesAmount;
            $ctrl.markedUnfinished = false;
            $ctrl.huntingEndDate = $ctrl.summary.huntingEndDate;
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.submit = function (form) {
            prepareForSubmit(form, $ctrl.isValid);

            $uibModalInstance.close($ctrl.summary);
        };

        $ctrl.doFinalSubmit = function (form) {
            prepareForSubmit(form, $ctrl.isValidForFinalSubmit);

            $ctrl.summary.huntingFinished = true;

            $uibModalInstance.close($ctrl.summary);
        };

        $ctrl.markUnfinished = function () {
            var summary = $ctrl.summary;

            DeerHuntingSummary.markUnfinished({id: summary.id}).$promise.then(function (deerHuntingSummary) {
                $ctrl.summary = deerHuntingSummary;
                $ctrl.markedUnfinished = true;
            });
        };

        $ctrl.isValid = function (form) {
            return form.$valid;
        };

        $ctrl.isValidForFinalSubmit = function (form) {
            return $ctrl.isValid(form) && allRequiredFieldsPopulated();
        };

        $ctrl.getGameSpeciesName = function () {
            return getGameSpeciesName($ctrl.summary.gameSpeciesCode);
        };

        $ctrl.showAlertForPermitAreaSize = function () {
            var summary = $ctrl.summary;
            var totalHuntingArea = summary.totalHuntingArea;

            return !_.isFinite(totalHuntingArea) || totalHuntingArea > summary.permitAreaSize;
        };

        $ctrl.isTotalHuntingAreaRequired = function () {
            return !_.isFinite($ctrl.summary.effectiveHuntingArea);
        };

        $ctrl.isEffectiveHuntingAreaRequired = function () {
            return !_.isFinite($ctrl.summary.totalHuntingArea);
        };

        $ctrl.isRemainingPopulationForTotalAreaRequired = function () {
            var summary = $ctrl.summary;

            return !_.isFinite(summary.effectiveHuntingArea) ||
                !_.isFinite(summary.remainingPopulationInEffectiveArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                _.isFinite(summary.effectiveHuntingArea);
        };

        $ctrl.isRemainingPopulationForEffectiveAreaRequired = function () {
            var summary = $ctrl.summary;

            return !_.isFinite(summary.totalHuntingArea) ||
                !_.isFinite(summary.remainingPopulationInTotalArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                _.isFinite(summary.effectiveHuntingArea);
        };

        $ctrl.getMaxForEffectiveHuntingArea = function () {
            return $ctrl.summary.totalHuntingArea || $ctrl.summary.permitAreaSize;
        };

        $ctrl.getMaxForRemainingPopulationInEffectiveArea = function () {
            return $ctrl.summary.remainingPopulationInTotalArea || 9999;
        };

        $ctrl.isHuntingFinished = function () {
            return !!$ctrl.summary.huntingFinished;
        };

        $ctrl.isHuntingFinishedOrLocked = function () {
            return $ctrl.isHuntingFinished() || !formEditingEnabled;
        };

        $ctrl.isHuntingFinishedAndNotLocked = function () {
            return $ctrl.isHuntingFinished() && formEditingEnabled;
        };

        function allRequiredFieldsPopulated() {
            var summary = $ctrl.summary;

            var isDefined = function (value) {
                return angular.isDefined(value) && value !== null;
            };

            var isHuntingAreaAndRemainingPopulationDefined = function () {
                return isDefined(summary.totalHuntingArea) && isDefined(summary.remainingPopulationInTotalArea) ||
                    isDefined(summary.effectiveHuntingArea) && isDefined(summary.remainingPopulationInEffectiveArea);
            };

            return $ctrl.huntingEndDate && isHuntingAreaAndRemainingPopulationDefined();
        }

        function prepareForSubmit(form, checkFormValidFn) {
            $scope.$broadcast('show-errors-check-validity');

            if (!checkFormValidFn(form)) {
                return;
            }

            $ctrl.summary.huntingEndDate = Helpers.dateToString($ctrl.huntingEndDate);
        }
    });
