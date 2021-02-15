package fi.riista.feature.permit.application.gamemanagement.amount;

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

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class GameManagementSpeciesAmountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameManagementSpeciesAmountFeature feature;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Person applicant;
    private Riistanhoitoyhdistys rhy;
    private HarvestPermitApplication application;
    private GameSpecies species;

    @Before
    public void setup() {
        applicant = model().newPerson();
        rhy = model().newRiistanhoitoyhdistys();
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.GAME_MANAGEMENT);
        model().newGameManagementPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        species = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getSpeciesAmount(application.getId());
        });
    }

    @Test
    public void testGetSpeciesAmounts() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, species);
        speciesAmount.setEggAmount(2);
        speciesAmount.setSubSpeciesName("Scientific name");

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final GameManagementSpeciesAmountDTO speciesAmountDto = feature.getSpeciesAmount(application.getId());
            assertThat(speciesAmountDto.getGameSpeciesCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat((double) speciesAmountDto.getSpecimenAmount(), is(closeTo(speciesAmount.getSpecimenAmount(), 0.01)));
            assertThat(speciesAmountDto.getEggAmount(), is(equalTo(speciesAmount.getEggAmount())));
            assertThat(speciesAmountDto.getSubSpeciesName(), is(equalTo(speciesAmount.getSubSpeciesName())));
        });
    }

    @Test
    public void testSaveSpeciesAmountsAndDerogationReasons() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final GameManagementSpeciesAmountDTO speciesAmountDTO = new GameManagementSpeciesAmountDTO();
            speciesAmountDTO.setSpecimenAmount(5);
            speciesAmountDTO.setEggAmount(2);
            speciesAmountDTO.setSubSpeciesName("Scientific name");
            speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            feature.saveSpeciesAmount(application.getId(), speciesAmountDTO);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> speciesAmountList = harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(speciesAmountList, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = speciesAmountList.get(0);
            assertThat(spa.getGameSpecies().getOfficialCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat((double)spa.getSpecimenAmount(), is(closeTo(5.0, 0.01)));
            assertThat(spa.getEggAmount(), is(equalTo(2)));
            assertThat(spa.getSubSpeciesName(), is(equalTo("Scientific name")));

        });
    }

    @Test
    public void testSaveSpeciesAmountsAndDerogationReasons_update() {
        final GameSpecies newSpecies = model().newGameSpecies(OFFICIAL_CODE_LYNX);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final GameManagementSpeciesAmountDTO updateDTO = new GameManagementSpeciesAmountDTO();
            updateDTO.setSpecimenAmount(5);
            updateDTO.setEggAmount(10);
            updateDTO.setSubSpeciesName("New scientific");
            updateDTO.setGameSpeciesCode(newSpecies.getOfficialCode());
            feature.saveSpeciesAmount(application.getId(), updateDTO);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all = harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount updatedSpa = all.get(0);
            assertThat((double) updatedSpa.getSpecimenAmount(), is(closeTo(5.0, 0.01)));
            assertThat(updatedSpa.getEggAmount(), is(equalTo(10)));
            assertThat(updatedSpa.getSubSpeciesName(), is(equalTo("New scientific")));
            assertThat(updatedSpa.getGameSpecies().getOfficialCode(), is(equalTo(OFFICIAL_CODE_LYNX)));
        });
    }
}
