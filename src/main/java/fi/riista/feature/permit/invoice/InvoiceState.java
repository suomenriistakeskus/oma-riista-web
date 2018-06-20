package fi.riista.feature.permit.invoice;

public enum InvoiceState {

    // Invoice is created but not further processed
    CREATED,

    // Invoice is delivered to recipient
    DELIVERED,

    // Invoice is paid
    PAID,

    // Reminder is sent; Paytrail payment must not be possible anymore
    REMINDER,

    // Payment state is unknown, because payment failed etc.
    UNKNOWN,

    // Invoice is marked void. Invoices cannot be deleted but marking it void changes the invoice
    // amount effectively to zero and keeps a record of the invoice.
    VOID;

}
