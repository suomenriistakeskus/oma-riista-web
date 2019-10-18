package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.permit.invoice.InvoiceState;

public enum InvoiceDisplayState {

    // Invoice is created but not further processed
    CREATED,

    // Invoice is delivered to recipient
    DELIVERED,

    // Invoice is paid
    PAID,

    // Invoice is past due, not paid as of its due date
    OVERDUE,

    // Overdue reminder is sent for invoice
    REMINDER,

    // Invoice is marked void. Invoices cannot be deleted but marking it void changes the invoice
    // amount effectively to zero and keeps a record of the invoice.
    VOID,

    UNKNOWN;

    public static InvoiceDisplayState from(final InvoiceState state, final boolean dueDateInPast) {
        switch (state) {
            case CREATED:
                return CREATED;
            case DELIVERED:
                return dueDateInPast ? OVERDUE : DELIVERED;
            case PAID:
                return PAID;
            case REMINDER:
                return REMINDER;
            case VOID:
                return VOID;
            case UNKNOWN:
            default:
                return UNKNOWN;
        }
    }
}
