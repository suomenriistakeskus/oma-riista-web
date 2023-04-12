package fi.riista.integration.paytrail.event;

public enum PaytrailPaymentEventType {
    REDIRECT_SUCCESS,
    REDIRECT_CANCEL,
    CALLBACK_SUCCESS,
    CALLBACK_CANCEL
}
