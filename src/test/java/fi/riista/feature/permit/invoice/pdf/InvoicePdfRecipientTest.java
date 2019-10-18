package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.DeliveryAddress;
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

        assertEquals(expectedOutput, getFormatLinesResult(contactPerson, address));
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

        assertEquals(expectedOutput, getFormatLinesResult(contactPerson, address));
    }

    private static String getFormatLinesResult(final Person contactPerson, final DeliveryAddress deliveryAddress) {
        return String.join(";", InvoicePdfRecipient.create(contactPerson, deliveryAddress).formatAsLines());
    }
}
