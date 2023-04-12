package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.error.ProhibitedFieldFound;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenOps;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.DeerHuntingType.DOG_HUNTING;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertLargeCarnivoreFieldsAreNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertLargeCarnivoreFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooseAmountFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmountFieldsAreNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmountFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmounts;
import static fi.riista.feature.gamediary.observation.ObservationType.AANI;
import static fi.riista.feature.gamediary.observation.ObservationType.MUUTON_AIKAINEN_LEPAILYALUE;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.ObservationType.RIISTAKAMERA;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.NO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

public class ObservationFeature_GameDiaryTest extends ObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    private final long twoDaysInMilliseconds = 172800000;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Resource
    private DeletedObservationRepository deletedObservationRepository;

    @Before
    public void setUp() {
        harvestPermitLockedByDateService.disableLockingForTests();
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
        harvestPermitLockedByDateService.normalLocking();
    }

    @Test
    public void testCreateObservation_withNormalCategory() {
        withRhy(rhy -> withPerson(author -> createObservationMetaF(NORMAL, NAKO).withAmount(YES).consumeBy(obsMeta -> {

            onSavedAndAuthenticated(createUser(author), () -> {
                final ObservationDTO inputDto = obsMeta.dtoBuilder()
                        .withAmountAndSpecimens(3)
                        .build();
                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                runInTransaction(() -> {
                    final Observation observation = assertObservationCreated(outputDto.getId());

                    assertThat(observation.getMobileClientRefId(), is(nullValue()));
                    assertThat(observation.isFromMobile(), is(false));

                    final GeoLocation geoLocation = observation.getGeoLocation();
                    assertThat(geoLocation, equalTo(inputDto.getGeoLocation()));
                    assertThat(geoLocation.getSource(), equalTo(GeoLocation.Source.MANUAL));
                    assertThat(observation.getRhy(), equalTo(rhy));

                    assertThat(observation.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(inputDto.getPointOfTime())));

                    assertThat(observation.getSpecies().getOfficialCode(), equalTo(inputDto.getGameSpeciesCode()));
                    assertThat(observation.getObservationCategory(), equalTo(NORMAL));
                    assertThat(observation.getObservationType(), equalTo(NAKO));

                    final String description = observation.getDescription();
                    assertThat(description, is(notNullValue()));
                    assertThat(description, equalTo(inputDto.getDescription()));

                    assertThat(observation.getAmount(), equalTo(3));

                    assertAuthorAndActor(observation, F.getId(author), F.getId(author));

                    assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));
                    assertMooselikeAmountFieldsAreNull(observation);
                    assertLargeCarnivoreFieldsAreNull(observation);

                    final List<ObservationSpecimen> specimens = findSpecimens(observation);
                    assertThat(specimens, hasSize(3));
                    assertSpecimens(specimens, inputDto.getSpecimens(), inputDto.specimenOps()::equalContent);
                });
            });
        })));
    }

    @Test
    public void testCreateObservation_withinMooseHunting() {
        testCreateObservation_withinHunting(MOOSE_HUNTING);
    }

    @Test
    public void testCreateObservation_withinDeerHunting() {
        testCreateObservation_withinHunting(DEER_HUNTING);
    }

    private void testCreateObservation_withinHunting(final ObservationCategory category) {
        final boolean isDeerCategory = category == DEER_HUNTING;
        checkArgument(isDeerCategory || category == MOOSE_HUNTING, "Expecting hunting category");

        withRhy(rhy -> withPerson(author -> {

            createObservationMetaF(category, NAKO)
                    .withDeerHuntingTypeFieldsAs(isDeerCategory ? YES : NO, isDeerCategory ? VOLUNTARY : NO)
                    .withAmount(NO)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .mutateMooselikeAmountFields()
                                    .chain(builder -> {
                                        if (isDeerCategory) {
                                            builder.mutateDeerHuntingTypeFields();
                                        }
                                    })
                                    .build();

                            final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());

                                assertThat(observation.getMobileClientRefId(), is(nullValue()));
                                assertThat(observation.isFromMobile(), is(false));

                                final GeoLocation geoLocation = observation.getGeoLocation();
                                assertThat(geoLocation, equalTo(inputDto.getGeoLocation()));
                                assertThat(geoLocation.getSource(), equalTo(GeoLocation.Source.MANUAL));
                                assertThat(observation.getRhy(), equalTo(rhy));

                                assertThat(observation.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(inputDto.getPointOfTime())));

                                assertThat(observation.getSpecies().getOfficialCode(), equalTo(obsMeta.getGameSpeciesCode()));
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

                                assertMooselikeAmountFieldsNotNull(observation, true, category.isWithinDeerHunting());
                                assertMooselikeAmounts(inputDto, observation);
                                assertThat(observation.getAmount(), equalTo(inputDto.getSumOfMooselikeAmounts()));

                                assertLargeCarnivoreFieldsAreNull(observation);
                                assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));

                                assertThat(findSpecimens(observation), hasSize(0));
                            });
                        });
                    });
        }));
    }

    @Test
    public void testCreateObservation_observerIsNotAuthor() {
        withRhy(rhy -> withPerson(author -> withPerson(observer -> {

            createObservationMetaF(NORMAL, NAKO).withAmount(YES).consumeBy(obsMeta -> {

                onSavedAndAuthenticated(createUser(author), () -> {

                    final ObservationDTO inputDto = obsMeta.dtoBuilder()
                            .withAmountAndSpecimens(1)
                            .withActorInfo(observer)
                            .build();
                    final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                    runInTransaction(() -> {
                        final Observation observation = assertObservationCreated(outputDto.getId());
                        assertAuthorAndActor(observation, F.getId(author), F.getId(observer));
                    });
                });
            });
        })));
    }

    @Test
    public void testCreateObservation_withAmountButNoSpecimens() {
        withPerson(author -> createObservationMetaF(NORMAL, MUUTON_AIKAINEN_LEPAILYALUE)
                .withAmount(YES)
                .consumeBy(obsMeta -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final ObservationDTO inputDto = obsMeta.dtoBuilder().withAmount(13).build();
                        final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                        runInTransaction(() -> {
                            final Observation observation = assertObservationCreated(outputDto.getId());

                            assertThat(observation.getAmount(), equalTo(13));
                            assertThat(findSpecimens(observation), hasSize(0));
                        });
                    });
                }));
    }

    @Test
    public void testCreateObservation_forLargeCarnivore_withCarnivoreAuthority() {
        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO);

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .withCarnivoreAuthority(true)
                                    .mutateLargeCarnivoreFields()
                                    .withAmountAndSpecimens(1)
                                    .build();
                            final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());
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

    @Test(expected = ProhibitedFieldFound.class)
    public void testCreateObservation_forLargeCarnivore_whenCarnivoreAuthorityExpired() {
        final LocalDate today = today();

        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO, today.minusYears(1), today.minusWeeks(1));

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .withCarnivoreAuthority(true)
                                    .withAmountAndSpecimens(1)
                                    .mutateLargeCarnivoreFields()
                                    .build();

                            invokeCreateObservation(inputDto);
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_fromNormalToMooseHunting() {
        // Update normal riistakamera observation to moose hunting näkö.

        final GameSpecies wtDeer = model().newGameSpeciesWhiteTailedDeer();
        final GameSpecies moose = model().newGameSpeciesMoose();

        withRhy(rhy -> createObservationMetaF(wtDeer, NORMAL, RIISTAKAMERA)
                .withAmount(YES)
                .createSpecimenFixture(3)
                .consumeBy(deerFixt -> {

                    createObservationMetaF(moose, MOOSE_HUNTING, NAKO)
                            .withMooselikeAmountFieldsAs(Required.YES)
                            .consumeBy(mooseMeta -> {

                                onSavedAndAuthenticated(createUser(deerFixt.author), () -> {

                                    final ObservationDTO dto = mooseMeta.dtoBuilder()
                                            .populateWith(deerFixt.observation)
                                            .mutate()
                                            .build();

                                    invokeUpdateObservation(dto);

                                    runInTransaction(() -> {
                                        final Observation updated = observationRepo.getOne(dto.getId());
                                        assertVersion(updated, 1);

                                        assertThat(updated.getMobileClientRefId(), is(nullValue()));
                                        assertThat(updated.isFromMobile(), is(false));

                                        final GeoLocation updatedLocation = updated.getGeoLocation();
                                        assertThat(updatedLocation, equalTo(dto.getGeoLocation()));
                                        assertThat(updatedLocation.getSource(), equalTo(GeoLocation.Source.MANUAL));
                                        assertThat(updated.getRhy(), equalTo(rhy));

                                        assertThat(updated.getPointOfTime(), is(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                                        assertThat(updated.getSpecies().getOfficialCode(), equalTo(moose.getOfficialCode()));
                                        assertThat(updated.getObservationCategory(), equalTo(MOOSE_HUNTING));
                                        assertThat(updated.getObservationType(), equalTo(NAKO));

                                        assertAuthorAndActor(updated, F.getId(deerFixt.author), F.getId(deerFixt.author));

                                        final String description = updated.getDescription();
                                        assertThat(description, is(notNullValue()));
                                        assertThat(description, equalTo(dto.getDescription()));

                                        assertMooseAmountFieldsNotNull(updated, true);
                                        assertMooselikeAmounts(dto, updated);
                                        assertThat(updated.getAmount(), equalTo(dto.getSumOfMooselikeAmounts()));

                                        assertThat(updated.getHuntingDayOfGroup(), is(nullValue()));
                                        assertLargeCarnivoreFieldsAreNull(updated);

                                        assertThat(findSpecimens(updated), hasSize(0));
                                    });
                                });
                            });
                }));
    }

    @Test
    public void testUpdateObservation_whenNothingChanged() {
        createObservationMetaF(NORMAL, NAKO)
                .withAmount(YES)
                .createSpecimenFixture(5)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createUser(obsFixt.author), () -> {

                        final List<ObservationSpecimen> originalSpecimens = obsFixt.specimens;

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .populateWith(obsFixt.observation)
                                .populateSpecimensWith(originalSpecimens)
                                .build();

                        invokeUpdateObservation(dto);

                        runInTransaction(() -> {
                            final Observation updated = observationRepo.getOne(dto.getId());
                            assertVersion(updated, 0);

                            // Specimens amount should not be changed.
                            final List<ObservationSpecimen> actualSpecimens = findSpecimens(updated);
                            assertThat(actualSpecimens, hasSize(5));

                            // In order to assert that specimens are unaffected it is checked that the content
                            // of specimen DTOs matches with both original and updated specimen entities.

                            final ObservationSpecimenOps specimenOps = dto.specimenOps();

                            assertSpecimens(originalSpecimens, dto.getSpecimens(), specimenOps::equalContent);
                            assertSpecimens(actualSpecimens, dto.getSpecimens(), specimenOps::equalContent);
                        });
                    });
                });
    }

    @Test
    public void testUpdateObservation_whenUpdatingSpecimensOnly() {
        createObservationMetaF(NORMAL, NAKO)
                .withAmount(YES)
                .createSpecimenFixture(5)
                .consumeBy((obsMeta, obsFixt) -> {

                    final Person author = obsFixt.author;

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final Observation original = obsFixt.observation;

                        final ObservationDTO dto = obsMeta.dtoBuilder()
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

                            assertThat(updated.getSpecies().getOfficialCode(), equalTo(original.getSpecies().getOfficialCode()));
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
    }

    @Test
    public void testUpdateObservation_withinDeerHunting_observationCreatedLongerThan24hAgo() {
        withDeerHuntingGroupFixture(huntingFixt -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixt.group, today());

            createObservationMetaF(DEER_HUNTING, NAKO)
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withGeoLocation(huntingFixt.zoneCentroid)
                    .withDeerHuntingType(DOG_HUNTING)
                    .withAuthor(huntingFixt.groupLeader)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(huntingFixt.groupLeader), () -> {

                            final Observation original = obsFixt.observation;

                            // Assert test assumptions.
                            final int sumOfOriginalMooselikeAmounts = original.getSumOfMooselikeAmounts();
                            assertMooselikeAmountFieldsNotNull(original, true, true);

                            MockTimeProvider.mockTime(DateUtil.now().getMillis() + twoDaysInMilliseconds);

                            final ObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(original)
                                    .withSomeGeoLocation()
                                    .mutateMooselikeAmountFields()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 0); // It should not be updated!

                                assertThat(updated.getGeoLocation(), equalTo(original.getGeoLocation()));

                                assertMooselikeAmountFieldsNotNull(updated, true, true);
                                assertMooselikeAmounts(updated, original);
                                assertThat(updated.getAmount(), equalTo(sumOfOriginalMooselikeAmounts));
                            });
                        });
                    });
        });
    }

    // Description and image can be edited even for locked observation.
    @Test
    public void testUpdateObservation_withinDeerHunting_updateDescription_observationCreatedLongerThan24hAgo() {
        withDeerHuntingGroupFixture(huntingFixt -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixt.group, today());

            createObservationMetaF(DEER_HUNTING, NAKO)
                    .withAmount(NO)
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withGeoLocation(huntingFixt.zoneCentroid)
                    .withDeerHuntingType(DOG_HUNTING)
                    .withAuthor(huntingFixt.groupLeader)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(huntingFixt.groupLeader), () -> {

                            final Observation original = obsFixt.observation;

                            // Assert test assumptions.
                            final int sumOfOriginalMooselikeAmounts = original.getSumOfMooselikeAmounts();
                            assertMooselikeAmountFieldsNotNull(original, true, true);

                            MockTimeProvider.mockTime(DateUtil.now().getMillis() + twoDaysInMilliseconds);

                            final ObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(original)
                                    .withSomeGeoLocation()              // This should *not* be updated
                                    .withDescription("New description") // This should be updated
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                assertThat(updated.getGeoLocation(), equalTo(original.getGeoLocation()));
                                assertThat(updated.getDescription(), equalTo("New description"));

                                assertMooselikeAmountFieldsNotNull(updated, true, true);
                                assertMooselikeAmounts(updated, original);
                                assertThat(updated.getAmount(), equalTo(sumOfOriginalMooselikeAmounts));
                            });
                        });
                    });
        });
    }

    @Test
    public void testUpdateObservation_withinDeerHunting_byGroupLeader() {
        withDeerHuntingGroupFixture(huntingFixt -> {
            testUpdateDeerHuntingObservationAsAuthor(huntingFixt, huntingFixt.groupLeader);
        });
    }

    @Test
    public void testUpdateObservation_withinDeerHunting_byGroupMember() {
        withDeerHuntingGroupFixture(huntingFixt -> {
            testUpdateDeerHuntingObservationAsAuthor(huntingFixt, huntingFixt.groupMember);
        });
    }

    private void testUpdateDeerHuntingObservationAsAuthor(final HuntingGroupFixture huntingFixture,
                                                          final Person author) {

        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixture.group, today());

        createObservationMetaF(DEER_HUNTING, NAKO)
                .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                .withMooselikeAmountFieldsAs(Required.YES)
                .createObservationFixture()
                .withGeoLocation(huntingFixture.zoneCentroid)
                .withDeerHuntingType(DOG_HUNTING)
                .withAuthor(author)
                .withGroupHuntingDay(huntingDay)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final Observation original = obsFixt.observation;

                        // Assert test assumptions.
                        assertMooselikeAmountFieldsNotNull(original, true, true);
                        assertThat(original.getSumOfMooselikeAmounts(), greaterThan(1));

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .populateWith(original)
                                .withSomeGeoLocation()
                                .mutateMooselikeAmountFields()
                                .mutateDeerHuntingTypeFields()
                                .build();

                        invokeUpdateObservation(dto);

                        runInTransaction(() -> {
                            final Observation updated = observationRepo.getOne(dto.getId());
                            assertVersion(updated, 2);

                            assertThat(updated.getGeoLocation(), equalTo(dto.getGeoLocation()));

                            assertThat(updated.getDeerHuntingType(), equalTo(dto.getDeerHuntingType()));

                            assertMooselikeAmountFieldsNotNull(updated, true, true);
                            assertMooselikeAmounts(dto, updated);
                            assertThat(updated.getAmount(), equalTo(dto.getSumOfMooselikeAmounts()));
                        });
                    });
                });
    }

    @Test
    public void testUpdateObservation_withinDeerHunting_moveLocationToOutsideGroupArea() {
        withDeerHuntingGroupFixture(huntingFixt -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixt.group, today());

            createObservationMetaF(DEER_HUNTING, NAKO)
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withGeoLocation(huntingFixt.zoneCentroid)
                    .withDeerHuntingType(DOG_HUNTING)
                    .withAuthor(huntingFixt.groupLeader)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(huntingFixt.groupLeader), () -> {

                            final GeoLocation newLocation = geoLocation();

                            final ObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(obsFixt.observation)
                                    .withGeoLocation(newLocation)
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 2);

                                // Hunting day should be affected too.
                                assertThat(updated.getHuntingDayOfGroup(), is(nullValue()));

                                assertThat(updated.getGeoLocation(), equalTo(newLocation));
                            });
                        });
                    });
        });
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensPresent() {
        createObservationMetaF(NORMAL, NAKO)
                .withAmount(YES)
                .createSpecimenFixture(5)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createUser(obsFixt.author), () -> {

                        final List<ObservationSpecimen> originalSpecimens = obsFixt.specimens;

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .populateWith(obsFixt.observation)
                                .populateSpecimensWith(originalSpecimens)
                                .withAmount(originalSpecimens.size() + 10)
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

                            // In order to assert that specimens are unaffected it is checked that the content
                            // of specimen DTOs match with both original and updated specimen entities.

                            final ObservationSpecimenOps specimenOps = dto.specimenOps();

                            assertSpecimens(originalSpecimens, dto.getSpecimens(), specimenOps::equalContent);
                            assertSpecimens(actualSpecimens, dto.getSpecimens(), specimenOps::equalContent);
                        });
                    });
                });
    }

    @Test
    public void testUpdateObservation_forAmountChange_whenSpecimensAbsent() {
        withPerson(author -> {

            createObservationMetaF(NORMAL, NAKO)
                    .withAmount(YES)
                    .createObservationFixture()
                    .withAuthor(author)
                    .consumeBy((oldMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO dto = oldMeta.dtoBuilder()
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

    @Test
    public void testUpdateObservation_whenChangingToObservationTypeNotAcceptingAmount() {
        createObservationMetaF(NORMAL, NAKO)
                .withAmount(YES)
                .createSpecimenFixture(5)
                .consumeBy(obsFixt -> {

                    createObservationMetaF(NORMAL, AANI).withAmount(NO).consumeBy(aaniMeta -> {

                        onSavedAndAuthenticated(createUser(obsFixt.author), () -> {

                            // Assert test invariant.
                            assertThat(findSpecimens(obsFixt.observation), hasSize(5));

                            final ObservationDTO dto = aaniMeta.dtoBuilder()
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
    }

    @Test
    public void testUpdateObservation_forLargeCarnivore_withCarnivoreAuthority() {
        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO);

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
                    .withAmount(YES)
                    .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                    .createSpecimenFixture(1)
                    .withAuthor(author)
                    .withCarnivoreAuthorityEnabled()
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO dto = obsMeta.dtoBuilder()
                                    .withCarnivoreAuthority(true)
                                    .populateWith(obsFixt.observation)
                                    .mutateLargeCarnivoreFields()
                                    .populateSpecimensWith(obsFixt.specimens)
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                assertLargeCarnivoreFieldsNotNull(updated);

                                assertThat(
                                        updated.getVerifiedByCarnivoreAuthority(),
                                        equalTo(dto.getVerifiedByCarnivoreAuthority()));
                                assertThat(updated.getObserverName(), equalTo(dto.getObserverName()));
                                assertThat(updated.getObserverPhoneNumber(), equalTo(dto.getObserverPhoneNumber()));
                                assertThat(
                                        updated.getOfficialAdditionalInfo(), equalTo(dto.getOfficialAdditionalInfo()));
                            });
                        });
                    });
        }));
    }

    @Test
    public void testUpdateObservation_forLargeCarnivore_carnivoreFieldsNotClearedWhenAuthorityExpired() {
        final LocalDate today = today();

        withRhy(rhy -> withPerson(author -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO, today.minusYears(1), today.minusWeeks(1));

            createObservationMetaF(OFFICIAL_CODE_WOLF, NAKO)
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

                            final ObservationDTO dto = obsMeta.dtoBuilder()
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

                                // Description should be updated.
                                final String description = updated.getDescription();
                                assertThat(description, equalTo(dto.getDescription()));
                                assertThat(description, not(equalTo(original.getDescription())));

                                assertLargeCarnivoreFieldsNotNull(updated);

                                // Large carnivore fields should NOT be updated i.e. the fields should have
                                // same value than original obsevation.

                                assertThat(
                                        updated.getVerifiedByCarnivoreAuthority(),
                                        equalTo(original.getVerifiedByCarnivoreAuthority()));
                                assertThat(updated.getObserverName(), equalTo(original.getObserverName()));
                                assertThat(
                                        updated.getObserverPhoneNumber(), equalTo(original.getObserverPhoneNumber()));
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

    @Test
    public void testDeleteObservation() {
        withPerson(author -> {

            createObservationMetaF(NORMAL, NAKO)
                    .withAmount(YES)
                    .createSpecimenFixture(1)
                    .withAuthor(author)
                    .consumeBy((obsMeta, obsFixt) -> {

                        model().newGameDiaryImage(obsFixt.observation);

                        onSavedAndAuthenticated(createUser(author), () -> {
                            final Long id = obsFixt.observation.getId();
                            feature.deleteObservation(id);
                        });

                        runInTransaction(() -> {
                            final Long id = obsFixt.observation.getId();
                            assertThat(observationRepo.findById(id).isPresent(), is(false));
                            final List<DeletedObservation> deletedObservations = deletedObservationRepository.findAll();
                            assertThat(deletedObservations, hasSize(1));
                            final DeletedObservation deletedObservation = deletedObservations.get(0);
                            assertThat(deletedObservation.getObservationId(), equalTo(id));
                        });
                    });
        });
    }

    @Test
    public void testDeleteObservation_whenAttachedToHuntingDay() {
        clubGroupUserFunctionsBuilder().withAdminAndModerator(true).build().forEach(userFn -> {
            withMooseHuntingGroupFixture(f -> {
                final SystemUser user = userFn.apply(f.club, f.group);
                final Person author = user.isModeratorOrAdmin() ? f.groupMember : user.getPerson();

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
                final Observation observation = model().newObservation(f.species, author, huntingDay);

                onSavedAndAuthenticated(user, () -> {
                    final Long observationId = observation.getId();

                    assertThrows("Deletion of observation associated with a hunting day should fail",
                            RuntimeException.class,
                            () -> feature.deleteObservation(observationId));

                    assertThat(observationRepo.findById(observationId).isPresent(), is(true));
                });
            });

            reset();
        });
    }
}
