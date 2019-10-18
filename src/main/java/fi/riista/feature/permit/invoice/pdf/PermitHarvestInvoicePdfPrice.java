package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.harvestpermit.payment.MooselikePrice;
import fi.riista.util.BigDecimalMoney;

public class PermitHarvestInvoicePdfPrice {
    private final BigDecimalMoney adultPrice;
    private final BigDecimalMoney youngPrice;

    public PermitHarvestInvoicePdfPrice(final MooselikePrice mooselikePrice) {
        this.adultPrice = new BigDecimalMoney(mooselikePrice.getAdultPrice());
        this.youngPrice = new BigDecimalMoney(mooselikePrice.getYoungPrice());
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
