'use strict';

angular.module('app.reporting.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting', {
                abstract: true,
                templateUrl: 'reporting/layout.html',
                url: '/moderator',
                controllerAs: '$ctrl',

                controller: function (ActiveRoleService, AvailableRoleService, ModeratorPrivileges) {
                    var $ctrl = this;

                    $ctrl.isAuthorizedForHabides = ActiveRoleService.isAdmin() ||
                            AvailableRoleService.hasPrivilege(ModeratorPrivileges.habides);

                }
            })
            .state('reporting.home', {
                url: '/home',
                templateUrl: 'reporting/home.html'
            })
            .state('reporting.announcements', {
                url: '/announcements',
                controllerAs: '$ctrl',
                templateUrl: 'reporting/announcements.html',
                controller: function () {
                    this.rk = {
                        organisationType: 'RK',
                        officialCode: '850'
                    };
                }
            })
            .state('reporting.srvaMap', {
                url: '/srvaMap',
                wideLayout: true,
                templateUrl: 'srva/srva-map.html',
                controller: 'SrvaEventMapController',
                controllerAs: '$ctrl',
                resolve: {
                    parameters: function (GameDiarySrvaParameters) {
                        return GameDiarySrvaParameters.query().$promise;
                    },
                    initialRhy: function () {
                        return null;
                    },
                    moderatorView: function () {
                        return true;
                    },
                    tabs: function (Rhys) {
                        return Rhys.searchParamOrganisations({id: null}).$promise;
                    }
                }
            });
    });
