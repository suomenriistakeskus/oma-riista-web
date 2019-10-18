'use strict';

angular.module('app.club.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club', {
                abstract: true,
                templateUrl: 'club/layout.html',
                url: '/club/{id:[0-9]{1,8}}',
                controller: function ($scope, club, todos) {
                    $scope.club = club;
                    $scope.todos = todos;
                    $scope.groupTodos = !$scope.todos.groupMissing && $scope.todos.groupPermitMissing;

                },
                resolve: {
                    clubId: function ($stateParams) {
                        return $stateParams.id;
                    },
                    club: function (Clubs, clubId) {
                        return Clubs.get({id: clubId}).$promise;
                    },
                    todos: function (ClubPermits, Helpers, clubId) {
                        return ClubPermits.todos({clubId: clubId, year: moment().year()}).$promise;
                    }
                }
            })

            .state('club.main', {
                url: '/',
                templateUrl: 'club/show.html',
                controller: 'ClubShowController',
                controllerAs: '$ctrl'
            })

            .state('club.announcements', {
                url: '/announcements',
                controllerAs: '$ctrl',
                templateUrl: 'club/announcements.html',
                resolve: {
                    club: function (Clubs, clubId) {
                        return Clubs.get({id: clubId}).$promise;
                    }
                },
                controller: function ($state, $stateParams, club) {
                    this.club = club;
                }
            })

            .state('club.harvestsummary', {
                url: '/harvestsummary?year',
                templateUrl: 'club/harvestsummary.html',
                controller: 'ClubHarvestSummaryController',
                resolve: {
                    year: function ($stateParams) {
                        return $stateParams.year ? _.parseInt($stateParams.year) : new Date().getFullYear();
                    },
                    summary: function (club, year, Clubs) {
                        return Clubs.harvestSummary({id: club.id, calendarYear: year}).$promise;
                    }
                }
            });
    })

    .controller('ClubShowController', function ($state, MapDefaults, NotificationService, ActiveRoleService,
                                                ClubMemberService, ChangeClubNameModal, ChangeClubTypeModal, Clubs,
                                                club) {
        var $ctrl = this;
        $ctrl.club = club;

        $ctrl.moderatorView = ActiveRoleService.isModerator();

        $ctrl.setPrimaryContact = function (member) {
            ClubMemberService.setPrimaryContact(member)
                .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                .finally(function () {
                    $state.reload();
                });
        };

        $ctrl.editName = function () {
            var modalPromise = ChangeClubNameModal.editClubName($ctrl.club);
            NotificationService.handleModalPromise(modalPromise).then(function () {
                $state.reload();
            });
        };

        $ctrl.editType = function () {
            var modalPromise = ChangeClubTypeModal.editClubType($ctrl.club);
            NotificationService.handleModalPromise(modalPromise).then(function () {
                $state.reload();
            });
        };

        // Activation
        $ctrl.setActive = function (active) {
            Clubs.updateActive({id: club.id, active: active ? 1 : 0}).$promise
                .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                .finally(function () {
                    $state.reload();
                });
        };
    })

    .component('clubLocationEdit', {
        templateUrl: 'club/form_location.html',
        bindings: {
            club: '<'
        },
        controller: function ($state, MapDefaults, NotificationService, Clubs, MapUtil, MapState) {
            var $ctrl = this;

            $ctrl.editLocation = false;

            $ctrl.$onInit = function () {
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);

                MapState.updateMapCenter($ctrl.club.geoLocation ? angular.copy($ctrl.club.geoLocation) : MapUtil.getDefaultGeoLocation());
                MapState.get().center.zoom = 5;

                $ctrl.mapState = MapState.get();
                $ctrl.mapDefaults = MapDefaults.create();

                $ctrl.originalLocation = $ctrl.club.geoLocation ? angular.copy($ctrl.club.geoLocation) : {
                    latitude: null,
                    longitude: null
                };
            };

            $ctrl.editLocationStart = function () {
                $ctrl.editLocation = true;
            };

            $ctrl.editLocationEnd = function () {
                $ctrl.editLocation = false;
                Clubs.updateLocation({id: $ctrl.club.id}, $ctrl.club.geoLocation).$promise
                    .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                    .finally(function () {
                        $state.reload();
                    });
            };

            function locationsEqual(a, b) {
                var lat = 'latitude';
                var lng = 'longitude';
                return _.get(a, lat) === _.get(b, lat) && _.get(a, lng) === _.get(b, lng);
            }

            $ctrl.locationChanged = function () {
                return $ctrl.club.geoLocation && !locationsEqual($ctrl.club.geoLocation, $ctrl.originalLocation);
            };

            $ctrl.editLocationCancel = function () {
                $ctrl.editLocation = false;
                $ctrl.club.geoLocation = angular.copy($ctrl.originalLocation);
            };
        }
    })

    .controller('ContactShareController',
        function ($q, $scope, $state, $translate, dialogs,
                  NotificationService, Account, ClubMembers,
                  clubOccupations) {
            $scope.clubOccupations = clubOccupations;

            var ok = function () {
                $state.reload();
                NotificationService.showDefaultSuccess();
            };
            var fail = function () {
                $state.reload();
                NotificationService.showDefaultFailure();
            };

            $scope.leaveClub = function (member) {
                var requestParams = {
                    clubId: member.organisation.id,
                    id: member.id
                };

                return ClubMembers.isLocked(requestParams).$promise.then(function (response) {
                    if (response.isLocked) {
                        NotificationService.showMessage('club.main.contactPersons.warningRemoveOnlyContactPersonWithData', 'error');
                        return $q.reject();
                    }
                    return $q.when();
                }).then(function () {
                    var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                    var dialogMessage = $translate.instant('global.dialog.confirmation.text');
                    return dialogs.confirm(dialogTitle, dialogMessage).result;
                }).then(function () {
                    ClubMembers.delete(requestParams).$promise.then(ok, fail);
                });
            };

            $scope.save = function (member) {
                var updates = [{occupationId: member.id, share: member.contactInfoShare}];
                Account.contactShare(updates).$promise.then(ok, fail);
            };
        })

    .controller('ClubHarvestSummaryController',
        function ($scope, $state, $stateParams, $translate,
                  summary, year) {

            $scope.summary = summary;

            $scope.getGameCategory = function (index, item) {
                // show category only for the first of each category
                var currentCategoryId = item.species.categoryId;
                if (index === 0 || currentCategoryId !== summary.items[index - 1].species.categoryId) {
                    return $translate.instant('club.harvestsummary.gameCategory' + currentCategoryId);
                }
                return '';
            };
            $scope.searchModel = {
                year: year
            };
            $scope.years = _.range(2014, new Date().getFullYear() + 1);

            if (angular.isArray($scope.summary.items)) {
                var sortFields = ['species.categoryId', 'count', 'species.code'];
                var sortOrders = ['asc', 'desc', 'asc'];

                $scope.summary.items = _.orderBy($scope.summary.items, sortFields, sortOrders);
            }

            $scope.yearChanged = function () {
                var params = angular.copy($stateParams);
                params.year = $scope.searchModel.year;

                $state.transitionTo($state.current, params, {reload: true, inherit: false, notify: true});
            };
        });
