(function () {
    'use strict';

    angular.module('app.moosepermit.directives', [])
        .directive('moosePermitTodo', MoosePermitTodoDirective)
        .directive('moosePermitListDetails', MoosePermitListDetailsDirective);

    function MoosePermitTodoDirective() {
        return {
            restrict: 'E',
            scope: {
                todo: '='
            },
            bindToController: true,
            controllerAs: '$ctrl',
            template: '<span ng-if="$ctrl.showTodo()" uib-tooltip="{{$ctrl.tooltipTxt}}" tooltip-placement="right" class="text-danger fa fa-exclamation-triangle">&nbsp;</span>',
            controller: function ($scope, $translate) {
                this.showTodo = function () {
                    return this.todo.todo;
                };

                var keys = [
                    this.todo.areaMissing ? 'club.permit.todo.area' : null,
                    this.todo.groupMissing ? 'club.permit.todo.group' : null,
                    this.todo.groupPermitMissing ? 'club.permit.todo.groupPermit' : null,
                    this.todo.groupLeaderMissing ? 'club.permit.todo.groupLeader' : null
                ];
                var str = _(keys).filter().map($translate.instant).join(', ');

                this.tooltipTxt = $translate.instant('club.permit.todo.prefix') + ' ' + str;
            }
        };
    }

    function MoosePermitListDetailsDirective() {
        return {
            restrict: 'E',
            scope: {
                showFunc: '&',
                editFunc: '&',
                mapFunc: '&',
                lukereportsFunc: '&',
                leadersFunc: '&',
                rhystatsFunc:'&',
                selectedPermit: '='
            },
            templateUrl: 'harvestpermit/moosepermit/list-details.html',
            controllerAs: '$ctrl',
            bindToController: false,
            controller: MoosePermitListDetailsController
        };
    }

    function MoosePermitListDetailsController($scope, $state, $translate, ActiveRoleService, DeerHuntingSummaryService,
                                              GameSpeciesCodes, MooseHarvestReportService, MooseHuntingSummaryService,
                                              MoosePermitPdfUrl) {
        var $ctrl = this;

        $ctrl.isModerator = ActiveRoleService.isModerator();

        $ctrl.show = $scope.showFunc();
        $ctrl.edit = $scope.editFunc();
        $ctrl.map = $scope.mapFunc();
        $ctrl.lukereports = $scope.lukereportsFunc();
        $ctrl.leaders = $scope.leadersFunc();
        $ctrl.rhystats = $scope.rhystatsFunc();

        $ctrl.getPdfUrl = MoosePermitPdfUrl.get;

        $ctrl.isActive = function () {
            return _.any(arguments, _.partial(_.endsWith, $state.current.name, _, undefined));
        };

        function reload() {
            $state.reload();
        }

        $scope.$watch('selectedPermit', function (permit) {
            if (!permit) {
                return;
            }
            $ctrl.selectedPermit = permit;

            var spa = permit.speciesAmount;
            var species = spa.gameSpecies;
            $ctrl.isMoosePermit = GameSpeciesCodes.isMoose(species.code);

            $ctrl.finishHuntingByModeratorOverride = function () {
                $state.go('reporting.huntingsummary', {permitId: permit.id, speciesCode: species.code});
            };

            $ctrl.editHuntingSummary = function () {
                var clubId = permit.viewedClubId;

                if ($ctrl.isMoosePermit) {
                    MooseHuntingSummaryService.editHuntingSummary(clubId, permit.id, spa).finally(reload);
                } else {
                    DeerHuntingSummaryService.editHuntingSummary(clubId, spa).finally(reload);
                }
            };

            $ctrl.isHuntingFinished = permit.huntingFinished && (!permit.mooseHarvestReport || !permit.mooseHarvestReport.moderatorOverride);
            $ctrl.isPermitFinished = permit.mooseHarvestReport;
            $ctrl.isModeratorOverridden = permit.mooseHarvestReport && permit.mooseHarvestReport.moderatorOverride || permit.huntingFinishedByModeration;
            $ctrl.isFinishHuntingByModeratorOverrideVisible = $ctrl.isModerator && (!permit.mooseHarvestReport || permit.mooseHarvestReport.moderatorOverride);
            $ctrl.listLeadersButtonVisible = permit.listLeadersButtonVisible;

            $ctrl.editMoosePermitHarvestReport = function () {
                MooseHarvestReportService.editMoosePermitHarvestReport(permit, species).finally(reload);
            };

            $ctrl.canEditAllocations = permit.canEditAllocations;
            $ctrl.viewedClubIsPartner = permit.viewedClubIsPartner;

            $ctrl.originalPermitAmount = spa.amount;
            $ctrl.amendmentPermitAmount = _.sum(_.values(permit.amendmentPermits));
            $ctrl.permitRestrictionType = spa.restrictionType;
            $ctrl.permitRestrictionAmount = spa.restrictionAmount;
            $ctrl.permitTotal = permit.total;
            $ctrl.permitUnallocated = permit.unallocated;
            $ctrl.usedPermits = permit.used;
            $ctrl.notEdiblePermits = permit.notEdible;

            $ctrl.permitNumbers = collectPermitNumbers(permit);
        });

        var amendmentPermitPrefix = $translate.instant('club.permit.amendmentPermitName');
        var permitPrefix = $translate.instant('club.permit.permitName');

        function collectPermitNumbers(permit) {
            var keys = _(permit.amendmentPermits).keys().sort().map(function (n) {
                return {text: amendmentPermitPrefix + n, permitNumber: n};
            }).value();

            keys.unshift({text: permitPrefix + ' ' + permit.permitNumber, permitNumber: permit.permitNumber});
            return keys;
        }
    }
})();
