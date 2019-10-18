'use strict';

angular.module('app.layout.services', ['angular-growl'])
    .run(function ($rootScope, NotificationService) {
        // Display all flash notifications after state has changed
        $rootScope.$on("$stateChangeSuccess", NotificationService.showFlashMessages);
    })

    .factory('NotificationService', function ($rootScope, $q, $timeout, growl) {
        var flashMessageQueue = [];

        function showFlashMessages() {
            // Message rendering must be deferred so that the layout has finished
            $timeout(function () {
                while (flashMessageQueue.length > 0) {
                    var item = flashMessageQueue.shift();

                    if (item) {
                        displayNotification(item.message, item.type, item.options);
                    }
                }
            }, 0);
        }

        function displayNotification(message, type, options) {
            if (type === 'success') {
                growl.success(message, options);
            } else if (type === 'warn') {
                growl.warning(message, options);
            } else if (type === 'info') {
                growl.info(message, options);
            } else {
                growl.error(message, options);
            }
        }

        var showDefaultSuccess = function () {
            $timeout(function () {
                displayNotification("global.messages.success", "success");
            }, 300);
        };

        var showDefaultFailure = function () {
            $timeout(function () {
                displayNotification("global.messages.error", "error");
            }, 300);
        };

        var showMessage = function (body, type, options) {
            $timeout(function () {
                displayNotification(body, type, options);
            }, 300);
        };

        var flashMessage = function (message, type, options) {
            flashMessageQueue.push({
                message: message,
                type: type || 'info',
                options: options || {}
            });
        };

        var handleModalPromise = function ($promise) {
            return $promise.then(function (result) {
                showDefaultSuccess();
                return result;

            }, function (err) {
                var errorsToIgnore = ['ignore', 'cancel', 'no', 'escape', 'escape key press', 'delete', 'back', 'previous'];

                if (!angular.isString(err) || errorsToIgnore.indexOf(err) < 0) {
                    var status = _.get(err, 'status');
                    var message = _.get(err, 'data.message');

                    var messageAlreadyCapturedByHttpInterceptor = status === 400 && message;

                    if (!messageAlreadyCapturedByHttpInterceptor) {
                        showDefaultFailure();
                    }
                }

                return $q.reject(err);
            });
        };

        // Public API
        return {
            showFlashMessages: showFlashMessages,
            flashMessage: flashMessage,
            showMessage: showMessage,
            showDefaultSuccess: showDefaultSuccess,
            showDefaultFailure: showDefaultFailure,
            handleModalPromise: handleModalPromise
        };
    })
    .service('FormSidebarService', function ($q, offCanvasStack, NotificationService) {
        function FormSidebar(options, repository, parametersToResolve) {
            this.currentItemId = null;
            this.currentModalInstance = null;
            this.options = options;
            this.repository = repository;
            this.parametersToResolve = parametersToResolve;
        }

        FormSidebar.prototype.open = function (modalOptions, itemId) {
            var self = this;
            var modalInstance = offCanvasStack.open(modalOptions);

            modalInstance.opened.then(function () {
                self.currentModalInstance = modalInstance;
                self.currentItemId = itemId;
            });

            modalInstance.result.finally(function (err) {
                self.currentModalInstance = null;
                self.currentItemId = null;
            });

            if (!self.repository) {
                return modalInstance.result;
            }

            return modalInstance.result.then(function (result) {
                var saveMethod = result.id ? self.repository.update : self.repository.save;

                return saveMethod(result).$promise.then(function (result) {
                    NotificationService.showDefaultSuccess();
                    return result;

                }, function (err) {
                    NotificationService.showDefaultFailure();
                    return $q.reject(err);
                });
            });
        };

        FormSidebar.prototype.show = function (parameters) {
            var self = this;
            var itemId = parameters.id || 'new';

            if (self.currentItemId && angular.equals(self.currentItemId, itemId)) {
                return $q.reject('ignore');
            } else {
                self.currentItemId = itemId;
            }

            if (self.currentModalInstance) {
                self.currentModalInstance.dismiss('previous');
                self.currentModalInstance = null;
            }

            var modalOptions = angular.copy(self.options);
            angular.extend(modalOptions.resolve, self.parametersToResolve(parameters));

            return self.open(modalOptions, itemId);
        };

        this.create = function (options, repository, parametersToResolve) {
            return new FormSidebar(options, repository, parametersToResolve);
        };
    })

    .service('TranslatedBlockUI', function ($translate, blockUI) {
        return {
            start: function (localisationKey) {
                return blockUI.start($translate.instant(localisationKey));
            },
            stop: function () {
                return blockUI.stop();
            },
            reset: function (executeCallbacks) {
                return blockUI.reset(executeCallbacks);
            },
            message: function (localisationKey) {
                return blockUI.message($translate.instant(localisationKey));
            },
            done: function (callback) {
                return blockUI.done(callback);
            }
        };
    });
