package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class HarvestSeasonRepositoryTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Test
    public void testListSeasons_validNow() {
        GameSpecies species = model().newGameSpecies();
        LocalDate today = DateUtil.today();

        HarvestSeason notValidToday = model().newHarvestSeason(species, today.minusDays(3), today.minusDays(2), today.minusDays(1));
        HarvestSeason validToday = model().newHarvestSeason(species, today.minusDays(2), today.minusDays(1), today.plusDays(2));

        persistAndAuthenticateWithNewUser(false);

        doTest(null, validToday, notValidToday);
        doTest(today, validToday);
    }

    @Test
    public void testListSeasons_validNow2() {
        GameSpecies species = model().newGameSpecies();
        LocalDate today = DateUtil.today();

        HarvestSeason notValidToday = model().newHarvestSeason(species, today.minusDays(10), today.minusDays(9), today.minusDays(8));
        notValidToday.setBeginDate2(today.minusDays(3));
        notValidToday.setEndDate2(today.minusDays(2));
        notValidToday.setEndOfReportingDate2(today.minusDays(1));

        HarvestSeason validToday = model().newHarvestSeason(species, today.minusDays(10), today.minusDays(9), today.minusDays(8));
        validToday.setBeginDate2(today.minusDays(2));
        validToday.setEndDate2(today.minusDays(1));
        validToday.setEndOfReportingDate2(today.plusDays(2));

        persistAndAuthenticateWithNewUser(false);

        doTest(null, validToday, notValidToday);
        doTest(today, validToday);
    }

    private void doTest(LocalDate date, HarvestSeason... expectedSeasons) {
        assertEquals(F.getUniqueIds(expectedSeasons), F.getUniqueIds(harvestSeasonRepository.listAllForReportingFetchSpecies(date)));
    }
}
