'use strict';

angular.module('app.login.services', ['http-auth-interceptor', 'app.account.services'])
    .run(function ($rootScope, $state, $stateParams, httpBuffer,
                   AuthenticationService, LoginRedirectService,
                   ActiveRoleService, SiteSearchService) {
        $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
            var pendingAuthentication = AuthenticationService.getAuthentication();
            var pendingUsername = pendingAuthentication ? pendingAuthentication.username : null;

            LoginRedirectService.setPendingState(toState, toParams, pendingUsername);

            if (AuthenticationService.isAuthenticated()) {
                return;
            }

            if (toState.authenticate !== false) {
                // Prevent unauthenticated access
                event.preventDefault();

                AuthenticationService.authenticate().catch(function () {
                    // Flush HTTP buffer so that all pending state resolves
                    // will not trigger when authentication is successful.
                    httpBuffer.rejectAll();

                    AuthenticationService.clearAuthentication();

                    $state.go('login', {notify: false});
                });
            }
        });

        // Call when the 401 response is returned by the client
        $rootScope.$on('event:auth-loginRequired', function () {
            AuthenticationService.clearAuthentication();

            ActiveRoleService.clearActiveRole();
            SiteSearchService.clearActiveSearch();
            updateRootScope(null);

            AuthenticationService.authenticate().catch(function () {
                $state.go('login', {notify: false});
            });
        });

        // Called when user has logged out or cancelled login
        $rootScope.$on('event:auth-loginCancelled', function () {
            AuthenticationService.clearAuthentication();
            LoginRedirectService.clearPendingState();

            ActiveRoleService.clearActiveRole();
            SiteSearchService.clearActiveSearch();
            updateRootScope(null);
        });

        // Called after login is successful or when user was already authenticated on page load
        $rootScope.$on('event:auth-loginConfirmed', function (event, account) {
            AuthenticationService.setAuthentication(account);

            SiteSearchService.clearActiveSearch();
            ActiveRoleService.updateRoles(account);

            updateRootScope(account);

            var defaultEntryState = _.size(ActiveRoleService.getAvailableRoles()) > 1 ? 'roleselection' : 'main';

            LoginRedirectService.processPendingState(defaultEntryState, account.username);
        });

        function updateRootScope(account) {
            if (account) {
                $rootScope.authenticated = true;
                $rootScope.account = account;

            } else {
                $rootScope.authenticated = false;
                $rootScope.account = null;
            }
        }
    })

    .service('LoginRedirectService', function ($state) {
        var pendingState;
        var pendingStateParams;
        var pendingUsername;

        this.setPendingState = function (state, params, username) {
            if (state.authenticate !== false) {
                pendingState = state.name;
                pendingStateParams = params;
                pendingUsername = username;
            }
        };

        this.clearPendingState = function () {
            pendingState = null;
            pendingStateParams = null;
            pendingUsername = null;
        };

        this.processPendingState = function (defaultState, username) {
            if (pendingState) {
                // Do not process pending state if username has changed
                if (!pendingUsername || pendingUsername === username) {
                    $state.go(pendingState, pendingStateParams);
                } else {
                    $state.go(defaultState);
                }
                this.clearPendingState();

            } else {
                $state.go(defaultState);
            }
        };
    })

    .service('AuthenticationService', function ($http, $q, authService) {
        var authentication = null;

        this.isAuthenticated = function () {
            return authentication !== null;
        };

        this.getAuthentication = function () {
            return authentication;
        };

        this.setAuthentication = function (value) {
            authentication = value;
        };

        this.clearAuthentication = function () {
            authentication = null;
        };

        this.reloadAuthentication = function () {
            $http.get('/api/v1/account').success(function (a) {
                authentication = a;
            });
        };

        this.authenticate = function () {
            if (authentication !== null) {
                return $q.when(authentication);
            }

            // Check authentication, 200 = logged in and 401 = logged out
            return $http.get('/api/v1/account', {
                ignoreAuthModule: 'ignoreAuthModule'
            }).success(function (authentication) {
                authService.loginConfirmed(authentication);
            });
        };

        this.isCurrentPersonId = function (personId) {
            var accountPersonId = authentication ? authentication.personId : null;
            return accountPersonId === personId;
        };
    })

    .service('LoginService', function ($http, authService, MapState) {
        this.login = function (param) {
            // Spring Security Form Login request
            var data = "username=" + encodeURIComponent(param.username) +
                "&password=" + encodeURIComponent(param.password) +
                (param.otp ? "&otp=" + encodeURIComponent(param.otp) : "") +
                "&remember-me=" + param.rememberMe;

            return $http.post('/login', data, {
                // Do not buffer authentication request if response status is 401
                ignoreAuthModule: 'ignoreAuthModule',
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            }).success(function (response) {
                authService.loginConfirmed(response);
            });
        };

        this.logout = function () {
            return $http.post('/logout', {}).success(function () {
                authService.loginCancelled();
                MapState.reset();
            });
        };
    });
