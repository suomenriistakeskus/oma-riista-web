package fi.riista.feature.permit.invoice.pdf;

import com.google.common.base.Joiner;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

class PermitHarvestInvoicePdfBuilder {
    private static final ClassPathResource PDF_TEMPLATE = new ClassPathResource("tilisiirtolomake-bottom.pdf");

    static byte[] getPdf(final PermitHarvestInvoicePdfModel model) throws IOException {
        try (final InputStream is = PDF_TEMPLATE.getInputStream();
             final PDDocument pdfTemplate = PDDocument.load(is)) {

            return new PermitHarvestInvoicePdfBuilder(pdfTemplate, model).build();
        }
    }

    private final PDDocument pdfDocument;
    private final PDPage pdfPage;
    private final PDAcroForm acroForm;
    private final PermitHarvestInvoicePdfModel model;

    private PermitHarvestInvoicePdfBuilder(final PDDocument pdfDocument, final PermitHarvestInvoicePdfModel model) {
        this.pdfDocument = Objects.requireNonNull(pdfDocument);
        this.pdfPage = Objects.requireNonNull(pdfDocument.getPage(0));
        this.acroForm = Objects.requireNonNull(pdfDocument.getDocumentCatalog().getAcroForm());
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

    private void addInvoiceSender(final InvoicePdfWriter writer) throws IOException {
        writer.topOffsetMm(13).marginLeftMm(20)
                .italicFont()
                .writeLine("Lähettäjä / Avsändare")
                .normalFont();

        for (final String line : model.getInvoiceRecipient().formatAsLines()) {
            writer.writeLine(line);
        }
    }

    private void addInvoiceReceiver(final InvoicePdfWriter writer) throws IOException {
        writer.topOffsetMm(40).marginLeftMm(20)
                .italicFont()
                .writeLine("Vastaanottaja / Mottagare")
                .boldFont()
                .writeLine(i18n(
                        "Suomen riistakeskus, lupahallintokirjaamo",
                        "Finlands Viltcentral /Tillståndsförvaltningens registratur"))
                .writeLine(i18n("Kuralankatu 2 ", "Kuralagatan 2"))
                .writeLine(i18n("20540 Turku", "205040 Åbo"));
    }

    private void addInvoiceHeader(final InvoicePdfWriter writer) throws IOException {
        writer.topOffsetMm(13).marginLeftRelative(65)
                .normalFont()
                .writeLine(i18n("LASKU", "FAKTURA"))
                .writeEmptyLine()
                .writeLine(model.getInvoiceDateString());

        writer.topOffsetMm(45).marginLeftRelative(65)
                .normalFont()
                .writeLine(i18n("Päätösnumero", "Beslutnummer"))
                .writeLine(i18n("Asiakasnumero", "Kundnummer"));

        writer.topOffsetMm(45).marginLeftRelative(80)
                .normalFont()
                .writeLine(model.getPermitNumber())
                .writeLine(model.getInvoiceRecipient().getCustomerNumber());
    }

    private void addInvoiceText(final InvoicePdfWriter writer) throws IOException {

        writer.topOffsetMm(70).marginLeftMm(20)
                .font(PDType1Font.HELVETICA_BOLD, 12)
                .writeLine(model.getInvoiceTitle())
                .addVerticalSpaceMm(2)

                .font(PDType1Font.HELVETICA_BOLD, 9.5f)
                .writeLine(i18n(
                        "Pyyntilupamaksu ja sen maksaminen",
                        "Licensavgiften och dess betalning"))
                .addVerticalSpaceMm(2)

                .font(PDType1Font.HELVETICA, 9.5f)
                .writeParagraph(i18n(
                        "Riistanhoitomaksusta ja pyyntilupamaksusta annetun lain (616/1993) 4 §:n mukaan: " +
                                "Metsästyslain 26 §:ssä tarkoitetusta hirvieläimen pyyntiluvasta on suoritettava maksu " +
                                "(pyyntilupamaksu) valtiolle. Pyyntilupamaksu suoritetaan kultakin kaadetulta " +
                                "hirvieläimeltä. Luvan saajan on suoritettava pyyntilupamaksu viimeistään seitsemän " +
                                "päivän kuluttua hirvieläimen metsästyskauden päättymisestä sen mukaan kuin " +
                                "valtioneuvoston asetuksella säädetään.",
                        "Lag om viltvårdsavgift och licensavgift (616/1993) 4 §: För i 26 § i jaktlagen avsedd " +
                                "jaktlicens för hjortdjur ska det betalas en avgift (jaktlicensavgift) till staten. " +
                                "Jaktlicensavgiften bestäms enligt antalet fällda hjortdjur. Licenshavaren ska betala " +
                                "jaktlicensavgiften inom sju dagar efter det att jaktsäsongen för hjortdjur är avslutad " +
                                "i enlighet med vad som föreskrivs genom förordning av statsrådet."))
                .writeEmptyLine()
                .writeParagraph(i18n(
                        "Metsästysasetuksen (666/1993) 9 §:n mukaan metsästyksen tuloksesta tehtävään " +
                                "ilmoitukseen on liitettävä tosite pyyntilupamaksun suorittamisesta. Ilmoitus on tehtävä " +
                                "seitsemän vuorokauden kuluessa pyyntiluvassa mainittujen eläinten tultua pyydystetyiksi " +
                                "taikka, jos eläimiä on jäänyt pyydystämättä, seitsemän vuorokauden kuluessa rauhoitusajan " +
                                "alkamisesta.",
                        "Enligt Jaktförordningens (666/1993) 9 § skall ett " +
                                "verifikat över att jaktlicensavgiften betalats fogas till byteanmälan. Anmälan ska göras inom " +
                                "sju dygn efter det att de i jaktlicensen nämnda djuren fällts eller, om djur inte fällts, " +
                                "inom sju dygn efter det att fredningstiden har börjat."))
                .writeEmptyLine()

                .writeParagraph(i18n(
                        "Valtiolle on maksettava maksu kaadetuista hirvieläimistä oheisella tilisiirtolomakkeella" +
                                " (esim. pankissa tai nettipankissa).",
                        "Betalning till staten skall göras för de fällda hjortdjuren med det bifogade bankgirot" +
                                " (tex i bank eller nätbank)"))
                .writeEmptyLine()

                .font(PDType1Font.HELVETICA_BOLD, 9.5f)
                .writeLine(i18n(
                        "Seuraamukset laiminlyönneistä",
                        "Påföljd av försummelse"))
                .addVerticalSpaceMm(2)

                .font(PDType1Font.HELVETICA, 9.5f)
                .writeParagraph(i18n(
                        "Metsästyslain (615/1993) 74 §:n mukaan em. ilmoitusvelvollisuuden laiminlyöjä syyllistyy metsästysrikkomukseen.",
                        "Enligt jaktlagens (615/1993) 74 § gör sig den som försummar att göra den ovan nämnda bytesanmälan, sig skyldig till en jaktförseelse."))
                .writeEmptyLine()
                .writeParagraph(i18n(
                        "Riistanhoitomaksusta ja pyyntilupamaksusta annetun lain 5 §:n mukaan: Jos " +
                                "pyyntilupamaksua ei suoriteta määräajassa, Suomen riistakeskuksen on pantava se maksuun. " +
                                "Maksun suorasta ulosottokelpoisuudesta  ilman ulosottoperustetta säädetään verojen ja " +
                                "maksujen täytäntöönpanosta annetussa laissa (706/2007).",
                        "Enligt lagen om viltvårdsavgift och licensavgift  (616 /1993) 5 §: Betalas en " +
                                "jaktlicensavgift inte inom föreskriven tid ska Finlands viltcentral driva in avgiften. " +
                                "Bestämmelser om avgiftens direkta utsökbarhet utan utsökningsgrund finns i lagen om " +
                                "verkställighet av skatter och avgifter (706/2007)."));
    }

    private void addPaymentTable(final InvoicePdfWriter writer) throws IOException {
        writer.font(PDType1Font.HELVETICA, 9.5f, 20f);

        final int baseTopOffset = 165;

        writer.topOffsetMm(baseTopOffset).marginLeftMm(25)
                .writeLine(i18n("Saalis", "Byte"))
                .writeLine(i18n("Aikuiset", "Vuxna"))
                .writeLine(i18n("Vasat", "Kalvar"));

        writer.topOffsetMm(baseTopOffset).marginLeftMm(43)
                .writeLine(i18n("Uros", "Hane"))
                .writeLine("______  +")
                .writeLine("______  +");

        writer.topOffsetMm(baseTopOffset).marginLeftMm(63)
                .writeLine(i18n("Naaras", "Hona"))
                .writeLine("______  =")
                .writeLine("______  =");

        writer.topOffsetMm(baseTopOffset).marginLeftMm(80)
                .writeLine(i18n("yht/kpl", "tot/st"))
                .writeLine("_______  -")
                .writeLine("_______  -");

        writer.topOffsetMm(baseTopOffset - 4.5f).marginLeftMm(100)
                .writeLine(i18n("Vähennä ML", "Dra av"))
                .addVerticalSpaceMm(-2.5f)
                .writeLine(i18n("28 §:n myön.", "beviljade enl."))
                .writeLine(i18n("_________ kpl   =", "_________ st    ="))
                .writeLine(i18n("_________ kpl   =", "_________ st    ="));

        writer.topOffsetMm(baseTopOffset).marginLeftMm(130)
                .writeLine(i18n("Maksettavat", "Skall betalas"))
                .writeLine(i18n("_________ kpl    x", "_________ st     x"))
                .writeLine(i18n("_________ kpl    x", "_________ st     x"));

        writer.topOffsetMm(baseTopOffset).marginLeftMm(162)
                .writeLine(i18n("Euroa", "Euro"))
                .writeLine(String.format("%s  =  _________", model.getSpecimenPrice().formatAdultPrice()))
                .writeLine(String.format("%s  =  _________", model.getSpecimenPrice().formatYoungPrice()));
    }

    private void addFormFieldData() throws IOException {
        textField("iban", model.getInvoiceAccountDetails().getCombinedBankNameAndIbanString());
        textField("bic", model.getInvoiceAccountDetails().getBic().toString());
        textField("saaja", model.getPaymentRecipient());
        textField("maksaja", Joiner.on('\n').join(model.getInvoiceRecipient().formatAsLines()));
        textField("summa", model.getAmountText());
        textField("viitenumero", model.getInvoiceReferenceForHuman());
        textField("lisatiedot", model.getInvoiceAdditionalInfo());

        this.acroForm.setNeedAppearances(false);

        // Fix annotations
        for (PDPage page : this.pdfDocument.getPages()) {
            for (PDAnnotation annot : page.getAnnotations()) {
                annot.setPage(page);
            }
        }

        // Define font resources names used in PDF template
        final PDResources dr = new PDResources();
        dr.put(COSName.getPDFName("Helv"), PDType1Font.HELVETICA);
        dr.put(COSName.getPDFName("HeBo"), PDType1Font.HELVETICA_BOLD);
        this.acroForm.setDefaultResources(dr);

        // Convert form fields to text
        this.acroForm.flatten();
    }

    public byte[] build() throws IOException {
        try (final InvoicePdfWriter writer = new InvoicePdfWriter(pdfDocument, pdfPage)) {
            addInvoiceHeader(writer);
            addInvoiceSender(writer);
            addInvoiceReceiver(writer);
            addInvoiceText(writer);
            addPaymentTable(writer);
            writer.barCode(model.createBarCodeMessage(null));
            writer.riistaLogo(10, 180, 20);
        }

        addFormFieldData();

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdfDocument.save(bos);
        pdfDocument.close();
        return bos.toByteArray();
    }

}
