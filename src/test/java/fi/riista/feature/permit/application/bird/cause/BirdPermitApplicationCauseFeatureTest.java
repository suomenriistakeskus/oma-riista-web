package fi.riista.feature.permit.application.bird.cause;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BirdPermitApplicationCauseFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private BirdPermitApplicationCauseFeature birdPermitApplicationCauseFeature;

    @Test
    public void updatePermitCauseAllFalse() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person contactPerson = model().newPerson();
        final HarvestPermitApplication application = createApplication(rhy, contactPerson);
        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final BirdPermitApplicationCauseDTO updateDto = createUpdateDTO(false);

            birdPermitApplicationCauseFeature.updateCauseInfo(application.getId(), updateDto);

            final BirdPermitApplicationCauseDTO dto =
                    birdPermitApplicationCauseFeature.getCauseInfo(application.getId());

            assertFalse(dto.isCausePublicHealth());
            assertFalse(dto.isCausePublicSafety());
            assertFalse(dto.isCauseAviationSafety());
            assertFalse(dto.isCauseCropsDamage());
            assertFalse(dto.isCauseDomesticPets());
            assertFalse(dto.isCauseForestDamage());
            assertFalse(dto.isCauseFishing());
            assertFalse(dto.isCauseWaterSystem());
            assertFalse(dto.isCauseFlora());
            assertFalse(dto.isCauseFauna());
            assertFalse(dto.isCauseResearch());
        });
    }


    @Test
    public void updatePermitCauseAllTrue() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person contactPerson = model().newPerson();
        final HarvestPermitApplication application = createApplication(rhy, contactPerson);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final BirdPermitApplicationCauseDTO updateDto = createUpdateDTO(true);

            birdPermitApplicationCauseFeature.updateCauseInfo(application.getId(), updateDto);

            final BirdPermitApplicationCauseDTO dto =
                    birdPermitApplicationCauseFeature.getCauseInfo(application.getId());

            assertTrue(dto.isCausePublicHealth());
            assertTrue(dto.isCausePublicSafety());
            assertTrue(dto.isCauseAviationSafety());
            assertTrue(dto.isCauseCropsDamage());
            assertTrue(dto.isCauseDomesticPets());
            assertTrue(dto.isCauseForestDamage());
            assertTrue(dto.isCauseFishing());
            assertTrue(dto.isCauseWaterSystem());
            assertTrue(dto.isCauseFlora());
            assertTrue(dto.isCauseFauna());
            assertTrue(dto.isCauseResearch());
        });
    }

    private static BirdPermitApplicationCauseDTO createUpdateDTO(final boolean value) {
        return new BirdPermitApplicationCauseDTO.Builder()
                .withCausePublicHealth(value)
                .withCausePublicSafety(value)
                .withCauseAviationSafety(value)
                .withCauseCropsDamage(value)
                .withCauseDomesticPets(value)
                .withCauseForestDamage(value)
                .withCauseFishing(value)
                .withCauseWaterSystem(value)
                .withCauseFlora(value)
                .withCauseFauna(value)
                .withCauseResearch(value)
                .build();
    }

    private HarvestPermitApplication createApplication(Riistanhoitoyhdistys rhy, Person contactPerson) {
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        return application;
    }
}
