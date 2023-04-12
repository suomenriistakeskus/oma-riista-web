package fi.riista.feature.permit.application.carnivore;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.carnivore.justification.CarnivorePermitApplicationJustificationDTO;
import fi.riista.feature.permit.application.carnivore.justification.CarnivorePermitApplicationJustificationFeature;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class CarnivorePermitApplicationJustificationFeatureTest extends EmbeddedDatabaseTest {

    private static final String LOREM_IPSUM = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem " +
            "accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et " +
            "quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit " +
            "aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi " +
            "nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, " +
            "sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. " +
            "Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut " +
            "aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse " +
            "quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?";

    @Resource
    private CarnivorePermitApplicationJustificationFeature feature;


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
        model().newCarnivorePermitApplication(application);
        model().newHarvestPermitApplicationSpeciesAmount(application, bear, 4.0f);
        persistInNewTransaction();
    }

    @Test
    public void testLongValuesArePersisted() {
        onSavedAndAuthenticated(createNewUser("applicant", applicant), () -> {
            final CarnivorePermitApplicationJustificationDTO dto = new CarnivorePermitApplicationJustificationDTO();
            dto.setPopulationAmount(LOREM_IPSUM + "amount");

            feature.updateJustification(application.getId(), dto);
        });

        runInTransaction(() -> {
            final List<HarvestPermitApplicationSpeciesAmount> all =
                    harvestPermitApplicationSpeciesAmountRepository.findAll();

            assertThat(all, hasSize(1));
            final HarvestPermitApplicationSpeciesAmount speciesAmount = all.get(0);
            assertEquals(LOREM_IPSUM + "amount", speciesAmount.getPopulationAmount());
        });
    }

}
