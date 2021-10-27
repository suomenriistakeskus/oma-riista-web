'use strict';

angular.module('app.account.controllers', ['ui.router', 'app.account.services'])
    .constant('ModeratorPrivileges', {
        bulkMessagePrivilege: 'SEND_BULK_MESSAGES',
        alterInvoicePayment: 'ALTER_INVOICE_PAYMENT',
        moderateRhyAnnualStatistics: 'MODERATE_RHY_ANNUAL_STATISTICS',
        harvestRegistry: 'HARVEST_REGISTRY',
        habides: 'EXPORT_HABIDES_REPORTS',
        saveHarvestWithIncompleteData: "SAVE_INCOMPLETE_HARVEST_DATA",
        moderateDisabilityPermitApplication: 'MODERATE_DISABILITY_PERMIT_APPLICATION',
        otherwiseDeceased: 'MUUTOIN_KUOLLEET'
    })
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile', {
                abstract: true,
                templateUrl: 'account/layout.html',
                url: '/profile/{id:[0-9a-zA-Z]{1,8}}',
                controllerAs: '$navCtrl',
                controller: function ($scope, $state, $stateParams,
                                      AuthenticationService, ActiveRoleService,
                                      AvailableRoleService, ModeratorPrivileges) {
                    var $navCtrl = this;

                    function routePersonId() {
                        return ActiveRoleService.isModerator() ? $stateParams.id : 'me';
                    }

                    $navCtrl.isModeratorView = function () {
                        return ActiveRoleService.isModerator();
                    };

                    $navCtrl.hideNavigation = function () {
                        return !AuthenticationService.isAuthenticated();
                    };

                    $navCtrl.openProfile = function () {
                        $state.go('profile.account', {id: routePersonId()});
                    };

                    $navCtrl.openPermits = function () {
                        $state.go('profile.permits', {id: routePersonId()});
                    };

                    $navCtrl.openAreas = function () {
                        $state.go('profile.areas.personal', {id: routePersonId()});
                    };

                    $navCtrl.openHarvestRegistry = function () {
                        $state.go('profile.harvestRegistry', {id: routePersonId()});
                    };

                    $navCtrl.isAuthorizedForHarvestRegistry = function () {
                        return ActiveRoleService.isAdmin() ||
                            !$navCtrl.isModeratorView() ||
                            AvailableRoleService.hasPrivilege(ModeratorPrivileges.harvestRegistry);
                    };
                }
            })
            .state('profile.clubconfig', {
                url: '/clubconfig',
                templateUrl: 'club/config.html',
                controller: 'ContactShareController',
                resolve: {
                    profile: function (AccountService, $stateParams) {
                        return AccountService.loadAccount($stateParams.id);
                    },
                    clubOccupations: function (profile) {
                        return profile.clubOccupations;
                    }
                }
            });
    })

    .controller('AccountClubCreateController', function (Clubs, MapUtil, MapState, MapDefaults, NotificationService, personId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.club = {
                geoLocation: null,
                personId: personId
            };

            MapState.updateMapCenter(MapUtil.getDefaultGeoLocation());
            MapState.get().center.zoom = 5;

            $ctrl.mapState = MapState.get();
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
            $ctrl.mapDefaults = MapDefaults.create();
        };

        $ctrl.canSave = function () {
            return $ctrl.club.geoLocation;
        };

        $ctrl.save = function () {
            Clubs.save($ctrl.club).$promise.then(
                function (createdClub) {
                    $ctrl.$close(createdClub);
                }, function () {
                    NotificationService.showDefaultFailure();
                });
        };


        $ctrl.cancel = function () {
            $ctrl.$dismiss('cancel');
        };
    });
