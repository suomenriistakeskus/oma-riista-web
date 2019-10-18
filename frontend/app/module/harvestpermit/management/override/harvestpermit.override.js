'use strict';

angular.module('app.harvestpermit.management.override', [])
    .config(function ($stateProvider) {
        $stateProvider.state('permitmanagement.override', {
            url: '/override/{speciesCode:[0-9]{5,6}}',
            templateUrl: 'harvestpermit/management/override/moderator-hunting-summary.html',
            controller: 'MoosePermitModeratorOverrideController',
            controllerAs: '$ctrl',
            resolve: {
                speciesCode: function ($stateParams) {
                    return _.parseInt($stateParams.speciesCode);
                },
                huntingSummaries: function (MoosePermits, permitId, speciesCode) {
                    return MoosePermits.getClubHuntingSummariesForModeration({
                        permitId: permitId,
                        speciesCode: speciesCode
                    }).$promise;
                },
                getGameSpeciesName: function (GameDiaryParameters) {
                    return GameDiaryParameters.query().$promise.then(function (parameters) {
                        return parameters.$getGameName;
                    });
                }
            }
        });
    })
    .controller('MoosePermitModeratorOverrideController', function ($state, $timeout, $translate,
                                                                    NotificationService, GameSpeciesCodes,
                                                                    ActiveRoleService, MoosePermits,
                                                                    getGameSpeciesName, huntingSummaries, permit,
                                                                    speciesCode) {
        var $ctrl = this;

        var idsOfClubsRequiringModeration = [];
        var idsOfClubsOriginallyModerated = [];

        $ctrl.$onInit = function () {
            $ctrl.permit = permit;
            $ctrl.huntingSummaries = huntingSummaries;
            $ctrl.gameSpeciesName = getGameSpeciesName(speciesCode);
            $ctrl.isPermitBasedDeer = GameSpeciesCodes.isPermitBasedDeer(speciesCode);
            $ctrl.completeHuntingOfPermit = true;

            idsOfClubsRequiringModeration = [];
            idsOfClubsOriginallyModerated = _($ctrl.huntingSummaries)
                .filter('moderatorOverridden')
                .map('clubId')
                .value();

            $ctrl.anyPersistentModeratorOverriddenSummariesPresent = idsOfClubsOriginallyModerated.length > 0;

            setModeratorOverriddenFlagForSummariesMissingRequiredData();
        };

        $ctrl.cancel = function () {
            goToPreviousState();
        };

        $ctrl.isValid = function (form) {
            var summaries = _.filter($ctrl.huntingSummaries, 'moderatorOverridden');
            return summaries.length > 0 && form.$valid &&
                _.every(summaries, huntingAreaDefined) &&
                _.every(summaries, remainingPopulationDefined);
        };

        $ctrl.submit = function () {
            var requestBody = angular.toJson($ctrl.huntingSummaries);

            MoosePermits.massOverrideClubHuntingSummaries({
                permitId: permit.id,
                speciesCode: speciesCode,
                complete: $ctrl.completeHuntingOfPermit ? 1 : 0
            }, requestBody).$promise.then(function () {
                NotificationService.showMessage('harvestpermit.management.moderatorOverride.onSavedNotification', 'success');
                goToPreviousState();
            });
        };

        $ctrl.delete = function () {
            MoosePermits.deleteModeratorOverriddenClubHuntingSummaries({
                permitId: permit.id,
                speciesCode: speciesCode
            }).$promise.then(function () {
                NotificationService.showMessage('harvestpermit.management.moderatorOverride.onDeletedNotification', 'success');
                goToPreviousState();
            });
        };

        $ctrl.completeHuntingOfPermitChanged = function () {
            setModeratorOverriddenFlagForSummariesMissingRequiredData();
        };

        $ctrl.moderationRequired = function (clubData) {
            return idsOfClubsRequiringModeration.indexOf(clubData.clubId) >= 0;
        };

        $ctrl.moderatedOriginally = function (clubData) {
            return idsOfClubsOriginallyModerated.indexOf(clubData.clubId) >= 0;
        };

        $ctrl.countTotalSumOf = function (key) {
            return _.sumBy($ctrl.huntingSummaries, key);
        };

        $ctrl.isEitherHuntingAreaPresent = function (clubData) {
            return _.isFinite(clubData.totalHuntingArea) || _.isFinite(clubData.effectiveHuntingArea);
        };

        $ctrl.isRemainingPopulationForTotalAreaRequired = function (clubData) {
            var effectiveHuntingAreaDefined = _.isFinite(clubData.effectiveHuntingArea);

            return !_.isFinite(clubData.remainingPopulationInTotalArea) &&
                !effectiveHuntingAreaDefined ||
                !_.isFinite(clubData.remainingPopulationInEffectiveArea) &&
                _.isFinite(clubData.totalHuntingArea) &&
                effectiveHuntingAreaDefined;
        };

        $ctrl.isRemainingPopulationForEffectiveAreaRequired = function (clubData) {
            var totalHuntingAreaDefined = _.isFinite(clubData.totalHuntingArea);

            return !_.isFinite(clubData.remainingPopulationInEffectiveArea) &&
                !totalHuntingAreaDefined ||
                !_.isFinite(clubData.remainingPopulationInTotalArea) &&
                totalHuntingAreaDefined &&
                _.isFinite(clubData.effectiveHuntingArea);
        };

        $ctrl.getMaxForEffectiveHuntingArea = function (clubData) {
            return clubData.totalHuntingArea || clubData.permitAreaSize;
        };

        $ctrl.getMaxForNonEdibleAdults = function (clubData) {
            return (clubData.adultMales || 0) + (clubData.adultFemales || 0);
        };

        $ctrl.getMaxForNonEdibleYoungs = function (clubData) {
            return (clubData.youngMales || 0) + (clubData.youngFemales || 0);
        };

        $ctrl.getMaxForRemainingPopulationInEffectiveHuntingArea = function (clubData) {
            return clubData.remainingPopulationInTotalArea || 99999;
        };

        function huntingAreaDefined(summary) {
            var totalArea = summary.totalHuntingArea;
            var effectiveArea = summary.effectiveHuntingArea;

            return _.isFinite(totalArea) && totalArea >= 0 ||
                _.isFinite(effectiveArea) && effectiveArea >= 0;
        }

        function remainingPopulationDefined(summary) {
            var totalRemaining = summary.remainingPopulationInTotalArea;
            var effectiveRemaining = summary.remainingPopulationInEffectiveArea;

            return _.isFinite(totalRemaining) && totalRemaining >= 0 ||
                _.isFinite(effectiveRemaining) && effectiveRemaining >= 0;
        }

        function setModeratorOverriddenFlagForSummariesMissingRequiredData() {
            _.forEach($ctrl.huntingSummaries, function (summary) {
                if ($ctrl.completeHuntingOfPermit) {
                    if (summary.moderatorOverridden || !huntingAreaDefined(summary) || !remainingPopulationDefined(summary)) {
                        summary.moderatorOverridden = true;
                        idsOfClubsRequiringModeration.push(summary.clubId);
                    }
                } else {
                    summary.moderatorOverridden = $ctrl.moderatedOriginally(summary);
                }
            });
        }

        function goToPreviousState() {
            $state.go('permitmanagement.endofmooselikehunting', {
                permitId: permit.id,
                gameSpeciesCode: speciesCode
            });
        }
    });
