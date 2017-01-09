'use strict';

angular.module('app.layout.directives', [])
    .directive('focusMe', function ($timeout, $parse) {
        return {
            link: function (scope, element, attrs) {
                var model = $parse(attrs.focusMe);
                scope.$watch(model, function (value) {
                    if (value === true) {
                        $timeout(function () {
                            element[0].focus();
                        });
                    }
                });
            }
        };
    })

    .directive('riistaContainerCss', function ($state) {
        return {
            restrict: 'A',
            controller: function ($scope, $element) {
                $scope.$on('$stateChangeSuccess', function () {
                    var useWideLayout = $state.current.wideLayout === true;

                    var add = useWideLayout ? 'container-fluid' : 'container';
                    var remove = useWideLayout ? 'container' : 'container-fluid';

                    $element.addClass(add);
                    $element.removeClass(remove);
                });
            }
        };
    })

    .directive('riistaFooterCss', function ($state) {
        return {
            restrict: 'A',
            controller: function ($scope, $element) {
                $scope.$on('$stateChangeSuccess', function () {
                    if ($state.current.wideLayout === true) {
                        $element.addClass('active');
                    } else {
                        $element.removeClass('active');
                    }
                });
            }
        };
    })

    .directive('riistaSidebarCollapse', function ($window, $document, $rootScope) {
        var globalState = {
            fullScreen: false,
            toggle: function () {
                this.fullScreen = !this.fullScreen;
            }
        };

        function updateLayout(elements, state) {
            if (state.fullScreen) {
                elements.sidebar().addClass('out');
                elements.mainColumn().addClass('in');
                elements.siteNav().css('margin-top', -elements.siteNav().height());
            } else {
                elements.sidebar().removeClass('out');
                elements.mainColumn().removeClass('in');
                elements.siteNav().css('margin-top', '');
            }

            recalculateLayout();
        }

        function recalculateLayout() {
            angular.element($window).triggerHandler('resize');
            $rootScope.$broadcast('invalidateSize');
        }

        return {
            restrict: 'E',
            priority: -100,
            template: "<div class='close-button'><a ng-click='$ctrl.toggleSidebar()'>" +
            "<span class='glyphicon glyphicon-resize-small' ng-show='$ctrl.isFullScreen()'></span>" +
            "<span class='glyphicon glyphicon-resize-full' ng-show='!$ctrl.isFullScreen()'></span></a></div>",
            link: function ($scope, element, attrs) {
                var parentEl = element.parent();

                $scope.elements = {
                    siteNav: function () {
                        return $document.find('.r-main-navbar');
                    },
                    mainColumn: function () {
                        return parentEl.find('.main-column');
                    },
                    sidebar: function () {
                        return parentEl.find('.left-column');
                    }
                };

                updateLayout($scope.elements, globalState);
            },
            scope: true,
            controllerAs: '$ctrl',
            controller: function ($scope) {
                var $ctrl = this;

                $ctrl.isFullScreen = function () {
                    return globalState.fullScreen;
                };

                $ctrl.toggleSidebar = function () {
                    globalState.toggle();
                    updateLayout($scope.elements, globalState);
                };

                $scope.$on('$destroy', function () {
                    if (globalState.fullScreen) {
                        $ctrl.toggleSidebar();
                    }
                });
            }
        };
    })

    .directive('rViewportHeight', function ($window) {
        return {
            scope: false,
            restrict: 'A',
            link: function (scope, element, attrs) {
                var $w = angular.element($window);
                var $el = angular.element(element);

                function _resizeElement() {
                    var windowHeight = $window.innerHeight;
                    var topNavHeight = $el.offset().top;

                    $el.height(windowHeight - topNavHeight);
                }

                $w.bind('resize', _resizeElement);
                _resizeElement();
            }
        };
    })

    .directive('riistaModalClose', function () {
        return {
            restrict: 'A',
            template: '<button type="button" class="btn btn-info pull-right" aria-hidden="true" ng-click="$dismiss(\'cancel\')">' +
            '<span class="glyphicon glyphicon-eject"></span>&nbsp;&nbsp;&nbsp;' +
            '<span translate="global.button.close">Sulje</span></button>',
            replace: true
        };
    })


    .directive('activeIfStateIncludes', function ($state) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var activeStates = attrs.activeIfStateIncludes.split(',');

                scope.$on('$stateChangeSuccess', function () {
                    var active = false;
                    for (var i = 0; i < activeStates.length; i++) {
                        if ($state.includes(activeStates[i])) {
                            active = true;
                        }
                    }
                    if (active) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                });
            }
        };
    });
