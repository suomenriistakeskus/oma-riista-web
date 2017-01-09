package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gis.MockGISQueryService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestReportRequirementsServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportRequirementsService service;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void initRhy() {
        this.rhy = model().newRiistanhoitoyhdistys();
    }

    @Test
    @Transactional
    public void testIsRequiredByPermit() {
        Harvest harvest = createHarvest(true, false, 2014, 9, 1);
        persistInCurrentlyOpenTransaction();

        assertTrue(invokeService(harvest));
    }

    @Test
    @Transactional
    public void testIsRequiredBySeason() {
        LocalDate beginDate = DateUtil.today();
        LocalDate endDate = beginDate.plusMonths(1);
        LocalDate endOfReportingDate = beginDate.plusMonths(2);

        LocalDate beginDate2 = endOfReportingDate.plusMonths(1);
        LocalDate endDate2 = beginDate2.plusMonths(2);
        LocalDate endOfReportingDate2 = beginDate2.plusMonths(3);

        HarvestSeason season = createHarvestSeason(
                beginDate, endDate, endOfReportingDate,
                beginDate2, endDate2, endOfReportingDate2);
        Harvest harvest = createHarvest(season, beginDate.toDateTimeAtStartOfDay());
        Harvest harvest2 = createHarvest(season, beginDate2.toDateTimeAtStartOfDay());
        persistInCurrentlyOpenTransaction();

        assertTrue(invokeService(harvest));
        assertTrue(invokeService(harvest2));
    }

    @Test
    @Transactional
    public void testNotRequiredByPermit() {
        Harvest harvest = createHarvest(false, false, 2014, 9, 1);
        persistInCurrentlyOpenTransaction();

        assertFalse(invokeService(harvest));
    }

    @Test
    @Transactional
    public void testNotRequiredByPermitBecauseFreeHunting() {
        Harvest harvest = createHarvest(true, true, 2014, 9, 1);
        persistInCurrentlyOpenTransaction();

        assertFalse(invokeService(harvest));
    }

    @Test
    @Transactional
    public void testNotRequiredByPermitBecauseNoRHY() {
        Harvest harvest = createHarvest(true, false, 2014, 9, 1);
        harvest.setGeoLocation(MockGISQueryService.RHY_GEOLOCATION_NOT_FOUND.copy());
        persistInCurrentlyOpenTransaction();

        assertFalse(invokeService(harvest));
    }

    @Test
    @Transactional
    public void testNotRequiredBecauseAttachedToListPermit() {
        Harvest harvest = createHarvest(true, false, 2014, 9, 1);
        HarvestPermit permit = model().newHarvestPermit(this.rhy, true);
        harvest.setHarvestPermit(permit);
        persistInCurrentlyOpenTransaction();

        assertFalse(invokeService(harvest, permit));
    }

    @Test
    @Transactional
    public void testRequiredBecauseAttachedToNonListPermit() {
        Harvest harvest = createHarvest(true, false, 2014, 9, 1);
        HarvestPermit permit = model().newHarvestPermit(this.rhy, false);
        harvest.setHarvestPermit(permit);
        persistInCurrentlyOpenTransaction();

        assertTrue(invokeService(harvest, permit));
    }

    @Test
    @Transactional
    public void testPermitRequiredButTooEarly() {
        Harvest harvest = createHarvest(true, false, 2014, 7, 1);

        persistInCurrentlyOpenTransaction();

        assertFalse(invokeService(harvest));
    }

    @Test
    @Transactional
    public void testNotRequiredOutOfSeason() {
        LocalDate beginDate = DateUtil.today();
        LocalDate endDate = beginDate.plusMonths(1);
        LocalDate endOfReportingDate = beginDate.plusMonths(2);
        HarvestSeason season = createHarvestSeason(beginDate, endDate, endOfReportingDate, null, null, null);
        Harvest harvest = createHarvest(season, beginDate.toDateTimeAtStartOfDay().minusSeconds(1));

        persistInCurrentlyOpenTransaction();

        assertFalse(invokeService(harvest));
    }

    private Harvest createHarvest(HarvestSeason season, DateTime pointOfTime) {
        return createHarvest(season.getFields().getSpecies(), model().newPerson(), pointOfTime);
    }

    private Harvest createHarvest(boolean withPermit, boolean freeHungingAlso, int year, int month, int day) {
        return createHarvest(withPermit, freeHungingAlso, model().newPerson(), new DateTime(year, month, day, 12, 0));
    }

    private Harvest createHarvest(boolean withPermit, boolean freeHungingAlso, Person author, DateTime pointOfTime) {
        GameSpecies species = model().newGameSpecies();
        if (withPermit) {
            HarvestReportFields fields = model().newHarvestReportFields(species, withPermit);
            fields.setFreeHuntingAlso(freeHungingAlso);
        }
        return createHarvest(species, author, pointOfTime);
    }

    private Harvest createHarvest(GameSpecies species, Person author, DateTime pointOfTime) {
        Harvest harvest = model().newHarvest(species, author);
        harvest.setRhy(this.rhy);
        harvest.setPointOfTime(pointOfTime.toDate());
        return harvest;
    }

    private HarvestSeason createHarvestSeason(LocalDate beginDate, LocalDate endDate, LocalDate endOfReportingDate,
                                              LocalDate beginDate2, LocalDate endDate2, LocalDate endOfReportingDate2) {
        HarvestSeason season = model().newHarvestSeason(beginDate, endDate, endOfReportingDate);
        season.setBeginDate2(beginDate2);
        season.setEndDate2(endDate2);
        season.setEndOfReportingDate2(endOfReportingDate2);
        return season;
    }

    private boolean invokeService(Harvest harvest) {
        return invokeService(harvest, null);
    }

    private boolean invokeService(Harvest harvest, HarvestPermit permit) {
        return service.isHarvestReportRequired(
                harvest.getSpecies(),
                DateUtil.toLocalDateNullSafe(harvest.getPointOfTime()),
                harvest.getGeoLocation(),
                permit);
    }

}
