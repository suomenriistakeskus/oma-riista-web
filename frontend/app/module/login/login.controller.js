'use strict';

angular.module('app.login.controllers', ['ui.router', 'app.login.services'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: 'login/login.html',
                controller: 'LoginController',
                authenticate: false
            })
            .state('logout', {
                url: '/logout',
                templateUrl: 'login/login.html',
                controller: 'LogoutController',
                authenticate: false
            })
            .state('roleselection', {
                url: '/roleselection',
                templateUrl: 'login/roleselection.html',
                controller: 'RoleSelectionController',
                controllerAs: '$ctrl',
                params: {lang: {value: null}},
                bindToController: true
            });
    })
    .controller('RoleSelectionController', function ($state, ActiveRoleService, MapState) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.availableRoles = ActiveRoleService.getAvailableRoles();
            $ctrl.getRoleTitle = ActiveRoleService.getRoleDisplayName;
            $ctrl.getRoleLogo = ActiveRoleService.getRoleLogo;
        };

        $ctrl.getRoleSubtitle = function (role) {
            if (role && role.type === 'PERMIT') {
                return role.context.permitNumber;
            }
            return '';
        };

        // Update selected role in view on change
        $ctrl.selectRole = function (role) {
            ActiveRoleService.selectActiveRole(role);
            MapState.reset();

            $state.go('main');
        };
    })
    .controller('LoginController',
        function ($scope, $uibModal,
                  LoginService, AuthenticationService, ActiveRoleService, NotificationService) {
            // Check if already authenticated?
            AuthenticationService.authenticate().then(_.noop, _.noop);

            // Active role must be cleared here, because event:auth-loginConfirmed
            // could be caused by normal page reload when authenticated
            ActiveRoleService.clearActiveRole();

            //reload twitter feed
            if (window.twttr) {
                window.twttr.widgets.load();
            }

            $scope.credentials = {
                username: null,
                password: null,
                rememberMe: true
            };

            function onLoginFailure() {
                $scope.credentials.password = "";
                NotificationService.showMessage('login.messages.error.authentication', 'error');
            }

            function showOneTimePasswordDialog(requestData) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'login/otp_dialog.html',
                    controller: 'LoginOtpController',
                    size: 'sm',
                    backdrop: false
                });

                modalInstance.result.then(function (result) {
                    requestData.otp = result;
                    LoginService.login(requestData).catch(onLoginFailure);

                }, onLoginFailure);
            }

            $scope.login = function () {
                LoginService.login($scope.credentials).catch(function (response) {
                    if (!response.data || !response.data.status) {
                        onLoginFailure();

                    } else if (response.data.status === 'OTP_REQUIRED') {
                        showOneTimePasswordDialog(angular.copy($scope.credentials));
                    } else {
                        onLoginFailure();

                        if (response.data.status === 'OTP_FAILURE') {
                            NotificationService.showMessage('login.otp.sendFailed', 'warn');
                        }
                    }
                });
            };
        })
    .controller('LoginOtpController',
        function ($scope, $uibModalInstance) {
            $scope.formData = {};

            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $scope.submit = function () {
                $uibModalInstance.close($scope.formData.otp);
            };
        })
    .controller('LogoutController',
        function (LoginService, $state) {
            LoginService.logout().then(function() {
                $state.go('login');
            });
        });
