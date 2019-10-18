describe('app.mapeditor.mml', function () {
    beforeEach(module('app.mapeditor.mml'));

    var $scope;
    var form;

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope.$new();

        var element = angular.element(
            '<form name="form" >' +
            '<textarea name="propertyList" id="propertyList"' +
            ' ng-model="model.propertyList"' +
            ' property-identifier-list>' +
            '</textarea>' +
            '</form>'
        );
        $scope.model = {propertyList: null};
        element = $compile(element)($scope);
        form = $scope.form;
    }));

    describe('propertyIdentifierList', function () {
        function _assertResult(viewValue, expected) {
            form.propertyList.$setViewValue(viewValue);
            $scope.$digest();
            expect($scope.model.propertyList).toEqual(expected);
            expect(form.propertyList.$valid).toBe(true);
        }

        it("should accept single formatted value", function () {
            _assertResult('418-406-10-23', ['41840600100023']);
        });

        it("should accept single un-formatted value", function () {
            _assertResult('41840600100023', ['41840600100023']);
        });

        it("should accept multiple formatted values with comma separator", function () {
            _assertResult('211-403-3-15, 211-403-4-31', ['21140300030015', '21140300040031']);
        });

        it("should accept multiple formatted values with semi-colon separator", function () {
            _assertResult('211-403-3-15; 211-403-4-31', ['21140300030015', '21140300040031']);
        });

        it("should accept multiple formatted values with new-line separator", function () {
            _assertResult('211-403-3-15\n211-403-4-31', ['21140300030015', '21140300040031']);
        });

        it("should accept multiple formatted values with tabulator separator", function () {
            _assertResult('211-403-3-15\t211-403-4-31', ['21140300030015', '21140300040031']);
        });

        it("should accept multiple formatted values with mixed separators", function () {
            _assertResult('211-403-3-15\n211-403-4-31; 211-403-4-32,211-403-4-33', [
                '21140300030015', '21140300040031', '21140300040032', '21140300040033'
            ]);
        });
    });
});
