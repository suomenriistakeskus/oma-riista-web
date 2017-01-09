package fi.riista.feature.huntingclub.permit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.permit.allocation.HuntingClubPermitAllocationDTO;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportDTO;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.util.DtoUtil;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HuntingClubPermitDTO extends BaseEntityDTO<Long> {

    @Nonnull
    public static HuntingClubPermitDTO create(@Nonnull final HarvestPermit permit,
                                              @Nonnull final GameSpecies species,
                                              @Nonnull final HarvestPermitSpeciesAmount speciesAmount,
                                              @Nonnull final Map<String, Float> amendmentPermits,
                                              @Nullable final Long viewedClubId,
                                              @Nonnull final List<HuntingClubPermitAllocationDTO> allocations,
                                              final boolean canEditAllocations,
                                              @Nonnull final Map<Long, HuntingClubPermitCountDTO> harvestCounts,
                                              @Nonnull final HuntingClubPermitHuntingDayStatisticsDTO totalStats,
                                              @Nonnull final Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> stats,
                                              @Nonnull
                                              final Map<Long, ClubHuntingSummaryBasicInfoDTO> summaryForPartnersTable,
                                              @Nonnull final HuntingClubPermitTotalPaymentDTO totalPayment,
                                              @Nonnull final Map<Long, HuntingClubPermitPaymentDTO> payments,
                                              final boolean amendmentPermitsMatchHarvests,
                                              @Nullable final MooseHarvestReportDTO finishedDto) {

        Objects.requireNonNull(permit, "permit must not be null");
        Objects.requireNonNull(speciesAmount, "speciesAmount must not be null");
        Objects.requireNonNull(amendmentPermits, "amendmentPermits must not be null");
        Objects.requireNonNull(allocations, "allocations must not be null");
        Objects.requireNonNull(harvestCounts, "harvestCounts must not be null");
        Objects.requireNonNull(stats, "stats must not be null");
        Objects.requireNonNull(summaryForPartnersTable, "summaryForPartnersTable must not be null");

        final HuntingClubPermitDTO dto = new HuntingClubPermitDTO();
        DtoUtil.copyBaseFields(permit, dto);

        dto.setPermitNumber(permit.getPermitNumber());
        dto.setPermitType(permit.getPermitType());
        dto.setSpeciesAmount(HarvestPermitSpeciesAmountDTO.create(speciesAmount, species));
        dto.setAmendmentPermits(amendmentPermits);
        dto.setViewedClubId(viewedClubId);
        dto.setAllocations(allocations);
        dto.setCanEditAllocations(canEditAllocations);
        dto.setHarvestCounts(harvestCounts);
        dto.setTotalStatistics(totalStats);
        dto.setStatistics(stats);
        dto.setSummaryForPartnersTable(summaryForPartnersTable);

        dto.setAmendmentPermitsMatchHarvests(amendmentPermitsMatchHarvests);
        dto.setTotalPayment(totalPayment);
        dto.setPayments(payments);

        dto.setMooseHarvestReport(finishedDto);
        return dto;
    }

    private Long id;
    private Integer rev;

    @FinnishHuntingPermitNumber
    private String permitNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    private HarvestPermitSpeciesAmountDTO speciesAmount;

    private Map<String, Float> amendmentPermits;

    private Long viewedClubId;

    private List<HuntingClubPermitAllocationDTO> allocations;

    private boolean canEditAllocations;

    private Map<Long, HuntingClubPermitCountDTO> harvestCounts;

    private HuntingClubPermitHuntingDayStatisticsDTO totalStatistics;
    private Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> statistics;

    @JsonInclude(Include.ALWAYS)
    private Map<Long, ClubHuntingSummaryBasicInfoDTO> summaryForPartnersTable;

    private boolean amendmentPermitsMatchHarvests;
    private HuntingClubPermitTotalPaymentDTO totalPayment;
    private Map<Long, HuntingClubPermitPaymentDTO> payments;

    private MooseHarvestReportDTO mooseHarvestReport;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public HarvestPermitSpeciesAmountDTO getSpeciesAmount() {
        return speciesAmount;
    }

    public void setSpeciesAmount(HarvestPermitSpeciesAmountDTO speciesAmount) {
        this.speciesAmount = speciesAmount;
    }

    public Map<String, Float> getAmendmentPermits() {
        return amendmentPermits;
    }

    public void setAmendmentPermits(Map<String, Float> amendmentPermits) {
        this.amendmentPermits = amendmentPermits;
    }

    public Long getViewedClubId() {
        return viewedClubId;
    }

    public void setViewedClubId(Long viewedClubId) {
        this.viewedClubId = viewedClubId;
    }

    public List<HuntingClubPermitAllocationDTO> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<HuntingClubPermitAllocationDTO> allocations) {
        this.allocations = allocations;
    }

    public boolean isCanEditAllocations() {
        return canEditAllocations;
    }

    public void setCanEditAllocations(boolean canEditAllocations) {
        this.canEditAllocations = canEditAllocations;
    }

    public Map<Long, HuntingClubPermitCountDTO> getHarvestCounts() {
        return harvestCounts;
    }

    public void setHarvestCounts(Map<Long, HuntingClubPermitCountDTO> harvestCounts) {
        this.harvestCounts = harvestCounts;
    }

    public HuntingClubPermitHuntingDayStatisticsDTO getTotalStatistics() {
        return totalStatistics;
    }

    public void setTotalStatistics(HuntingClubPermitHuntingDayStatisticsDTO totalStatistics) {
        this.totalStatistics = totalStatistics;
    }

    public Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> statistics) {
        this.statistics = statistics;
    }

    public Map<Long, ClubHuntingSummaryBasicInfoDTO> getSummaryForPartnersTable() {
        return summaryForPartnersTable;
    }

    public void setSummaryForPartnersTable(Map<Long, ClubHuntingSummaryBasicInfoDTO> summaryForPartnersTable) {
        this.summaryForPartnersTable = summaryForPartnersTable;
    }

    public HuntingClubPermitTotalPaymentDTO getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(HuntingClubPermitTotalPaymentDTO totalPayment) {
        this.totalPayment = totalPayment;
    }

    public Map<Long, HuntingClubPermitPaymentDTO> getPayments() {
        return payments;
    }

    public void setPayments(Map<Long, HuntingClubPermitPaymentDTO> payments) {
        this.payments = payments;
    }

    public boolean isAmendmentPermitsMatchHarvests() {
        return amendmentPermitsMatchHarvests;
    }

    public void setAmendmentPermitsMatchHarvests(boolean amendmentPermitsMatchHarvests) {
        this.amendmentPermitsMatchHarvests = amendmentPermitsMatchHarvests;
    }

    public MooseHarvestReportDTO getMooseHarvestReport() {
        return mooseHarvestReport;
    }

    public void setMooseHarvestReport(MooseHarvestReportDTO mooseHarvestReport) {
        this.mooseHarvestReport = mooseHarvestReport;
    }
}
