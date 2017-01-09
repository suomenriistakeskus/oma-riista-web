'use strict';

angular.module('app.common.directives', ['dialogs.main'])
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
    .directive('springCsrfCookie', function($cookies) {
        return {
            replace: false,
            scope: false,
            restrict: 'A',
            link: function(scope, element, attrs) {
                function readXSRFCookieValue() {
                    return $cookies.get('XSRF-TOKEN');
                }

                element.attr('name', '_csrf');

                scope.$watch(readXSRFCookieValue, function(cookieValue) {
                    element.attr('value', cookieValue);
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
            scope: { model: '=checkPwStrength' },
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
                        return { idx: idx + 1, col: this.colors[idx] };
                    }
                };
                scope.$watch('model', function (newValue, oldValue) {
                    if (!newValue || newValue === '') {
                        element.css({ "display": "none"  });
                    } else {
                        var s = strength.mesureStrength(newValue);
                        var c = strength.getColor(s);
                        element.css({ "display": "block" });
                        element.children('li')
                            .css({ "background": "#DDD" })
                            .slice(0, c.idx)
                            .css({ "background": c.col });
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
            return { create: create };
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
                _.each(element.context.children, function (e) {
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

                ngModelController.$formatters.push(function(data) {
                    return i18NFilter(data);
                });
            }
        };
    })

    .directive('nameTranslated', function ($filter) {
        return {
            restrict: 'A',
            scope: { nameTranslated: '&' },
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
    .directive('rHarvestReportSpecimenOrPermittedSpecies', function ($parse) {
        return {
            replace: false,
            restrict: 'A',
            scope: false,
            link: function (scope, element, attrs) {
                scope.report = $parse(attrs.rHarvestReportSpecimenOrPermittedSpecies)(scope);
            },
            templateUrl: 'common/r-harvest-report-specimen-or-permitted-species.html'
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
    .component('rShowEntrySpecimens', {
        templateUrl: 'common/r-show-entry-specimens.html',
        bindings: {
            entry: '<'
        }
    })
    .component('rShowHarvestSpecimens', {
        templateUrl: 'common/r-show-harvest-specimens.html',
        bindings: {
            entry: '<'
        }
    })
    .component('rShowObservationSpecimens', {
        templateUrl: 'common/r-show-observation-specimens.html',
        bindings: {
            entry: '<'
        },
        controller: function (ObservationFieldsMetadata) {
            var $ctrl = this;
            var e = $ctrl.entry;
            if (e.isObservation()) {
                ObservationFieldsMetadata
                    .forSpecies({gameSpeciesCode: e.gameSpeciesCode}).$promise
                    .then(function (metadata) {
                        var fieldRequirements = metadata.getFieldRequirements(e.withinMooseHunting, e.observationType);

                        $ctrl.isGenderVisible = fieldRequirements.isFieldLegal('gender');
                        $ctrl.isAgeVisible = fieldRequirements.isFieldLegal('age');
                        $ctrl.isStateVisible = fieldRequirements.isFieldLegal('state');
                        $ctrl.isMarkingVisible = fieldRequirements.isFieldLegal('marking');
                    });
            }

            $ctrl.showMooselikeObservationAmounts = function () {
                return e.isObservation() && (e.isMoose() || e.isMooselike()) && e.withinMooseHunting;
            };
        }
    })
    .component('rShowSrvaSpecimens', {
        templateUrl: 'common/r-show-srva-specimens.html',
        bindings: {
            entry: '<'
        }
    })
    .directive('rDownloadPdf', function () {
        return {
            replace: false,
            restrict: 'A',
            scope: { pdfUrl: '=rDownloadPdf' },
            templateUrl: 'common/r-download-pdf.html'
        };
    })
    .directive('rDownloadXml', function () {
        return {
            replace: false,
            restrict: 'A',
            scope: { xmlUrl: '=rDownloadXml' },
            templateUrl: 'common/r-download-xml.html'
        };
    })
    .directive('rDownloadFile', function () {
        return {
            replace: false,
            restrict: 'A',
            scope: { fileUrl: '=rDownloadFile', translateKey: '=rDownloadFileTranslate'},
            templateUrl: 'common/r-download-file.html'
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
;
