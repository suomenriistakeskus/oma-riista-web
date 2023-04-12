package fi.riista.feature.permit.application.lawsectionten.amount;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LawSectionTenPermitApplicationSpeciesAmountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private LawSectionTenPermitApplicationSpeciesAmountFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private GameSpecies europeanBeaver;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.EUROPEAN_BEAVER);
        model().newLawSectionTenPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        europeanBeaver = model().newGameSpecies(OFFICIAL_CODE_EUROPEAN_BEAVER);
        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {

        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getSpeciesAmounts(application.getId());
        });
    }

    @Test
    public void testSmoke_getAmounts() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, europeanBeaver);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final LawSectionTenPermitApplicationSpeciesAmountDTO result =
                    feature.getSpeciesAmounts(application.getId());

            assertNotNull(result);
            assertEquals(OFFICIAL_CODE_EUROPEAN_BEAVER, result.getGameSpeciesCode());
            assertEquals(speciesAmount.getSpecimenAmount(), result.getAmount(), 0.01);
        });
    }

    @Test
    public void testSmoke_saveAmounts() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmountDTO =
                    new LawSectionTenPermitApplicationSpeciesAmountDTO();
            speciesAmountDTO.setAmount(5.0f);
            speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);
            feature.saveSpeciesAmounts(application.getId(), speciesAmountDTO);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertEquals(OFFICIAL_CODE_EUROPEAN_BEAVER, spa.getGameSpecies().getOfficialCode());
            assertEquals(5.0f, spa.getSpecimenAmount(), 0.01);
        });
    }

    @Test
    public void testSmoke_updateAmounts() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, europeanBeaver);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmountDTO =
                    new LawSectionTenPermitApplicationSpeciesAmountDTO(speciesAmount);
            speciesAmountDTO.setAmount(speciesAmount.getSpecimenAmount() + 1);
            feature.saveSpeciesAmounts(application.getId(), speciesAmountDTO);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertEquals(OFFICIAL_CODE_EUROPEAN_BEAVER, spa.getGameSpecies().getOfficialCode());
            assertEquals(speciesAmount.getSpecimenAmount() + 1, spa.getSpecimenAmount(), 0.01);
        });
    }
}
