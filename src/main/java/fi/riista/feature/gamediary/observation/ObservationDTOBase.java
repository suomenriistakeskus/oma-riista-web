package fi.riista.feature.gamediary.observation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.HuntingDiaryEntryDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationContext;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.util.F;
import fi.riista.validation.PhoneNumber;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.riista.util.F.coalesceAsInt;

public abstract class ObservationDTOBase extends HuntingDiaryEntryDTO {

    @NotNull
    private ObservationType observationType;

    @JsonProperty(value = "totalSpecimenAmount")
    @Range(min = Observation.MIN_AMOUNT, max = Observation.MAX_AMOUNT)
    private Integer amount;

    private Boolean withinMooseHunting;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 100)
    private Integer mooselikeMaleAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 100)
    private Integer mooselikeFemaleAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 50)
    private Integer mooselikeCalfAmount;

    /**
     * Amount of groups of one adult female moose with one calf within one game
     * observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 50)
    private Integer mooselikeFemale1CalfAmount;

    /**
     * Amount of groups of one adult female moose with two calfs within one game
     * observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 50)
    private Integer mooselikeFemale2CalfsAmount;

    /**
     * Amount of groups of one adult female moose with three calfs within one
     * game observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 50)
    private Integer mooselikeFemale3CalfsAmount;

    /**
     * Amount of groups of one adult female moose with four calfs within one
     * game observation (event).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 50)
    private Integer mooselikeFemale4CalfsAmount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Range(min = 0, max = 50)
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

    public boolean observedWithinMooseHunting() {
        return Boolean.TRUE.equals(withinMooseHunting);
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
                getObservationSpecVersion(), getGameSpeciesCode(), observedWithinMooseHunting(), getObservationType());
    }

    public boolean containsAnyMooselikeAmount() {
        return F.anyNonNull(
                mooselikeMaleAmount, mooselikeFemaleAmount, mooselikeCalfAmount, mooselikeFemale1CalfAmount,
                mooselikeFemale2CalfsAmount, mooselikeFemale3CalfsAmount, mooselikeFemale4CalfsAmount,
                mooselikeUnknownSpecimenAmount);
    }

    public int getSumOfMooselikeAmountFields() {
        return coalesceAsInt(mooselikeMaleAmount, 0)
                + coalesceAsInt(mooselikeFemaleAmount, 0)
                + coalesceAsInt(mooselikeCalfAmount, 0)
                + 2 * coalesceAsInt(mooselikeFemale1CalfAmount, 0)
                + 3 * coalesceAsInt(mooselikeFemale2CalfsAmount, 0)
                + 4 * coalesceAsInt(mooselikeFemale3CalfsAmount, 0)
                + 5 * coalesceAsInt(mooselikeFemale4CalfsAmount, 0)
                + coalesceAsInt(mooselikeUnknownSpecimenAmount, 0);
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

    public Boolean getWithinMooseHunting() {
        return withinMooseHunting;
    }

    public void setWithinMooseHunting(final Boolean withinMooseHunting) {
        this.withinMooseHunting = withinMooseHunting;
    }

    public Integer getMooselikeMaleAmount() {
        return mooselikeMaleAmount;
    }

    public void setMooselikeMaleAmount(final Integer mooselikeMaleAmount) {
        this.mooselikeMaleAmount = mooselikeMaleAmount;
    }

    public Integer getMooselikeFemaleAmount() {
        return mooselikeFemaleAmount;
    }

    public void setMooselikeFemaleAmount(final Integer mooselikeFemaleAmount) {
        this.mooselikeFemaleAmount = mooselikeFemaleAmount;
    }

    public Integer getMooselikeCalfAmount() {
        return mooselikeCalfAmount;
    }

    public void setMooselikeCalfAmount(final Integer mooselikeCalfAmount) {
        this.mooselikeCalfAmount = mooselikeCalfAmount;
    }

    public Integer getMooselikeFemale1CalfAmount() {
        return mooselikeFemale1CalfAmount;
    }

    public void setMooselikeFemale1CalfAmount(final Integer mooselikeFemale1CalfAmount) {
        this.mooselikeFemale1CalfAmount = mooselikeFemale1CalfAmount;
    }

    public Integer getMooselikeFemale2CalfsAmount() {
        return mooselikeFemale2CalfsAmount;
    }

    public void setMooselikeFemale2CalfsAmount(final Integer mooselikeFemale2CalfsAmount) {
        this.mooselikeFemale2CalfsAmount = mooselikeFemale2CalfsAmount;
    }

    public Integer getMooselikeFemale3CalfsAmount() {
        return mooselikeFemale3CalfsAmount;
    }

    public void setMooselikeFemale3CalfsAmount(final Integer mooselikeFemale3CalfsAmount) {
        this.mooselikeFemale3CalfsAmount = mooselikeFemale3CalfsAmount;
    }

    public Integer getMooselikeFemale4CalfsAmount() {
        return mooselikeFemale4CalfsAmount;
    }

    public void setMooselikeFemale4CalfsAmount(final Integer mooselikeFemale4CalfsAmount) {
        this.mooselikeFemale4CalfsAmount = mooselikeFemale4CalfsAmount;
    }

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

        public SELF withWithinMooseHunting(@Nullable final Boolean withinMooseHunting) {
            dto.setWithinMooseHunting(withinMooseHunting);
            return self();
        }

        public SELF withObservationType(@Nullable final ObservationType observationType) {
            dto.setObservationType(observationType);
            return self();
        }

        public SELF withAmount(@Nullable final Integer amount) {
            dto.setAmount(amount);
            return self();
        }

        // ASSOCIATIONS MUST NOT BE TRAVERSED IN THIS METHOD (except for identifiers that are
        // part of the entity itself).
        public SELF populateWith(@Nonnull final Observation observation, final boolean populateLargeCarnivoreFields) {
            return populateWithEntry(observation)
                    .withWithinMooseHunting(observation.getWithinMooseHunting())
                    .withObservationType(observation.getObservationType())
                    .withAmount(observation.getAmount())
                    .withSpecimens(observation.getAmount() != null ? new ArrayList<>() : null)
                    .chain(self -> {
                        dto.setMooselikeMaleAmount(observation.getMooselikeMaleAmount());
                        dto.setMooselikeFemaleAmount(observation.getMooselikeFemaleAmount());
                        dto.setMooselikeCalfAmount(observation.getMooselikeCalfAmount());
                        dto.setMooselikeFemale1CalfAmount(observation.getMooselikeFemale1CalfAmount());
                        dto.setMooselikeFemale2CalfsAmount(observation.getMooselikeFemale2CalfsAmount());
                        dto.setMooselikeFemale3CalfsAmount(observation.getMooselikeFemale3CalfsAmount());
                        dto.setMooselikeFemale4CalfsAmount(observation.getMooselikeFemale4CalfsAmount());
                        dto.setMooselikeUnknownSpecimenAmount(observation.getMooselikeUnknownSpecimenAmount());

                        if (populateLargeCarnivoreFields) {
                            dto.setVerifiedByCarnivoreAuthority(observation.getVerifiedByCarnivoreAuthority());
                            dto.setObserverName(observation.getObserverName());
                            dto.setObserverPhoneNumber(observation.getObserverPhoneNumber());
                            dto.setOfficialAdditionalInfo(observation.getOfficialAdditionalInfo());
                        }
                    });
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
