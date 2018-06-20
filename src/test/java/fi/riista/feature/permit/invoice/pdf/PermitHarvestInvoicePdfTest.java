package fi.riista.feature.permit.invoice.pdf;

import com.google.common.io.Files;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PermitHarvestInvoicePdfTest {

    @Test
    public void testGetPdf() throws IOException {
        createDummyPdf();
    }

    private static PermitHarvestInvoicePdf createDummyPdf() throws IOException {
        final PermitDecision decision = createDummyDecision();
        final GameSpecies gameSpecies = createMoose();
        return PermitHarvestInvoicePdf.createInvoice(decision, gameSpecies);
    }

    private static GameSpecies createMoose() {
        final GameSpecies gameSpecies = new GameSpecies();
        gameSpecies.setOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE);
        gameSpecies.setNameFinnish("Hirvi");
        gameSpecies.setNameSwedish("Älg");
        return gameSpecies;
    }

    private static PermitDecision createDummyDecision() {
        final Address address = new Address();
        address.setStreetAddress("Katu 123");
        address.setPostalCode("33700");
        address.setCity("Tampere");

        final Person contactPerson = new Person();
        contactPerson.setId(123456L);
        contactPerson.setFirstName("Erkki");
        contactPerson.setLastName("Esimerkki");
        contactPerson.setMrAddress(address);

        final HuntingClub permitHolder = new HuntingClub();
        permitHolder.setOfficialCode("12345678");
        permitHolder.setNameFinnish("Hirvenmetsästäjät ry");

        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setHuntingYear(2017);
        application.setApplicationNumber(20_000_001);
        application.setPermitNumber("2017-1-200-00001-0");

        final PermitDecision permitDecision = new PermitDecision();
        permitDecision.setApplication(application);
        permitDecision.setContactPerson(contactPerson);
        permitDecision.setPermitHolder(permitHolder);
        permitDecision.setPublishDate(DateUtil.now());
        permitDecision.setLocale(Locales.SV);

        return permitDecision;
    }

    public static void main(String[] args) {
        try {
            final byte[] data = createDummyPdf().getData();
            final File tempFile = File.createTempFile("harvest-invoice", ".pdf");
            Files.write(data, tempFile);

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(tempFile.toURI());
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not generate PDF", e);
        }
    }
}
