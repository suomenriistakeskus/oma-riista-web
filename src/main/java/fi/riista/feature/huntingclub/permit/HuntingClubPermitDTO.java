package fi.riista.feature.huntingclub.permit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportDTO;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;

import java.util.List;
import java.util.Map;

public class HuntingClubPermitDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    @FinnishHuntingPermitNumber
    private String permitNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;

    private HarvestPermitSpeciesAmountDTO speciesAmount;

    private Map<String, Float> amendmentPermits;

    private Long viewedClubId;

    private List<MoosePermitAllocationDTO> allocations;

    private Map<Long, HuntingClubPermitCountDTO> harvestCounts;

    private HuntingClubPermitHuntingDayStatisticsDTO totalStatistics;
    private Map<Long, HuntingClubPermitHuntingDayStatisticsDTO> statistics;

    @JsonInclude(Include.ALWAYS)
    private Map<Long, ClubHuntingSummaryBasicInfoDTO> summaryForPartnersTable;

    private boolean allPartnersFinishedHunting;
    private boolean canModifyEndOfHunting;
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

    public List<MoosePermitAllocationDTO> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<MoosePermitAllocationDTO> allocations) {
        this.allocations = allocations;
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

    public boolean isAllPartnersFinishedHunting() {
        return allPartnersFinishedHunting;
    }

    public void setAllPartnersFinishedHunting(final boolean allPartnersFinishedHunting) {
        this.allPartnersFinishedHunting = allPartnersFinishedHunting;
    }

    public boolean isCanModifyEndOfHunting() {
        return canModifyEndOfHunting;
    }

    public void setCanModifyEndOfHunting(final boolean canModifyEndOfHunting) {
        this.canModifyEndOfHunting = canModifyEndOfHunting;
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
