'use strict';

describe("finnish-date-time-in-past", function () {
    var $scope;
    var formNoDate;
    var formWithDate;


    beforeEach(module('app.common.validation'));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope.$new();

        var element = angular.element(
                '<form name="formNoDate" >' +
                '   <input type="text" name="finnishTime" ' +
                '       ng-model="model.time" ' +
                '       finnish-date-time-in-past> ' +
                '</form>' +
                '<form name="formWithDate" >' +
                '   <input type="text" name="finnishTime" ' +
                '       ng-model="model.time" ' +
                '       finnish-date-time-in-past="model.date"> ' +
                '</form>'
        );
        $scope.model = {};
        element = $compile(element)($scope);
        formNoDate = $scope.formNoDate;
        formWithDate = $scope.formWithDate;
    }));

    describe("with no date", function () {
        it("should accept valid input", function () {
            var momentNow = moment();

            formNoDate.finnishTime.$setViewValue(momentNow.format("HH mm"));
            $scope.$digest();
            expect($scope.model.time).toEqual(momentNow.format("HH:mm"));
            expect(formNoDate.finnishTime.$viewValue).toEqual(momentNow.format("HH:mm"));
            expect(formNoDate.finnishTime.$valid).toBe(true);
        });

        it("should reject invalid input", function () {
            formNoDate.finnishTime.$setViewValue("25");
            $scope.$digest();
            expect($scope.model.time).toEqual(undefined);
            expect(formNoDate.finnishTime.$viewValue).toEqual("25");
            expect(formNoDate.finnishTime.$valid).toBe(false);
        });

        it("should accept current time", function () {
            var timeNow = moment().format("HH:mm");

            formNoDate.finnishTime.$setViewValue(timeNow);
            $scope.$digest();
            expect($scope.model.time).toEqual(timeNow);
            expect(formNoDate.finnishTime.$viewValue).toEqual(timeNow);
            expect(formNoDate.finnishTime.$valid).toBe(true);
        });

        it("should accept future", function () {
            var future = moment().add(1, 'minute').format("HH:mm");

            formNoDate.finnishTime.$setViewValue(future);
            $scope.$digest();
            expect($scope.model.time).toEqual(future);
            expect(formNoDate.finnishTime.$viewValue).toEqual(future);
            expect(formNoDate.finnishTime.$valid).toBe(true);
        });
    });

    describe("with date", function () {
        it("should accept future date and future time", function () {
            var date = moment().add(1, 'days').add(2, 'minute');
            $scope.model.date = date.toDate();
            var time = date.format("HH:mm");

            formWithDate.finnishTime.$setViewValue(time);
            $scope.$digest();
            expect($scope.model.time).toEqual(time);
            expect(formWithDate.finnishTime.$viewValue).toEqual(time);
            expect(formWithDate.finnishTime.$valid).toBe(true);
        });

        it("should accept past date and time", function () {
            var date = moment().subtract(1, 'days').add(2, 'minute');
            $scope.model.date = date.toDate();
            var time = date.format("HH:mm");

            formWithDate.finnishTime.$setViewValue(time);
            $scope.$digest();
            expect($scope.model.time).toEqual(time);
            expect(formWithDate.finnishTime.$viewValue).toEqual(time);
            expect(formWithDate.finnishTime.$valid).toBe(true);
        });

        it("should reject current date and future time", function () {
            var date = moment().add(2, 'minute');
            $scope.model.date = date.toDate();
            var time = date.format("HH:mm");

            formWithDate.finnishTime.$setViewValue(time);
            $scope.$digest();
            expect($scope.model.time).toEqual(time);
            expect(formWithDate.finnishTime.$viewValue).toEqual(time);
            expect(formWithDate.finnishTime.$valid).toBe(false);
        });

        it("changing invalid current date and time to past will be valid", function () {
            var date = moment().add(2, 'minute');
            $scope.model.date = date.toDate();
            var time = date.format("HH:mm");

            formWithDate.finnishTime.$setViewValue(time);
            $scope.$digest();
            expect(formWithDate.finnishTime.$valid).toBe(false);

            $scope.model.date = date.subtract(1, 'day').toDate();
            $scope.$digest();
            expect($scope.model.time).toEqual(time);
            expect(formWithDate.finnishTime.$viewValue).toEqual(time);
            expect(formWithDate.finnishTime.$valid).toBe(true);
        });

        it("changing past to current date and future time will be invalid", function () {
            var date = moment().subtract(1, 'day').add(2, 'minute');
            $scope.model.date = date.toDate();
            var time = date.format("HH:mm");

            formWithDate.finnishTime.$setViewValue(time);
            $scope.$digest();
            expect(formWithDate.finnishTime.$valid).toBe(true);

            $scope.model.date = date.add(1, 'day').toDate();
            $scope.$digest();
            expect(formWithDate.finnishTime.$viewValue).toEqual(time);
            expect(formWithDate.finnishTime.$valid).toBe(false);
        });


    });
});

