(function () {
    "use strict";

    angular.module('app.rhy.annualreport', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('rhy.annualreport', {
                    url: '/annualreport',
                    templateUrl: 'rhy/annualreport/annualreport.html',
                    controller: 'AnnualReportController',
                    controllerAs: '$ctrl',
                    resolve: {
                        rhy: function (Rhys, rhyId) {
                            return Rhys.get({id: rhyId}).$promise;
                        },
                        availableYears: function (rhyId, AnnualStatisticsYears) {
                            return AnnualStatisticsYears.get({rhyId: rhyId}).$promise;
                        }
                    }
                });
        })
        .service('RhyAnnualReportService', function (FetchAndSaveBlob) {
            this.loadAnnualReport = function (rhyId, year) {
                return FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/' + rhyId + '/annualreport/' + year);
            };
        })
        .controller('AnnualReportController',
            function (FetchAndSaveBlob, RhyAnnualReportService, rhy, availableYears) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.rhy = rhy;

                    $ctrl.availableYears = availableYears;
                    $ctrl.calendarYear = _.last($ctrl.availableYears);
                };

                $ctrl.exportAnnualReport = function () {
                    RhyAnnualReportService.loadAnnualReport($ctrl.rhy.id, $ctrl.calendarYear);
                };

            });
})();