package fi.riista.feature.permit.invoice;

public enum InvoiceStateChangeEventType {

    // When invoice is swtiched to paper route
    ELECTRONIC_INVOICING_DISABLED,

    // When overdue reminder PDF is printed. Will see later is this state really needed?
    OVERDUE_REMINDER_CREATED,

    // When invoice is made void i.e. when it is not legally binding anymore
    VOIDED

}
