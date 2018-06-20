(function () {
    "use strict";

    angular.module('app.jht.payments', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('jht.payments', {
                    abstract: true,
                    url: '/payments',
                    template: '<ui-view autoscroll="false"/>'
                })
                .state('jht.payments.invoices', {
                    url: '/invoices?{applicationNumber:[0-9]{5,8}}',
                    params: {
                        applicationNumber: null
                    },
                    templateUrl: 'jht/payments/invoice-search.html',
                    controller: 'InvoiceSearchController',
                    controllerAs: '$ctrl',
                    resolve: {
                        filters: function ($stateParams) {
                            var filters = {
                                invoiceNumber: null,
                                creditorReference: null,
                                applicationNumber: null,
                                type: null,
                                deliveryType: null,
                                beginDate: null,
                                endDate: null
                            };

                            var applicationNumber = parseInt($stateParams.applicationNumber);

                            if (_.isFinite(applicationNumber)) {
                                filters.applicationNumber = applicationNumber;
                            }

                            return filters;
                        },
                        initialResults: function (InvoiceService, filters) {
                            var anyNonNull = _.chain(filters)
                                .any(function (val) {
                                    return !_.isNull(val);
                                })
                                .value();

                            if (anyNonNull) {
                                return InvoiceService.search(filters);
                            }

                            return null;
                        }
                    }
                })
                .state('jht.payments.fivaldi', {
                    url: '/fivaldi',
                    templateUrl: 'jht/payments/fivaldi.html',
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

        .constant('InvoiceType', {
            list: function () {
                return ['PERMIT_PROCESSING', 'PERMIT_HARVEST'];
            }
        })

        .constant('InvoiceState', {
            CREATED: 'CREATED',
            DELIVERED: 'DELIVERED',
            PAID: 'PAID',
            OVERDUE: 'OVERDUE',
            REMINDER: 'REMINDER',
            VOID: 'VOID',
            UNKNOWN: 'UNKNOWN'
        })

        .service('InvoiceDeliveryType', function () {
            var email = 'EMAIL',
                letter = 'LETTER';

            this.list = function () {
                return [email, letter];
            };
        })

        .factory('Invoice', function ($resource) {
            var urlPrefix = '/api/v1/invoice';

            return $resource(urlPrefix, {}, {
                get: {
                    method: 'GET',
                    url: urlPrefix + '/:invoiceId',
                    params: {invoiceId: '@invoiceId'}
                },
                search: {method: 'POST', isArray: true},
                disableElectronicInvoicing: {
                    method: 'POST',
                    url: urlPrefix + '/:invoiceId/disableelectronicinvoicing',
                    params: {invoiceId: '@invoiceId'}
                }
            });
        })

        .service('InvoiceService', function (Invoice) {
            this.getById = function (invoiceId) {
                return Invoice.get({invoiceId: invoiceId}).$promise;
            };

            this.search = function (searchParams) {
                return Invoice.search({}, searchParams).$promise;
            };

            this.disableElectronicInvoicing = function (invoiceId) {
                return Invoice.disableElectronicInvoicing({invoiceId: invoiceId}, null).$promise;
            };
        })

        .service('InvoicePdf', function () {
            this.download = function (invoiceId) {
                window.open('/api/v1/invoice/' + invoiceId + '/pdf');
            };

            this.getReminderUrl = function (invoiceId) {
                return '/api/v1/invoice/' + invoiceId + '/reminder/pdf';
            };
        })

        .service('InvoiceStateVisualization', function (InvoiceState) {
            this.getTextClass = function (state) {
                switch (state) {
                    case InvoiceState.DELIVERED:
                    case InvoiceState.PAID:
                        return 'text-primary';
                    case InvoiceState.VOID:
                        return 'text-info';
                    default:
                    case InvoiceState.CREATED:
                    case InvoiceState.OVERDUE:
                    case InvoiceState.REMINDER:
                    case InvoiceState.UNKNOWN:
                        return 'text-danger';
                }
            };

            this.getIconClass = function (state) {
                switch (state) {
                    case InvoiceState.CREATED:
                    case InvoiceState.DELIVERED:
                        return 'fa fa-paper-plane';
                    case InvoiceState.PAID:
                    case InvoiceState.VOID:
                        return 'fa fa-check';
                    case InvoiceState.OVERDUE:
                        return 'fa fa-times';
                    case InvoiceState.REMINDER:
                        return 'fa fa-exclamation-triangle';
                    default:
                    case InvoiceState.UNKNOWN:
                        return 'fa fa-question';
                }
            };
        })

        .service('InvoiceFivaldiStateVisualization', function () {
            this.getTextClass = function (state) {
                switch (state) {
                    case 'NOT_BATCHED':
                    case 'BATCHED':
                        return 'text-danger';
                    case 'DOWNLOADED':
                        return 'text-primary';
                    default:
                        return '';
                }
            };

            this.getIconClass = function (state) {
                switch (state) {
                    case 'NOT_BATCHED':
                        return 'fa fa-hourglass';
                    case 'BATCHED':
                        return 'fa fa-download';
                    case 'DOWNLOADED':
                        return 'fa fa-check';
                    default:
                        return '';
                }
            };
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

        .controller('InvoiceSearchController', function ($state, $uibModal, InvoiceService, filters, initialResults) {
            var $ctrl = this;

            $ctrl.filters = filters;
            $ctrl.results = initialResults;

            $ctrl.search = function () {
                InvoiceService.search($ctrl.filters).then(function (searchResults) {
                    $ctrl.results = searchResults;
                });
            };

            $ctrl.openInvoice = function (invoiceId) {
                InvoiceService.getById(invoiceId).then(function (invoice) {
                    var modalInstance = $uibModal.open({
                        templateUrl: 'jht/payments/show-invoice.html',
                        size: 'lg',
                        controllerAs: '$ctrl',
                        controller: 'InvoiceController',
                        resolve: {
                            invoice: _.constant(invoice)
                        }
                    });

                    modalInstance.result.then(function (result) {
                        var permitDecisionId = _.get(result, 'permitDecisionId');
                        var updated = _.get(result, 'updated', false);

                        if (_.isFinite(permitDecisionId)) {
                            $state.go('jht.decision.application.summary', {decisionId: permitDecisionId});
                        } else if (updated) {
                            // Trigger search if invoice state was updated.
                            $ctrl.search();
                        }
                    });
                });
            };
        })

        .controller('InvoiceController', function ($uibModalInstance, FileSaver, HttpGetBlob, InvoicePdf,
                                                   InvoiceService, InvoiceState, InvoiceStateVisualization,
                                                   InvoiceFivaldiStateVisualization, invoice) {
            var $modalCtrl = this;
            var invoiceId = invoice.id;

            $modalCtrl.invoice = invoice;
            $modalCtrl.updated = false;

            $modalCtrl.getStateClass = function () {
                return InvoiceStateVisualization.getTextClass($modalCtrl.invoice.state);
            };

            $modalCtrl.getStateIcon = function () {
                return InvoiceStateVisualization.getIconClass($modalCtrl.invoice.state);
            };

            $modalCtrl.getFivaldiStateClass = function () {
                return InvoiceFivaldiStateVisualization.getTextClass($modalCtrl.invoice.fivaldiState);
            };

            $modalCtrl.getFivaldiStateIcon = function () {
                return InvoiceFivaldiStateVisualization.getIconClass($modalCtrl.invoice.fivaldiState);
            };

            $modalCtrl.close = function () {
                $uibModalInstance.close({updated: $modalCtrl.updated});
            };

            $modalCtrl.canDisableElectronicInvoicing = function () {
                var invoice = $modalCtrl.invoice;
                return invoice.state !== InvoiceState.PAID && invoice.electronicInvoicingEnabled;
            };

            var updateInvoice = function (invoice) {
                $modalCtrl.invoice = invoice;
                $modalCtrl.updated = true;
            };

            $modalCtrl.disableElectronicInvoicing = function () {
                InvoiceService.disableElectronicInvoicing(invoiceId).then(updateInvoice);
            };

            $modalCtrl.downloadPdf = function () {
                InvoicePdf.download(invoiceId);
            };

            $modalCtrl.isPrintReminderButtonVisible = function () {
                var invoice = $modalCtrl.invoice;
                return !invoice.electronicInvoicingEnabled && invoice.overdue;
            };

            $modalCtrl.printReminder = function () {
                var reminderUrl = InvoicePdf.getReminderUrl(invoiceId);

                HttpGetBlob.get(reminderUrl, 'arraybuffer')
                    .then(function (response) {
                        var blob = response.data;
                        var filename = blob.name || 'maksumuistutus.pdf';

                        // Third parameter disableAutoBOM=true is likely unnecessary since input content is not XML.
                        FileSaver.saveAs(blob, filename, true);

                        InvoiceService.getById(invoiceId).then(updateInvoice);
                    });
            };

            $modalCtrl.openPermitDecision = function () {
                $uibModalInstance.close({permitDecisionId: $modalCtrl.invoice.permitDecisionId});
            };
        })

        .component('moderatorInvoiceSearchFilters', {
            templateUrl: 'jht/payments/invoice-search-filters.html',
            bindings: {
                filters: '<',
                search: '&'
            },
            controller: function (InvoiceDeliveryType, InvoiceType) {
                var $ctrl = this,
                    INVOICE_NUMBER = 'invoiceNumber',
                    CREDITOR_REFERENCE = 'creditorReference',
                    APPLICATION_NUMBER = 'applicationNumber',
                    previousByNumberSearchType = null,
                    previousValues = {};

                $ctrl.$onInit = function () {
                    $ctrl.byNumberSearchTypes = [INVOICE_NUMBER, CREDITOR_REFERENCE, APPLICATION_NUMBER];

                    var byNumberSearchType = _.find($ctrl.byNumberSearchTypes, function (type) {
                        return $ctrl.filters[type] !== null;
                    });

                    $ctrl.byNumberSearchType = byNumberSearchType || INVOICE_NUMBER;
                    previousByNumberSearchType = $ctrl.byNumberSearchType;

                    _.forEach($ctrl.byNumberSearchTypes, function (type) {
                        previousValues[type] = null;
                    });

                    $ctrl.invoiceTypes = InvoiceType.list();
                    $ctrl.deliveryTypes = InvoiceDeliveryType.list();
                };

                $ctrl.isInvoiceNumberFieldVisible = function () {
                    return $ctrl.byNumberSearchType === INVOICE_NUMBER;
                };

                $ctrl.isCreditorReferenceFieldVisible = function () {
                    return $ctrl.byNumberSearchType === CREDITOR_REFERENCE;
                };

                $ctrl.isApplicationNumberFieldVisible = function () {
                    return $ctrl.byNumberSearchType === APPLICATION_NUMBER;
                };

                var cacheValue = function (type) {
                    if (type) {
                        previousValues[type] = $ctrl.filters[type];
                        $ctrl.filters[type] = null;
                    }
                };

                var restoreValue = function (type) {
                    $ctrl.filters[type] = previousValues[type];
                };

                $ctrl.onByNumberSearchTypeChanged = function () {
                    cacheValue(previousByNumberSearchType);
                    restoreValue($ctrl.byNumberSearchType);
                    previousByNumberSearchType = $ctrl.byNumberSearchType;
                };

                $ctrl.isExactNumberSearchActive = function () {
                    return $ctrl.isInvoiceNumberFieldVisible() && _.isFinite($ctrl.filters.invoiceNumber)
                        || $ctrl.isApplicationNumberFieldVisible() && _.isFinite($ctrl.filters.applicationNumber);
                };
            }
        })

        .component('moderatorInvoiceSearchResults', {
            templateUrl: 'jht/payments/invoice-search-results.html',
            bindings: {
                results: '<',
                openInvoice: '&'
            },
            controller: function (InvoiceStateVisualization) {
                this.getStateClass = function (state) {
                    var textClass = InvoiceStateVisualization.getTextClass(state);
                    var iconClass = InvoiceStateVisualization.getIconClass(state);

                    return textClass + ' ' + iconClass;
                };
            }
        })

        .controller('FivaldiController', function (FivaldiInvoiceBatch, availableYearMonths, loadPreviousBatchesFn,
                                                   newBatches, previousBatches, selectedYearMonth) {
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
            templateUrl: 'jht/payments/fivaldi-batch-list.html',
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
