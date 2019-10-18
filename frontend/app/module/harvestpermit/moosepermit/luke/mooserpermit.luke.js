'use strict';

angular.module('app.moosepermit.luke', [])
    .service('LukeUrlService', function ($httpParamSerializer) {
        this.get = function (permitId, clubId, lukeOrg, lukePresentation, file) {
            var url = '/api/v1/moosepermit/' + permitId + '/luke-reports';

            return url + '?' + $httpParamSerializer({
                clubId: clubId,
                org: lukeOrg,
                presentation: lukePresentation,
                fileName: file
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
                templateUrl: 'harvestpermit/moosepermit/luke/single-luke-report.html',
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
    .controller('MoosePermitSingleLukeReportController', function ($scope, LukeUrlService,
                                                                   permitId, clubId, orgName, presentationName, file) {
        var $ctrl = this;
        $ctrl.isPresentationTable = function () {
            return presentationName === 'TABLE_COMPARISON' || presentationName === 'TABLE_FULL';
        };

        $ctrl.url = LukeUrlService.get(permitId, clubId, orgName, presentationName, file);
    })
    .controller('MoosePermitLukeReportsController', function ($scope, $timeout, $httpParamSerializer, LukeUrlService,
                                                              lukeReportParams, permitId, clubId) {

        $scope.data = lukeReportParams.params;

        $scope.scrollToIndex = null;
        var tocMapping = {
            'FIGURE': {
                '1': {id: 1, name: 'Saaliskertymä'},
                '2': {id: 2, name: 'Sonnien saaliskertymä'},
                '3': {id: 3, name: 'Lehmien saaliskertymä'},
                '4': {id: 4, name: 'Aikuisten saaliskertymä'},
                '5': {id: 5, name: 'Vasojen saaliskertymä'},
                '6': {id: 6, name: 'Urosten osuus aikuissaaliskertymästä'},
                '7': {id: 7, name: 'Aikuisten osuus kokonaissaaliskertymästä'},
                '8': {id: 8, name: 'Vasaosuus kokonaissaaliskertymästä'},
                '9': {id: 9, name: 'Urosten osuus vasasaaliskertymästä'},
                '10': {id: 10, name: 'Havaintoja per seuruepäivä'},
                '11': {id: 11, name: 'Havaintoja per miestyöpäivä'},
                '12': {id: 12, name: 'Saalista per seuruepäivä'},
                '13': {id: 13, name: 'Saalista per miestyöpäivä'}
            },
            'TABLE_FULL': {
                'f1': {id: 1, name: 'Metsästyspäivät'},
                'f2': {id: 2, name: 'Metsästystavat'},
                'f3': {id: 3, name: 'Hirvihavainnot'},
                'f4': {id: 4, name: 'Hirvisaaliit'},
                'f5': {id: 5, name: 'Teuraspainot'},
                'f6': {id: 6, name: 'Kuntoluokitukset'},
                'f7': {id: 7, name: 'Sarvityypit'},
                'f8': {id: 8, name: 'Sarvipiikit'},
                's1': {id: 9, name: 'Muiden sorkkaeläinten kanta-arviot'},
                's2': {id: 10, name: 'Seuruekohtaiset tiheysindeksit'},
                's3': {id: 11, name: 'Hirvikanta-arvio'}
            },
            'TABLE_COMPARISON': {
                '1': {id: 1, name: 'Metsästyspäivät'},
                '2': {id: 2, name: 'Metsästystavat'},
                '3': {id: 3, name: 'Hirvihavainnot'},
                '4': {id: 4, name: 'Hirvisaaliit'},
                '5': {id: 5, name: 'Teuraspainot'},
                '6': {id: 6, name: 'Kuntoluokitukset'},
                '7': {id: 7, name: 'Sarvityypit'},
                '8': {id: 8, name: 'Sarvipiikit'}
            },
            'MAP': {
                'h1': {id: 1, name: 'Havainnot yhteensä'},
                'h2': {id: 2, name: 'Sonnihavainnot'},
                'h3': {id: 3, name: 'Vasattomien lehmien havainnot'},
                'h4': {id: 4, name: 'Ykkösvasallisten lehmien havainnot'},
                'h5': {id: 5, name: 'Kaksosvasallisten lehmien havainnot'},
                's1': {id: 6, name: 'Saalis yhteensä'},
                's2': {id: 7, name: 'Sonnisaalis'},
                's3': {id: 8, name: 'Lehmäsaalis'},
                's4': {id: 9, name: 'Vasasaalis'},
                'r1': {id: 10, name: 'Aikuishavaintojen sonniosuus'},
                'r2': {id: 11, name: 'Havaintojen vasaosuus'},
                'r3': {id: 12, name: 'Havaintoja / seuruepäivä'},
                'r4': {id: 13, name: 'Havaintoja / miestyöpäivä'},
                'r5': {id: 14, name: 'Saalista / seuruepäivä'},
                'r6': {id: 15, name: 'Saalista / miestyöpäivä'},
                'u1': {id: 16, name: 'Ilmoitettu hirvitiheys'},
                'u2': {id: 17, name: 'Ilmoitettu valkohäntäpeuratiheys'},
                'u3': {id: 18, name: 'Ilmoitettu metsäkauristiheys'},
                'u4': {id: 19, name: 'Ilmoitettu kuusipeuratiheys'},
                'u5': {id: 20, name: 'Ilmoitettu villisikatiheys'}
            },
            'FORECAST': {
                'm1': {id: 1, name: 'Ennuste'}
            }
        };

        function findTocMapping(file) {
            var presentation = $scope.uiState.presentation.name;
            if (tocMapping[presentation]) {
                return tocMapping[presentation][file] || {};
            }
            return {};
        }

        $scope.tocText = function (file) {
            return findTocMapping(file).name;
        };

        $scope.getScrollId = function (file) {
            return findTocMapping(file).id;
        };

        function doScroll(index) {
            // could set scrollToIndex directly to correct value, but then clicking same item again would not scroll
            $scope.scrollToIndex = index === 0 ? 1 : 0;
            $timeout(function () {
                $scope.scrollToIndex = index;
            });
        }

        $scope.scrollTo = function (file) {
            doScroll(findTocMapping(file).id);
        };

        $scope.scrollToTop = function () {
            doScroll(0);
        };

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
        $scope.newTabUrl = function (file) {
            return '/#/single-luke-report?' + $httpParamSerializer({
                clubId: clubId,
                permitId: permitId,
                fileName: file,
                orgName: $scope.uiState.org.name,
                presentationName: $scope.uiState.presentation.name
            });
        };

        $scope.showFilesForSelectedOrg = function () {
            return $scope.uiState.org.name !== 'CLUB' || lukeReportParams.clubReportsExist;
        };

        $scope.noFilesForSelectedClub = function () {
            return !$scope.showFilesForSelectedOrg();
        };
    });
