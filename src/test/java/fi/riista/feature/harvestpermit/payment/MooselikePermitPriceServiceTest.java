package fi.riista.feature.harvestpermit.payment;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class MooselikePermitPriceServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private MooselikePermitPriceService mooselikePermitPriceService;

    private static BigDecimal bd(final long value) {
        return BigDecimal.valueOf(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Test
    public void testSmoke_Moose() {
        final GameSpecies gameSpecies = model().newGameSpeciesMoose();

        persistInNewTransaction();

        runInTransaction(() -> {
            HasHarvestCountsForPermit counts = HasHarvestCountsForPermit.of(6, 5, 4, 3, 2, 1);
            final long huntingClubId = 142;
            final HarvestCountDTO count = new HarvestCountDTO(counts);
            final HuntingClubPermitPriceBreakdownDTO partnerPriceBreakdown = mooselikePermitPriceService.getPartnerPriceBreakdown(gameSpecies, singletonMap(huntingClubId, count));

            assertThat(partnerPriceBreakdown.getPayments(), Matchers.hasKey(huntingClubId));
            final HuntingClubPermitPaymentDTO clubPayment = partnerPriceBreakdown.getPayments().get(huntingClubId);

            final int adultPayment = 120 * (6 + 5 - 2);
            final int youngPayment = 50 * (4 + 3 - 1);
            final int totalPayment = adultPayment + youngPayment;

            assertEquals(bd(adultPayment), clubPayment.getAdultsPayment());
            assertEquals(bd(youngPayment), clubPayment.getYoungPayment());
            assertEquals(bd(totalPayment), clubPayment.getTotalPayment());

            final HuntingClubPermitTotalPaymentDTO total = partnerPriceBreakdown.getTotalPayment();

            assertEquals(bd(120), total.getAdultPrice());
            assertEquals(bd(50), total.getYoungPrice());

            assertEquals(bd(adultPayment), total.getAdultsPayment());
            assertEquals(bd(youngPayment), total.getYoungPayment());

            assertEquals(2, total.getAdultsNotEdibleCount());
            assertEquals(1, total.getYoungNotEdibleCount());

            assertEquals(bd(totalPayment), total.getTotalPayment());
        });
    }

    @Test
    public void testSmoke_Deer() {
        final GameSpecies gameSpecies = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);

        persistInNewTransaction();

        runInTransaction(() -> {
            HasHarvestCountsForPermit counts = HasHarvestCountsForPermit.of(12, 23, 7, 9, 4, 5);
            final long huntingClubId = 142;
            final HarvestCountDTO count = new HarvestCountDTO(counts);
            final HuntingClubPermitPriceBreakdownDTO partnerPriceBreakdown = mooselikePermitPriceService.getPartnerPriceBreakdown(gameSpecies, Collections.singletonMap(huntingClubId, count));

            assertThat(partnerPriceBreakdown.getPayments(), Matchers.hasKey(huntingClubId));
            final HuntingClubPermitPaymentDTO clubPayment = partnerPriceBreakdown.getPayments().get(huntingClubId);

            final int adultPayment = 17 * (12 + 23 - 4);
            final int youngPayment = 8 * (7 + 9 - 5);
            final int totalPayment = adultPayment + youngPayment;

            assertEquals(bd(adultPayment), clubPayment.getAdultsPayment());
            assertEquals(bd(youngPayment), clubPayment.getYoungPayment());
            assertEquals(bd(totalPayment), clubPayment.getTotalPayment());

            final HuntingClubPermitTotalPaymentDTO total = partnerPriceBreakdown.getTotalPayment();

            assertEquals(bd(17), total.getAdultPrice());
            assertEquals(bd(8), total.getYoungPrice());

            assertEquals(bd(adultPayment), total.getAdultsPayment());
            assertEquals(bd(youngPayment), total.getYoungPayment());

            assertEquals(4, total.getAdultsNotEdibleCount());
            assertEquals(5, total.getYoungNotEdibleCount());

            assertEquals(bd(totalPayment), total.getTotalPayment());
        });
    }

}
