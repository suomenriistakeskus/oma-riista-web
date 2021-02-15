package fi.riista.feature.permit.application.mammal.forbidden;

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
import fi.riista.feature.permit.application.mammal.MammalPermitApplication;
import fi.riista.feature.permit.application.mammal.MammalPermitApplicationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class MammalPermitApplicationForbiddenMethodsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MammalPermitApplicationForbiddenMethodsFeature feature;

    @Resource
    private MammalPermitApplicationRepository mammalPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Riistanhoitoyhdistys rhy;
    private Person contactPerson;
    private GameSpecies mammalSpecies;
    private HarvestPermitApplication application;
    private MammalPermitApplication mammalPermitApplication;
    private HarvestPermitApplicationSpeciesAmount spa;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        contactPerson = model().newPerson();
        mammalSpecies = model().newGameSpecies(42, GameCategory.GAME_MAMMAL, "mammal-fi", "mammal-sv", "mammal-en");
        application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.MAMMAL);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        spa = model().newHarvestPermitApplicationSpeciesAmount(application,
                mammalSpecies, 3);
        application.setSpeciesAmounts(ImmutableList.of(spa));
        mammalPermitApplication = model().newMammalPermitApplication(application);
    }

    @Test
    public void testAuthentication_contactPerson() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            feature.getCurrentMethodInfo(application.getId());
        });
    }

    @Test
    public void testAuthentication_moderator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.getCurrentMethodInfo(application.getId());
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unaouthorized() {
        onSavedAndAuthenticated(createNewUser("newPerson"), () -> {
            feature.getCurrentMethodInfo(application.getId());
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

        mammalPermitApplication.setForbiddenMethods(forbiddenMethods);
        spa.setForbiddenMethodJustification("justification");

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final DerogationPermitApplicationForbiddenMethodsDTO dto =
                    feature.getCurrentMethodInfo(application.getId());

            assertEquals("32", dto.getDeviateSection32());
            assertEquals("33", dto.getDeviateSection33());
            assertEquals("34", dto.getDeviateSection34());
            assertEquals("35", dto.getDeviateSection35());
            assertEquals("51", dto.getDeviateSection51());
            assertTrue(dto.isTapeRecorders());
            assertTrue(dto.isTraps());

            assertThat(dto.getSpeciesJustifications(), hasSize(1));
            final DerogationPermitApplicationForbiddenMethodsSpeciesDTO speciesDTO =
                    dto.getSpeciesJustifications().get(0);
            assertEquals("justification", speciesDTO.getJustification());
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

            feature.updateMethodInfo(application.getId(), updateDTO);
        });

        runInTransaction(() -> {
            final MammalPermitApplication mammalPermitApplication =
                    mammalPermitApplicationRepository.findByHarvestPermitApplication(application);
            final DerogationPermitApplicationForbiddenMethods forbiddenMethods =
                    mammalPermitApplication.getForbiddenMethods();
            assertNotNull(forbiddenMethods);
            assertTrue(StringUtils.isBlank(forbiddenMethods.getDeviateSection32()));
            assertTrue(StringUtils.isBlank(forbiddenMethods.getDeviateSection33()));
            assertEquals("34", forbiddenMethods.getDeviateSection34());
            assertTrue(StringUtils.isBlank(forbiddenMethods.getDeviateSection35()));
            assertTrue(StringUtils.isBlank(forbiddenMethods.getDeviateSection51()));
            assertTrue(forbiddenMethods.isTraps());
            assertFalse(forbiddenMethods.isTapeRecorders());

            final List<HarvestPermitApplicationSpeciesAmount> spas =
                    harvestPermitApplicationSpeciesAmountRepository.findByHarvestPermitApplication(application);
            assertThat(spas, hasSize(1));
            final HarvestPermitApplicationSpeciesAmount spa = spas.get(0);
            assertEquals("justification", spa.getForbiddenMethodJustification());
        });
    }
}
