package fi.riista.feature.permit.application.bird.area;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class BirdPermitApplicationProtectedAreaFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private BirdPermitApplicationProtectedAreaFeature protectedAreaFeature;

    @Test
    public void updateProtectedArea() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person contactPerson = model().newPerson();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        application.setContactPerson(contactPerson);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final BirdPermitApplicationProtectedAreaDTO updateDTO = new BirdPermitApplicationProtectedAreaDTO(ProtectedAreaType.AIRPORT,
                    "Lentoasema", 12, "Lentokentäntie 12",
                    "00002", "Lentokenttä", "On mulla oikeus",
                    new GeoLocation(123, 234));

            protectedAreaFeature.updateProtectedArea(application.getId(), updateDTO);

            final BirdPermitApplicationProtectedAreaDTO dto = protectedAreaFeature.getProtectedAreaInfo(application.getId());
            assertEquals(updateDTO.getName(), dto.getName());
            assertEquals(updateDTO.getPostalCode(), dto.getPostalCode());
            assertEquals(234, dto.getGeoLocation().getLongitude());
            assertEquals(123, dto.getGeoLocation().getLatitude());
        });
    }

}
