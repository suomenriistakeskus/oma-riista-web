package fi.riista.feature.permit.invoice.pdf;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceAccountDetails;
import fi.riista.feature.permit.invoice.InvoiceRecipient;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.PermitDecisionInvoice;
import fi.riista.util.BigDecimalMoney;
import fi.riista.util.InvoiceUtil;
import fi.riista.util.LocalisedString;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;

class PermitDecisionInvoicePdfModel {
    public enum ResultType {
        INVOICE,
        REMINDER,
        RECEIPT
    }

    static PermitDecisionInvoicePdfModel create(final @Nonnull PermitDecisionInvoicePdfModel.ResultType resultType,
                                                final @Nonnull PermitDecision decision,
                                                final @Nonnull Invoice invoice) {
        requireNonNull(resultType);
        requireNonNull(decision);
        requireNonNull(invoice);

        Preconditions.checkArgument(invoice.getType() == InvoiceType.PERMIT_PROCESSING);

        final HarvestPermitApplication application = requireNonNull(decision.getApplication());
        final LocalDate invoiceDate = resultType == ResultType.REMINDER ? today() : invoice.getInvoiceDate();

        return new PermitDecisionInvoicePdfModel(resultType, decision.getLocale(), invoice.getAmount(),
                InvoiceAccountDetails.create(invoice), invoice.getInvoiceNumber(), invoice.getCreditorReference(),
                InvoiceRecipient.create(decision), invoiceDate, application.getPermitNumber(), invoice.getDueDate(),
                invoice.getPaymentDate());
    }

    private static final DateTimeFormatter INVOICE_DATE_PATTERN = DateTimeFormat.forPattern("d.M.yyyy");
    private static final DateTimeFormatter DUE_DATE_PATTERN = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final ResultType resultType;
    private final Locale locale;
    private final InvoiceAccountDetails invoiceAccountDetails;
    private final InvoiceRecipient invoiceRecipient;
    private final Integer invoiceNumber;
    private final CreditorReference invoiceReference;
    private final LocalDate invoiceDate;
    private final LocalDate dueDate;
    private final LocalDate paymentDate;
    private final String permitNumber;
    private final BigDecimalMoney paymentAmount;

    PermitDecisionInvoicePdfModel(final @Nonnull ResultType resultType,
                                  final @Nonnull Locale locale,
                                  final @Nonnull BigDecimal paymentAmount,
                                  final @Nonnull InvoiceAccountDetails invoiceAccountDetails,
                                  final @Nonnull Integer invoiceNumber,
                                  final @Nonnull CreditorReference invoiceReference,
                                  final @Nonnull InvoiceRecipient invoiceRecipient,
                                  final @Nonnull LocalDate invoiceDate,
                                  final @Nonnull String permitNumber,
                                  final LocalDate dueDate,
                                  final LocalDate paymentDate) {
        this.resultType = requireNonNull(resultType);
        this.locale = requireNonNull(locale);
        this.invoiceAccountDetails = requireNonNull(invoiceAccountDetails);
        this.invoiceNumber = requireNonNull(invoiceNumber);
        this.invoiceReference = requireNonNull(invoiceReference);
        this.paymentAmount = new BigDecimalMoney(paymentAmount);
        this.invoiceDate = requireNonNull(invoiceDate);
        this.invoiceRecipient = requireNonNull(invoiceRecipient);
        this.permitNumber = requireNonNull(permitNumber);
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;

        Preconditions.checkArgument(StringUtils.isNotBlank(permitNumber));
        Preconditions.checkArgument(this.invoiceReference.isValid());
        Preconditions.checkArgument(resultType != ResultType.INVOICE || dueDate != null && paymentDate == null);
        Preconditions.checkArgument(resultType != ResultType.RECEIPT || paymentDate != null);
    }

    @Nonnull
    public String getPdfFileName() {
        final String invoiceTypeName = PermitDecisionInvoice.getInvoiceTypeName(locale);

        switch (resultType) {
            case REMINDER:
                return String.format("%s-%s-%s.pdf", getInvoiceTitle().toLowerCase(), invoiceTypeName, permitNumber);
            default:
                return String.format("%s-%s.pdf", invoiceTypeName, permitNumber);
        }
    }

    @Nonnull
    public String getLocalisedString(final LocalisedString localisedString) {
        return requireNonNull(localisedString).getTranslation(locale);
    }

    @Nonnull
    public String getLocalisedString(final String finnish, final String swedish) {
        return getLocalisedString(new LocalisedString(finnish, swedish));
    }

    @Nonnull
    public BigDecimalMoney getAmount() {
        return paymentAmount;
    }

    @Nonnull
    public String getProductAmountText() {
        return paymentAmount.formatPaymentAmount();
    }

    @Nonnull
    public String getInvoiceAmountText() {
        switch (resultType) {
            case INVOICE:
            case REMINDER:
                return getProductAmountText();
            case RECEIPT:
                return getLocalisedString("MAKSETTU", "BETALD");
            default:
                throw new IllegalArgumentException("invalid resultType");
        }
    }

    @Nonnull
    public InvoiceAccountDetails getInvoiceAccountDetails() {
        return invoiceAccountDetails;
    }

    @Nonnull
    public InvoiceRecipient getInvoiceRecipient() {
        return invoiceRecipient;
    }

    @Nonnull
    public List<String> getInvoiceSender() {
        return Arrays.asList(
                InvoicePdfConstants.RK_NAME.getTranslation(locale),
                InvoicePdfConstants.RK_STREET.getTranslation(locale),
                InvoicePdfConstants.RK_POST_OFFICE.getTranslation(locale),
                InvoicePdfConstants.RK_PHONE);
    }

    @Nullable
    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    @Nonnull
    public String getInvoiceNumberString() {
        return Optional.ofNullable(invoiceNumber).map(String::valueOf).orElse("");
    }

    @Nonnull
    public String getProductName() {
        return getLocalisedString(
                String.format("Päätös %s, käsittelymaksu", permitNumber),
                String.format("Beslut %s, handläggningsavgift", permitNumber));
    }

    @NotNull
    public String getInvoiceTitle() {
        switch (resultType) {
            case REMINDER:
                return getLocalisedString("MAKSUMUISTUTUS", "BETALNINGSPÅMINNELSE");
            case RECEIPT:
                return getLocalisedString("KUITTI", "KVITTO");
            case INVOICE:
                return getLocalisedString("LASKU", "FAKTURA");
            default:
                throw new IllegalArgumentException("invalid resultType");
        }
    }

    @Nonnull
    public String getPaymentPolicy() {
        switch (resultType) {
            case REMINDER:
                return getLocalisedString("HETI", "OMEDELBART");
            case RECEIPT:
            case INVOICE:
                final int days = InvoiceType.PERMIT_PROCESSING.getTermOfPayment().getDays();
                return getLocalisedString(days + " pv", days + " dagar");
            default:
                throw new IllegalArgumentException("invalid resultType");
        }
    }

    @Nonnull
    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    @Nonnull
    public String getInvoiceDateString() {
        return INVOICE_DATE_PATTERN.print(invoiceDate);
    }

    @Nonnull
    public LocalDate getDueDate() {
        return dueDate;
    }

    @Nonnull
    public String getDueDateString() {
        switch (resultType) {
            case REMINDER:
                return getLocalisedString("HETI", "OMEDELBART");
            case RECEIPT:
            case INVOICE:
                return DUE_DATE_PATTERN.print(dueDate);
            default:
                throw new IllegalArgumentException("invalid resultType");
        }
    }

    @Nonnull
    public String getPaymentDateString() {
        return paymentDate != null ? INVOICE_DATE_PATTERN.print(paymentDate) : "";
    }

    @Nonnull
    public String getInvoiceReferenceForHuman() {
        return invoiceReference.toString();
    }

    @Nonnull
    public String getInvoiceAdditionalInfo() {
        return InvoicePdfConstants.ADDITIONAL_INFO.getTranslation(this.locale);
    }

    @Nonnull
    public String getPermitNumber() {
        return permitNumber;
    }

    @Nonnull
    public String createBarCodeMessage(final LocalDate dueDate) {
        return InvoiceUtil.createBarCodeMessage(paymentAmount.getEuros(), paymentAmount.getCents(), invoiceReference,
                invoiceAccountDetails.getIban(), dueDate);
    }
}
