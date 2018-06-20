package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextParameters;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.test.TestUtils.createList;

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
                                                                  @Nonnull final ObservationSpecVersion specVersion,
                                                                  @Nullable final Boolean withinMooseHunting,
                                                                  @Nonnull final ObservationType observationType) {

        final Required mooseHuntingReq = withinMooseHunting != null ? Required.YES : Required.NO;
        final EntitySupplier entitySupplier = getEntitySupplier();

        final ObservationMetadata metadata = new ObservationMetadata(
                entitySupplier.newObservationBaseFields(species, mooseHuntingReq, specVersion),
                entitySupplier.newObservationContextSensitiveFields(
                        species, Boolean.TRUE.equals(withinMooseHunting), observationType, specVersion));

        return new ObservationMetaFixture.Builder(metadata, entitySupplier);
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

            public SELF withAmount(@Nonnull final DynamicObservationFieldPresence amount) {
                metadata.withAmount(amount);
                return self();
            }

            public SELF withMooselikeAmountFieldsAs(@Nonnull final Required presence) {
                Objects.requireNonNull(presence);
                metadata.getContextSensitiveFields().setMooselikeMaleAmount(presence);
                metadata.getContextSensitiveFields().setMooselikeFemaleAmount(presence);
                metadata.getContextSensitiveFields().setMooselikeFemale1CalfAmount(presence);
                metadata.getContextSensitiveFields().setMooselikeFemale2CalfsAmount(presence);
                metadata.getContextSensitiveFields().setMooselikeFemale3CalfsAmount(presence);
                metadata.getContextSensitiveFields().setMooselikeFemale4CalfsAmount(presence);
                metadata.getContextSensitiveFields().setMooselikeUnknownSpecimenAmount(presence);

                if (metadata.getSpecVersion().supportsMooselikeCalfAmount()) {
                    metadata.getContextSensitiveFields().setMooselikeCalfAmount(presence);
                }

                return self();
            }

            public SELF withLargeCarnivoreFieldsAs(@Nonnull final DynamicObservationFieldPresence presence) {
                Objects.requireNonNull(presence);
                metadata.getContextSensitiveFields().setVerifiedByCarnivoreAuthority(presence);
                metadata.getContextSensitiveFields().setObserverName(presence);
                metadata.getContextSensitiveFields().setObserverPhoneNumber(presence);
                metadata.getContextSensitiveFields().setOfficialAdditionalInfo(presence);
                metadata.getContextSensitiveFields().setWidthOfPaw(presence);
                metadata.getContextSensitiveFields().setLengthOfPaw(presence);
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

        ObservationMetaFixture(@Nonnull final ObservationMetadata metadata,
                               @Nonnull final EntitySupplier entitySupplier) {

            super(metadata, entitySupplier);
        }

        public ObservationDTOBuilderForTests dtoBuilder(@Nonnull final ObservationContextParameters params) {
            return ObservationDTOBuilderForTests.create(this, params);
        }

        public static class Builder extends MetaFixtureBase.Builder<ObservationMetaFixture, Builder> {

            public Builder(@Nonnull final ObservationMetadata metadata, @Nonnull final EntitySupplier es) {
                super(metadata, es, false);
            }

            public MobileObservationMetaFixture.Builder forMobile() {
                return forMobile(false);
            }

            public MobileObservationMetaFixture.Builder forMobile(final boolean duplicateMetadataForMostRecentVersionAsSideEffect) {
                return new MobileObservationMetaFixture.Builder(
                        metadata, entitySupplier, duplicateMetadataForMostRecentVersionAsSideEffect);
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

        private final ObservationMetadata mostRecentMetadata;

        MobileObservationMetaFixture(@Nonnull final ObservationMetadata metadataUnderTest,
                                     @Nonnull final EntitySupplier entitySupplier) {

            this(metadataUnderTest, metadataUnderTest, entitySupplier);
        }

        MobileObservationMetaFixture(@Nonnull final ObservationMetadata metadataUnderTest,
                                     @Nonnull final ObservationMetadata mostRecentMetadata,
                                     @Nonnull final EntitySupplier entitySupplier) {

            super(metadataUnderTest, entitySupplier);
            this.mostRecentMetadata = mostRecentMetadata;
        }

        public ObservationMetadata getMostRecentMetadata() {
            return mostRecentMetadata;
        }

        public MobileObservationDTOBuilderForTests dtoBuilder(@Nonnull final ObservationContextParameters params) {
            return MobileObservationDTOBuilderForTests.create(this, params);
        }

        public static class Builder extends MetaFixtureBase.Builder<MobileObservationMetaFixture, Builder> {

            private final boolean duplicateMetadataForMostRecentVersionAsSideEffect;
            private Required commonPresenseForAllMooselikeAmountsFields;

            public Builder(@Nonnull final ObservationMetadata metadata,
                           @Nonnull final EntitySupplier es,
                           final boolean duplicateMetadataForMostRecentVersionAsSideEffect) {

                super(metadata, es, true);
                this.duplicateMetadataForMostRecentVersionAsSideEffect =
                        duplicateMetadataForMostRecentVersionAsSideEffect;
            }

            @Override
            public Builder withMooselikeAmountFieldsAs(@Nonnull final Required presence) {
                this.commonPresenseForAllMooselikeAmountsFields = presence;
                return super.withMooselikeAmountFieldsAs(presence);
            }

            @Override
            public MobileObservationMetaFixture build() {
                if (duplicateMetadataForMostRecentVersionAsSideEffect && !metadata.getSpecVersion().isMostRecent()) {
                    final ObservationBaseFields mostRecentBaseFields =
                            entitySupplier.newObservationBaseFields(metadata.getSpecies(), MOST_RECENT);
                    mostRecentBaseFields.setWithinMooseHunting(metadata.getBaseFields().getWithinMooseHunting());

                    final ObservationContextSensitiveFields mostRecentCtxFields =
                            entitySupplier.newObservationContextSensitiveFields(
                                    metadata.getSpecies(),
                                    metadata.getContextSensitiveFields().isWithinMooseHunting(),
                                    metadata.getObservationType(),
                                    MOST_RECENT);

                    metadata.getContextSensitiveFields().copyRequirementsTo(mostRecentCtxFields);

                    // mooselike-calf-amount from old metadata cannot be relied upon as it may not
                    // be supported by it.
                    if (commonPresenseForAllMooselikeAmountsFields != null) {
                        mostRecentCtxFields.setMooselikeCalfAmount(commonPresenseForAllMooselikeAmountsFields);
                    }

                    final ObservationMetadata mostRecentMetadata =
                            new ObservationMetadata(mostRecentBaseFields, mostRecentCtxFields);
                    return new MobileObservationMetaFixture(metadata, mostRecentMetadata, entitySupplier);
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
            this.author = Objects.requireNonNull(observation.getAuthor(), "observation.author is null");
            this.specimens = Objects.requireNonNull(specimens, "specimens is null");
        }

        public static class Builder<META extends MetaFixtureBase>
                extends AbstractObservationFixtureBuilder<META, ObservationSpecimenFixture> {

            private final int numSpecimensToCreate;
            private final ObservationContextParameters params;

            private GeoLocation location;
            private Person author;
            private boolean isCarnivoreAuthority;

            public Builder(@Nonnull final META metadataFixture,
                           @Nonnull final EntitySupplier entitySupplier,
                           final int numSpecimensToCreate,
                           final boolean forMobileAPI) {

                super(metadataFixture, entitySupplier, forMobileAPI);

                this.numSpecimensToCreate = numSpecimensToCreate;
                this.isCarnivoreAuthority = false;
                this.params = new ObservationContextParameters(() -> isCarnivoreAuthority);

                if (numSpecimensToCreate > 0 && !metadata.isAmountLegal(this.isCarnivoreAuthority)) {
                    throw new IllegalStateException("Metadata does not allow specimens to be created");
                }
            }

            public Builder<META> withGeoLocation(@Nonnull final GeoLocation location) {
                this.location = Objects.requireNonNull(location)
                        .withSource(forMobileAPI ? GeoLocation.Source.GPS_DEVICE : GeoLocation.Source.MANUAL);
                return this;
            }

            public Builder<META> withAuthor(@Nullable final Person person) {
                this.author = person;
                return this;
            }

            public Builder<META> withCarnivoreAuthority(final boolean isCarnivoreAuthority) {
                this.isCarnivoreAuthority = isCarnivoreAuthority;
                return this;
            }

            @Override
            public ObservationSpecimenFixture build() {
                final Observation observation = newObservation();

                return new ObservationSpecimenFixture(observation, createList(numSpecimensToCreate, () -> {
                    return entitySupplier.newObservationSpecimen(
                            observation, metadata, params.isUserAssignedCarnivoreAuthority());
                }));
            }

            public void consumeBy(@Nonnull final BiConsumer<META, ObservationSpecimenFixture> consumer) {
                Objects.requireNonNull(consumer);
                consumer.accept(metadata, build());
            }

            private Observation newObservation() {
                final Person person = Optional.ofNullable(author).orElseGet(entitySupplier::newPerson);
                final boolean carnivoreAuthority = params.isUserAssignedCarnivoreAuthority();

                final Observation observation = forMobileAPI
                        ? entitySupplier.newMobileObservation(person, metadata, carnivoreAuthority)
                        : entitySupplier.newObservation(person, metadata, carnivoreAuthority);

                observation.setAmount(numSpecimensToCreate);

                if (location != null) {
                    observation.setGeoLocation(location);
                }

                return observation;
            }
        }
    }
}
