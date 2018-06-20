'use strict';

angular.module('app.harvestpermit.management.tables', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('permitmanagement.tables', {
                url: '/tables?gameSpeciesCode',
                templateUrl: 'harvestpermit/management/tables/permit-tables.html',
                controller: 'MoosePermitTablesController',
                controllerAs: '$ctrl',
                bindToController: true,
                params: {
                    gameSpeciesCode: null
                },
                resolve: {
                    permit: function (MoosePermits, permitId, $stateParams) {
                        return MoosePermits.get({
                            permitId: permitId,
                            species: $stateParams.gameSpeciesCode
                        }).$promise;
                    },
                    todos: function (MoosePermits, permitId, $stateParams) {
                        return MoosePermits.listTodos({
                            permitId: permitId,
                            speciesCode: $stateParams.gameSpeciesCode
                        }).$promise;
                    }
                }
            });
    })
    .controller('MoosePermitTablesController', function ($filter,
                                                         GameSpeciesCodes,
                                                         MoosePermitCounterService,
                                                         permit, todos) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permit = permit;
            $ctrl.todos = todos;

            $ctrl.latestUpdatesByClubId = _.transform($ctrl.permit.statistics, function (result, val, key) {
                result[key] = val.latestUpdate;
            });

            $ctrl.fromMooseDataCard = _.transform($ctrl.permit.summaryForPartnersTable, function (result, val, key) {
                result[key] = val.fromMooseDataCard;
            });

            var i18NFilter = $filter('rI18nNameFilter');
            $ctrl.allocations = _.sortBy($ctrl.permit.allocations, function (a) {
                return i18NFilter(a.huntingClubName);
            });

            $ctrl.counter = MoosePermitCounterService.create($ctrl.permit, $ctrl.allocations);

            $ctrl.isMoose = function () {
                return GameSpeciesCodes.isMoose($ctrl.permit.speciesAmount.gameSpecies.code);
            };
        };
    });
