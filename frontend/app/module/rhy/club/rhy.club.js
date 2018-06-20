'use strict';

angular.module('app.rhy.club', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy.club', {
                abstract: true,
                templateUrl: 'rhy/club/layout.html',
                url: '/club',
                resolve: {
                    contacts: function (Rhys, orgId) {
                        return Rhys.clubContacts({id: orgId});
                    },
                    year: function (HuntingYearService) {
                        return HuntingYearService.getCurrent();
                    },
                    availableYears: function (HuntingYearService) {
                        return HuntingYearService.createHuntingYearChoices(2016);
                    },
                    exportToExcel: function (FormPostService, orgId, year) {
                        function exportToExcel(orgId, year) {
                            FormPostService.submitFormUsingBlankTarget('/api/v1/riistanhoitoyhdistys/contacts-and-leaders/excel',
                                {orgId: orgId, year: year});
                        }
                        return _.partial(exportToExcel, orgId);
                    },
                    loadLeaders: function (Rhys, orgId) {
                        return function (year) {
                            return Rhys.clubLeaders({id: orgId, year: year});
                        };
                    },
                    leaders: function (loadLeaders, year) {
                        return loadLeaders(year);
                    }
                },
                controllerAs: '$ctrl',
                controller: function (contacts, loadLeaders, leaders, availableYears, exportToExcel) {
                    var $ctrl = this;
                    $ctrl.contacts = contacts;

                    $ctrl.exportToExcel = function () {
                        exportToExcel($ctrl.selectedYear.year);
                    };

                    $ctrl.leaders = leaders;

                    $ctrl.years = availableYears;
                    $ctrl.selectedYear = _($ctrl.years).last();
                    $ctrl.loadLeaders = function () {
                        $ctrl.leaders = loadLeaders($ctrl.selectedYear.year);
                    };
                }
            })
            .state('rhy.club.contacts', {
                url: '/contacts',
                templateUrl: 'rhy/club/contacts.html'
            })
            .state('rhy.club.huntingLeaders', {
                url: '/leaders',
                templateUrl: 'rhy/club/leaders.html'
            })
        ;
    })
;