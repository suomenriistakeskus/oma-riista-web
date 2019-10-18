'use strict';

angular.module('app.harvestpermit.management.payment', [])
    .factory('PermitInvoice', function ($resource) {
        var apiPrefix = '/api/v1/harvestpermit/:permitId/invoice/:id';

        return $resource(apiPrefix, {id: '@id', permitId: '@permitId'}, {
            getPaymentForm: {method: 'POST', url: apiPrefix + '/payment'},
            listDueByPermit: {method: 'GET', url: apiPrefix + '/due', isArray: true},
            listPaidByPermit: {method: 'GET', url: apiPrefix + '/paid', isArray: true}
        });
    })

    .config(function ($stateProvider) {
        $stateProvider
            .state('permitmanagement.payment', {
                url: '/payments',
                templateUrl: 'harvestpermit/management/payment/layout.html',
                abstract: true,
                resolve: {
                    permitId: function ($stateParams) {
                        return _.parseInt($stateParams.permitId);
                    }
                },
                controllerAs: '$navCtrl',
                controller: function ($state, duePaymentsCount, partiallyPaidInvoiceCount) {
                    var $navCtrl = this;

                    $navCtrl.$onInit = function () {
                        $navCtrl.duePaymentsCount = duePaymentsCount;
                        $navCtrl.partiallyPaidInvoiceCount = partiallyPaidInvoiceCount;
                    };

                    $navCtrl.showNav = function () {
                        return $state.includes('permitmanagement.payment.due') ||
                            $state.includes('permitmanagement.payment.paid');
                    };
                }
            })
            .state('permitmanagement.payment.due', {
                url: '/list-due',
                templateUrl: 'harvestpermit/management/payment/list-due.html',
                controller: 'HarvestPermitPaymentDueListController',
                controllerAs: '$ctrl',
                resolve: {
                    paymentList: function (PermitInvoice, permitId) {
                        return PermitInvoice.listDueByPermit({permitId: permitId}).$promise;
                    }
                }
            })
            .state('permitmanagement.payment.paid', {
                url: '/list-paid',
                templateUrl: 'harvestpermit/management/payment/list-paid.html',
                controller: 'HarvestPermitPaymentPaidListController',
                controllerAs: '$ctrl',
                resolve: {
                    paymentList: function (PermitInvoice, permitId) {
                        return PermitInvoice.listPaidByPermit({permitId: permitId}).$promise;
                    }
                }
            })
            .state('permitmanagement.payment.confirmation', {
                url: '/confirmation/{invoiceId:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/management/payment/confirmation.html',
                controller: 'HarvestPermitPaymentConfirmationController',
                controllerAs: '$ctrl',
                resolve: {
                    invoiceId: function ($stateParams) {
                        return _.parseInt($stateParams.invoiceId);
                    },
                    invoice: function (PermitInvoice, permitId, invoiceId) {
                        return PermitInvoice.get({permitId: permitId, id: invoiceId}).$promise;
                    },
                    paymentForm: function (PermitInvoice, permitId, invoiceId) {
                        return PermitInvoice.getPaymentForm({permitId: permitId, id: invoiceId}).$promise;
                    }
                }
            })
            .state('permitmanagement.payment.receipt', {
                url: '/receipt/{invoiceId:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/management/payment/receipt.html',
                controller: 'HarvestPermitPaymentReceiptController',
                controllerAs: '$ctrl',
                resolve: {
                    invoiceId: function ($stateParams) {
                        return _.parseInt($stateParams.invoiceId);
                    },
                    invoice: function (PermitInvoice, permitId, invoiceId) {
                        return PermitInvoice.get({permitId: permitId, id: invoiceId}).$promise;
                    }
                }
            })
        ;
    })

    .controller('HarvestPermitPaymentDueListController', function ($state, permitId, paymentList) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.paymentList = paymentList;
        };

        $ctrl.startPayment = function (invoice) {
            $state.go('permitmanagement.payment.confirmation', {
                permitId: permitId,
                invoiceId: invoice.id
            });
        };
    })

    .controller('HarvestPermitPaymentPaidListController', function (FormPostService, paymentList, permitId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.paymentList = paymentList;
        };

        $ctrl.downloadReceipt = function (invoice) {
            FormPostService.submitFormUsingSelfTarget('/api/v1/harvestpermit/' + permitId + '/invoice/' + invoice.id + '/receipt');
        };
    })

    .controller('HarvestPermitPaymentConfirmationController', function (FormPostService, invoice, paymentForm) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.invoice = invoice;
        };

        $ctrl.makePayment = function () {
            FormPostService.submitFormUsingSelfTarget('https://payment.paytrail.com/e2', paymentForm, true);
        };
    })

    .controller('HarvestPermitPaymentReceiptController', function (FormPostService, permitId, invoice) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.invoice = invoice;
        };

        $ctrl.downloadReceipt = function () {
            FormPostService.submitFormUsingSelfTarget('/api/v1/harvestpermit/' + permitId + '/invoice/' + invoice.id + '/receipt');
        };
    });
