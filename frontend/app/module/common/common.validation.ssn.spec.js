'use strict';

describe("validation", function () {
    var $scope;
    var form;

    beforeEach(module('app.common.validation'));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope.$new();

        var element = angular.element(
            '<form name="form" >' +
            '   <input type="text" name="finnishPersonalIdentity" ' +
            '       ng-model="model.finnishPersonalIdentity" ' +
            '       valid-ssn> ' +
            '</form>'
        );
        $scope.model = {finnishPersonalIdentity: null};
        element = $compile(element)($scope);
        form = $scope.form;
    }));

    describe("finnishPersonalIdentityValidation", function () {

        it("should accept valid input", function () {
            var validPersonalIdentityNumbers = [
                "121212-0150", "121212-0161", "121212-0172", "121212-0183", "121212-0194", "121212-0205",
                "121212-0216", "121212-0227", "121212-0238", "121212-0559", "121212-025A", "121212A026B",
                "121212A027C", "121212-028D", "121212-029E", "121212A030F", "121212-031H", "121212-001J",
                "121212-002K", "121212A003L", "121212-004M", "121212-005N", "121212-006P", "121212-007R",
                "121212-008S", "121212-009T", "121212-010U", "121212-042V", "121212A012W", "121212-013X",
                "121212A014Y"
            ];
            validPersonalIdentityNumbers.forEach(function (validPersonalIdentity) {
                form.finnishPersonalIdentity.$setViewValue(validPersonalIdentity);
                $scope.$digest();
                expect($scope.model.finnishPersonalIdentity).toEqual(validPersonalIdentity);
                expect(form.finnishPersonalIdentity.$viewValue).toEqual(validPersonalIdentity);
                expect(form.finnishPersonalIdentity.$valid).toBe(true);
            });
        });

        it("should reject invalid input", function () {
            var invalidPersonalIdentityNumbers = [
                "121212A000", "121212A000HH", "121212A000J", "131212A000H", "121112A000H", "121212B000H",
                "1212"
            ];
            invalidPersonalIdentityNumbers.forEach(function (invalidPersonalIdentity) {
                form.finnishPersonalIdentity.$setViewValue(invalidPersonalIdentity);
                $scope.$digest();
                expect(form.finnishPersonalIdentity.$viewValue).toEqual(invalidPersonalIdentity);
                expect(form.finnishPersonalIdentity.$invalid).toBe(true);
            });
        });
    });
});

