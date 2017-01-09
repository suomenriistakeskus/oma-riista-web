package fi.riista.feature.gamediary.fixture;

import com.google.common.base.Preconditions;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.util.NumberGenerator;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;

import java.util.Objects;

import static fi.riista.util.DateUtil.localDateTime;
import static fi.riista.util.TestUtils.createList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@FunctionalInterface
public interface MobileObservationDTOBuilderFactory extends ValueGeneratorMixin {

    default Builder create(@Nonnull final ObservationMetadata metadata) {
        return create(metadata, 0);
    }

    default Builder create(@Nonnull final ObservationMetadata metadata, final int numIdentifiedSpecimens) {
        Preconditions.checkArgument(numIdentifiedSpecimens >= 0, "numIdentifiedSpecimens must not be negative");

        final Builder builder = new Builder(metadata, getNumberGenerator())
                .applyMetadata()
                .withMobileClientRefId(getNumberGenerator().nextLong())
                .withGeoLocation(geoLocation(GeoLocation.Source.GPS_DEVICE))
                .withPointOfTime(localDateTime())
                .withDescription("description");

        if (numIdentifiedSpecimens > 0) {
            builder.withAmount(numIdentifiedSpecimens).withSpecimens(numIdentifiedSpecimens);
        } else if (metadata.getAmount().isAllowedField()) {
            builder.withAmount(1).withSpecimens(emptyList());
        }

        return builder;
    }

    default Builder create(@Nonnull final ObservationMetadata metadata, @Nonnull final Observation observation) {
        return create(metadata, observation, 0);
    }

    default Builder create(@Nonnull final ObservationMetadata metadata,
                           @Nonnull final Observation observation,
                           final int numIdentifiedSpecimens) {

        Preconditions.checkArgument(numIdentifiedSpecimens >= 0, "numIdentifiedSpecimens must not be negative");

        final Builder builder = new Builder(metadata, getNumberGenerator()).populateWith(observation).applyMetadata();

        return numIdentifiedSpecimens > 0
                ? builder.withAmount(numIdentifiedSpecimens).withSpecimens(numIdentifiedSpecimens)
                : builder.withAmount(metadata.getAmount().isAllowedField() ? observation.getAmount() : null);
    }

    default Builder create(@Nonnull final ObservationMetadata metadata, @Nonnull final MobileObservationDTO initial) {
        return new Builder(metadata, getNumberGenerator()) {
            @Override
            protected MobileObservationDTO createDTO() {
                return initial;
            }
        }.applyMetadata();
    }

    class Builder extends MobileObservationDTO.Builder<Builder> implements CanPopulateObservationSpecimen {

        private final ObservationMetadata metadata;
        private final NumberGenerator ng;

        public Builder(@Nonnull final ObservationMetadata metadata, @Nonnull final NumberGenerator ng) {
            super(ObservationSpecVersion.fromIntValue(metadata.getMetadataVersion()));
            this.metadata = Objects.requireNonNull(metadata, "metadata is null");
            this.ng = Objects.requireNonNull(ng, "ng is null");
        }

        @Override
        public NumberGenerator getNumberGenerator() {
            return ng;
        }

        @Override
        public ObservationContextSensitiveFields getContextSensitiveFields() {
            return metadata.getContextSensitiveFields();
        }

        public Builder applyMetadata() {
            return populateWith(metadata.getSpecies())
                    .withWithinMooseHunting(metadata.getWithinMooseHunting())
                    .withObservationType(metadata.getObservationType());
        }

        public Builder withSpecimens(final int numSpecimens) {
            return withSpecimens(createList(numSpecimens, this::newObservationSpecimenDTO));
        }

        public Builder mutate() {
            return withGeoLocation(geoLocation(GeoLocation.Source.GPS_DEVICE))
                    .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                    .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED");
        }

        public Builder mutateSpecimens() {
            dto.getSpecimens().forEach(this::mutateContent);
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

}
