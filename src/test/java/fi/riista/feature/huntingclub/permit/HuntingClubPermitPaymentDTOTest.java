package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.payment.HuntingClubPermitPaymentDTO;
import fi.riista.feature.harvestpermit.payment.MooselikePrice;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import org.junit.Test;

import static fi.riista.util.NumberUtils.bigDecimalEquals;

public class HuntingClubPermitPaymentDTOTest {

    @Test
    public void test() {
        final HarvestCountDTO count = new HarvestCountDTO(
                10, 9,
                5, 4,
                7, 1,
                2, 1);
        final MooselikePrice prices = MooselikePrice.get(GameSpecies.OFFICIAL_CODE_MOOSE);
        final HuntingClubPermitPaymentDTO dto = HuntingClubPermitPaymentDTO.create(1L, count, prices);

        final int expectedAdultsPayment = (10 + 9 - 8) * 120;
        final int expectedYoungPayment = (5 + 4 - 3) * 50;

        bigDecimalEquals(expectedAdultsPayment, dto.getAdultsPayment());
        bigDecimalEquals(expectedYoungPayment, dto.getYoungPayment());
        bigDecimalEquals(expectedAdultsPayment + expectedYoungPayment, dto.getTotalPayment());
    }
}
