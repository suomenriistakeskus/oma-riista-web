package fi.riista.feature.permit.application.lawsectionten.period;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.util.DateUtil.currentYear;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LawSectionTenPermitApplicationSpeciesPeriodFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private LawSectionTenPermitApplicationSpeciesPeriodFeature feature;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private SystemUser user;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.EUROPEAN_BEAVER);
        model().newLawSectionTenPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        user = createNewUser("applicant", applicant);
        persistInNewTransaction();
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getPermitPeriodInformation(application.getId());
        });
    }

    @Test
    public void testSmoke_getPermitPeriod_initialValues() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);

        onSavedAndAuthenticated(user, () -> {

            final LawSectionTenPermitApplicationSpeciesPeriodDTO permitPeriod =
                    feature.getPermitPeriodInformation(application.getId());

            assertNotNull(permitPeriod);
            assertEquals(OFFICIAL_CODE_EUROPEAN_BEAVER, permitPeriod.getGameSpeciesCode());
            assertNull(permitPeriod.getBeginDate());
            assertNull(permitPeriod.getEndDate());
        });
    }


    @Test
    public void testSmoke_getPermitPeriod() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        final HarvestPermitApplicationSpeciesAmount spa =
                model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final int currentYear = currentYear();
        final LocalDate beginDate = new LocalDate(currentYear, 9, 1);
        final LocalDate endDate = new LocalDate(currentYear, 9, 10);
        spa.setBeginDate(beginDate);
        spa.setEndDate(endDate);

        onSavedAndAuthenticated(user, () -> {

            final LawSectionTenPermitApplicationSpeciesPeriodDTO permitPeriod =
                    feature.getPermitPeriodInformation(application.getId());

            assertNotNull(permitPeriod);
            assertEquals(OFFICIAL_CODE_EUROPEAN_BEAVER, permitPeriod.getGameSpeciesCode());
            assertEquals(beginDate, permitPeriod.getBeginDate());
            assertEquals(endDate, permitPeriod.getEndDate());
        });
    }

    @Test
    public void testSmoke_updatePermitPeriodCurrentSeason() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final LocalDate mockedTime = new LocalDate(2020, 8, 20);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 8, 20);
            final LocalDate endDate = new LocalDate(currentYear + 1, 4, 30);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }

    @Test
    public void testSmoke_updatePermitPeriodNextSeason() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final LocalDate mockedTime = new LocalDate(2020, 4, 30);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 8, 20);
            final LocalDate endDate = new LocalDate(currentYear + 1, 4, 30);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePeriod_previousSeason() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final LocalDate mockedTime = new LocalDate(2020, 5, 1);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 3, 1);
            final LocalDate endDate = new LocalDate(currentYear, 4, 1);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }


    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePeriod_beginDateAfterEndDate() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 1, 2);
            final LocalDate endDate = new LocalDate(currentYear, 1, 1);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePeriod_beginDateBeforePermitPeriodCurrentSeason() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final LocalDate mockedTime = new LocalDate(2020, 8, 20);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 8, 19);
            final LocalDate endDate = new LocalDate(currentYear, 9, 10);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePeriod_endDateAfterPermitPeriodCurrentSeason() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final LocalDate mockedTime = new LocalDate(2020, 8, 20);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 8, 20);
            final LocalDate endDate = new LocalDate(currentYear + 1, 5, 1);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePeriod_beginDateBeforePermitPeriodNextSeason() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final LocalDate mockedTime = new LocalDate(2020, 5, 1);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 8, 19);
            final LocalDate endDate = new LocalDate(currentYear, 9, 10);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePeriod_endDateAfterPermitPeriodNextSeason() {
        final GameSpecies beaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        model().newHarvestPermitApplicationSpeciesAmount(application, beaver);
        final LocalDate mockedTime = new LocalDate(2020, 6, 1);
        MockTimeProvider.mockTime(mockedTime.toDate().getTime());

        onSavedAndAuthenticated(user, () -> {
            final int currentYear = currentYear();
            final LawSectionTenPermitApplicationSpeciesPeriodDTO beaverDTO = new LawSectionTenPermitApplicationSpeciesPeriodDTO();
            final LocalDate beginDate = new LocalDate(currentYear, 12, 1);
            final LocalDate endDate = new LocalDate(currentYear + 1, 5, 1);
            beaverDTO.setBeginDate(beginDate);
            beaverDTO.setEndDate(endDate);
            beaverDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);

            feature.saveSpeciesPeriods(application.getId(), beaverDTO);
        });
    }
}
