package fi.riista.feature.gamediary.harvest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOps;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.util.F;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

public abstract class HarvestDTOBase extends HuntingDiaryEntryDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._6)
    private HuntingAreaType huntingAreaType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._6)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String huntingParty;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._6)
    private Double huntingAreaSize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._6)
    private HuntingMethod huntingMethod;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._6)
    private Boolean reportedWithPhoneCall;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._6)
    private Boolean feedingPlace;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._6)
    private Boolean taigaBeanGoose;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._2)
    private boolean harvestReportRequired;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._2)
    private HarvestReportState harvestReportState;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._2)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;

    @JsonIgnore
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._2)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    protected String permitType;

    @HarvestSpecVersionSupport(since = HarvestSpecVersion._2)
    private Harvest.StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit;

    @Valid
    @HarvestSpecVersionSupport(since = HarvestSpecVersion._1)
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

    public HuntingAreaType getHuntingAreaType() {
        return huntingAreaType;
    }

    public void setHuntingAreaType(HuntingAreaType huntingAreaType) {
        this.huntingAreaType = huntingAreaType;
    }

    public String getHuntingParty() {
        return huntingParty;
    }

    public void setHuntingParty(String huntingParty) {
        this.huntingParty = huntingParty;
    }

    public Double getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public void setHuntingAreaSize(Double huntingAreaSize) {
        this.huntingAreaSize = huntingAreaSize;
    }

    public HuntingMethod getHuntingMethod() {
        return huntingMethod;
    }

    public void setHuntingMethod(HuntingMethod huntingMethod) {
        this.huntingMethod = huntingMethod;
    }

    public Boolean getReportedWithPhoneCall() {
        return reportedWithPhoneCall;
    }

    public void setReportedWithPhoneCall(Boolean reportedWithPhoneCall) {
        this.reportedWithPhoneCall = reportedWithPhoneCall;
    }

    public Boolean getFeedingPlace() {
        return feedingPlace;
    }

    public Boolean getTaigaBeanGoose() {
        return taigaBeanGoose;
    }

    public void setTaigaBeanGoose(final Boolean taigaBeanGoose) {
        this.taigaBeanGoose = taigaBeanGoose;
    }

    public void setFeedingPlace(final Boolean feedingPlace) {
        this.feedingPlace = feedingPlace;
    }

    public boolean isHarvestReportRequired() {
        return harvestReportRequired;
    }

    public void setHarvestReportRequired(final boolean harvestReportRequired) {
        this.harvestReportRequired = harvestReportRequired;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState harvestReportState) {
        this.harvestReportState = harvestReportState;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPermitType() {
        return permitType;
    }

    @JsonIgnore
    public void setPermitType(final String permitType) {
        this.permitType = permitType;
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

        public SELF withHarvestReportState(@Nullable final HarvestReportState harvestReportState) {
            dto.setHarvestReportState(harvestReportState);
            return self();
        }

        public SELF withPermitNumber(@Nullable final String permitNumber) {
            dto.setPermitNumber(permitNumber);
            return self();
        }

        public SELF withPermitType(@Nullable final String permitType) {
            dto.setPermitType(permitType);
            return self();
        }

        public SELF withStateAcceptedToHarvestPermit(
                @Nullable final StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit) {

            dto.setStateAcceptedToHarvestPermit(stateAcceptedToHarvestPermit);
            return self();
        }

        public SELF withHuntingAreaType(final HuntingAreaType huntingAreaType) {
            dto.setHuntingAreaType(huntingAreaType);
            return self();
        }

        public SELF withHuntingAreaSize(final Double huntingAreaSize) {
            dto.setHuntingAreaSize(huntingAreaSize);
            return self();
        }

        public SELF withHuntingParty(final String huntingParty) {
            dto.setHuntingParty(huntingParty);
            return self();
        }

        public SELF withHuntingMethod(final HuntingMethod huntingMethod) {
            dto.setHuntingMethod(huntingMethod);
            return self();
        }

        public SELF withReportedWithPhoneCall(final Boolean reportedWithPhoneCall) {
            dto.setReportedWithPhoneCall(reportedWithPhoneCall);
            return self();
        }

        public SELF withFeedingPlace(final Boolean feedingPlace) {
            dto.setFeedingPlace(feedingPlace);
            return self();
        }

        public SELF withTaigaBeanGoose(final Boolean taigaBeanGoose) {
            dto.setTaigaBeanGoose(taigaBeanGoose);
            return self();
        }

        // ASSOCIATIONS MUST NOT BE TRAVERSED IN THIS METHOD (except for identifiers that are
        // part of Harvest itself).
        public SELF populateWith(@Nonnull final Harvest harvest) {
            final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();

            final SELF builder = populateWithEntry(harvest)
                    .withHarvestReportState(harvest.getHarvestReportState())
                    .withHarvestReportRequired(harvest.isHarvestReportRequired())
                    .withStateAcceptedToHarvestPermit(specVersion.supportsHarvestPermitState()
                            ? harvest.getStateAcceptedToHarvestPermit()
                            : null);

            if (!specVersion.supportsHarvestReport()) {
                return builder;
            }

            return builder
                    .withHuntingAreaType(harvest.getHuntingAreaType())
                    .withHuntingAreaSize(harvest.getHuntingAreaSize())
                    .withHuntingParty(harvest.getHuntingParty())
                    .withHuntingMethod(harvest.getHuntingMethod())
                    .withReportedWithPhoneCall(harvest.getReportedWithPhoneCall())
                    .withFeedingPlace(harvest.getFeedingPlace())
                    .withTaigaBeanGoose(harvest.getTaigaBeanGoose());
        }

        public SELF populateWith(@Nullable final HarvestPermit permit) {
            if (permit != null) {
                withPermitNumber(permit.getPermitNumber());
                withPermitType(permit.getPermitType());
            }
            return self();
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
