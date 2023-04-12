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

            if(window.DD_LOGS) {
                window.DD_LOGS.logger.error(exception.name + ': ' + exception.message, {
                    _customDataHolder: {
                        stack: exception.stack
                    }
                });
            }

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
    });
