package fi.riista.feature.permit.application.bird;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationName;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BirdPermitApplicationSummaryFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private BirdPermitApplicationSummaryFeature birdPermitApplicationSummaryFeature;

    @Test
    public void getFullDetails() {
        final Person contactPerson = model().newPerson();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermitApplication application =
                model().newHarvestPermitApplication(rhy, null, HarvestPermitCategory.BIRD);
        final DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setRecipient("Recipient");
        deliveryAddress.setStreetAddress("Katuosoite 1");
        deliveryAddress.setPostalCode("54321");
        deliveryAddress.setCity("Postipaikka");
        deliveryAddress.setCountry("Suomi");

        application.setLocale(Locales.SV);
        application.setApplicationName("Lintulupa");
        application.setApplicationYear(2019);
        application.setContactPerson(contactPerson);
        application.setPermitHolder(PermitHolder.createHolderForPerson(contactPerson));
        application.setDeliveryByMail(true);
        application.setDeliveryAddress(deliveryAddress);

        final BirdPermitApplication birdPermitApplication = model().newBirdPermitApplication(application);

        onSavedAndAuthenticated(createUser(contactPerson), () -> {
            final BirdPermitApplicationSummaryDTO dto = birdPermitApplicationSummaryFeature.readDetails(application.getId());

            assertNull(dto.getPermitHolder().getCode());
            assertEquals(contactPerson.getFullName(), dto.getPermitHolder().getName());
            assertEquals(contactPerson.getFirstName(), dto.getContactPerson().getFirstName());
            assertEquals(contactPerson.getLastName(), dto.getContactPerson().getLastName());
            assertEquals(2019, dto.getHuntingYear());

            assertEquals(HarvestPermitApplicationName.BIRD.getSwedish(), dto.getApplicationName());

            assertEquals("Recipient", dto.getDeliveryAddress().getRecipient());
            assertEquals("Katuosoite 1", dto.getDeliveryAddress().getStreetAddress());
            assertEquals("54321", dto.getDeliveryAddress().getPostalCode());
            assertEquals("Postipaikka", dto.getDeliveryAddress().getCity());
            assertEquals("Suomi", dto.getDeliveryAddress().getCountry());
            assertEquals(HarvestPermitCategory.BIRD, dto.getHarvestPermitCategory());
        });
    }

}
