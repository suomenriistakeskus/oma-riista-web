'use strict';

describe("HuntingYearService tests", function () {
    var formatPropertyIdentifier;

    beforeEach(module('app.common.filters'));

    beforeEach(inject(function ($filter) {
        formatPropertyIdentifier = $filter('formatPropertyIdentifier');
    }));

    describe("formatPropertyIdentifier", function () {
        it("should format valid value", function () {
            expect(formatPropertyIdentifier("12312312341234")).toEqual('123-123-1234-1234');
        });

        it("should ignore invalid length", function() {
            expect(formatPropertyIdentifier("123")).toEqual('123');
        });

        it("should ignore non-digit characters", function() {
            expect(formatPropertyIdentifier("1b312312341234")).toEqual('1b312312341234');
        });

        it("should ignore null value", function() {
            expect(formatPropertyIdentifier(null)).toBeNull();
        });

        it("should ignore empty value", function() {
            expect(formatPropertyIdentifier('')).toEqual('');
        });
    });
});
