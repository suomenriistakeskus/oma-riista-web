'use strict';

angular.module('app.moosepermit.deerhuntingsummary', [])

    .factory('DeerHuntingSummary', function ($resource) {
        return $resource('api/v1/deersummary/:id', { id: "@id" }, {
            'findByClubIdAndSpeciesAmountId': {
                method: 'GET',
                url: 'api/v1/deersummary/club/:clubId/speciesamount/:speciesAmountId',
                params: {
                    clubId: "@clubId",
                    speciesAmountId: "@speciesAmountId"
                }
            },
            'getEditState': {
                method: 'GET',
                url: 'api/v1/deersummary/editstate/club/:clubId/speciesamount/:speciesAmountId',
                params: {
                    clubId: "@clubId",
                    speciesAmountId: "@speciesAmountId"
                }
            },
            'update': { method: 'PUT' },
            'markUnfinished': {
                method: 'POST',
                url: 'api/v1/deersummary/:id/markunfinished',
                params: { id: "@id" }
            },
        });
    })

    .service('DeerHuntingSummaryService', function ($q, DeerHuntingSummary, FormSidebarService, GameDiaryParameters) {

        var modalOptions = {
            controller: 'DeerHuntingSummaryFormController',
            templateUrl: 'harvestpermit/moosepermit/deersummary/deer-hunting-summary.html',
            largeDialog: true,
            resolve: {
                // Nothing to resolve. Will break if resolve object removed!
            }
        };

        function parametersToResolve(parameters) {
            return {
                deerHuntingSummary: _.constant(parameters.deerHuntingSummary),
                speciesAmount: _.constant(parameters.speciesAmount),
                formEditingEnabled: _.constant(!parameters.editState.isLocked),
                getGameSpeciesName: function () {
                    return GameDiaryParameters.query().$promise.then(function (parameters) {
                        return parameters.$getGameName;
                    });
                }
            };
        }

        var formSidebar = FormSidebarService.create(modalOptions, DeerHuntingSummary, parametersToResolve);

        this.editHuntingSummary = function (clubId, speciesAmount) {
            var params = {
                clubId: clubId,
                speciesAmountId: speciesAmount.id
            };

            return $q.all([
                DeerHuntingSummary.findByClubIdAndSpeciesAmountId(params).$promise,
                DeerHuntingSummary.getEditState(params).$promise])
                .then(function (promises) {
                    var summary = promises[0];
                    var editState = promises[1];

                    return formSidebar.show({
                        id: summary.id,
                        deerHuntingSummary: summary,
                        speciesAmount: speciesAmount,
                        editState: editState
                    });
                });
        };
    })

    .controller('DeerHuntingSummaryFormController', function ($scope, DeerHuntingSummary, Helpers, deerHuntingSummary,
                                                              speciesAmount, formEditingEnabled, getGameSpeciesName) {
        $scope.summary = deerHuntingSummary;
        $scope.speciesAmount = speciesAmount;
        $scope.markedUnfinished = false;

        $scope.viewState = {
            huntingEndDate: $scope.summary.huntingEndDate
        };

        $scope.getGameSpeciesName = function () {
            return getGameSpeciesName($scope.summary.gameSpeciesCode);
        };

        $scope.showPermitAreaSize = function () {
            var summary = $scope.summary;
            var totalHuntingArea = summary.totalHuntingArea;
            return !_.isFinite(totalHuntingArea) || totalHuntingArea > summary.permitAreaSize;
        };

        $scope.isTotalHuntingAreaRequired = function () {
            return !_.isFinite($scope.summary.effectiveHuntingArea);
        };

        $scope.isEffectiveHuntingAreaRequired = function () {
            return !_.isFinite($scope.summary.totalHuntingArea);
        };

        $scope.isRemainingPopulationForTotalAreaRequired = function () {
            var summary = $scope.summary;

            return !_.isFinite(summary.effectiveHuntingArea) ||
                !_.isFinite(summary.remainingPopulationInEffectiveArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                _.isFinite(summary.effectiveHuntingArea);
        };

        $scope.isRemainingPopulationForEffectiveAreaRequired = function () {
            var summary = $scope.summary;

            return !_.isFinite(summary.totalHuntingArea) ||
                !_.isFinite(summary.remainingPopulationInTotalArea) &&
                _.isFinite(summary.totalHuntingArea) &&
                _.isFinite(summary.effectiveHuntingArea);
        };

        $scope.getMaxForEffectiveHuntingArea = function () {
            return $scope.summary.totalHuntingArea || $scope.summary.permitAreaSize;
        };

        $scope.getMaxForRemainingPopulationInEffectiveArea = function () {
            return $scope.summary.remainingPopulationInTotalArea || 9999;
        };

        $scope.isHuntingFinished = function () {
            return !!$scope.summary.huntingFinished;
        };

        $scope.isHuntingFinishedOrLocked = function () {
            return $scope.isHuntingFinished() || !formEditingEnabled;
        };

        $scope.isHuntingFinishedAndNotLocked = function () {
            return $scope.isHuntingFinished() && formEditingEnabled;
        };

        $scope.isValid = function (form) {
            return form.$valid;
        };

        var allRequiredFieldsPopulated = function () {
            var summary = $scope.summary;

            var isDefined = function (value) {
                return angular.isDefined(value) && value !== null;
            };

            var isHuntingAreaAndRemainingPopulationDefined = function () {
                return isDefined(summary.totalHuntingArea) && isDefined(summary.remainingPopulationInTotalArea) ||
                       isDefined(summary.effectiveHuntingArea) && isDefined(summary.remainingPopulationInEffectiveArea);
            };

            return $scope.viewState.huntingEndDate && isHuntingAreaAndRemainingPopulationDefined();
        };

        $scope.isValidForFinalSubmit = function (form) {
            return $scope.isValid(form) && allRequiredFieldsPopulated();
        };

        var prepareForSubmit = function (form, checkFormValidFn) {
            $scope.$broadcast('show-errors-check-validity');

            if (!checkFormValidFn(form)) {
                return;
            }

            $scope.summary.huntingEndDate = Helpers.dateToString($scope.viewState.huntingEndDate);
        };

        $scope.markUnfinished = function () {
            var summary = $scope.summary;

            DeerHuntingSummary.markUnfinished({ id: summary.id }).$promise
                .then(function (deerHuntingSummary) {
                    $scope.summary = deerHuntingSummary;
                    $scope.markedUnfinished = true;
                });
        };

        $scope.submit = function (form) {
            prepareForSubmit(form, $scope.isValid);
            $scope.$close($scope.summary);
        };

        $scope.doFinalSubmit = function (form) {
            prepareForSubmit(form, $scope.isValidForFinalSubmit);
            $scope.summary.huntingFinished = true;
            $scope.$close($scope.summary);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    })
;
