package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenPopulator;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.util.DateUtil;
import fi.riista.util.ValueGeneratorMixin;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.test.TestUtils.createList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@FunctionalInterface
public interface HarvestDTOBuilderFactory extends ValueGeneratorMixin {

    default HarvestSpecVersion getDefaultSpecVersion() {
        // TODO Switch to `CURRENTLY_SUPPORTED` when deer pilot 2020 is over.
        return HarvestSpecVersion._7;
    }

    default Builder create(@Nonnull final GameSpecies species) {
        return create(species, 0);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion, @Nonnull final GameSpecies species) {
        return create(specVersion, species, 0);
    }

    default Builder create(@Nonnull final GameSpecies species, final int numIdentifiedSpecimens) {
        return create(getDefaultSpecVersion(), species, numIdentifiedSpecimens);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion,
                           @Nonnull final GameSpecies species,
                           final int numIdentifiedSpecimens) {

        checkArgument(numIdentifiedSpecimens >= 0, "numIdentifiedSpecimens must not be negative");

        final Builder builder = new Builder(specVersion, this)
                .withGameSpeciesCode(species.getOfficialCode())
                .withGeoLocation(geoLocation(GeoLocation.Source.MANUAL))
                .withPointOfTime(DateUtil.localDateTime())
                .withDescription("description");

        return numIdentifiedSpecimens > 0
                ? builder.withAmount(numIdentifiedSpecimens).withSpecimens(numIdentifiedSpecimens)
                : builder.withAmount(1);
    }

    default Builder create(@Nonnull final Harvest harvest) {
        return create(getDefaultSpecVersion(), harvest);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion, @Nonnull final Harvest harvest) {
        requireNonNull(harvest);
        return create(specVersion, harvest, harvest.getSpecies());
    }

    default Builder create(@Nonnull final Harvest harvest, @Nonnull final GameSpecies species) {
        return create(getDefaultSpecVersion(), harvest, species);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion,
                           @Nonnull final Harvest harvest,
                           @Nonnull final GameSpecies species) {

        return new Builder(specVersion, this)
                .populateWith(harvest)
                .populateWith(harvest.getHarvestPermit())
                .withGameSpeciesCode(species.getOfficialCode())
                .withActorInfo(harvest.getActor())
                .withAuthorInfo(harvest.getAuthor());
    }

    default Builder create(@Nonnull final Harvest harvest, final int numIdentifiedSpecimens) {
        return create(getDefaultSpecVersion(), harvest, numIdentifiedSpecimens);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion,
                           @Nonnull final Harvest harvest,
                           final int numIdentifiedSpecimens) {

        requireNonNull(harvest);
        return create(specVersion, harvest, harvest.getSpecies(), numIdentifiedSpecimens);
    }

    default Builder create(@Nonnull final Harvest harvest,
                           @Nonnull final GameSpecies species,
                           final int numIdentifiedSpecimens) {

        return create(getDefaultSpecVersion(), harvest, species, numIdentifiedSpecimens);
    }

    default Builder create(@Nonnull final HarvestSpecVersion specVersion,
                           @Nonnull final Harvest harvest,
                           @Nonnull final GameSpecies species,
                           final int numIdentifiedSpecimens) {

        checkArgument(numIdentifiedSpecimens >= 0, "numIdentifiedSpecimens must not be negative");

        return create(specVersion, harvest, species).chain(self -> {
            if (numIdentifiedSpecimens > 0) {
                self.withAmount(numIdentifiedSpecimens).withSpecimens(numIdentifiedSpecimens);
            }
        });
    }

    class Builder extends HarvestDTO.Builder<Builder> {

        private final ValueGeneratorMixin values;

        public Builder(@Nonnull final HarvestSpecVersion specVersion, @Nonnull final ValueGeneratorMixin mixin) {
            super(specVersion);

            this.values = requireNonNull(mixin);
        }

        public Builder withSpecimen(@Nonnull final HarvestSpecimenType specimenType) {
            final HarvestSpecimenDTO specimen = new HarvestSpecimenDTO();
            dto.setSpecimens(singletonList(specimen));

            createSpecimenPopulator().mutateContent(specimen, specimenType);

            return self();
        }

        public Builder withSpecimens(final int numSpecimens) {
            dto.setSpecimens(createList(numSpecimens, HarvestSpecimenDTO::new));
            return mutateSpecimens();
        }

        public Builder mutate() {
            return withGeoLocation(values.geoLocation(GeoLocation.Source.MANUAL))
                    .withPointOfTime(ofNullable(dto.getPointOfTime()).map(ldt -> ldt.minusDays(1)).orElse(null))
                    .withDescription(ofNullable(dto.getDescription()).orElse("") + "CHANGED");
        }

        public Builder mutateSpecimens() {
            final HarvestSpecimenPopulator populator = createSpecimenPopulator();
            dto.getSpecimens().forEach(populator::mutateContent);
            return self();
        }

        public Builder linkToHuntingDay(@Nonnull final GroupHuntingDay huntingDay) {
            requireNonNull(huntingDay);
            dto.setHuntingDayId(huntingDay.getId());
            if (!huntingDay.containsInstant(dto.getPointOfTime())) {
                dto.setPointOfTime(huntingDay.getStartAsLocalDateTime());
            }
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        protected HarvestSpecimenPopulator createSpecimenPopulator() {
            return new HarvestSpecimenPopulator(
                    dto.getGameSpeciesCode(),
                    dto.getHarvestSpecVersion(),
                    DateUtil.huntingYearContaining(dto.getPointOfTime().toLocalDate()),
                    values.getNumberGenerator());
        }
    }
}
