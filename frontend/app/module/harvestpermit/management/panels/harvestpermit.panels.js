'use strict';

angular.module('app.harvestpermit.management.panels', [])

    .component('permitOtherPanels', {
        templateUrl: 'harvestpermit/management/panels/other-panels.html',
        bindings: {
            permit: '<'
        },
        controller: function ($state, NotificationService, PermitEndOfHuntingReportModal) {

            var $ctrl = this;

            $ctrl.showUsage = function () {
                var currentState = $state.current;
                $state.go('permitmanagement.usage', {
                    permitId: $ctrl.permit.id
                }).catch(function () {
                    $state.go(currentState.name, currentState.params);
                    NotificationService.showDefaultFailure();
                });
            };

            $ctrl.showEndOfHuntingReport = function () {
                PermitEndOfHuntingReportModal.openModal($ctrl.permit.id).finally(function () {
                    $state.reload();
                });
            };
        }
    })

    .component('permitMooselikePanels', {
        templateUrl: 'harvestpermit/management/panels/mooselike-panels.html',
        bindings: {
            permit: '<',
            getGameSpeciesName: '<',
            selectedGameSpeciesCode: '<',
            changeGameSpeciesCode: '<',
            huntingYear: '<'
        },
        controller: function ($state, MoosePermitLeadersService, HarvestPermitAmendmentApplicationService) {

            var $ctrl = this;

            $ctrl.getSelectedSpeciesName = function () {
                return $ctrl.getGameSpeciesName($ctrl.selectedGameSpeciesCode);
            };

            $ctrl.editAllocations = function () {
                $state.go('permitmanagement.allocation', {
                    permitId: $ctrl.permit.id,
                    gameSpeciesCode: $ctrl.selectedGameSpeciesCode
                });
            };

            $ctrl.showHuntingGroupLeaders = function () {
                MoosePermitLeadersService.showLeadersForContactPerson({
                    id: $ctrl.permit.id,
                    huntingYear: $ctrl.huntingYear,
                    gameSpeciesCode: $ctrl.selectedGameSpeciesCode
                });
            };

            $ctrl.showMap = function () {
                $state.go('permitmanagement.map', {
                    permitId: $ctrl.permit.id,
                    gameSpeciesCode: $ctrl.selectedGameSpeciesCode,
                    huntingYear: $ctrl.huntingYear
                });
            };

            $ctrl.showTables = function () {
                $state.go('permitmanagement.tables', {
                    permitId: $ctrl.permit.id,
                    gameSpeciesCode: $ctrl.selectedGameSpeciesCode
                });
            };

            $ctrl.listAmendmentApplications = function () {
                HarvestPermitAmendmentApplicationService.openModal($ctrl.permit);
            };

            $ctrl.endHuntingForMooselikePermit = function () {
                $state.go('permitmanagement.endofmooselikehunting', {
                    permitId: $ctrl.permit.id,
                    gameSpeciesCode: $ctrl.selectedGameSpeciesCode
                });
            };
        }
    })

    .component('permitNestremovalPanels', {
        templateUrl: 'harvestpermit/management/panels/nestremoval-panels.html',
        bindings: {
            permit: '<',
            lastModifier: '<'
        },
        controller: function ($state, NotificationService, EndOfHuntingNestRemovalReportModal) {

            var $ctrl = this;

            $ctrl.reportUsage = function () {
                var currentState = $state.current;
                $state.go('permitmanagement.nestremoval', {
                    permitId: $ctrl.permit.id
                }).catch(function () {
                    $state.go(currentState.name, currentState.params);
                    NotificationService.showDefaultFailure();
                });
            };

            $ctrl.showEndOfHuntingReport = function () {
                EndOfHuntingNestRemovalReportModal.openModal($ctrl.permit.id).finally(function () {
                    $state.reload();
                });
            };
        }
    })
    .component('permitUsagePanels', {
        templateUrl: 'harvestpermit/management/panels/permit-usage-panels.html',
        bindings: {
            permit: '<',
            lastModifier: '<'
        },
        controller: function ($state, NotificationService, PermitTypes, EndOfPermitPeriodReportModal) {

            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.showEndOfPermitPeriodPanel = $ctrl.permit.permitTypeCode === PermitTypes.GAME_MANAGEMENT;
            };

            $ctrl.reportUsage = function () {
                var currentState = $state.current;
                $state.go('permitmanagement.permitusage', {
                    permitId: $ctrl.permit.id
                }).catch(function () {
                    $state.go(currentState.name, currentState.params);
                    NotificationService.showDefaultFailure();
                });
            };

            $ctrl.showEndOfPermitPeriodReport = function () {
                EndOfPermitPeriodReportModal.openModal($ctrl.permit.id).finally(function () {
                    $state.reload();
                });
            };

        }
    });

