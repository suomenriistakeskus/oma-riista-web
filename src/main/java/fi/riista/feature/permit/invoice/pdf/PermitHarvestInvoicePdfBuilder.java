package fi.riista.feature.permit.invoice.pdf;

import com.google.common.base.Joiner;
import fi.riista.common.AcroFormPdfBuilder;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.util.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.iban4j.Bic;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static fi.riista.feature.common.money.FinnishBankAccount.MOOSELIKE_HARVEST_FEE_OP_POHJOLA;
import static fi.riista.feature.permit.invoice.pdf.PermitHarvestInvoicePdfModel.HARVEST_FEE_ACCOUNTS;
import static java.util.stream.Collectors.joining;

class PermitHarvestInvoicePdfBuilder extends AcroFormPdfBuilder {
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
        super(pdfDocument);
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

    private void addInvoiceSender(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(13).marginLeftMm(20)
                .italicFont()
                .writeLine("Lähettäjä / Avsändare")
                .normalFont();

        for (final String line : model.getInvoiceRecipient().formatAsLines()) {
            writer.writeLine(line);
        }
    }

    private void addInvoiceReceiver(final PdfWriter writer) throws IOException {
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

    private void addInvoiceHeader(final PdfWriter writer) throws IOException {
        writer.topOffsetMm(13).marginLeftRelative(65)
                .normalFont()
                .writeLine(model.getInvoiceTitle())
                .writeEmptyLine()
                .writeLine(model.getInvoiceDateString());

        writer.topOffsetMm(45).marginLeftRelative(65)
                .normalFont()
                .writeLine(i18n("Päätösnumero", "Beslutnummer"))
                .writeLine(i18n("Asiakasnumero", "Kundnummer"));

        if (StringUtils.hasText(model.getPaymentDateString())) {
            writer.writeLine(i18n("Maksupäivä", "Betalningsdatum"));
        }

        writer.topOffsetMm(45).marginLeftRelative(80)
                .normalFont()
                .writeLine(model.getPermitNumber())
                .writeLine(model.getInvoiceRecipient().getCustomerNumber())
                .writeLine(model.getPaymentDateString());
    }

    private void addInvoiceText(final PdfWriter writer) throws IOException {

        writer.topOffsetMm(70).marginLeftMm(20)
                .font(PDType1Font.HELVETICA_BOLD, 12)
                .writeLine(model.getInvoiceHeader())
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
                                "verifikat över att jaktlicensavgiften betalats fogas till byteanmälan. Anmälan ska " +
                                "göras inom " +
                                "sju dygn efter det att de i jaktlicensen nämnda djuren fällts eller, om djur inte " +
                                "fällts, " +
                                "inom sju dygn efter det att fredningstiden har börjat."))
                .writeEmptyLine()

                .writeParagraph(i18n(
                        "Valtiolle on maksettava maksu kaadetuista hirvieläimistä oheisella tilisiirtolomakkeella" +
                                " (esim. pankissa tai nettipankissa).",
                        "Betalning till staten skall göras för de fällda hjortdjuren med det bifogade bankgirot" +
                                " (tex i bank eller nätbank)."))
                .writeParagraph(i18n(
                        "Pankkiyhteystietomme muuttuvat 1.12.2020.",
                        "Den 1 december 2020 ändras våra bankkontouppgifter."))
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
                                "jaktlicensavgift inte inom föreskriven tid ska Finlands viltcentral driva in " +
                                "avgiften. " +
                                "Bestämmelser om avgiftens direkta utsökbarhet utan utsökningsgrund finns i lagen om " +
                                "verkställighet av skatter och avgifter (706/2007)."));
    }

    private void addPaymentTable(final PdfWriter writer) throws IOException {
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

        textField("bic", writeBics());
        textField("saaja", model.getPaymentRecipient());
        textField("maksaja", Joiner.on('\n').join(model.getInvoiceRecipient().formatAsLines()));
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

    private void writeIbans(final PdfWriter writer) throws IOException {
        // Write ibans separately in order to be able to justify account numbers

        // TODO: Remove hard coded validity periods once OP account is no longer used
        final FinnishBankAccount op = MOOSELIKE_HARVEST_FEE_OP_POHJOLA;
        checkState(HARVEST_FEE_ACCOUNTS.size() == 3);
        checkState(HARVEST_FEE_ACCOUNTS.get(0).equals(op));

        writer.font(PDType1Font.TIMES_ROMAN, 8f).topOffsetMm(192).marginLeftMm(32)
                .writeLine(HARVEST_FEE_ACCOUNTS.get(0).getBankName())
                .writeLine(HARVEST_FEE_ACCOUNTS.get(1).getBankName())
                .writeLine(HARVEST_FEE_ACCOUNTS.get(2).getBankName());

        writer.topOffsetMm(192).marginLeftMm(45)
                .writeLine(iban(HARVEST_FEE_ACCOUNTS.get(0)))
                .writeLine(iban(HARVEST_FEE_ACCOUNTS.get(1)))
                .writeLine(iban(HARVEST_FEE_ACCOUNTS.get(2)));

        writer.topOffsetMm(192).marginLeftMm(80)
                .writeLine(i18n("(30.11.2020 asti)", "(till 30.11.2020)"))
                .writeLine(i18n("(1.12.2020 lähtien)", "(från 1.12.2020)"))
                .writeLine(i18n("(1.12.2020 lähtien)", "(från 1.12.2020)"));
    }

    private String iban(final FinnishBankAccount account) {
        return account.getIban().toFormattedString();
    }

    private String writeBics() {
        return HARVEST_FEE_ACCOUNTS.stream()
                .map(FinnishBankAccount::getBic)
                .map(Bic::toString)
                .collect(joining("\n"));

    }

    public byte[] build() throws IOException {
        try (final PdfWriter writer = new PdfWriter(pdfDocument, pdfPage)) {
            addInvoiceHeader(writer);
            addInvoiceSender(writer);
            addInvoiceReceiver(writer);
            addInvoiceText(writer);

            if (model.includePaymentTable()) {
                addPaymentTable(writer);
            }

            writer.barCode(model.createBarCodeMessage(model.getDueDate()));
            writer.riistaLogo(10, 180, 20);

            // Ibans are explicitly written for justifying ibans after bank names
            writeIbans(writer);
        }

        addFormFieldData();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdfDocument.save(bos);
        pdfDocument.close();
        return bos.toByteArray();
    }

}
