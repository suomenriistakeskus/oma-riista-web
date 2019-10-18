'use strict';

angular.module('app.config.router', [])

    .config(function ($urlRouterProvider, $locationProvider) {
        $urlRouterProvider.otherwise(function ($injector) {
            var $state = $injector.get('$state');
            $state.go('login');
        });

        // Without server side support html5 must be disabled.
        $locationProvider.html5Mode(false);
        $locationProvider.hashPrefix('');

        // Endpoint for simple page refresh using fragment
        $urlRouterProvider.when('/restart', function ($injector) {
            var rev = $injector.get('appRevision');
            window.location.href = '/?rev=' + rev + '#/login';
            return true;
        });
    })

    .config(function ($provide) {
        $provide.decorator('$uiViewScroll', function ($delegate, $window, $timeout) {
            return function (uiViewElement) {
                return $timeout(function () {
                    $window.scrollTo(0, 0);
                }, 0, false);
            };
        });
    })

    .run(function ($rootScope, $log, $state, $uiViewScroll,
                   NotificationService, UnsavedChangesConfirmationService) {

        $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
            UnsavedChangesConfirmationService.checkEvent(event);

            if (toState && fromState && toState.name !== fromState.name) {
                // scroll to top when state changes
                $uiViewScroll();
            }
        });

        $rootScope.$on('$stateNotFound', function (event, unfoundState, fromState, fromParams) {
            event.preventDefault();

            $log.error('State was not found:', unfoundState.to, unfoundState.toParams);

            displayNavigationError();
        });

        $rootScope.$on('$stateChangeError', function (event, toState, toParams, fromState, fromParams, rejectionError) {
            event.preventDefault();

            $log.error('Could not transition to UI router state: ' + toState.name);

            if (_.isError(rejectionError)) {
                Raven.captureException(rejectionError);
            }

            if (toState.authenticate !== false) {
                $log.error('$stateChangeError -> roleselection', rejectionError);
                $state.go('roleselection');
                displayNavigationError();

            } else {
                $log.error('$stateChangeError -> no action', rejectionError);
            }
        });

        function displayNavigationError() {
            NotificationService.showMessage("Sivun lataaminen epäonnistui ja ongelmasta on lähetetty ylläpidolle viesti." +
                " Mikäli sivuston lataus uudelleen (F5) ei auta, niin kyseessä on todennäköisesti järjestelmävirhe.",
                "error", {ttl: -1, translateMessage: false});
        }
    });
