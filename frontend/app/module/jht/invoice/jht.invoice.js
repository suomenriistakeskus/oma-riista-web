"use strict";

angular
    .module('app.jht.invoice', [
        'app.jht.invoice.search',
        'app.jht.invoice.fivaldi'
    ])

    .config(function ($stateProvider) {
        $stateProvider.state('jht.invoice', {
            abstract: true,
            url: '/invoice',
            template: '<ui-view autoscroll="false"/>'
        });
    })

    .constant('InvoiceType', {
        PERMIT_PROCESSING: 'PERMIT_PROCESSING',
        PERMIT_HARVEST: 'PERMIT_HARVEST'
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

    .constant('InvoiceDeliveryType', {
        ELECTRONIC: 'ELECTRONIC',
        MAIL: 'MAIL'
    });
