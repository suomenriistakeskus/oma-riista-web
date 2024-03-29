package fi.riista.feature.permit.invoice.pdf;

import com.google.common.base.Joiner;
import fi.riista.common.AcroFormPdfBuilder;
import fi.riista.util.PdfWriter;
import fi.riista.util.RiistakeskusConstants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

class PermitDecisionInvoicePdfBuilder extends AcroFormPdfBuilder {
    private static final ClassPathResource PDF_TEMPLATE = new ClassPathResource("tilisiirtolomake-bottom.pdf");

    static byte[] getPdf(final PermitDecisionInvoicePdfModel model) throws IOException {
        try (final InputStream is = PDF_TEMPLATE.getInputStream();
             final PDDocument pdfTemplate = PDDocument.load(is)) {

            return new PermitDecisionInvoicePdfBuilder(pdfTemplate, model).build();
        }
    }

    private final PDDocument pdfDocument;
    private final PDPage pdfPage;
    private final PermitDecisionInvoicePdfModel model;

    private PermitDecisionInvoicePdfBuilder(final PDDocument pdfDocument, final PermitDecisionInvoicePdfModel model) {
        super(pdfDocument);
        this.pdfDocument = Objects.requireNonNull(pdfDocument);
        this.pdfPage = Objects.requireNonNull(pdfDocument.getPage(0));
        this.model = Objects.requireNonNull(model);
    }

    private String i18n(final String finnish, final String swedish) {
        return model.getLocalisedString(finnish, swedish);
    }

    private void textField(final String fieldName, final String value) throws IOException {
        final PDField field = acroForm.getField(fieldName);

        if (field instanceof PDTextField) {
            final PDTextField textField = (PDTextField) field;
            textField.setValue(value);
            textField.setReadOnly(true);
        } else {
            throw new IllegalArgumentException("No such field " + fieldName);
        }
    }

    private void addInvoiceSender(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(13).marginLeftMm(20)
                .italicFont()
                .writeLine("Lähettäjä / Avsändare")
                .normalFont();

        for (final String line : model.getInvoiceSender()) {
            writer.writeLine(line);
        }
    }

    private void addInvoiceReceiver(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(40).marginLeftMm(20)
                .italicFont()
                .writeLine("Vastaanottaja / Mottagare")
                .boldFont();

        for (final String line : model.getInvoiceRecipient().formatAsLines()) {
            writer.writeParagraph(line, 50);
        }
    }

    private void addInvoiceHeader(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(13).marginLeftRelative(60)
                .writeLine(model.getInvoiceTitle())
                .writeEmptyLine()
                .writeLine(model.getInvoiceDateString());
    }

    private void addInvoiceDetails(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(40).marginLeftRelative(60)
                .normalFont()
                .writeLine(i18n("Laskunumero", "Fakturanummer"))
                .writeLine(i18n("Asiakasnumero", "Kundnummer"))
                .writeLine(i18n("Maksuehto", "Betalningsvillkor"))
                .writeLine(i18n("Valuutta", "Valuta"));

        if (StringUtils.hasText(model.getPaymentDateString())) {
            writer.writeLine(i18n("Maksupäivä", "Betalningsdatum"));
        }

        writer.topOffsetMm(40).marginLeftRelative(80)
                .normalFont()
                .writeLine(model.getInvoiceNumberString())
                .writeLine(model.getInvoiceRecipient().getCustomerNumber())
                .writeLine(model.getPaymentPolicy())
                .writeLine("EUR")
                .writeLine(model.getPaymentDateString());
    }

    private void addInvoiceLines(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(90).marginLeftMm(20)
                .normalFont()
                .writeLine(i18n("Päätösnumero ja luvansaaja", "Beslutnummer och licenshavaren"))
                .addVerticalSpaceMm(3)
                .writeLine(model.getProductName())
                .writeLine(model.getPermitHolder())
                .writeEmptyLine()
                .marginLeftMm(115)
                .writeLine(i18n("Maksettava yhteensä", "Bet. sammanlagt"));

        writer.topOffsetMm(90).marginLeftRelative(80)
                .normalFont()
                .writeLine(i18n("Hinta", "Pris"))
                .addVerticalSpaceMm(3)
                .writeLine(model.getProductAmountText())
                .writeEmptyLine()
                .writeEmptyLine()
                .writeLine(model.getProductAmountText());

        writer.drawBox(135, 18, 178, 50);
        writer.drawBox(135, 18, 178, 50 - 7);
    }

    private void addSmallPrint(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(140).marginLeftMm(25)
                .smallFont()
                .writeLine(i18n("MMMa (1463/2016) 1 §", "JSM förordning (1463/2016) 1 §"))
                .writeLine(i18n(" ALV (1501/1993) 4 §", "Skattefri försäljning (1501/1993) 4 §"));

        writer.topOffsetMm(140).marginLeftMm(100)
                .writeLine(i18n(
                        "Puh +358 29 431 2001  Y-tunnus 0201724-4  Kotipaikka Helsinki",
                        "Tn +358 29 431 2001  FO-nummer 0201724-4  Hemort Helsingfors"))
                .writeLine(i18n(
                        "Sähköposti asiakaspalvelu@riista.fi   Kotisivut www.riista.fi",
                        "E-post asiakaspalvelu@riista.fi   Hemsida www.riista.fi"));
    }

    private void addFormFieldData() throws IOException {
        textField("iban", model.getInvoiceAccountDetails().getCombinedBankNameAndIbanForInvoicePdf());
        textField("bic", model.getInvoiceAccountDetails().getBic().toString());
        textField("saaja", model.getLocalisedString(RiistakeskusConstants.NAME));
        textField("maksaja", Joiner.on('\n').join(model.getInvoiceRecipient().formatAsLinesWithPermitHolder()));
        textField("erapaiva", model.getDueDateString());
        textField("summa", model.getInvoiceAmountText());
        textField("viitenumero", model.getInvoiceReferenceForHuman());
        textField("lisatiedot", model.getInvoiceAdditionalInfo());


        // Fix annotations
        for (PDPage page : this.pdfDocument.getPages()) {
            for (PDAnnotation annot : page.getAnnotations()) {
                annot.setPage(page);
            }
        }

        // Convert form fields to text
        this.acroForm.flatten();
    }

    public byte[] build() throws IOException {
        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            addInvoiceHeader(writer);
            addInvoiceSender(writer);
            addInvoiceReceiver(writer);
            addInvoiceDetails(writer);
            addInvoiceLines(writer);
            addSmallPrint(writer);
            writer.barCode(model.createBarCodeMessage(model.getDueDate()));
            writer.riistaLogo(10, 180, 20);
        }

        addFormFieldData();

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdfDocument.save(bos);
        pdfDocument.close();
        return bos.toByteArray();
    }
}
