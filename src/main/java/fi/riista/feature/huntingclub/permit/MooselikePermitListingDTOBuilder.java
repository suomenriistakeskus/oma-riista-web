package fi.riista.feature.huntingclub.permit;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.permit.allocation.HuntingClubPermitAllocationDTO;
import fi.riista.feature.huntingclub.permit.harvestreport.MooseHarvestReport;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MooselikePermitListingDTOBuilder {
    private HarvestPermit permit;
    private HarvestPermitSpeciesAmount spa;
    private GameSpecies species;
    private Map<String, Float> amendmentPermits;
    private Long viewedClubId;
    private boolean viewedClubIsPartner;
    private boolean canEditAllocations;
    private boolean hasPermissionToCreateOrRemove;
    private boolean allPartnersFinishedHunting;
    private boolean amendmentPermitsMatchHarvests;
    private MooseHarvestReport mooseHarvestReport;
    private boolean listLeadersButtonVisible;
    private boolean huntingFinished;
    private boolean huntingFinishedByModeration;
    private Collection<HuntingClubPermitCountDTO> harvests;
    private List<HuntingClubPermitAllocationDTO> allocations;
    private HuntingClubPermitTotalPaymentDTO totalPayment;

    public MooselikePermitListingDTOBuilder setPermit(HarvestPermit permit) {
        this.permit = permit;
        return this;
    }

    public MooselikePermitListingDTOBuilder setSpa(HarvestPermitSpeciesAmount spa) {
        this.spa = spa;
        return this;
    }

    public MooselikePermitListingDTOBuilder setSpecies(GameSpecies species) {
        this.species = species;
        return this;
    }

    public MooselikePermitListingDTOBuilder setAmendmentPermits(Map<String, Float> amendmentPermits) {
        this.amendmentPermits = amendmentPermits;
        return this;
    }

    public MooselikePermitListingDTOBuilder setViewedClubId(Long viewedClubId) {
        this.viewedClubId = viewedClubId;
        return this;
    }

    public MooselikePermitListingDTOBuilder setViewedClubIsPartner(boolean viewedClubIsPartner) {
        this.viewedClubIsPartner = viewedClubIsPartner;
        return this;
    }

    public MooselikePermitListingDTOBuilder setCanEditAllocations(boolean canEditAllocations) {
        this.canEditAllocations = canEditAllocations;
        return this;
    }

    public MooselikePermitListingDTOBuilder setHasPermissionToCreateOrRemove(boolean hasPermissionToCreateOrRemove) {
        this.hasPermissionToCreateOrRemove = hasPermissionToCreateOrRemove;
        return this;
    }

    public MooselikePermitListingDTOBuilder setAllPartnersFinishedHunting(boolean allPartnersFinishedHunting) {
        this.allPartnersFinishedHunting = allPartnersFinishedHunting;
        return this;
    }

    public MooselikePermitListingDTOBuilder setAmendmentPermitsMatchHarvests(boolean amendmentPermitsMatchHarvests) {
        this.amendmentPermitsMatchHarvests = amendmentPermitsMatchHarvests;
        return this;
    }

    public MooselikePermitListingDTOBuilder setMooseHarvestReport(MooseHarvestReport mooseHarvestReport) {
        this.mooseHarvestReport = mooseHarvestReport;
        return this;
    }

    public MooselikePermitListingDTOBuilder setListLeadersButtonVisible(boolean listLeadersButtonVisible) {
        this.listLeadersButtonVisible = listLeadersButtonVisible;
        return this;
    }

    public MooselikePermitListingDTOBuilder setHuntingFinished(boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
        return this;
    }

    public MooselikePermitListingDTOBuilder setHuntingFinishedByModeration(boolean huntingFinishedByModeration) {
        this.huntingFinishedByModeration = huntingFinishedByModeration;
        return this;
    }

    public MooselikePermitListingDTOBuilder setHarvests(Collection<HuntingClubPermitCountDTO> harvests) {
        this.harvests = harvests;
        return this;
    }

    public MooselikePermitListingDTOBuilder setAllocations(List<HuntingClubPermitAllocationDTO> allocations) {
        this.allocations = allocations;
        return this;
    }

    public MooselikePermitListingDTOBuilder setTotalPayment(HuntingClubPermitTotalPaymentDTO totalPayment) {
        this.totalPayment = totalPayment;
        return this;
    }

    public MooselikePermitListingDTO createMooselikePermitListingDTO() {
        return new MooselikePermitListingDTO(permit, spa, species, amendmentPermits, viewedClubId, viewedClubIsPartner,
                canEditAllocations, hasPermissionToCreateOrRemove,
                allPartnersFinishedHunting, amendmentPermitsMatchHarvests, mooseHarvestReport,
                listLeadersButtonVisible, huntingFinished, huntingFinishedByModeration, harvests, allocations, totalPayment);
    }
}
