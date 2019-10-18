package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.money.FinnishBankAccount;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.payment.MooselikePrice;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.CreditorReferenceCalculator;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.util.BigDecimalMoney;
import fi.riista.util.InvoiceUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.requireNonNull;

public class PermitHarvestInvoicePdfModel {

    private enum ResultType {
        INVOICE,
        REMINDER,
        RECEIPT
    }

    static final String PAYMENT_RECIPIENT = "MMM/Hirvieläinten pyyntilupamaksujen keräilytili";

    static final DateTimeFormatter INVOICE_DATE_PATTERN = DateTimeFormat.forPattern("d.M.yyyy");
    static final DateTimeFormatter DUE_DATE_PATTERN = DateTimeFormat.forPattern("dd.MM.yyyy");

    public static PermitHarvestInvoicePdfModel createInvoice(final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                             final @Nonnull Invoice invoice) {

        return create(ResultType.INVOICE, speciesAmount, invoice);
    }

    public static PermitHarvestInvoicePdfModel createReceipt(final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                             final @Nonnull Invoice invoice) {

        requireNonNull(invoice.getPaymentDate(), "paymentDate is null");
        return create(ResultType.RECEIPT, speciesAmount, invoice);
    }

    public static PermitHarvestInvoicePdfModel createReminder(final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                              final @Nonnull Invoice invoice) {

        return create(ResultType.REMINDER, speciesAmount, invoice);
    }

    private static PermitHarvestInvoicePdfModel create(final @Nonnull ResultType resultType,
                                                       final @Nonnull HarvestPermitSpeciesAmount speciesAmount,
                                                       final @Nonnull Invoice invoice) {
        requireNonNull(resultType);
        requireNonNull(speciesAmount);
        requireNonNull(invoice);

        final HarvestPermit permit = speciesAmount.getHarvestPermit();
        final GameSpecies gameSpecies = speciesAmount.getGameSpecies();
        final PermitHarvestInvoicePdfPrice specimenPrice =
                new PermitHarvestInvoicePdfPrice(MooselikePrice.get(gameSpecies));

        final BigDecimal amount;

        switch (resultType) {
            case INVOICE:
                amount = invoice.getAmount();
                break;
            case RECEIPT:
                amount = invoice.getReceiptAmount();
                break;
            case REMINDER:
                amount = invoice.getRemainingAmount();
                break;
            default:
                throw new IllegalArgumentException("invalid resultType");
        }

        return new PermitHarvestInvoicePdfModel(permit.getPermitDecision().getLocale(), resultType,
                permit.getPermitNumber(), specimenPrice, gameSpecies.getNameLocalisation(),
                invoice.resolveBankAccountDetails(), invoice.getCreditorReference(), InvoicePdfRecipient.create(permit),
                invoice.getInvoiceDate(), amount, invoice.getDueDate(), invoice.getPaymentDate());
    }

    public static PermitHarvestInvoicePdfModel createBlank(final @Nonnull PermitDecision decision,
                                                           final @Nonnull FinnishBankAccount invoiceAccountDetails,
                                                           final @Nonnull GameSpecies gameSpecies) {
        requireNonNull(decision);
        requireNonNull(invoiceAccountDetails);
        requireNonNull(gameSpecies);

        final LocalDate invoiceDate = decision.getPublishDate().toLocalDate();
        final PermitHarvestInvoicePdfPrice specimenPrice = new PermitHarvestInvoicePdfPrice(MooselikePrice.get(gameSpecies));
        final CreditorReference invoiceReference = CreditorReferenceCalculator.computeReferenceForPermitHarvestInvoice(
                decision.getDecisionYear(), decision.getDecisionNumber(), gameSpecies.getOfficialCode());

        return new PermitHarvestInvoicePdfModel(decision.getLocale(), ResultType.INVOICE, decision.createPermitNumber(),
                specimenPrice, gameSpecies.getNameLocalisation(), invoiceAccountDetails, invoiceReference,
                InvoicePdfRecipient.create(decision), invoiceDate, null, null, null);
    }

    private final Locale locale;
    private final ResultType resultType;
    private final String permitNumber;
    private final PermitHarvestInvoicePdfPrice specimenPrice;
    private final LocalisedString speciesName;
    private final FinnishBankAccount invoiceAccountDetails;
    private final InvoicePdfRecipient invoiceRecipient;
    private final CreditorReference invoiceReference;
    private final LocalDate invoiceDate;
    private final LocalDate dueDate;
    private final LocalDate paymentDate;
    private final BigDecimalMoney paymentAmount;

    PermitHarvestInvoicePdfModel(final @Nonnull Locale locale,
                                 final @Nonnull ResultType resultType,
                                 final @Nonnull String permitNumber,
                                 final @Nonnull PermitHarvestInvoicePdfPrice specimenPrice,
                                 final @Nonnull LocalisedString speciesName,
                                 final @Nonnull FinnishBankAccount invoiceAccountDetails,
                                 final @Nonnull CreditorReference invoiceReference,
                                 final @Nonnull InvoicePdfRecipient invoiceRecipient,
                                 final @Nonnull LocalDate invoiceDate,
                                 final BigDecimal paymentAmount,
                                 final LocalDate dueDate,
                                 final LocalDate paymentDate) {
        this.locale = requireNonNull(locale);
        this.resultType = requireNonNull(resultType);
        this.permitNumber = requireNonNull(permitNumber);
        this.specimenPrice = requireNonNull(specimenPrice);
        this.speciesName = requireNonNull(speciesName);
        this.invoiceAccountDetails = requireNonNull(invoiceAccountDetails);
        this.invoiceReference = requireNonNull(invoiceReference);
        this.invoiceDate = requireNonNull(invoiceDate);
        this.invoiceRecipient = requireNonNull(invoiceRecipient);
        this.paymentAmount = paymentAmount != null ? new BigDecimalMoney(paymentAmount) : null;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;

        checkArgument(this.invoiceReference.isValid());
        checkArgument(resultType != ResultType.INVOICE || paymentDate == null);
        checkArgument(resultType != ResultType.REMINDER || paymentAmount != null && dueDate != null);
        checkArgument(resultType != ResultType.RECEIPT || paymentDate != null && dueDate != null);
    }

    @Nonnull
    public String getPdfFileName() {
        return String.format("%s-%s-%s.pdf",
                InvoiceType.PERMIT_HARVEST.getName(locale).toLowerCase(),
                permitNumber,
                speciesName.getTranslation(locale));
    }

    @Nonnull
    public String getLocalisedString(final String finnish, final String swedish) {
        return new LocalisedString(finnish, swedish).getTranslation(locale);
    }

    public boolean includePaymentTable() {
        return resultType == ResultType.INVOICE && paymentAmount == null;
    }

    @Nonnull
    public String getInvoiceAmountText() {
        return paymentAmount != null ? paymentAmount.formatPaymentAmount() : "";
    }

    @Nonnull
    public FinnishBankAccount getInvoiceAccountDetails() {
        return invoiceAccountDetails;
    }

    @Nonnull
    public PermitHarvestInvoicePdfPrice getSpecimenPrice() {
        return specimenPrice;
    }

    @Nonnull
    public String getPaymentRecipient() {
        return PAYMENT_RECIPIENT;
    }

    @Nonnull
    public InvoicePdfRecipient getInvoiceRecipient() {
        return invoiceRecipient;
    }

    @Nonnull
    public String getInvoiceHeader() {
        return String.format("%s (%s)",
                getLocalisedString("PYYNTILUPAMAKSUN MAKSAMINEN", "BETALNING AV LICENSAVGIFT"),
                speciesName.getTranslation(locale).toUpperCase());
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
    public String getInvoiceDateString() {
        final LocalDate date;

        switch (resultType) {
            case INVOICE:
                date = invoiceDate;
                break;
            case RECEIPT:
                date = paymentDate;
                break;
            case REMINDER:
                date = today();
                break;
            default:
                throw new IllegalArgumentException("invalid resultType");
        }

        return INVOICE_DATE_PATTERN.print(date);
    }

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
                return dueDate != null ? DUE_DATE_PATTERN.print(dueDate) : "";
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
        final StringBuilder sb = new StringBuilder()
                .append(InvoicePdfConstants.INFO_LINE_1.getTranslation(locale))
                .append("\n\n")
                .append(InvoicePdfConstants.INFO_LINE_2.getTranslation(locale));

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
        return InvoiceUtil.createBarCodeMessage(
                paymentAmount != null ? paymentAmount.getEuros() : 0,
                paymentAmount != null ? paymentAmount.getCents() : 0,
                invoiceReference,
                invoiceAccountDetails.getIban(),
                dueDate);
    }
}
