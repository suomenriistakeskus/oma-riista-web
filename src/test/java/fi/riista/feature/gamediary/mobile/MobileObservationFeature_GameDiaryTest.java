package fi.riista.feature.gamediary.mobile;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple;
import org.joda.time.LocalDate;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_CANADIAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_EUROPEAN_BEAVER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_CATEGORY;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertLargeCarnivoreFieldsAreNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertLargeCarnivoreFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooseAmountFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooseAmounts;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmountFieldsAreNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmountFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmounts;
import static fi.riista.feature.gamediary.observation.ObservationType.AANI;
import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.ULOSTE;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.NO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class MobileObservationFeature_GameDiaryTest extends MobileObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    @DataPoints("specVersions")
    public static final ObservationSpecVersion[] SPEC_VERSIONS = ObservationSpecVersion.values();

    @Resource
    protected GameDiaryImageRepository imageRepo;

    @Theory
    public void testCreateObservation_withNormalCategory(final ObservationSpecVersion version) {
        withPerson(author -> {

            // Add test coverage by testing 'amount' field with all Required enums with specified
            // specimens and without them.
            ImmutableMap.of(NAKO, YES, JALKI, VOLUNTARY, ULOSTE, NO).forEach((type, amountReq) -> {

                createObservationMetaF(version, NORMAL, type)
                        .forMobile(true)
                        .withAmount(amountReq)
                        .consumeBy(obsMeta -> {

                            final Integer amount = amountReq == NO ? null : 5;
                            final int numSpecimens = Optional.ofNullable(amount).orElse(0);

                            onSavedAndAuthenticated(createUser(author), () -> {

                                final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                        .withAmount(amount)
                                        .withSpecimens(numSpecimens)
                                        .build();

                                final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                                runInTransaction(() -> {
                                    final Observation observation = assertObservationCreated(outputDto.getId());

                                    assertThat(observation.isFromMobile(), is(true));

                                    final Long mobileClientRefId = observation.getMobileClientRefId();
                                    assertThat(mobileClientRefId, is(notNullValue()));
                                    assertThat(mobileClientRefId, equalTo(inputDto.getMobileClientRefId()));

                                    final GeoLocation geoLocation = observation.getGeoLocation();
                                    assertThat(geoLocation, equalTo(inputDto.getGeoLocation()));
                                    assertThat(geoLocation.getSource(), equalTo(GeoLocation.Source.GPS_DEVICE));

                                    assertThat(observation.getPointOfTime(), equalTo(DateUtil.toDateTimeNullSafe(inputDto.getPointOfTime())));

                                    assertThat(observation.getSpecies().getOfficialCode(), equalTo(inputDto.getGameSpeciesCode()));
                                    assertThat(observation.getObservationCategory(), equalTo(NORMAL));
                                    assertThat(observation.getObservationType(), equalTo(type));

                                    assertThat(observation.getDeerHuntingType(), is(nullValue()));
                                    assertThat(observation.getDeerHuntingTypeDescription(), is(nullValue()));

                                    final String description = observation.getDescription();
                                    assertThat(description, is(notNullValue()));
                                    assertThat(description, equalTo(inputDto.getDescription()));

                                    assertThat(observation.getAmount(), equalTo(amount));

                                    assertAuthorAndActor(observation, F.getId(author), F.getId(author));

                                    assertMooselikeAmountFieldsAreNull(observation);
                                    assertLargeCarnivoreFieldsAreNull(observation);
                                    assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));

                                    final List<ObservationSpecimen> specimens = findSpecimens(observation);

                                    if (amount == null) {
                                        assertThat(specimens, is(empty()));
                                    } else {
                                        assertThat(specimens, hasSize(numSpecimens));
                                        assertSpecimens(specimens, inputDto.getSpecimens(), inputDto.specimenOps()::equalContent);
                                    }

                                    assertThat(imageRepo.findByObservation(observation), is(empty()));
                                });
                            });
                        });
            });
        });
    }

    @Theory
    public void testCreateObservation_withinMooseHunting(final ObservationSpecVersion version) {
        testCreateObservation_withinHunting(MOOSE_HUNTING, version);
    }

    @Theory
    public void testCreateObservation_withinDeerHunting(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_CATEGORY));

        testCreateObservation_withinHunting(DEER_HUNTING, version);
    }

    private void testCreateObservation_withinHunting(final ObservationCategory category,
                                                     final ObservationSpecVersion version) {

        final boolean isDeerCategory = category == DEER_HUNTING;
        checkArgument(isDeerCategory || category == MOOSE_HUNTING, "Expecting hunting category");

        withPerson(author -> {

            createObservationMetaF(version, category, NAKO)
                    .forMobile(true)
                    .withAmount(NO)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .withDeerHuntingTypeFieldsAs(isDeerCategory ? YES : NO, isDeerCategory ? VOLUNTARY : NO)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .mutateMooselikeAmountFields()
                                    .chain(builder -> {
                                        if (isDeerCategory) {
                                            builder.mutateDeerHuntingTypeFields();
                                        }
                                    })
                                    .build();

                            final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());

                                assertThat(observation.isFromMobile(), is(true));

                                final Long mobileClientRefId = observation.getMobileClientRefId();
                                assertThat(mobileClientRefId, is(notNullValue()));
                                assertThat(mobileClientRefId, equalTo(inputDto.getMobileClientRefId()));

                                final GeoLocation geoLocation = observation.getGeoLocation();
                                assertThat(geoLocation, equalTo(inputDto.getGeoLocation()));
                                assertThat(geoLocation.getSource(), equalTo(GeoLocation.Source.GPS_DEVICE));

                                assertThat(observation.getPointOfTime(), equalTo(DateUtil.toDateTimeNullSafe(inputDto.getPointOfTime())));

                                assertThat(observation.getSpecies().getOfficialCode(), equalTo(inputDto.getGameSpeciesCode()));
                                assertThat(observation.getObservationCategory(), equalTo(category));
                                assertThat(observation.getObservationType(), equalTo(NAKO));

                                final DeerHuntingType deerHuntingType = observation.getDeerHuntingType();
                                if (isDeerCategory) {
                                    assertThat(deerHuntingType, is(notNullValue()));
                                    assertThat(deerHuntingType, equalTo(inputDto.getDeerHuntingType()));
                                    assertThat(observation.getDeerHuntingTypeDescription(), equalTo(inputDto.getDeerHuntingTypeDescription()));
                                } else {
                                    assertThat(deerHuntingType, is(nullValue()));
                                    assertThat(observation.getDeerHuntingTypeDescription(), is(nullValue()));
                                }

                                final String description = observation.getDescription();
                                assertThat(description, is(notNullValue()));
                                assertThat(description, equalTo(inputDto.getDescription()));

                                assertAuthorAndActor(observation, F.getId(author), F.getId(author));

                                final boolean calfSupported = version.supportsMooselikeCalfAmount();
                                assertMooselikeAmountFieldsNotNull(observation, calfSupported, isDeerCategory);
                                assertMooselikeAmounts(inputDto, observation, calfSupported, isDeerCategory);
                                assertThat(observation.getAmount(), equalTo(inputDto.getSumOfMooselikeAmounts()));

                                assertLargeCarnivoreFieldsAreNull(observation);
                                assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));

                                assertThat(findSpecimens(observation), hasSize(0));
                            });
                        });
                    });
        });
    }

    @Theory
    public void testCreateObservation_forTranslationOfObsoleteBeaverObservationType(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES));

        withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, PESA_KEKO).forMobile().consumeBy(updatedMeta -> {

                            createObservationMetaF(species, version, PESA).forMobile().consumeBy(oldMeta -> {

                                if (!species.isBeaver()) {
                                    model().newObservationContextSensitiveFields(species, NORMAL, PESA, MOST_RECENT);
                                }

                                onSavedAndAuthenticated(createUser(author), () -> {

                                    final MobileObservationDTO inputDto = oldMeta.dtoBuilder().build();
                                    final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                                    runInTransaction(() -> {
                                        final Observation observation = assertObservationCreated(outputDto.getId());

                                        final ObservationType expectedType = species.isBeaver() ? PESA_KEKO : PESA;
                                        assertThat(observation.getObservationType(), equalTo(expectedType));
                                    });
                                });
                            });
                        });
                    });
        });
    }

    @Theory
    public void testCreateObservation_whenMooseObservedWithinHunting(final ObservationSpecVersion version) {
        withRhy(rhy -> withPerson(author -> {

            createObservationMetaF(OFFICIAL_CODE_MOOSE, version, NAKO)
                    .forMobile(true)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .mutateMooselikeAmountFields()
                                    .build();

                            final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());

                                final boolean calfAmountSupported = version.supportsMooselikeCalfAmount();

                                assertMooseAmountFieldsNotNull(observation, calfAmountSupported);
                                assertMooseAmounts(inputDto, observation, calfAmountSupported);

                                // Total amount field is illegal for mooselike observations in hunting
                                // scenarios because it is automatically calculated based on mooselike amounts.
                                assertThat(observation.getAmount(), equalTo(inputDto.getSumOfMooselikeAmounts()));
                            });
                        });
                    });
        }));
    }

    @Theory
    public void testCreateObservation_forLargeCarnivoreFieldsPersisted(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS));

        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, OccupationType.PETOYHDYSHENKILO);

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .forMobile()
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .withCarnivoreAuthority(true)
                                    .mutateLargeCarnivoreFields()
                                    .withAmountAndSpecimens(5)
                                    .build();

                            final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());

                                assertThat(observation.getSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_WOLF));
                                assertThat(observation.getObservationCategory(), equalTo(NORMAL));
                                assertThat(observation.getObservationType(), equalTo(NAKO));

                                assertLargeCarnivoreFieldsNotNull(observation);

                                assertThat(
                                        observation.getVerifiedByCarnivoreAuthority(),
                                        equalTo(inputDto.getVerifiedByCarnivoreAuthority()));
                                assertThat(observation.getObserverName(), equalTo(inputDto.getObserverName()));
                                assertThat(
                                        observation.getObserverPhoneNumber(),
                                        equalTo(inputDto.getObserverPhoneNumber()));
                                assertThat(
                                        observation.getOfficialAdditionalInfo(),
                                        equalTo(inputDto.getOfficialAdditionalInfo()));
                            });
                        });
                    });
        }));
    }

    @Theory
    public void testUpdateObservation_smoke(final ObservationSpecVersion version) {
        withRhy(rhy -> withPerson(author -> {

            final ObservationType obsType = some(ObservationType.class);

            // Add test coverage by varying the voluntary "amount" field and number of specimens.
            // Tuple consists of (total amount of specimens / number of defined specimens).

            Stream.of(Tuple.of(5, 2), Tuple.of(3, 0), Tuple.<Integer, Integer>of(null, null)).forEach(tup -> {

                createObservationMetaF(version, obsType)
                        .forMobile(true)
                        .withAmount(VOLUNTARY)
                        .createObservationFixture()
                        .withAuthor(author)
                        .consumeBy((obsMeta, obsFixt) -> {

                            final Observation original = obsFixt.observation;

                            onSavedAndAuthenticated(createUser(author), () -> {

                                final Integer newAmount = tup._1;
                                final Integer newNumSpecimens = tup._2;

                                final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                        .populateWith(original)
                                        .mutate()
                                        .withAmount(newAmount)
                                        .chain(builder -> Optional
                                                .ofNullable(newNumSpecimens).ifPresent(builder::withSpecimens))
                                        .build();

                                invokeUpdateObservation(dto);

                                runInTransaction(() -> {
                                    final Observation updated = observationRepo.getOne(dto.getId());

                                    assertVersion(updated, 1);
                                    assertThat(updated.getMobileClientRefId(), is(notNullValue()));
                                    assertThat(updated.isFromMobile(), is(true));

                                    final GeoLocation updatedLocation = updated.getGeoLocation();
                                    assertThat(updatedLocation, equalTo(dto.getGeoLocation()));
                                    assertThat(updatedLocation.getSource(), equalTo(GeoLocation.Source.GPS_DEVICE));
                                    assertThat(updated.getRhy(), equalTo(rhy));

                                    assertThat(updated.getPointOfTime(), equalTo(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                                    assertThat(updated.getSpecies().getOfficialCode(), equalTo(obsMeta.getGameSpeciesCode()));
                                    assertThat(updated.getObservationCategory(), equalTo(NORMAL));
                                    assertThat(updated.getObservationType(), equalTo(obsType));

                                    assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                                    final String description = updated.getDescription();
                                    assertThat(description, is(notNullValue()));
                                    assertThat(description, equalTo(dto.getDescription()));

                                    assertThat(updated.getAmount(), equalTo(newAmount));

                                    assertMooselikeAmountFieldsAreNull(updated);
                                    assertThat(updated.getHuntingDayOfGroup(), is(nullValue()));
                                    assertLargeCarnivoreFieldsAreNull(updated);

                                    final List<ObservationSpecimen> updatedSpecimens = findSpecimens(original);

                                    if (newNumSpecimens == null) {
                                        assertThat(updatedSpecimens, hasSize(0));
                                    } else {
                                        assertThat(updatedSpecimens, hasSize(newNumSpecimens));
                                        assertSpecimens(updatedSpecimens, dto.getSpecimens(), dto.specimenOps()::equalContent);
                                    }
                                });
                            });
                        });
            });
        }));
    }

    @Theory
    public void testUpdateObservation_whenNothingChanged(final ObservationSpecVersion version) {
        withPerson(author -> {

            createObservationMetaF(version, NAKO)
                    .forMobile(true)
                    .withAmount(YES)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(obsFixt.observation)
                                    .populateSpecimensWith(obsFixt.specimens)
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 0);

                                // Specimens amount should not be changed.
                                final List<ObservationSpecimen> actualSpecimens = findSpecimens(updated);
                                assertThat(actualSpecimens, hasSize(5));

                                // In order to assert that specimens are unaffected it is checked that the content
                                // of specimen DTOs match with both original and updated specimen entities.

                                final ObservationSpecimenOps specimenOps = dto.specimenOps();

                                assertSpecimens(obsFixt.specimens, dto.getSpecimens(), specimenOps::equalContent);
                                assertSpecimens(actualSpecimens, dto.getSpecimens(), specimenOps::equalContent);
                            });
                        });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_whenUpdatingSpecimensOnly(final ObservationSpecVersion version) {
        withPerson(author -> {

            createObservationMetaF(version, NAKO)
                    .forMobile(true)
                    .withAmount(YES)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final Observation original = obsFixt.observation;

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(original)
                                    .populateSpecimensWith(obsFixt.specimens)
                                    .mutateSpecimens()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                // Assert that observation fields are NOT changed even though version was updated
                                // because of specimen changes.

                                assertThat(updated.getGeoLocation(), equalTo(original.getGeoLocation()));
                                assertThat(updated.getPointOfTime(), equalTo(original.getPointOfTime()));

                                assertThat(updated.getSpecies().getOfficialCode(), equalTo(obsMeta.getGameSpeciesCode()));
                                assertThat(updated.getObservationCategory(), equalTo(NORMAL));
                                assertThat(updated.getObservationType(), equalTo(NAKO));

                                assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                                assertThat(updated.getDescription(), equalTo(original.getDescription()));
                                assertThat(updated.getAmount(), equalTo(5));

                                // Finally assert that specimens are changed.

                                final List<ObservationSpecimen> updatedSpecimens = findSpecimens(updated);
                                assertThat(updatedSpecimens, hasSize(5));
                                assertSpecimens(updatedSpecimens, dto.getSpecimens(), dto.specimenOps()::equalContent);
                            });
                        });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_forTranslationOfObsoleteBeaverObservationType(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES));

        withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, PESA_KEKO).forMobile().consumeBy(updatedMeta -> {

                            createObservationMetaF(species, version, PESA)
                                    .forMobile()
                                    .createObservationFixture()
                                    .withAuthor(author)
                                    .consumeBy((oldMeta, obsFixt) -> {

                                        final boolean isBeaver = species.isBeaver();

                                        if (!isBeaver) {
                                            model().newObservationContextSensitiveFields(species, NORMAL, PESA, MOST_RECENT);
                                        }

                                        onSavedAndAuthenticated(createUser(author), () -> {

                                            final MobileObservationDTO dto = oldMeta.dtoBuilder()
                                                    .populateWith(obsFixt.observation)
                                                    .mutate()
                                                    .build();

                                            invokeUpdateObservation(dto);

                                            runInTransaction(() -> {
                                                final Observation updated = observationRepo.getOne(dto.getId());
                                                assertVersion(updated, 1);

                                                final ObservationType expectedType = isBeaver ? PESA_KEKO : PESA;
                                                assertThat(updated.getObservationType(), equalTo(expectedType));

                                                // Assert that some other field is updated as well.
                                                assertThat(updated.getGeoLocation(), equalTo(dto.getGeoLocation()));
                                            });
                                        });
                                    });
                        });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_updatedObservationTypeShouldNotBeReplacedWithDefaultTranslation(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_XTRA_BEAVER_TYPES));

        withPerson(author -> {

            Stream.of(OFFICIAL_CODE_CANADIAN_BEAVER, OFFICIAL_CODE_EUROPEAN_BEAVER, OFFICIAL_CODE_BEAR)
                    .forEach(speciesCode -> {

                        final GameSpecies species = model().newGameSpecies(speciesCode);

                        createObservationMetaF(species, PESA_PENKKA)
                                .forMobile()
                                .createObservationFixture()
                                .withAuthor(author)
                                .consumeBy((currentMeta, obsFixt) -> {

                                    createObservationMetaF(species, version, PESA).forMobile().consumeBy(oldMeta -> {

                                        final boolean isBeaver = species.isBeaver();

                                        if (!isBeaver) {
                                            model().newObservationContextSensitiveFields(species, NORMAL, PESA, MOST_RECENT);
                                        }

                                        onSavedAndAuthenticated(createUser(author), () -> {

                                            final MobileObservationDTO dto = oldMeta.dtoBuilder()
                                                    .populateWith(obsFixt.observation)
                                                    .mutate()
                                                    .build();

                                            invokeUpdateObservation(dto);

                                            runInTransaction(() -> {
                                                final Observation updated = observationRepo.getOne(dto.getId());
                                                assertVersion(updated, 1);

                                                final ObservationType expectedType = isBeaver ? PESA_PENKKA : PESA;
                                                assertThat(updated.getObservationType(), equalTo(expectedType));

                                                // Assert that some other field is updated as well.
                                                assertThat(updated.getDescription(), equalTo(dto.getDescription()));
                                            });
                                        });
                                    });
                                });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_forAmountChange_whenSpecimensPresent(final ObservationSpecVersion version) {
        withPerson(author -> {

            createObservationMetaF(version, NAKO)
                    .forMobile(true)
                    .withAmount(YES)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(obsFixt.observation)
                                    .populateSpecimensWith(obsFixt.specimens)
                                    .withAmount(obsFixt.specimens.size() + 10)
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                // Amount should be increased by ten.
                                assertThat(updated.getAmount(), equalTo(5 + 10));

                                // Specimens amount should not be changed.
                                final List<ObservationSpecimen> actualSpecimens = findSpecimens(updated);
                                assertThat(actualSpecimens, hasSize(5));

                                // In order to assert that specimens are unaffected it is verified that the content
                                // of specimen DTOs match with both original and updated specimen entities.

                                final ObservationSpecimenOps specimenOps = dto.specimenOps();

                                assertSpecimens(obsFixt.specimens, dto.getSpecimens(), specimenOps::equalContent);
                                assertSpecimens(actualSpecimens, dto.getSpecimens(), specimenOps::equalContent);
                            });
                        });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_forAmountChange_whenSpecimensAbsent(final ObservationSpecVersion version) {
        withPerson(author -> {

            createObservationMetaF(version, NAKO)
                    .forMobile(true)
                    .withAmount(YES)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(obsFixt.observation)
                                    .withAmount(10)
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);
                                assertThat(updated.getAmount(), equalTo(10));
                                assertThat(findSpecimens(updated), hasSize(0));
                            });
                        });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_whenChangingToObservationTypeNotAcceptingAmount(final ObservationSpecVersion version) {
        withPerson(author -> {

            createObservationMetaF(version, NAKO)
                    .forMobile(true)
                    .withAmount(YES)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .consumeBy(obsFixt -> {

                        createObservationMetaF(version, AANI)
                                .forMobile(true)
                                .withAmount(NO)
                                .consumeBy(newMeta -> {

                                    onSavedAndAuthenticated(createUser(author), () -> {

                                        // Assert test invariant.
                                        assertThat(findSpecimens(obsFixt.observation), hasSize(5));

                                        final MobileObservationDTO dto = newMeta.dtoBuilder()
                                                .populateWith(obsFixt.observation)
                                                .mutate()
                                                .withAmount(null)
                                                .build();

                                        invokeUpdateObservation(dto);

                                        runInTransaction(() -> {
                                            final Observation updated = observationRepo.getOne(dto.getId());
                                            assertVersion(updated, 1);
                                            assertThat(updated.getAmount(), is(nullValue()));
                                            assertThat(updated.getObservationType(), equalTo(AANI));
                                            assertThat(findSpecimens(updated), hasSize(0));
                                        });
                                    });
                                });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard(final ObservationSpecVersion version) {
        withMooseHuntingGroupFixture(huntingFixt -> {

            huntingFixt.group.setFromMooseDataCard(true);

            final GameSpecies moose = huntingFixt.species;
            final Person author = huntingFixt.groupMember;

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixt.group, today());

            createObservationMetaF(moose, version, MOOSE_HUNTING, NAKO)
                    .forMobile()
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withAuthor(author)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        final Observation original = obsFixt.observation;

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(original)
                                    .mutate()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                // Assert that description is changed.
                                assertThat(updated.getDescription(), equalTo(dto.getDescription()));

                                // Assert that other observation fields are NOT changed.

                                assertThat(updated.getGeoLocation(), equalTo(original.getGeoLocation()));
                                assertThat(updated.getPointOfTime(), equalTo(original.getPointOfTime()));

                                final boolean calfAmountSupported = version.supportsMooselikeCalfAmount();

                                assertMooseAmountFieldsNotNull(updated, calfAmountSupported);
                                assertMooseAmounts(original, updated, calfAmountSupported);

                                // Total amount field is illegal for mooselike observations in hunting
                                // scenarios because it is automatically calculated based on mooselike amounts.
                                assertThat(updated.getAmount(), equalTo(original.getSumOfMooselikeAmounts()));
                            });
                        });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_forLargeCarnivoreFieldsMutated(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS));

        withRhy(rhy -> withPerson(author -> {

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .forMobile()
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .withCarnivoreAuthorityEnabled()
                    .consumeBy((obsMeta, obsFixt) -> {

                        model().newOccupation(rhy, author, OccupationType.PETOYHDYSHENKILO);

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .withCarnivoreAuthority(true)
                                    .populateWith(obsFixt.observation)
                                    .populateSpecimensWith(obsFixt.specimens)
                                    .mutateLargeCarnivoreFields()
                                    .mutateSpecimens()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersionOneOf(updated, 1, 2);

                                assertLargeCarnivoreFieldsNotNull(updated);

                                assertThat(
                                        updated.getVerifiedByCarnivoreAuthority(),
                                        equalTo(dto.getVerifiedByCarnivoreAuthority()));
                                assertThat(updated.getObserverName(), equalTo(dto.getObserverName()));
                                assertThat(updated.getObserverPhoneNumber(), equalTo(dto.getObserverPhoneNumber()));
                                assertThat(
                                        updated.getOfficialAdditionalInfo(),
                                        equalTo(dto.getOfficialAdditionalInfo()));

                                final List<ObservationSpecimen> updatedSpecimens = findSpecimens(updated);
                                assertThat(updatedSpecimens, hasSize(5));
                                assertSpecimens(updatedSpecimens, dto.getSpecimens(), dto.specimenOps()::equalContent);
                            });
                        });
                    });
        }));
    }

    @Theory
    public void testUpdateObservation_forLargeCarnivore_carnivoreFieldsNotMutatedWhenAuthorityExpired(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS));

        withRhy(rhy -> withPerson(author -> {

            final LocalDate today = today();
            model().newOccupation(rhy, author, PETOYHDYSHENKILO, today.minusYears(1), today.minusWeeks(1));

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .forMobile()
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .createSpecimenFixture(3)
                    .withAuthor(author)
                    .withCarnivoreAuthorityEnabled()
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final Observation original = obsFixt.observation;

                            // Assert test invariant.
                            assertLargeCarnivoreFieldsNotNull(original);

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .withCarnivoreAuthority(false)
                                    .populateWith(original)
                                    .populateSpecimensWith(obsFixt.specimens)
                                    .withDescription("xyz" + nextPositiveInt())
                                    .mutateSpecimens()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                // Assert that description is changed.
                                assertThat(updated.getDescription(), equalTo(dto.getDescription()));

                                assertLargeCarnivoreFieldsNotNull(updated);

                                // Assert that large carnivore fields are NOT mutated i.e. they have same
                                // values than original observation.

                                assertThat(
                                        updated.getVerifiedByCarnivoreAuthority(),
                                        equalTo(original.getVerifiedByCarnivoreAuthority()));
                                assertThat(updated.getObserverName(), equalTo(original.getObserverName()));
                                assertThat(
                                        updated.getObserverPhoneNumber(),
                                        equalTo(original.getObserverPhoneNumber()));
                                assertThat(
                                        updated.getOfficialAdditionalInfo(),
                                        equalTo(original.getOfficialAdditionalInfo()));

                                // Specimens should NOT be changed. This is asserted by comparing actual
                                // specimens (in database) to expected DTO results transformed from original
                                // specimens.

                                final List<ObservationSpecimen> actualSpecimens = findSpecimens(updated);
                                final ObservationSpecimenOps specimenOps = dto.specimenOps();

                                final List<ObservationSpecimenDTO> expectedConversions =
                                        specimenOps.transformList(obsFixt.specimens);

                                assertThat(actualSpecimens, hasSize(3));
                                assertSpecimens(actualSpecimens, expectedConversions, specimenOps::equalContent);
                            });
                        });
                    });
        }));
    }

    @Theory
    public void testUpdateObservation_forLargeCarnivoreFieldsNotNulledWhenClientDoesNotSupportThem(final ObservationSpecVersion version) {
        assumeTrue(version.lessThan(LOWEST_VERSION_SUPPORTING_LARGE_CARNIVORE_FIELDS));

        withRhy(rhy -> withPerson(author -> {

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .forMobile()
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .withCarnivoreAuthorityEnabled()
                    .consumeBy((newMeta, obsFixt) -> {

                        model().newOccupation(rhy, author, OccupationType.PETOYHDYSHENKILO);

                        createObservationMetaF(newMeta.getSpecies(), version, NAKO)
                                .forMobile()
                                .withAmount(YES)
                                .consumeBy(oldMeta -> {

                                    onSavedAndAuthenticated(createUser(author), () -> {

                                        final Observation original = obsFixt.observation;

                                        final MobileObservationDTO dto = oldMeta.dtoBuilder()
                                                .withCarnivoreAuthority(true)
                                                .populateWith(original)
                                                .populateSpecimensWith(obsFixt.specimens)
                                                .mutate()
                                                .mutateSpecimens()
                                                .build();

                                        invokeUpdateObservation(dto);

                                        runInTransaction(() -> {
                                            final Observation updated = observationRepo.getOne(dto.getId());
                                            assertVersionOneOf(updated, 1, 2);

                                            assertLargeCarnivoreFieldsNotNull(updated);

                                            final List<ObservationSpecimen> actualSpecimens = findSpecimens(updated);
                                            assertThat(actualSpecimens, hasSize(5));

                                            actualSpecimens.forEach(specimen -> {
                                                assertThat(specimen.getWidthOfPaw(), is(notNullValue()));
                                                assertThat(specimen.getLengthOfPaw(), is(notNullValue()));
                                            });
                                        });
                                    });
                                });
                    });
        }));
    }

    @Theory
    public void testUpdateObservation_forLargeCarnivoreFieldsNulledWhenSpeciesChangedToNonCarnivore(final ObservationSpecVersion version) {
        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, OccupationType.PETOYHDYSHENKILO);

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .forMobile()
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .createSpecimenFixture(5)
                    .withAuthor(author)
                    .withCarnivoreAuthorityEnabled()
                    .consumeBy(wolfFixt -> {

                        createObservationMetaF(OFFICIAL_CODE_MOOSE, version, NAKO)
                                .forMobile(true)
                                .withAmount(YES)
                                .withLargeCarnivoreFieldsAs(NO)
                                .consumeBy(mooseMeta -> {

                                    onSavedAndAuthenticated(createUser(author), () -> {

                                        final MobileObservationDTO dto = mooseMeta.dtoBuilder()
                                                .withCarnivoreAuthority(true)
                                                .populateWith(wolfFixt.observation)
                                                .populateSpecimensWith(wolfFixt.specimens)
                                                .mutate()
                                                .mutateSpecimens()
                                                .build();

                                        invokeUpdateObservation(dto);

                                        runInTransaction(() -> {
                                            final Observation updated = observationRepo.getOne(dto.getId());
                                            assertVersion(updated, 2);

                                            assertLargeCarnivoreFieldsAreNull(updated);

                                            // Assert that some other changes are persisted as well.

                                            assertThat(updated.getGeoLocation(), equalTo(dto.getGeoLocation()));

                                            final List<ObservationSpecimen> actualSpecimens = findSpecimens(updated);
                                            assertThat(actualSpecimens, hasSize(5));
                                            assertSpecimens(actualSpecimens, dto.getSpecimens(), dto.specimenOps()::equalContent);
                                        });
                                    });
                                });
                    });
        }));
    }

    @Theory
    public void testUpdateObservation_whenMooseObservedWithinHunting(final ObservationSpecVersion version) {
        withRhy(rhy -> withPerson(author -> {

            createObservationMetaF(OFFICIAL_CODE_MOOSE, version, NAKO)
                    .forMobile(true)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(obsMeta -> {

                        final Observation observation =
                                model().newMobileObservation(author, obsMeta.getMostRecentMetadata());

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(observation)
                                    .mutateMooselikeAmountFields()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                final boolean calfAmountSupported = version.supportsMooselikeCalfAmount();

                                assertMooseAmountFieldsNotNull(updated, calfAmountSupported);
                                assertMooseAmounts(dto, updated, calfAmountSupported);

                                // Total amount field is illegal for mooselike observations in hunting
                                // scenarios because it is automatically calculated based on mooselike amounts.
                                assertThat(updated.getAmount(), equalTo(dto.getSumOfMooselikeAmounts()));
                            });
                        });
                    });
        }));
    }

    @Theory
    public void testDeleteObservation_whenNotAttachedToHuntingDay(final ObservationSpecVersion version) {
        withPerson(author -> {

            createObservationMetaF(version, NORMAL, NAKO)
                    .forMobile()
                    .withAmount(YES)
                    .createSpecimenFixture(1)
                    .withAuthor(author)
                    .consumeBy(obsFixt -> {

                        onSavedAndAuthenticated(createUser(author), () -> {
                            final Long id = obsFixt.observation.getId();
                            feature.deleteObservation(id);
                            assertThat(observationRepo.findById(id).isPresent(), is(false));
                        });
                    });
        });
    }

    @Theory
    public void testDeleteObservation_whenAttachedToHuntingDay(final ObservationSpecVersion version) {
        withMooseHuntingGroupFixture(huntingFixt -> {

            final Person author = huntingFixt.groupMember;
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixt.group, today());

            createObservationMetaF(version, MOOSE_HUNTING, NAKO)
                    .forMobile()
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withAuthor(author)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy(obsFixt -> {

                        onSavedAndAuthenticated(createUser(author), () -> {
                            final Long observationId = obsFixt.observation.getId();

                            assertThrows("Deletion of observation associated with a hunting day should fail",
                                    RuntimeException.class,
                                    () -> feature.deleteObservation(observationId));

                            assertThat(observationRepo.findById(observationId).isPresent(), is(true));
                        });
                    });
        });
    }
}
