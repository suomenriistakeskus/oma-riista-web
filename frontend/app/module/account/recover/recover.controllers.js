'use strict';

angular.module('app.account.recover.controllers', ['ui.router', 'app.account.recover.services'])
    .config(['$stateProvider',
        function ($stateProvider) {
            $stateProvider
                .state('recoverPassword', {
                    url: '/password/recover',
                    templateUrl: 'account/recover/send_email.html',
                    controller: 'RecoverPasswordController',
                    params: {
                        tokenError: false
                    },
                    authenticate: false
                })
                .state('resetPassword', {
                    url: '/password/reset/{token}',
                    templateUrl: 'account/recover/from_email.html',
                    controller: 'PasswordResetController',
                    authenticate: false,
                    resolve: {
                        token: function ($state, $stateParams, PasswordResetService) {
                            var token = $stateParams.token;
                            var ok = _.constant($stateParams.token);
                            var fail = function () {
                                $state.go('recoverPassword', {tokenError: true});
                            };
                            return PasswordResetService.verifyToken(token)
                                .then(ok, fail);
                        }
                    }
                });
        }])
    .controller('RecoverPasswordController',
        function ($scope, $state, $stateParams, PasswordResetService, NotificationService) {
            $scope.tokenError = $stateParams.tokenError;
            $scope.email = "";
            $scope.getValidationClasses = function (field) {
                return {
                    'has-success': field.$valid,
                    'has-error': field.$invalid && field.$dirty
                };
            };

            $scope.submit = function () {
                $scope.error = false;
                $scope.processing = true;

                PasswordResetService.requestPasswordResetEmail($scope.email)
                    .then(function () {
                        NotificationService.flashMessage("recover_password.email_sent.msg", "success");

                        $state.go('login');

                    }, function () {
                        $scope.error = true;
                    })
                    .finally(function () {
                        $scope.processing = true;
                    });
            };
        })
    .controller('PasswordResetController',
        function ($scope, $state, PasswordResetService, NotificationService, token) {

            $scope.password = "";
            $scope.getValidationClasses = function (field) {
                return {
                    'has-success': field.$valid,
                    'has-error': field.$invalid && field.$dirty
                };
            };

            $scope.submit = function () {
                $scope.error = false;

                if ($scope.password !== $scope.confirmPassword) {
                    $scope.password = "";
                    $scope.confirmPassword = "";
                    $scope.doNotMatch = true;

                } else {
                    $scope.doNotMatch = false;

                    PasswordResetService.resetPasswordUsingToken(token, $scope.password)
                        .then(function () {
                            NotificationService.flashMessage("recover_password.success.msg", "success");

                            $state.go('login');

                        }, function () {
                            $scope.error = true;
                        });
                }
            };
        });
