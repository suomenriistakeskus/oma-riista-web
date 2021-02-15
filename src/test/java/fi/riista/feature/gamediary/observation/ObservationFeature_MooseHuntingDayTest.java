package fi.riista.feature.gamediary.observation;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfHuntingDayException;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfPermittedDatesException;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertLargeCarnivoreFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooseAmountFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmounts;
import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY;
import static fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence.YES;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ObservationFeature_MooseHuntingDayTest extends ObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    private HuntingGroupFixture fixture;
    private GroupHuntingDay huntingDay;

    @Before
    public void setup() {
        fixture = new HuntingGroupFixture(model());
        huntingDay = model().newGroupHuntingDay(fixture.group, today());
    }

    @Test
    public void testCreateObservationForHuntingDay() {
        final Person author = fixture.groupLeader;
        final Person observer = fixture.groupMember;

        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                .mutateMooselikeAmountFields()
                                .withActorInfo(observer)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                        runInTransaction(() -> {
                            final Observation observation = assertObservationCreated(outputDto.getId());

                            assertMooseAmountFieldsNotNull(observation, true);
                            assertMooselikeAmounts(inputDto, observation);
                            assertThat(observation.getAmount(), equalTo(inputDto.getSumOfMooselikeAmounts()));

                            assertAuthorAndActor(observation, F.getId(author), F.getId(observer));

                            assertAcceptanceToHuntingDay(observation, huntingDay, author);
                        });
                    });
                });
    }

    @Test
    public void testCreateObservationForHuntingDay_asModerator() {
        final Person author = fixture.groupLeader;
        final Person observer = fixture.groupMember;

        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    onSavedAndAuthenticated(createNewModerator(), () -> {

                        final ObservationDTO inputDto = obsMeta.dtoBuilder()
                                .mutateMooselikeAmountFields()
                                .withAuthorInfo(author)
                                .withActorInfo(observer)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                        runInTransaction(() -> {
                            final Observation observation = assertObservationCreated(outputDto.getId());

                            assertThat(observation.getDescription(), is(nullValue()));

                            assertMooseAmountFieldsNotNull(observation, true);
                            assertMooselikeAmounts(inputDto, observation);
                            assertThat(observation.getAmount(), equalTo(inputDto.getSumOfMooselikeAmounts()));

                            assertAuthorAndActor(observation, F.getId(author), F.getId(observer));

                            assertAcceptanceToHuntingDay(observation, huntingDay, null);
                        });
                    });
                });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testCreateObservationForHuntingDay_whenHuntingFinished() {
        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    // Set club hunting finished.
                    model().newModeratedBasicHuntingSummary(fixture.speciesAmount, fixture.club);

                    onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                        invokeCreateObservation(obsMeta.dtoBuilder()
                                .mutateMooselikeAmountFields()
                                .linkToHuntingDay(huntingDay)
                                .build());
                    });
                });
    }

    @Test(expected = PointOfTimeOutsideOfHuntingDayException.class)
    public void testCreateObservationForHuntingDay_whenPointOfTimeOutsideOfHuntingDay() {
        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                        invokeCreateObservation(obsMeta.dtoBuilder()
                                .mutateMooselikeAmountFields()
                                .linkToHuntingDay(huntingDay)
                                .withPointOfTime(huntingDay.getStartAsLocalDateTime().minusMinutes(1))
                                .build());
                    });
                });
    }

    @Test(expected = PointOfTimeOutsideOfPermittedDatesException.class)
    public void testCreateObservationForHuntingDay_whenPointOfTimeOutsideOfPermitted() {
        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .consumeBy(obsMeta -> {

                    final LocalDate date = fixture.speciesAmount.getBeginDate().minusDays(1);
                    huntingDay.setStartDate(date);
                    huntingDay.setEndDate(date);

                    onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                        invokeCreateObservation(obsMeta.dtoBuilder()
                                .mutateMooselikeAmountFields()
                                .linkToHuntingDay(huntingDay)
                                .withPointOfTime(date.toLocalDateTime(new LocalTime(12, 02)))
                                .build());
                    });
                });
    }

    @Test
    public void testUpdateObservation_linkToHuntingDay() {
        final Person member = fixture.groupMember;
        final Person leader = fixture.groupLeader;

        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .createObservationFixture()
                .withAuthor(member)
                .withGeoLocation(fixture.zoneCentroid)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createUser(leader), () -> {

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .populateWith(obsFixt.observation)
                                .withActorInfo(leader)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        runInTransaction(() -> {
                            final Observation updated = observationRepo.getOne(dto.getId());
                            assertVersion(updated, 1);

                            assertAuthorAndActor(updated, F.getId(member), F.getId(leader));

                            assertAcceptanceToHuntingDay(updated, huntingDay, leader);
                        });
                    });
                });
    }

    @Test
    public void testUpdateObservation_linkToHuntingDay_asModerator() {
        final Person originalAuthor = fixture.clubContact;

        final Person newAuthor = fixture.groupLeader;
        final Person newActor = fixture.groupMember;

        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .createObservationFixture()
                .withAuthor(originalAuthor)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createNewModerator(), () -> {

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .populateWith(obsFixt.observation)
                                .withAuthorInfo(newAuthor)
                                .withActorInfo(newActor)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        runInTransaction(() -> {
                            final Observation updated = observationRepo.getOne(dto.getId());
                            assertVersion(updated, 1);

                            assertAuthorAndActor(updated, F.getId(newAuthor), F.getId(newActor));

                            assertAcceptanceToHuntingDay(updated, huntingDay, null);
                        });
                    });
                });
    }

    @Test(expected = ObservationLinkableToHuntingDayOnlyWithHuntingCategoryException.class)
    public void testUpdateObservation_linkToHuntingDay_allowedOnlyWithinMooseHunting() {
        final Person author = fixture.groupLeader;

        createObservationMetaF(fixture.species, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .createObservationFixture()
                .withAuthor(author)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        invokeUpdateObservation(obsMeta.dtoBuilder()
                                .populateWith(obsFixt.observation)
                                .linkToHuntingDay(huntingDay)
                                .build());
                    });
                });
    }

    @Test
    public void testUpdateObservation_acceptorNotChangedOnModeratorOverrideWhenAlreadyLinkedToHuntingDay() {
        final Person author = fixture.groupMember;

        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .createObservationFixture()
                .withAuthor(author)
                .withGroupHuntingDay(huntingDay)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createNewModerator(), () -> {

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .populateWith(obsFixt.observation)
                                .withAuthorInfo(author)
                                .withActorInfo(author)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        runInTransaction(() -> {
                            final Observation updated = observationRepo.getOne(dto.getId());
                            assertVersion(updated, 1);

                            assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                            assertAcceptanceToHuntingDay(updated, huntingDay, author);
                        });
                    });
                });
    }

    // Test that observation is not mutated (except for description/images) when hunting is finished.
    @Test
    public void testUpdateObservation_whenHuntingFinished() {
        final Person author = fixture.clubContact;

        final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(fixture.group, today().minusDays(1));

        // Set club hunting finished.
        model().newModeratedBasicHuntingSummary(fixture.speciesAmount, fixture.club);

        createObservationMetaF(fixture.species, MOOSE_HUNTING, NAKO)
                .withMooselikeAmountFieldsAs(Required.YES)
                .createObservationFixture()
                .withAuthor(author)
                .withGroupHuntingDay(huntingDay)
                .consumeBy((nakoMeta, nakoFixt) -> {

                    final Observation original = nakoFixt.observation;

                    createObservationMetaF(MOOSE_HUNTING, JALKI)
                            .withMooselikeAmountFieldsAs(Required.YES)
                            .consumeBy(jalkiMeta -> {

                                onSavedAndAuthenticated(createUser(author), () -> {
                                    final ObservationDTO dto = jalkiMeta.dtoBuilder()
                                            .populateWith(original)
                                            .mutate()
                                            .withActorInfo(fixture.groupMember)
                                            .linkToHuntingDay(huntingDay2)
                                            .build();

                                    invokeUpdateObservation(dto);

                                    runInTransaction(() -> {
                                        final Observation updated = observationRepo.getOne(dto.getId());
                                        assertVersion(updated, 1);

                                        // Assert that description is changed.
                                        assertThat(updated.getDescription(), equalTo(dto.getDescription()));

                                        // Assert that other observation fields are NOT changed.

                                        assertThat(updated.getGeoLocation(), equalTo(original.getGeoLocation()));
                                        assertThat(updated.getRhy(), is(nullValue()));

                                        assertThat(updated.getPointOfTime(), equalTo(original.getPointOfTime()));

                                        assertThat(updated.getSpecies().getOfficialCode(), equalTo(nakoMeta.getGameSpeciesCode()));
                                        assertThat(updated.getObservationCategory(), equalTo(MOOSE_HUNTING));
                                        assertThat(updated.getObservationType(), equalTo(NAKO));

                                        assertMooseAmountFieldsNotNull(updated, true);
                                        assertMooselikeAmounts(original, updated);
                                        assertThat(updated.getAmount(), equalTo(original.getSumOfMooselikeAmounts()));

                                        assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                                        assertAcceptanceToHuntingDay(updated, huntingDay, author);
                                    });
                                });
                            });
                });
    }

    // Test that observation is not mutated (except for linking to hunting day).
    @Test
    public void testUpdateLargeCarnivoreObservation_linkToHuntingDay_whenAuthorNotHavingCarnivoreAuthority() {
        final Person author = fixture.groupMember;
        final Person proposedAuthor = fixture.groupLeader;
        final Person acceptor = fixture.clubContact;

        createObservationMetaF(OFFICIAL_CODE_WOLF, MOOSE_HUNTING, JALKI)
                .withAmount(YES)
                .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                .createSpecimenFixture(5)
                .withAuthor(author)
                .withCarnivoreAuthorityEnabled()
                .withGeoLocation(fixture.zoneCentroid)
                .consumeBy((obsMeta, obsFixt) -> {

                    final Observation original = obsFixt.observation;

                    original.setPointOfTime(huntingDay.getStartAsDateTime());

                    onSavedAndAuthenticated(createUser(acceptor), () -> {

                        // Assert test invariant.
                        assertLargeCarnivoreFieldsNotNull(original);

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .withCarnivoreAuthority(false)
                                .populateWith(original)
                                .populateSpecimensWith(obsFixt.specimens)
                                .mutate()
                                .mutateSpecimens()
                                .withAuthorInfo(proposedAuthor)
                                .withActorInfo(proposedAuthor)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        runInTransaction(() -> {
                            final Observation updated = observationRepo.getOne(dto.getId());
                            assertVersion(updated, 1);

                            // Assert that observation fields are NOT changed except for hunting day.

                            assertThat(updated.getGeoLocation(), equalTo(original.getGeoLocation()));
                            assertThat(updated.getPointOfTime(), equalTo(original.getPointOfTime()));

                            assertThat(updated.getSpecies().getOfficialCode(), equalTo(OFFICIAL_CODE_WOLF));
                            assertThat(updated.getObservationCategory(), equalTo(MOOSE_HUNTING));
                            assertThat(updated.getObservationType(), equalTo(JALKI));

                            assertThat(updated.getDescription(), equalTo(original.getDescription()));
                            assertThat(updated.getAmount(), equalTo(5));

                            assertAuthorAndActor(updated, F.getId(author), F.getId(author));

                            // Assert that linking with hunting day has occurred.
                            assertAcceptanceToHuntingDay(updated, huntingDay, acceptor);
                        });
                    });
                });
    }

    // Test that original author with carnivore authority can mutate observation within linking to hunting day.
    @Test
    public void testUpdateLargeCarnivoreObservation_linkToHuntingDay_withOriginalAuthor() {
        final Person author = fixture.groupLeader;

        model().newOccupation(fixture.rhy, author, OccupationType.PETOYHDYSHENKILO);

        createObservationMetaF(OFFICIAL_CODE_WOLF, MOOSE_HUNTING, JALKI)
                .withAmount(YES)
                .withLargeCarnivoreFieldsAs(VOLUNTARY_CARNIVORE_AUTHORITY)
                .createSpecimenFixture(5)
                .withAuthor(author)
                .withCarnivoreAuthorityEnabled()
                .withGeoLocation(fixture.zoneCentroid)
                .consumeBy((obsMeta, obsFixt) -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final ObservationDTO dto = obsMeta.dtoBuilder()
                                .withCarnivoreAuthority(true)
                                .populateWith(obsFixt.observation)
                                .populateSpecimensWith(obsFixt.specimens)
                                .mutateLargeCarnivoreFields()
                                .mutateSpecimens()
                                .linkToHuntingDay(huntingDay)
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
                            assertThat(updated.getOfficialAdditionalInfo(), equalTo(dto.getOfficialAdditionalInfo()));

                            assertAcceptanceToHuntingDay(updated, huntingDay, author);

                            final List<ObservationSpecimen> updatedSpecimens = findSpecimens(updated);
                            assertThat(updatedSpecimens, hasSize(5));
                            assertSpecimens(updatedSpecimens, dto.getSpecimens(), dto.specimenOps()::equalContent);
                        });
                    });
                });
    }
}
