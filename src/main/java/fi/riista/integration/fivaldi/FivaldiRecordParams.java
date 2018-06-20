package fi.riista.integration.fivaldi;

import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class FivaldiRecordParams {

    private static final int DEFAULT_PAYMENT_DAYS = 21;

    private final int invoiceNumber;

    private final BigDecimal amount;
    private final LocalDate invoiceDate;
    private final LocalDate netDueDate;
    private final long creditorReferenceNumber;

    private final FivaldiPaymentMethod paymentMethod;
    private final boolean production;

    public FivaldiRecordParams(final int invoiceNumber,
                               @Nonnull final BigDecimal amount,
                               @Nonnull final LocalDate invoiceDate,
                               final long creditorReferenceNumber,
                               @Nonnull final FivaldiPaymentMethod paymentMethod) {

        this(invoiceNumber,
                amount,
                invoiceDate,
                invoiceDate.plusDays(DEFAULT_PAYMENT_DAYS),
                creditorReferenceNumber,
                paymentMethod,
                true);
    }

    public FivaldiRecordParams(final int invoiceNumber,
                               @Nonnull final BigDecimal amount,
                               @Nonnull final LocalDate invoiceDate,
                               @Nonnull final LocalDate netDueDate,
                               final long creditorReferenceNumber,
                               @Nonnull final FivaldiPaymentMethod paymentMethod,
                               final boolean production) {

        this.invoiceNumber = invoiceNumber;
        this.amount = requireNonNull(amount, "amount is null");
        this.invoiceDate = requireNonNull(invoiceDate, "invoiceDate is null");
        this.netDueDate = requireNonNull(netDueDate, "netDueDate is null");
        this.creditorReferenceNumber = creditorReferenceNumber;
        this.paymentMethod = requireNonNull(paymentMethod, "paymentMethod is null");
        this.production = production;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public LocalDate getNetDueDate() {
        return netDueDate;
    }

    public long getCreditorReferenceNumber() {
        return creditorReferenceNumber;
    }

    public FivaldiPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public boolean isProduction() {
        return production;
    }
}
