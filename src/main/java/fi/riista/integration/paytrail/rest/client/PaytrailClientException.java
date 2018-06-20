package fi.riista.integration.paytrail.rest.client;

import fi.riista.integration.paytrail.rest.model.ErrorMessage;

public class PaytrailClientException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public PaytrailClientException(final ErrorMessage msg) {
        super(String.format("Paytrail failure code: %s message: %s", msg.getErrorCode(), msg.getErrorMessage()));

        this.errorCode = msg.getErrorCode();
        this.errorMessage = msg.getErrorMessage();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
