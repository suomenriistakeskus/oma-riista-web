package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.error.ProhibitedFieldFound;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.gamediary.DeerHuntingType.DOG_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY_DEER_PILOT;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES_DEER_PILOT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class ObservationFeature_DeerHuntingDayTest extends ObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

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

    @Test
    public void testSetHuntingDayForDeerObservation_asGroupLeader() {
        withDeerHuntingGroupFixture(fixture -> {
            makeWithinDeerHuntingObservation(fixture, fixture.groupLeader);
        });
    }

    @Test
    public void testSetHuntingDayForDeerObservation_asGroupMember() {
        withDeerHuntingGroupFixture(fixture -> {
            makeWithinDeerHuntingObservation(fixture, fixture.groupMember);
        });
    }

    @Test(expected = ProhibitedFieldFound.class)
    public void testSetHuntingDayForDeerObservation_asNonMember() {
        withDeerHuntingGroupFixture(fixture -> {
            withPerson(randomGuy -> {
                makeWithinDeerHuntingObservation(fixture, randomGuy);
            });
        });
    }

    private void makeWithinDeerHuntingObservation(final HuntingGroupFixture fixture, final Person author) {
        enableDeerPilot(fixture.permit);

        createObservationMetaF(DEER_HUNTING, NAKO)
                .withDeerHuntingTypeFieldsAs(YES_DEER_PILOT, VOLUNTARY_DEER_PILOT)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {
                    onSavedAndAuthenticated(createUser(author), () -> {

                        final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                .withDeerHuntingType(DOG_HUNTING)
                                .withGeoLocation(fixture.zoneCentroid)
                                .mutateMooselikeAmountFields()
                                .build();

                        final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                        runInTransaction(() -> {
                            final Observation observation = assertObservationCreated(outputDto.getId());

                            assertThat(observation.getHuntingDayOfGroup(), is(notNullValue()));
                            assertThat(observation.getApproverToHuntingDay(), is(equalTo(author)));
                        });
                    });
                });
    }

    @Test
    public void testSetHuntingDay_beforeFirstPeriod() {
        makeObservationForPermitValidDates(
                observationDay.plusDays(1),
                observationDay.plusDays(7),
                observationDay.plusDays(14),
                observationDay.plusDays(21),
                false);
    }

    @Test
    public void testSetHuntingDay_onFirstPeriod() {
        makeObservationForPermitValidDates(
                observationDay.minusDays(7),
                observationDay.plusDays(1),
                observationDay.plusDays(7),
                observationDay.plusDays(14),
                true);
    }

    @Test
    public void testSetHuntingDay_betweenAllowedDates() {
        makeObservationForPermitValidDates(
                observationDay.minusDays(7),
                observationDay.minusDays(1),
                observationDay.plusDays(1),
                observationDay.plusDays(7),
                false);
    }

    @Test
    public void testSetHuntingDay_onSecondPeriod() {
        makeObservationForPermitValidDates(
                observationDay.minusDays(14),
                observationDay.minusDays(7),
                observationDay.minusDays(1),
                observationDay.plusDays(1),
                true);
    }

    @Test
    public void testSetHuntingDay_afterSecondPeriod() {
        makeObservationForPermitValidDates(
                observationDay.minusDays(21),
                observationDay.minusDays(14),
                observationDay.minusDays(7),
                observationDay.minusDays(1),
                false);
    }

    private void makeObservationForPermitValidDates(final LocalDate begin,
                                                    final LocalDate end,
                                                    final LocalDate begin2,
                                                    final LocalDate end2,
                                                    final boolean shouldHuntingDayBeSet) {

        // Use mocked date, otherwise tests will fail near new hunting year
        MockTimeProvider.mockTime(observationDay.toDateTimeAtStartOfDay().getMillis());

        final HarvestPermit permit = model().newHarvestPermit();
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();
        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

        speciesAmount.setBeginDate(begin);
        speciesAmount.setEndDate(end);

        if (begin2 != null && end2 != null) {
            speciesAmount.setBeginDate2(begin2);
            speciesAmount.setEndDate2(end2);
        }

        enableDeerPilot(permit);

        withHuntingGroupFixture(speciesAmount, huntingFixt -> {

            createObservationMetaF(species, DEER_HUNTING, NAKO)
                    .withDeerHuntingTypeFieldsAs(YES_DEER_PILOT, VOLUNTARY_DEER_PILOT)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .consumeBy(obsMeta -> {

                        final Person author = huntingFixt.groupMember;

                        onSavedAndAuthenticated(createUser(author), () -> {

                            final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                    .withGeoLocation(huntingFixt.zoneCentroid)
                                    .withDeerHuntingType(DOG_HUNTING)
                                    .mutateMooselikeAmountFields()
                                    .build();

                            final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                            runInTransaction(() -> {
                                final Observation observation = assertObservationCreated(outputDto.getId());

                                if (shouldHuntingDayBeSet) {
                                    assertThat(observation.getHuntingDayOfGroup(), is(notNullValue()));
                                    assertThat(observation.getApproverToHuntingDay(), is(equalTo(author)));
                                } else {
                                    assertThat(observation.getHuntingDayOfGroup(), is(nullValue()));
                                    assertThat(observation.getApproverToHuntingDay(), is(nullValue()));
                                }
                            });
                        });
                    });
        });
    }

    @Test
    public void testHuntingDayUpdateIsIgnoredForOlderThan24hObservation() {
        updateHuntingDayByChangingLocationForObservation(TWO_DAYS, false);
    }

    @Test
    public void testHuntingDayIsUpdatedForObservationDoneWithin24h() {
        updateHuntingDayByChangingLocationForObservation(SIX_HOURS, true);
    }

    private void updateHuntingDayByChangingLocationForObservation(final long observationAge,
                                                                  final boolean shouldHuntingDayBeChanged) {
        withDeerHuntingGroupFixture(fixture -> {

            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, observationDay);

            createObservationMetaF(fixture.species, DEER_HUNTING, NAKO)
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

                            final ObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(obsFixt.observation)
                                    .withSomeGeoLocation()
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation actual = observationRepo.getOne(dto.getId());

                                if (shouldHuntingDayBeChanged) {
                                    assertVersion(actual, 2);
                                    assertThat(actual.getHuntingDayOfGroup(), is(nullValue()));
                                } else {
                                    assertVersion(actual, 0);
                                    assertThat(actual.getHuntingDayOfGroup(), equalTo(huntingDay));
                                }
                            });
                        });
                    });
        });
    }

    @Test
    public void testUpdateObservation_huntingDayShouldBeNulledWhenChangingSpeciesAndObservationCategory() {
        withDeerHuntingGroupFixture(fixture -> {

            final Person author = fixture.groupLeader;
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, observationDay);

            createObservationMetaF(fixture.species, DEER_HUNTING, NAKO)
                    .withDeerHuntingTypeFieldsAs(YES, VOLUNTARY)
                    .withMooselikeAmountFieldsAs(Required.YES)
                    .createObservationFixture()
                    .withGeoLocation(fixture.zoneCentroid)
                    .withDeerHuntingType(DOG_HUNTING)
                    .withAuthor(author)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        final GameSpecies anotherSpecies = model().newGameSpecies();

                        createObservationMetaF(anotherSpecies, NORMAL, NAKO)
                                .forMobile()
                                .consumeBy(anotherObsMeta -> {

                                    onSavedAndAuthenticated(createUser(author), () -> {

                                        // Create input DTO on the basis of original observation (having deer hunting
                                        // fields set) but change species and category to different values.

                                        final ObservationDTO dto = obsMeta.dtoBuilder()
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

    private void enableDeerPilot(final HarvestPermit permit) {
        model().newDeerPilot(permit);
    }
}
