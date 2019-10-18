package fi.riista.feature.permit.invoice.harvest;

import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.util.NumberUtils.nullableSum;
import static java.util.Objects.requireNonNull;

public class InvoicePaymentAmountsDTO {

    public static final InvoicePaymentAmountsDTO ZEROS =
            new InvoicePaymentAmountsDTO(ZERO_MONETARY_AMOUNT, ZERO_MONETARY_AMOUNT, ZERO_MONETARY_AMOUNT);

    private final BigDecimal receivedAmount;
    private final BigDecimal surplusAmount;
    private final BigDecimal deficientAmount;

    public static final InvoicePaymentAmountsDTO sum(@Nullable final InvoicePaymentAmountsDTO a,
                                                     @Nullable final InvoicePaymentAmountsDTO b) {
        return new InvoicePaymentAmountsDTO(
                nullableSum(a, b, InvoicePaymentAmountsDTO::getReceivedAmount),
                nullableSum(a, b, InvoicePaymentAmountsDTO::getSurplusAmount),
                nullableSum(a, b, InvoicePaymentAmountsDTO::getDeficientAmount));
    }

    public static InvoicePaymentAmountsDTO reduce(@Nonnull final Stream<InvoicePaymentAmountsDTO> items) {
        requireNonNull(items);
        return items.reduce(ZEROS, InvoicePaymentAmountsDTO::sum);
    }

    public static <T> InvoicePaymentAmountsDTO sum(@Nonnull final Iterable<? extends T> items,
                                                   @Nonnull final Function<? super T, InvoicePaymentAmountsDTO> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    public InvoicePaymentAmountsDTO(@Nonnull final BigDecimal receivedAmount,
                                    @Nonnull final BigDecimal surplusAmount,
                                    @Nonnull final BigDecimal deficientAmount) {

        this.receivedAmount = requireNonNull(receivedAmount).setScale(2);
        this.surplusAmount = requireNonNull(surplusAmount).setScale(2);
        this.deficientAmount = requireNonNull(deficientAmount).setScale(2);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof InvoicePaymentAmountsDTO)) {
            return false;
        } else {
            final InvoicePaymentAmountsDTO that = (InvoicePaymentAmountsDTO) o;

            return Objects.equals(this.receivedAmount, that.receivedAmount)
                    && Objects.equals(this.surplusAmount, that.surplusAmount)
                    && Objects.equals(this.deficientAmount, that.deficientAmount);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(receivedAmount, surplusAmount, deficientAmount);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", receivedAmount, surplusAmount, deficientAmount);
    }

    // Accessors -->


    public BigDecimal getReceivedAmount() {
        return receivedAmount;
    }

    public BigDecimal getSurplusAmount() {
        return surplusAmount;
    }

    public BigDecimal getDeficientAmount() {
        return deficientAmount;
    }
}
