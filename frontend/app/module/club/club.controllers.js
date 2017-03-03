'use strict';

angular.module('app.club.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club', {
                abstract: true,
                templateUrl: 'club/layout.html',
                url: '/club/{id:[0-9]{1,8}}',
                controller: function ($scope, club) {
                    $scope.club = club;
                },
                resolve: {
                    clubId: function ($stateParams) {
                        return $stateParams.id;
                    },
                    club: function (Clubs, clubId) {
                        return Clubs.get({id: clubId}).$promise;
                    }
                }
            })

            .state('club.main', {
                url: '/',
                templateUrl: 'club/show.html',
                controller: 'ClubShowController'
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
                    huntingYear: function ($stateParams, HuntingYearService) {
                        return $stateParams.year ? _.parseInt($stateParams.year) : HuntingYearService.getCurrent();
                    },
                    summary: function (club, huntingYear, Clubs) {
                        return Clubs.harvestSummary({id: club.id, huntingYear: huntingYear}).$promise;
                    }
                }
            });
    })

    .controller('ClubShowController', function ($scope, $state, MapDefaults, NotificationService, ActiveRoleService,
                                                ClubMemberService, ClubService, Clubs,
                                                club) {
        $scope.club = club;
        $scope.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
        $scope.mapDefaults = MapDefaults.create();

        $scope.setPrimaryContact = function (member) {
            ClubMemberService.setPrimaryContact(member)
                .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                .finally(function () {
                    $state.reload();
                });
        };

        $scope.canEdit = function () {
            return club.canEdit;
        };

        $scope.edit = function () {
            var modalPromise = ClubService.editClub($scope.club);
            NotificationService.handleModalPromise(modalPromise).then(function () {
                $state.reload();
            });
        };

        // Location edit
        var originalLocation = club.geoLocation ? angular.copy(club.geoLocation) : {latitude: null, longitude: null};

        $scope.locationChanged = function () {
            return club.geoLocation && (club.geoLocation.latitude !== originalLocation.latitude || club.geoLocation.longitude !== originalLocation.longitude);
        };

        $scope.editLocation = false;

        $scope.editLocationStart = function () {
            $scope.editLocation = true;
        };

        $scope.editLocationEnd = function () {
            $scope.editLocation = false;
            Clubs.updateLocation({id: club.id}, club.geoLocation).$promise
                .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                .finally(function () {
                    $state.reload();
                });
        };

        $scope.editLocationCancel = function () {
            $scope.editLocation = false;
            $scope.club.geoLocation = originalLocation;
        };
    })

    .controller('ClubFormController', function ($scope, $uibModalInstance, club) {
        $scope.club = club;

        $scope.save = function () {
            $scope.club.rhy = {officialCode: $scope.club.rhy.officialCode};
            $scope.club.mooseArea = {number: $scope.club.mooseArea.number};
            $uibModalInstance.close($scope.club);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
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
                  HuntingYearService,
                  summary, huntingYear, club) {

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
                huntingYear: huntingYear
            };
            $scope.huntingYears = HuntingYearService.createHuntingYearChoices();

            if (angular.isArray($scope.summary.items)) {
                var sortFields = ['species.categoryId', 'count', 'species.code'];
                var sortOrders = [true, false, true];

                $scope.summary.items = _.sortByOrder($scope.summary.items, sortFields, sortOrders);
            }

            $scope.$watch('searchModel.huntingYear', function (newHuntingYear, oldHuntingYear) {
                if (newHuntingYear !== oldHuntingYear) {
                    var params = angular.copy($stateParams);
                    params.year = newHuntingYear;

                    $state.transitionTo($state.current, params, {reload: true, inherit: false, notify: true});
                }
            });
        });
