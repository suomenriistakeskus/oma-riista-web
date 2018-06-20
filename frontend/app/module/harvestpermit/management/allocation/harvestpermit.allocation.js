'use strict';

angular.module('app.harvestpermit.management.allocation', [])
    .config(function ($stateProvider) {
        $stateProvider.state('permitmanagement.allocation', {
            url: '/allocations/{gameSpeciesCode:[0-9]{1,8}}',
            templateUrl: 'harvestpermit/management/allocation/permit-allocation.html',
            controller: 'MoosePermitAllocationController',
            controllerAs: '$ctrl',
            resolve: {
                gameSpeciesCode: function ($stateParams) {
                    return _.parseInt($stateParams.gameSpeciesCode);
                },
                gameSpeciesName: function (GameDiaryParameters, gameSpeciesCode) {
                    return GameDiaryParameters.query().$promise.then(function (parameters) {
                        return parameters.$getGameName(gameSpeciesCode);
                    });
                },
                moosePermit: function (MoosePermits, permitId, gameSpeciesCode) {
                    return MoosePermits.get({
                        permitId: permitId,
                        species: gameSpeciesCode
                    }).$promise;
                }
            }
        });
    })
    .controller('MoosePermitAllocationController', function ($scope, $state, $filter, NotificationService,
                                                             MoosePermits, MoosePermitCounterService,
                                                             permitId, moosePermit, gameSpeciesCode, gameSpeciesName) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            var i18n = $filter('rI18nNameFilter');
            $ctrl.moosePermit = moosePermit;
            $ctrl.gameSpeciesName = gameSpeciesName;
            $ctrl.allocations = _.sortBy(moosePermit.allocations, function (a) {
                return i18n(a.huntingClubName);
            });
            $ctrl.harvestCounts = moosePermit.harvestCounts;
            $ctrl.counter = MoosePermitCounterService.create($ctrl.moosePermit, $ctrl.allocations);
            $ctrl.permitTotal = moosePermit.speciesAmount.amount + _.sum(_.values(moosePermit.amendmentPermits));
            $ctrl.permitUnallocated = 0;
            $ctrl.permitAllocated = 0;
            $ctrl.recalculate();
            $scope.$broadcast('show-errors-check-validity');
        };

        $ctrl.cancel = function () {
            $state.go('permitmanagement.dashboard', {
                permitId: permitId,
                gameSpeciesCode: gameSpeciesCode
            });
        };

        $ctrl.floor = _.floor;

        $ctrl.countUsedPermitForPartner = function (clubId) {
            var c = $ctrl.harvestCounts[clubId];
            return _.isObject(c) ? c.adultMales + c.adultFemales + 0.5 * (c.youngMales + c.youngFemales) : 0;
        };

        $ctrl.recalculate = function () {
            _.each($ctrl.allocations, function (a) {
                a.total = (a.adultMales || 0) + (a.adultFemales || 0) + (a.young || 0) / 2;
            });

            $ctrl.permitAllocated = _.sum($ctrl.allocations, 'total');
            $ctrl.permitUnallocated = $ctrl.permitTotal - $ctrl.permitAllocated;
        };

        $ctrl.saveAllocations = function () {
            return MoosePermits.updateAllocations({
                permitId: permitId,
                gameSpeciesCode: gameSpeciesCode

            }, $ctrl.allocations).$promise.then(function () {
                NotificationService.showDefaultSuccess();

                $ctrl.cancel();

            }, function () {
                NotificationService.showDefaultFailure();
            });
        };
    });
