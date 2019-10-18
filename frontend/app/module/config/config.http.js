'use strict';

angular.module('app.config.http', ['app.metadata'])

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

    .config(function ($httpProvider) {
        $httpProvider.interceptors.push(function ($q, NotificationService) {
            return {
                responseError: function (event) {
                    if (event && isHttpErrorStatus(event.status)) {
                        showErrorMessage(event, NotificationService);
                    }

                    return $q.reject(event);
                }
            };
        });

        function showErrorMessage(event, NotificationService) {
            try {
                if (event.status === 400 || event.status >= 500) {
                    showOtherServerError(event, NotificationService);

                } else if (event.status === 403 && !isIgnoreAuthModule(event)) {
                    showAccessForbiddenError(NotificationService);
                }
            } catch (e) {
            }
        }

        function showAccessForbiddenError(NotificationService) {
            var errorMessage = "Pääsy sivulle estettiin.";
            NotificationService.showMessage(errorMessage, 'error', {translateMessage: false});
        }

        function showOtherServerError(event, NotificationService) {
            var errorHttpMessage = event.data && event.data.message ? event.data.message : event.status;
            var errorMessage = "Toiminto epäonnistui. Palvelimen virheviesti: " + errorHttpMessage;

            NotificationService.showMessage(errorMessage, 'error', {translateMessage: false});
        }

        function isIgnoreAuthModule(event) {
            return event.config && !!event.config.ignoreAuthModule;
        }

        function isHttpErrorStatus(httpStatusCode) {
            return httpStatusCode < 200 || httpStatusCode >= 400;
        }
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
    });

