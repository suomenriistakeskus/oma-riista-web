package fi.riista.feature.gamediary.fixture;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.common.entity.RequiredWithinDeerPilot;
import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence;
import fi.riista.feature.gamediary.observation.metadata.ObservationBaseFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.test.TestUtils.createList;
import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ObservationFixtureMixin extends FixtureMixin {

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(getEntitySupplier().nextPositiveInt(), version, NORMAL, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final ObservationCategory observationCategory,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(MOST_RECENT, observationCategory, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(gameSpeciesCode, MOST_RECENT, NORMAL, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(gameSpeciesCode, version, NORMAL, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationCategory observationCategory,
                                                                  @Nonnull final ObservationType observationType) {

        final int gameSpeciesCode = getEntitySupplier().nextPositiveInt();

        return createObservationMetaF(gameSpeciesCode, version, observationCategory, observationType);

    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nonnull final ObservationCategory observationCategory,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(gameSpeciesCode, MOST_RECENT, observationCategory, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(final int gameSpeciesCode,
                                                                  @Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationCategory observationCategory,
                                                                  @Nonnull final ObservationType observationType) {

        final GameSpecies species = getEntitySupplier().newGameSpecies(gameSpeciesCode);

        return createObservationMetaF(species, version, observationCategory, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(species, MOST_RECENT, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationSpecVersion version,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(species, version, NORMAL, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationCategory observationCategory,
                                                                  @Nonnull final ObservationType observationType) {

        return createObservationMetaF(species, MOST_RECENT, observationCategory, observationType);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationSpecVersion specVersion,
                                                                  @Nonnull final ObservationCategory observationCategory,
                                                                  @Nonnull final ObservationType observationType) {

        final Required mooseHuntingReq = observationCategory.isWithinMooseHunting() ? Required.YES : Required.NO;

        return createObservationMetaF(species, specVersion, observationCategory, observationType, mooseHuntingReq);
    }

    default ObservationMetaFixture.Builder createObservationMetaF(@Nonnull final GameSpecies species,
                                                                  @Nonnull final ObservationSpecVersion specVersion,
                                                                  @Nonnull final ObservationCategory observationCategory,
                                                                  @Nonnull final ObservationType observationType,
                                                                  @Nonnull final Required mooseHuntingReq) {

        final RequiredWithinDeerPilot deerHuntingReq =
                observationCategory.isWithinDeerHunting() ? RequiredWithinDeerPilot.YES : RequiredWithinDeerPilot.NO;

        final EntitySupplier entitySupplier = getEntitySupplier();

        final ObservationMetadata metadata = new ObservationMetadata(
                entitySupplier.newObservationBaseFields(species, mooseHuntingReq, deerHuntingReq, specVersion),
                entitySupplier.newObservationContextSensitiveFields(
                        species, observationCategory, observationType, specVersion));

        // By default, disallow amount field and require explicit setting in tests for clarity.
        metadata.getContextSensitiveFields().setAmount(DynamicObservationFieldPresence.NO);

        return new ObservationMetaFixture.Builder(metadata, entitySupplier);
    }

    abstract class MetaFixtureBase extends ObservationMetadata {

        protected final EntitySupplier entitySupplier;

        MetaFixtureBase(@Nonnull final ObservationMetadata metadata, @Nonnull final EntitySupplier entitySupplier) {
            super(metadata.getBaseFields(), metadata.getContextSensitiveFields());

            this.entitySupplier = requireNonNull(entitySupplier);
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
                requireNonNull(amount);
                metadata.getContextSensitiveFields().setAmount(amount);
                return self();
            }

            public SELF withDeerHuntingTypeFieldsAs(@Nonnull final DynamicObservationFieldPresence deerHuntingType,
                                                    @Nonnull final DynamicObservationFieldPresence deerHuntingTypeDescription) {

                requireNonNull(deerHuntingType);
                requireNonNull(deerHuntingTypeDescription);

                final ObservationContextSensitiveFields ctxFields = metadata.getContextSensitiveFields();

                ctxFields.setDeerHuntingType(deerHuntingType);
                ctxFields.setDeerHuntingTypeDescription(deerHuntingTypeDescription);

                return self();
            }

            public SELF withMooselikeAmountFieldsAs(@Nonnull final Required presence) {
                requireNonNull(presence);

                final ObservationContextSensitiveFields ctxFields = metadata.getContextSensitiveFields();

                ctxFields.setMooselikeMaleAmount(presence);
                ctxFields.setMooselikeFemaleAmount(presence);
                ctxFields.setMooselikeFemale1CalfAmount(presence);
                ctxFields.setMooselikeFemale2CalfsAmount(presence);
                ctxFields.setMooselikeFemale3CalfsAmount(presence);
                ctxFields.setMooselikeFemale4CalfsAmount(presence);
                ctxFields.setMooselikeUnknownSpecimenAmount(presence);

                if (metadata.getSpecVersion().supportsMooselikeCalfAmount()) {
                    ctxFields.setMooselikeCalfAmount(presence);
                }

                // Disable specimen specific fields.
                ctxFields.setExtendedAgeRange(false);
                ctxFields.setAmount(DynamicObservationFieldPresence.NO);
                ctxFields.setGender(Required.NO);
                ctxFields.setAge(Required.NO);
                ctxFields.setWounded(Required.NO);
                ctxFields.setDead(Required.NO);
                ctxFields.setOnCarcass(Required.NO);
                ctxFields.setCollarOrRadioTransmitter(Required.NO);
                ctxFields.setLegRingOrWingMark(Required.NO);
                ctxFields.setEarMark(Required.NO);

                return self();
            }

            public SELF withLargeCarnivoreFieldsAs(@Nonnull final DynamicObservationFieldPresence presence) {
                requireNonNull(presence);

                final ObservationContextSensitiveFields ctxFields = metadata.getContextSensitiveFields();

                ctxFields.setVerifiedByCarnivoreAuthority(presence);
                ctxFields.setObserverName(presence);
                ctxFields.setObserverPhoneNumber(presence);
                ctxFields.setOfficialAdditionalInfo(presence);
                ctxFields.setWidthOfPaw(presence);
                ctxFields.setLengthOfPaw(presence);

                return self();
            }

            public ObservationSpecimenFixture.Builder<T> createObservationFixture() {
                // Specimens not allowed within real world deer hunting scenarios => hence zero specimens
                return createSpecimenFixture(0);
            }

            public ObservationSpecimenFixture.Builder<T> createSpecimenFixture(final int numSpecimens) {
                return new ObservationSpecimenFixture.Builder<>(build(), entitySupplier, numSpecimens, forMobileAPI);
            }
        }
    }

    class ObservationMetaFixture extends MetaFixtureBase {

        ObservationMetaFixture(@Nonnull final ObservationMetadata metadata,
                               @Nonnull final EntitySupplier entitySupplier) {

            super(metadata, entitySupplier);
        }

        public ObservationDTOBuilderForTests dtoBuilder() {
            return ObservationDTOBuilderForTests.create(this);
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

        public MobileObservationDTOBuilderForTests dtoBuilder() {
            return MobileObservationDTOBuilderForTests.create(this);
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
                                    metadata.getContextSensitiveFields().getObservationCategory(),
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

            this.observation = requireNonNull(observation, "observation is null");
            this.author = requireNonNull(observation.getAuthor(), "observation.author is null");
            this.specimens = requireNonNull(specimens, "specimens is null");
        }

        public static class Builder<META extends MetaFixtureBase>
                extends AbstractObservationFixtureBuilder<META, ObservationSpecimenFixture> {

            private final int numSpecimensToCreate;

            private GeoLocation location;

            private Person author;
            private boolean isCarnivoreAuthority;

            private DeerHuntingType deerHuntingType;
            private GroupHuntingDay huntingDay;

            public Builder(@Nonnull final META metadataFixture,
                           @Nonnull final EntitySupplier entitySupplier,
                           final int numSpecimensToCreate,
                           final boolean forMobileAPI) {

                super(metadataFixture, entitySupplier, forMobileAPI);

                this.numSpecimensToCreate = numSpecimensToCreate;
                this.isCarnivoreAuthority = false;

                if (numSpecimensToCreate > 0 && !metadata.isAmountLegal(this.isCarnivoreAuthority)) {
                    throw new IllegalStateException("Metadata does not allow specimens to be created");
                }
            }

            public Builder<META> withGeoLocation(@Nonnull final GeoLocation location) {
                this.location = requireNonNull(location)
                        .withSource(forMobileAPI ? GeoLocation.Source.GPS_DEVICE : GeoLocation.Source.MANUAL);
                return this;
            }

            public Builder<META> withAuthor(@Nullable final Person person) {
                this.author = person;
                return this;
            }

            public Builder<META> withDeerHuntingType(@Nullable final DeerHuntingType deerHuntingType) {
                this.deerHuntingType = deerHuntingType;
                return this;
            }

            public Builder<META> withGroupHuntingDay(@Nullable final GroupHuntingDay huntingDay) {
                this.huntingDay = huntingDay;
                return this;
            }

            public Builder<META> withCarnivoreAuthorityEnabled() {
                this.isCarnivoreAuthority = true;
                return this;
            }

            @Override
            public ObservationSpecimenFixture build() {
                final Observation observation = newObservation();

                return new ObservationSpecimenFixture(observation, createList(numSpecimensToCreate, () -> {
                    return entitySupplier.newObservationSpecimen(observation, metadata, isCarnivoreAuthority);
                }));
            }

            public void consumeBy(@Nonnull final BiConsumer<META, ObservationSpecimenFixture> consumer) {
                requireNonNull(consumer);
                consumer.accept(metadata, build());
            }

            private Observation newObservation() {
                final Person person = Optional.ofNullable(author).orElseGet(entitySupplier::newPerson);

                final Observation observation = forMobileAPI
                        ? entitySupplier.newMobileObservation(person, metadata, isCarnivoreAuthority)
                        : entitySupplier.newObservation(person, metadata, isCarnivoreAuthority);

                if (location != null) {
                    observation.setGeoLocation(location);
                }

                if (deerHuntingType != null) {
                    observation.setDeerHuntingType(deerHuntingType);
                }

                if (numSpecimensToCreate > 0) {
                    observation.setAmount(numSpecimensToCreate);
                }

                if (huntingDay != null) {
                    observation.updateHuntingDayOfGroup(huntingDay, person);
                }

                return observation;
            }
        }
    }
}
