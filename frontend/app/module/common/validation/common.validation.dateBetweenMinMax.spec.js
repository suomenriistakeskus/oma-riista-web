'use strict';

describe("date-between-min-max", function () {

    function ld(diff) {
        return moment().add(diff || 0, 'day').format('YYYY-MM-DD');
    }

    function assertValid(form, viewValueStr) {
        $scope.$apply(function () {
            form.testDate.$setViewValue(viewValueStr);
        });
        expect($scope.model.date).toEqual(viewValueStr);
        expect(form.testDate.$viewValue).toEqual(viewValueStr);
        expect(form.testDate.$valid).toBe(true);
    }

    function assertInvalid(form, viewValueStr) {
        $scope.$apply(function () {
            form.testDate.$setViewValue(viewValueStr);
        });
        expect($scope.model.date).toBeUndefined();
        expect(form.testDate.$viewValue).toEqual(viewValueStr);
        expect(form.testDate.$error.dateBetweenMinMax).toBe(true);
    }

    var $scope;
    var formNoMinMax;
    var formWithMinMax;
    var formWithMinMax2;

    beforeEach(module('app.common.validation'));
    beforeEach(module('app.common.services'));
    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope.$new();

        var element = angular.element(
                '<form name="formNoMinMax" >' +
                '   <input type="text" name="testDate" ' +
                '       ng-model="model.date" ' +
                '       date-between-min-max' +
                '   > ' +
                '</form>' +
                '<form name="formWithMinMax" >' +
                '   <input type="text" name="testDate" ' +
                '       ng-model="model.date" ' +
                '       date-between-min-max' +
                '       min-date="model.min"' +
                '       max-date="model.max"' +
                '   > ' +
                '</form>' +
                '<form name="formWithMinMax2" >' +
                '   <input type="text" name="testDate" ' +
                '       ng-model="model.date" ' +
                '       date-between-min-max' +
                '       min-date="model.min"' +
                '       max-date="model.max"' +
                '       min-date2="model.min2"' +
                '       max-date2="model.max2"' +
                '   > ' +
                '</form>'
        );
        $scope.model = {max: null, min: null, max2: null, min2: null};
        element = $compile(element)($scope);
        formNoMinMax = $scope.formNoMinMax;
        formWithMinMax = $scope.formWithMinMax;
        formWithMinMax2 = $scope.formWithMinMax2;
    }));

    describe("form with no min and max", function () {
        it("should accept today", function () {
            assertValid(formNoMinMax, ld());
        });

        it("should accept tomorrow", function () {
            assertValid(formNoMinMax, ld(1));
        });

        it("should accept yesterday", function () {
            assertValid(formNoMinMax, ld(-1));
        });
    });

    describe("form with min and max", function () {
        it("should accept max", function () {
            $scope.model.max = ld();
            assertValid(formWithMinMax, ld());
        });

        it("should accept min", function () {
            $scope.model.min = ld();
            assertValid(formWithMinMax, ld());
        });

        it("should accept between", function () {
            $scope.model.max = ld(1);
            $scope.model.min = ld(-1);
            assertValid(formWithMinMax, ld(-1));
            assertValid(formWithMinMax, ld());
            assertValid(formWithMinMax, ld(1));
        });

        it("should reject after max", function () {
            $scope.model.max = ld();
            assertInvalid(formWithMinMax, ld(1));
            assertInvalid(formWithMinMax, ld(2));
        });

        it("should reject before min", function () {
            $scope.model.min = ld();
            assertInvalid(formWithMinMax, ld(-1));
            assertInvalid(formWithMinMax, ld(-2));
        });
    });

    describe("form with min2 and max2", function () {
        it("should accept max2", function () {
            $scope.model.max2 = ld();
            assertValid(formWithMinMax2, ld());
        });

        it("should accept min2", function () {
            $scope.model.min2 = ld();
            assertValid(formWithMinMax2, ld());
        });

        it("should accept between2", function () {
            $scope.model.max2 = ld(1);
            $scope.model.min2 = ld(-1);
            assertValid(formWithMinMax, ld(-1));
            assertValid(formWithMinMax, ld());
            assertValid(formWithMinMax, ld(1));
        });

        it("should reject after max2", function () {
            $scope.model.max2 = ld();
            assertInvalid(formWithMinMax2, ld(1));
            assertInvalid(formWithMinMax2, ld(2));
        });

        it("should reject before min2", function () {
            $scope.model.min2 = ld();
            assertInvalid(formWithMinMax2, ld(-1));
            assertInvalid(formWithMinMax2, ld(-2));
        });
    });

    describe("form with both ranges", function () {
        it("should accept inside either one", function () {
            $scope.model.min = ld(-3);
            $scope.model.max = ld(-1);
            $scope.model.min2 = ld(1);
            $scope.model.max2 = ld(3);
            assertValid(formWithMinMax2, ld(-3));
            assertValid(formWithMinMax2, ld(-2));
            assertValid(formWithMinMax2, ld(-1));
            assertValid(formWithMinMax2, ld(1));
            assertValid(formWithMinMax2, ld(2));
            assertValid(formWithMinMax2, ld(3));
        });

        it("should reject outside both", function () {
            $scope.model.min = ld(-3);
            $scope.model.max = ld(-1);
            $scope.model.min2 = ld(1);
            $scope.model.max2 = ld(3);
            assertInvalid(formWithMinMax2, ld());
            assertInvalid(formWithMinMax2, ld(-4));
            assertInvalid(formWithMinMax2, ld(4));
        });
    });

    describe("form with array value for min or max", function () {
        it("should accept max", function () {
            $scope.model.max = [ld(), ld(1)];
            assertValid(formWithMinMax, ld());
        });

        it("should accept min", function () {
            $scope.model.min = [ld(), ld(-1)];
            assertValid(formWithMinMax, ld());
        });

        it("should accept between", function () {
            $scope.model.max = [ld(1)];
            $scope.model.min = [ld(-1)];
            assertValid(formWithMinMax, ld(-1));
            assertValid(formWithMinMax, ld());
            assertValid(formWithMinMax, ld(1));
        });

        it("should reject after max", function () {
            $scope.model.max = [ld(), ld(1)];
            assertInvalid(formWithMinMax, ld(1));
            assertInvalid(formWithMinMax, ld(2));
            assertInvalid(formWithMinMax, ld(3));
        });

        it("should reject before min", function () {
            $scope.model.min = [ld(), ld(-1)];
            assertInvalid(formWithMinMax, ld(-1));
            assertInvalid(formWithMinMax, ld(-2));
        });
    });
});
