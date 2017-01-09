'use strict';

describe("date-between-min-max", function () {
    var $scope;
    var formNoMinMax;
    var formWithMinMax;
    var formWithMinMax2;
    var helpers;

    beforeEach(module('app.common.validation'));
    beforeEach(module('app.common.services'));
    beforeEach(inject(function (Helpers) {
        helpers = Helpers;
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope.$new();

        var element = angular.element(
                '<form name="formNoMinMax" >' +
                '   <input type="text" name="testDate" ' +
                '       ng-model="model.date" ' +
                '       date-between-min-max' +
                '       uib-datepicker-popup="d.M.yyyy"' +
                '   > ' +
                '</form>' +
                '<form name="formWithMinMax" >' +
                '   <input type="text" name="testDate" ' +
                '       ng-model="model.date" ' +
                '       date-between-min-max' +
                '       uib-datepicker-popup="d.M.yyyy"' +
                '       max-date="model.max"' +
                '       min-date="model.min"' +
                '   > ' +
                '</form>' +
                '<form name="formWithMinMax2" >' +
                '   <input type="text" name="testDate" ' +
                '       ng-model="model.date" ' +
                '       date-between-min-max' +
                '       uib-datepicker-popup="d.M.yyyy"' +
                '       max-date="model.max"' +
                '       min-date="model.min"' +
                '       max-date2="model.max2"' +
                '       min-date2="model.min2"' +
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
            var dateStr = helpers.dateToString(moment().toDate());
            assertViewValue(formNoMinMax, dateStr, true);
        });

        it("should accept tomorrow", function () {
            var dateStr = helpers.dateToString(moment().add(1, 'day').toDate());
            assertViewValue(formNoMinMax, dateStr, true);
        });

        it("should accept yesterday", function () {
            var dateStr = helpers.dateToString(moment().subtract(1, 'day').toDate());
            assertViewValue(formNoMinMax, dateStr, true);
        });
    });

    describe("form with min and max", function () {
        it("should accept max", function () {
            var dateStr = helpers.dateToString(new Date());
            $scope.model.max = moment(dateStr).toDate();
            assertViewValue(formWithMinMax, dateStr, true);
        });

        it("should accept min", function () {
            var dateStr = helpers.dateToString(new Date());
            $scope.model.min = moment(dateStr).toDate();
            assertViewValue(formWithMinMax, dateStr, true);
        });

        it("should accept between", function () {
            var dateStr = helpers.dateToString(new Date());
            $scope.model.max = moment(dateStr).add(1, 'day').toDate();
            $scope.model.min = moment(dateStr).subtract(1, 'day').toDate();
            assertViewValue(formWithMinMax, dateStr, true);
        });

        it("should reject after max", function () {
            $scope.model.max = new Date();
            var dateStr = helpers.dateToString(moment().add(1, 'day').toDate());
            assertViewAndModelValue(formWithMinMax, undefined, dateStr, false);
        });

        it("should reject before min", function () {
            $scope.model.min = new Date();
            var dateStr = helpers.dateToString(moment().subtract(1, 'day').toDate());
            assertViewAndModelValue(formWithMinMax, undefined, dateStr, false);
        });
    });

    describe("form with min2 and max2", function () {
        it("should accept max2", function () {
            var dateStr = helpers.dateToString(new Date());
            $scope.model.max2 = moment(dateStr).toDate();
            assertViewValue(formWithMinMax2, dateStr, true);
        });

        it("should accept min2", function () {
            var dateStr = helpers.dateToString(new Date());
            $scope.model.min2 = moment(dateStr).toDate();
            assertViewValue(formWithMinMax2, dateStr, true);
        });

        it("should accept between2", function () {
            var dateStr = helpers.dateToString(new Date());
            $scope.model.max2 = moment(dateStr).add(1, 'day').toDate();
            $scope.model.min2 = moment(dateStr).subtract(1, 'day').toDate();
            assertViewValue(formWithMinMax2, dateStr, true);
        });

        it("should reject after max2", function () {
            $scope.model.max2 = new Date();
            var dateStr = helpers.dateToString(moment().add(1, 'day').toDate());
            assertViewAndModelValue(formWithMinMax2, undefined, dateStr, false);
        });

        it("should reject before min2", function () {
            $scope.model.min2 = new Date();
            var dateStr = helpers.dateToString(moment().subtract(1, 'day').toDate());
            assertViewAndModelValue(formWithMinMax2, undefined, dateStr, false);
        });
    });

    function assertViewValue(form, viewValue, valid) {
        form.testDate.$setViewValue(viewValue);
        $scope.$digest();
        expect(helpers.dateToString($scope.model.date)).toEqual(viewValue);
        expect(form.testDate.$viewValue).toEqual(viewValue);
        expect(form.testDate.$valid).toBe(valid);
    }

    function assertViewAndModelValue(form, modelValueStr, viewValueStr, valid) {
        form.testDate.$setViewValue(viewValueStr);
        $scope.$digest();
        expect(helpers.dateToString($scope.model.date)).toEqual(modelValueStr);
        expect(form.testDate.$viewValue).toEqual(viewValueStr);
        expect(form.testDate.$valid).toBe(valid);
    }
});

