'use strict';

angular.module('app.account.controllers', ['ui.router', 'app.account.services'])
    .constant('ModeratorPrivileges', {
        bulkMessagePrivilege: 'SEND_BULK_MESSAGES',
        alterInvoicePayment: 'ALTER_INVOICE_PAYMENT',
        moderateRhyAnnualStatistics: 'MODERATE_RHY_ANNUAL_STATISTICS',
        harvestRegistry: 'HARVEST_REGISTRY',
        habides: 'EXPORT_HABIDES_REPORTS',
        saveHarvestWithIncompleteData: "SAVE_INCOMPLETE_HARVEST_DATA",
        moderateDisabilityPermitApplication: 'MODERATE_DISABILITY_PERMIT_APPLICATION'
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

    .controller('AccountClubRegisterController', function ($q, $http, $filter, personId) {
        var $ctrl = this;

        $ctrl.selectedOrganisation = null;
        $ctrl.nameQuery = '';
        $ctrl.codeQuery = '';
        $ctrl.warningClubAlreadyActive = false;
        $ctrl.existingContactPersonName = '';

        var i18nFilter = $filter('rI18nNameFilter');

        this.searchResultTitle = function (item) {
            if (!item) {
                return '?';
            }

            return i18nFilter(item) +
                (item.contactPersonName ? ' - ' + item.contactPersonName : '') +
                ((item.hasActiveContactPerson) ? ' (*)' : '');
        };

        function search(url, queryString) {
            if (!_.isString(queryString) || _.isEmpty(queryString)) {
                return $q.when([]);
            }

            $ctrl.selectedOrganisation = null;
            $ctrl.warningClubAlreadyActive = false;
            $ctrl.existingContactPersonName = '';

            return $http.get(url, {params: {queryString: queryString}}).then(function (response) {
                return response.data;
            });
        }

        this.searchByName = function ($viewValue) {
            $ctrl.codeQuery = '';
            return search('/api/v1/club/lh/findByName', $viewValue);
        };

        this.searchByCode = function ($viewValue) {
            $ctrl.nameQuery = '';
            return search('/api/v1/club/lh/findByCode', $viewValue);
        };

        this.onSelectSearchResult = function ($item, $model, $label) {
            $ctrl.selectedOrganisation = $item;
            $ctrl.codeQuery = '';
            $ctrl.nameQuery = '';

            if ($item) {
                $ctrl.warningClubAlreadyActive = $item.hasActiveContactPerson;
                $ctrl.existingContactPersonName = $item.contactPersonName;
            } else {
                $ctrl.warningClubAlreadyActive = false;
                $ctrl.existingContactPersonName = '';
            }
        };

        this.canSave = function () {
            return $ctrl.selectedOrganisation && !this.selectedOrganisation.hasActiveContactPerson;
        };

        this.save = function () {
            $ctrl.selectedOrganisation.personId = personId;
            $http.post('/api/v1/club/lh/register', $ctrl.selectedOrganisation).then(function (response) {
                if (response.data.result === 'success') {
                    $ctrl.$close(response.data);
                } else if (response.data.result === 'exists') {
                    $ctrl.warningClubAlreadyActive = true;
                    $ctrl.existingContactPersonName = response.data.contactPersonName;
                }
            });
        };

        this.cancel = function () {
            $ctrl.$dismiss('cancel');
        };
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
