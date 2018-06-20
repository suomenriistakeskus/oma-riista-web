package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReport;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReportDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitCountDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.NumberUtils;
import fi.riista.validation.DoNotValidate;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MooselikePermitListDTO {

    private static OrganisationNameDTO createContactPersonAsPermitHolder(final @Nonnull Person contactPerson) {
        Objects.requireNonNull(contactPerson);

        final OrganisationNameDTO dto = new OrganisationNameDTO();
        dto.setNameFI(contactPerson.getFullName());
        dto.setNameSV(contactPerson.getFullName());
        return dto;
    }

    MooselikePermitListDTO(@Nonnull final HarvestPermit permit,
                           @Nonnull final HarvestPermitSpeciesAmount spa,
                           @Nonnull final GameSpecies species,
                           @Nonnull final Map<String, Float> amendmentPermits,
                           @Nullable final Long viewedClubId,
                           final boolean viewedClubIsPartner,
                           final boolean amendmentPermitsMatchHarvests,
                           final MooseHarvestReport mooseHarvestReport,
                           final boolean listLeadersButtonVisible,
                           final boolean huntingFinished,
                           final boolean huntingFinishedByModeration,
                           @Nonnull final Collection<HuntingClubPermitCountDTO> harvests,
                           @Nonnull final List<MoosePermitAllocationDTO> allocations) {

        Objects.requireNonNull(permit);
        Objects.requireNonNull(spa);
        Objects.requireNonNull(species);
        Objects.requireNonNull(amendmentPermits);
        Objects.requireNonNull(harvests);
        Objects.requireNonNull(allocations);

        this.id = permit.getId();
        this.permitNumber = permit.getPermitNumber();
        this.permitHolder = permit.getPermitHolder() != null
                ? OrganisationNameDTO.create(permit.getPermitHolder())
                : createContactPersonAsPermitHolder(permit.getOriginalContactPerson());

        this.speciesAmount = HarvestPermitSpeciesAmountDTO.create(spa, species);
        this.amendmentPermits = amendmentPermits;

        this.viewedClubId = viewedClubId;
        this.viewedClubIsPartner = viewedClubIsPartner;

        this.amendmentPermitsMatchHarvests = amendmentPermitsMatchHarvests;
        this.mooseHarvestReport = MooseHarvestReportDTO.create(mooseHarvestReport);

        this.listLeadersButtonVisible = listLeadersButtonVisible;
        this.huntingFinished = huntingFinished;
        this.huntingFinishedByModeration = huntingFinishedByModeration;

        double total = spa.getAmount() + NumberUtils.sum(amendmentPermits.values(), Float::doubleValue);
        double unallocated = total - allocations.stream()
                .mapToDouble(a -> a.getAdultMales() + a.getAdultFemales() + a.getYoung() / 2.0).sum();

        this.total = total;
        this.unallocated = unallocated;
        this.used = harvests.stream()
                .mapToDouble(h -> h.countAdults() + h.countYoung() / 2.0).sum();
        this.notEdible = harvests.stream()
                .mapToDouble(h -> h.getNumberOfNonEdibleAdults() + h.getNumberOfNonEdibleYoungs() / 2.0).sum();

        if (speciesAmount.getRestrictionType() != null) {
            final Integer restrictedHarvests = harvests.stream()
                    .mapToInt(h -> {
                        switch (speciesAmount.getRestrictionType()) {
                            case AE:
                                return h.getNumberOfAdultMales() + h.getNumberOfAdultFemales() - h.getNumberOfNonEdibleAdults();
                            case AU:
                                return h.getNumberOfAdultMales() - h.getNumberOfNonEdibleAdultMales();
                            default:
                                throw new IllegalStateException("Unknown restriction type:" + speciesAmount.getRestrictionType());
                        }
                    }).sum();
            this.restrictionViolated = restrictedHarvests > speciesAmount.getRestrictionAmount();
        }
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

    private boolean amendmentPermitsMatchHarvests;
    private MooseHarvestReportDTO mooseHarvestReport;
    private boolean listLeadersButtonVisible;
    private boolean huntingFinished;
    private boolean huntingFinishedByModeration;

    private double total;
    private double unallocated;
    private double used;
    private double notEdible;

    private boolean restrictionViolated;

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

    public boolean isRestrictionViolated() {
        return restrictionViolated;
    }

    public void setRestrictionViolated(boolean restrictionViolated) {
        this.restrictionViolated = restrictionViolated;
    }

    public boolean isCurrentlyViewedRhyIsRelated() {
        return currentlyViewedRhyIsRelated;
    }

    public void setCurrentlyViewedRhyIsRelated(boolean currentlyViewedRhyIsRelated) {
        this.currentlyViewedRhyIsRelated = currentlyViewedRhyIsRelated;
    }
}
