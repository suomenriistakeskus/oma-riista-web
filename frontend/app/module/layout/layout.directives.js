'use strict';

angular.module('app.layout.directives', [])
    .directive('focusMe', function ($timeout) {
        return {
            link: function (scope, element, attrs) {
                $timeout(function () {
                    element[0].focus();
                });
            }
        };
    })

    .directive('autoScrollTo', function () {
        return function (scope, element, attrs) {
            var firstScroll = true;

            scope.$watch(attrs.autoScrollTo, function (value) {
                var $ = angular.element;

                if (value !== undefined && value !== null) {// if (value) won't scroll when value=0
                    var parentElement = $(element);
                    var childElementId = "#" + attrs.prefixId + value;
                    var childElement = $(childElementId, parentElement);

                    if (!childElement.length) {
                        console.warn('Could not find element with id', childElementId);
                        return;
                    }

                    var childPos = childElement.position();
                    var parentPos = parentElement.position();
                    var parentScrollTop = parentElement.scrollTop();
                    var pos = childPos.top + parentScrollTop - parentPos.top;

                    if (firstScroll) {
                        parentElement.animate({scrollTop: pos}, 0);
                        firstScroll = false;
                    } else {
                        parentElement.animate({scrollTop: pos}, 300);
                    }
                }
            });
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
                    if ($state.current.wideLayout === true ||
                        $state.current.hideFooter === true) {
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
            template: '<a class="btn-modal-close" aria-hidden="true" ng-click="$dismiss(\'cancel\')">' +
            '<span class="fa fa-close"></span></a>',
            replace: true
        };
    })


    .directive('activeIfStateIncludes', function ($state, $timeout) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                var activeStates = attrs.activeIfStateIncludes.split(',');

                $scope.$on('$stateChangeSuccess', updateActiveState);
                $timeout(updateActiveState, 0);

                function updateActiveState() {
                    var active = false;
                    for (var i = 0; i < activeStates.length; i++) {
                        if ($state.includes(activeStates[i].trim())) {
                            active = true;
                        }
                    }
                    if (active) {
                        element.addClass('active');
                    } else {
                        element.removeClass('active');
                    }
                }
            }
        };
    });
