package fi.riista.feature.gamediary.observation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationContext;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.validation.PhoneNumber;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public abstract class ObservationDTOBase extends HuntingDiaryEntryDTO implements HasMooselikeObservationAmounts {

    @NotNull
    private ObservationType observationType;

    @JsonProperty(value = "totalSpecimenAmount")
    @Min(Observation.MIN_AMOUNT)
    @Max(Observation.MAX_AMOUNT)
    private Integer amount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ObservationCategory observationCategory;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DeerHuntingType deerHuntingType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String deerHuntingTypeDescription;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(100)
    private Integer mooselikeMaleAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(100)
    private Integer mooselikeFemaleAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(50)
    private Integer mooselikeCalfAmount;

    /**
     * Amount of groups of one adult female moose with one calf within one game
     * observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(50)
    private Integer mooselikeFemale1CalfAmount;

    /**
     * Amount of groups of one adult female moose with two calfs within one game
     * observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(50)
    private Integer mooselikeFemale2CalfsAmount;

    /**
     * Amount of groups of one adult female moose with three calfs within one
     * game observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(50)
    private Integer mooselikeFemale3CalfsAmount;

    /**
     * Amount of groups of one adult female moose with four calfs within one
     * game observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(50)
    private Integer mooselikeFemale4CalfsAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(0)
    @Max(50)
    private Integer mooselikeUnknownSpecimenAmount;

    private Integer inYardDistanceToResidence;

    private Boolean verifiedByCarnivoreAuthority;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String observerName;

    @PhoneNumber
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String observerPhoneNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String officialAdditionalInfo;

    @Valid
    private List<ObservationSpecimenDTO> specimens;

    // Lauma
    @JsonIgnore
    private Boolean pack;

    // Pentue
    @JsonIgnore
    private Boolean litter;

    protected ObservationDTOBase() {
        super(GameDiaryEntryType.OBSERVATION);
    }

    public boolean observedWithinHunting() {
        return observationCategory == ObservationCategory.MOOSE_HUNTING
                || observationCategory == ObservationCategory.DEER_HUNTING;
    }

    @AssertFalse
    public boolean isGeolocationSourceNull() {
        return getGeoLocation().getSource() == null;
    }

    @AssertTrue
    public boolean isAmountAndSpecimenListBothNullOrNotNull() {
        return amount != null && specimens != null || amount == null && specimens == null;
    }

    public ObservationSpecimenOps specimenOps() {
        return new ObservationSpecimenOps(getGameSpeciesCode(), getObservationSpecVersion());
    }

    public ObservationContext getObservationContext() {
        return new ObservationContext(
                getObservationSpecVersion(),
                getGameSpeciesCode(),
                getObservationCategory(),
                getObservationType());
    }

    // Accessors -->

    public abstract ObservationSpecVersion getObservationSpecVersion();

    public ObservationType getObservationType() {
        return observationType;
    }

    public void setObservationType(final ObservationType observationType) {
        this.observationType = observationType;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(final Integer amount) {
        this.amount = amount;
    }

    public ObservationCategory getObservationCategory() {
        return observationCategory;
    }

    public void setObservationCategory(final ObservationCategory observationCategory) {
        this.observationCategory = observationCategory;
    }

    public DeerHuntingType getDeerHuntingType() {
        return deerHuntingType;
    }

    public void setDeerHuntingType(final DeerHuntingType deerHuntingType) {
        this.deerHuntingType = deerHuntingType;
    }

    public String getDeerHuntingTypeDescription() {
        return deerHuntingTypeDescription;
    }

    public void setDeerHuntingTypeDescription(final String deerHuntingTypeDescription) {
        this.deerHuntingTypeDescription = deerHuntingTypeDescription;
    }

    @Override
    public Integer getMooselikeMaleAmount() {
        return mooselikeMaleAmount;
    }

    public void setMooselikeMaleAmount(final Integer mooselikeMaleAmount) {
        this.mooselikeMaleAmount = mooselikeMaleAmount;
    }

    @Override
    public Integer getMooselikeFemaleAmount() {
        return mooselikeFemaleAmount;
    }

    public void setMooselikeFemaleAmount(final Integer mooselikeFemaleAmount) {
        this.mooselikeFemaleAmount = mooselikeFemaleAmount;
    }

    @Override
    public Integer getMooselikeCalfAmount() {
        return mooselikeCalfAmount;
    }

    public void setMooselikeCalfAmount(final Integer mooselikeCalfAmount) {
        this.mooselikeCalfAmount = mooselikeCalfAmount;
    }

    @Override
    public Integer getMooselikeFemale1CalfAmount() {
        return mooselikeFemale1CalfAmount;
    }

    public void setMooselikeFemale1CalfAmount(final Integer mooselikeFemale1CalfAmount) {
        this.mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount;
    }

    @Override
    public Integer getMooselikeFemale2CalfsAmount() {
        return mooselikeFemale2CalfsAmount;
    }

    public void setMooselikeFemale2CalfsAmount(final Integer mooselikeFemale2CalfsAmount) {
        this.mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount;
    }

    @Override
    public Integer getMooselikeFemale3CalfsAmount() {
        return mooselikeFemale3CalfsAmount;
    }

    public void setMooselikeFemale3CalfsAmount(final Integer mooselikeFemale3CalfsAmount) {
        this.mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount;
    }

    @Override
    public Integer getMooselikeFemale4CalfsAmount() {
        return mooselikeFemale4CalfsAmount;
    }

    public void setMooselikeFemale4CalfsAmount(final Integer mooselikeFemale4CalfsAmount) {
        this.mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount;
    }

    @Override
    public Integer getMooselikeUnknownSpecimenAmount() {
        return mooselikeUnknownSpecimenAmount;
    }

    public void setMooselikeUnknownSpecimenAmount(final Integer mooselikeUnknownSpecimenAmount) {
        this.mooselikeUnknownSpecimenAmount = mooselikeUnknownSpecimenAmount;
    }

    public Integer getInYardDistanceToResidence() {
        return inYardDistanceToResidence;
    }

    public void setInYardDistanceToResidence(final Integer inYardDistanceToResidence) {
        this.inYardDistanceToResidence = inYardDistanceToResidence;
    }

    public Boolean getVerifiedByCarnivoreAuthority() {
        return verifiedByCarnivoreAuthority;
    }

    public void setVerifiedByCarnivoreAuthority(final Boolean verifiedByCarnivoreAuthority) {
        this.verifiedByCarnivoreAuthority = verifiedByCarnivoreAuthority;
    }

    public String getObserverName() {
        return observerName;
    }

    public void setObserverName(final String observerName) {
        this.observerName = observerName;
    }

    public String getObserverPhoneNumber() {
        return observerPhoneNumber;
    }

    public void setObserverPhoneNumber(final String observerPhoneNumber) {
        this.observerPhoneNumber = observerPhoneNumber;
    }

    public String getOfficialAdditionalInfo() {
        return officialAdditionalInfo;
    }

    public void setOfficialAdditionalInfo(final String officialAdditionalInfo) {
        this.officialAdditionalInfo = officialAdditionalInfo;
    }

    public List<ObservationSpecimenDTO> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(final List<ObservationSpecimenDTO> specimens) {
        this.specimens = specimens;
    }

    public void setSpecimensMappedFrom(final List<ObservationSpecimen> specimenEntities) {
        this.specimens = specimenEntities == null
                ? null
                : specimenEntities.isEmpty() ? Collections.emptyList() : specimenOps().transformList(specimenEntities);
    }

    @JsonProperty
    public Boolean isPack() {
        return pack;
    }

    @JsonIgnore
    public void setPack(final Boolean pack) {
        this.pack = pack;
    }

    @JsonProperty
    public Boolean isLitter() {
        return litter;
    }

    @JsonIgnore
    public void setLitter(final Boolean litter) {
        this.litter = litter;
    }

    // Builder -->

    public static abstract class Builder<DTO extends ObservationDTOBase, SELF extends Builder<DTO, SELF>>
            extends HuntingDiaryEntryDTO.Builder<DTO, SELF> {

        public SELF withObservationCategory(@Nullable final ObservationCategory observationCategory) {
            dto.setObservationCategory(observationCategory);
            return self();
        }

        public SELF withObservationType(@Nullable final ObservationType observationType) {
            dto.setObservationType(observationType);
            return self();
        }

        public SELF withDeerHuntingType(@Nullable final DeerHuntingType deerHuntingType) {
            dto.setDeerHuntingType(deerHuntingType);
            return self();
        }

        public SELF withDeerHuntingTypeDescription(@Nullable final String deerHuntingTypeDescription) {
            dto.setDeerHuntingTypeDescription(deerHuntingTypeDescription);
            return self();
        }

        public SELF withAmount(@Nullable final Integer amount) {
            dto.setAmount(amount);
            return self();
        }

        public SELF withMooselikeAmountsFrom(@Nonnull final Observation observation) {
            dto.setMooselikeMaleAmount(observation.getMooselikeMaleAmount());
            dto.setMooselikeFemaleAmount(observation.getMooselikeFemaleAmount());
            dto.setMooselikeCalfAmount(observation.getMooselikeCalfAmount());
            dto.setMooselikeFemale1CalfAmount(observation.getMooselikeFemale1CalfAmount());
            dto.setMooselikeFemale2CalfsAmount(observation.getMooselikeFemale2CalfsAmount());
            dto.setMooselikeFemale3CalfsAmount(observation.getMooselikeFemale3CalfsAmount());
            dto.setMooselikeFemale4CalfsAmount(observation.getMooselikeFemale4CalfsAmount());
            dto.setMooselikeUnknownSpecimenAmount(observation.getMooselikeUnknownSpecimenAmount());
            return self();
        }

        public SELF withSpecimens(@Nullable final List<ObservationSpecimenDTO> specimens) {
            dto.setSpecimens(specimens);
            return self();
        }

        public SELF populateSpecimensWith(@Nullable final List<ObservationSpecimen> specimenEntities) {
            dto.setSpecimensMappedFrom(specimenEntities);
            return self();
        }
    }
}
