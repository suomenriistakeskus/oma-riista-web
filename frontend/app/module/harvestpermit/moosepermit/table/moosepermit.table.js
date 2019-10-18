'use strict';

angular.module('app.moosepermit.table', [])

    .service('MoosePermitCounterService', function () {
        var countHarvestsBy = function (permit, key) {
            if (key === 'adult') {
                return _.sumBy(permit.partners, 'harvestCount.adultMales') +
                    _.sumBy(permit.partners, 'harvestCount.adultFemales');
            }
            if (key === 'young') {
                return _.sumBy(permit.partners, 'harvestCount.youngMales') +
                    _.sumBy(permit.partners, 'harvestCount.youngFemales');
            }
            return _.sumBy(permit.partners, 'harvestCount.' + key);
        };

        var countAllocateBy = function (permit, key) {
            return _.sumBy(permit.partners, 'allocation.' + key);
        };

        var countSummaryForPartnersTable = function (permit, key) {
            return _.sumBy(permit.partners, 'summary.' + key);
        };

        var countMaleAdultPercentage = function (func) {
            var m = func('adultMales');
            var f = func('adultFemales');
            return _.round(100 * m / (m + f)) || 0;
        };

        var countYoungPercentage = function (func) {
            var m = func('adultMales');
            var f = func('adultFemales');
            var y = func('young');
            return _.round(100 * y / (m + f + y)) || 0;
        };

        this.create = function (permit) {
            var harvestsBy = _.partial(countHarvestsBy, permit);
            var allocatedBy = _.partial(countAllocateBy, permit);

            return {
                harvestsBy: harvestsBy,
                allocatedBy: allocatedBy,
                maleAdultPercentage: _.partial(countMaleAdultPercentage, allocatedBy),
                youngPercentage: _.partial(countYoungPercentage, allocatedBy),
                maleAdultHarvestPercentage: _.partial(countMaleAdultPercentage, harvestsBy),
                youngHarvestPercentage: _.partial(countYoungPercentage, harvestsBy),
                totalRemainingPopulationInTotalArea: function () {
                    return countSummaryForPartnersTable(permit, 'remainingPopulationInTotalArea');
                },
                totalRemainingPopulationInEffectiveArea: function () {
                    return countSummaryForPartnersTable(permit, 'remainingPopulationInEffectiveArea');
                }
            };
        };
    })

    .component('moosePermitTodo', {
        bindings: {
            todo: '<'
        },
        template: '<span ng-if="::$ctrl.showTodo"' +
            ' uib-tooltip="{{$ctrl.tooltipTxt}}"' +
            ' tooltip-placement="right"' +
            ' tooltip-popup-delay="0"' +
            ' class="text-danger fa fa-exclamation-triangle">&nbsp;</span>',
        controller: function ($translate) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.tooltipTxt = getTooltipText($ctrl.todo);
                $ctrl.showTodo = !!$ctrl.tooltipTxt;
            };

            function getTooltipText(todo) {
                if (!todo || !todo.todo) {
                    return null;
                }

                var todoMessages = _([
                    todo.areaMissing ? 'club.permit.todo.area' : null,
                    todo.groupMissing ? 'club.permit.todo.group' : null,
                    todo.groupPermitMissing ? 'club.permit.todo.groupPermit' : null,
                    todo.groupLeaderMissing ? 'club.permit.todo.groupLeader' : null
                ]).filter().map($translate.instant).join(', ');

                return $translate.instant('club.permit.todo.prefix') + ' ' + todoMessages;
            }
        }
    })

    .component('moosePermitTableHunting', {
        templateUrl: 'harvestpermit/moosepermit/table/show-table-hunting.html',
        bindings: {
            partners: '<',
            latestUpdate: '<',
            counter: '<',
            todo: '<',
            canNavigateToClub: '<',
            isCurrentClub: '&',
            navigateToClub: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.getHuntingEndDate = function (partner) {
                return partner.summary.huntingFinished ? partner.summary.huntingEndDate : null;
            };

            $ctrl.getTotalUsedPermits = function () {
                return $ctrl.counter.harvestsBy('adult') + 0.5 * $ctrl.counter.harvestsBy('young');
            };

            $ctrl.getUsedPermits = function (partner) {
                var h = partner.harvestCount;
                return h.adultMales + h.adultFemales + 0.5 * (h.youngMales + h.youngFemales);
            };
        }
    })

    .component('moosePermitTableIndexes', {
        templateUrl: 'harvestpermit/moosepermit/table/show-table-indexes.html',
        bindings: {
            partners: '<',
            totalStatistics: '<',
            todo: '<',
            canNavigateToClub: '<',
            isCurrentClub: '&',
            navigateToClub: '&'
        }
    })

    .component('moosePermitTablePayments', {
        templateUrl: 'harvestpermit/moosepermit/table/show-table-payments.html',
        bindings: {
            partners: '<',
            counter: '<',
            totalPayment: '<',
            amendmentPermitsMatchHarvests: '<',
            todo: '<',
            canNavigateToClub: '<',
            isCurrentClub: '&',
            navigateToClub: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.showTotalPaymentAmount = function (partner) {
                return $ctrl.amendmentPermitsMatchHarvests ||
                    partner.harvestCount.adultsNotEdible === 0 && partner.harvestCount.youngsNotEdible === 0;
            };

            $ctrl.showAmendmentPermitDoNotMatchWarning = function (partner) {
                return !$ctrl.amendmentPermitsMatchHarvests && (
                    partner.harvestCount.adultsNotEdible > 0 || partner.harvestCount.youngsNotEdible > 0
                );
            };
        }
    })

    .component('moosePermitTableAreas', {
        templateUrl: 'harvestpermit/moosepermit/table/show-table-areas.html',
        bindings: {
            partners: '<',
            todo: '<',
            canNavigateToClub: '<',
            isCurrentClub: '&',
            navigateToClub: '&'
        }
    });
