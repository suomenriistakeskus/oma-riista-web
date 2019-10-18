package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.violation.AmendmentPermitMatchHarvest;
import fi.riista.feature.harvestpermit.violation.PermitRestrictionViolationChecker;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class MooselikePermitListDTO {
    public static MooselikePermitListDTO.Builder builder(final HarvestPermitSpeciesAmount speciesAmount) {
        return new MooselikePermitListDTO.Builder(speciesAmount);
    }

    private static OrganisationNameDTO createContactPersonAsPermitHolder(final @Nonnull Person contactPerson) {
        Objects.requireNonNull(contactPerson);

        final OrganisationNameDTO dto = new OrganisationNameDTO();
        dto.setNameFI(contactPerson.getFullName());
        dto.setNameSV(contactPerson.getFullName());
        return dto;
    }

    public static class Builder {
        private final HarvestPermit harvestPermit;
        private final GameSpecies gameSpecies;
        private final HarvestPermitSpeciesAmount speciesAmount;

        private String permitAreaExternalId;
        private boolean listLeadersButtonVisible;

        private Collection<HarvestCountDTO> harvestCounts;
        private double allocatedCount;
        private Map<String, Float> amendmentPermits;
        private ClubHuntingSummaryBasicInfoDTO huntingSummary;

        private Long viewedClubId;

        private Builder(final @Nonnull HarvestPermitSpeciesAmount speciesAmount) {
            this.speciesAmount = requireNonNull(speciesAmount);
            this.harvestPermit = requireNonNull(speciesAmount.getHarvestPermit());
            this.gameSpecies = requireNonNull(speciesAmount.getGameSpecies());
        }

        public Builder withPermitAreaExternalId(final String permitAreaExternalId) {
            this.permitAreaExternalId = permitAreaExternalId;
            return this;
        }

        public Builder withAmendmentPermits(Map<String, Float> amendmentPermits) {
            this.amendmentPermits = amendmentPermits;
            return this;
        }

        public Builder withViewedClubId(Long viewedClubId) {
            this.viewedClubId = viewedClubId;
            return this;
        }

        public Builder withListLeadersButtonVisible(boolean listLeadersButtonVisible) {
            this.listLeadersButtonVisible = listLeadersButtonVisible;
            return this;
        }

        public Builder withClubHuntingSummary(ClubHuntingSummaryBasicInfoDTO dto) {
            this.huntingSummary = dto;
            return this;
        }

        public Builder withHarvestCounts(Collection<HarvestCountDTO> harvests) {
            this.harvestCounts = harvests;
            return this;
        }

        public Builder withAllocatedCount(double allocatedCount) {
            this.allocatedCount = allocatedCount;
            return this;
        }

        private OrganisationNameDTO getPermitHolder() {
            return harvestPermit.getHuntingClub() != null
                    ? OrganisationNameDTO.create(harvestPermit.getHuntingClub())
                    : createContactPersonAsPermitHolder(harvestPermit.getOriginalContactPerson());
        }

        private boolean getPermitHolderFinishedHunting() {
            return speciesAmount.isMooselikeHuntingFinished();
        }

        private boolean getPermitPartnerFinishedHunting() {
            return huntingSummary != null && huntingSummary.isHuntingFinished();
        }

        private boolean getHuntingFinishedByModeration() {
            return huntingSummary != null && huntingSummary.isHuntingFinishedByModeration() || speciesAmount.isHuntingFinishedByModerator();
        }

        private double getPermitAmount() {
            return speciesAmount.getAmount();
        }

        private double getAmendmentAmount() {
            return NumberUtils.sum(amendmentPermits.values(), Float::doubleValue);
        }

        private double getUsedCount() {
            return NumberUtils.sum(harvestCounts, HarvestCountDTO::getRequiredPermitAmount);
        }

        private double getRequiredAmendmentPermits() {
            return NumberUtils.sum(harvestCounts, HarvestCountDTO::getRequiredAmendmentPermits);
        }

        private boolean isAmendmentPermitsMatchHarvests() {
            return AmendmentPermitMatchHarvest.countMatches(harvestCounts, amendmentPermits);
        }

        private boolean isRestrictionViolated() {
            return new PermitRestrictionViolationChecker(speciesAmount, harvestCounts).isRestrictionViolated();
        }

        private boolean isViewedClubPartner() {
            return F.getUniqueIds(harvestPermit.getPermitPartners()).contains(viewedClubId);
        }

        public MooselikePermitListDTO build() {
            Objects.requireNonNull(harvestPermit);
            Objects.requireNonNull(speciesAmount);
            Objects.requireNonNull(amendmentPermits);
            Objects.requireNonNull(harvestCounts);

            return new MooselikePermitListDTO(this);
        }
    }

    private final long id;
    private final String permitNumber;
    private final String permitType;
    private final int gameSpeciesCode;

    private final Long viewedClubId;
    private final boolean viewedClubIsPartner;

    // This is set only on those views where resolving this is relevant
    private boolean currentlyViewedRhyIsRelated;

    private final double permitAmount;
    private final double amendmentAmount;
    private final double totalAmount;
    private final double harvestedAmount;
    private final double requiredAmendmentAmount;
    private final double allocatedAmount;

    private final OrganisationNameDTO permitHolder;
    private final HarvestPermitSpeciesAmountDTO speciesAmount;
    private String permitAreaExternalId;

    private final Set<String> amendmentPermits;
    private final boolean amendmentPermitsMatchHarvests;
    private final boolean restrictionViolated;

    private final boolean permitHolderFinishedHunting;
    private final boolean permitPartnerFinishedHunting;
    private final boolean huntingFinishedByModeration;

    private final boolean listLeadersButtonVisible;

    private MooselikePermitListDTO(final Builder builder) {
        this.id = builder.harvestPermit.getId();
        this.permitNumber = builder.harvestPermit.getPermitNumber();
        this.permitType = builder.harvestPermit.getPermitType();
        this.gameSpeciesCode = builder.gameSpecies.getOfficialCode();

        this.viewedClubId = builder.viewedClubId;
        this.viewedClubIsPartner = builder.isViewedClubPartner();

        this.permitAmount = builder.getPermitAmount();
        this.amendmentAmount = builder.getAmendmentAmount();
        this.totalAmount = this.permitAmount + this.amendmentAmount;
        this.allocatedAmount = builder.allocatedCount;
        this.harvestedAmount = builder.getUsedCount();
        this.requiredAmendmentAmount = builder.getRequiredAmendmentPermits();

        this.permitHolder = builder.getPermitHolder();
        this.speciesAmount = HarvestPermitSpeciesAmountDTO.create(builder.speciesAmount);
        this.permitAreaExternalId = builder.permitAreaExternalId;

        this.amendmentPermits = builder.amendmentPermits.keySet();
        this.amendmentPermitsMatchHarvests = builder.isAmendmentPermitsMatchHarvests();
        this.restrictionViolated = builder.isRestrictionViolated();

        this.permitPartnerFinishedHunting = builder.getPermitPartnerFinishedHunting();
        this.permitHolderFinishedHunting = builder.getPermitHolderFinishedHunting();
        this.huntingFinishedByModeration = builder.getHuntingFinishedByModeration();

        this.listLeadersButtonVisible = builder.listLeadersButtonVisible;
    }

    public long getId() {
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

    public void setPermitAreaExternalId(final String permitAreaExternalId) {
        this.permitAreaExternalId = permitAreaExternalId;
    }

    public OrganisationNameDTO getPermitHolder() {
        return permitHolder;
    }

    public HarvestPermitSpeciesAmountDTO getSpeciesAmount() {
        return speciesAmount;
    }

    public String getPermitAreaExternalId() {
        return permitAreaExternalId;
    }

    public Set<String> getAmendmentPermits() {
        return amendmentPermits;
    }

    public Long getViewedClubId() {
        return viewedClubId;
    }

    public boolean isViewedClubIsPartner() {
        return viewedClubIsPartner;
    }

    public boolean isAmendmentPermitsMatchHarvests() {
        return amendmentPermitsMatchHarvests;
    }

    public boolean getPermitHolderFinishedHunting() {
        return permitHolderFinishedHunting;
    }

    public boolean isListLeadersButtonVisible() {
        return listLeadersButtonVisible;
    }

    public boolean isPermitHolderFinishedHunting() {
        return permitHolderFinishedHunting;
    }

    public boolean isPermitPartnerFinishedHunting() {
        return permitPartnerFinishedHunting;
    }

    public boolean isHuntingFinishedByModeration() {
        return huntingFinishedByModeration;
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

    public double getAllocatedAmount() {
        return allocatedAmount;
    }

    public double getHarvestedAmount() {
        return harvestedAmount;
    }

    public double getRequiredAmendmentAmount() {
        return requiredAmendmentAmount;
    }

    public boolean isRestrictionViolated() {
        return restrictionViolated;
    }

    public boolean isCurrentlyViewedRhyIsRelated() {
        return currentlyViewedRhyIsRelated;
    }

    public void setCurrentlyViewedRhyIsRelated(boolean currentlyViewedRhyIsRelated) {
        this.currentlyViewedRhyIsRelated = currentlyViewedRhyIsRelated;
    }
}
