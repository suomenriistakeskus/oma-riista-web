package fi.riista.feature.permit.invoice.payment;

import fi.riista.util.LocalisedEnum;

public enum InvoicePaymentUpdateErrorType implements LocalisedEnum {

    CAN_ADD_PAYMENT_LINES_ONLY_FOR_PERMIT_HARVEST_INVOICES,
    CANNOT_REMOVE_ACCOUNT_TRANSFER_BASED_INVOICE_PAYMENT_LINE,
    SUM_OF_PAYMENT_LINES_MUST_NOT_BE_NEGATIVE
}
