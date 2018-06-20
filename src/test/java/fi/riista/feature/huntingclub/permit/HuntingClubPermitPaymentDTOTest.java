package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.harvestpermit.season.MooselikePrice;
import org.junit.Test;

import java.math.BigDecimal;

import static fi.riista.util.NumberUtils.bigDecimalEquals;

public class HuntingClubPermitPaymentDTOTest {

    @Test
    public void test() {
        final long artificialClubId = 1L;
        final HuntingClubPermitCountDTO count = new HuntingClubPermitCountDTO(artificialClubId,
                10, 9,
                5, 4,
                7 + 1, 2 + 1,
                7, 1,
                2, 1);
        final MooselikePrice prices = createPrices(120, 50);
        final HuntingClubPermitPaymentDTO dto = HuntingClubPermitPaymentDTO.create(count, prices);

        final int expectedAdultsPayment = (10 + 9 - 8) * 120;
        final int expectedYoungPayment = (5 + 4 - 3) * 50;

        bigDecimalEquals(expectedAdultsPayment, dto.getAdultsPayment());
        bigDecimalEquals(expectedYoungPayment, dto.getYoungPayment());
        bigDecimalEquals(expectedAdultsPayment + expectedYoungPayment, dto.getTotalPayment());
    }

    private static MooselikePrice createPrices(final int adultPrice, final int youngPrice) {
        final MooselikePrice p = new MooselikePrice();
        p.setAdultPrice(new BigDecimal(adultPrice));
        p.setYoungPrice(new BigDecimal(youngPrice));
        return p;
    }

}
