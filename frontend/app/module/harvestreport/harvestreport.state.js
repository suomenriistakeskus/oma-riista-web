'use strict';

angular.module('app.harvestreport.state', [])
    .component('harvestReportStateTransitions', {
        templateUrl: 'harvestreport/report-state-transitions.html',
        bindings: {
            harvest: '<',
            onStateChange: '&'
        },
        controller: function (ActiveRoleService, HarvestReportStateChangeService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.moderatorView = ActiveRoleService.isModerator();
                $ctrl.showTransitions = true;
            };

            $ctrl.showActions = function () {
                return $ctrl.moderatorView && (!$ctrl.harvest.permitNumber || $ctrl.harvest.stateAcceptedToHarvestPermit === 'ACCEPTED');
            };

            $ctrl.changeState = function (state) {
                HarvestReportStateChangeService.changeState($ctrl.harvest, state, $ctrl.onStateChange);
            };
        }
    })
    .service('HarvestReportStateChangeService', function (NotificationService, TranslatedBlockUI,
                                                          HarvestReports, HarvestReportReasonAsker) {

        this.changeState = function (report, newState, onStateChange) {
            var params = {
                harvestId: report.id,
                rev: report.rev,
                to: newState,
                reason: null
            };

            if (newState === 'APPROVED') {
                return doChange(onStateChange, params);

            } else {
                return HarvestReportReasonAsker.promptForReason().then(function (reason) {
                    params.reason = reason;
                    return doChange(onStateChange, params);
                });
            }
        };

        function doChange(onStateChange, params) {
            TranslatedBlockUI.start("global.block.wait");

            return HarvestReports.changeState(params).$promise
                .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                .finally(function () {
                    TranslatedBlockUI.stop();
                    onStateChange({
                        'state': params.to
                    });
                });
        }
    });
