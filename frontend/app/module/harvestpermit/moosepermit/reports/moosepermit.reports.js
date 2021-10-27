'use strict';

angular.module('app.moosepermit.reports', [])
    .service('LukeSpeciesFilter', function () {
        this.filter = function (years) {
            return _.chain(years)
                .map(function (year) {
                    var species = _.filter(year.species, function (species) {
                        return species.code === 47503 || species.code === 47629;
                    });
                    return {year: year.year, species: species};
                })
                .filter(function (year) {
                    return !!year;
                })
                .value();
        };
    })
    .constant('LukeTocMapping', {
        tocMapping: {
            'MOOSE_FIGURE': {
                '1': {name: 'Saaliskertymä'},
                '2': {name: 'Sonnien saaliskertymä'},
                '3': {name: 'Lehmien saaliskertymä'},
                '4': {name: 'Aikuisten saaliskertymä'},
                '5': {name: 'Vasojen saaliskertymä'},
                '6': {name: 'Urosten osuus aikuissaaliskertymästä'},
                '7': {name: 'Aikuisten osuus kokonaissaaliskertymästä'},
                '8': {name: 'Vasaosuus kokonaissaaliskertymästä'},
                '9': {name: 'Urosten osuus vasasaaliskertymästä'},
                '10': {name: 'Havaintoja per seuruepäivä'},
                '11': {name: 'Havaintoja per miestyöpäivä'},
                '12': {name: 'Saalista per seuruepäivä'},
                '13': {name: 'Saalista per miestyöpäivä'}
            },
            'MOOSE_TABLE_FULL': {
                'f1': {name: 'Metsästyspäivät'},
                'f2': {name: 'Metsästystavat'},
                'f3': {name: 'Hirvihavainnot'},
                'f4': {name: 'Hirvisaaliit'},
                'f5': {name: 'Teuraspainot'},
                'f6': {name: 'Kuntoluokitukset'},
                'f7': {name: 'Sarvityypit'},
                'f8': {name: 'Sarvipiikit'},
                's1': {name: 'Muiden sorkkaeläinten kanta-arviot'},
                's2': {name: 'Seuruekohtaiset tiheysindeksit'},
                's3': {name: 'Hirvikanta-arvio'}
            },
            'MOOSE_TABLE_COMPARISON': {
                '1': {name: 'Metsästyspäivät'},
                '2': {name: 'Metsästystavat'},
                '3': {name: 'Hirvihavainnot'},
                '4': {name: 'Hirvisaaliit'},
                '5': {name: 'Teuraspainot'},
                '6': {name: 'Kuntoluokitukset'},
                '7': {name: 'Sarvityypit'},
                '8': {name: 'Sarvipiikit'}
            },
            'MOOSE_MAP': {
                'h1': {name: 'Havainnot yhteensä'},
                'h2': {name: 'Sonnihavainnot'},
                'h3': {name: 'Vasattomien lehmien havainnot'},
                'h4': {name: 'Ykkösvasallisten lehmien havainnot'},
                'h5': {name: 'Kaksosvasallisten lehmien havainnot'},
                's1': {name: 'Saalis yhteensä'},
                's2': {name: 'Sonnisaalis'},
                's3': {name: 'Lehmäsaalis'},
                's4': {name: 'Vasasaalis'},
                'r1': {name: 'Aikuishavaintojen sonniosuus'},
                'r2': {name: 'Havaintojen vasaosuus'},
                'r3': {name: 'Havaintoja / seuruepäivä'},
                'r4': {name: 'Havaintoja / miestyöpäivä'},
                'r5': {name: 'Saalista / seuruepäivä'},
                'r6': {name: 'Saalista / miestyöpäivä'},
                'u1': {name: 'Ilmoitettu hirvitiheys'},
                'u2': {name: 'Ilmoitettu valkohäntäpeuratiheys'},
                'u3': {name: 'Ilmoitettu metsäkauristiheys'},
                'u4': {name: 'Ilmoitettu kuusipeuratiheys'},
                'u5': {name: 'Ilmoitettu villisikatiheys'}
            },
            'MOOSE_FORECAST': {
                'm1': {name: 'Ennuste'}
            },

            'WTD_PRE2020_FIGURE': {
                'wtd_S1': {name: 'Saaliskertymä'},
                'wtd_S2': {name: 'Aikuisten urosten saaliskertymä'},
                'wtd_S3': {name: 'Aikuisten naaraiden saaliskertymä'},
                'wtd_S4': {name: 'Vasojen saaliskertymä'},
                'wtd_S5': {name: 'Urosten osuus aikuissaaliskertymästä'},
                'wtd_S6': {name: 'Vasaosuus kokonaissaaliskertymästä'}
            },

            'WTD_PRE2020_TABLE_FULL': {
                'wtd_S1': {name: 'Saalistaulukko'},
            },

            /**
             * Luke reports for white tailed deer starting from hunting year 2020
             */

            'WTD_HARVEST_FIGURE': {
                '1': {name: 'Saaliskertymä'},
                '2': {name: 'Saalis uroksia'},
                '3': {name: 'Saalis naaraita'},
                '4': {name: 'Urosvasoja'},
                '5': {name: 'Naarasvasoja'},
                '6': {name: 'Urososuus aikuisista'},
                '7': {name: 'Vasaosuus'}
            },
            'WTD_HARVEST_MAP': {
                's1': {name: 'Saalis yhteensä'},
                's2': {name: 'Urossaalis'},
                's3': {name: 'Naarassaalis'},
                's4': {name: 'Vasasaalis'},
                's5': {name: 'Aikuissaalis'},
                'r1': {name: 'Aikuissaaliin urososuus'},
                'r2': {name: 'Saaliin vasaosuus'}
            },
            'WTD_HARVEST_TABLE_FULL': {
                '1': {name: 'Saalis vuosittain'},
                '2': {name: 'Saalis kuukausittain'},
                '3': {name: 'Saaliin metsästystapa vuosittain'},
                '4': {name: 'Saaliin metsästystapa kuukausittain'},
                '5': {name: 'Kauden saalis metsästystavan mukaan'}
            },
            'WTD_OBSERVATION_FIGURE': {
                '1': {name: 'Havaintokertymä yhteensä'},
                '2': {name: 'Nähdyt urokset'},
                '3': {name: 'Nähdyt naaraat'},
                '4': {name: 'Nähdyt vasat'},
                '5': {name: 'Nähdyt aikuiset'},
                '6': {name: 'Aikuishavaintojen urososuus'},
                '7': {name: 'Vasaosuus nähdyistä'}
            },
            'WTD_OBSERVATION_MAP': {
                'h1': {name: 'Havainnot yhteensä'},
                'h2': {name: 'Uroshavainnot'},
                'h3': {name: 'Naarashavainnot'},
                'h4': {name: 'Vasahavainnot'},
                'h5': {name: 'Aikuishavainnot'},
                'r1': {name: 'Aikuishavaintojen urososuus'},
                'r2': {name: 'Havaintojen vasaosuus'}
            },
            'WTD_OBSERVATION_TABLE_FULL': {
                '1': {name: 'Havainnot vuosittain'},
                '2': {name: 'Havainnot kuukausittain'},
                '3': {name: 'Vuosittaiset havainnot metsästystavoittain'},
                '4': {name: 'Kuukausittaiset havainnot metsästystavoittain'},
                '5': {name: 'Kauden havainnot metsästystavan mukaan'}
            },
            'WTD_HARVEST_ANTLER_FIGURE': {
                'hist_1': {name: 'Sarvipiikkien lukumäärä'},
                'hist_2': {name: 'Sarvien kärkiväli'},
                'hist_3': {name: 'Sarventyven ympärysmitta'},
                'hist_4': {name: 'Sarven pituus'},
                'hist_5': {name: 'Sarvien sisäleveys'}
            },
            'WTD_HARVEST_WEIGHT_FIGURE': {
                'hist_6': {name: 'Ruhopaino, aikuiset pukit'},
                'hist_7': {name: 'Ruhopaino, aikuiset naaraat'},
                'hist_8': {name: 'Ruhopaino, vasat'}
            }
        }
    })
    .service('LukeUrlService', function ($httpParamSerializer) {
        this.get = function (permitId, clubId, lukeOrg, lukePresentation, file, activeOccupationId) {
            var url = '/api/v1/moosepermit/' + permitId + '/luke-reports';

            return url + '?' + $httpParamSerializer({
                clubId: clubId,
                org: lukeOrg,
                presentation: lukePresentation,
                fileName: file,
                activeOccupationId: activeOccupationId
            });
        };
    })
    .config(function ($stateProvider) {
        $stateProvider
            .state('single_luke_report', {
                url: '/single-luke-report?clubId&permitId&orgName&presentationName&fileName',
                params: {
                    clubId: null,
                    permitId: null,
                    orgName: null,
                    presentationName: null,
                    file: null
                },
                templateUrl: 'harvestpermit/moosepermit/reports/single-luke-report.html',
                controller: 'MoosePermitSingleLukeReportController',
                controllerAs: '$ctrl',
                resolve: {
                    permitId: function ($stateParams) {
                        return $stateParams.permitId;
                    },
                    clubId: function ($stateParams) {
                        return $stateParams.clubId;
                    },
                    orgName: function ($stateParams) {
                        return $stateParams.orgName;
                    },
                    presentationName: function ($stateParams) {
                        return $stateParams.presentationName;
                    },
                    file: function ($stateParams) {
                        return $stateParams.fileName;
                    }
                }
            });
    })
    .controller('MoosePermitSingleLukeReportController', function ($scope, LukeUrlService, ActiveRoleService,
                                                                   permitId, clubId, orgName, presentationName, file) {
        var $ctrl = this;
        $ctrl.isPresentationTable = function () {
            return presentationName === 'MOOSE_TABLE_COMPARISON' || presentationName === 'MOOSE_TABLE_FULL' ||
                presentationName === 'WTD_OBSERVATION_TABLE_FULL' || presentationName === 'WTD_HARVEST_TABLE_FULL' ||
                presentationName === 'WTD_PRE2020_TABLE_FULL';
        };

        $ctrl.url = LukeUrlService.get(permitId, clubId, orgName, presentationName, file, ActiveRoleService.getActiveOccupationId());
    })
    .controller('MoosePermitReportsFilterController', function ($state, $stateParams, $q, $filter, $translate, $httpParamSerializer,
                                                                $timeout, ActiveRoleService,
                                                                LukeTocMapping,
                                                                MoosePermitListSelectedHuntingYearService,
                                                                MoosePermitSelection, LukeUrlService,
                                                                clubId, permits, permitId, selectedYearAndSpecies, huntingYears, lukeReportParams) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            if (!permitId || !_.some(permits, ['id', _.parseInt(permitId)])) {
                // Permit id not defined or not found in selected years/species, choose first in the list
                var firstPermit = _.first(permits);
                if (firstPermit) {
                    $state.go('.', {permitId: firstPermit.id}, {reloadOnSearch: false});
                }
            }
            $ctrl.permits = permits;
            $ctrl.selectedPermit = MoosePermitSelection;
            $ctrl.selectedYearAndSpecies = selectedYearAndSpecies;
            $ctrl.yearOptions = MoosePermitListSelectedHuntingYearService.createYearOptions(huntingYears);
            $ctrl.speciesOptions = MoosePermitListSelectedHuntingYearService.createSpeciesOptions(
                huntingYears, selectedYearAndSpecies.huntingYear);
            $ctrl.data = lukeReportParams ? lukeReportParams.params : null;

            // Three-level luke report selection
            $ctrl.org = _.first($ctrl.data);
            $ctrl.reportType = null;
            $ctrl.presentation = null;
            $ctrl.file = null;
            $ctrl.selectOrg($ctrl.org);
        };

        var i18NFilter = $filter('rI18nNameFilter');
        var relatedRhyText = $translate.instant('club.permit.relatedRhy');
        $ctrl.getPermitName = function (p) {
            return p.permitNumber
                + ' '
                + i18NFilter(p.permitHolder)
                + (p.currentlyViewedRhyIsRelated ? ' (' + relatedRhyText + ')' : '');
        };

        $ctrl.onHuntingYearOrSpeciesChange = function () {
            var updatedParam = $ctrl.selectedYearAndSpecies;
            $state.go('.', updatedParam, {reloadOnSearch: false});
        };

        $ctrl.onPermitChange = function () {
            var updatedParam = {permitId: $ctrl.selectedPermit.permitId};
            $state.go('.', updatedParam, {reloadOnSearch: false});
        };

        $ctrl.tocText = function (presentation, file) {
            if (LukeTocMapping.tocMapping[presentation.name]) {
                return LukeTocMapping.tocMapping[presentation.name][file].name || '';
            }
            return '';
        };

        $ctrl.isOrgSelected = function (org) {
            return $ctrl.org === org;
        };

        $ctrl.selectOrg = function (org) {
            $ctrl.org = org;
            if (org) {
                $ctrl.selectReportType(_.first(org.reportTypes));
            }
        };

        $ctrl.isReportTypeSelected = function (rt) {
            return $ctrl.reportType === rt;
        };

        $ctrl.selectReportType = function (rt) {
            $ctrl.reportType = rt;
            if (rt) {
                var presentation = _.first(rt.presentations);
                $ctrl.selectFile(presentation, _.first(presentation.files));
            }
        };

        $ctrl.selectFile = function (presentation, file) {
            $ctrl.presentation = presentation;
            $ctrl.file = file;
        };

        $ctrl.isFileSelected = function (p, f) {
            return $ctrl.presentation === p && $ctrl.file === f;
        };

        $ctrl.showFilesForSelectedOrg = function () {
            if (!_.isFinite($ctrl.selectedPermit.permitId) || _.isNil($ctrl.file)) {
                return false;
            }

            return $ctrl.org.name !== 'CLUB' || lukeReportParams.clubReportsExist;
        };

        $ctrl.isPresentationTable = function () {
            return $ctrl.presentation.name === 'MOOSE_TABLE_COMPARISON' || $ctrl.presentation.name === 'MOOSE_TABLE_FULL' ||
                $ctrl.presentation.name === 'WTD_OBSERVATION_TABLE_FULL' || $ctrl.presentation.name === 'WTD_HARVEST_TABLE_FULL' ||
                $ctrl.presentation.name === 'WTD_PRE2020_TABLE_FULL';
        };

        $ctrl.url = function (file) {
            return LukeUrlService.get($ctrl.selectedPermit.permitId, clubId, $ctrl.org.name, $ctrl.presentation.name,
                file, ActiveRoleService.getActiveOccupationId());
        };

        $ctrl.newTabUrl = function (file) {
            return '/#/single-luke-report?' + $httpParamSerializer({
                clubId: clubId,
                permitId: $ctrl.selectedPermit.permitId,
                fileName: file,
                orgName: $ctrl.org.name,
                presentationName: $ctrl.presentation.name
            });
        };
    });
