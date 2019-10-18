(function () {
    "use strict";

    angular.module('app.jht.invoice.fivaldi', [])
        .config(function ($stateProvider) {
            $stateProvider.state('jht.invoice.fivaldi', {
                url: '/fivaldi',
                templateUrl: 'jht/invoice/fivaldi/fivaldi.html',
                controller: 'FivaldiController',
                controllerAs: '$ctrl',
                resolve: {
                    newBatches: function (FivaldiInvoiceBatch) {
                        return FivaldiInvoiceBatch.listNew().$promise;
                    },
                    availableYearMonths: function (FivaldiInvoiceBatch) {
                        return FivaldiInvoiceBatch.listAvailableYearMonths().$promise;
                    },
                    selectedYearMonth: function (availableYearMonths) {
                        return availableYearMonths.length > 0 ? availableYearMonths[0] : null;
                    },
                    loadPreviousBatchesFn: function (FivaldiInvoiceBatch) {
                        return function (yearMonth) {
                            if (!yearMonth) {
                                return [];
                            }

                            var tokens = yearMonth.split('-');
                            var params = {
                                year: tokens[0],
                                month: tokens[1]
                            };

                            return FivaldiInvoiceBatch.listPrevious(params).$promise;
                        };
                    },
                    previousBatches: function (loadPreviousBatchesFn, selectedYearMonth) {
                        return loadPreviousBatchesFn(selectedYearMonth);
                    }
                }
            });
        })

        .factory('FivaldiInvoiceBatch', function ($resource) {
            var urlPrefix = '/api/v1/fivaldi';

            return $resource(urlPrefix, {}, {
                listNew: {
                    method: 'GET',
                    url: urlPrefix + '/batch/new',
                    isArray: true
                },
                listPrevious: {
                    method: 'GET',
                    url: urlPrefix + '/batch/:year/:month',
                    params: {
                        year: '@year',
                        month: '@month'
                    },
                    isArray: true
                },
                listAvailableYearMonths: {
                    method: 'GET',
                    url: urlPrefix + '/availableyearmonths',
                    isArray: true
                }
            });
        })

        .controller('FivaldiController', function (availableYearMonths, loadPreviousBatchesFn, newBatches,
                                                   previousBatches, selectedYearMonth) {
            var $ctrl = this;

            $ctrl.newBatches = newBatches;
            $ctrl.previousBatches = previousBatches;

            $ctrl.availableYearMonths = availableYearMonths;
            $ctrl.selectedYearMonth = selectedYearMonth;

            $ctrl.reloadPreviousBatches = function () {
                loadPreviousBatchesFn($ctrl.selectedYearMonth)
                    .then(function (reloadedBatches) {
                        $ctrl.previousBatches = reloadedBatches;
                    });
            };
        })

        .component('fivaldiInvoiceBatches', {
            templateUrl: 'jht/invoice/fivaldi/fivaldi-batch-list.html',
            bindings: {
                batches: '<'
            },
            controller: function (FormPostService) {
                var $ctrl = this;

                $ctrl.downloadBatchFile = function (batchId) {
                    var url = '/api/v1/fivaldi/batch/' + batchId + '/file';
                    FormPostService.submitFormUsingBlankTarget(url);
                };
            }
        });

})();
