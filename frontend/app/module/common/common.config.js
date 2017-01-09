'use strict';

angular.module('app.common.config', ['ui.router', 'angular-growl'])
    .run(function ($rootScope, $log, $state, $window, $location,
                   NotificationService, UnsavedChangesConfirmationService,
                   CacheFactory, appRevision) {
        $log.info("Application revision is", appRevision);

        $rootScope.$on('$stateNotFound', function (event, unfoundState, fromState, fromParams) {
            event.preventDefault();

            $log.error('State was not found:', unfoundState.to, unfoundState.toParams);

            NotificationService.showMessage("Navigointi virhe: " + unfoundState.to, "error");
        });

        $rootScope.$on('$stateChangeError', function (event, toState, toParams, fromState, fromParams, error) {
            event.preventDefault();

            $log.error("State change error: ", toState);
            $log.log(error);

            if (toState.name === 'registration-from-vetuma' || toState.name === 'main') {
                $state.go('login');
            } else {
                $state.go('main');
            }

            NotificationService.showMessage("Navigointi virhe: " + toState.name, "error");
        });

        $rootScope.$on('$stateChangeStart', UnsavedChangesConfirmationService.checkEvent);

        // Google Analytics
        $rootScope.$on('$stateChangeSuccess', function (event) {
            if (angular.isDefined($window.ga)) {
                $window.ga('send', 'pageview', $location.path());
            }
        });

        CacheFactory.createCache('diaryParameterCache', {
            storageMode: 'sessionStorage',
            maxAge: 30 * 60 * 1000, // 30 min
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('diarySrvaParameterCache', {
            storageMode: 'sessionStorage',
            maxAge: 30 * 60 * 1000, // 30 min
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('accountTodoCountCache', {
            storageMode: 'memory',
            maxAge: 2 * 1000, // 2 seconds
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('accountSrvaTodoCountCache', {
            storageMode: 'memory',
            maxAge: 2 * 1000, // 2 seconds
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('areasContactSearchCache', {
            storageMode: 'sessionStorage',
            maxAge: 30 * 60 * 1000, // 30 min
            deleteOnExpire: 'aggressive'
        });
    })

    .config(function ($compileProvider, isProductionEnvironment) {
        // https://docs.angularjs.org/guide/production
        $compileProvider.debugInfoEnabled(!isProductionEnvironment);
    })

    .config(function ($urlRouterProvider, $locationProvider) {
        $urlRouterProvider.otherwise('/login');

        // Without server side support html5 must be disabled.
        $locationProvider.html5Mode(false);

        // Endpoint for simple page refresh using fragment
        $urlRouterProvider.when('/restart', function ($match, $stateParams) {
            window.location = '#';
            window.location.reload();
            return true;
        });
    })

    .config(function ($translateProvider, versionUrlPrefix) {
        // Initialize angular-translate
        $translateProvider.useStaticFilesLoader({
            prefix: versionUrlPrefix + '/i18n/',
            suffix: '.json'
        });

        $translateProvider.preferredLanguage('fi');
        $translateProvider.useLocalStorage();
        $translateProvider.useMissingTranslationHandlerLog();
        $translateProvider.useSanitizeValueStrategy('sanitizeParameters');
    })

    .config(function (uibDatepickerConfig) {
        uibDatepickerConfig.showWeeks = false;
        uibDatepickerConfig.startingDay = 1;
    })

    .config(function (uibDatepickerPopupConfig) {
        uibDatepickerPopupConfig.showButtonBar = false;
        uibDatepickerPopupConfig.datepickerPopup = 'd.M.yyyy';
        uibDatepickerPopupConfig.altInputFormats = ['dd.MM.yyyy', 'yyyy-MM-dd'];
    })

    .config(function ($animateProvider) {
        $animateProvider.classNameFilter(/enable-ng-animate/);
    })

    .config(function (growlProvider) {
        // Default notification timeout
        growlProvider.globalTimeToLive(5000);
    })

    .run(function (blockUIConfig) {
        blockUIConfig.delay = 1;
        blockUIConfig.autoBlock = false;
    })

    .config(function ($httpProvider) {
        $httpProvider.interceptors.push(function ($q) {
            function shouldCacheBust(config) {
                return config && !config.cache && config.url &&
                    config.method === 'GET' &&
                    config.url.indexOf(".html") === -1 &&
                    config.url.indexOf("/i18n/") === -1;
            }

            return {
                'request': function (config) {
                    if (config && config.url && config.method && shouldCacheBust(config)) {
                        var ts = +(new Date());
                        // try replacing _= if it is there
                        var ret = config.url.replace(/(\?|&)_=.*?(&|$)/, "$1_=" + ts + "$2");
                        // if nothing was replaced, add timestamp to the end
                        config.url = ret + ((ret === config.url) ? (config.url.match(/\?/) ? "&" : "?") + "_=" + ts : "");
                    }

                    return config || $q.when(config);
                }
            };
        });
    })
    .config(function(KeepaliveProvider, IdleProvider, TitleProvider) {
        KeepaliveProvider.http('/api/ping');
        KeepaliveProvider.interval(5*60);
        IdleProvider.idle(25*60);
        IdleProvider.timeout(5*60);
        IdleProvider.windowInterrupt('focus');
        IdleProvider.keepalive(true);
    })
    .config(function ($httpProvider) {
        $httpProvider.interceptors.push(function ($q, NotificationService) {
            return {
                responseError: function responseError(event) {
                    if (event && event.status && event.status >= 500 && event.data && event.data.message) {
                        var message = "Palvelin virhe: " + event.data.message;

                        NotificationService.showMessage(message, "error");
                    }

                    return $q.reject(event);
                }
            };
        });
    })
    .config(function ($httpProvider) {
        $httpProvider.interceptors.push(function (NotificationService, appRevision, environmentId) {
            return {
                response: function (response) {
                    if (response.config.cache) {
                        return response;
                    }

                    var backendRevision = response.headers('X-Revision');

                    if (appRevision && backendRevision && appRevision !== backendRevision && environmentId !== 'dev') {
                        NotificationService.showMessage('global.messages.appRevisionChanged', "warn", {ttl: -1});
                    }

                    return response;
                }
            };
        });
    })
    .factory('$exceptionHandler', function ($injector, $log, isProductionEnvironment) {
        return function errorHandler(exception) {
            var errorMessage = exception.message ? exception.message : exception;

            $injector.get("NotificationService").showMessage("JavaScript virhe: " + errorMessage, "error", {
                'translateMessage': false
            });

            if (isProductionEnvironment || !_.isError(exception)) {
                $log.error(exception);

            } else {
                var rootScope = $injector.get('$rootScope');

                // Throwing exception during digest loop will be cause infinite recursion
                if (rootScope.$$phase) {
                    $log.error(exception);
                } else {
                    throw exception;
                }
            }
        };
    })
    .config(function ($uibModalProvider) {
        $uibModalProvider.options.backdrop = 'static';
    });
