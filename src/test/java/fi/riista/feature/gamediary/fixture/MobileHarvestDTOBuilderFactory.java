package fi.riista.feature.gamediary.fixture;

import com.google.common.base.Preconditions;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOpsForTest;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.util.DateUtil;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;

import java.util.Objects;

import static fi.riista.util.TestUtils.createList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@FunctionalInterface
public interface MobileHarvestDTOBuilderFactory extends ValueGeneratorMixin {

    default Builder create(@Nonnull final HarvestSpecVersion specVersion, @Nonnull final GameSpecies species) {
        return create(specVersion, species, 0);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion,
                           @Nonnull final GameSpecies species,
                           final int numIdentifiedSpecimens) {

        Preconditions.checkArgument(numIdentifiedSpecimens >= 0, "numIdentifiedSpecimens must not be negative");

        final Builder builder = new Builder(specVersion, this)
                .populateWith(species)
                .withGeoLocation(geoLocation(GeoLocation.Source.GPS_DEVICE))
                .withPointOfTime(DateUtil.localDateTime())
                .withDescription("description")
                .withMobileClientRefId(specVersion.greaterThanOrEqualTo(HarvestSpecVersion._3)
                        ? getNumberGenerator().nextLong()
                        : null);

        return numIdentifiedSpecimens > 0
                ? builder.withAmount(numIdentifiedSpecimens).withSpecimens(numIdentifiedSpecimens)
                : builder.withAmount(1).withSpecimens(specVersion.requiresSpecimenList() ? emptyList() : null);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion, @Nonnull final Harvest harvest) {
        Objects.requireNonNull(harvest, "harvest is null");
        return create(specVersion, harvest, harvest.getSpecies());
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion,
                           @Nonnull final Harvest harvest,
                           @Nonnull final GameSpecies species) {

        return new Builder(specVersion, this).populateWith(harvest).populateWith(species);
    }

    default Builder create(@Nonnull final MobileHarvestDTO initial) {
        return new Builder(initial.getHarvestSpecVersion(), this) {
            @Override
            protected MobileHarvestDTO createDTO() {
                return initial;
            }
        };
    }

    class Builder extends MobileHarvestDTO.Builder<Builder> {

        private final ValueGeneratorMixin values;

        public Builder(@Nonnull final HarvestSpecVersion specVersion, @Nonnull final ValueGeneratorMixin mixin) {
            super(specVersion);
            this.values = Objects.requireNonNull(mixin);
        }

        public Builder withSpecimens(final int numSpecimens) {
            return withSpecimens(createList(numSpecimens, getSpecimenOps()::newHarvestSpecimenDTO));
        }

        public Builder mutate() {
            return withGeoLocation(values.geoLocation(GeoLocation.Source.GPS_DEVICE))
                    .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                    .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED");
        }

        public Builder mutateSpecimens() {
            dto.getSpecimens().forEach(getSpecimenOps()::mutateContent);
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        protected HarvestSpecimenOpsForTest getSpecimenOps() {
            return new HarvestSpecimenOpsForTest(
                    dto.getGameSpeciesCode(), dto.getHarvestSpecVersion(), values.getNumberGenerator());
        }
    }

}
