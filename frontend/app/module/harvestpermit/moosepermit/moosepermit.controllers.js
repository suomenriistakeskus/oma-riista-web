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
        $ctrl.edit = createStateTransition(initialState + '.edit');
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
    .controller('MoosePermitShowController', function ($scope, $filter, $state,
                                                       ClubPermits,
                                                       GameSpeciesCodes,
                                                       MoosePermitCounterService,
                                                       ActiveRoleService,
                                                       NotificationService,
                                                       permit, edit, todos, initialState, selectedYearAndSpecies) {

        $scope.permit = permit;
        $scope.todos = todos;

        var updateAllocations = function (p) {
            var i18NFilter = $filter('rI18nNameFilter');
            $scope.allocations = _.sortBy(p.allocations, function (a) {
                return i18NFilter(a.huntingClubName);
            });
        };
        updateAllocations($scope.permit);

        var spa = $scope.permit.speciesAmount;
        $scope.amendmentPermitNumbers = _.keys($scope.permit.amendmentPermits);
        var amendmentPermitAmount = _.sum(_.values($scope.permit.amendmentPermits));
        $scope.permitTotal = spa.amount + amendmentPermitAmount;

        $scope.isCurrentClub = function (allocation) {
            return allocation.huntingClubId === permit.viewedClubId;
        };

        $scope.isMoose = function () {
            return GameSpeciesCodes.isMoose($scope.permit.speciesAmount.gameSpecies.code);
        };

        $scope.canNavigateToClub = ActiveRoleService.isModerator;

        $scope.navigateToClub = function (row) {
            $state.go('club.permit.show', {
                id: row.huntingClubId,
                permitId: $scope.permit.id,
                huntingYear: selectedYearAndSpecies.huntingYear,
                species: selectedYearAndSpecies.species
            });
        };

        // calculate

        $scope.floor = _.floor;

        $scope.recalculate = function () {
            _.each($scope.allocations, function (a) {
                a.total = (a.adultMales || 0) + (a.adultFemales || 0) + (a.young || 0) / 2;
            });
            var aSum = _.sum($scope.allocations, 'total');
            $scope.permitUnallocated = $scope.permitTotal - aSum;
            $scope.permitAllocated = aSum;
        };
        $scope.recalculate();

        $scope.countHarvestsBy = MoosePermitCounterService.createCountHarvestsBy($scope.permit);

        $scope.countAllocatedBy = function (key) {
            return _.sum($scope.allocations, key);
        };

        var countMalePercentage = function (func) {
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

        $scope.malePercentage = function () {
            return countMalePercentage($scope.countAllocatedBy);
        };

        $scope.youngPercentage = function () {
            return countYoungPercentage($scope.countAllocatedBy);
        };

        $scope.maleHarvestPercentage = function () {
            return countMalePercentage($scope.countHarvestsBy);
        };

        $scope.youngHarvestPercentage = function () {
            return countYoungPercentage($scope.countHarvestsBy);
        };

        $scope.countSummaryForPartnersTable = function (key) {
            return _.sum($scope.permit.summaryForPartnersTable, key);
        };

        $scope.canEditAllocations = _.constant($scope.permit.canEditAllocations);
        $scope.isEditingAllocations = _.constant(edit);
        $scope.startEditAllocations = function () {
            $state.go(initialState + '.edit', {permitId: $scope.permit.id});
        };
        $scope.saveAllocations = function () {
            var params = {
                permitId: $scope.permit.id,
                gameSpeciesCode: $scope.permit.speciesAmount.gameSpecies.code
            };
            var data = angular.copy($scope.permit.allocations);

            return ClubPermits.updateAllocations(params, data).$promise
                .then(function () {
                        NotificationService.showDefaultSuccess();
                        $state.go(initialState + '.show', {permitId: $scope.permit.id}, {reload: true});
                    },
                    NotificationService.showDefaultFailure
                );
        };
        $scope.cancelAllocations = function () {
            $state.go(initialState + '.show', {permitId: $scope.permit.id});
        };
    })

    .controller('MoosePermitLeadersController', function ($scope, leaders) {
        $scope.leaders = leaders;

        var previousClub = null;

        // Hide repetitive club names
        for (var i = 0; i < leaders.length; i++) {
            var club = leaders[i].club;
            if (previousClub && angular.equals(previousClub, club)) {
                leaders[i].club = null;
            }
            previousClub = club;
        }

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    })

    .controller('MoosePermitLukeReportsController', function ($scope, LukeUrlService,
                                                              lukeReportParams, permitId, clubId) {

        $scope.data = lukeReportParams.params;

        $scope.uiState = {
            org: $scope.data[0],
            presentation: $scope.data[0].presentations[0]
        };

        $scope.isOrgSelected = function (org) {
            return $scope.uiState.org === org;
        };

        $scope.selectOrg = function (org) {
            $scope.uiState.org = org;
            $scope.uiState.presentation = org.presentations[0];
        };

        $scope.isPresentationSelected = function (p) {
            return $scope.uiState.presentation === p;
        };

        $scope.selectPresentation = function (p) {
            $scope.uiState.presentation = p;
        };

        $scope.isPresentationTable = function () {
            return $scope.uiState.presentation.name === 'TABLE_COMPARISON' || $scope.uiState.presentation.name === 'TABLE_FULL';
        };

        $scope.url = function (file) {
            return LukeUrlService.get(permitId, clubId, $scope.uiState.org.name, $scope.uiState.presentation.name, file);
        };

        $scope.showFilesForSelectedOrg = function () {
            return $scope.uiState.org.name !== 'CLUB' || lukeReportParams.clubReportsExist;
        };

        $scope.noFilesForSelectedClub = function () {
            return !$scope.showFilesForSelectedOrg();
        };
    })
;
