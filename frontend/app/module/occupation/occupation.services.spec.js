describe('app.rhy.services', function () {
    beforeEach(module('app.occupation.services'));

    describe('OccupationsFilterSorterService', function () {
        var sorterfilter;

        beforeEach(inject(function (_OccupationsFilterSorterService_) {
            sorterfilter = _OccupationsFilterSorterService_;
        }));

        it('should filter to empty to empty', function () {
            var allOccupations = [];
            expect(sorterfilter.current(allOccupations)).toEqual({occupations: [], board: []});
            expect(sorterfilter.past(allOccupations)).toEqual({occupations: [], board: null});
            expect(sorterfilter.future(allOccupations)).toEqual({occupations: [], board: null});
        });
    });

});