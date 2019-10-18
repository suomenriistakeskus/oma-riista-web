'use strict';

angular.module('app.common.directives', ['dialogs.main', 'app.metadata'])
    .directive('uibDatepickerPopup', function ($window, $filter) {
        // Extend external directive to support storing model value as ISO-8601 string date
        return {
            restrict: 'A',
            priority: 1,
            require: 'ngModel',
            link: function (scope, element, attr, ngModel) {
                var dateFilter = $filter('date');

                ngModel.$formatters.push(function (modelValue) {
                    if (angular.isString(modelValue)) {
                        var parsedMoment = $window.moment(modelValue, 'YYYY-MM-DD', true);

                        if (parsedMoment.isValid() && parsedMoment.year() > 1900) {
                            return parsedMoment.toDate();
                        }
                    }

                    return modelValue;
                });

                ngModel.$parsers.push(function (viewValue) {
                    if (angular.isDate(viewValue) && !isNaN(viewValue)) {
                        return dateFilter(viewValue, 'yyyy-MM-dd');
                    }
                    return viewValue;
                });
            }
        };
    })
    .directive('springCsrfCookie', function ($cookies) {
        return {
            replace: false,
            scope: false,
            restrict: 'A',
            link: function (scope, element, attrs) {
                function readXSRFCookieValue() {
                    return $cookies.get('XSRF-TOKEN');
                }

                element.attr('name', '_csrf');

                scope.$watch(readXSRFCookieValue, function (cookieValue) {
                    element.attr('value', cookieValue);
                });
            }
        };
    })

    .directive('panelToggle', function () {
        return {
            replace: false,
            scope: {
                panelToggle: '<'
            },
            restrict: 'A',
            link: function ($scope, element, attr) {
                attr.$addClass('glyphicon');

                var classArray = ['glyphicon-chevron-down','glyphicon-chevron-right'];

                $scope.$watch('panelToggle', function (toggle) {
                    attr.$addClass(toggle ? classArray[0] : classArray[1]);
                    attr.$removeClass(toggle ? classArray[1] : classArray[0]);
                });
            }
        };
    })

    .directive('ngConfirmClick',
        function (dialogs, $translate) {
            return {
                restrict: 'A',
                scope: false,
                replace: false,
                link: function (scope, element, attr) {
                    element.bind('click', function ($event) {
                        $event.stopPropagation();

                        var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                        var dialogMessage = attr.ngConfirmClick || $translate.instant('global.dialog.confirmation.text');
                        var dialog = dialogs.confirm(dialogTitle, dialogMessage);

                        dialog.result.then(function () {
                            scope.$eval(attr.ngConfirmClickAction);
                        });
                    });
                }
            };
        }
    )

    .directive('checkPwStrength', function () {
        return {
            replace: false,
            restrict: 'A',
            scope: {model: '=checkPwStrength'},
            link: function (scope, element, attrs) {
                var strength = {
                    colors: ['#F00', '#F90', '#FF0', '#9F0', '#0F0'],
                    mesureStrength: function (val) {
                        var totalStrength = 0;
                        if (val.length >= 8) {
                            // has two lower case letters?
                            var checkLowerCase = new RegExp(".*[a-zåäö].*[a-zåäö].*");
                            // has two upper case letters?
                            var checkUpperCase = new RegExp(".*[A-ZÅÄÖ].*[A-ZÅÄÖ].*");
                            // has at least one digit ?
                            var checkDigits = new RegExp(".*[0-9].*");
                            // has at least one special character ?
                            var checkSpecialChar = new RegExp(".*[!@#$%&/{}()=?+*,.].*");

                            // bonus for extra length
                            totalStrength = Math.floor(Math.max((val.length - 8), 0) * 0.2);
                            var hasLower = checkLowerCase.test(val);
                            var hasUpper = checkUpperCase.test(val);
                            var hasDigits = checkDigits.test(val);
                            var hasSpecial = checkSpecialChar.test(val);
                            if (hasUpper && hasLower) {
                                totalStrength += 1;
                            }
                            if (hasDigits && (hasLower || hasUpper)) {
                                totalStrength += 1;
                            }
                            if (hasSpecial) {
                                totalStrength += 1;
                            }
                        }
                        return totalStrength;
                    },
                    getColor: function (s) {
                        var idx = Math.min(s, 4);
                        return {idx: idx + 1, col: this.colors[idx]};
                    }
                };
                scope.$watch('model', function (newValue, oldValue) {
                    if (!newValue || newValue === '') {
                        element.css({"display": "none"});
                    } else {
                        var s = strength.mesureStrength(newValue);
                        var c = strength.getColor(s);
                        element.css({"display": "block"});
                        element.children('li')
                            .css({"background": "#DDD"})
                            .slice(0, c.idx)
                            .css({"background": c.col});
                    }
                });
            },
            template: '<li class="r-pw-point"></li><li class="r-pw-point"></li><li class="r-pw-point"></li><li class="r-pw-point"></li><li class="r-pw-point r-pw-point-last"></li>'
        };
    })

    .service('EnvironmentAwareShowDirectoryFactory',
        function (isProductionEnvironment, $animate) {
            var create = function (expected, directiveName) {
                return function (scope, element, attr) {
                    scope.$watch(attr[directiveName], function (value) {
                        if (angular.isUndefined(value)) {
                            value = true;
                        }
                        if (isProductionEnvironment === expected && value) {
                            $animate.removeClass(element, 'hidden');
                        } else {
                            $animate.addClass(element, 'hidden');
                        }
                    });
                };
            };
            return {create: create};
        })

    .directive('showInProduction', function (EnvironmentAwareShowDirectoryFactory) {
        return EnvironmentAwareShowDirectoryFactory.create(true, 'showInProduction');
    })

    .directive('showInDevelopment', function (EnvironmentAwareShowDirectoryFactory) {
        return EnvironmentAwareShowDirectoryFactory.create(false, 'showInDevelopment');
    })

    .directive('ie10OptgroupFix', function () {
        // IE10 optgroup do not work as you might expect. There needs
        // to be non-empty value attribute, otherwise the first element
        // of first optgroup is invalid, second from second optgroup is invalid ...
        //
        // we bind the fix to click, because when this is run the data (and optgroups) might not be there
        var link = function (scope, element, attr) {
            var fixOptGroup = function () {
                if (!element || !element.context) {
                    return;
                }
                _.forEach(element.context.children, function (e) {
                    if (e.tagName === 'OPTGROUP') {
                        angular.element(e).attr('value', '*');
                    }
                });
            };
            element.bind('click', _.once(fixOptGroup));
        };

        return {
            link: link,
            restrict: 'A'
        };
    })

    .directive('nameTranslatedInput', function ($filter) {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: false,
            link: function (scope, element, attrs, ngModelController) {
                var i18NFilter = $filter('rI18nNameFilter');

                ngModelController.$formatters.push(function (data) {
                    return i18NFilter(data);
                });
            }
        };
    })

    .directive('nameTranslated', function ($filter) {
        return {
            restrict: 'A',
            scope: {nameTranslated: '&'},
            link: function (scope, element, attrs) {
                var i18NFilter = $filter('rI18nNameFilter');
                var modelValue = scope.nameTranslated();

                element.html(i18NFilter(modelValue) || '?');
            }
        };
    })

    .directive('rClickSelect', function () {
        return {
            restrict: 'AC',
            link: function (scope, element, attrs) {
                element.bind('click', function () {
                    element.select();
                });
            }
        };
    })

    .directive('rBinocularsSymbol', function () {
        return {
            restrict: 'EA',
            scope: false,
            replace: true,
            template: '<span class="r-gamediary-binoculars"><span class="fa fa-binoculars"></span></span>'
        };
    })
    .directive('rForceShowErrorsCheckValidity', function ($timeout) {
        return {
            replace: false,
            restrict: 'A',
            require: 'form',
            scope: false,
            link: function (scope, element, attrs) {
                var timer = null;
                var _stop = function () {
                    if (timer) {
                        $timeout.cancel(timer);
                        timer = null;
                    }
                };
                var _forceCheck = function () {
                    scope.$broadcast('show-errors-check-validity');
                    timer = $timeout(_forceCheck, 500);
                };
                timer = $timeout(_forceCheck, 200);
                scope.$on('$destroy', _stop);
            }
        };
    })
    .directive('rWithTooltip', function ($translate) {
        /**
         * Renders content with tooltips.
         *
         * For content give
         * - bind-content which two way binded
         * - content-val which is content value used as is
         * - or content-key which is translation key for content
         *
         * For tooltip give
         * - tooltip-val which is tooltip value used as is
         * - or tooltip-key which is translation key for tooltip
         */
        return {
            replace: true,
            restrict: 'E',
            scope: {bindContent: '@?'},
            link: function (scope, element, attrs) {
                if (_.isUndefined(scope.bindContent)) {
                    scope.content = attrs.contentVal || $translate.instant(attrs.contentKey);
                } else {
                    scope.content = scope.bindContent;
                }
                scope.tooltip = attrs.tooltipVal || $translate.instant(attrs.tooltipKey);
            },
            template: '<div>' +
            '<div ng-if="!bindContent" uib-tooltip="{{tooltip}}" ng-bind-html="content" tooltip-popup-delay="0" tooltip-placement="left"></div>' +
            '<div ng-if="bindContent" uib-tooltip="{{tooltip}}" ng-bind="bindContent" tooltip-popup-delay="0" tooltip-placement="left"></div>' +
            '</div>'
        };
    })
    .directive('rCopyOnBlurToEmptyInput', function () {
        return {
            restrict: 'A',
            scope: false,
            require: ['^^form', '^ngModel'],
            link: function (scope, element, attrs, controllers) {
                var formController = controllers[0];
                var modelController = controllers[1];

                var input = attrs.rCopyOnBlurToEmptyInput;

                if (input) {
                    element.on('blur', function (event) {
                        var inputViewValue = modelController.$viewValue;
                        var otherInputModelController = formController[input];

                        if (angular.isObject(otherInputModelController)) {
                            if (inputViewValue && _.isEmpty(otherInputModelController.$viewValue)) {
                                otherInputModelController.$setViewValue(inputViewValue);
                                otherInputModelController.$render();
                            }
                        }
                    });
                }
            }
        };
    })
    .directive('rCopyZeroOnBlurToEmptyInput', function () {
        return {
            restrict: 'A',
            priority: 1,
            scope: false,
            require: ['^^form', '^ngModel'],
            link: function (scope, element, attrs, controllers) {
                var formController = controllers[0];
                var modelController = controllers[1];

                var targetInput = attrs.rCopyZeroOnBlurToEmptyInput;

                if (targetInput) {
                    element.on('blur', function (event) {
                        if (modelController.$viewValue === "0") {
                            var targetInputModelController = formController[targetInput];

                            if (angular.isObject(targetInputModelController)) {
                                if (_.isEmpty(targetInputModelController.$viewValue)) {
                                    targetInputModelController.$setViewValue("0");
                                    targetInputModelController.$render();
                                }
                            }
                        }
                    });
                }
            }
        };
    })
    .directive('numItems', function ($parse) {
        // right-aligned number of items with unit
        return {
            restrict: 'A',
            replace: false,
            scope: true,
            link: function (scope, element, attrs) {
                scope.items = $parse(attrs.numItems)(scope);

                if (_.isFinite(scope.items)) {
                    if (_.isString(attrs.unitKey)) {
                        scope.unitKey = attrs.unitKey;
                    } else {
                        scope.unit = attrs.unit;
                    }
                } else {
                    scope.items = '-';
                    scope.unit = '';
                }
            },
            template: '' +
            '<div class="text-right">' +
            '  <span ng-bind="::items"></span>&nbsp;<span ng-switch="::!!unitKey" class="unit-suffix">' +
            '    <span ng-switch-when="true">{{unitKey | translate}}</span>' +
            '    <span ng-switch-when="false">{{::unit}}</span>' +
            '  </span>' +
            '</div>'
        };
    })
    .directive('numPieces', function ($compile) {
        // right-aligned number of pieces
        return {
            restrict: 'A',
            priority: 10000,
            terminal: true,
            link: function (scope, element, attrs) {
                attrs.$set('numItems', attrs.numPieces);
                attrs.$set('numPieces', null);
                attrs.$set('unitKey', 'global.pcs');
                $compile(element)(scope);
            }
        };
    })
    .directive('numPersons', function ($compile) {
        // right-aligned number of persons
        return {
            restrict: 'A',
            priority: 10000,
            terminal: true,
            link: function (scope, element, attrs) {
                attrs.$set('numItems', attrs.numPersons);
                attrs.$set('numPersons', null);
                attrs.$set('unitKey', 'global.personUnit');
                $compile(element)(scope);
            }
        };
    })
    .directive('moneySum', function ($parse) {
        // right-aligned sum of money
        return {
            restrict: 'A',
            replace: false,
            scope: true,
            link: function (scope, element, attrs) {
                scope.sum = $parse(attrs.moneySum)(scope);
                scope.isFinite = _.isFinite(scope.sum);
            },
            template: '' +
            '<div ng-if="::isFinite" class="text-right"><span ng-bind="::(sum | number : 2)"></span>&nbsp;<span class="unit-suffix">&euro;</span></div>' +
            '<div ng-if="::!isFinite" class="text-right"><span>-</span>&nbsp;<span class="unit-suffix"></span></div>'
        };
    })
;
