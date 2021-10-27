package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.DeerHuntingType.DOG_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE;
import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class MobileObservationFeature_DeerHuntingDayTest extends MobileObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    @DataPoints("specVersions")
    public static final ObservationSpecVersion[] SPEC_VERSIONS = ObservationSpecVersion.values();

    private static final long TWO_DAYS = 172800000; // in milliseconds
    private static final long SIX_HOURS = 21600000; // in milliseconds

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    private LocalDate observationDay;

    @Before
    public void setUp() {
        observationDay = new LocalDate(2020, 2, 15);

        harvestPermitLockedByDateService.disableLockingForTests();
    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
        harvestPermitLockedByDateService.normalLocking();
    }

    @Theory
    public void testSetHuntingDayForDeerObservation_asGroupLeader(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE));

        withDeerHuntingGroupFixture(fixture -> {
            makeWithinDeerHuntingObservation(version, fixture, fixture.groupLeader);
        });
    }

    @Theory
    public void testSetHuntingDayForDeerObservation_asGroupMember(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE));

        withDeerHuntingGroupFixture(fixture -> {
            makeWithinDeerHuntingObservation(version, fixture, fixture.groupMember);
        });
    }

    @Theory
    public void testSetHuntingDayForDeerObservation_asNonMember(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE));

        withDeerHuntingGroupFixture(fixture -> {
            final Person randomGuy = model().newPerson();
            createObservationMetaF(fixture.species, version, DEER_HUNTING, NAKO)
                    .forMobile()
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(randomGuy), () -> {

                            final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .withDeerHuntingType(DOG_HUNTING)
                                    .withGeoLocation(fixture.zoneCentroid)
                                    .mutateMooselikeAmountFields()
                                    .build();

                            final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());

                                assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));
                                assertThat(observation.getApproverToHuntingDay(), is(nullValue()));
                            });
                        });
                    });
        });
    }

    private void makeWithinDeerHuntingObservation(final ObservationSpecVersion version,
                                                  final HuntingGroupFixture fixture,
                                                  final Person author) {

        createObservationMetaF(fixture.species, version, DEER_HUNTING, NAKO)
                .forMobile()
                .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                .withDeerHuntingType(DOG_HUNTING)
                                .withGeoLocation(fixture.zoneCentroid)
                                .mutateMooselikeAmountFields()
                                .build();

                        final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                        runInTransaction(() -> {
                            final Observation observation = assertObservationCreated(outputDto.getId());

                            assertThat(observation.getHuntingDayOfGroup(), is(notNullValue()));
                            assertThat(observation.getApproverToHuntingDay(), is(equalTo(author)));
                        });
                    });
                });
    }

    @Theory
    public void testSetHuntingDay_beforeFirstPeriod(final ObservationSpecVersion version) {
        makeObservationForPermitValidDates(
                observationDay.plusDays(1),
                observationDay.plusDays(7),
                observationDay.plusDays(14),
                observationDay.plusDays(21),
                false,
                version);
    }

    @Theory
    public void testSetHuntingDay_onFirstPeriod(final ObservationSpecVersion version) {
        makeObservationForPermitValidDates(
                observationDay.minusDays(7),
                observationDay.plusDays(1),
                observationDay.plusDays(7),
                observationDay.plusDays(14),
                true,
                version);
    }

    @Theory
    public void testSetHuntingDay_betweenAllowedDates(final ObservationSpecVersion version) {
        makeObservationForPermitValidDates(
                observationDay.minusDays(7),
                observationDay.minusDays(1),
                observationDay.plusDays(1),
                observationDay.plusDays(7),
                false,
                version);
    }

    @Theory
    public void testSetHuntingDay_onSecondPeriod(final ObservationSpecVersion version) {
        makeObservationForPermitValidDates(
                observationDay.minusDays(14),
                observationDay.minusDays(7),
                observationDay.minusDays(1),
                observationDay.plusDays(1),
                true,
                version);
    }

    @Theory
    public void testSetHuntingDay_afterSecondPeriod(final ObservationSpecVersion version) {
        makeObservationForPermitValidDates(
                observationDay.minusDays(21),
                observationDay.minusDays(14),
                observationDay.minusDays(7),
                observationDay.minusDays(1),
                false,
                version);
    }

    private void makeObservationForPermitValidDates(final LocalDate begin,
                                                    final LocalDate end,
                                                    final LocalDate begin2,
                                                    final LocalDate end2,
                                                    final boolean shouldHuntingDayBeSet,
                                                    final ObservationSpecVersion version) {

        // Use mocked date, otherwise tests will fail near new hunting year
        MockTimeProvider.mockTime(observationDay.toDateTimeAtStartOfDay().getMillis());

        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE));

        withDeerHuntingGroupFixture(fixt -> {

            fixt.speciesAmount.setBeginDate(begin);
            fixt.speciesAmount.setEndDate(end);

            if (begin2 != null && end2 != null) {
                fixt.speciesAmount.setBeginDate2(begin2);
                fixt.speciesAmount.setEndDate2(end2);
            }

            createObservationMetaF(fixt.species, MOST_RECENT, DEER_HUNTING, NAKO)
                    .forMobile()
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(obsMeta -> {

                        onSavedAndAuthenticated(createUser(fixt.groupMember), u -> {

                            final MobileObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .withGeoLocation(fixt.zoneCentroid)
                                    .withDeerHuntingType(DOG_HUNTING)
                                    .mutateMooselikeAmountFields()
                                    .build();

                            final MobileObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());

                                if (shouldHuntingDayBeSet) {
                                    assertThat(observation.getHuntingDayOfGroup(), is(notNullValue()));
                                    assertThat(observation.getApproverToHuntingDay(), is(equalTo(u.getPerson())));
                                } else {
                                    assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));
                                    assertThat(observation.getApproverToHuntingDay(), is(nullValue()));
                                }
                            });
                        });
                    });
        });
    }

    @Theory
    public void testHuntingDayUpdateIsIgnoredForOlderThan24hObservation(final ObservationSpecVersion version) {
        updateHuntingDayByChangingLocationForObservation(TWO_DAYS, false, version);
    }

    @Theory
    public void testHuntingDayIsUpdatedForObservationDoneWithin24h(final ObservationSpecVersion version) {
        updateHuntingDayByChangingLocationForObservation(SIX_HOURS, true, version);
    }

    private void updateHuntingDayByChangingLocationForObservation(final long observationAge,
                                                                  final boolean shouldHuntingDayBeChanged,
                                                                  final ObservationSpecVersion version) {

        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE));

        withDeerHuntingGroupFixture(fixture -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, observationDay);

            createObservationMetaF(fixture.species, version, DEER_HUNTING, NAKO)
                    .forMobile()
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withGeoLocation(fixture.zoneCentroid)
                    .withDeerHuntingType(DOG_HUNTING)
                    .withAuthor(fixture.groupLeader)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(fixture.groupLeader), () -> {

                            MockTimeProvider.mockTime(DateUtil.now().getMillis() + observationAge);

                            final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(obsFixt.observation)
                                    .withSomeGeoLocation()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());

                                if (shouldHuntingDayBeChanged) {
                                    assertVersion(updated, 2);
                                    assertThat(updated.getHuntingDayOfGroup(), is(nullValue()));
                                } else {
                                    assertVersion(updated, 0);
                                    assertThat(updated.getHuntingDayOfGroup(), is(equalTo(huntingDay)));
                                }
                            });
                        });
                    });
        });
    }

    @Theory
    public void testUpdateObservation_huntingDayShouldBeNulledWhenChangingSpeciesAndObservationCategory(final ObservationSpecVersion version) {
        assumeTrue(version.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_DEER_HUNTING_TYPE));

        withDeerHuntingGroupFixture(fixture -> {

            final Person author = fixture.groupLeader;
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, observationDay);

            createObservationMetaF(fixture.species, version, DEER_HUNTING, NAKO)
                    .forMobile()
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withGeoLocation(fixture.zoneCentroid)
                    .withDeerHuntingType(DOG_HUNTING)
                    .withAuthor(author)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        final GameSpecies anotherSpecies = model().newGameSpecies();

                        createObservationMetaF(anotherSpecies, version, NORMAL, NAKO)
                                .forMobile()
                                .consumeBy(anotherObsMeta -> {

                                    onSavedAndAuthenticated(createUser(author), () -> {

                                        // Create input DTO on the basis of original observation (having deer
                                        // hunting fields set) but change species and category to different values.

                                        final MobileObservationDTO dto = obsMeta.dtoBuilder()
                                                .populateWith(obsFixt.observation)
                                                .withGameSpeciesCode(anotherSpecies.getOfficialCode())
                                                .withObservationCategory(NORMAL)
                                                .withDeerHuntingType(null)
                                                .withMooselikeAmountFieldsCleared()
                                                .build();

                                        invokeUpdateObservation(dto);

                                        runInTransaction(() -> {
                                            final Observation updated = observationRepo.getOne(dto.getId());
                                            assertVersion(updated, 1);

                                            assertThat(updated.getSpecies(), is(equalTo(anotherSpecies)));
                                            assertThat(updated.getObservationCategory(), is(equalTo(NORMAL)));

                                            // Hunting day association must be removed.
                                            assertThat(updated.getHuntingDayOfGroup(), is(nullValue()));
                                            assertThat(updated.getApproverToHuntingDay(), is(nullValue()));
                                        });
                                    });
                                });
                    });
        });
    }
}
