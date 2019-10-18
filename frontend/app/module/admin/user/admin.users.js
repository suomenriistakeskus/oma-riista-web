'use strict';

angular.module('app.admin.users', [])
    .constant('UserRoles', {
        admin: 'ROLE_ADMIN',
        moderator: 'ROLE_MODERATOR',
        rest: 'ROLE_REST'
    })

    .factory('Users', function ($resource) {
        return $resource('api/v1/admin/users/:id', {"id": "@id"}, {
            query: {method: 'GET', params: {type: "page", roles: '@roles'}},
            update: {method: 'PUT'},
            privileges: {method: 'GET', url: 'api/v1/admin/users/privileges', isArray: true}
        });
    })

    .config(function ($stateProvider) {
        $stateProvider.state('admin.users', {
            url: '/users',
            templateUrl: 'admin/user/admin-user-list.html',
            controller: 'AdminUserListController',
            controllerAs: '$ctrl'
        });
    })

    .controller('AdminUserListController', function ($uibModal, NotificationService,
                                                     Users, UserRoles, AdminUserEditModal) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.roles = UserRoles;
            $ctrl.selectedRole = 'ROLE_ADMIN';
            $ctrl.showOnlyActive = true;
            $ctrl.page = {
                content: []
            };

            reloadUserList();
        };

        $ctrl.selectRole = function (role) {
            $ctrl.selectedRole = role;
            reloadUserList();
        };

        $ctrl.isRoleSelected = function (role) {
            return role === $ctrl.selectedRole;
        };

        $ctrl.create = function () {
            AdminUserEditModal.create().then(function () {
                NotificationService.showMessage("admin.users.save_success", "success");
                reloadUserList();
            });
        };

        $ctrl.edit = function (id) {
            AdminUserEditModal.edit(id).then(function () {
                NotificationService.showMessage("admin.users.save_success", "success");
                reloadUserList();
            });
        };

        $ctrl.delete = function (id) {
            Users.delete({id: id}).$promise.then(function () {
                NotificationService.showMessage("admin.users.delete_success", "success");
                reloadUserList();
            });
        };

        $ctrl.toggleShowOnlyActive = function () {
            $ctrl.showOnlyActive = !$ctrl.showOnlyActive;
        };

        function reloadUserList() {
            Users.query({
                sort: 'username',
                size: 300,
                roles: $ctrl.selectedRole
            }).$promise.then(function (data) {
                $ctrl.page = data;
            });
        }
    })

    .filter('userfilter', function () {
        return function(input, showOnlyActive) {
            if (showOnlyActive) {
                return input.filter(function(user) {
                    return user.active === true;
                });
            }
            return input;
        };
    })

    .service('AdminUserEditModal', function ($uibModal, Users, UserRoles) {
        this.create = function () {
            return openModal(function () {
                return {
                    active: true,
                    role: 'ROLE_MODERATOR',
                    nameEditable: true
                };
            });
        };

        this.edit = function (id) {
            return openModal(function () {
                return Users.get({id: id}).$promise;
            });
        };

        function openModal(userFn) {
            return $uibModal.open({
                templateUrl: 'admin/user/admin-user-edit.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                resolve: {
                    user: userFn,
                    availablePrivileges: function (Users) {
                        return Users.privileges().$promise;
                    }
                }
            }).result;
        }

        function ModalController($uibModalInstance, user, availablePrivileges) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.user = user;
                $ctrl.availableRoles = _.toArray(UserRoles);
                $ctrl.availablePrivileges = _.map(availablePrivileges, function (privilege) {
                    return {
                        name: privilege.privilege,
                        role: privilege.role,
                        selected: _.includes($ctrl.user.privileges, privilege.privilege)
                    };
                });
            };

            $ctrl.hasRoleAnyPotentialPrivileges = function () {
                return _.some($ctrl.availablePrivileges, function (p) {
                    return p.role === $ctrl.user.role;
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.save = function () {
                $ctrl.user.privileges = _($ctrl.availablePrivileges)
                    .filter('selected')
                    .map('name')
                    .value();

                var saveMethod = $ctrl.user.id ? Users.update : Users.save;

                saveMethod($ctrl.user).$promise.then(function () {
                    $uibModalInstance.close();
                });
            };

            $ctrl.usernameChanged = function () {
                if ($ctrl.user.role === 'ROLE_REST') {
                    $ctrl.user.lastName = $ctrl.user.username;
                }
            };

            $ctrl.roleChanged = function () {
                if ($ctrl.user.role === 'ROLE_REST') {
                    $ctrl.user.lastName = 'API';
                    $ctrl.user.firstName = $ctrl.user.username;
                    $ctrl.user.email = '';
                    $ctrl.user.phoneNumber = '';
                    $ctrl.user.twoFactorAuthentication = null;
                } else {
                    $ctrl.user.lastName = '';
                }
            };
        }
    });
