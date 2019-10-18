'use strict';

angular.module('app.config.error', ['app.metadata'])
    .factory('$exceptionHandler', function ($injector, $log, isProductionEnvironment) {
        var totalErrorCount = 0;

        return function (exception, cause) {
            if (++totalErrorCount > 50) {
                return;
            }

            if (isUnhandledRejectionError(exception)) {
                $log.warn(exception);
                return;
            }

            showErrorNotification(exception);

            Raven.captureException(exception, {
                extra: {cause: cause}
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

        function showErrorNotification(exception) {
            try {
                var NotificationService = $injector.get("NotificationService");
                var errorMessage = "Tapahtui odottamaton sovellusvirhe. " + (exception.message ? exception.message : '');

                NotificationService.showMessage(errorMessage, "error", {
                    translateMessage: false
                });
            } catch (ignore) {
            }
        }

        function isUnhandledRejectionError(error) {
            return _.isString(error) && error.indexOf('Possibly unhandled rejection') !== -1;
        }
    })

    .run(function () {
        var angularPattern = /^\[((?:[$a-zA-Z0-9]+:)?(?:[$a-zA-Z0-9]+))\] (.*?)\n?(\S+)$/;

        // source: https://github.com/getsentry/sentry-javascript/blob/master/packages/raven-js/plugins/angular.js
        Raven.setDataCallback(function (data) {
            var exception = data.exception;

            if (exception) {
                exception = exception.values[0];
                var matches = angularPattern.exec(exception.value);

                if (matches) {
                    // This type now becomes something like: $rootScope:inprog
                    exception.type = matches[1];
                    exception.value = matches[2];

                    data.message = exception.type + ': ' + exception.value;
                    // auto set a new tag specifically for the angular error url
                    data.extra.angularDocs = matches[3].substr(0, 250);
                }
            }

            return data;
        });
    });
