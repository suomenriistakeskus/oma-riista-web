'use strict';

angular.module('app.jht.largecarnivorereport', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.largecarnivorereport', {
            url: '/largecarnivorereport',
            templateUrl: 'jht/largecarnivorereport/largecarnivorereport.html',
            controllerAs: '$ctrl',
            controller: 'LargeCarnivoreReportController'
        });
    })
    .controller('LargeCarnivoreReportController', function (HuntingYearService, FetchAndSaveBlob) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.huntingYear = HuntingYearService.getCurrent();
            $ctrl.availableYears = _.range(2020, $ctrl.huntingYear + 1);
        };

        $ctrl.exportExcel = function () {
            FetchAndSaveBlob.get('api/v1/large-carnivore-report/' + $ctrl.huntingYear);
        };
    });
