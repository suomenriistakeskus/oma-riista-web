package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.error.MessageExposableValidationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static fi.riista.feature.permit.invoice.payment.InvoicePaymentUpdateErrorType.CANNOT_REMOVE_ACCOUNT_TRANSFER_BASED_INVOICE_PAYMENT_LINE;
import static fi.riista.feature.permit.invoice.payment.InvoicePaymentUpdateErrorType.CAN_ADD_PAYMENT_LINES_ONLY_FOR_PERMIT_HARVEST_INVOICES;
import static fi.riista.feature.permit.invoice.payment.InvoicePaymentUpdateErrorType.SUM_OF_PAYMENT_LINES_MUST_NOT_BE_NEGATIVE;
import static java.util.Objects.requireNonNull;

public class InvoicePaymentLineUpdateException extends MessageExposableValidationException {

    public static InvoicePaymentLineUpdateException canAddPaymentLinesOnlyForPermitHarvestInvoices(final EnumLocaliser localiser) {
        return from(CAN_ADD_PAYMENT_LINES_ONLY_FOR_PERMIT_HARVEST_INVOICES, localiser);
    }

    public static InvoicePaymentLineUpdateException cannotRemoveAccountTransferBasedInvoicePaymentLine(final EnumLocaliser localiser) {
        return from(CANNOT_REMOVE_ACCOUNT_TRANSFER_BASED_INVOICE_PAYMENT_LINE, localiser);
    }

    public static InvoicePaymentLineUpdateException sumMustNotBeNegative(final EnumLocaliser localiser) {
        return from(SUM_OF_PAYMENT_LINES_MUST_NOT_BE_NEGATIVE, localiser);
    }

    static InvoicePaymentLineUpdateException from(@Nonnull final InvoicePaymentUpdateErrorType errorType,
                                                  @Nullable final EnumLocaliser localiser) {

        requireNonNull(errorType);

        // localiser may be null in unit tests.
        final String message = localiser != null ? localiser.getTranslation(errorType) : errorType.name();

        return new InvoicePaymentLineUpdateException(message);
    }

    public InvoicePaymentLineUpdateException(final String message) {
        super(message);
    }
}
