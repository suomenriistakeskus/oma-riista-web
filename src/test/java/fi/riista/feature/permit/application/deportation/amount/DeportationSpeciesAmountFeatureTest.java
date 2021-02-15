package fi.riista.feature.permit.application.deportation.amount;

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

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_RACCOON;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLVERINE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DeportationSpeciesAmountFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private DeportationSpeciesAmountFeature feature;

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
        application = model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DEPORTATION);
        model().newDeportationPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);
        species = model().newGameSpecies(OFFICIAL_CODE_WOLVERINE);
    }

    @Test(expected = AccessDeniedException.class)
    public void test_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getSpeciesAmounts(application.getId());
        });
    }

    @Test
    public void testGetSpeciesAmounts() {
        final HarvestPermitApplicationSpeciesAmount speciesAmount =
                model().newHarvestPermitApplicationSpeciesAmount(application, species);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final DeportationSpeciesAmountDTO speciesAmountDTO = feature.getSpeciesAmounts(application.getId());

            assertThat(speciesAmountDTO, is(notNullValue()));
            assertThat(speciesAmountDTO.getGameSpeciesCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat(speciesAmountDTO.getAmount().doubleValue(), is(closeTo(speciesAmount.getSpecimenAmount(), 0.01)));
        });
    }

    @Test
    public void testSaveSpeciesAmounts() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final DeportationSpeciesAmountDTO speciesAmountDTO = new DeportationSpeciesAmountDTO();
            speciesAmountDTO.setAmount(5);
            speciesAmountDTO.setGameSpeciesCode(OFFICIAL_CODE_WOLVERINE);
            feature.saveSpeciesAmounts(application.getId(), speciesAmountDTO);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> speciesAmountList = harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(speciesAmountList, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = speciesAmountList.get(0);
            assertThat(spa.getGameSpecies().getOfficialCode(), is(equalTo(OFFICIAL_CODE_WOLVERINE)));
            assertThat(spa.getSpecimenAmount().doubleValue(), is(closeTo(5.0, 0.01)));
        });
    }

    @Test
    public void testSaveSpeciesAmounts_update() {
        final GameSpecies newSpecies = model().newGameSpecies(OFFICIAL_CODE_RACCOON);

        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final DeportationSpeciesAmountDTO speciesAmountDTO = new DeportationSpeciesAmountDTO();
            speciesAmountDTO.setAmount(5);
            speciesAmountDTO.setGameSpeciesCode(newSpecies.getOfficialCode());
            feature.saveSpeciesAmounts(application.getId(), speciesAmountDTO);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all = harvestPermitApplicationSpeciesAmountRepository.findAll();
            assertThat(all, hasSize(1));

            final HarvestPermitApplicationSpeciesAmount spa = all.get(0);
            assertThat(spa.getSpecimenAmount().doubleValue(), is(closeTo(5.0, 0.01)));

            assertThat(spa.getGameSpecies().getOfficialCode(), is(equalTo(OFFICIAL_CODE_RACCOON)));
        });
    }
}
