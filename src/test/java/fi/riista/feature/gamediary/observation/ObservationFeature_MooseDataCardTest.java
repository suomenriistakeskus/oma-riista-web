package fi.riista.feature.gamediary.observation;

import fi.riista.feature.gamediary.fixture.ObservationDTOBuilderForTests;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.feature.gamediary.observation.ObservationType.JALKI;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.util.DateUtil.today;

public class ObservationFeature_MooseDataCardTest extends ObservationFeatureTestBase
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    // Test that observation is not mutated (except for description/images) when group is created
    // within moose data card import.
    @Test
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard_asClubContact() {
        testUpdateObservation_whenGroupOriginatingFromMooseDataCard(false);
    }

    @Test
    public void testUpdateObservation_whenGroupOriginatingFromMooseDataCard_asModerator() {
        testUpdateObservation_whenGroupOriginatingFromMooseDataCard(true);
    }

    private void testUpdateObservation_whenGroupOriginatingFromMooseDataCard(final boolean moderator) {
        createObservationMetaF(true, NAKO).consumeBy(m -> withHuntingGroupFixture(m.getSpecies(), f -> {
            f.group.setFromMooseDataCard(true);

            final LocalDate today = today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);
            final GroupHuntingDay huntingDay2 = model().newGroupHuntingDay(f.group, today.minusDays(1));

            final Observation observation = model().newObservation(m.getSpecies(), f.clubContact, huntingDay);

            createObservationMetaF(true, JALKI).consumeBy(m2 -> {

                onSavedAndAuthenticated(moderator ? createNewModerator() : createUser(f.clubContact), () -> {

                    final ObservationDTO dto = m2.dtoBuilder(params)
                            .populateWith(observation)
                            .withAmountAndSpecimens(1)
                            .mutate()
                            .withAuthorInfo(f.groupMember)
                            .withActorInfo(f.groupMember)
                            .linkToHuntingDay(huntingDay2)
                            .build();

                    invokeUpdateObservation(dto);

                    final ObservationDTOBuilderForTests expectations = moderator
                            ? m2.dtoBuilder(params).populateWith(dto).withDescription(observation.getDescription())
                            : m.dtoBuilder(params).populateWith(observation).withDescription(dto.getDescription());

                    final Person expectedAuthor = moderator ? f.groupMember : f.clubContact;

                    doUpdateAssertions(expectations.build(), expectedAuthor, expectedAuthor, 1);
                });
            });
        }));
    }
}
