package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOps;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.util.F;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class HarvestDTOBase extends HuntingDiaryEntryDTO {

    private boolean harvestReportRequired;

    private HarvestReport.State harvestReportState;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;

    private Harvest.StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit;

    private List<HarvestSpecimenDTO> specimens;

    protected HarvestDTOBase() {
        super(GameDiaryEntryType.HARVEST);
    }

    public boolean hasPermitNumber() {
        return StringUtils.isNotBlank(getPermitNumber());
    }

    public HarvestSpecimenOps specimenOps() {
        return new HarvestSpecimenOps(getGameSpeciesCode(), getHarvestSpecVersion());
    }

    // Accessors -->

    public abstract HarvestSpecVersion getHarvestSpecVersion();

    public boolean isHarvestReportRequired() {
        return harvestReportRequired;
    }

    public void setHarvestReportRequired(final boolean harvestReportRequired) {
        this.harvestReportRequired = harvestReportRequired;
    }

    public HarvestReport.State getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReport.State harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public StateAcceptedToHarvestPermit getStateAcceptedToHarvestPermit() {
        return stateAcceptedToHarvestPermit;
    }

    public void setStateAcceptedToHarvestPermit(final StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit) {
        this.stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit;
    }

    public List<HarvestSpecimenDTO> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(final List<HarvestSpecimenDTO> specimens) {
        this.specimens = specimens;
    }

    public void setSpecimensMappedFrom(final List<HarvestSpecimen> specimenEntities) {
        this.specimens = !F.isNullOrEmpty(specimenEntities)
                ? specimenOps().transformList(specimenEntities)
                : getHarvestSpecVersion().requiresSpecimenList() ? Collections.emptyList() : null;
    }

    // Builder -->

    protected static abstract class Builder<DTO extends HarvestDTOBase, SELF extends Builder<DTO, SELF>>
            extends HuntingDiaryEntryDTO.Builder<DTO, SELF> {

        public SELF withHarvestReportRequired(final boolean required) {
            dto.setHarvestReportRequired(required);
            return self();
        }

        public SELF withHarvestReportState(@Nullable final HarvestReport.State harvestReportState) {
            dto.setHarvestReportState(harvestReportState);
            return self();
        }

        public SELF withPermitNumber(@Nullable final String permitNumber) {
            dto.setPermitNumber(permitNumber);
            return self();
        }

        public SELF withStateAcceptedToHarvestPermit(
                @Nullable final StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit) {

            dto.setStateAcceptedToHarvestPermit(stateAcceptedToHarvestPermit);
            return self();
        }

        // ASSOCIATIONS MUST NOT BE TRAVERSED IN THIS METHOD (except for identifiers that are
        // part of Harvest itself).
        public SELF populateWith(@Nonnull final Harvest harvest) {
            return populateWithEntry(harvest)
                    .withHarvestReportRequired(harvest.isHarvestReportRequired())
                    .withStateAcceptedToHarvestPermit(dto.getHarvestSpecVersion().supportsHarvestPermitState()
                            ? harvest.getStateAcceptedToHarvestPermit()
                            : null);
        }

        public SELF withSpecimens(@Nullable final List<HarvestSpecimenDTO> specimens) {
            dto.setSpecimens(specimens);
            return self();
        }

        public SELF populateSpecimensWith(@Nullable final List<HarvestSpecimen> specimenEntities) {
            dto.setSpecimensMappedFrom(specimenEntities);
            return self();
        }
    }

}
