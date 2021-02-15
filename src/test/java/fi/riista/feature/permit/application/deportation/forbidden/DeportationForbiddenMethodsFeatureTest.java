package fi.riista.feature.permit.application.deportation.forbidden;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplication;
import fi.riista.feature.permit.application.deportation.DeportationPermitApplicationRepository;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeportationForbiddenMethodsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private DeportationForbiddenMethodsFeature feature;

    @Resource
    private DeportationPermitApplicationRepository deportationPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    private Riistanhoitoyhdistys rhy;
    private Person contactPerson;
    private GameSpecies deportationSpecies;
    private HarvestPermitApplication application;
    private DeportationPermitApplication deportationPermitApplication;
    private HarvestPermitApplicationSpeciesAmount spa;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        contactPerson = model().newPerson();
        deportationSpecies =
                model().newGameSpecies(42, GameCategory.GAME_MAMMAL, "mammal-fi", "mammal-sv", "mammal-en");
        application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.DEPORTATION);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        spa = model().newHarvestPermitApplicationSpeciesAmount(application,
                deportationSpecies, 3);
        application.setSpeciesAmounts(ImmutableList.of(spa));
        deportationPermitApplication = model().newDeportationPermitApplication(application);
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.getForbiddenMethods(application.getId());
        });
    }

    @Test
    public void testGetForbiddenMethods() {
        final DerogationPermitApplicationForbiddenMethods forbiddenMethods =
                new DerogationPermitApplicationForbiddenMethods();
        forbiddenMethods.setDeviateSection32("32");
        forbiddenMethods.setDeviateSection33("33");
        forbiddenMethods.setDeviateSection34("34");
        forbiddenMethods.setDeviateSection35("35");
        forbiddenMethods.setDeviateSection51("51");
        forbiddenMethods.setTapeRecorders(true);
        forbiddenMethods.setTraps(true);

        deportationPermitApplication.setForbiddenMethods(forbiddenMethods);
        spa.setForbiddenMethodJustification("justification");

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final DerogationPermitApplicationForbiddenMethodsDTO dto = feature.getForbiddenMethods(application.getId());

            assertThat(dto.getDeviateSection32(), is(equalTo("32")));
            assertThat(dto.getDeviateSection33(), is(equalTo("33")));
            assertThat(dto.getDeviateSection34(), is(equalTo("34")));
            assertThat(dto.getDeviateSection35(), is(equalTo("35")));
            assertThat(dto.getDeviateSection51(), is(equalTo("51")));
            assertThat(dto.isTapeRecorders(), is(true));
            assertThat(dto.isTraps(), is(true));

            assertThat(dto.getSpeciesJustifications(), hasSize(1));
            final DerogationPermitApplicationForbiddenMethodsSpeciesDTO speciesDto =
                    dto.getSpeciesJustifications().get(0);
            assertThat(speciesDto.getJustification(), is(equalTo("justification")));
        });
    }

    @Test
    public void testUpdateForbiddenMethods() {
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final DerogationPermitApplicationForbiddenMethodsSpeciesDTO justificationDTO =
                    new DerogationPermitApplicationForbiddenMethodsSpeciesDTO();
            justificationDTO.setGameSpeciesCode(deportationSpecies.getOfficialCode());
            justificationDTO.setJustification("justification");

            final DerogationPermitApplicationForbiddenMethodsDTO updateDto =
                    new DerogationPermitApplicationForbiddenMethodsDTO();
            updateDto.setDeviateSection34("34");
            updateDto.setTraps(true);
            updateDto.setSpeciesJustifications(ImmutableList.of(justificationDTO));

            feature.updateForbiddenMethods(application.getId(), updateDto);
        });

        runInTransaction(() -> {
            final DeportationPermitApplication updatedApplication =
                    deportationPermitApplicationRepository.findByHarvestPermitApplication(application);
            final DerogationPermitApplicationForbiddenMethods forbiddenMethods =
                    updatedApplication.getForbiddenMethods();
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
