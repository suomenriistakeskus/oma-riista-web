package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class HarvestReportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportFeature harvestReportFeature;

    @Test
    public void testListReportableHuntingSeasons_validNow() {
        LocalDate today = DateUtil.today();

        HarvestSeason notValidToday = model().newHarvestSeason(today.minusDays(3), today.minusDays(2), today.minusDays(1));
        HarvestSeason validToday = model().newHarvestSeason(today.minusDays(2), today.minusDays(1), today.plusDays(2));

        persistAndAuthenticateWithNewUser(false);

        doTest(null, validToday, notValidToday);
        doTest(today, validToday);
    }

    @Test
    public void testListReportableHuntingSeasons_validNow2() {
        LocalDate today = DateUtil.today();

        HarvestSeason notValidToday = model().newHarvestSeason(today.minusDays(10), today.minusDays(9), today.minusDays(8));
        notValidToday.setBeginDate2(today.minusDays(3));
        notValidToday.setEndDate2(today.minusDays(2));
        notValidToday.setEndOfReportingDate2(today.minusDays(1));

        HarvestSeason validToday = model().newHarvestSeason(today.minusDays(10), today.minusDays(9), today.minusDays(8));
        validToday.setBeginDate2(today.minusDays(2));
        validToday.setEndDate2(today.minusDays(1));
        validToday.setEndOfReportingDate2(today.plusDays(2));

        persistAndAuthenticateWithNewUser(false);

        doTest(null, validToday, notValidToday);
        doTest(today, validToday);
    }

    private void doTest(LocalDate date, HarvestSeason... expectedSeasons) {
        assertEquals(F.getUniqueIds(expectedSeasons), F.getUniqueIds(harvestReportFeature.listReportableHuntingSeasons(date, null)));
    }
}
