package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportDTO;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.util.DtoUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HuntingClubPermitDTOBuilder {
    private HuntingClubPermitDTO dto;

    private HuntingClubPermitDTOBuilder(@Nonnull final HarvestPermit permit) {
        this.dto = new HuntingClubPermitDTO();
        DtoUtil.copyBaseFields(permit, dto);
        dto.setPermitNumber(permit.getPermitNumber());
        dto.setPermitType(permit.getPermitType());
    }

    public static HuntingClubPermitDTOBuilder builder(@Nonnull final HarvestPermit permit) {
        Objects.requireNonNull(permit, "permit must not be null");

        return new HuntingClubPermitDTOBuilder(permit);
    }

    public HuntingClubPermitDTOBuilder withSpeciesAmount(HarvestPermitSpeciesAmountDTO speciesAmount) {
        dto.setSpeciesAmount(speciesAmount);
        return this;
    }

    public HuntingClubPermitDTOBuilder withAmendmentPermits(Map<String, Float> amendmentPermits) {
        dto.setAmendmentPermits(amendmentPermits);
        return this;
    }

    public HuntingClubPermitDTOBuilder withViewedClubId(Long viewedClubId) {
        dto.setViewedClubId(viewedClubId);
        return this;
    }

    public HuntingClubPermitDTOBuilder withAllocations(List<MoosePermitAllocationDTO> allocations) {
        dto.setAllocations(allocations);
        return this;
    }

    public HuntingClubPermitDTOBuilder withHarvestCounts(Map<Long, HuntingClubPermitCountDTO> harvestCounts) {
        dto.setHarvestCounts(harvestCounts);
        return this;
    }

    public HuntingClubPermitDTOBuilder withTotalStatistics(HuntingClubPermitHuntingDayStatisticsDTO totalStatistics) {
        dto.setTotalStatistics(totalStatistics);
        return this;
    }

    public HuntingClubPermitDTOBuilder withStatistics(Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> statistics) {
        dto.setStatistics(statistics);
        return this;
    }

    public HuntingClubPermitDTOBuilder withSummaryForPartnersTable(Map<Long, ClubHuntingSummaryBasicInfoDTO> summaryForPartnersTable) {
        dto.setSummaryForPartnersTable(summaryForPartnersTable);
        return this;
    }

    public HuntingClubPermitDTOBuilder withCanModifyEndOfHunting(boolean canModifyEndOfHunting) {
        dto.setCanModifyEndOfHunting(canModifyEndOfHunting);
        return this;
    }

    public HuntingClubPermitDTOBuilder withAllPartnersFinishedHunting(boolean allPartnersFinishedHunting) {
        dto.setAllPartnersFinishedHunting(allPartnersFinishedHunting);
        return this;
    }

    public HuntingClubPermitDTOBuilder withAmendmentPermitsMatchHarvests(boolean amendmentPermitsMatchHarvests) {
        dto.setAmendmentPermitsMatchHarvests(amendmentPermitsMatchHarvests);
        return this;
    }

    public HuntingClubPermitDTOBuilder withTotalPayment(HuntingClubPermitTotalPaymentDTO totalPayment) {
        dto.setTotalPayment(totalPayment);
        return this;
    }

    public HuntingClubPermitDTOBuilder withPayments(Map<Long, HuntingClubPermitPaymentDTO> payments) {
        dto.setPayments(payments);
        return this;
    }

    public HuntingClubPermitDTOBuilder withMooseHarvestReport(MooseHarvestReportDTO mooseHarvestReport) {
        dto.setMooseHarvestReport(mooseHarvestReport);
        return this;
    }

    public HuntingClubPermitDTO build() {
        Objects.requireNonNull(dto.getSpeciesAmount(), "speciesAmount must not be null");
        Objects.requireNonNull(dto.getAmendmentPermits(), "amendmentPermits must not be null");
        Objects.requireNonNull(dto.getAllocations(), "allocations must not be null");
        Objects.requireNonNull(dto.getHarvestCounts(), "harvestCounts must not be null");
        Objects.requireNonNull(dto.getStatistics(), "statistics must not be null");
        Objects.requireNonNull(dto.getSummaryForPartnersTable(), "summaryForPartnersTable must not be null");

        return dto;
    }
}
