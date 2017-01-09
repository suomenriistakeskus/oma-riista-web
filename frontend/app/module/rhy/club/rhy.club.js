'use strict';

angular.module('app.rhy.club', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy.club', {
                abstract: true,
                templateUrl: 'rhy/club/layout.html',
                url: '/club',
                resolve: {
                    year: function (HuntingYearService) {
                        return HuntingYearService.getCurrent();
                    },
                    exportToExcel: function (FormPostService, orgId, year) {
                        function exportToExcel(orgId, year) {
                            FormPostService.submitFormUsingBlankTarget('/api/v1/riistanhoitoyhdistys/contacts-and-leaders/excel',
                                {orgId: orgId, year: year});
                        }

                        return _.partial(exportToExcel, orgId, year);
                    }
                },
                controllerAs: '$ctrl',
                controller: function (exportToExcel) {
                    this.exportToExcel = exportToExcel;
                }
            })
            .state('rhy.club.contacts', {
                url: '/contacts',
                templateUrl: 'rhy/club/contacts.html',
                controllerAs: '$ctrl',
                controller: 'RhyClubContactsController',
                resolve: {
                    contacts: function (Rhys, orgId) {
                        return Rhys.clubContacts({id: orgId});
                    }
                }
            })
            .state('rhy.club.huntingLeaders', {
                url: '/leaders',
                templateUrl: 'rhy/club/leaders.html',
                controllerAs: '$ctrl',
                controller: 'RhyClubLeadersController',
                resolve: {
                    leaders: function (Rhys, year, orgId) {
                        return Rhys.clubLeaders({id: orgId, year: year});
                    }
                }
            })
        ;
    })
    .controller('RhyClubContactsController',
        function ($scope, contacts) {
            this.contacts = contacts;
        })
    .controller('RhyClubLeadersController',
        function ($scope, leaders) {
            this.leaders = leaders;
        })
;