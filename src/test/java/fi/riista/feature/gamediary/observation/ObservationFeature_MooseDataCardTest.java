package fi.riista.feature.gamediary.observation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.feature.common.entity.Required.YES;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooseAmountFieldsNotNull;
import static fi.riista.feature.gamediary.observation.ObservationTestAsserts.assertMooselikeAmounts;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ObservationFeature_MooseDataCardTest extends ObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    // Test that observation is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Test
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard_asClubContact() {
        withMooseHuntingGroupFixture(huntingFixt -> {

            huntingFixt.group.setFromMooseDataCard(true);

            final GameSpecies species = huntingFixt.species;
            final Person originalAuthor = huntingFixt.clubContact;

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixt.group, today);
            final GroupHuntingDay huntingDay2 =
                    model().newGroupHuntingDay(huntingFixt.group, today.minusDays(1));

            createObservationMetaF(species, MOOSE_HUNTING, NAKO)
                    .withMooselikeAmountFieldsAs(YES)
                    .createObservationFixture()
                    .withAuthor(originalAuthor)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createUser(originalAuthor), () -> {

                            final Observation original = obsFixt.observation;

                            final ObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(original)
                                    .mutate()
                                    .withAuthorInfo(huntingFixt.groupMember)
                                    .withActorInfo(huntingFixt.groupMember)
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
                                assertThat(updated.getPointOfTime(), equalTo(original.getPointOfTime()));

                                assertMooseAmountFieldsNotNull(updated, true);
                                assertMooselikeAmounts(original, updated);
                                assertThat(updated.getAmount(), equalTo(original.getSumOfMooselikeAmounts()));

                                assertAuthorAndActor(updated, F.getId(originalAuthor), F.getId(originalAuthor));

                                assertAcceptanceToHuntingDay(updated, huntingDay, originalAuthor);
                            });
                        });
                    });
        });
    }

    @Test
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard_asModerator() {
        withMooseHuntingGroupFixture(huntingFixt -> {

            huntingFixt.group.setFromMooseDataCard(true);

            final GameSpecies species = huntingFixt.species;

            final Person originalAuthor = huntingFixt.clubContact;
            final Person newAuthor = huntingFixt.groupMember;

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(huntingFixt.group, today);
            final GroupHuntingDay huntingDay2 =
                    model().newGroupHuntingDay(huntingFixt.group, today.minusDays(1));

            createObservationMetaF(species, MOOSE_HUNTING, NAKO)
                    .withMooselikeAmountFieldsAs(YES)
                    .createObservationFixture()
                    .withAuthor(originalAuthor)
                    .withGroupHuntingDay(huntingDay)
                    .consumeBy((obsMeta, obsFixt) -> {

                        onSavedAndAuthenticated(createNewModerator(), () -> {

                            final Observation original = obsFixt.observation;

                            final ObservationDTO dto = obsMeta.dtoBuilder()
                                    .populateWith(original)
                                    .mutate()
                                    .withAuthorInfo(newAuthor)
                                    .withActorInfo(newAuthor)
                                    .linkToHuntingDay(huntingDay2)
                                    .build();

                            invokeUpdateObservation(dto);

                            runInTransaction(() -> {
                                final Observation updated = observationRepo.getOne(dto.getId());
                                assertVersion(updated, 1);

                                // Assert that description is NOT changed.
                                assertThat(updated.getDescription(), equalTo(original.getDescription()));

                                // Assert that other observation fields are changed.

                                assertThat(updated.getGeoLocation(), equalTo(dto.getGeoLocation()));
                                assertThat(updated.getPointOfTime(), equalTo(DateUtil.toDateTimeNullSafe(dto.getPointOfTime())));

                                assertMooseAmountFieldsNotNull(updated, true);
                                assertMooselikeAmounts(dto, updated);
                                assertThat(updated.getAmount(), equalTo(dto.getSumOfMooselikeAmounts()));

                                assertAuthorAndActor(updated, F.getId(newAuthor), F.getId(newAuthor));

                                assertAcceptanceToHuntingDay(updated, huntingDay2, null);
                            });
                        });
                    });
        });
    }
}
