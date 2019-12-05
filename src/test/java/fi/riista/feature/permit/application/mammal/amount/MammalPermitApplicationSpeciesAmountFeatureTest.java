package fi.riista.feature.permit.application.mammal.amount;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ERMINE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_OTTER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MammalPermitApplicationSpeciesAmountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MammalPermitApplicationSpeciesAmountFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private GameSpecies wolverineSpecies;
    private GameSpecies otterSpecies;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MAMMAL);
        model().newMammalPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        wolverineSpecies = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
        otterSpecies = model().newGameSpecies(OFFICIAL_CODE_OTTER);
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
                model().newHarvestPermitApplicationSpeciesAmount(application, wolverineSpecies);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final List<MammalPermitApplicationSpeciesAmountDTO> speciesAmounts =
                    feature.getSpeciesAmounts(application.getId());

            assertThat(speciesAmounts, hasSize(1));
            final MammalPermitApplicationSpeciesAmountDTO spa =
                    speciesAmounts.get(0);
            assertEquals(OFFICIAL_CODE_WOLVERINE, spa.getGameSpeciesCode());
            assertEquals(speciesAmount.getAmount(), spa.getAmount(), 0.01);
        });
    }

    @Test
    public void testSmoke_updateAmounts() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final MammalPermitApplicationSpeciesAmountDTO speciesAmountDTO =
                    new MammalPermitApplicationSpeciesAmountDTO();
            speciesAmountDTO.setAmount(5.0f);
            speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(speciesAmountDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertEquals(OFFICIAL_CODE_WOLVERINE, spa.getGameSpecies().getOfficialCode());
            assertEquals(5.0f, spa.getAmount(), 0.01);
        });
    }

    @Test
    public void testSmoke_updateAmounts_multipleSpecies() {
        final GameSpecies firstSpecies = model().newGameSpecies(OFFICIAL_CODE_RACCOON);
        final GameSpecies secondSpecies = model().newGameSpecies(OFFICIAL_CODE_ERMINE);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final MammalPermitApplicationSpeciesAmountDTO firstDTO =
                    new MammalPermitApplicationSpeciesAmountDTO();
            firstDTO.setAmount(5.0f);
            firstDTO.setGameSpeciesCode(firstSpecies.getOfficialCode());
            final MammalPermitApplicationSpeciesAmountDTO secondDTO =
                    new MammalPermitApplicationSpeciesAmountDTO();
            secondDTO.setAmount(5.0f);
            secondDTO.setGameSpeciesCode(secondSpecies.getOfficialCode());
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(firstDTO, secondDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(2));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertEquals(5.0f, spa.getAmount(), 0.01);

            final HarvestPermitApplicationSpeciesAmount spa2 = all.get(1);
            assertEquals(5.0f, spa2.getAmount(), 0.01);

            final ImmutableSet<Integer> speciesCodes = ImmutableSet.of(
                    spa.getGameSpecies().getOfficialCode(),
                    spa2.getGameSpecies().getOfficialCode());

            assertTrue(speciesCodes.containsAll(ImmutableSet.of(OFFICIAL_CODE_RACCOON, OFFICIAL_CODE_ERMINE)));

        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmoke_updateAmounts_multipleSpecies_carnivoreWithOtherSpecies() {
        final GameSpecies otherSpecies = model().newGameSpecies();
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final MammalPermitApplicationSpeciesAmountDTO wolverineDTO =
                    new MammalPermitApplicationSpeciesAmountDTO();
            wolverineDTO.setAmount(5.0f);
            wolverineDTO.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            final MammalPermitApplicationSpeciesAmountDTO otherSpeciesDTO =
                    new MammalPermitApplicationSpeciesAmountDTO();
            otherSpeciesDTO.setAmount(5.0f);
            otherSpeciesDTO.setGameSpeciesCode(otherSpecies.getOfficialCode());
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(wolverineDTO, otherSpeciesDTO));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSmoke_updateAmounts_multipleSpecies_otterWithOtherSpecies() {
        final GameSpecies otherSpecies = model().newGameSpecies();
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final MammalPermitApplicationSpeciesAmountDTO otterDTO =
                    new MammalPermitApplicationSpeciesAmountDTO();
            otterDTO.setAmount(5.0f);
            otterDTO.setGameSpeciesCode(OFFICIAL_CODE_OTTER);
            final MammalPermitApplicationSpeciesAmountDTO otherSpeciesDTO =
                    new MammalPermitApplicationSpeciesAmountDTO();
            otherSpeciesDTO.setAmount(5.0f);
            otherSpeciesDTO.setGameSpeciesCode(otherSpecies.getOfficialCode());
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(otterDTO, otherSpeciesDTO));
        });
    }
}
