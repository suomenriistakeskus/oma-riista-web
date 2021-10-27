package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.PermitHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InvoicePdfRecipientTest {

    @Test
    public void testFormatLines_contactPerson() {
        final Person contactPerson = InvoicePdfTestData.createContactPerson();

        final DeliveryAddress address = new DeliveryAddress();
        address.setRecipient("Vastaanottaja");
        address.setStreetAddress("Katu 3");
        address.setPostalCode("12345");
        address.setCity("Kaupunki");

        final String expectedOutput = "Vastaanottaja;Katu 3;12345 Kaupunki";

        assertEquals(expectedOutput, getFormatLinesResult(PermitHolder.createHolderForPerson(contactPerson), contactPerson, address));
    }

    @Test
    public void testFormatLines_withAddress() {
        final Person contactPerson = InvoicePdfTestData.createContactPerson();

        final DeliveryAddress address = new DeliveryAddress();
        address.setRecipient("Vastaanottaja");
        address.setStreetAddress("Katu 3");
        address.setPostalCode("12345");
        address.setCity("Kaupunki");
        address.setCountry("Ruotsi");

        final String expectedOutput = "Vastaanottaja;Katu 3;12345 Kaupunki;Ruotsi";

        assertEquals(expectedOutput, getFormatLinesResult(PermitHolder.createHolderForPerson(contactPerson), contactPerson, address));
    }

    @Test
    public void testFormatLinesWithPermitHolder_permitHolderClub() {
        final HuntingClub club = InvoicePdfTestData.createClub();
        final Person contactPerson = InvoicePdfTestData.createContactPerson();

        final DeliveryAddress address = new DeliveryAddress();
        address.setRecipient("Vastaanottaja");
        address.setStreetAddress("Katu 3");
        address.setPostalCode("12345");
        address.setCity("Kaupunki");

        final String expectedOutput = "12345678 Hirvenmetsästäjät ry;Vastaanottaja;Katu 3;12345 Kaupunki";

        final String joined = String.join(";",
                InvoicePdfRecipient.create(PermitHolder.createHolderForClub(club), contactPerson, address)
                        .formatAsLinesWithPermitHolder());

        assertEquals(expectedOutput, joined);
    }

    @Test
    public void testFormatLinesWithPermitHolder_permitHolderPerson() {
        final Person contactPerson = InvoicePdfTestData.createContactPerson();

        final DeliveryAddress address = new DeliveryAddress();
        address.setRecipient("Vastaanottaja");
        address.setStreetAddress("Katu 3");
        address.setPostalCode("12345");
        address.setCity("Kaupunki");

        final String expectedOutput = "Vastaanottaja;Katu 3;12345 Kaupunki";

        final String joined = String.join(";",
                InvoicePdfRecipient.create(PermitHolder.createHolderForPerson(contactPerson), contactPerson, address)
                        .formatAsLinesWithPermitHolder());

        assertEquals(expectedOutput, joined);
    }

    private static String getFormatLinesResult(final PermitHolder permitHolder, final Person contactPerson, final DeliveryAddress deliveryAddress) {
        return String.join(";", InvoicePdfRecipient.create(permitHolder, contactPerson, deliveryAddress).formatAsLines());
    }
}
