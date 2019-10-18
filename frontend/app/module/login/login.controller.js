'use strict';

angular.module('app.login.controllers', ['ui.router', 'app.login.services'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: 'login/login.html',
                controller: 'LoginController',
                controllerAs: '$ctrl',
                bindToController: true,
                authenticate: false
            });
    })

    .controller('LoginController', function ($uibModal, ActiveRoleService, AvailableRoleService, NotificationService,
                                             LoginService, AuthenticationService, LanguageService) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.isSelectedLanguage = LanguageService.isSelectedLanguage;
            $ctrl.changeLanguage = LanguageService.changeLanguage;
            $ctrl.credentials = {
                username: null,
                password: null,
                rememberMe: true
            };

            // Check if already authenticated?
            AuthenticationService.authenticate().catch(function () {
                // Load twitter widget only when "login is required" and user is not immediately redirected away
                window.setTimeout(function () {
                    try {
                        loadTwitterWidget();
                    } catch (e) {
                        console.error(e);
                    }
                }, 0);
            });
        };

        $ctrl.login = function () {
            LoginService.login($ctrl.credentials).catch(function (response) {
                if (!response.data || !response.data.status) {
                    onLoginFailure();

                } else if (response.data.status === 'OTP_REQUIRED') {
                    showOneTimePasswordDialog(angular.copy($ctrl.credentials));
                } else {
                    onLoginFailure();

                    if (response.data.status === 'OTP_FAILURE') {
                        NotificationService.showMessage('login.otp.sendFailed', 'warn');
                    }
                }
            });
        };

        function onLoginFailure() {
            $ctrl.credentials.password = "";
            NotificationService.showMessage('login.messages.error.authentication', 'error');
        }

        function showOneTimePasswordDialog(requestData) {
            var modalInstance = $uibModal.open({
                templateUrl: 'login/otp_dialog.html',
                controller: 'LoginOtpController',
                controllerAs: '$ctrl'
            });

            modalInstance.result.then(function (result) {
                requestData.otp = result;
                LoginService.login(requestData).catch(onLoginFailure);

            }, onLoginFailure);
        }

        function loadTwitterWidget() {
            window.twttr = (function (d, s, id) {
                var fjs = d.getElementsByTagName(s)[0];
                var t = window.twttr || {};

                if (d.getElementById(id)) {
                    return t;
                }

                var js = d.createElement(s);
                js.id = id;
                js.src = "https://platform.twitter.com/widgets.js";
                fjs.parentNode.insertBefore(js, fjs);

                t._e = [];

                t.ready = function (f) {
                    t._e.push(f);
                };

                return t;
            }(document, "script", "twitter-wjs"));

            if (window.twttr.ready()) {
                window.twttr.widgets.load();
            }
        }
    })

    .controller('LoginOtpController', function ($uibModalInstance) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.otp = '';
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.submit = function () {
            $uibModalInstance.close($ctrl.otp);
        };
    });
