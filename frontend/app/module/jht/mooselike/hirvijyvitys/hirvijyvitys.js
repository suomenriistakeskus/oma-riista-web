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
                }
            }
        });
    })
    .controller('HirvijyvitysExcelController', function (FetchAndSaveBlob, areas) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.areas = areas;
            $ctrl.selectedArea = null;
        };

        $ctrl.exportExcel = function (officialCode) {
                FetchAndSaveBlob.get('api/v1/permitplanning/' + officialCode + '/excel');
        };
    })
;
