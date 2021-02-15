'use strict';

angular.module('app.login.services', [])
    .run(function ($rootScope, $state, $stateParams, httpBuffer,
                   AuthenticationService, ActiveRoleService, AvailableRoleService,
                   PendingRouterStateService, LoginRedirectService, SiteSearchService) {
        $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
            var pendingAuthentication = AuthenticationService.getAuthentication();
            var pendingUsername = pendingAuthentication ? pendingAuthentication.username : null;

            PendingRouterStateService.setPendingState(toState, toParams, pendingUsername);

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
            httpBuffer.rejectAll();

            AuthenticationService.clearAuthentication();

            ActiveRoleService.clearActiveRole();
            AvailableRoleService.clearAvailableRoles();
            SiteSearchService.clearActiveSearch();

            $state.go('login', {notify: false});
        });

        // Called when user has logged out or cancelled login
        $rootScope.$on('event:auth-loginCancelled', function () {
            AuthenticationService.clearAuthentication();
            PendingRouterStateService.clearPendingState();

            ActiveRoleService.clearActiveRole();
            AvailableRoleService.clearAvailableRoles();
            SiteSearchService.clearActiveSearch();
        });

        // Called after login is successful or when user was already authenticated on page load
        $rootScope.$on('event:auth-loginConfirmed', function (event, account) {
            AuthenticationService.setAuthentication(account);

            var pendingState = PendingRouterStateService.getPendingState(account.username);

            PendingRouterStateService.clearPendingState();
            AvailableRoleService.updateAvailableRoles(account);

            if (pendingState) {
                LoginRedirectService.redirectToPendingState(pendingState.name, pendingState.params);
            } else {
                LoginRedirectService.redirectToDefault();
            }
        });
    })

    .service('PendingRouterStateService', function () {
        var pendingState;
        var pendingStateParams;
        var pendingUsername;

        this.setPendingState = function (state, params, username) {
            if (state.authenticate !== false && state.name !== 'roleselection') {
                pendingState = state.name;
                pendingStateParams = params;
                pendingUsername = username;
            }
        };

        this.getPendingState = function (username) {
            if (!pendingState) {
                return null;
            }

            if (_.isString(pendingUsername) && _.isString(username) && pendingUsername !== username) {
                return null;
            }

            return {
                name: pendingState,
                params: pendingStateParams || {}
            };
        };

        this.clearPendingState = function () {
            pendingState = null;
            pendingStateParams = null;
            pendingUsername = null;
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

            if (Raven && _.isFunction(Raven.setUserContext)) {
                Raven.setUserContext(_.pick(value, ['id', 'role', 'personId']));
            }
        };

        this.clearAuthentication = function () {
            authentication = null;

            if (Raven && _.isFunction(Raven.setUserContext)) {
                Raven.setUserContext();
            }
        };

        this.reloadAuthentication = function () {
            $http.get('/api/v1/account').then(function (response) {
                authentication = response.data;
            });
        };

        this.authenticate = function () {
            // Check authentication, 200 = logged in and 401 = logged out
            return $http.get('/api/v1/account', {
                ignoreAuthModule: 'ignoreAuthModule'
            }).then(function (response) {
                authService.loginConfirmed(response.data);
            });
        };

        this.isCurrentPersonId = function (personId) {
            var accountPersonId = authentication ? authentication.personId : null;
            return accountPersonId === personId;
        };

        this.isCarnivoreAuthority = function () {
            return authentication && _.some(authentication.occupations, function (occupation) {
                return occupation.occupationType === 'PETOYHDYSHENKILO';
            });
        };

        this.isDeerPilotUser = function() {
            return authentication && authentication.deerPilotUser;
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
            }).then(function (response) {
                authService.loginConfirmed(response.data);
            });
        };

        this.logout = function () {
            return $http.post('/logout', {}).then(function (response) {
                authService.loginCancelled('logout', response);
                MapState.reset();
            });
        };
    });
