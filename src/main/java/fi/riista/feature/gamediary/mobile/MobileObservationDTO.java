package fi.riista.feature.gamediary.mobile;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationDTOBase;
import fi.riista.feature.gamediary.observation.ObservationType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import java.util.Objects;

import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_SEKA;

@JsonPropertyOrder({ "id", "mobileClientRefId", "rev", "type", "observationSpecVersion", "geoLocation", "pointOfTime", "gameSpeciesCode", "withinMooseHunting", "observationType", "description", "totalSpecimenAmount", "specimens", "imageIds" })
public class MobileObservationDTO extends ObservationDTOBase {

    private Long mobileClientRefId;

    @NotNull
    private ObservationSpecVersion observationSpecVersion;

    private boolean linkedToGroupHuntingDay;

    public MobileObservationDTO() {
    }

    public MobileObservationDTO(@Nonnull final ObservationSpecVersion specVersion) {
        setObservationSpecVersion(Objects.requireNonNull(specVersion));
    }

    public boolean requiresBeaverObservationTypeTranslation() {
        return getObservationSpecVersion().requiresBeaverObservationTypeTranslationForMobile(getGameSpeciesCode());
    }

    // Accessors -->

    public Long getMobileClientRefId() {
        return mobileClientRefId;
    }

    public void setMobileClientRefId(final Long mobileClientRefId) {
        this.mobileClientRefId = mobileClientRefId;
    }

    @Override
    public ObservationSpecVersion getObservationSpecVersion() {
        return observationSpecVersion;
    }

    public void setObservationSpecVersion(final ObservationSpecVersion observationSpecVersion) {
        this.observationSpecVersion = observationSpecVersion;
    }

    public boolean isLinkedToGroupHuntingDay() {
        return linkedToGroupHuntingDay;
    }

    public void setLinkedToGroupHuntingDay(final boolean linkedToGroupHuntingDay) {
        this.linkedToGroupHuntingDay = linkedToGroupHuntingDay;
    }

    // Builder -->

    public static Builder<?> builder(@Nonnull final ObservationSpecVersion specVersion) {
        return new ConcreteBuilder(specVersion);
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<SELF extends Builder<SELF>>
            extends ObservationDTOBase.Builder<MobileObservationDTO, SELF> {

        protected Builder(@Nonnull final ObservationSpecVersion specVersion) {
            super();
            withSpecVersion(Objects.requireNonNull(specVersion));
        }

        public SELF withSpecVersion(@Nullable final ObservationSpecVersion specVersion) {
            dto.setObservationSpecVersion(specVersion);
            return self();
        }

        public SELF withMobileClientRefId(@Nullable final Long mobileClientRefId) {
            dto.setMobileClientRefId(mobileClientRefId);
            return self();
        }

        @Override
        public SELF withGameSpeciesCode(final int gameSpeciesCode) {
            return super.withGameSpeciesCode(gameSpeciesCode).translateObservationType();
        }

        @Override
        public SELF withObservationType(@Nullable final ObservationType observationType) {
            return super.withObservationType(observationType).translateObservationType();
        }

        // ASSOCIATIONS MUST NOT BE TRAVERSED IN THIS METHOD (except for identifiers that are
        // part of Observation itself).
        @Override
        public SELF populateWith(@Nonnull final Observation observation) {
            return super.populateWith(observation)
                    .withMobileClientRefId(observation.getMobileClientRefId())
                    .chain(self -> dto.setLinkedToGroupHuntingDay(observation.getHuntingDayOfGroup() != null));
        }

        protected SELF translateObservationType() {
            if (dto.requiresBeaverObservationTypeTranslation()) {
                final ObservationType oType = dto.getObservationType();

                if (oType == PESA_KEKO || oType == PESA_PENKKA || oType == PESA_SEKA) {
                    dto.setObservationType(PESA);
                }
            }

            return self();
        }

        @Override
        protected MobileObservationDTO createDTO() {
            return new MobileObservationDTO();
        }
    }

    private static final class ConcreteBuilder extends Builder<ConcreteBuilder> {

        public ConcreteBuilder(@Nonnull final ObservationSpecVersion specVersion) {
            super(specVersion);
        }

        @Override
        protected ConcreteBuilder self() {
            return this;
        }
    }

}
