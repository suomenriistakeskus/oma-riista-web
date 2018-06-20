package fi.riista.feature.permit.invoice;

import fi.riista.util.BigDecimalMoney;

import java.math.BigDecimal;

public class PermitHarvestInvoicePrice {
    private final BigDecimalMoney adultPrice;
    private final BigDecimalMoney youngPrice;

    public PermitHarvestInvoicePrice(final BigDecimal adultPrice,
                                     final BigDecimal youngPrice) {
        this.adultPrice = new BigDecimalMoney(adultPrice);
        this.youngPrice = new BigDecimalMoney(youngPrice);
    }

    public String formatAdultPrice() {
        return formatPrice(adultPrice);
    }

    public String formatYoungPrice() {
        return formatPrice(youngPrice);
    }

    private static String formatPrice(final BigDecimalMoney price) {
        return price.getCents() == 0
                ? String.format("%3d €", price.getEuros())
                : String.format("%d.%02d €", price.getEuros(), price.getCents());

    }
}
