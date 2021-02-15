package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationDTOBase;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_SEKA;
import static java.util.Objects.requireNonNull;

public class MobileObservationDTO extends ObservationDTOBase {

    private Long mobileClientRefId;

    @NotNull
    private ObservationSpecVersion observationSpecVersion;

    @Deprecated
    private Boolean withinMooseHunting;

    private boolean linkedToGroupHuntingDay;

    public MobileObservationDTO() {
    }

    public MobileObservationDTO(@Nonnull final ObservationSpecVersion specVersion) {
        setObservationSpecVersion(requireNonNull(specVersion));
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

    public Boolean getWithinMooseHunting() {
        return withinMooseHunting;
    }

    public void setWithinMooseHunting(final Boolean withinMooseHunting) {
        this.withinMooseHunting = withinMooseHunting;
    }

    public boolean isLinkedToGroupHuntingDay() {
        return linkedToGroupHuntingDay;
    }

    public void setLinkedToGroupHuntingDay(final boolean linkedToGroupHuntingDay) {
        this.linkedToGroupHuntingDay = linkedToGroupHuntingDay;
    }

    // Builder -->

    public static Builder<?> builder(@Nonnull final ObservationBaseFields baseFields) {
        return new ConcreteBuilder(baseFields);
    }

    // Allows sub-classing for tests and adding new fluent interface style methods.
    public static abstract class Builder<SELF extends Builder<SELF>>
            extends ObservationDTOBase.Builder<MobileObservationDTO, SELF> {

        private boolean nullWithinMooseHunting;

        protected Builder(@Nonnull final ObservationBaseFields baseFields) {
            super();
            withObservationBaseFields(baseFields);
        }

        // Required for MobileObservationFeatureTests
        public SELF withObservationBaseFields(@Nullable final ObservationBaseFields baseFields) {
            nullWithinMooseHunting = baseFields.getWithinMooseHunting().nullValueRequired();

            final ObservationSpecVersion specVersion =
                    requireNonNull(ObservationSpecVersion.fromIntValue(baseFields.getMetadataVersion()));

            return withSpecVersion(specVersion)
                    .withGameSpeciesCode(baseFields.getSpecies().getOfficialCode());
        }

        public SELF withSpecVersion(@Nonnull final ObservationSpecVersion specVersion) {
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
        public SELF withObservationCategory(@Nullable final ObservationCategory observationCategory) {
            if (dto.getObservationSpecVersion().supportsCategory()) {
                dto.setObservationCategory(observationCategory);
                dto.setWithinMooseHunting(null);
            } else {
                dto.setObservationCategory(null);
                dto.setWithinMooseHunting(nullWithinMooseHunting ? null : observationCategory.isWithinMooseHunting());
            }
            return self();
        }

        @Override
        public SELF withObservationType(@Nullable final ObservationType observationType) {
            return super.withObservationType(observationType).translateObservationType();
        }

        @Override
        public SELF withDeerHuntingType(@Nullable final DeerHuntingType deerHuntingType) {
            if (dto.getObservationSpecVersion().supportsDeerHuntingType()) {
                dto.setDeerHuntingType(deerHuntingType);
            } else {
                dto.setDeerHuntingType(null);
            }
            return self();
        }

        @Override
        public SELF withDeerHuntingTypeDescription(@Nullable final String deerHuntingTypeDescription) {
            if (dto.getObservationSpecVersion().supportsDeerHuntingType()) {
                dto.setDeerHuntingTypeDescription(deerHuntingTypeDescription);
            } else {
                dto.setDeerHuntingTypeDescription(null);
            }
            return self();
        }

        @Override
        public SELF withMooselikeAmountsFrom(final Observation observation) {
            // Mooselike amount fields are not sent within normal observation.
            if (!dto.getObservationSpecVersion().supportsCategory()
                    && observation.getObservationCategory().isWithinDeerHunting()) {

                return self();
            }

            return super.withMooselikeAmountsFrom(observation);
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

        public ConcreteBuilder(@Nonnull final ObservationBaseFields baseFields) {
            super(baseFields);
        }

        @Override
        protected ConcreteBuilder self() {
            return this;
        }
    }
}
