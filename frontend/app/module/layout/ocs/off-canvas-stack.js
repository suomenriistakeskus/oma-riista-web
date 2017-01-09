/*global angular:true, browser:true */

(function () {
    'use strict';

    var m = angular.module('sprout.off-canvas-stack', [
        'ui.router'
    ]);

    m.run(function ($rootScope, offCanvasStack) {
        $rootScope.$on("$stateChangeSuccess", function () {
            offCanvasStack.onBackNavigation();
        });
    });

    /**
     * Service to show multiple off canvas menus / dialogs so that they
     * will be stacked one on top of another.
     *
     * To customize styles§ add your specific class to
     * <off-canvas-container class="custom-styles"></off-canvas-container>
     * element and add CSS of your preference.
     *
     * https://medium.com/@mibosc/responsive-design-why-and-how-we-ditched-the-good-old-select-element-bc190d62eff5
     */

    /**
     * API to create / close off canvas dialogs
     */
    m.factory('offCanvasStack', ['$q', '$document', '$rootScope', '$controller', '$resolve', '_offCanvasDialogStacks',
        function ($q, $document, $rootScope, $controller, $resolve, _offCanvasDialogStacks) {
            function _getTopDialog() {
                if ('default' in _offCanvasDialogStacks) {
                    var last = _.last(_offCanvasDialogStacks['default']);

                    return last ? last.dialogInstance : null;
                }

                return null;
            }

            $document.bind('keydown', function (evt) {
                if (evt.which === 27) {
                    var dialogInstance = _getTopDialog();

                    if (dialogInstance) {
                        evt.preventDefault();

                        $rootScope.$apply(function () {
                            dialogInstance.dismiss('escape');
                        });
                    }
                }
            });

            return {
                onBackNavigation: function () {
                    var dialogInstance = _getTopDialog();

                    if (dialogInstance) {
                        dialogInstance.dismiss('back');
                    }
                },

                /**
                 * Opens new page to off-canvas dialog stack.
                 *
                 * Following functions are exposed to every dialog scope
                 * `$close(result)`, `$dismiss(err)` to allow very light syntax
                 * for writing dialog pages without specifying controller / scope.
                 *
                 * @param {{
                 *   templateUrl : String,
                 *   controller : String|Array|Function
                 *   scope : Object=,
                 *   resolve : Object=
                 * }} dialogOptions Params how to open dialog
                 *
                 *  @param stackName {String=} Optional parameter to tell for which dialog
                 *   stack should be opened.
                 *
                 * @return {{
                 *   close: function(*),
                 *   dismiss : function(*),
                 *   opened : Promise,
                 *   result : Promise
                 * }} Similar structure that bootstrap UI $modal returns.
                 * */
                open: function (dialogOptions, stackName) {
                    stackName = _.isString(stackName) ? stackName : 'default';

                    if (!_offCanvasDialogStacks[stackName]) {
                        _offCanvasDialogStacks[stackName] = [];
                    }
                    var stackRef = _offCanvasDialogStacks[stackName];

                    if (!dialogOptions.templateUrl) {
                        throw new Error("Error: templateUrl is required parameter.");
                    }

                    var stackIndex = stackRef.length;

                    // initialize template / controller / scope (heavily inspired by bootstrap-ui $modal)
                    var dialogResultDeferred = $q.defer();
                    var dialogOpenedDeferred = $q.defer();

                    //prepare an instance of a modal to be injected into controllers and returned to a caller

                    /**
                     * Public API for dialog.
                     *
                     * This is actually returned from OffCanvasStack.open() method.
                     *
                     * @type {{
                     *   result: Promise,
                     *   opened: Promise,
                     *   stackIndex: Number,
                     *   close: function(result),
                     *   dismiss: function(err)}}
                     */
                    var dialogInstance = {
                        result: dialogResultDeferred.promise,
                        opened: dialogOpenedDeferred.promise,
                        stackIndex: stackIndex,

                        /**
                         * Resolve this dialog and reject all child dialogs.
                         */
                        close: function (result) {
                            closeChildIfExist(this);
                            dialogResultDeferred.resolve(result);
                            freeDialogStackItem(this);
                        },

                        /**
                         * Dismiss this and all child dialogs.
                         */
                        dismiss: function (err) {
                            closeChildIfExist(this);
                            dialogResultDeferred.reject(err);
                            freeDialogStackItem(this);
                        }
                    };

                    function closeChildIfExist(dialogInstance) {
                        if (dialogInstance.stackIndex + 1 < stackRef.length) {
                            stackRef[dialogInstance.stackIndex + 1].dialogInstance
                                .dismiss(new Error("Parent dialog was closed."));
                        }
                    }

                    // pops item from top of dialog stack and checks that pop order is as expected
                    function freeDialogStackItem(dialogInstance) {
                        if (stackRef.length === dialogInstance.stackIndex + 1) {
                            stackRef.pop();
                        } else {
                            console.log("Internal implementation fail. " +
                                "This should be reported to off-canvas-stack maintainer. " +
                                "Trying to close dialogs in invalid order.");
                        }
                    }

                    var dialogScope = (dialogOptions.scope || $rootScope).$new();
                    dialogScope.$close = _.bind(dialogInstance.close, dialogInstance);
                    dialogScope.$dismiss = _.bind(dialogInstance.dismiss, dialogInstance);

                    // pushed controller template etc for rendering item...
                    // this structure is rendered in off-canvas-item directive
                    var scopeReady = false;
                    stackRef.push({
                        dialogInstance: dialogInstance,
                        renderOpts: {
                            scope: dialogScope,
                            templateUrl: dialogOptions.templateUrl,
                            isScopeReady: function () {
                                return scopeReady;
                            },
                            largeDialog: _.isBoolean(dialogOptions.largeDialog) ? dialogOptions.largeDialog : false
                        }
                    });

                    if (dialogOptions.controller) {
                        var toResolve = dialogOptions.resolve || {};

                        $resolve.resolve(toResolve, null, null, dialogOptions)
                            .then(function (resolutions) {
                                var ctrlLocals = angular.extend({}, resolutions);
                                ctrlLocals.$scope = dialogScope;
                                ctrlLocals.dialogOptions = dialogInstance;
                                $controller(dialogOptions.controller, ctrlLocals);
                                scopeReady = true;
                                dialogOpenedDeferred.resolve();
                            })
                            .catch(function (reason) {
                                // Reject both promises so errors wont be missed
                                dialogOpenedDeferred.reject(reason);

                                // Rejects result promise and removes from visible stack
                                dialogInstance.dismiss(reason);
                            });
                    } else {
                        scopeReady = true;
                        dialogOpenedDeferred.resolve();
                    }

                    return dialogInstance;
                }
            };
        }
    ]);

    /**
     * Named list of stacks in program. Named stack useful in case if one
     * needs multiple off-canvas dialog stacks at the same time.
     *
     * @type {{stackName : { dialogInstance: Object, renderOpts: Object }}}
     */
    m.value('_offCanvasDialogStacks', {"default": []});

    /**
     * <off-canvas-container> this is directive to which dialog pages are
     * loaded.
     *
     * By default this tag is styled to render as fixed element opening from
     * right side of window.
     *
     * @attribute stack-name {String} Name of container if multiple separate
     *     off canvas dialog stacks are needed.
     *     Stack-name can be given as 2nd parameter for
     *     `.open(params, stackName)` to tell where to open next dialog.
     *
     * Styles: `relative-layout` class can be applied to make canvas
     * to appear in relatively positioned div.
     *
     * Override any styles to change default behavior.
     */
    m.directive('offCanvasContainer', ['_offCanvasDialogStacks',
        function (_offCanvasDialogStacks) {
            return {
                templateUrl: 'layout/ocs/ocs.container.tpl.html',
                restrict: 'E',
                scope: {
                    stackName: "@"
                },

                link: function (scope, element, attrs) {
                    var stackName = scope.stackName || 'default';
                    if (!_offCanvasDialogStacks[stackName]) {
                        _offCanvasDialogStacks[stackName] = [];
                    }
                    // ng-repeat in template registers watch for stack...
                    scope.stack = _offCanvasDialogStacks[stackName];

                    // on scope destroy, cleanup also offCanvasDialogStack of the element
                    scope.$on('$destroy', function () {
                        scope.stack.splice(0, scope.stack.length);
                    });
                }
            };
        }
    ]);

    m.directive('offCanvasStackItem', [
        '$templateCache', '$compile',
        function ($templateCache, $compile) {
            return {
                template: '', // populated in link
                restrict: 'E',
                scope: {
                    item: '='
                },
                link: function (scope, element) {
                    var openParams = scope.item.renderOpts;
                    var template = $templateCache.get(openParams.templateUrl);
                    if (!_.isString(template) || template.length === 0) {
                        throw new Error('template "' + openParams.templateUrl + '" not found in $templateCache');
                    }
                    var dialogContentEl = angular.element(
                        '<div class="ocs-item-inner-wrapper">' + template + '</div>');
                    var linkTemplate = $compile(dialogContentEl);

                    var cancelReadyWatch, ensureNotInDOM;
                    cancelReadyWatch = ensureNotInDOM = scope.$watch('item.renderOpts.isScopeReady()', function (ready) {
                        if (ready) {
                            // The template must only be linked at this point, after the controller has been run,
                            // otherwise child controllers will be ran too early.
                            linkTemplate(openParams.scope);
                            element.append(dialogContentEl);
                            cancelReadyWatch();

                            // Get the element removed later on
                            ensureNotInDOM = function () {
                                element.empty();
                            };
                        }
                    });

                    scope.$on('$destroy', function () {
                        // Free item scope when item is removed and make sure nothing using the scope is or will get in the DOM.
                        openParams.scope.$destroy();
                        ensureNotInDOM();
                    });
                }
            };
        }
    ]);
})();
