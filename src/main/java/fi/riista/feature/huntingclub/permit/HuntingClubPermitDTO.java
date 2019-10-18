package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.payment.HuntingClubPermitPaymentDTO;
import fi.riista.feature.harvestpermit.payment.HuntingClubPermitPriceBreakdownDTO;
import fi.riista.feature.harvestpermit.payment.HuntingClubPermitTotalPaymentDTO;
import fi.riista.feature.harvestpermit.violation.AmendmentPermitMatchHarvest;
import fi.riista.feature.harvestpermit.violation.PermitRestrictionViolationChecker;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingEndStatus;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerAreaDTO;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerDTO;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HuntingDayStatisticsDTO;
import fi.riista.feature.huntingclub.permit.todo.MoosePermitTodoDTO;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class HuntingClubPermitDTO {
    public static Builder builder(final HarvestPermitSpeciesAmount speciesAmount) {
        return new Builder(speciesAmount);
    }

    public static class Builder {
        private HarvestPermit harvestPermit;
        private GameSpecies gameSpecies;
        private HarvestPermitSpeciesAmount speciesAmount;

        private Long viewedClubId;
        private Boolean permitHolderFinishedHunting;
        private Boolean huntingFinishedByModeration;
        private Boolean canModifyEndOfHunting;

        private Map<String, Float> amendmentPermits;

        private List<MoosePermitAllocationDTO> allocations;

        private Map<Long, HuntingClubPermitPaymentDTO> payments;
        private HuntingClubPermitTotalPaymentDTO totalPayment;

        private Map<Long, HuntingDayStatisticsDTO> huntingDayStatistics;
        private HuntingDayStatisticsDTO totalHuntingDayStatistics;

        private Map<Long, HarvestCountDTO> harvestCounts;
        private Map<Long, ClubHuntingSummaryBasicInfoDTO> summary;
        private Map<Long, MoosePermitTodoDTO> todo;
        private Map<Long, List<HarvestPermitPartnerAreaDTO>> partnerAreas;

        private Builder(final @Nonnull HarvestPermitSpeciesAmount speciesAmount) {
            this.speciesAmount = requireNonNull(speciesAmount);
            this.harvestPermit = requireNonNull(speciesAmount.getHarvestPermit());
            this.gameSpecies = requireNonNull(speciesAmount.getGameSpecies());
        }

        public Builder withCanModifyEndOfHunting(boolean canModifyEndOfHunting) {
            this.canModifyEndOfHunting = canModifyEndOfHunting;
            return this;
        }

        public Builder withViewedClubId(Long viewedClubId) {
            this.viewedClubId = viewedClubId;
            return this;
        }

        public Builder withHuntingFinishedByModeration(boolean huntingFinishedByModeration) {
            this.huntingFinishedByModeration = huntingFinishedByModeration;
            return this;
        }

        public Builder withPermitHolderFinishedHunting(boolean permitHolderFinishedHunting) {
            this.permitHolderFinishedHunting = permitHolderFinishedHunting;
            return this;
        }

        public Builder withAmendmentPermits(Map<String, Float> amendmentPermits) {
            this.amendmentPermits = requireNonNull(amendmentPermits);
            return this;
        }

        public Builder withAllocations(List<MoosePermitAllocationDTO> allocations) {
            this.allocations = requireNonNull(allocations);
            return this;
        }

        public Builder withTodo(Map<Long, MoosePermitTodoDTO> todo) {
            this.todo = requireNonNull(todo);
            return this;
        }

        public Builder withHarvestCounts(Map<Long, HarvestCountDTO> harvestCounts) {
            this.harvestCounts = requireNonNull(harvestCounts);
            return this;
        }

        public Builder withHuntingDayStatistics(Map<Long, HuntingDayStatisticsDTO> huntingDayStatistics) {
            this.huntingDayStatistics = requireNonNull(huntingDayStatistics);
            this.totalHuntingDayStatistics = HuntingDayStatisticsDTO.calculateTotal(huntingDayStatistics.values());
            return this;
        }

        public Builder withSummary(Map<Long, ClubHuntingSummaryBasicInfoDTO> summary) {
            this.summary = requireNonNull(summary);
            return this;
        }

        public Builder withPaymentBreakdown(HuntingClubPermitPriceBreakdownDTO dto) {
            this.payments = requireNonNull(dto.getPayments());
            this.totalPayment = requireNonNull(dto.getTotalPayment());
            return this;
        }

        public Builder withPartnerAreas(final Map<Long, List<HarvestPermitPartnerAreaDTO>> partnerAreas) {
            this.partnerAreas = requireNonNull(partnerAreas);
            return this;
        }

        private List<HarvestPermitPartnerDTO> createPartners() {
            return F.mapNonNullsToList(allocations, allocation -> {
                final long clubId = allocation.getHuntingClubId();

                return new HarvestPermitPartnerDTO(clubId,
                        allocation.getHuntingClubName(),
                        allocation,
                        todo.get(clubId),
                        harvestCounts.get(clubId),
                        payments.get(clubId),
                        summary.get(clubId),
                        partnerAreas.get(clubId),
                        huntingDayStatistics.get(clubId));
            });
        }

        private double getPermitAmount() {
            return speciesAmount.getAmount();
        }

        private double getAmendmentAmount() {
            return NumberUtils.sum(amendmentPermits.values(), Float::doubleValue);
        }

        private double getUsedCount() {
            return NumberUtils.sum(harvestCounts.values(), HarvestCountDTO::getRequiredPermitAmount);
        }

        private double getRequiredAmendmentPermits() {
            return NumberUtils.sum(harvestCounts.values(), HarvestCountDTO::getRequiredAmendmentPermits);
        }

        private boolean isAmendmentPermitsMatchHarvest() {
            return AmendmentPermitMatchHarvest.countMatches(harvestCounts.values(), amendmentPermits);
        }

        private boolean isRestrictionViolated() {
            return new PermitRestrictionViolationChecker(speciesAmount, harvestCounts.values()).isRestrictionViolated();
        }

        private boolean getDidAllPartnersFinishedHunting() {
            return summary.values().stream().allMatch(HuntingEndStatus::isHuntingFinished);
        }

        public HuntingClubPermitDTO build() {
            return new HuntingClubPermitDTO(this);
        }
    }

    private final Long id;
    private final String permitNumber;
    private final String permitType;
    private final int gameSpeciesCode;

    private final Long viewedClubId;

    private final double permitAmount;
    private final double amendmentAmount;
    private final double totalAmount;
    private final double harvestedAmount;
    private final double requiredAmendmentAmount;

    private final HuntingDayStatisticsDTO totalStatistics;
    private final HuntingClubPermitTotalPaymentDTO totalPayment;
    private final List<HarvestPermitPartnerDTO> partners;

    private final boolean permitHolderFinishedHunting;
    private final boolean huntingFinishedByModeration;
    private final boolean allPartnersFinishedHunting;
    private final boolean canModifyEndOfHunting;
    private final boolean amendmentPermitsMatchHarvests;
    private final boolean restrictionViolated;

    private HuntingClubPermitDTO(final Builder builder) {
        this.id = builder.harvestPermit.getId();
        this.permitNumber = builder.harvestPermit.getPermitNumber();
        this.permitType = builder.harvestPermit.getPermitType();
        this.gameSpeciesCode = builder.gameSpecies.getOfficialCode();
        this.viewedClubId = builder.viewedClubId;

        this.permitAmount = builder.getPermitAmount();
        this.amendmentAmount = builder.getAmendmentAmount();
        this.totalAmount = this.permitAmount + this.amendmentAmount;
        this.harvestedAmount = builder.getUsedCount();
        this.requiredAmendmentAmount = builder.getRequiredAmendmentPermits();

        this.restrictionViolated = builder.isRestrictionViolated();
        this.amendmentPermitsMatchHarvests = builder.isAmendmentPermitsMatchHarvest();

        this.totalPayment = requireNonNull(builder.totalPayment);
        this.totalStatistics = requireNonNull(builder.totalHuntingDayStatistics);
        this.partners = builder.createPartners();

        this.allPartnersFinishedHunting = builder.getDidAllPartnersFinishedHunting();
        this.permitHolderFinishedHunting = requireNonNull(builder.permitHolderFinishedHunting);
        this.huntingFinishedByModeration = requireNonNull(builder.huntingFinishedByModeration);
        this.canModifyEndOfHunting = requireNonNull(builder.canModifyEndOfHunting);
    }

    public Long getId() {
        return id;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public double getPermitAmount() {
        return permitAmount;
    }

    public double getAmendmentAmount() {
        return amendmentAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getHarvestedAmount() {
        return harvestedAmount;
    }

    public double getRequiredAmendmentAmount() {
        return requiredAmendmentAmount;
    }

    public List<HarvestPermitPartnerDTO> getPartners() {
        return partners;
    }

    public Long getViewedClubId() {
        return viewedClubId;
    }

    public HuntingDayStatisticsDTO getTotalStatistics() {
        return totalStatistics;
    }

    public HuntingClubPermitTotalPaymentDTO getTotalPayment() {
        return totalPayment;
    }

    public boolean isPermitHolderFinishedHunting() {
        return permitHolderFinishedHunting;
    }

    public boolean isHuntingFinishedByModeration() {
        return huntingFinishedByModeration;
    }

    public boolean isAllPartnersFinishedHunting() {
        return allPartnersFinishedHunting;
    }

    public boolean isCanModifyEndOfHunting() {
        return canModifyEndOfHunting;
    }

    public boolean isAmendmentPermitsMatchHarvests() {
        return amendmentPermitsMatchHarvests;
    }

    public boolean isRestrictionViolated() {
        return restrictionViolated;
    }
}
