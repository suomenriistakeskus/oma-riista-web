'use strict';

angular.module('app.club.services', [])
    .factory('Clubs', function ($resource) {
        return $resource('api/v1/club/:id', {"id": "@id"}, {
            'get': {method: 'GET'},
            'update': {method: 'PUT'},
            'updateLocation': {url: 'api/v1/club/:id/location', method: 'PUT'},
            'harvestSummary': {url: 'api/v1/club/:id/harvestsummary', method: 'GET'}
        });
    })
    .service('ClubService', function ($q, $uibModal, Clubs) {
        function showModal(club) {
            var modalInstance = $uibModal.open({
                templateUrl: 'club/form.html',
                resolve: {
                    club: _.constant(club)
                },
                controller: 'ClubFormController'
            });

            return modalInstance.result.then(function (club) {
                var saveMethod = club.id ? Clubs.update : Clubs.save;
                return saveMethod({id: club.id}, club).$promise;
            });
        }

        this.editClub = function (club) {
            return showModal(angular.copy(club));
        };
    })
    .service('ClubTodoService', function ($uibModal, $state, $q,
                                          HarvestPermits) {

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
                }
            });
        }

        this.showTodo = function (clubId, year) {
            HarvestPermits.listClubTodos({clubId: clubId, year: year}).$promise.then(function (todos) {
                if (todos.todo) {
                    showNotification(clubId, year, todos);
                }
            });
        };
    })
;
