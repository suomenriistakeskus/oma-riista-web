describe('Off canvas dialog stack', function() {

    beforeEach(module('sprout.off-canvas-stack'));
    beforeEach(module('templates'));
    //beforeEach(module('off-canvas-stack/ocs.container.tpl.html'));

    var $rootScope, $scope, $compile, jq, OffCanvasStack, OffCanvasDialogStacks;

    beforeEach(
        inject(function($templateCache) {

            // static template without data bindings
            $templateCache.put("ocs.test.template1.html", [
                "<div class='template1'>",
                "  <h1>I'm #1</h1>",
                "</div>"].join('\n'));

            // renders scope.content and gets result for close button from
            // scope.result and scope.fail for dismiss
            $templateCache.put("ocs.test.template2.html", [
                "<div class='template2'>",
                "  <h2>{{content}}</h2>",
                "</div>",
                "<button class='okButton' ng-click='$close(result)'>ok</button>",
                "<button class='cancelButton' ng-click='$dismiss(fail)'>fail</button>"].join('\n'));
        })
    );

    var initDirective = function (template) {
        var directiveEl = angular.element(template);
        $scope.$apply(function () {
            $compile(directiveEl)($scope);
        });
        jq = $(directiveEl);
        $('body').html(jq);
    };

    beforeEach(
        inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            $compile = $injector.get('$compile');

            OffCanvasStack = $injector.get('offCanvasStack');
            OffCanvasDialogStacks = $injector.get('_offCanvasDialogStacks');

            // reset off canvas stacks
            _.each(OffCanvasDialogStacks, function(dialogStack) {
                dialogStack.splice(0, dialogStack.length);
            });

            var defaultTemplate = [
                '<div>',
                '<off-canvas-container></off-canvas-container>',
                '</div>'
            ].join('\n');

            initDirective(defaultTemplate);

        })
    );

    it("Calling open causes stack to grow", function () {
        OffCanvasStack.open({templateUrl : 'ocs.test.template1.html'});
        $scope.$digest();
        OffCanvasStack.open({templateUrl : 'ocs.test.template1.html'});
        $scope.$digest();
        expect(OffCanvasDialogStacks['default'].length).toBe(2);
    });

    it("Open without templateUrl fails", function () {
        expect(function () {
            OffCanvasStack.open({});
        }).toThrow();
    });

    it("Open without controller", function () {
        var openedHandler = jasmine.createSpy('opened handler');
        OffCanvasStack.open({templateUrl : 'ocs.test.template1.html'})
            .opened.then(openedHandler);
        $scope.$digest();

        expect(jq.find('off-canvas-stack-item .template1').length).toBe(1);
        expect(openedHandler).toHaveBeenCalled();
    });

    it("Open with controller without scope $scope.$close() and $scope.$dismiss() work", function () {
        OffCanvasStack
            .open({
                templateUrl : 'ocs.test.template1.html',
                controller : function ($scope) {

                    OffCanvasStack
                        .open({
                            templateUrl : 'ocs.test.template1.html',
                            controller : function ($scope) {
                                $scope.$dismiss("error");
                            }
                        })
                        .result.catch(function (err) {
                            expect(err).toBe("error");
                        })
                        .finally(function () {
                            $scope.$close("fine");
                        });
                }
            }).result.then(function (result) {
                expect(result).toBe("fine");
            });
    });

    it('destroys OCD scope on close', function () {
        var destroyHandler = jasmine.createSpy('OCD scope destroy handler');

        var ocd = OffCanvasStack.open({
            templateUrl: 'ocs.test.template1.html',
            controller: function ($scope) {
                $scope.$on('$destroy', destroyHandler);
            }
        });

        // Make ocd appear
        $scope.$digest();
        expect(jq.find('off-canvas-stack-item .template1').length).toBe(1);

        // Close it, scope should be destroyed
        ocd.dismiss();
        $scope.$digest();
        expect(destroyHandler).toHaveBeenCalled();
    });

    it("Open with controller with scope", function () {
        var openedHandler = jasmine.createSpy('opened handler');
        var tehBestestScope = $scope.$new();
        tehBestestScope.scopeIsPassedCorrectly = true;

        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template1.html',
                controller: function ($scope) {
                    expect($scope.scopeIsPassedCorrectly).toBe(true);
                    expect($scope.doesntExist).toBeUndefined();
                },
                scope: tehBestestScope
            })
            .opened.then(openedHandler);

        $scope.$digest();
        expect(openedHandler).toHaveBeenCalled();
    });

    it("Pass caller scope to modal", function () {
        var tehBestestScope = $scope.$new();
        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template1.html',
                scope: tehBestestScope
            });
        expect(OffCanvasDialogStacks['default'][0].renderOpts.scope.$parent)
            .toBe(tehBestestScope);
    });

    it("Resolve simple non-promise dependencies and inject to controller", function () {
        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template2.html',
                resolve: {
                    tehMessage: function () {
                        return 'bestest msg';
                    }
                },
                controller: function ($scope, tehMessage) {
                    $scope.content = tehMessage;
                }
            });

        $rootScope.$digest();

        expect(jq.find('off-canvas-stack-item .template2').length).toBe(1);
        expect(jq.find('off-canvas-stack-item h2').text()).toBe('bestest msg');
    });

    it("Open with throwing resolve rejects opened promise and OCD doesnt appear", function () {
        var resolved = jasmine.createSpy('resolved'), rejected = jasmine.createSpy('rejected');

        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template2.html',
                resolve: {
                    tehMessage: function () {
                        throw new Error('you wont be getting teh bestest msg');
                    }
                },
                controller: function ($scope, tehMessage) {
                    $scope.content = tehMessage;
                }
            })
            .opened.then(resolved).catch(rejected);

        $rootScope.$digest();

        expect(resolved).not.toHaveBeenCalled();
        expect(rejected).toHaveBeenCalled();

        expect(jq.find('off-canvas-stack-item').length).toBe(0);
        expect(jq.find('.off-canvas-panels').hasClass('open')).toBe(false);
    });

    it("Open with throwing resolve rejects also result promise", function () {
        var resolved = jasmine.createSpy('resolved'), rejected = jasmine.createSpy('rejected');

        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template2.html',
                resolve: {
                    tehMessage: function () {
                        throw new Error('you wont be getting teh bestest msg');
                    }
                },
                controller: function ($scope, tehMessage) {
                    $scope.content = tehMessage;
                }
            })
            .result.then(resolved).catch(rejected);

        $rootScope.$digest();

        expect(resolved).not.toHaveBeenCalled();
        expect(rejected).toHaveBeenCalled();
    });

    it("Resolve simple promise dependencies and inject to controller", function () {
        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template2.html',
                resolve: {
                    tehMessage: function ($q) {
                        return $q.when('later msg');
                    }
                },
                controller: function ($scope, tehMessage) {
                    $scope.content = tehMessage;
                }
            });

        $rootScope.$digest();

        expect(jq.find('off-canvas-stack-item .template2').length).toBe(1);
        expect(jq.find('off-canvas-stack-item h2').text()).toBe('later msg');
        expect(jq.find('.off-canvas-panels').hasClass('open')).toBe(true);
    });

    it("Open with rejected promise from resolve rejects opened promise", function () {
        var resolved = jasmine.createSpy('resolved'), rejected = jasmine.createSpy('rejected');

        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template2.html',
                resolve: {
                    tehMessage: function () {
                        return $q.when('you wont be getting teh bestest msg');
                    }
                },
                controller: function ($scope, tehMessage) {
                    $scope.content = tehMessage;
                }
            })
            .opened.then(resolved).catch(rejected);

        $rootScope.$digest();

        expect(resolved).not.toHaveBeenCalled();
        expect(rejected).toHaveBeenCalled();
    });

    it("Resolve dependencies with recursive dependencies and inject to controller and even then resolve opened promise", function () {
        var openedHandler = jasmine.createSpy('opened handler');
        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template2.html',
                resolve: {
                    tehSuffix: function () {
                        return 'msg';
                    },
                    tehMessage: function ($q, tehSuffix) {
                        return $q.when('complex ' + tehSuffix);
                    }
                },
                controller: function ($scope, tehMessage) {
                    $scope.content = tehMessage;
                }
            })
            .opened.then(openedHandler);

        $rootScope.$digest();

        expect(jq.find('off-canvas-stack-item .template2').length).toBe(1);
        expect(jq.find('off-canvas-stack-item h2').text()).toBe('complex msg');

        expect(openedHandler).toHaveBeenCalled();
    });

    it("Only runs controller and links template after all resolves are complete", function () {
        var slowOp = null;
        var controller = jasmine.createSpy('controller function')
            .and.callFake(function ($scope, slowStuff) {
                expect(slowStuff).toBe('teh result, finally');
            });

        OffCanvasStack
            .open({
                templateUrl: 'ocs.test.template1.html',
                resolve: {
                    slowStuff: function ($q) {
                        slowOp = $q.defer();
                        return slowOp.promise;
                    }
                },
                controller: ['$scope', 'slowStuff', controller] // jasmine spy ruins DI
            });

        $rootScope.$digest();

        // Should have called resolve function
        expect(slowOp).not.toBe(null);

        // Promise not resolved yet
        expect(controller).not.toHaveBeenCalled();
        expect(jq.find('off-canvas-stack-item .template1').length).toBe(0);

        // Now resolve it
        slowOp.resolve('teh result, finally');
        $rootScope.$digest();

        // Only now it should appear
        expect(controller).toHaveBeenCalled();
        expect(jq.find('off-canvas-stack-item .template1').length).toBe(1);
        expect(jq.find('off-canvas-stack-item .template1 h1').text()).toBe("I'm #1");
        expect(jq.find('.off-canvas-panels').hasClass('open')).toBe(true);
    });

    it("Closing dialog in middle rejects rest of the stack", function () {
        var retVal = "I'll be back";
        var one = OffCanvasStack.open({templateUrl: 'ocs.test.template1.html'});
        var two = OffCanvasStack.open({templateUrl: 'ocs.test.template1.html'});
        var three = OffCanvasStack.open({templateUrl: 'ocs.test.template1.html'});

        var closeOne = spyOn(one, 'close').and.callThrough();
        var threeRejected = jasmine.createSpy('three reject handler');

        one.result.finally(function () {
            expect(closeOne).toHaveBeenCalled();
        });

        two.result.then(function (result) {
            expect(result).toBe(retVal);
        }).finally(closeOne);

        three.result
            .catch(threeRejected)
            .finally(function () {
                expect(threeRejected).toHaveBeenCalled();
            });

        // close two -> three is rejected and one closed
        two.close(retVal);

        // digest needed to update directive rendering etc.
        $scope.$digest();
    });

    /*it("Implicitly exposed $close() and $dismiss() does something", function () {
        var dialog1 = OffCanvasStack.open({templateUrl: 'ocs.test.template2.html'});
        var dialog2 = OffCanvasStack.open({templateUrl: 'ocs.test.template2.html'});
        $scope.$digest();

        var wrongHandler = jasmine.createSpy();
        var correctHandler = jasmine.createSpy();

        dialog1.result.then(wrongHandler).catch(correctHandler);
        dialog2.result.then(correctHandler).catch(wrongHandler);

        // close last dialog first
        testTools.click(jq.find('off-canvas-stack-item:last .okButton'));
        $scope.$digest();

        // and first dialog after
        testTools.click(jq.find('off-canvas-stack-item:first .cancelButton'));
        $scope.$digest();

        expect(correctHandler.calls.count()).toEqual(2);
        expect(wrongHandler).not.toHaveBeenCalled();
    });*/

    it("Multiple named off-canvas-containers does not interfere", function () {
        var manyNamedContainers = [
            '<div>',
            '<off-canvas-container class="defaultStack"></off-canvas-container>',
            '<off-canvas-container class="stack1Stack" stack-name="stack1"></off-canvas-container>',
            '<off-canvas-container class="stack2Stack" stack-name="stack2"></off-canvas-container>',
            '</div>'
        ].join('\n');
        initDirective(manyNamedContainers);

        // open 3 dialogs, and close them one by one
        var d1 = OffCanvasStack.open({templateUrl: 'ocs.test.template1.html'}, 'stack1');
        var d2 = OffCanvasStack.open({templateUrl: 'ocs.test.template2.html'}, 'stack2');
        var d3 = OffCanvasStack.open({templateUrl: 'ocs.test.template2.html'});

        expect(OffCanvasDialogStacks['default'].length).toBe(1);
        expect(OffCanvasDialogStacks['stack1'].length).toBe(1);
        expect(OffCanvasDialogStacks['stack2'].length).toBe(1);

        $scope.$digest();
        expect(jq.find('.stack1Stack off-canvas-stack-item').length).toBe(1);
        expect(jq.find('.stack2Stack off-canvas-stack-item').length).toBe(1);
        expect(jq.find('.defaultStack off-canvas-stack-item').length).toBe(1);

        d2.close();
        $scope.$digest();
        expect(jq.find('.stack1Stack off-canvas-stack-item').length).toBe(1);
        expect(jq.find('.stack2Stack off-canvas-stack-item').length).toBe(0);
        expect(jq.find('.defaultStack off-canvas-stack-item').length).toBe(1);

        d3.dismiss();
        $scope.$digest();
        expect(jq.find('.stack1Stack off-canvas-stack-item').length).toBe(1);
        expect(jq.find('.stack2Stack off-canvas-stack-item').length).toBe(0);
        expect(jq.find('.defaultStack off-canvas-stack-item').length).toBe(0);

        OffCanvasStack.open({templateUrl: 'ocs.test.template1.html'}, 'stack1');
        $scope.$digest();
        expect(jq.find('.stack1Stack off-canvas-stack-item').length).toBe(2);
        expect(jq.find('.stack2Stack off-canvas-stack-item').length).toBe(0);
        expect(jq.find('.defaultStack off-canvas-stack-item').length).toBe(0);

        // close all stack1 dialogs
        d1.close();
        $scope.$digest();
        expect(jq.find('.stack1Stack off-canvas-stack-item').length).toBe(0);
        expect(jq.find('.stack2Stack off-canvas-stack-item').length).toBe(0);
        expect(jq.find('.defaultStack off-canvas-stack-item').length).toBe(0);
    });
});
