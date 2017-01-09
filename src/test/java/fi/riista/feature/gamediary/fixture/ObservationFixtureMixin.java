package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.mobile.MobileObservationDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.util.TestUtils.createList;

@FunctionalInterface
public interface ObservationFixtureMixin extends FixtureMixin {

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final ObservationType observationType) {
        return createObservationMetaF(MOST_RECENT, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(getEntitySupplier().nextPositiveInt(), version, null, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nullable final Boolean withinMooseHunting,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(MOST_RECENT, withinMooseHunting, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(gameSpeciesCode, MOST_RECENT, null, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(gameSpeciesCode, version, null, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final ObservationSpecVersion version,
                                                                  @Nullable final Boolean withinMooseHunting,
                                                                  @Nonnull final ObservationType observationType) {

        final int gameSpeciesCode = getEntitySupplier().nextPositiveInt();
        return createObservationMetaF(gameSpeciesCode, version, withinMooseHunting, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nullable final Boolean withinMooseHunting,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(gameSpeciesCode, MOST_RECENT, withinMooseHunting, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nonnull final ObservationSpecVersion version,
                                                                  @Nullable final Boolean withinMooseHunting,
                                                                  @Nonnull final ObservationType observationType) {

        final GameSpecies species = getEntitySupplier().newGameSpecies(gameSpeciesCode);
        return createObservationMetaF(species, version, withinMooseHunting, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(species, MOST_RECENT, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(species, version, null, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nullable final Boolean withinMooseHunting,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(species, MOST_RECENT, withinMooseHunting, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationSpecVersion version,
                                                                  @Nullable final Boolean withinMooseHunting,
                                                                  @Nonnull final ObservationType observationType) {

        final EntitySupplier es = getEntitySupplier();
        final ObservationMetadata metadata =
                es.newObservationMetadata(species, version.toIntValue(), withinMooseHunting, observationType);
        return new ObservationMetaFixture.Builder(metadata, es);
    }

    abstract class MetaFixtureBase extends ObservationMetadata {

        protected final EntitySupplier entitySupplier;

        MetaFixtureBase(@Nonnull final ObservationMetadata metadata, @Nonnull final EntitySupplier entitySupplier) {
            super(metadata.getBaseFields(), metadata.getContextSensitiveFields());
            this.entitySupplier = Objects.requireNonNull(entitySupplier);
        }

        protected static abstract class Builder<T extends MetaFixtureBase, SELF extends Builder<T, SELF>>
                extends AbstractObservationFixtureBuilder<ObservationMetadata, T> {

            protected Builder(@Nonnull final ObservationMetadata metadata,
                              @Nonnull final EntitySupplier entitySupplier,
                              final boolean forMobileAPI) {

                super(metadata, entitySupplier, forMobileAPI);
            }

            protected abstract SELF self();

            public SELF withAmount(@Nonnull final Required amount) {
                Objects.requireNonNull(amount);
                metadata.setAmount(amount);
                return self();
            }

            public ObservationSpecimenFixture.Builder<T> createSpecimensF(final int numSpecimens) {
                return new ObservationSpecimenFixture.Builder<>(build(), entitySupplier, numSpecimens, forMobileAPI);
            }

            public ObservationSpecimenFixture.Builder<T> createSpecimensF(@Nullable final Person author,
                                                                          final int numSpecimens) {

                return createSpecimensF(numSpecimens).withAuthor(author);
            }
        }
    }

    class ObservationMetaFixture extends MetaFixtureBase {

        private final ObservationDTOBuilderFactory dtoBuilderFactory = entitySupplier::getNumberGenerator;

        ObservationMetaFixture(@Nonnull final ObservationMetadata metadata,
                               @Nonnull final EntitySupplier entitySupplier) {
            super(metadata, entitySupplier);
        }

        public ObservationDTOBuilderFactory.Builder dtoBuilder() {
            return dtoBuilderFactory.create(this);
        }

        public ObservationDTOBuilderFactory.Builder dtoBuilder(final int numIdentifiedSpecimens) {
            return dtoBuilderFactory.create(this, numIdentifiedSpecimens);
        }

        public ObservationDTOBuilderFactory.Builder dtoBuilder(@Nonnull final Observation observation) {
            return dtoBuilderFactory.create(this, observation);
        }

        public ObservationDTOBuilderFactory.Builder dtoBuilder(@Nonnull final Observation observation,
                                                               final int numIdentifiedSpecimens) {
            return dtoBuilderFactory.create(this, observation, numIdentifiedSpecimens);
        }

        public ObservationDTOBuilderFactory.Builder dtoBuilder(@Nonnull final ObservationDTO dto) {
            return dtoBuilderFactory.create(this, dto);
        }

        public static class Builder extends MetaFixtureBase.Builder<ObservationMetaFixture, Builder> {

            public Builder(@Nonnull final ObservationMetadata metadata, @Nonnull final EntitySupplier es) {
                super(metadata, es, false);
            }

            public MobileObservationMetaFixture.Builder forMobile() {
                return forMobile(false);
            }

            public MobileObservationMetaFixture.Builder forMobile(final boolean duplicateMetadataForMostRecentVersionAsSideEffect) {
                return new MobileObservationMetaFixture.Builder(metadata, entitySupplier, duplicateMetadataForMostRecentVersionAsSideEffect);
            }

            @Override
            public ObservationMetaFixture build() {
                return new ObservationMetaFixture(metadata, entitySupplier);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }

    class MobileObservationMetaFixture extends MetaFixtureBase {

        private final MobileObservationDTOBuilderFactory dtoBuilderFactory = entitySupplier::getNumberGenerator;

        MobileObservationMetaFixture(@Nonnull final ObservationMetadata metadata,
                                     @Nonnull final EntitySupplier entitySupplier) {
            super(metadata, entitySupplier);
        }

        public MobileObservationDTOBuilderFactory.Builder dtoBuilder() {
            return dtoBuilderFactory.create(this);
        }

        public MobileObservationDTOBuilderFactory.Builder dtoBuilder(final int numIdentifiedSpecimens) {
            return dtoBuilderFactory.create(this, numIdentifiedSpecimens);
        }

        public MobileObservationDTOBuilderFactory.Builder dtoBuilder(@Nonnull final Observation observation) {
            return dtoBuilderFactory.create(this, observation);
        }

        public MobileObservationDTOBuilderFactory.Builder dtoBuilder(@Nonnull final Observation observation,
                                                                     final int numIdentifiedSpecimens) {
            return dtoBuilderFactory.create(this, observation, numIdentifiedSpecimens);
        }

        public MobileObservationDTOBuilderFactory.Builder dtoBuilder(@Nonnull final MobileObservationDTO dto) {
            return dtoBuilderFactory.create(this, dto);
        }

        public static class Builder extends MetaFixtureBase.Builder<MobileObservationMetaFixture, Builder> {

            private final boolean duplicateMetadataForMostRecentVersionAsSideEffect;

            public Builder(@Nonnull final ObservationMetadata metadata,
                           @Nonnull final EntitySupplier es,
                           final boolean duplicateMetadataForMostRecentVersionAsSideEffect) {

                super(metadata, es, true);
                this.duplicateMetadataForMostRecentVersionAsSideEffect =
                        duplicateMetadataForMostRecentVersionAsSideEffect;
            }

            @Override
            public MobileObservationMetaFixture build() {
                if (duplicateMetadataForMostRecentVersionAsSideEffect) {
                    final int mostRecentMetadataVersion = MOST_RECENT.toIntValue();

                    if (metadata.getMetadataVersion() != mostRecentMetadataVersion) {
                        entitySupplier.newObservationMetadata(metadata.getSpecies(), mostRecentMetadataVersion,
                                metadata.getWithinMooseHunting(), metadata.getObservationType());
                    }
                }

                return new MobileObservationMetaFixture(metadata, entitySupplier);
            }

            @Override
            protected Builder self() {
                return this;
            }
        }
    }

    class ObservationSpecimenFixture {

        public final Person author;
        public final Observation observation;
        public final List<ObservationSpecimen> specimens;

        ObservationSpecimenFixture(@Nonnull final Observation observation,
                                   @Nonnull final List<ObservationSpecimen> specimens) {

            this.observation = Objects.requireNonNull(observation, "observation is null");
            this.author = Objects.requireNonNull(observation.getAuthor());
            this.specimens = Objects.requireNonNull(specimens, "specimens is null");
        }

        public static class Builder<META extends MetaFixtureBase>
                extends AbstractObservationFixtureBuilder<META, ObservationSpecimenFixture> {

            private final int numSpecimensToCreate;
            private Person author;

            public Builder(@Nonnull final META metadataFixture,
                           @Nonnull final EntitySupplier entitySupplier,
                           final int numSpecimensToCreate,
                           final boolean forMobileAPI) {

                super(metadataFixture, entitySupplier, forMobileAPI);
                this.numSpecimensToCreate = numSpecimensToCreate;
            }

            public Builder<META> withAuthor(@Nullable final Person person) {
                this.author = person;
                return this;
            }

            @Override
            public ObservationSpecimenFixture build() {
                final Observation observation = newObservation();

                final List<ObservationSpecimen> specimens =
                        createList(numSpecimensToCreate, () -> entitySupplier.newObservationSpecimen(observation));

                return new ObservationSpecimenFixture(observation, specimens);
            }

            public void consumeBy(@Nonnull final BiConsumer<META, ObservationSpecimenFixture> consumer) {
                Objects.requireNonNull(consumer);
                consumer.accept(metadata, build());
            }

            private Observation newObservation() {
                final Person person = Optional.ofNullable(this.author).orElseGet(entitySupplier::newPerson);

                return forMobileAPI
                        ? entitySupplier.newMobileObservation(person, metadata, numSpecimensToCreate)
                        : entitySupplier.newObservation(person, metadata, numSpecimensToCreate);
            }
        }
    }

}
