package fi.riista.feature.permit.invoice.pdf;

import com.google.common.io.Files;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.CreditorReferenceCalculator;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class PermitDecisionInvoicePdfTest {

    @Test
    public void testGetPdf() throws IOException {
        createDummyPdf();
    }

    private static PermitDecisionInvoicePdf createDummyPdf() throws IOException {
        final PermitDecision dummyDecision = createDummyDecision();
        final Invoice invoice = createInvoice(dummyDecision);
        return PermitDecisionInvoicePdf.createInvoice(dummyDecision, invoice);
    }

    private static Invoice createInvoice(final PermitDecision decision) {
        final LocalDate invoiceDate = DateUtil.today();
        final LocalDate dueDate = DateUtil.today().plusDays(21);
        final int applicationNumber = decision.getApplication().getApplicationNumber();
        final CreditorReference invoiceReference = CreditorReferenceCalculator
                .computeReferenceForPermitDecisionProcessingInvoice(DateUtil.huntingYear(), applicationNumber);

        final Invoice invoice = new Invoice(false);
        invoice.setType(InvoiceType.PERMIT_PROCESSING);
        invoice.setInvoiceNumber(100_000);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setBic(PermitDecisionInvoicePdf.DECISION_BANK_ACCOUNT.getBic());
        invoice.setIban(PermitDecisionInvoicePdf.DECISION_BANK_ACCOUNT.getIban());
        invoice.setDueDate(dueDate);
        invoice.setAmount(decision.getPaymentAmount());
        invoice.setCreditorReference(invoiceReference);
        invoice.setRecipientAddress(decision.getContactPerson().getAddress());
        invoice.setRecipientName(decision.getContactPerson().getFullName());

        return invoice;
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
        permitDecision.setPaymentAmount(PermitDecision.DECISION_PRICE_MOOSELIKE);
        permitDecision.setLocale(Locales.SV);

        return permitDecision;
    }

    public static void main(final String[] args) {
        try {
            final byte[] pdfData = createDummyPdf().getData();
            final File tempFile = File.createTempFile("decision-invoice", ".pdf");
            Files.write(pdfData, tempFile);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(tempFile.toURI());
            }

        } catch (final IOException e) {
            throw new RuntimeException("Could not generate PDF", e);
        }
    }
}
