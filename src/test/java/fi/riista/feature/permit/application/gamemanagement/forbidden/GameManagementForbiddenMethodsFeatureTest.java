package fi.riista.feature.permit.application.gamemanagement.forbidden;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplicationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class GameManagementForbiddenMethodsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameManagementForbiddenMethodsFeature feature;

    @Resource
    private GameManagementPermitApplicationRepository gameManagementPermitApplicationRepository;
    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Riistanhoitoyhdistys rhy;
    private Person contactPerson;
    private GameSpecies mammalSpecies;
    private HarvestPermitApplication application;
    private GameManagementPermitApplication gameManagementPermitApplication;
    private HarvestPermitApplicationSpeciesAmount spa;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        contactPerson = model().newPerson();
        mammalSpecies = model().newGameSpecies(42, GameCategory.GAME_MAMMAL, "mammal-fi", "mammal-sv", "mammal-en");
        application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.GAME_MANAGEMENT);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        spa = model().newHarvestPermitApplicationSpeciesAmount(application,
                mammalSpecies, 3);
        application.setSpeciesAmounts(ImmutableList.of(spa));
        gameManagementPermitApplication = model().newGameManagementPermitApplication(application);
    }

    @Test
    public void testAuthentication_contactPerson() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            feature.getMethods(application.getId());
        });
    }

    @Test
    public void testAuthentication_moderator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.getMethods(application.getId());
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unauthorized() {
        onSavedAndAuthenticated(createNewUser("newPerson"), () -> {
            feature.getMethods(application.getId());
        });
    }

    @Test
    public void testGetMethods() {
        final DerogationPermitApplicationForbiddenMethods forbiddenMethods =
                new DerogationPermitApplicationForbiddenMethods();
        forbiddenMethods.setDeviateSection32("32");
        forbiddenMethods.setDeviateSection33("33");
        forbiddenMethods.setDeviateSection34("34");
        forbiddenMethods.setDeviateSection35("35");
        forbiddenMethods.setDeviateSection51("51");
        forbiddenMethods.setTapeRecorders(true);
        forbiddenMethods.setTraps(true);

        gameManagementPermitApplication.setForbiddenMethods(forbiddenMethods);
        spa.setForbiddenMethodJustification("justification");

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final DerogationPermitApplicationForbiddenMethodsDTO dto =
                    feature.getMethods(application.getId());

            assertThat(dto.getDeviateSection32(), is(equalTo("32")));
            assertThat(dto.getDeviateSection33(), is(equalTo("33")));
            assertThat(dto.getDeviateSection34(), is(equalTo("34")));
            assertThat(dto.getDeviateSection35(), is(equalTo("35")));
            assertThat(dto.getDeviateSection51(), is(equalTo("51")));
            assertThat(dto.isTapeRecorders(), is(true));
            assertThat(dto.isTraps(), is(true));

            assertThat(dto.getSpeciesJustifications(), hasSize(1));
            final DerogationPermitApplicationForbiddenMethodsSpeciesDTO speciesDTO =
                    dto.getSpeciesJustifications().get(0);
            assertThat(speciesDTO.getJustification(), is(equalTo("justification")));
        });
    }

    @Test
    public void updateDeviationJustification() {

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final DerogationPermitApplicationForbiddenMethodsSpeciesDTO justificationDTO =
                    new DerogationPermitApplicationForbiddenMethodsSpeciesDTO();
            justificationDTO.setGameSpeciesCode(mammalSpecies.getOfficialCode());
            justificationDTO.setJustification("justification");

            final DerogationPermitApplicationForbiddenMethodsDTO updateDTO =
                    new DerogationPermitApplicationForbiddenMethodsDTO();
            updateDTO.setDeviateSection34("34");
            updateDTO.setTraps(true);
            updateDTO.setSpeciesJustifications(ImmutableList.of(justificationDTO));

            feature.updateMethods(application.getId(), updateDTO);
        });

        runInTransaction(() -> {
            final GameManagementPermitApplication gameManagementPermitApplication =
                    gameManagementPermitApplicationRepository.findByHarvestPermitApplication(application);
            final DerogationPermitApplicationForbiddenMethods forbiddenMethods =
                    gameManagementPermitApplication.getForbiddenMethods();
            assertThat(forbiddenMethods, is(notNullValue()));
            assertThat(forbiddenMethods.getDeviateSection32(), isEmptyOrNullString());
            assertThat(forbiddenMethods.getDeviateSection33(), isEmptyOrNullString());
            assertThat(forbiddenMethods.getDeviateSection34(), is(equalTo("34")));
            assertThat(forbiddenMethods.getDeviateSection35(), isEmptyOrNullString());
            assertThat(forbiddenMethods.getDeviateSection51(), isEmptyOrNullString());
            assertThat(forbiddenMethods.isTraps(), is(true));
            assertThat(forbiddenMethods.isTapeRecorders(), is(false));

            final List<HarvestPermitApplicationSpeciesAmount> spas =
                    harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
            assertThat(spas, hasSize(1));
            final HarvestPermitApplicationSpeciesAmount spa = spas.get(0);
            assertThat(spa.getForbiddenMethodJustification(), is(equalTo("justification")));
        });
    }
}
