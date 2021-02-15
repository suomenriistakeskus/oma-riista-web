package fi.riista.feature.permit.application.nestremoval.amount;

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
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NestRemovalPermitApplicationSpeciesAmountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private NestRemovalPermitApplicationSpeciesAmountFeature feature;

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
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.NEST_REMOVAL);
        model().newNestRemovalPermitApplication(application);
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
                model().newHarvestPermitApplicationSpeciesAmountForNestRemoval(application, europeanBeaver, 10, 11, 12);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final List<NestRemovalPermitApplicationSpeciesAmountDTO> speciesAmounts =
                    feature.getSpeciesAmounts(application.getId());

            assertThat(speciesAmounts, hasSize(1));
            final NestRemovalPermitApplicationSpeciesAmountDTO spa =
                    speciesAmounts.get(0);
            assertEquals(OFFICIAL_CODE_EUROPEAN_BEAVER, spa.getGameSpeciesCode());
            assertEquals(speciesAmount.getNestAmount(), spa.getNestAmount());
            assertEquals(speciesAmount.getEggAmount(), spa.getEggAmount());
            assertEquals(speciesAmount.getConstructionAmount(), spa.getConstructionAmount());
        });
    }

    @Test
    public void testSmoke_updateAmounts() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final NestRemovalPermitApplicationSpeciesAmountDTO speciesAmountDTO =
                    new NestRemovalPermitApplicationSpeciesAmountDTO();
            speciesAmountDTO.setNestAmount(5);
            speciesAmountDTO.setEggAmount(6);
            speciesAmountDTO.setConstructionAmount(7);
            speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_EUROPEAN_BEAVER);
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(speciesAmountDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertEquals(OFFICIAL_CODE_EUROPEAN_BEAVER, spa.getGameSpecies().getOfficialCode());
            assertEquals(Integer.valueOf(5), spa.getNestAmount());
            assertEquals(Integer.valueOf(6), spa.getEggAmount());
            assertEquals(Integer.valueOf(7), spa.getConstructionAmount());
        });
    }

    @Test
    public void testSmoke_updateAmounts_multipleSpecies() {
        final GameSpecies firstSpecies = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE);
        final GameSpecies secondSpecies = model().newGameSpecies(OFFICIAL_CODE_ERMINE);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final NestRemovalPermitApplicationSpeciesAmountDTO firstDTO =
                    new NestRemovalPermitApplicationSpeciesAmountDTO();
            firstDTO.setNestAmount(5);
            firstDTO.setEggAmount(6);
            firstDTO.setConstructionAmount(7);
            firstDTO.setGameSpeciesCode(firstSpecies.getOfficialCode());
            final NestRemovalPermitApplicationSpeciesAmountDTO secondDTO =
                    new NestRemovalPermitApplicationSpeciesAmountDTO();
            secondDTO.setNestAmount(8);
            secondDTO.setEggAmount(9);
            secondDTO.setConstructionAmount(10);
            secondDTO.setGameSpeciesCode(secondSpecies.getOfficialCode());
            feature.saveSpeciesAmounts(application.getId(), ImmutableList.of(firstDTO, secondDTO));
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(2));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertEquals(Integer.valueOf(5), spa.getNestAmount());
            assertEquals(Integer.valueOf(6), spa.getEggAmount());
            assertEquals(Integer.valueOf(7), spa.getConstructionAmount());

            final HarvestPermitApplicationSpeciesAmount spa2 = all.get(1);
            assertEquals(Integer.valueOf(8), spa2.getNestAmount());
            assertEquals(Integer.valueOf(9), spa2.getEggAmount());
            assertEquals(Integer.valueOf(10), spa2.getConstructionAmount());

            final ImmutableSet<Integer> speciesCodes = ImmutableSet.of(
                    spa.getGameSpecies().getOfficialCode(),
                    spa2.getGameSpecies().getOfficialCode());

            assertTrue(speciesCodes.containsAll(ImmutableSet.of(OFFICIAL_CODE_PARTRIDGE, OFFICIAL_CODE_ERMINE)));

        });
    }
}
