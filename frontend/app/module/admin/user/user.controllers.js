'use strict';

angular.module('app.admin.user.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('admin.users_base', {
                abstract: true,
                url: '',
                template: '<ui-view autoscroll="false"/>',
                resolve: {
                    helper: function () {
                        var admin = 'ROLE_ADMIN',
                            moderator = 'ROLE_MODERATOR',
                            rest = 'ROLE_REST';

                        return {
                            getRoles: function () {
                                return {
                                    admin: admin,
                                    moderator: moderator,
                                    rest: rest
                                };
                            },
                            getInitiallySelectedRole: function () {
                                return admin;
                            },
                            getSinglePageParams: function (selectedRoles) {
                                return {
                                    sort: 'username',
                                    size: 300,
                                    roles: selectedRoles
                                };
                            }
                        };
                    }
                }
            })
            .state('admin.users', {
                parent: 'admin.users_base',
                url: '/users',
                templateUrl: 'admin/user/users.html',
                controller: 'UserController',
                resolve: {
                    initialPage: function (Users, helper) {
                        // Pre-load data before route is changed
                        var initialRole = helper.getInitiallySelectedRole();
                        return Users.query(helper.getSinglePageParams(initialRole)).$promise;
                    }
                }
            });
    })
    .controller('UserController',
        function ($scope, $uibModal, Users, NotificationService, initialPage, helper) {
            $scope.page = initialPage;
            $scope.roles = helper.getRoles();

            var selectedRole = helper.getInitiallySelectedRole();

            var reloadPage = function () {
                Users.query(helper.getSinglePageParams(selectedRole)).$promise
                    .then(function (data) {
                        $scope.page = data;
                    });
            };

            $scope.selectRole = function (role) {
                selectedRole = role;
                reloadPage();
            };

            $scope.isRoleSelected = function (role) {
                return role === selectedRole;
            };

            $scope.create = function () {
                var modalInstance = $uibModal.open({
                    templateUrl: 'admin/user/form.html',
                    resolve: {
                        user: function () {
                            return { active: true, role: $scope.roles.moderator, nameEditable: true };
                        },
                        roles: function () {
                            return _.toArray($scope.roles);
                        }
                    },
                    controller: 'UserFormController'
                });

                modalInstance.result.then(function () {
                    reloadPage();

                    NotificationService.showMessage("admin.users.save_success", "success");
                });
            };

            $scope.edit = function (id) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'admin/user/form.html',
                    resolve: {
                        user: function () {
                            return Users.get({ id: id }).$promise;
                        },
                        roles: function () {
                            return _.toArray($scope.roles);
                        }
                    },
                    controller: 'UserFormController'
                });

                modalInstance.result.then(function () {
                    reloadPage();

                    NotificationService.showMessage("admin.users.save_success", "success");
                });
            };

            $scope.delete = function (id) {
                Users.delete({ id: id }, function () {
                    reloadPage();

                    NotificationService.showMessage("admin.users.delete_success", "success");
                });
            };
        }
    )
    .controller('UserFormController',
        function ($scope, $uibModalInstance, Users, user, roles) {
            $scope.user = user;
            $scope.roles = roles;

            $scope.privileges = [];
            Users.privileges().$promise.then(function (privileges) {
                $scope.privileges = _.map(privileges, function (p) {
                    return {name: p, selected: _.contains($scope.user.privileges, p)};
                });
            });

            $scope.usernameChanged = function () {
                if ($scope.user.role === 'ROLE_REST') {
                    $scope.user.lastName  = $scope.user.username;
                }
            };

            $scope.roleChanged = function () {
                if ($scope.user.role === 'ROLE_REST') {
                    $scope.user.lastName = 'API';
                    $scope.user.firstName  = $scope.user.username;
                    $scope.user.email  = '';
                    $scope.user.phoneNumber  = '';
                } else {
                    $scope.user.lastName = '';
                }
            };

            var onSaveSuccessful = function () {
                $uibModalInstance.close();
            };

            $scope.save = function () {
                $scope.user.privileges = _($scope.privileges).filter('selected').pluck('name').value();

                if ($scope.user.id) {
                    Users.update($scope.user, onSaveSuccessful);
                } else {
                    Users.save($scope.user, onSaveSuccessful);
                }
            };

            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        });
