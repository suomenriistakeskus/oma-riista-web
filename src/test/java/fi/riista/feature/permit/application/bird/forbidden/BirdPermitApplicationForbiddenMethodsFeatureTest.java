package fi.riista.feature.permit.application.bird.forbidden;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BirdPermitApplicationForbiddenMethodsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private BirdPermitApplicationForbiddenMethodsFeature birdPermitApplicationMethodsFeature;

    @Test
    public void updateDeviationJustification() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person contactPerson = model().newPerson();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setSpeciesAmounts(ImmutableList.of(model().newHarvestPermitApplicationSpeciesAmount(application, model().newGameSpecies(42), 3)));

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final DerogationPermitApplicationForbiddenMethodsSpeciesDTO justificationDTO =
                    new DerogationPermitApplicationForbiddenMethodsSpeciesDTO();
            justificationDTO.setGameSpeciesCode(42);
            justificationDTO.setJustification("Because I can");

            final DerogationPermitApplicationForbiddenMethodsDTO updateDTO =
                    new DerogationPermitApplicationForbiddenMethodsDTO();
            updateDTO.setDeviateSection34("Perustelu pykälälle 34");
            updateDTO.setTraps(true);
            updateDTO.setSpeciesJustifications(ImmutableList.of(justificationDTO));

            birdPermitApplicationMethodsFeature.updateMethodInfo(application.getId(), updateDTO);

            final DerogationPermitApplicationForbiddenMethodsDTO dto =
                    birdPermitApplicationMethodsFeature.getCurrentMethodInfo(application.getId());

            assertTrue(dto.isTraps());
            assertFalse(dto.isTapeRecorders());
            assertEquals(updateDTO.getDeviateSection34(), dto.getDeviateSection34());
            assertEquals(justificationDTO.getJustification(), dto.getSpeciesJustifications().iterator().next().getJustification());
        });
    }
}
