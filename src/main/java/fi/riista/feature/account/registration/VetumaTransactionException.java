package fi.riista.feature.account.registration;

import fi.riista.feature.error.MessageExposableValidationException;

public class VetumaTransactionException extends MessageExposableValidationException {
    public VetumaTransactionException(final String message) {
        super(message);
    }
}
