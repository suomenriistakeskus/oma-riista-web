package fi.riista.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class BigDecimalMoney {

    private final int euros;
    private final int cents;

    public BigDecimalMoney(final int euros, final int cents) {
        this.euros = euros;
        this.cents = cents;
        Preconditions.checkArgument(this.euros >= 0);
        Preconditions.checkArgument(this.cents >= 0 && this.cents < 100);
    }

    public BigDecimalMoney(final BigDecimal amount) {
        this(amount.toBigInteger().intValue(), amount.remainder(BigDecimal.ONE).movePointRight(2).toBigInteger().intValue());
    }

    @Nonnull
    public String formatPaymentAmount() {
        return String.format("%d.%02d", euros, cents);
    }

    public int getEuros() {
        return euros;
    }

    public int getCents() {
        return cents;
    }

    @Override
    public String toString() {
        return formatPaymentAmount();
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(euros).movePointRight(2).add(new BigDecimal(cents)).movePointLeft(2);
    }
}
