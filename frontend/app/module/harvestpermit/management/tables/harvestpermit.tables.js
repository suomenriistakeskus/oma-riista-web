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
                resolve: {
                    gameSpeciesCode: function ($stateParams) {
                        return _.parseInt($stateParams.gameSpeciesCode);
                    },
                    permit: function (MoosePermits, permitId, gameSpeciesCode) {
                        return MoosePermits.get({
                            permitId: permitId,
                            species: gameSpeciesCode
                        }).$promise;
                    },
                    gameSpeciesName: function (GameDiaryParameters, gameSpeciesCode) {
                        return GameDiaryParameters.query().$promise.then(function (parameters) {
                            return parameters.$getGameName(gameSpeciesCode);
                        });
                    },
                    observationSummary: function (MoosePermits, permitId, gameSpeciesCode) {
                        return MoosePermits.getObservationSummary({
                            permitId: permitId,
                            species: gameSpeciesCode
                        }).$promise;
                    }
                }
            });
    })
    .controller('MoosePermitTablesController', function ($state, $filter,
                                                         GameSpeciesCodes,
                                                         MoosePermitCounterService,
                                                         permit, gameSpeciesCode, gameSpeciesName,
                                                         observationSummary) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permit = permit;
            $ctrl.gameSpeciesName = gameSpeciesName;
            $ctrl.observationSummary = observationSummary;
            $ctrl.isMoose = GameSpeciesCodes.isMoose(gameSpeciesCode);
            $ctrl.counter = MoosePermitCounterService.create($ctrl.permit);
            $ctrl.canNavigateToClub = false;

            var i18NFilter = $filter('rI18nNameFilter');

            $ctrl.permit.partners = _.sortBy($ctrl.permit.partners, function (a) {
                return i18NFilter(a.huntingClubName);
            });
        };

        $ctrl.isCurrentClub = function (partner) {
            return false;
        };

        $ctrl.goBack = function () {
            $state.go('permitmanagement.dashboard', {
                permitId: permit.id,
                gameSpeciesCode: gameSpeciesCode
            }, {reload: true});
        };
    })
    .component('mooseHuntingTableObservationSummary', {
        templateUrl: 'harvestpermit/management/tables/observation-list.html',
        bindings: {
            observationSummary: '<',
            isMoose: '<'
        },
        controllerAs: '$ctrl'
    });
