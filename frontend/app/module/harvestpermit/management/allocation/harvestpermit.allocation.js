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
            $ctrl.moosePermit.partners = _.sortBy($ctrl.moosePermit.partners, function (a) {
                return i18n(a.huntingClubName);
            });
            $ctrl.counter = MoosePermitCounterService.create($ctrl.moosePermit);
            $ctrl.permitTotal = moosePermit.totalAmount;
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

        $ctrl.getMaxAdultMale = function (partner) {
            return _.floor($ctrl.permitUnallocated + partner.allocation.adultMales);
        };

        $ctrl.getMaxAdultFemale = function (partner) {
            return _.floor($ctrl.permitUnallocated + partner.allocation.adultFemales);
        };

        $ctrl.getMaxYoung = function (partner) {
            return _.floor($ctrl.permitUnallocated + partner.allocation.young);
        };

        $ctrl.countUsedPermitForPartner = function (partner) {
            var c = partner.harvestCount;
            return _.isObject(c) ? c.adultMales + c.adultFemales + 0.5 * (c.youngMales + c.youngFemales) : 0;
        };

        $ctrl.recalculate = function () {
            _.forEach($ctrl.moosePermit.partners, function (partner) {
                var allocation = partner.allocation;

                allocation.total = (allocation.adultMales || 0)
                    + (allocation.adultFemales || 0)
                    + (allocation.young || 0) / 2;
            });

            $ctrl.permitAllocated = _.sumBy($ctrl.moosePermit.partners, 'allocation.total');
            $ctrl.permitUnallocated = $ctrl.permitTotal - $ctrl.permitAllocated;
        };

        $ctrl.saveAllocations = function () {
            var allocations = _.map($ctrl.moosePermit.partners, 'allocation');

            return MoosePermits.updateAllocations({
                permitId: permitId,
                gameSpeciesCode: gameSpeciesCode

            }, allocations).$promise.then(function () {
                NotificationService.showDefaultSuccess();

                $ctrl.cancel();

            }, function () {
                NotificationService.showDefaultFailure();
            });
        };
    });
