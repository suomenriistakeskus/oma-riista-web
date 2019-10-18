package fi.riista.feature.permit.application.carnivore.species;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CarnivorePermitApplicationSpeciesAmountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private CarnivorePermitApplicationSpeciesAmountFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private Person applicant;
    private GameSpecies bear;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        bear = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_BEAR);
        applicant = model().newPerson();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.LARGE_CARNIVORE_BEAR);
        application.setContactPerson(applicant);
        application.setSubmitDate(null);
        application.setApplicationNumber(null);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        persistInNewTransaction();
    }

    @Test
    public void testAuthorization() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            feature.getSpeciesAmount(application.getId());
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthorization_invalidUser() {
        onSavedAndAuthenticated(createNewUser("invalid"), () -> {
            feature.getSpeciesAmount(application.getId());
        });
    }

    @Test
    public void testGetSpeciesAmount_amountNotCreated() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final CarnivorePermitApplicationSpeciesAmountDTO dto = feature.getSpeciesAmount(application.getId());
            assertEquals(GameSpecies.OFFICIAL_CODE_BEAR, dto.getGameSpeciesCode());
            assertEquals(0, dto.getAmount(), 0.01);
        });
    }

    @Test
    public void testGetSpeciesAmount_amountMatch() {
        model().newHarvestPermitApplicationSpeciesAmount(application, bear, 13.0f);
        persistInNewTransaction();

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final CarnivorePermitApplicationSpeciesAmountDTO dto = feature.getSpeciesAmount(application.getId());
            assertEquals(GameSpecies.OFFICIAL_CODE_BEAR, dto.getGameSpeciesCode());
            assertEquals(13.0, dto.getAmount(), 0.01);
        });
    }

    @Test
    public void testUpdate() {
        final CarnivorePermitApplicationSpeciesAmountDTO dto = new CarnivorePermitApplicationSpeciesAmountDTO();
        dto.setAmount(42);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_BEAR);
        dto.setBegin(new LocalDate(2019, 10, 1));
        dto.setEnd(new LocalDate(2019, 10, 15));

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            feature.saveSpeciesAmount(application.getId(), dto);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> spaList = harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
            assertEquals(1, spaList.size());
            final HarvestPermitApplicationSpeciesAmount spa = spaList.get(0);
            assertEquals(bear, spa.getGameSpecies());
            assertEquals(42, spa.getAmount(), 0.01);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdate_invalidPeriodBear_tooEarly() {
        final CarnivorePermitApplicationSpeciesAmountDTO dto = new CarnivorePermitApplicationSpeciesAmountDTO();
        dto.setAmount(42);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_BEAR);
        dto.setBegin(new LocalDate(2019, 8, 19));
        dto.setEnd(new LocalDate(2019, 10, 15));

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            feature.saveSpeciesAmount(application.getId(), dto);
            fail("Should have thrown an exception");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdate_invalidPeriodBear_tooLate() {
        final CarnivorePermitApplicationSpeciesAmountDTO dto = new CarnivorePermitApplicationSpeciesAmountDTO();
        dto.setAmount(42);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_BEAR);
        dto.setBegin(new LocalDate(2019, 8, 20));
        dto.setEnd(new LocalDate(2019, 11, 1));

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            feature.saveSpeciesAmount(application.getId(), dto);
            fail("Should have thrown an exception");
        });
    }

}
