'use strict';

describe("HuntingYearService tests", function () {
    var huntingYearService;

    beforeEach(module('app.common.huntingyear'));

    beforeEach(inject(function (HuntingYearService) {
        huntingYearService = HuntingYearService;
    }));

    describe("HuntingYearService", function () {
        describe('toStr', function () {
            it("should format year", function () {
                expect(huntingYearService.toStr(2014)).toEqual('2014-15');
            });
        });

        describe('toObj', function () {
            it("should create correct object", function () {
                expect(huntingYearService.toObj(2014)).toEqual({year: 2014, name: '2014-15'});
            });
        });

        describe('getCurrent', function () {
            it("should resolve current hunting year", function () {
                expect(huntingYearService.getCurrent()).toEqual(huntingYearService.dateToHuntingYear(new Date()));
            });
        });

        describe('dateToHuntingYear', function () {
            it("should work on first hunting year date", function () {
                expect(huntingYearService.dateToHuntingYear(new Date('2014-08-01'))).toEqual(2014);
            });

            it("should work on first hunting year date with different year", function () {
                expect(huntingYearService.dateToHuntingYear(new Date('2015-08-01'))).toEqual(2015);
            });

            it("should work on last hunting year date", function () {
                expect(huntingYearService.dateToHuntingYear(new Date('2015-07-31'))).toEqual(2014);
            });
        });

        describe('currentAndNextObj', function () {
            it("should resolve current and next", function () {
                var now = new Date();
                var expectedCurrentYear = huntingYearService.dateToHuntingYear(now);

                var currentAndNext = huntingYearService.currentAndNextObj();
                expect(currentAndNext.length).toEqual(2);
                expect(currentAndNext[0].year).toEqual(expectedCurrentYear);
                expect(currentAndNext[1].year).toEqual(expectedCurrentYear + 1);
            });
        });

        describe('getBeginDateStr', function () {
            var currentHuntingYearBegin;

            beforeEach(function () {
                currentHuntingYearBegin = huntingYearService.getBeginDateStr(huntingYearService.getCurrent());
            });

            it("should accept numeric input", function () {
                expect(huntingYearService.getBeginDateStr(2014)).toEqual('2014-08-01');
            });

            it("should accept Date input", function () {
                expect(huntingYearService.getBeginDateStr(new Date('2014-08-01'))).toEqual('2014-08-01');
                expect(huntingYearService.getBeginDateStr(new Date('2015-07-31'))).toEqual('2014-08-01');
            });

            it("should accept undefined input", function () {
                expect(huntingYearService.getBeginDateStr()).toEqual(currentHuntingYearBegin);
            });

            it("should accept null input", function () {
                expect(huntingYearService.getBeginDateStr(null)).toEqual(currentHuntingYearBegin);
            });

            it("should throw on invalid input", function () {
                function testInvalidInput(input) {
                    return _.partial(huntingYearService.getBeginDateStr, input);
                }

                expect(testInvalidInput('abc')).toThrowError(TypeError);
                expect(testInvalidInput({})).toThrowError(TypeError);
                expect(testInvalidInput([])).toThrowError(TypeError);
                expect(testInvalidInput(moment())).toThrowError(TypeError);
            });
        });

        describe('getEndDateStr', function () {
            var currentHuntingYearEnd;

            beforeEach(function () {
                currentHuntingYearEnd = huntingYearService.getEndDateStr(huntingYearService.getCurrent());
            });

            it("should accept numeric input", function () {
                expect(huntingYearService.getEndDateStr(2014)).toEqual('2015-07-31');
            });

            it("should accept Date input", function () {
                expect(huntingYearService.getEndDateStr(new Date('2014-08-01'))).toEqual('2015-07-31');
                expect(huntingYearService.getEndDateStr(new Date('2015-07-31'))).toEqual('2015-07-31');
            });

            it("should accept undefined input", function () {
                expect(huntingYearService.getEndDateStr()).toEqual(currentHuntingYearEnd);
            });

            it("should accept null input", function () {
                expect(huntingYearService.getEndDateStr(null)).toEqual(currentHuntingYearEnd);
            });

            it("should throw on invalid input", function () {
                function testInvalidInput(input) {
                    return _.partial(huntingYearService.getEndDateStr, input);
                }

                expect(testInvalidInput('abc')).toThrowError(TypeError);
                expect(testInvalidInput({})).toThrowError(TypeError);
                expect(testInvalidInput([])).toThrowError(TypeError);
                expect(testInvalidInput(moment())).toThrowError(TypeError);
            });
        });
    });
});

