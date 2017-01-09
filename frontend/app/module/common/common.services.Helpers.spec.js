'use strict';

describe("Helpers tests", function () {
    var helpers;

    beforeEach(module('app.common.services'));

    beforeEach(inject(function (Helpers) {
        helpers = Helpers;
    }));

    describe("dateToString", function () {
        it("should format date correctly with single-digit month and date", function () {
            expect(helpers.dateToString(new Date(2014, 0, 1))).toEqual('2014-01-01');
        });

        it("should format date correctly with double-digit month and date", function () {
            expect(helpers.dateToString(new Date(2014, 11, 31))).toEqual('2014-12-31');
        });
    });

    describe("dateTimeToString", function () {
        it("should format datetime correctly with single-digit components", function () {
            expect(helpers.dateTimeToString(new Date(2014, 0, 1, 2, 3))).toEqual('2014-01-01T02:03');
        });

        it("should format datetime correctly with double-digit month and date", function () {
            expect(helpers.dateTimeToString(new Date(2014, 11, 31, 23, 45))).toEqual('2014-12-31T23:45');
        });
    });
});
