package fi.riista.feature.gamediary.observation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.observation.metadata.DynamicObservationFieldPresence;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfHuntingDayException;
import fi.riista.feature.huntingclub.hunting.day.PointOfTimeOutsideOfPermittedDatesException;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;

public class ObservationFeature_HuntingDayTest extends ObservationFeatureTestBase
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
        final Person actor = fixture.groupMember;

        createObservationMetaF(fixture.species, true, NAKO).consumeBy(m -> {

            onSavedAndAuthenticated(createUser(author), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(params)
                        .withAmountAndSpecimens(5)
                        .withActorInfo(actor)
                        .linkToHuntingDay(huntingDay)
                        .build();

                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                doCreateAssertions(
                        outputDto.getId(), inputDto, author, actor, assertAcceptedToHuntingDay(author, huntingDay));
            });
        });
    }

    @Test
    public void testCreateObservationForHuntingDay_asModerator() {
        final Person author = fixture.groupLeader;
        final Person actor = fixture.groupMember;

        createObservationMetaF(fixture.species, true, NAKO).consumeBy(m -> {

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final ObservationDTO inputDto = m.dtoBuilder(params)
                        .withAmountAndSpecimens(5)
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .linkToHuntingDay(huntingDay)
                        .build();

                final ObservationDTO outputDto = invokeCreateObservation(inputDto);

                inputDto.setDescription(null);

                doCreateAssertions(
                        outputDto.getId(), inputDto, author, actor, assertAcceptedToHuntingDay(null, huntingDay));
            });
        });
    }

    @Test(expected = ClubHuntingFinishedException.class)
    public void testCreateObservationForHuntingDay_whenHuntingFinished() {
        createObservationMetaF(fixture.species, true, NAKO).consumeBy(m -> {

            // Set club hunting finished.
            model().newModeratedBasicHuntingSummary(fixture.speciesAmount, fixture.club);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                invokeCreateObservation(m.dtoBuilder(params)
                        .withAmountAndSpecimens(1)
                        .linkToHuntingDay(huntingDay)
                        .build());
            });
        });
    }

    @Test(expected = PointOfTimeOutsideOfHuntingDayException.class)
    public void testCreateObservationForHuntingDay_whenPointOfTimeOutsideOfHuntingDay() {
        createObservationMetaF(fixture.species, true, NAKO).consumeBy(m -> {

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                invokeCreateObservation(m.dtoBuilder(params)
                        .withAmountAndSpecimens(1)
                        .linkToHuntingDay(huntingDay)
                        .withPointOfTime(huntingDay.getStartAsLocalDateTime().minusMinutes(1))
                        .build());
            });
        });
    }

    @Test(expected = PointOfTimeOutsideOfPermittedDatesException.class)
    public void testCreateObservationForHuntingDay_whenPointOfTimeOutsideOfPermitted() {
        createObservationMetaF(fixture.species, true, NAKO).consumeBy(m -> {

            final LocalDate date = fixture.speciesAmount.getBeginDate().minusDays(1);
            huntingDay.setStartDate(date);
            huntingDay.setEndDate(date);

            onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {
                invokeCreateObservation(m.dtoBuilder(params)
                        .withAmountAndSpecimens(1)
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

        createObservationMetaF(fixture.species, true, NAKO)
                .createSpecimensF(member, 5)
                .withGeoLocation(fixture.zoneCentroid)
                .consumeBy((m, fixture2) -> {

                    onSavedAndAuthenticated(createUser(leader), () -> {

                        final ObservationDTO dto = m.dtoBuilder(params)
                                .populateWith(fixture2.observation)
                                .populateSpecimensWith(fixture2.specimens)
                                .withActorInfo(leader)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        doUpdateAssertions(dto, member, leader, 1, assertAcceptedToHuntingDay(leader, huntingDay));
                    });
                });
    }

    @Test
    public void testUpdateObservation_linkToHuntingDay_asModerator() {
        final Person newAuthor = fixture.groupLeader;
        final Person newActor = fixture.groupMember;

        createObservationMetaF(fixture.species, true, NAKO)
                .createSpecimensF(fixture.clubContact, 5)
                .consumeBy((m, fixture2) -> {

                    onSavedAndAuthenticated(createNewModerator(), () -> {

                        final ObservationDTO dto = m.dtoBuilder(params)
                                .populateWith(fixture2.observation)
                                .populateSpecimensWith(fixture2.specimens)
                                .withAuthorInfo(newAuthor)
                                .withActorInfo(newActor)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        dto.setDescription(fixture2.observation.getDescription());

                        doUpdateAssertions(dto, newAuthor, newActor, 1, assertAcceptedToHuntingDay(null, huntingDay));
                    });
                });
    }

    @Test(expected = ObservationCanBeLinkedToHuntingDayOnlyWithinMooseHuntingException.class)
    public void testUpdateObservation_linkToHuntingDay_allowedOnlyWithinMooseHunting() {
        final Person author = fixture.groupLeader;

        createObservationMetaF(fixture.species, NAKO).createSpecimensF(author, 5).consumeBy((m, fixture2) -> {

            onSavedAndAuthenticated(createUser(author), () -> {

                invokeUpdateObservation(m.dtoBuilder(params)
                        .populateWith(fixture2.observation)
                        .populateSpecimensWith(fixture2.specimens)
                        .linkToHuntingDay(huntingDay)
                        .build());
            });
        });
    }

    @Test
    public void testUpdateObservation_acceptorNotChangedOnModeratorOverride() {
        final Person author = fixture.groupMember;

        createObservationMetaF(fixture.species, true, NAKO).createSpecimensF(author, 5).consumeBy((m, fixture2) -> {

            fixture2.observation.updateHuntingDayOfGroup(huntingDay, author);

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final ObservationDTO dto = m.dtoBuilder(params)
                        .populateWith(fixture2.observation)
                        .populateSpecimensWith(fixture2.specimens)
                        .mutateSpecimens()
                        .withAuthorInfo(fixture2.observation.getAuthor())
                        .withActorInfo(fixture2.observation.getActor())
                        .linkToHuntingDay(huntingDay)
                        .build();

                invokeUpdateObservation(dto);

                doUpdateAssertions(dto, author, author, 2, assertAcceptedToHuntingDay(author, huntingDay));
            });
        });
    }

    // Test that observation is not mutated (except for description/images) when hunting is finished.
    @Test
    public void testUpdateObservation_whenHuntingFinished() {
        final Person author = fixture.clubContact;

        createObservationMetaF(fixture.species, true, NAKO).consumeBy(m -> {

            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(fixture.group, today().minusDays(1));

            // Set club hunting finished.
            model().newModeratedBasicHuntingSummary(fixture.speciesAmount, fixture.club);

            final Observation observation = model().newObservation(fixture.species, author, huntingDay);

            createObservationMetaF(true, JALKI).consumeBy(m2 -> {

                onSavedAndAuthenticated(createUser(author), () -> {

                    final ObservationDTO dto = m2.dtoBuilder(params)
                            .populateWith(observation)
                            .withAmountAndSpecimens(5)
                            .mutate()
                            .withActorInfo(fixture.groupMember)
                            .linkToHuntingDay(huntingDay2)
                            .build();

                    invokeUpdateObservation(dto);

                    final ObservationDTO expectedValues = m.dtoBuilder(params)
                            .populateWith(observation)
                            .withDescription(dto.getDescription())
                            .build();

                    doUpdateAssertions(expectedValues, author, author, 1, o -> assertNull(o.getRhy()));
                });
            });
        });
    }

    // Test that observation is not mutated (except for linking to hunting day).
    @Test
    public void testUpdateLargeCarnivoreObservation_linkToHuntingDay_whenAuthorNotHavingCarnivoreAuthority() {
        final Person author = fixture.groupMember;

        createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, true, JALKI)
                .withLargeCarnivoreFieldsAs(DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY)
                .createSpecimensF(author, 5)
                .withGeoLocation(fixture.zoneCentroid)
                .withCarnivoreAuthority(true)
                .consumeBy((m, fxt2) -> {

                    fxt2.observation.setPointOfTime(huntingDay.getStartAsLocalDateTime().toDate());

                    onSavedAndAuthenticated(createUser(fixture.clubContact), () -> {

                        final ObservationDTO dto = m.dtoBuilder(params)
                                .populateWith(fxt2.observation)
                                .populateSpecimensWith(fxt2.specimens)
                                .mutate()
                                .mutateSpecimens()
                                .withAuthorInfo(fixture.groupLeader)
                                .withActorInfo(fixture.groupLeader)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        final ObservationDTO expectedValues = m.dtoBuilder(params)
                                .populateWith(fxt2.observation)
                                .populateSpecimensWith(fxt2.specimens)
                                .linkToHuntingDay(huntingDay)
                                .build();

                        doUpdateAssertions(expectedValues, author, author, 1);
                    });
                });
    }

    // Test that original author with carnivore authority can mutate observation within linking to hunting day.
    @Test
    public void testUpdateLargeCarnivoreObservation_linkToHuntingDay_withOriginalAuthor() {
        final Person author = fixture.groupLeader;

        model().newOccupation(fixture.rhy, author, OccupationType.PETOYHDYSHENKILO);

        createObservationMetaF(GameSpecies.OFFICIAL_CODE_WOLF, true, JALKI)
                .withLargeCarnivoreFieldsAs(DynamicObservationFieldPresence.VOLUNTARY_CARNIVORE_AUTHORITY)
                .createSpecimensF(author, 5)
                .withGeoLocation(fixture.zoneCentroid)
                .withCarnivoreAuthority(true)
                .consumeBy((m, fxt2) -> {

                    onSavedAndAuthenticated(createUser(author), () -> {

                        final ObservationDTO dto = m.dtoBuilder(params)
                                .populateWith(fxt2.observation)
                                .populateSpecimensWith(fxt2.specimens)
                                .mutateLargeCarnivoreFieldsAsserting(notNullValue())
                                .mutateSpecimens()
                                .linkToHuntingDay(huntingDay)
                                .build();

                        invokeUpdateObservation(dto);

                        doUpdateAssertions(dto, author, author, 2);
                    });
                });
    }
}
