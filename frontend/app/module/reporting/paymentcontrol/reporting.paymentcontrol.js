'use strict';

angular.module('app.reporting.paymentcontrol', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting.paymentcontrol', {
                url: '/paymentcontrol',
                templateUrl: 'reporting/paymentcontrol/layout.html',
                controllerAs: '$ctrl',
                controller: 'ReportingPaymentControlController',
                resolve: {
                    availableHuntingYears: function (HuntingYearService) {
                        var currentHuntingYear = HuntingYearService.getCurrent();
                        return _.range(2018, currentHuntingYear + 1);
                    },
                    mooselikeSpecies: function (Species) {
                        return Species.getPermitBasedMooselike();
                    }
                }
            });
    })

    .controller('ReportingPaymentControlController', function (FormPostService, availableHuntingYears,
                                                               mooselikeSpecies) {
        var $ctrl = this;

        $ctrl.availableHuntingYears = availableHuntingYears;
        $ctrl.mooselikeSpecies = mooselikeSpecies;

        $ctrl.$onInit = function () {
            $ctrl.huntingYear = _.max($ctrl.availableHuntingYears);
        };

        $ctrl.selectHuntingYear = function (huntingYear) {
            $ctrl.huntingYear = huntingYear;
        };

        $ctrl.exportUnfinishedMoosePermitsToExcel = function () {
            var url = '/api/v1/moosepermit/unfinished/' + $ctrl.huntingYear +  '/excel';
            FormPostService.submitFormUsingBlankTarget(url, {});
        };

        $ctrl.exportMooselikeHarvestPaymentSummaryToExcel = function (gameSpeciesCode) {
            var url = '/api/v1/moosepermit/paymentsummary/' + $ctrl.huntingYear +  '/' + gameSpeciesCode + '/excel';
            FormPostService.submitFormUsingBlankTarget(url, {});
        };
    });
