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

    .controller('LoginController', function ($q, $uibModal, $translate, $http, ActiveRoleService, AvailableRoleService, NotificationService,
                                             LoginService, AuthenticationService, LanguageService, isProductionEnvironment, News) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.isSelectedLanguage = LanguageService.isSelectedLanguage;
            $ctrl.credentials = {
                username: null,
                password: null,
                rememberMe: true
            };

            $ctrl.isProductionEnvironment = isProductionEnvironment;

            $ctrl.news = [];
            $ctrl.metsastajaFeeds = [];
            $ctrl.riistaFeeds = [];

            // Check if already authenticated?
            AuthenticationService.authenticate().catch(function () {
                News.listLatest().$promise.then(function (news) {
                    $ctrl.news = news;
                });

                var feedLanguage =  $ctrl.getLanguage();
                loadMetsastajaFeeds(feedLanguage).then(function (feeds) {
                    $ctrl.metsastajaFeeds = feeds;
                });

                loadRiistaFeeds(feedLanguage, 1, []).then(function (feeds) {
                    $ctrl.riistaFeeds = feeds;
                });
            });

        };

        $ctrl.getLanguage = function () {
            var language = $translate.proposedLanguage() || $translate.use();
            return language === 'sv' ? 'sv' : 'fi';
        };

        $ctrl.changeLanguage = function (language) {
            LanguageService.changeLanguage(language);
            var feedLanguage = language === 'sv' ? 'sv' : 'fi';
            loadMetsastajaFeeds(feedLanguage).then(function (feeds) {
                $ctrl.metsastajaFeeds = feeds;
            });
            loadRiistaFeeds(feedLanguage,1, []).then(function (feeds) {
                $ctrl.riistaFeeds = feeds;
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

        function loadMetsastajaFeeds(language) {
            return $q(function (resolve) {
                $http({
                    url: 'https://metsastajalehti.fi/wp-json/wp/v2/posts',
                    method: "GET",
                    params: {_fields: 'id, title, link', per_page: 3, lang: language}
                }).then(function (res) {
                    var queryResult = _.map(res.data, function (item) {
                        return {
                            id: item.id,
                            title: item.title.rendered,
                            link: item.link
                        };
                    });
                    resolve(queryResult);
                });
            });
        }

        function loadRiistaFeeds(language, page, queryResult) {
            return $q(function (resolve) {
                $http({
                    url: 'https://riista.fi/wp-json/wp/v2/posts',
                    method: "GET",
                    params: {_fields: 'id, title, link', page: page}
                }).then(function (res) {
                    queryResult = queryResult.concat(_.chain(res.data)
                        .filter(function (item) {
                            return riistaFeedsFilter(item, language);
                        })
                        .map(function (item) {
                            return {
                                id: item.id,
                                title: item.title.rendered,
                                link: item.link
                            };
                        })
                        .value());
                    if (queryResult.length >= 3 || page === res.headers(['x-wp-totalpages'])) {
                        queryResult = _.slice(queryResult, 0, 3);
                        resolve(queryResult);
                    } else {
                        loadRiistaFeeds(language,page + 1, queryResult).then(function (res) {
                            resolve(res);
                        });
                    }
                });
            });
        }

        function riistaFeedsFilter(item, language) {
            return language === 'sv' ?
                _.startsWith(item.link, 'https://riista.fi/sv') :
                !_.startsWith(item.link, 'https://riista.fi/sv');
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
    })

    .component('loginAdditionalInfo', {
        templateUrl: 'login/additional-info.html',
        bindings: {
            riistaFeeds: '<',
            metsastajaFeeds: '<',
            isSelectedLanguage: '&'
        },
        controllerAs: '$ctrl'
    })

    .component('loginLogoRow', {
        templateUrl: 'login/logo-row.html'
    })

    .component('loginStaticInfo', {
        templateUrl: 'login/static-info.html'
    })

    .component('loginNews', {
        templateUrl: 'login/news.html',
        bindings: {
            news: '<',
            getLanguage: '&'
        },
        controllerAs: '$ctrl'
    });
