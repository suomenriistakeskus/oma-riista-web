package fi.riista.feature.permit.invoice.pdf;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.util.BigDecimalMoney;
import fi.riista.util.InvoiceUtil;
import fi.riista.util.LocalisedString;
import fi.riista.util.RiistakeskusConstants;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasText;

class PermitDecisionInvoicePdfModel {

    public enum ResultType {
        INVOICE,
        REMINDER,
        RECEIPT
    }

    static PermitDecisionInvoicePdfModel create(final @Nonnull ResultType resultType,
                                                final @Nonnull PermitDecision decision,
                                                final @Nonnull Invoice invoice) {
        requireNonNull(resultType);
        requireNonNull(decision);
        requireNonNull(invoice);

        Preconditions.checkArgument(invoice.getType() == InvoiceType.PERMIT_PROCESSING);

        final boolean isReminder = resultType == ResultType.REMINDER;
        final LocalDate invoiceDate = isReminder ? today() : invoice.getInvoiceDate();
        final BigDecimal amount = isReminder ? invoice.getRemainingAmount() : invoice.getAmount();

        return new PermitDecisionInvoicePdfModel(resultType, decision.getLocale(), amount,
                invoice.resolveBankAccountDetails(), invoice.getInvoiceNumber(), invoice.getCreditorReference(),
                InvoicePdfRecipient.create(decision), invoiceDate, decision.createPermitNumber(), decision.getPermitHolder(),
                invoice.getDueDate(),
                invoice.getPaymentDate());
    }


    private static final DateTimeFormatter INVOICE_DATE_PATTERN = DateTimeFormat.forPattern("d.M.yyyy");
    private static final DateTimeFormatter DUE_DATE_PATTERN = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final ResultType resultType;
    private final Locale locale;
    private final FinnishBankAccount invoiceAccountDetails;
    private final InvoicePdfRecipient invoiceRecipient;
    private final int invoiceNumber;
    private final CreditorReference invoiceReference;
    private final LocalDate invoiceDate;
    private final LocalDate dueDate;
    private final LocalDate paymentDate;
    private final String permitNumber;
    private final PermitHolder permitHolder;
    private final BigDecimalMoney paymentAmount;

    PermitDecisionInvoicePdfModel(final @Nonnull ResultType resultType,
                                  final @Nonnull Locale locale,
                                  final @Nonnull BigDecimal paymentAmount,
                                  final @Nonnull FinnishBankAccount invoiceAccountDetails,
                                  final int invoiceNumber,
                                  final @Nonnull CreditorReference invoiceReference,
                                  final @Nonnull InvoicePdfRecipient invoiceRecipient,
                                  final @Nonnull LocalDate invoiceDate,
                                  final @Nonnull String permitNumber,
                                  final @Nonnull PermitHolder permitHolder,
                                  final LocalDate dueDate,
                                  final LocalDate paymentDate) {
        this.resultType = requireNonNull(resultType);
        this.locale = requireNonNull(locale);
        this.invoiceAccountDetails = requireNonNull(invoiceAccountDetails);
        this.invoiceNumber = invoiceNumber;
        this.invoiceReference = requireNonNull(invoiceReference);
        this.paymentAmount = new BigDecimalMoney(paymentAmount);
        this.invoiceDate = requireNonNull(invoiceDate);
        this.invoiceRecipient = requireNonNull(invoiceRecipient);
        this.permitNumber = requireNonNull(permitNumber);
        this.permitHolder = requireNonNull(permitHolder);
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;

        Preconditions.checkArgument(StringUtils.isNotBlank(permitNumber));
        Preconditions.checkArgument(this.invoiceReference.isValid());
        Preconditions.checkArgument(resultType != ResultType.INVOICE || dueDate != null && paymentDate == null);
        Preconditions.checkArgument(resultType != ResultType.RECEIPT || paymentDate != null);
    }

    @Nonnull
    public String getPdfFileName() {
        final String invoiceTypeName = InvoiceType.PERMIT_PROCESSING.getName(locale).toLowerCase();

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
        return getProductAmountText();
    }

    @Nonnull
    public FinnishBankAccount getInvoiceAccountDetails() {
        return invoiceAccountDetails;
    }

    @Nonnull
    public InvoicePdfRecipient getInvoiceRecipient() {
        return invoiceRecipient;
    }

    @Nonnull
    public List<String> getInvoiceSender() {
        return Arrays.asList(
                RiistakeskusConstants.NAME.getTranslation(locale),
                RiistakeskusConstants.STREET_ADDRESS.getTranslation(locale),
                RiistakeskusConstants.POST_OFFICE.getTranslation(locale),
                RiistakeskusConstants.PHONE_NUMBER);
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    @Nonnull
    public String getInvoiceNumberString() {
        return String.valueOf(invoiceNumber);
    }

    @Nonnull
    public String getProductName() {
        return getLocalisedString(
                String.format("Päätös %s, käsittelymaksu", permitNumber),
                String.format("Beslut %s, handläggningsavgift", permitNumber));
    }

    @Nonnull
    public String getPermitHolder() {
        final String code = permitHolder.getCode();
        return hasText(code)
                ? String.format("%s (%s)", permitHolder.getName(), code)
                : permitHolder.getName();
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
                return getLocalisedString("MAKSETTU", "BETALD");
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
        final StringBuilder sb = new StringBuilder();
        sb.append(InvoicePdfConstants.INFO_LINE_1.getTranslation(locale));
        sb.append("\n\n");
        sb.append(InvoicePdfConstants.INFO_LINE_2.getTranslation(locale));

        if (resultType != ResultType.RECEIPT) {
            sb.append("\n\n");
            sb.append(InvoicePdfConstants.INFO_LINE_3.getTranslation(locale));
        }

        return sb.toString();
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
