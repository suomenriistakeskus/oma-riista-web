(function () {
    "use strict";

    angular.module('app.jht.invoice.search', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('jht.invoice.search', {
                    url: '/search?{invoiceType:[1-2]}&{applicationNumber:[0-9]{5,8}}',
                    params: {
                        invoiceType: null,
                        applicationNumber: null
                    },
                    templateUrl: 'jht/invoice/search/layout.html',
                    controller: 'InvoiceSearchController',
                    controllerAs: '$ctrl',
                    resolve: {
                        invoiceTypeRestriction: function ($stateParams, InvoiceType) {
                            switch ($stateParams.invoiceType) {
                                case '1':
                                    return InvoiceType.PERMIT_PROCESSING;
                                case '2':
                                    return InvoiceType.PERMIT_HARVEST;
                                default:
                                    return null;
                            }
                        },
                        availableHuntingYears: function () {
                            var beginYear = 2018;
                            var endYear = new Date().getFullYear();
                            return _.range(beginYear, endYear + 1);
                        },
                        filters: function ($stateParams, InvoiceType, HuntingYearService,
                                           invoiceTypeRestriction, availableHuntingYears) {
                            var defaultHuntingYear = invoiceTypeRestriction === InvoiceType.PERMIT_PROCESSING
                                ? _.last(availableHuntingYears)
                                : HuntingYearService.getCurrent(); // For tracking mooselike harvest invoices

                            var filters = {
                                invoiceNumber: null,
                                creditorReference: null,
                                applicationNumber: null,
                                type: invoiceTypeRestriction,
                                paymentState: null,
                                deliveryType: null,
                                huntingYear: defaultHuntingYear,
                                gameSpeciesCode: null,
                                rkaOfficialCode: null,
                                rhyOfficialCode: null,
                                beginDate: null,
                                endDate: null
                            };

                            var applicationNumber = parseInt($stateParams.applicationNumber);

                            if (_.isFinite(applicationNumber)) {
                                filters.applicationNumber = applicationNumber;
                            }

                            return filters;
                        },
                        availableSpecies: function (MooselikeSpecies) {
                            return MooselikeSpecies.getPermitBased();
                        },
                        initialResults: function (InvoiceService, filters) {
                            if (_.isFinite(filters.applicationNumber)) {
                                return InvoiceService.search(filters);
                            }

                            return null;
                        }
                    }
                });
        })

        .service('InvoicePaymentStates', function (InvoiceType) {
            this.list = function (invoiceType) {
                switch (invoiceType) {
                    case InvoiceType.PERMIT_PROCESSING:
                        return ['PAID', 'OTHER'];
                    case InvoiceType.PERMIT_HARVEST:
                        return ['PAID', 'PAYMENT_SUM_DIFFERS', 'OTHER'];
                    default:
                        return [];
                }
            };
        })

        .service('InvoiceDeliveryTypes', function (InvoiceDeliveryType) {
            this.list = function () {
                return [InvoiceDeliveryType.ELECTRONIC, InvoiceDeliveryType.MAIL];
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
                    method: 'PUT',
                    url: urlPrefix + '/:invoiceId/disableelectronicinvoicing',
                    params: {invoiceId: '@invoiceId'}
                },
                addPaymentLine: {
                    method: 'POST',
                    url: urlPrefix + '/:invoiceId/paymentline',
                    params: {invoiceId: '@invoiceId'}
                },
                removePaymentLine: {
                    method: 'DELETE',
                    url: urlPrefix + '/paymentline/:invoicePaymentLineId',
                    params: {invoicePaymentLineId: '@invoicePaymentLineId'}
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

        .controller('InvoiceSearchController', function ($state, $uibModal, ActiveRoleService, AvailableRoleService,
                                                         InvoiceService, ModeratorPrivileges, availableHuntingYears,
                                                         availableSpecies, filters, initialResults) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.filters = filters;
                $ctrl.availableHuntingYears = availableHuntingYears;
                $ctrl.availableSpecies = availableSpecies;
                $ctrl.results = initialResults;
            };

            $ctrl.search = function () {
                InvoiceService.search($ctrl.filters).then(function (searchResults) {
                    $ctrl.results = searchResults;
                });
            };

            $ctrl.openInvoice = function (invoiceId) {
                InvoiceService.getById(invoiceId).then(function (invoice) {
                    var modalInstance = $uibModal.open({
                        templateUrl: 'jht/invoice/search/show/show-invoice.html',
                        size: 'lg',
                        controllerAs: '$ctrl',
                        controller: 'InvoiceController',
                        resolve: {
                            invoice: _.constant(invoice),
                            paymentAlterPermissionGranted: function () {
                                return ActiveRoleService.isAdmin() ||
                                    AvailableRoleService.hasPrivilege(ModeratorPrivileges.alterInvoicePayment);
                            }
                        }
                    });

                    modalInstance.result.then(function (result) {
                        var permitDecisionId = _.get(result, 'permitDecisionId');
                        var updated = _.get(result, 'updated', false);

                        if (_.isFinite(permitDecisionId)) {
                            $state.go('jht.decision.application.overview', {decisionId: permitDecisionId});
                        } else if (updated) {
                            // Trigger search if invoice state was updated.
                            $ctrl.search();
                        }
                    });
                });
            };
        })

        .component('moderatorInvoiceSearchFilters', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-filters.html',
            bindings: {
                filters: '<',
                availableHuntingYears: '<',
                availableSpecies: '<',
                search: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.isExactNumberSearchActive = function () {
                    return _.isFinite($ctrl.filters.invoiceNumber) || _.isFinite($ctrl.filters.applicationNumber);
                };
            }
        })

        .component('invoiceSearchNumberFilters', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-numbers.html',
            bindings: {
                filters: '<'
            },
            controller: function () {
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
            }
        })

        .component('invoiceSearchYearFilter', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-year.html',
            bindings: {
                filters: '<',
                availableHuntingYears: '<',
                isDisabled: '&'
            },
            controller: function (HuntingYearService) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.huntingYears = _.map($ctrl.availableHuntingYears, HuntingYearService.toObj);
                };
            }
        })

        .component('invoiceSearchTemporalFilter', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-temporal.html',
            bindings: {
                filters: '<',
                availableHuntingYears: '<',
                isDisabled: '&'
            }
        })

        .component('invoiceSearchSpeciesFilter', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-species.html',
            bindings: {
                filters: '<',
                availableSpecies: '<',
                isDisabled: '&'
            }
        })

        .component('invoiceSearchPaymentStateFilter', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-payment-state.html',
            bindings: {
                filters: '<',
                isDisabled: '&'
            },
            controller: function (InvoicePaymentStates) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.paymentStates = InvoicePaymentStates.list($ctrl.filters.type);
                };
            }
        })

        .component('invoiceSearchDeliveryTypeFilter', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-delivery-type.html',
            bindings: {
                filters: '<',
                isDisabled: '&'
            },
            controller: function (InvoiceDeliveryTypes) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.deliveryTypes = InvoiceDeliveryTypes.list();
                };
            }
        })

        .component('invoiceSearchOrganisationFilter', {
            templateUrl: 'jht/invoice/search/filter/invoice-search-organisation.html',
            bindings: {
                filters: '<',
                isDisabled: '&'
            }
        })

        .component('processingInvoiceSearchResults', {
            templateUrl: 'jht/invoice/search/list/processing-invoice-search-results.html',
            bindings: {
                results: '<',
                openInvoice: '&'
            }
        })

        .component('harvestInvoiceSearchResults', {
            templateUrl: 'jht/invoice/search/list/harvest-invoice-search-results.html',
            bindings: {
                results: '<',
                openInvoice: '&'
            }
        })

        .component('genericInvoiceSearchResults', {
            templateUrl: 'jht/invoice/search/list/generic-invoice-search-results.html',
            bindings: {
                results: '<',
                openInvoice: '&'
            }
        })

        .component('invoiceStateRepresentation', {
            templateUrl: 'jht/invoice/search/invoice-state-representation.html',
            bindings: {
                state: '<',
                showIconOnly: '<',
                largeIcon: '<'
            },
            controller: function (InvoiceState) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.showIconOnly = !!$ctrl.showIconOnly;
                    $ctrl.largeIcon = !!$ctrl.largeIcon;
                };

                $ctrl.getColorClass = function () {
                    switch ($ctrl.state) {
                        case InvoiceState.DELIVERED:
                        case InvoiceState.PAID:
                            return 'text-primary';
                        case InvoiceState.VOID:
                            return 'text-warning';
                        default:
                        case InvoiceState.CREATED:
                        case InvoiceState.OVERDUE:
                        case InvoiceState.REMINDER:
                        case InvoiceState.UNKNOWN:
                            return 'text-danger';
                    }
                };

                $ctrl.getIconClass = function () {
                    switch ($ctrl.state) {
                        case InvoiceState.CREATED:
                        case InvoiceState.DELIVERED:
                            return 'fa-paper-plane';
                        case InvoiceState.PAID:
                        case InvoiceState.VOID:
                            return 'fa-check';
                        case InvoiceState.OVERDUE:
                            return 'fa-times';
                        case InvoiceState.REMINDER:
                            return 'fa-exclamation-triangle';
                        default:
                        case InvoiceState.UNKNOWN:
                            return 'fa-question';
                    }
                };
            }
        })

        .component('invoiceListItemDeliveryType', {
            templateUrl: 'jht/invoice/search/list/invoice-list-item-delivery-type.html',
            bindings: {
                value: '<'
            }
        })

        .controller('InvoiceController', function ($uibModalInstance, FileSaver, HttpGetBlob, Invoice,
                                                   InvoicePaymentLineModal, InvoicePdf, InvoiceService, InvoiceState,
                                                   NotificationService, invoice, paymentAlterPermissionGranted) {
            var $ctrl = this;
            var invoiceId = invoice.id;

            $ctrl.invoice = invoice;
            $ctrl.paymentAlterPermissionGranted = paymentAlterPermissionGranted;
            $ctrl.updated = false;

            $ctrl.close = function () {
                $uibModalInstance.close({updated: $ctrl.updated});
            };

            $ctrl.canDisableElectronicInvoicing = function () {
                var invoice = $ctrl.invoice;

                if (!invoice.electronicInvoicingEnabled) {
                    return false;
                }

                var inPaidState = invoice.state === InvoiceState.PAID;

                switch (invoice.type) {
                    case 'PERMIT_PROCESSING':
                        return !inPaidState;
                    case 'PERMIT_HARVEST':
                        if (inPaidState && invoice.receivedAmount === 0) {
                            return false;
                        }
                        return invoice.paymentAmount > invoice.receivedAmount;
                    default:
                        return false;
                }
            };

            var updateInvoice = function (invoice) {
                $ctrl.invoice = invoice;
                $ctrl.updated = true;

                NotificationService.showDefaultSuccess();
            };

            $ctrl.disableElectronicInvoicing = function () {
                InvoiceService.disableElectronicInvoicing(invoiceId).then(updateInvoice);
            };

            $ctrl.downloadPdf = function () {
                InvoicePdf.download(invoiceId);
            };

            $ctrl.isPrintReminderButtonVisible = function () {
                var invoice = $ctrl.invoice;
                return !invoice.electronicInvoicingEnabled && invoice.overdue;
            };

            $ctrl.printReminder = function () {
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

            $ctrl.openPermitDecision = function () {
                $uibModalInstance.close({permitDecisionId: $ctrl.invoice.permitDecisionId});
            };

            $ctrl.addPayment = function () {
                InvoicePaymentLineModal.openAddPaymentDialog($ctrl.invoice).then(updateInvoice, _.noop);
            };

            $ctrl.removePayment = function (paymentId) {
                var params = {invoicePaymentLineId: paymentId};
                return Invoice.removePaymentLine(params, null).$promise.then(updateInvoice, _.noop);
            };
        })

        .service('InvoicePaymentLineModal', function ($q, $uibModal, Invoice) {
            var self = this;

            self.openAddPaymentDialog = function (invoice) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'jht/invoice/search/show/add-invoice-payment-line.html',
                    resolve: {
                        invoiceDate: _.constant(invoice.invoiceDate)
                    },
                    controllerAs: '$ctrl',
                    controller: function ($uibModalInstance, invoiceDate) {
                        var $ctrl = this;

                        // Min payment date is selected here quite arbitrarily but should leave enough marginal.
                        $ctrl.minPaymentDate = moment(invoiceDate).subtract(6, 'months');
                        $ctrl.payment = {paymentDate: null, amount: null};

                        $ctrl.save = function () {
                            $uibModalInstance.close($ctrl.payment);
                        };

                        $ctrl.cancel = function () {
                            $uibModalInstance.dismiss('cancel');
                        };
                    }
                });

                return modalInstance.result.then(
                    function (payment) {
                        return Invoice.addPaymentLine({invoiceId: invoice.id}, payment).$promise;
                    },
                    function (errReason) {
                        return $q.reject(errReason);
                    });
            };
        })

        .component('showProcessingInvoiceDetails', {
            templateUrl: 'jht/invoice/search/show/layout-processing-invoice.html',
            bindings: {
                invoice: '<'
            }
        })

        .component('showHarvestInvoiceDetails', {
            templateUrl: 'jht/invoice/search/show/layout-harvest-invoice.html',
            bindings: {
                invoice: '<',
                paymentAlterPermissionGranted: '<',
                addPayment: '&',
                removePayment: '&'
            }
        })

        .component('showInvoiceMoney', {
            templateUrl: 'jht/invoice/search/show/show-invoice-money.html',
            bindings: {
                titleKey: '<',
                sum: '<'
            }
        })

        .component('showInvoiceRecipient', {
            templateUrl: 'jht/invoice/search/show/show-invoice-recipient.html',
            bindings: {
                recipient: '<'
            }
        })

        .component('showInvoiceDetails', {
            templateUrl: 'jht/invoice/search/show/show-invoice-details.html',
            bindings: {
                invoice: '<'
            }
        })

        .component('showInvoiceItemization', {
            templateUrl: 'jht/invoice/search/show/show-invoice-itemization.html',
            bindings: {
                invoice: '<'
            }
        })

        .component('showInvoiceFivaldiState', {
            templateUrl: 'jht/invoice/search/show/show-invoice-fivaldi-state.html',
            bindings: {
                fivaldiState: '<'
            }
        })

        .component('invoiceFivaldiStateRepresentation', {
            templateUrl: 'jht/invoice/search/show/invoice-fivaldi-state-representation.html',
            bindings: {
                state: '<'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.getColorClass = function () {
                    switch ($ctrl.state) {
                        case 'NOT_BATCHED':
                        case 'BATCHED':
                            return 'text-danger';
                        case 'DOWNLOADED':
                            return 'text-primary';
                        default:
                            return '';
                    }
                };

                $ctrl.getIconClass = function () {
                    switch ($ctrl.state) {
                        case 'NOT_BATCHED':
                            return 'fa-hourglass';
                        case 'BATCHED':
                            return 'fa-download';
                        case 'DOWNLOADED':
                            return 'fa-check';
                        default:
                            return '';
                    }
                };
            }
        })

        .component('showInvoicePayments', {
            templateUrl: 'jht/invoice/search/show/show-invoice-payments.html',
            bindings: {
                payments: '<',
                paymentAlterPermissionGranted: '<',
                addPayment: '&',
                removePayment: '&'
            }
        })

        .component('showInvoiceActions', {
            templateUrl: 'jht/invoice/search/show/show-invoice-actions.html',
            bindings: {
                actions: '<'
            }
        });

})();
