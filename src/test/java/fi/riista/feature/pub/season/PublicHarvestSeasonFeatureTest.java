package fi.riista.feature.pub.season;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.harvestpermit.report.HarvestReport.State.APPROVED;
import static fi.riista.feature.harvestpermit.report.HarvestReport.State.DELETED;
import static fi.riista.feature.harvestpermit.report.HarvestReport.State.REJECTED;
import static fi.riista.feature.harvestpermit.report.HarvestReport.State.SENT_FOR_APPROVAL;
import static org.junit.Assert.assertEquals;

public class PublicHarvestSeasonFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PublicHarvestSeasonFeature feature;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    @Test
    public void testOnlyActiveSeasons() {
        HarvestSeason seasonExpired1 = createSeasonExpired();
        createAreaAndInsertHarvests(seasonExpired1, APPROVED);

        HarvestSeason seasonExpired2 = createSeasonExpired();
        createAreaAndInsertHarvests(seasonExpired2, APPROVED);

        HarvestSeason season3 = createSeasonActiveNow();
        HarvestQuota quota3 = createAreaAndInsertHarvests(season3, APPROVED);

        persistInNewTransaction();

        List<PublicHarvestSeasonDTO> result = feature.listSeasonsWithQuotas(true);

        assertEquals(1, result.size());

        PublicHarvestSeasonDTO dtoSeason = findSeason(result, season3.getNameFinnish());
        assertEquals(1, dtoSeason.getQuotas().size());
        assertUsedQuotas(dtoSeason.getQuotas(), Tuple.of(quota3, 1));
    }

    @Test
    public void testAllSeasons() {
        HarvestSeason seasonExpired1 = createSeasonExpired();
        HarvestQuota quota1 = createAreaAndInsertHarvests(seasonExpired1, APPROVED);

        HarvestSeason seasonExpired2 = createSeasonExpired();
        HarvestQuota quota2 = createAreaAndInsertHarvests(seasonExpired2, APPROVED);

        HarvestSeason season3 = createSeasonActiveNow();
        HarvestQuota quota3 = createAreaAndInsertHarvests(season3, APPROVED);

        persistInNewTransaction();

        List<PublicHarvestSeasonDTO> result = feature.listSeasonsWithQuotas(false);

        assertEquals(3, result.size());

        PublicHarvestSeasonDTO dtoSeason1 = findSeason(result, seasonExpired1.getNameFinnish());
        assertEquals(1, dtoSeason1.getQuotas().size());
        assertUsedQuotas(dtoSeason1.getQuotas(), Tuple.of(quota1, 1));

        PublicHarvestSeasonDTO dtoSeason2 = findSeason(result, seasonExpired2.getNameFinnish());
        assertEquals(1, dtoSeason2.getQuotas().size());
        assertUsedQuotas(dtoSeason2.getQuotas(), Tuple.of(quota2, 1));

        PublicHarvestSeasonDTO dtoSeason = findSeason(result, season3.getNameFinnish());
        assertEquals(1, dtoSeason.getQuotas().size());
        assertUsedQuotas(dtoSeason.getQuotas(), Tuple.of(quota3, 1));
    }

    @Test
    public void testCounting() {
        HarvestSeason season1 = createSeasonActiveNow();
        HarvestQuota quota1 = createAreaAndInsertHarvests(season1, APPROVED);
        HarvestQuota quota2 = createAreaAndInsertHarvests(season1, APPROVED, APPROVED);
        HarvestQuota quota3 = createAreaAndInsertHarvests(season1, APPROVED, SENT_FOR_APPROVAL, APPROVED, APPROVED);
        HarvestQuota quota4 = createAreaAndInsertHarvests(season1, APPROVED, DELETED, APPROVED, SENT_FOR_APPROVAL, APPROVED, DELETED, APPROVED);

        HarvestSeason season2 = createSeasonActiveNow();
        HarvestQuota quota5 = createAreaAndInsertHarvests(season2, APPROVED, REJECTED);
        HarvestQuota quota6 = createAreaAndInsertHarvests(season2, DELETED, APPROVED);

        // season 1 has 4 quotas, used 1,2,3,4
        // season 2 has 2 quotas, used 1,1

        persistInNewTransaction();

        List<PublicHarvestSeasonDTO> result = feature.listSeasonsWithQuotas(true);

        assertEquals(2, result.size());

        PublicHarvestSeasonDTO dtoSeason1 = findSeason(result, season1.getNameFinnish());
        assertEquals(4, dtoSeason1.getQuotas().size());
        assertUsedQuotas(
                dtoSeason1.getQuotas(), Tuple.of(quota1, 1), Tuple.of(quota2, 2), Tuple.of(quota3, 3), Tuple.of(quota4, 4));

        PublicHarvestSeasonDTO dtoSeason2 = findSeason(result, season2.getNameFinnish());
        assertEquals(2, dtoSeason2.getQuotas().size());
        assertUsedQuotas(dtoSeason2.getQuotas(), Tuple.of(quota5, 1), Tuple.of(quota6, 1));
    }

    @SafeVarargs
    private static void assertUsedQuotas(
            List<PublicHarvestQuotaDTO> quotas, Tuple2<HarvestQuota, Integer>... allQuotasToExpectedUse) {

        if (allQuotasToExpectedUse != null) {
            for (Tuple2<HarvestQuota, Integer> quotaToExpectedUse : allQuotasToExpectedUse) {
                PublicHarvestQuotaDTO publicQuota = findQuota(quotas, quotaToExpectedUse._1());
                assertEquals(quotaToExpectedUse._2(), publicQuota.getUsedQuota());
            }
        }
    }

    private static PublicHarvestQuotaDTO findQuota(List<PublicHarvestQuotaDTO> quotas, HarvestQuota q) {
        for (PublicHarvestQuotaDTO publicQuota : quotas) {
            if (publicQuota.getNameFinnish().equals(q.getHarvestArea().getNameFinnish())) {
                return publicQuota;
            }
        }
        return null;
    }

    private static PublicHarvestSeasonDTO findSeason(List<PublicHarvestSeasonDTO> results, String nameFinnish) {
        for (PublicHarvestSeasonDTO dto : results) {
            if (dto.getNameFinnish().equals(nameFinnish)) {
                return dto;
            }
        }
        return null;
    }

    private HarvestQuota createAreaAndInsertHarvests(HarvestSeason harvestSeason,
                                                     HarvestReport.State... harvestStates) {
        HarvestArea harvestArea = model().newHarvestArea(this.rhy);
        HarvestQuota quota = model().newHarvestQuota(harvestSeason, harvestArea, 10);
        for (HarvestReport.State state : harvestStates) {
            createHarvest(quota, state);
        }
        return quota;
    }

    private void createHarvest(HarvestQuota quota, HarvestReport.State state) {
        Harvest harvest = model().newHarvest();
        model().newHarvestReport(harvest, state, quota);
    }

    private HarvestSeason createSeasonActiveNow() {
        LocalDate end = DateUtil.today();
        LocalDate begin = end.minusWeeks(1);
        LocalDate endOfReporting = end.plusWeeks(1);
        return model().newHarvestSeason(begin, end, endOfReporting);
    }

    private HarvestSeason createSeasonExpired() {
        LocalDate endOfReporting = DateUtil.today().minusWeeks(1);
        LocalDate begin = endOfReporting.minusWeeks(2);
        LocalDate end = endOfReporting.minusWeeks(1);
        return model().newHarvestSeason(begin, end, endOfReporting);
    }

}
