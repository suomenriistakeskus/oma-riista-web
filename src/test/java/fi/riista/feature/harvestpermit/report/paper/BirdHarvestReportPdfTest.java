package fi.riista.feature.harvestpermit.report.paper;

import com.google.common.io.Files;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BirdHarvestReportPdfTest {

    @Test
    public void testGetPdf() throws IOException {
        createDummyPdf();
    }

    private static PermitHarvestReportPdf createDummyPdf() throws IOException {
        final PermitHolder permitHolder = PermitHolder.create("Yritys Oy", "1234567",
                PermitHolder.PermitHolderType.BUSINESS);

        final Person contactPerson = new Person();
        contactPerson.setFirstName("Firstname");
        contactPerson.setLastName("Lastname");
        contactPerson.setPhoneNumber("0501234567");
        contactPerson.setEmail("first.last@invalid");

        final DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setRecipient("Firstname LastName");
        deliveryAddress.setStreetAddress("Street 123");
        deliveryAddress.setPostalCode("00010");
        deliveryAddress.setCity("CITY");

        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setHarvestPermitCategory(HarvestPermitCategory.BIRD);

        final PermitDecision permitDecision = new PermitDecision(
                PermitDecision.DecisionType.HARVEST_PERMIT,
                2014,
                5,
                207,
                PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD,
                new Riistanhoitoyhdistys(),
                application,
                contactPerson,
                permitHolder,
                Locales.SV);
        permitDecision.setDeliveryAddress(deliveryAddress);

        final PermitDecisionSpeciesAmount spa1 = new PermitDecisionSpeciesAmount();
        spa1.setPermitDecision(permitDecision);
        spa1.setGameSpecies(new GameSpecies(41423, GameCategory.FOWL, "", "", ""));
        spa1.setBeginDate(new LocalDate(2015, 1, 1));
        spa1.setEndDate(new LocalDate(2015, 12, 31));
        spa1.setAmount(1);

        final PermitDecisionSpeciesAmount spa2 = new PermitDecisionSpeciesAmount();
        spa2.setPermitDecision(permitDecision);
        spa2.setGameSpecies(new GameSpecies(41423, GameCategory.FOWL, "", "", ""));
        spa2.setBeginDate(new LocalDate(2016, 1, 1));
        spa2.setEndDate(new LocalDate(2016, 12, 31));
        spa2.setAmount(1);

        final List<PermitDecisionSpeciesAmount> speciesAmountList = Arrays.asList(spa1, spa2);

        final PermitHarvestReportModel model = PermitHarvestReportModel.create(permitDecision, speciesAmountList);
        final Map<Integer, LocalisedString> speciesNameIndex = Collections
                .singletonMap(41423, new LocalisedString("Varis", "Varis"));
        final PermitHarvestReportI18n i18n = new PermitHarvestReportI18n(speciesNameIndex, permitDecision.getLocale());

        return PermitHarvestReportPdf.create(BirdHarvestReportPdfBuilder.getPdf(model, i18n));
    }

    public static void main(final String[] args) {
        try {
            final byte[] pdfData = createDummyPdf().getData();
            final File tempFile = File.createTempFile("bird-harvest-report", ".pdf");
            Files.write(pdfData, tempFile);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(tempFile.toURI());
            }

        } catch (final IOException e) {
            throw new RuntimeException("Could not generate PDF", e);
        }
    }

}
