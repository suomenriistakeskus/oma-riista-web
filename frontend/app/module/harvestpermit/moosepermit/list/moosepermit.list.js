'use strict';

angular.module('app.moosepermit.list', [])

    .service('MoosePermitSelection', function () {
        var self = this;

        self.permitId = null;

        this.updateSelectedPermitId = function (stateParams) {
            self.permitId = _.parseInt(stateParams.permitId);
            return self.permitId;
        };
    })

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
            var yearAndSpecies = _.find(huntingYears, {year: selectedHuntingYear});
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
            if (selectedValue && _.some(options, [keyName, selectedValue])) {
                return selectedValue;
            }
            if (defaultValue && _.some(options, [keyName, defaultValue])) {
                return defaultValue;
            }

            return options.length > 0 ? _.get(options[0], keyName) : null;
        }
    })

    .controller('MoosePermitListController', function ($state, $stateParams, $translate, $filter, HuntingYearService,
                                                       MoosePermitLeadersService, MoosePermitSelection,
                                                       MoosePermitListSelectedHuntingYearService,
                                                       huntingYears, stateBase, permits, selectedYearAndSpecies) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permits = permits;
            $ctrl.selectedPermit = MoosePermitSelection;
            $ctrl.selectedYearAndSpecies = selectedYearAndSpecies;
            $ctrl.yearOptions = MoosePermitListSelectedHuntingYearService.createYearOptions(huntingYears);
            $ctrl.speciesOptions = MoosePermitListSelectedHuntingYearService.createSpeciesOptions(
                huntingYears, selectedYearAndSpecies.huntingYear);

            function createStateTransition(to) {
                return function () {
                    navigateToState(MoosePermitSelection.permitId, to, false);
                };
            }

            $ctrl.table = createStateTransition(stateBase + '.moosepermit.table');
            $ctrl.map = createStateTransition(stateBase + '.moosepermit.map');
            $ctrl.lukereports = createStateTransition(stateBase + '.reports');
            $ctrl.rhystats = createStateTransition(stateBase + '.moosepermit.rhystats');

            if ($state.current.name === stateBase + '.moosepermit') {
                var defaultState = stateBase + '.moosepermit.table';
                var selectedPermitId = MoosePermitSelection.permitId;

                if (selectedPermitId && _.some($ctrl.permits, ['id', selectedPermitId])) {
                    navigateToState(selectedPermitId, defaultState, false);

                } else if (permits.length > 0) {
                    navigateToState(permits[0].id, defaultState, false);

                } else {
                    MoosePermitSelection.permitId = null;
                }
            }
        };

        $ctrl.onHuntingYearOrSpeciesChange = function () {
            // Go to initial state instead of reloading current, because currently selected permit might not have selected species
            navigateToState(null, stateBase + '.moosepermit', true);
        };

        var i18NFilter = $filter('rI18nNameFilter');
        var relatedRhyText = $translate.instant('club.permit.relatedRhy');

        $ctrl.getPermitName = function (p) {
            return p.permitNumber
                + ' '
                + i18NFilter(p.permitHolder)
                + (p.currentlyViewedRhyIsRelated ? ' (' + relatedRhyText + ')' : '');
        };

        $ctrl.getSelectedPermit = function () {
            return _.find($ctrl.permits, {id: MoosePermitSelection.permitId});
        };

        $ctrl.isSelectedPermit = function (permit) {
            return permit && permit.id === MoosePermitSelection.permitId;
        };

        $ctrl.leaders = function () {
            MoosePermitLeadersService.showLeaders({
                id: MoosePermitSelection.permitId,
                huntingYear: $ctrl.selectedYearAndSpecies.huntingYear,
                gameSpeciesCode: $ctrl.selectedYearAndSpecies.species
            });
        };

        function navigateToState(permitId, stateName, reloadState) {
            var stateParams = MoosePermitListSelectedHuntingYearService.resolveSelectedToStateParameters(
                huntingYears, $ctrl.selectedYearAndSpecies);

            if (permitId) {
                stateParams.permitId = permitId;
            }

            $state.go(stateName, stateParams, {reload: reloadState});
        }
    })

    .component('moosePermitListDetails', {
        templateUrl: 'harvestpermit/moosepermit/list/details.html',
        bindings: {
            table: '&',
            map: '&',
            lukereports: '&',
            leaders: '&',
            rhystats: '&',
            selectedPermit: '<'
        },
        controller: function ($state, $translate, $stateParams, ActiveRoleService, DeerHuntingSummaryService,
                              GameSpeciesCodes, MooseHuntingSummaryService,
                              MapPdfModal, HarvestPermitPdfUrl,
                              MoosePermitPartnerDownloadModal) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.isModerator = ActiveRoleService.isModerator();
            };

            $ctrl.$onChanges = function (changes) {
                if (changes.selectedPermit) {
                    var permit = changes.selectedPermit.currentValue;
                    $ctrl.isMoosePermit = permit && GameSpeciesCodes.isMoose(permit.gameSpeciesCode);
                    $ctrl.isMooseWhiteTailedDeerPermit = permit && GameSpeciesCodes.isWhiteTailedDeer(permit.gameSpeciesCode);
                    $ctrl.huntingYear = $stateParams.huntingYear;
                }
            };

            $ctrl.isActive = function (stateName) {
                return _.endsWith($state.current.name, stateName);
            };

            $ctrl.editHuntingSummary = function () {
                var spa = $ctrl.selectedPermit.speciesAmount;
                var clubId = $ctrl.selectedPermit.viewedClubId;

                if ($ctrl.isMoosePermit) {
                    MooseHuntingSummaryService.editHuntingSummary(clubId, $ctrl.selectedPermit.id, spa).finally(function () {
                        $state.reload();
                    });
                } else {
                    DeerHuntingSummaryService.editHuntingSummary(clubId, spa).finally(function () {
                        $state.reload();
                    });
                }
            };

            $ctrl.openDownloadModal = function () {
                MoosePermitPartnerDownloadModal.showModal($ctrl.selectedPermit);
            };

            $ctrl.printApplicationArea = function () {
                MapPdfModal.printArea('/api/v1/moosepermit/' + $ctrl.selectedPermit.id + '/application-map');
            };
        }
    })

    .controller('MoosePermitTableController', function ($filter, $state,
                                                        NotificationService, GameSpeciesCodes, ActiveRoleService,
                                                        MoosePermits, MoosePermitCounterService,
                                                        permit, selectedYearAndSpecies) {

        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permit = permit;
            $ctrl.counter = MoosePermitCounterService.create($ctrl.permit);
            $ctrl.isMoose = GameSpeciesCodes.isMoose($ctrl.permit.gameSpeciesCode);

            var i18NFilter = $filter('rI18nNameFilter');

            $ctrl.permit.partners = _.sortBy($ctrl.permit.partners, function (partner) {
                return i18NFilter(partner.huntingClubName);
            });

            $ctrl.canNavigateToClub = ActiveRoleService.isModerator();

            $ctrl.isCurrentClub = function (partner) {
                return partner.huntingClubId === permit.viewedClubId;
            };

            $ctrl.navigateToClub = function (partner) {
                $state.go('club.moosepermit.table', {
                    id: partner.huntingClubId,
                    permitId: $ctrl.permit.id,
                    huntingYear: selectedYearAndSpecies.huntingYear,
                    species: selectedYearAndSpecies.species
                });
            };
        };
    });
