'use strict';

angular.module('app.jht.hirvijyvitys', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.hirvijyvitys', {
            url: '/hirvijyvitys',
            templateUrl: 'jht/mooselike/hirvijyvitys/hirvijyvitys.html',
            controller: 'HirvijyvitysExcelController',
            controllerAs: '$ctrl',
            resolve: {
                areas: function (OrganisationsByArea) {
                    return OrganisationsByArea.queryActive().$promise;
                },
                huntingYears: function (HuntingYearService) {
                    var currentHuntingYear = HuntingYearService.getCurrent();

                    // Include next huntingYear + 1 since range end is exclusive
                    return _.range(2019, currentHuntingYear + 1 + 1);
                }
            }
        });
    })
    .controller('HirvijyvitysExcelController', function (FetchAndSaveBlob, areas, huntingYears) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.areas = areas;
            $ctrl.selectedArea = null;
            $ctrl.huntingYears = huntingYears;
            $ctrl.selectedHuntingYear = _.last($ctrl.huntingYears);
        };

        $ctrl.exportExcel = function (officialCode) {
            FetchAndSaveBlob.get('api/v1/permitplanning/' + $ctrl.selectedHuntingYear + '/' + officialCode + '/excel');
        };
    })
;
