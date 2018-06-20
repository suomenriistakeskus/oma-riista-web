package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.endofhunting.MooseHarvestReport;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitCountDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MooselikePermitListDTOBuilder {
    private HarvestPermit permit;
    private HarvestPermitSpeciesAmount spa;
    private GameSpecies species;
    private Map<String, Float> amendmentPermits;
    private Long viewedClubId;
    private boolean viewedClubIsPartner;
    private boolean amendmentPermitsMatchHarvests;
    private MooseHarvestReport mooseHarvestReport;
    private boolean listLeadersButtonVisible;
    private boolean huntingFinished;
    private boolean huntingFinishedByModeration;
    private Collection<HuntingClubPermitCountDTO> harvests;
    private List<MoosePermitAllocationDTO> allocations;

    public MooselikePermitListDTOBuilder setPermit(HarvestPermit permit) {
        this.permit = permit;
        return this;
    }

    public MooselikePermitListDTOBuilder setSpa(HarvestPermitSpeciesAmount spa) {
        this.spa = spa;
        return this;
    }

    public MooselikePermitListDTOBuilder setSpecies(GameSpecies species) {
        this.species = species;
        return this;
    }

    public MooselikePermitListDTOBuilder setAmendmentPermits(Map<String, Float> amendmentPermits) {
        this.amendmentPermits = amendmentPermits;
        return this;
    }

    public MooselikePermitListDTOBuilder setViewedClubId(Long viewedClubId) {
        this.viewedClubId = viewedClubId;
        return this;
    }

    public MooselikePermitListDTOBuilder setViewedClubIsPartner(boolean viewedClubIsPartner) {
        this.viewedClubIsPartner = viewedClubIsPartner;
        return this;
    }

    public MooselikePermitListDTOBuilder setAmendmentPermitsMatchHarvests(boolean amendmentPermitsMatchHarvests) {
        this.amendmentPermitsMatchHarvests = amendmentPermitsMatchHarvests;
        return this;
    }

    public MooselikePermitListDTOBuilder setMooseHarvestReport(MooseHarvestReport mooseHarvestReport) {
        this.mooseHarvestReport = mooseHarvestReport;
        return this;
    }

    public MooselikePermitListDTOBuilder setListLeadersButtonVisible(boolean listLeadersButtonVisible) {
        this.listLeadersButtonVisible = listLeadersButtonVisible;
        return this;
    }

    public MooselikePermitListDTOBuilder setHuntingFinished(boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
        return this;
    }

    public MooselikePermitListDTOBuilder setHuntingFinishedByModeration(boolean huntingFinishedByModeration) {
        this.huntingFinishedByModeration = huntingFinishedByModeration;
        return this;
    }

    public MooselikePermitListDTOBuilder setHarvests(Collection<HuntingClubPermitCountDTO> harvests) {
        this.harvests = harvests;
        return this;
    }

    public MooselikePermitListDTOBuilder setAllocations(List<MoosePermitAllocationDTO> allocations) {
        this.allocations = allocations;
        return this;
    }

    public MooselikePermitListDTO createMooselikePermitListingDTO() {
        return new MooselikePermitListDTO(permit, spa, species, amendmentPermits, viewedClubId, viewedClubIsPartner,
                amendmentPermitsMatchHarvests, mooseHarvestReport,
                listLeadersButtonVisible, huntingFinished, huntingFinishedByModeration, harvests, allocations);
    }
}
