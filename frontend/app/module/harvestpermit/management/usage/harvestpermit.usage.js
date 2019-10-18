'use strict';

angular.module('app.harvestpermit.management.usage', [])

    .component('permitSpeciesAmountUsage', {
        templateUrl: 'harvestpermit/management/usage/species-amount-usage.html',
        bindings: {
            permitUsage: '<'
        }
    })

    .controller('HarvestPermitUsageController', function ($state, $window, FormPostService, MapDefaults, MapBounds,
                                                          PermitEndOfHuntingReportModal, DiaryEntryService,
                                                          EditHarvestPermitContactPersonsModal, HarvestReportSearchSidebar,
                                                          Harvest, HarvestReportSearchMarkers,
                                                          permit, permitUsage, harvestList) {

        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.showSidebar = HarvestReportSearchSidebar.createSidebar();
            $ctrl.permit = permit;
            $ctrl.permitUsage = permitUsage;
            $ctrl.harvestList = harvestList;
            $ctrl.activeTabIndex = 0;
            $ctrl.markers = HarvestReportSearchMarkers.createMarkers(harvestList, function (marker) {
                $ctrl.showSidebar(_.find(harvestList, {id: marker}));
            }, function (harvest) {
                return harvest.harvestReportState || harvest.stateAcceptedToHarvestPermit;
            });
            $ctrl.mapBounds = MapBounds.getBounds($ctrl.markers, _.identity, MapBounds.getBoundsOfFinland());
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapDefaults = MapDefaults.create({
                dragging: true,
                minZoom: 5
            });
        };

        $ctrl.exportHarvestReports = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/' + $ctrl.permit.id + '/export-reports', {});
        };

        $ctrl.createHarvestForPermit = function () {
            var permitNumber = $ctrl.permit.permitNumber;
            var gameSpeciesCodes = $ctrl.permit.gameSpeciesCodes;
            var gameSpeciesCode = _.size(gameSpeciesCodes) === 1 ? gameSpeciesCodes[0] : null;

            DiaryEntryService.createHarvestForPermit(permitNumber, gameSpeciesCode);
        };
    })

    .component('harvestPermitHarvestList', {
        templateUrl: 'harvestpermit/management/usage/show-harvest-list.html',
        bindings: {
            permit: '<',
            harvestList: '<'
        },
        controller: function ($state, MapDefaults, NotificationService, ActiveRoleService, DiaryEntryService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.mapDefaults = MapDefaults.create();
                $ctrl.moderator = ActiveRoleService.isModerator();
            };

            $ctrl.toggleHarvest = function (harvest) {
                harvest.toggle = !harvest.toggle;
            };

            $ctrl.canEditHarvest = function (harvest) {
                return $ctrl.permit.canAddHarvest && harvest.canEdit;
            };

            $ctrl.showHarvestReportStateTransitions = function () {
                if (!$ctrl.moderator || !$ctrl.permit) {
                    return false;
                }

                var endOfHuntingReportApproved = $ctrl.permit.harvestReportState === 'APPROVED';
                var endOfHuntingReportRejected = $ctrl.permit.harvestReportState === 'REJECTED';
                var endOfHuntingReportDone = !!$ctrl.permit.harvestReportState;

                return !endOfHuntingReportDone || !endOfHuntingReportApproved && !endOfHuntingReportRejected;
            };

            $ctrl.editHarvest = function (harvest) {
                DiaryEntryService.edit(harvest);
            };

            $ctrl.buttonStateColor = function (harvest) {
                return harvest.harvestReportState || harvest.stateAcceptedToHarvestPermit;
            };

            $ctrl.translationKey = function (harvest) {
                return harvest.harvestReportState ?
                    'harvestreport.stateVerbose.' + harvest.harvestReportState :
                    'gamediary.stateVerbose.' + harvest.stateAcceptedToHarvestPermit;
            };

            $ctrl.harvestReportStateChanged = function (harvest, state) {
                harvest.harvestReportState = state;
                harvest.rev = harvest.rev + 1;
                harvest.canEdit = false;
            };
        }
    })

    .component('harvestPermitHarvestStateActions', {
        templateUrl: 'harvestpermit/management/usage/permit-harvest-actions.html',
        bindings: {
            harvest: '<',
            permit: '<'
        },
        controller: function ($state, NotificationService, PermitAcceptHarvest) {
            var $ctrl = this;

            $ctrl.canAcceptOrRejectToPermit = function () {
                var harvestReportApproved = $ctrl.harvest.harvestReportState === 'APPROVED';
                var harvestReportRejected = $ctrl.harvest.harvestReportState === 'REJECTED';
                var endOfHuntingReportDone = !!$ctrl.permit.harvestReportState;

                return !endOfHuntingReportDone && !harvestReportApproved && !harvestReportRejected;
            };

            $ctrl.permitStateChangePossible = function (state) {
                if (!$ctrl.canAcceptOrRejectToPermit()) {
                    return false;
                }

                return $ctrl.harvest.stateAcceptedToHarvestPermit !== state;
            };

            $ctrl.changeAcceptedToPermit = function (state) {
                if (!$ctrl.permitStateChangePossible(state)) {
                    return;
                }

                PermitAcceptHarvest.accept($ctrl.harvest.id, $ctrl.harvest.rev, state).then(function () {
                    $state.reload();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            };
        }
    });
