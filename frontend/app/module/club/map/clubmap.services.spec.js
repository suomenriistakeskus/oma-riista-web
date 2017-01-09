describe('PolygonService', function () {
    beforeEach(module('app.clubmap.services'));

    var PolygonService;

    beforeEach(inject(function (_PolygonService_) {
        PolygonService = _PolygonService_;
    }));

    var ringA = [[0, 0], [20, 0], [20, 20], [0, 20]];
    var ringB = [[5, 5], [15, 5], [15, 15], [5, 15]];
    var ringC = [[15, 15], [25, 15], [25, 25], [15, 25]];

    describe('geometryToPolygons', function () {
        var simplyPolygon = {
            type: 'Polygon',
            coordinates: [ringA]
        };

        var holePolygon = {
            type: 'Polygon',
            coordinates: [ringA, ringB]
        };

        var multiPolygon = {
            type: 'MultiPolygon',
            coordinates: [[ringA], [ringC]]
        };

        var multiPolygonWithHoles = {
            type: 'MultiPolygon',
            coordinates: [[ringA, ringB], [ringC]]
        };

        var geometryCollection = {
            type: 'GeometryCollection',
            geometries: [
                {
                    type: 'Polygon',
                    coordinates: [ringA]
                },
                {
                    type: 'MultiPolygon',
                    coordinates: [[ringB, ringA], [ringC]]
                }
            ]
        };

        it('should ignore falsy values', function () {
            expect(PolygonService.geometryToPolygons(null)).toEqual([]);
            expect(PolygonService.geometryToPolygons(undefined)).toEqual([]);
            expect(PolygonService.geometryToPolygons(false)).toEqual([]);
        });

        it('should ignore unknown geometry types', function () {
            var unknownGeometry = {
                type: 'Curve',
                coordinates: [ringA]
            };
            expect(PolygonService.geometryToPolygons(unknownGeometry)).toEqual([]);
        });

        it('should ignore unknown geometry without type', function () {
            var unknownGeometry = {
                coordinates: [ringA]
            };
            expect(PolygonService.geometryToPolygons(unknownGeometry)).toEqual([]);
        });

        it('should handle Polygon with only exterior rings', function () {
            var result = PolygonService.geometryToPolygons(simplyPolygon);
            expect(result).toEqual([
                ringA
            ]);
        });

        it('should handle Polygon with interior rings', function () {
            var result = PolygonService.geometryToPolygons(holePolygon);
            expect(result).toEqual([
                ringA
            ]);
        });

        it('should handle MultiPolygon with only exterior rings', function () {
            var result = PolygonService.geometryToPolygons(multiPolygon);
            expect(result).toEqual([
                ringA, ringC
            ]);
        });

        it('should handle MultiPolygon with interior rings', function () {
            var result = PolygonService.geometryToPolygons(multiPolygonWithHoles);
            expect(result).toEqual([
                ringA, ringC
            ]);
        });

        it('should handle GeometryCollection', function () {
            var result = PolygonService.geometryToPolygons(geometryCollection);
            expect(result).toEqual([
                ringA, ringB, ringC
            ]);
        });
    });

    describe('intersection', function () {
        it('should work with boundary intersection', function () {
            var clipPolygon = ringC;
            var sourcePolygons = [ringA];
            var result = PolygonService.intersection(clipPolygon, sourcePolygons);

            expect(result).toEqual([[[20, 15], [20, 20], [15, 20], [15, 15], [20, 15]]]);
        });

        it('should work with no boundary intersection', function () {
            var clipPolygon = ringB;
            var sourcePolygons = [ringA];
            var result = PolygonService.intersection(clipPolygon, sourcePolygons);

            expect(result).toEqual([ringB]);
        });
    });

    describe('difference', function () {
        it('should work with boundary intersection', function () {
            var clipPolygon = ringC;
            var sourcePolygons = [ringA];
            var result = PolygonService.difference(clipPolygon, sourcePolygons);

            expect(result).toEqual([[[20, 15], [20, 0], [0, 0], [0, 20], [15, 20], [15, 15], [20, 15]]]);
        });

        it('should not touch source polygon when clip polygon is inside', function () {
            var clipPolygon = ringB;
            var sourcePolygons = [ringA];
            var result = PolygonService.difference(clipPolygon, sourcePolygons);

            expect(result).toEqual([ringA]);
        });

        it('should remove original area when clip polygon is around source polygon', function () {
            var clipPolygon = ringA;
            var sourcePolygons = [ringB];
            var result = PolygonService.difference(clipPolygon, sourcePolygons);

            expect(result).toEqual([]);
        });
    });

    describe('joinPolygons', function () {
        var $httpBackend;

        beforeEach(inject(function (_$httpBackend_) {
            $httpBackend = _$httpBackend_;
        }));

        afterEach(function () {
            $httpBackend.verifyNoOutstandingExpectation();
            $httpBackend.verifyNoOutstandingRequest();
        });

        it('should work', function () {
            var expectedBody = {
                "type": "FeatureCollection",
                "features": [{
                    "type": "Feature",
                    "properties": {},
                    "geometry": {
                        "type": "Polygon", "coordinates": [[[0, 0], [20, 0], [20, 20], [0, 20]]]
                    }
                }, {
                    "type": "Feature",
                    "properties": {},
                    "geometry": {"type": "Polygon", "coordinates": [[[5, 5], [15, 5], [15, 15], [5, 15]]]}
                }, {
                    "type": "Feature",
                    "properties": {},
                    "geometry": {"type": "Polygon", "coordinates": [[[15, 15], [25, 15], [25, 25], [15, 25]]]}
                }]
            };

            var unionPolygon = [[[0, 0], [20, 0], [20, 15], [25, 15], [25, 25], [15, 25], [15, 20], [20, 20], [0, 20]]];
            var responseBody = {
                "type": "Polygon",
                "coordinates": unionPolygon
            };

            var resultPromise = PolygonService.joinPolygons([ringA, ringB, ringC]);

            $httpBackend.expectPOST('/api/v1/gis/polygonUnion', expectedBody).respond(200, responseBody);
            $httpBackend.flush();

            resultPromise.then(function (result) {
                expect(result).toEqual(unionPolygon);
            });
        });
    });
});
