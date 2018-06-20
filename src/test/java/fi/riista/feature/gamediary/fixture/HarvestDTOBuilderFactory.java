package fi.riista.feature.gamediary.fixture;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOpsForTest;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DateUtil;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;
import java.util.Objects;

import static fi.riista.test.TestUtils.createList;
import static java.util.Optional.ofNullable;

@FunctionalInterface
public interface HarvestDTOBuilderFactory extends ValueGeneratorMixin {

    default Builder create(@Nonnull final GameSpecies species) {
        return create(species, 0);
    }

    default Builder create(@Nonnull final GameSpecies species, final int numIdentifiedSpecimens) {
        Preconditions.checkArgument(numIdentifiedSpecimens >= 0, "numIdentifiedSpecimens must not be negative");

        final Builder builder = new Builder(this)
                .populateWith(species)
                .withGeoLocation(geoLocation(GeoLocation.Source.MANUAL))
                .withPointOfTime(DateUtil.localDateTime())
                .withDescription("description");

        return numIdentifiedSpecimens > 0
                ? builder.withAmount(numIdentifiedSpecimens).withSpecimens(numIdentifiedSpecimens)
                : builder.withAmount(1);
    }

    default Builder create(@Nonnull final Harvest harvest) {
        Objects.requireNonNull(harvest);
        return create(harvest, harvest.getSpecies());
    }

    default Builder create(@Nonnull final Harvest harvest, @Nonnull final GameSpecies species) {
        return new Builder(this)
                .populateWith(harvest)
                .populateWith(harvest.getHarvestPermit())
                .populateWith(species);
    }

    default Builder create(@Nonnull final Harvest harvest, final int numIdentifiedSpecimens) {
        Objects.requireNonNull(harvest);
        return create(harvest, harvest.getSpecies(), numIdentifiedSpecimens);
    }

    default Builder create(@Nonnull final Harvest harvest,
                           @Nonnull final GameSpecies species,
                           final int numIdentifiedSpecimens) {

        Preconditions.checkArgument(numIdentifiedSpecimens >= 0, "numIdentifiedSpecimens must not be negative");

        return create(harvest, species).chain(self -> {
            if (numIdentifiedSpecimens > 0) {
                self.withAmount(numIdentifiedSpecimens).withSpecimens(numIdentifiedSpecimens);
            }
        });
    }

    default Builder create(@Nonnull final HarvestDTO initial) {
        return new Builder(this) {
            @Override
            protected HarvestDTO createDTO() {
                return initial;
            }
        };
    }

    class Builder extends HarvestDTO.Builder<Builder> {

        private final ValueGeneratorMixin values;

        public Builder(@Nonnull final ValueGeneratorMixin mixin) {
            this.values = Objects.requireNonNull(mixin);
        }

        public Builder withSpecimens(final int numSpecimens) {
            final HarvestSpecimenOpsForTest ops = getSpecimenOps();
            return withSpecimens(createList(numSpecimens, ops::createDTO));
        }

        public Builder mutate() {
            return withGeoLocation(values.geoLocation(GeoLocation.Source.MANUAL))
                    .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                    .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED");
        }

        public Builder mutate(final GroupHuntingDay huntingDay) {
            final int seconds = values.nextIntBetween(0, huntingDay.calculateHuntingDayDurationInMinutes() - 1);
            return withGeoLocation(values.geoLocation(GeoLocation.Source.MANUAL))
                    .withPointOfTime(huntingDay.getStartAsLocalDateTime().plusSeconds(seconds))
                    .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED");
        }

        public Builder mutateSpecimens() {
            final HarvestSpecimenOpsForTest ops = getSpecimenOps();
            dto.getSpecimens().forEach(ops::mutateContent);
            return this;
        }

        public Builder linkToHuntingDay(@Nonnull final GroupHuntingDay huntingDay) {
            Objects.requireNonNull(huntingDay);
            dto.setHuntingDayId(huntingDay.getId());
            if (!huntingDay.containsInstant(dto.getPointOfTime())) {
                dto.setPointOfTime(huntingDay.getStartAsLocalDateTime());
            }
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
