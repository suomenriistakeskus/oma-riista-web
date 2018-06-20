'use strict';

angular.module('app.moosepermit.controllers', [])
    .service('MoosePermitListSelectedHuntingYearService', function (HuntingYearService, SpeciesSortByName) {
        var self = this;

        this.resolve = function ($stateParams, huntingYearAndSpecies) {
            var selectedYearAndSpecies = parseStateParameters($stateParams);
            return self.resolveSelectedToStateParameters(huntingYearAndSpecies, selectedYearAndSpecies);
        };

        function parseStateParameters($stateParams) {
            var parseNumber = function (s) {
                var value = _.parseInt(s);
                return _.isFinite(value) ? value : null;
            };

            return {huntingYear: parseNumber($stateParams.huntingYear), species: parseNumber($stateParams.species)};
        }

        this.resolveSelectedToStateParameters = function (huntingYearAndSpecies, selectedYearAndSpecies) {
            var huntingYear = resolveHuntingYear(huntingYearAndSpecies, selectedYearAndSpecies.huntingYear);
            var species = resolveSpeciesCode(huntingYearAndSpecies, selectedYearAndSpecies.species, huntingYear);
            return {huntingYear: huntingYear, species: species};
        };

        this.createYearOptions = function (huntingYears) {
            return _(huntingYears)
                .map('year')
                .map(HuntingYearService.toObj)
                .value();
        };

        this.createSpeciesOptions = function (huntingYears, selectedHuntingYear) {
            var yearAndSpecies = _.find(huntingYears, 'year', selectedHuntingYear);
            var species = _.get(yearAndSpecies, 'species', []);
            return SpeciesSortByName.sort(species);
        };

        function resolveHuntingYear(huntingYearAndSpecies, selectedHuntingYear) {
            return resolveOptionOrDefault(huntingYearAndSpecies, 'year', selectedHuntingYear, HuntingYearService.getCurrent());
        }

        function resolveSpeciesCode(huntingYearAndSpecies, selectedSpeciesCode, selectedHuntingYear) {
            var speciesOptions = self.createSpeciesOptions(huntingYearAndSpecies, selectedHuntingYear);
            return resolveOptionOrDefault(speciesOptions, 'code', selectedSpeciesCode, null);
        }

        function resolveOptionOrDefault(options, keyName, selectedValue, defaultValue) {
            if (selectedValue && _.some(options, keyName, selectedValue)) {
                return selectedValue;
            }
            if (defaultValue && _.some(options, keyName, defaultValue)) {
                return defaultValue;
            }
            return options.length > 0 ? _.get(options[0], keyName) : defaultValue;
        }
    })
    .controller('MoosePermitListController', function ($state, $stateParams, HuntingYearService,
                                                       MoosePermitLeadersService, MoosePermitSelection,
                                                       MoosePermitListSelectedHuntingYearService,
                                                       huntingYears, initialState, permits, selectedYearAndSpecies) {
        var $ctrl = this;

        $ctrl.permits = permits;
        $ctrl.selectedYearAndSpecies = selectedYearAndSpecies;
        $ctrl.yearOptions = MoosePermitListSelectedHuntingYearService.createYearOptions(huntingYears);
        $ctrl.speciesOptions = MoosePermitListSelectedHuntingYearService.createSpeciesOptions(
            huntingYears, selectedYearAndSpecies.huntingYear);

        $ctrl.reloadState = function () {
            var params = MoosePermitListSelectedHuntingYearService.resolveSelectedToStateParameters(
                huntingYears, $ctrl.selectedYearAndSpecies);

            // Go to initial state instead of reloading current, because currently selected permit might not have selected species
            $state.go(initialState, params, {reload: true});
        };

        $ctrl.isSelectedPermit = function (permit) {
            return permit && permit.id === MoosePermitSelection.permitId;
        };

        $ctrl.getSelectedPermit = function () {
            return _.find($ctrl.permits, 'id', MoosePermitSelection.permitId);
        };

        $ctrl.show = createStateTransition(initialState + '.show');
        $ctrl.map = createStateTransition(initialState + '.map');
        $ctrl.lukereports = createStateTransition(initialState + '.lukereports');
        $ctrl.rhystats = createStateTransition(initialState + '.rhystats');

        $ctrl.leaders = function () {
            MoosePermitLeadersService.showLeaders({
                id: MoosePermitSelection.permitId,
                huntingYear: $ctrl.selectedYearAndSpecies.huntingYear,
                gameSpeciesCode: $ctrl.selectedYearAndSpecies.species
            });
        };

        function createStateTransition(to) {
            return function (params) {
                $state.go(to, {permitId: params.permit.id});
            };
        }

        function getInitialPermitId() {
            if ($ctrl.getSelectedPermit()) {
                return MoosePermitSelection.permitId;
            } else if (permits.length > 0) {
                return permits[0].id;
            }
            return null;
        }

        var currentState = $state.current.name;
        var currentStateIsInitial = currentState === initialState;

        if (!$ctrl.getSelectedPermit() || currentStateIsInitial) {
            var targetState = currentStateIsInitial ? initialState + '.show' : currentState;
            var initialPermitId = getInitialPermitId();

            if (initialPermitId) {
                $state.go(targetState, {permitId: initialPermitId}, {reload: true});
            }
        }
    })
    .controller('MoosePermitShowController', function ($filter, $state,
                                                       NotificationService, GameSpeciesCodes, ActiveRoleService,
                                                       MoosePermits, MoosePermitCounterService,
                                                       permit, todos, initialState, selectedYearAndSpecies) {

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

            $ctrl.isCurrentClub = function (allocation) {
                return allocation.huntingClubId === permit.viewedClubId;
            };

            $ctrl.canNavigateToClub = ActiveRoleService.isModerator;

            $ctrl.navigateToClub = function (row) {
                $state.go('club.permit.show', {
                    id: row.huntingClubId,
                    permitId: $ctrl.permit.id,
                    huntingYear: selectedYearAndSpecies.huntingYear,
                    species: selectedYearAndSpecies.species
                });
            };
        };
    })
    .component('moosePermitTableHunting', {
        templateUrl: 'harvestpermit/moosepermit/show-table-hunting.html',
        bindings: {
            latestUpdate: '<',
            latestUpdatesByClubId: '<',
            fromMooseDataCard: '<',
            stats: '<',
            harvestCounts: '<',
            allocations: '<',
            summaryForPartnersTable: '<',
            counter: '<',
            todos: '<',
            isCurrentClub: '&',
            canNavigateToClub: '<',
            navigateToClubFn: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.navigateToClub = function (c) {
                    $ctrl.navigateToClubFn()(c);
                };
            };
        }
    })
    .component('moosePermitTableIndexes', {
        templateUrl: 'harvestpermit/moosepermit/show-table-indexes.html',
        bindings: {
            totalStats: '<',
            stats: '<',
            fromMooseDataCard: '<',
            allocations: '<',
            todos: '<',
            isCurrentClub: '&',
            canNavigateToClub: '<',
            navigateToClubFn: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.navigateToClub = function (c) {
                    $ctrl.navigateToClubFn()(c);
                };
            };
        }
    })
    .component('moosePermitTablePayments', {
        templateUrl: 'harvestpermit/moosepermit/show-table-payments.html',
        bindings: {
            counter: '<',
            totalPayment: '<',
            amendmentPermitsMatchHarvests: '<',
            harvestCounts: '<',
            payments: '<',
            fromMooseDataCard: '<',
            allocations: '<',
            todos: '<',
            isCurrentClub: '&',
            canNavigateToClub: '<',
            navigateToClubFn: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.navigateToClub = function (c) {
                    $ctrl.navigateToClubFn()(c);
                };
            };
        }
    })
;
