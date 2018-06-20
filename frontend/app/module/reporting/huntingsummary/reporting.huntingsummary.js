'use strict';

angular.module('app.reporting.huntingsummary', [])
    .config(function ($stateProvider) {
        $stateProvider.state('reporting.huntingsummary', {
            url: '/huntingsummary/permit/{permitId:[0-9]{1,8}}/species/{speciesCode:[0-9]{5,6}}',
            templateUrl: 'reporting/huntingsummary/moderator-hunting-summary.html',
            controller: 'ModeratorHuntingSummaryController',
            controllerAs: '$ctrl',
            resolve: {
                permitId: function ($stateParams) {
                    return $stateParams.permitId;
                },
                permit: function (HarvestPermits, permitId) {
                    return HarvestPermits.get({id: permitId}).$promise;
                },
                speciesCode: function ($stateParams) {
                    return _.parseInt($stateParams.speciesCode);
                },
                huntingSummaries: function (MoosePermits, permitId, speciesCode) {
                    var params = {permitId: permitId, speciesCode: speciesCode};

                    return MoosePermits.getClubHuntingSummariesForModeration(params).$promise;
                },
                getGameSpeciesName: function (GameDiaryParameters) {
                    return GameDiaryParameters.query().$promise.then(function (parameters) {
                        return parameters.$getGameName;
                    });
                }
            }
        });
    })
    .controller('ModeratorHuntingSummaryController', function ($state, $history, $timeout, $translate,
                                                               NotificationService, GameSpeciesCodes,
                                                               ActiveRoleService, MoosePermits,
                                                               getGameSpeciesName, huntingSummaries, permit,
                                                               speciesCode) {
        var $ctrl = this;
        var maxSpecimenCount = 99999;

        $ctrl.permit = permit;
        $ctrl.huntingSummaries = huntingSummaries;
        $ctrl.isPermitBasedDeer = GameSpeciesCodes.isPermitBasedDeer(speciesCode);
        $ctrl.completeHuntingOfPermit = true;

        var huntingAreaDefined = function (summary) {
            var totalArea = summary.totalHuntingArea;
            var effectiveArea = summary.effectiveHuntingArea;

            return _.isFinite(totalArea) && totalArea >= 0 ||
                _.isFinite(effectiveArea) && effectiveArea >= 0;
        };

        var remainingPopulationDefined = function (summary) {
            var totalRemaining = summary.remainingPopulationInTotalArea;
            var effectiveRemaining = summary.remainingPopulationInEffectiveArea;

            return _.isFinite(totalRemaining) && totalRemaining >= 0 ||
                _.isFinite(effectiveRemaining) && effectiveRemaining >= 0;
        };

        var idsOfClubsRequiringModeration = [];
        var idsOfClubsOriginallyModerated = _($ctrl.huntingSummaries)
            .filter('moderatorOverridden')
            .map('clubId')
            .value();

        function setModeratorOverriddenFlagForSummariesMissingRequiredData(completeHuntingOfPermit) {
            _.forEach($ctrl.huntingSummaries, function (summary) {
                if (completeHuntingOfPermit) {
                    if (summary.moderatorOverridden || !huntingAreaDefined(summary) || !remainingPopulationDefined(summary)) {
                        summary.moderatorOverridden = true;
                        idsOfClubsRequiringModeration.push(summary.clubId);
                    }
                } else {
                    summary.moderatorOverridden = $ctrl.moderatedOriginally(summary);
                }
            });
        }

        setModeratorOverriddenFlagForSummariesMissingRequiredData($ctrl.completeHuntingOfPermit);

        $ctrl.anyPersistentModeratorOverriddenSummariesPresent = idsOfClubsOriginallyModerated.length > 0;


        $ctrl.completeHuntingOfPermitChanged = function () {
            setModeratorOverriddenFlagForSummariesMissingRequiredData($ctrl.completeHuntingOfPermit);
        };

        $ctrl.moderationRequired = function (clubData) {
            return idsOfClubsRequiringModeration.indexOf(clubData.clubId) >= 0;
        };

        $ctrl.moderatedOriginally = function (clubData) {
            return idsOfClubsOriginallyModerated.indexOf(clubData.clubId) >= 0;
        };

        $ctrl.getGameSpeciesName = function () {
            return getGameSpeciesName(speciesCode);
        };

        $ctrl.countTotalSumOf = function (key) {
            return _.sum($ctrl.huntingSummaries, key);
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
            return clubData.remainingPopulationInTotalArea || maxSpecimenCount;
        };

        $ctrl.isValid = function (form) {
            var summaries = _.filter($ctrl.huntingSummaries, 'moderatorOverridden');
            return summaries.length > 0 && form.$valid &&
                _.every(summaries, huntingAreaDefined) &&
                _.every(summaries, remainingPopulationDefined);
        };

        var goToPreviousState = function () {
            $history.back().catch(function (error) {
                $state.go(ActiveRoleService.isModerator() ? 'jht.home' : 'profile.diary');
            });
        };

        $ctrl.submit = function () {
            var pathVariables = {
                permitId: permit.id,
                speciesCode: speciesCode,
                complete: $ctrl.completeHuntingOfPermit ? 1 : 0
            };

            var requestBody = angular.toJson($ctrl.huntingSummaries);

            MoosePermits.massOverrideClubHuntingSummaries(pathVariables, requestBody).$promise.then(function () {
                NotificationService.showMessage('reporting.huntingSummary.onSavedNotification', 'success');
                goToPreviousState();
            });
        };

        $ctrl.delete = function () {
            var pathVariables = {
                permitId: permit.id,
                speciesCode: speciesCode
            };

            MoosePermits.deleteModeratorOverriddenClubHuntingSummaries(pathVariables).$promise.then(function () {
                NotificationService.showMessage('reporting.huntingSummary.onDeletedNotification', 'success');
                goToPreviousState();
            });
        };

        $ctrl.cancel = function () {
            goToPreviousState();
        };
    });
