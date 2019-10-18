package fi.riista.feature.permit.invoice.harvest.excel;

import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.invoice.harvest.InvoicePaymentAmountsDTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static fi.riista.util.NumberUtils.nullableSum;
import static java.util.Objects.requireNonNull;

public class MooselikeHarvestPaymentSummaryDTO {

    private final OrganisationNameDTO organisation;

    private final HarvestCountDTO harvestCounts;

    private final InvoicePaymentAmountsDTO invoicePaymentAmounts;

    private final BigDecimal chargeableAmountBasedOnHarvestCount;

    public static MooselikeHarvestPaymentSummaryDTO createSummary(@Nonnull final Iterable<MooselikeHarvestPaymentSummaryDTO> iterable) {
        final HarvestCountDTO totalHarvestCount = HarvestCountDTO
                .createTotal(F.mapNonNullsToList(iterable, MooselikeHarvestPaymentSummaryDTO::getHarvestCounts));

        return new MooselikeHarvestPaymentSummaryDTO(
                new OrganisationNameDTO(),
                totalHarvestCount,
                InvoicePaymentAmountsDTO.sum(iterable, dto -> dto.invoicePaymentAmounts),
                nullableSum(iterable, MooselikeHarvestPaymentSummaryDTO::getChargeableAmountBasedOnHarvestCount));
    }

    public MooselikeHarvestPaymentSummaryDTO(@Nonnull final OrganisationNameDTO organisation,
                                             @Nonnull final HarvestCountDTO harvestCounts,
                                             @Nonnull final InvoicePaymentAmountsDTO paymentAmounts,
                                             @Nonnull final BigDecimal chargeableAmountBasedOnHarvestCount) {

        this.organisation = requireNonNull(organisation);
        this.harvestCounts = requireNonNull(harvestCounts);
        this.invoicePaymentAmounts = requireNonNull(paymentAmounts);
        this.chargeableAmountBasedOnHarvestCount = requireNonNull(chargeableAmountBasedOnHarvestCount);
    }

    public BigDecimal getReceivedAmount() {
        return invoicePaymentAmounts.getReceivedAmount();
    }

    public BigDecimal getReceicedAmountSubtractedByAmountBasedOnActualHarvestCount() {
        return invoicePaymentAmounts.getReceivedAmount().subtract(chargeableAmountBasedOnHarvestCount);
    }

    public BigDecimal getSurplusAmount() {
        return invoicePaymentAmounts.getSurplusAmount();
    }

    public BigDecimal getDeficientAmount() {
        return invoicePaymentAmounts.getDeficientAmount();
    }

    // Accessors -->

    public OrganisationNameDTO getOrganisation() {
        return organisation;
    }

    public HarvestCountDTO getHarvestCounts() {
        return harvestCounts;
    }

    public BigDecimal getChargeableAmountBasedOnHarvestCount() {
        return chargeableAmountBasedOnHarvestCount;
    }
}
