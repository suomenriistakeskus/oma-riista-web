package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.huntingclub.permit.allocation.HuntingClubPermitAllocationDTO;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReport;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReportDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.summingDouble;

public class MooselikePermitListingDTO {

    MooselikePermitListingDTO(@Nonnull final HarvestPermit permit,
                              @Nonnull final HarvestPermitSpeciesAmount spa,
                              @Nonnull final GameSpecies species,
                              @Nonnull final Map<String, Float> amendmentPermits,
                              @Nullable final Long viewedClubId,
                              final boolean viewedClubIsPartner,
                              final boolean canEditAllocations,
                              final boolean hasPermissionToCreateOrRemove,
                              final boolean allPartnersFinishedHunting,
                              final boolean amendmentPermitsMatchHarvests,
                              final MooseHarvestReport mooseHarvestReport,
                              final boolean listLeadersButtonVisible,
                              final boolean huntingFinished,
                              final boolean huntingFinishedByModeration,
                              @Nonnull final Collection<HuntingClubPermitCountDTO> harvests,
                              @Nonnull final List<HuntingClubPermitAllocationDTO> allocations,
                              @Nonnull final HuntingClubPermitTotalPaymentDTO totalPayment) {

        Objects.requireNonNull(permit);
        Objects.requireNonNull(spa);
        Objects.requireNonNull(species);
        Objects.requireNonNull(amendmentPermits);
        Objects.requireNonNull(harvests);
        Objects.requireNonNull(allocations);
        Objects.requireNonNull(totalPayment);

        this.id = permit.getId();
        this.permitNumber = permit.getPermitNumber();
        this.permitHolder = OrganisationNameDTO.create(permit.getPermitHolder());
        this.speciesAmount = HarvestPermitSpeciesAmountDTO.create(spa, species);
        this.amendmentPermits = amendmentPermits;

        this.viewedClubId = viewedClubId;
        this.viewedClubIsPartner = viewedClubIsPartner;

        this.canEditAllocations = canEditAllocations;
        this.hasPermissionToCreateOrRemove = hasPermissionToCreateOrRemove;

        this.allPartnersFinishedHunting = allPartnersFinishedHunting;
        this.amendmentPermitsMatchHarvests = amendmentPermitsMatchHarvests;
        this.mooseHarvestReport = MooseHarvestReportDTO.create(mooseHarvestReport);

        this.listLeadersButtonVisible = listLeadersButtonVisible;
        this.huntingFinished = huntingFinished;
        this.huntingFinishedByModeration = huntingFinishedByModeration;

        double total = spa.getAmount() + F.sum(amendmentPermits.values(), Float::doubleValue);
        double unallocated = total - allocations.stream()
                .map(a -> a.getAdultMales() + a.getAdultFemales() + a.getYoung() / 2.0)
                .collect(summingDouble(v -> v));

        this.total = total;
        this.unallocated = unallocated;
        this.used = harvests.stream()
                .map(h1 -> h1.countAdults() + h1.countYoung() / 2.0)
                .collect(summingDouble(v1 -> v1));
        this.notEdible = harvests.stream()
                .map(h -> h.getNumberOfNonEdibleAdults() + h.getNumberOfNonEdibleYoungs() / 2.0)
                .collect(summingDouble(v -> v));

        this.totalPayment = totalPayment;
    }

    private long id;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;

    private final OrganisationNameDTO permitHolder;

    @DoNotValidate
    private HarvestPermitSpeciesAmountDTO speciesAmount;

    private Map<String, Float> amendmentPermits;

    private Long viewedClubId;
    private boolean viewedClubIsPartner;

    private boolean canEditAllocations;
    private boolean hasPermissionToCreateOrRemove;

    private boolean allPartnersFinishedHunting;
    private boolean amendmentPermitsMatchHarvests;
    private MooseHarvestReportDTO mooseHarvestReport;
    private boolean listLeadersButtonVisible;
    private boolean huntingFinished;
    private boolean huntingFinishedByModeration;

    private double total;
    private double unallocated;
    private double used;
    private double notEdible;

    private HuntingClubPermitTotalPaymentDTO totalPayment;

    // This is set only on those views where resolving this is relevant
    private boolean currentlyViewedRhyIsRelated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public OrganisationNameDTO getPermitHolder() {
        return permitHolder;
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

    public boolean isViewedClubIsPartner() {
        return viewedClubIsPartner;
    }

    public void setViewedClubIsPartner(boolean viewedClubIsPartner) {
        this.viewedClubIsPartner = viewedClubIsPartner;
    }

    public boolean isCanEditAllocations() {
        return canEditAllocations;
    }

    public void setCanEditAllocations(boolean canEditAllocations) {
        this.canEditAllocations = canEditAllocations;
    }

    public boolean isHasPermissionToCreateOrRemove() {
        return hasPermissionToCreateOrRemove;
    }

    public void setHasPermissionToCreateOrRemove(boolean hasPermissionToCreateOrRemove) {
        this.hasPermissionToCreateOrRemove = hasPermissionToCreateOrRemove;
    }

    public boolean isAllPartnersFinishedHunting() {
        return allPartnersFinishedHunting;
    }

    public void setAllPartnersFinishedHunting(boolean allPartnersFinishedHunting) {
        this.allPartnersFinishedHunting = allPartnersFinishedHunting;
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

    public boolean isListLeadersButtonVisible() {
        return listLeadersButtonVisible;
    }

    public void setListLeadersButtonVisible(final boolean listLeadersButtonVisible) {
        this.listLeadersButtonVisible = listLeadersButtonVisible;
    }

    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    public void setHuntingFinished(boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
    }

    public boolean isHuntingFinishedByModeration() {
        return huntingFinishedByModeration;
    }

    public void setHuntingFinishedByModeration(boolean huntingFinishedByModeration) {
        this.huntingFinishedByModeration = huntingFinishedByModeration;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getUnallocated() {
        return unallocated;
    }

    public void setUnallocated(double unallocated) {
        this.unallocated = unallocated;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }

    public double getNotEdible() {
        return notEdible;
    }

    public void setNotEdible(double notEdible) {
        this.notEdible = notEdible;
    }

    public HuntingClubPermitTotalPaymentDTO getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(HuntingClubPermitTotalPaymentDTO totalPayment) {
        this.totalPayment = totalPayment;
    }

    public boolean isCurrentlyViewedRhyIsRelated() {
        return currentlyViewedRhyIsRelated;
    }

    public void setCurrentlyViewedRhyIsRelated(boolean currentlyViewedRhyIsRelated) {
        this.currentlyViewedRhyIsRelated = currentlyViewedRhyIsRelated;
    }
}
