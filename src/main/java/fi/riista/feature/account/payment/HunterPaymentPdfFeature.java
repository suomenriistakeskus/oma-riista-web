package fi.riista.feature.account.payment;

import fi.riista.feature.account.audit.AccountActivityMessage;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.audit.AccountAuditService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.MediaTypeExtras;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Component
public class HunterPaymentPdfFeature {
    private static final ClassPathResource PDF_TEMPLATE = new ClassPathResource("tilisiirtolomake.pdf");
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final int BAR_CODE_DPI = 300;
    private static final int BAR_CODE_MARGIN_LEFT = 75;
    private static final int BAR_CODE_MARGIN_BOTTOM = 565;
    private static final float BAR_CODE_HEIGHT_MM = 20.0f;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AccountAuditService accountAuditService;

    @Transactional
    public ResponseEntity<byte[]> create(final long personId, final int huntingYear) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);

        final HuntingPaymentInfo paymentInfo = person.getPaymentInfo(huntingYear)
                .orElseThrow(() -> new RuntimeException("Could not calculate paymentInfo"));

        final LocalDate beginDate = DateUtil.huntingYearBeginDate(huntingYear);
        final LocalDate endDate = DateUtil.huntingYearEndDate(huntingYear);
        final String hunterNumber = Objects.requireNonNull(person.getHunterNumber(), "hunterNumber is null");
        final String filename = String.format("%d-%d-riistanhoitomaksu-%s.pdf",
                huntingYear, huntingYear + 1, hunterNumber);

        audit(paymentInfo, person);

        try (final InputStream is = PDF_TEMPLATE.getInputStream();
             final PDDocument pdfTemplate = PDDocument.load(is)) {
            final PaymentPdfBuilder builder = new PaymentPdfBuilder(pdfTemplate)
                    .textField("iban", paymentInfo.getPaymentReceiverIban())
                    .textField("bic", paymentInfo.getPaymentReceiverBic())
                    .textField("saaja", paymentInfo.getPaymentReceiver())
                    .textField("maksaja", formatPaymentRecipient(person).toUpperCase())
                    .textField("voimassa", "Riistanhoitomaksu metsästysvuodelta, Jaktvårdsavgift för jaktåret\n" +
                            String.format("%s - %s",
                                    DATE_PATTERN.print(beginDate),
                                    DATE_PATTERN.print(endDate)))
                    .textField("lisatiedot", paymentInfo.getAdditionalInfo())
                    .textField("erapaiva", DATE_PATTERN.print(DateUtil.today()))
                    .textField("summa", paymentInfo.getAmountText())
                    .textField("viitenumero", paymentInfo.getInvoiceReferenceForHuman())
                    // Use undefined dueDate for barCode -> automatically calculated in client software
                    .barCode(paymentInfo.createBarCodeMessage(null), BAR_CODE_MARGIN_LEFT, BAR_CODE_MARGIN_BOTTOM);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaTypeExtras.APPLICATION_PDF)
                    .header(ContentDispositionUtil.HEADER_NAME,
                            ContentDispositionUtil.encodeAttachmentFilename(filename))
                    .body(builder.build());

        } catch (IOException e) {
            throw new RuntimeException("Could not generate PDF", e);
        }
    }

    private void audit(final HuntingPaymentInfo paymentInfo, final Person person) {
        final SystemUser activeUser = activeUserService.getActiveUser();
        final Authentication authentication = activeUserService.getAuthentication();
        final String barcode = paymentInfo.createBarCodeMessage(null);
        final String auditMessage = person.getHunterNumber() + ":" + barcode;

        accountAuditService.auditUserEvent(activeUser, authentication,
                AccountActivityMessage.ActivityType.PDF_HUNTER_PAYMENT, auditMessage);
    }

    private static String formatPaymentRecipient(final Person person) {
        final Address address = person.getAddress();

        if (address != null &&
                StringUtils.isNotBlank(address.getStreetAddress()) &&
                StringUtils.isNotBlank(address.getPostalCode()) &&
                StringUtils.isNotBlank(address.getCity())) {
            return person.getFullName() + "\n" +
                    address.getStreetAddress() + "\n" +
                    address.getPostalCode() + " " +
                    address.getCity();
        }
        return person.getFullName();
    }

    static class PaymentPdfBuilder {
        private final PDDocument pdfDocument;
        private final PDAcroForm acroForm;

        private PaymentPdfBuilder(final PDDocument pdfDocument) {
            this.pdfDocument = Objects.requireNonNull(pdfDocument);
            this.acroForm = Objects.requireNonNull(pdfDocument.getDocumentCatalog().getAcroForm());
        }

        public PaymentPdfBuilder textField(final String fieldName, final String value) throws IOException {
            final PDField field = acroForm.getField(fieldName);

            if (field != null && field instanceof PDTextField) {
                final PDTextField textField = (PDTextField) field;
                textField.setValue(value);
                textField.setReadOnly(true);
            } else {
                throw new IllegalArgumentException("No such field " + fieldName);
            }

            return this;
        }

        public PaymentPdfBuilder barCode(final String barCodeMessage,
                                         final float x,
                                         final float y) throws IOException {
            final float scale = 100f / BAR_CODE_DPI;
            final Code128Bean code128Bean = new Code128Bean();
            code128Bean.setCodeset(Code128Constants.CODESET_C);
            code128Bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);
            code128Bean.setBarHeight(scale * BAR_CODE_HEIGHT_MM);

            final BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    BAR_CODE_DPI, BufferedImage.TYPE_BYTE_BINARY, false, 0);
            code128Bean.generateBarcode(canvas, barCodeMessage);
            canvas.finish();

            final PDImageXObject pdImage = LosslessFactory.createFromImage(pdfDocument, canvas.getBufferedImage());

            try (final PDPageContentStream contentStream = new PDPageContentStream(
                    pdfDocument, pdfDocument.getPage(0), PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(pdImage, x, y, scale * pdImage.getWidth(), scale * pdImage.getHeight());
            }

            return this;
        }

        public byte[] build() throws IOException {
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

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            pdfDocument.save(bos);
            pdfDocument.close();
            return bos.toByteArray();
        }
    }
}
