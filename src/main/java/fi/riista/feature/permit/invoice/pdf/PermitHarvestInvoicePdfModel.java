package fi.riista.feature.permit.invoice.pdf;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.CreditorReferenceCalculator;
import fi.riista.feature.permit.invoice.InvoiceAccountDetails;
import fi.riista.feature.permit.invoice.InvoiceRecipient;
import fi.riista.feature.permit.invoice.PermitHarvestInvoicePrice;
import fi.riista.util.InvoiceUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class PermitHarvestInvoicePdfModel {

    private static final PermitHarvestInvoicePrice MOOSE_PRICE =
            new PermitHarvestInvoicePrice(new BigDecimal("120.00"), new BigDecimal("50.00"));

    private static final PermitHarvestInvoicePrice DEER_PRICE =
            new PermitHarvestInvoicePrice(new BigDecimal("17.00"), new BigDecimal("8.00"));

    private static final LocalisedString HARVEST_INVOICE_FILENAME_PREFIX =
            new LocalisedString("Pyyntilupamaksu", "Licensavgift");

    private static final DateTimeFormatter INVOICE_DATE_PATTERN = DateTimeFormat.forPattern("d.M.yyyy");

    public static PermitHarvestInvoicePdfModel create(final @Nonnull PermitDecision decision,
                                                      final @Nonnull InvoiceAccountDetails invoiceAccountDetails,
                                                      final @Nonnull GameSpecies gameSpecies) {
        requireNonNull(decision);
        requireNonNull(invoiceAccountDetails);
        requireNonNull(gameSpecies);

        final LocalDate invoiceDate = decision.getPublishDate().toLocalDate();
        final PermitHarvestInvoicePrice specimenPrice = gameSpecies.isMoose() ? MOOSE_PRICE : DEER_PRICE;

        final HarvestPermitApplication application = decision.getApplication();
        final int applicationNumber = application.getApplicationNumber();
        final CreditorReference invoiceReference = CreditorReferenceCalculator.computeReferenceForPermitHarvestInvoice(
                application.getHuntingYear(), applicationNumber, gameSpecies.getOfficialCode());

        return new PermitHarvestInvoicePdfModel(decision.getLocale(), application.getPermitNumber(), specimenPrice,
                gameSpecies.getNameLocalisation(), invoiceAccountDetails, invoiceReference,
                InvoiceRecipient.create(decision), invoiceDate);
    }

    private final Locale locale;
    private final String permitNumber;
    private final PermitHarvestInvoicePrice specimenPrice;
    private final LocalisedString speciesName;
    private final InvoiceAccountDetails invoiceAccountDetails;
    private final InvoiceRecipient invoiceRecipient;
    private final CreditorReference invoiceReference;
    private final LocalDate invoiceDate;

    PermitHarvestInvoicePdfModel(final Locale locale,
                                 final String permitNumber,
                                 final PermitHarvestInvoicePrice specimenPrice,
                                 final LocalisedString speciesName,
                                 final InvoiceAccountDetails invoiceAccountDetails,
                                 final CreditorReference invoiceReference,
                                 final InvoiceRecipient invoiceRecipient,
                                 final LocalDate invoiceDate) {
        this.locale = requireNonNull(locale);
        this.permitNumber = requireNonNull(permitNumber);
        this.specimenPrice = requireNonNull(specimenPrice);
        this.speciesName = requireNonNull(speciesName);
        this.invoiceAccountDetails = requireNonNull(invoiceAccountDetails);
        this.invoiceReference = requireNonNull(invoiceReference);
        this.invoiceDate = requireNonNull(invoiceDate);
        this.invoiceRecipient = requireNonNull(invoiceRecipient);

        Preconditions.checkArgument(this.invoiceReference.isValid());
    }

    @Nonnull
    public String getPdfFileName() {
        return String.format("%s-%s-%s.pdf",
                HARVEST_INVOICE_FILENAME_PREFIX.getTranslation(locale),
                permitNumber,
                speciesName.getTranslation(locale));
    }

    @Nonnull
    public String getLocalisedString(final String finnish, final String swedish) {
        return new LocalisedString(finnish, swedish).getTranslation(locale);
    }

    @Nonnull
    public String getAmountText() {
        return "";
    }

    @Nonnull
    public InvoiceAccountDetails getInvoiceAccountDetails() {
        return invoiceAccountDetails;
    }

    @Nonnull
    public PermitHarvestInvoicePrice getSpecimenPrice() {
        return specimenPrice;
    }

    @Nonnull
    public String getPaymentRecipient() {
        return "MMM/Hirvieläinten pyyntilupamaksujen keräilytili";
    }

    @Nonnull
    public InvoiceRecipient getInvoiceRecipient() {
        return invoiceRecipient;
    }

    @Nonnull
    public String getInvoiceTitle() {
        return String.format("%s (%s)",
                getLocalisedString("PYYNTILUPAMAKSUN MAKSAMINEN", "BETALNING AV LICENSAVGIFT"),
                speciesName.getTranslation(locale).toUpperCase());
    }

    @Nonnull
    public String getInvoiceDateString() {
        return INVOICE_DATE_PATTERN.print(invoiceDate);
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
        return InvoiceUtil.createBarCodeMessage(0, 0, invoiceReference, invoiceAccountDetails.getIban(), dueDate);
    }
}
