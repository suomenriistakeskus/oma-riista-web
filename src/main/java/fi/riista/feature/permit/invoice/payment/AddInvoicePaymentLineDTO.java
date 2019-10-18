package fi.riista.feature.permit.invoice.payment;

import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class AddInvoicePaymentLineDTO {

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private BigDecimal amount;

    public AddInvoicePaymentLineDTO() {
    }

    public AddInvoicePaymentLineDTO(@Nonnull final LocalDate paymentDate, @Nonnull final BigDecimal amount) {
        this.paymentDate = requireNonNull(paymentDate);
        this.amount = requireNonNull(amount);
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(final LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }
}
