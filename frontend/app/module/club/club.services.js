'use strict';

angular.module('app.club.services', [])
    .factory('Clubs', function ($resource) {
        return $resource('api/v1/club/:id', {'id': '@id'}, {
            'get': {method: 'GET'},
            'update': {method: 'PUT'},
            'updateLocation': {url: 'api/v1/club/:id/location', method: 'PUT'},
            'updateActive': {
                url: 'api/v1/club/:id/active/:active',
                method: 'PUT',
                params: {'id': '@id', 'active': '@active'}
            },
            'harvestSummary': {url: 'api/v1/club/harvestsummary', method: 'POST'}
        });
    })
    .service('ChangeClubNameModal', function ($q, $uibModal, Clubs) {
        function ModalController($uibModalInstance, club) {
            var $ctrl = this;
            $ctrl.club = club;

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.club);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }

        function showModal(club) {
            var modalInstance = $uibModal.open({
                templateUrl: 'club/form_name.html',
                resolve: {
                    club: _.constant(angular.copy(club))
                },
                controller: ModalController,
                controllerAs: '$ctrl'
            });

            return modalInstance.result.then(function (club) {
                var saveMethod = club.id ? Clubs.update : Clubs.save;
                return saveMethod({id: club.id}, club).$promise;
            });
        }

        this.editClubName = function (club) {
            return showModal(club);
        };
    })
    .service('ChangeClubTypeModal', function ($q, $uibModal, Clubs) {
        function ModalController($uibModalInstance,
                                 ActiveRoleService, PersonSearchModal, AccountService,
                                 club) {
            var $ctrl = this;
            $ctrl.club = club;
            $ctrl.moderator = ActiveRoleService.isModerator();
            $ctrl.currentPerson = {};
            if (!$ctrl.moderator) {
                AccountService.loadAccount('me').then(function (acc) {
                    $ctrl.currentPerson = {byName: acc.byName, lastName: acc.lastName};
                });
            }

            $ctrl.setSubType = function (value) {
                $ctrl.club.subtype = value;
            };

            $ctrl.findPerson = function () {
                PersonSearchModal.searchPerson(true, true).then(function (personInfo) {
                    $ctrl.club.clubPerson = {
                        id: personInfo.id,
                        lastName: personInfo.lastName,
                        byName: personInfo.byName
                    };
                });
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.club);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }

        function showModal(club) {
            var modalInstance = $uibModal.open({
                templateUrl: 'club/form_type.html',
                size: 'lg',
                resolve: {
                    club: _.constant(angular.copy(club))
                },
                controller: ModalController,
                controllerAs: '$ctrl'
            });

            return modalInstance.result.then(function (club) {
                var saveMethod = club.id ? Clubs.update : Clubs.save;
                return saveMethod({id: club.id}, club).$promise;
            });
        }

        this.editClubType = function (club) {
            return showModal(club);
        };
    })
    .service('ClubTodoService', function ($uibModal, $state, $q,
                                          ClubPermits) {

        function showNotification(clubId, year, todos) {
            return $uibModal.open({
                templateUrl: 'club/todo_modal.html',
                controllerAs: '$ctrl',
                bindToController: true,
                controller: function () {
                    var $ctrl = this;
                    $ctrl.todos = todos;

                    function go(state) {
                        $state.go(state, {clubId: clubId, year: year});
                    }

                    $ctrl.fixAreaMissing = _.partial(go, 'club.area.list');
                    $ctrl.fixGroupMissing = _.partial(go, 'club.groups');
                    $ctrl.fixGroupPermitMissing = _.partial(go, 'club.groups');
                    $ctrl.fixGroupLeaderMissing = _.partial(go, 'club.groups');
                    $ctrl.fixPartnerSummaryMissing = _.partial(go, 'club.moosepermit');
                }
            });
        }

        this.showTodo = function (clubId, year) {
            ClubPermits.todos({clubId: clubId, year: year}).$promise.then(function (todos) {
                if (todos.todo) {
                    showNotification(clubId, year, todos);
                }
            });
        };
    });
