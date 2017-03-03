'use strict';

angular.module('app.account.registration.controllers', [
    'app.account.registration.services',
    'app.layout.services'
])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('registration-email-changed', {
                url: '/register/email-changed',
                templateUrl: 'account/registration/email_changed.html',
                controller: function () {
                    // without controller a element will not have href attribute -> no hover underlining
                },
                authenticate: false
            })
            .state('registration-send-email', {
                url: '/register/send-email',
                templateUrl: 'account/registration/send_email.html',
                controller: 'AccountRegistrationSendEmailController',
                controllerAs: '$ctrl',
                authenticate: false
            })
            .state('registration-from-email', {
                url: '/register/from-email/{token}?lang',
                templateUrl: 'account/registration/from_email.html',
                controller: 'AccountRegistrationFromEmailController',
                controllerAs: '$ctrl',
                authenticate: false
            })
            .state('registration-from-sso', {
                url: '/register/from-sso/success/{trid:[0-9a-f]{19}}',
                templateUrl: 'account/registration/from_sso.html',
                controller: 'AccountRegistrationFromSsoController',
                controllerAs: '$ctrl',
                authenticate: false,
                resolve: {
                    'registrationData': function ($stateParams, AccountRegistrationService) {
                        return AccountRegistrationService.requestDataToConfirm($stateParams.trid);
                    }
                }
            })
            .state('registration-from-sso-error', {
                url: '/register/from-sso/error',
                templateUrl: 'account/registration/from_sso_error.html',
                authenticate: false
            });
    }])
    .controller('AccountRegistrationSendEmailController',
        function ($state, $translate, AccountRegistrationService, NotificationService) {
            var $ctrl = this;
            $ctrl.email = "";
            $ctrl.processing = false;
            $ctrl.error = false;

            $ctrl.submitForm = function () {
                $ctrl.error = false;
                $ctrl.processing = true;

                AccountRegistrationService.requestAccountRegistrationEmail({
                    email: $ctrl.email,
                    lang: $translate.use() || 'fi'
                }).then(function () {
                    $ctrl.error = false;
                    NotificationService.flashMessage("registration.email.success", "success");
                    $state.go('login');

                }, function () {
                    $ctrl.error = true;
                    $ctrl.processing = false;
                });
            };
        }
    )
    .controller('AccountRegistrationFromEmailController', function ($stateParams, $uibModal, $translate,
                                                                    AccountRegistrationService) {
        var $ctrl = this;
        $ctrl.showTermsAndConditions = false;
        $ctrl.termsAndConditionsAccepted = false;
        $ctrl.checkStatus = 'waiting';
        $ctrl.lang = $stateParams.lang || $translate.use() || 'fi';

        $ctrl.toggleTermsVisibility = function () {
            $ctrl.showTermsAndConditions = !$ctrl.showTermsAndConditions;
        };

        if ($stateParams.lang) {
            $translate.use($stateParams.lang);
        }

        AccountRegistrationService.processTokenFromEmail({
            token: $stateParams.token,
            lang: $ctrl.lang
        }).then(function (response) {
            $ctrl.checkStatus = response.data.status;
            $ctrl.trid = response.data.trid;
        }, function () {
            $ctrl.checkStatus = "error";
        });
    })
    .controller('AccountRegistrationFromSsoController', function ($state, $stateParams, $translate,
                                                                  AccountRegistrationService, NotificationService,
                                                                  registrationData) {
        var $ctrl = this;

        $ctrl.registrationData = registrationData.data;
        $ctrl.password = '';
        $ctrl.confirmPassword = '';
        $ctrl.doNotMatch = null;

        if (!$ctrl.registrationData.lang) {
            $ctrl.registrationData.lang = $translate.use() || 'fi';
        }

        $ctrl.completeRegistration = function () {
            if ($ctrl.password !== $ctrl.confirmPassword) {
                $ctrl.doNotMatch = "ERROR";
            } else {
                $ctrl.doNotMatch = null;
                $ctrl.registrationData.password = $ctrl.password;

                AccountRegistrationService.completeRegistration($ctrl.registrationData).then(function () {
                    NotificationService.flashMessage("registration.completion.success", "success", {ttl: -1});
                    $state.go('login');
                });
            }
        };
    });
