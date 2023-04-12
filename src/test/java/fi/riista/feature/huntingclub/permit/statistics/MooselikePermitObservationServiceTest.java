package fi.riista.feature.huntingclub.permit.statistics;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.MooselikePermitObservationSummaryDTO;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class MooselikePermitObservationServiceTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MooselikePermitObservationService service;

    // MOOSE

    @Test
    @Transactional
    public void testGetObservationSummary_oneGroup_moose() {
        withMooseHuntingGroupFixture(fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
            createObservation(fixture.species, fixture.groupMember, huntingDay);

            onSavedAndAuthenticated(createNewUser("contact", fixture.permit.getOriginalContactPerson()), () -> {
                final MooselikePermitObservationSummaryDTO dto =
                        service.getObservationSummaryDTO(fixture.permit, fixture.species.getOfficialCode());

                assertMooseValues(1, dto);
            });
        });
    }

    @Test
    @Transactional
    public void testGetObservationSummary_otherGroupUnderSamePermit_moose() {
        withMooseHuntingGroupFixture(fixture -> {
            withHuntingGroupFixture(fixture.speciesAmount, otherFixture -> {
                final GroupHuntingDay otherFixtureDay = model().newGroupHuntingDay(otherFixture.group, today());
                createObservation(otherFixture.species, otherFixture.groupMember, otherFixtureDay);

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
                createObservation(fixture.species, fixture.groupMember, huntingDay);

                onSavedAndAuthenticated(createNewUser("contact", fixture.permit.getOriginalContactPerson()), () -> {
                    final MooselikePermitObservationSummaryDTO dto =
                            service.getObservationSummaryDTO(fixture.permit, fixture.species.getOfficialCode());
                    assertMooseValues(2, dto);
                });
            });
        });
    }

    @Test
    @Transactional
    public void testGetObservationSummary_observationsInOtherPermitNotCounted_moose() {
        withMooseHuntingGroupFixture(fixture -> {
            withHuntingGroupFixture(fixture.rhy, fixture.species, otherFixture -> {
                final GroupHuntingDay otherFixtureDay = model().newGroupHuntingDay(otherFixture.group, today());
                createObservation(otherFixture.species, otherFixture.groupMember, otherFixtureDay);

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
                createObservation(fixture.species, fixture.groupMember, huntingDay);

                onSavedAndAuthenticated(createNewUser("contact", fixture.permit.getOriginalContactPerson()), () -> {
                    final MooselikePermitObservationSummaryDTO dto =
                            service.getObservationSummaryDTO(fixture.permit, fixture.species.getOfficialCode());
                    assertMooseValues(1, dto);
                });
            });
        });
    }

    // WHITE TAILED DEER

    @Test
    @Transactional
    public void testGetObservationSummary_oneGroup_whiteTailedDeer() {
        final GameSpecies deer = model().newGameSpeciesWhiteTailedDeer();
        withHuntingGroupFixture(deer, fixture -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
            createObservation(deer, fixture.groupMember, huntingDay);

            onSavedAndAuthenticated(createNewUser("contact", fixture.permit.getOriginalContactPerson()), () -> {
                final MooselikePermitObservationSummaryDTO dto =
                        service.getObservationSummaryDTO(fixture.permit, deer.getOfficialCode());

                assertDeerValues(1, dto);
            });
        });
    }

    @Test
    @Transactional
    public void testGetObservationSummary_otherGroupUnderSamePermit_whiteTailedDeer() {
        final GameSpecies deer = model().newGameSpeciesWhiteTailedDeer();
        withHuntingGroupFixture(deer, fixture -> {
            withHuntingGroupFixture(fixture.speciesAmount, otherFixture -> {
                final GroupHuntingDay otherFixtureDay = model().newGroupHuntingDay(otherFixture.group, today());
                createObservation(deer, otherFixture.groupMember, otherFixtureDay);

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
                createObservation(deer, fixture.groupMember, huntingDay);

                onSavedAndAuthenticated(createNewUser("contact", fixture.permit.getOriginalContactPerson()), () -> {
                    final MooselikePermitObservationSummaryDTO dto =
                            service.getObservationSummaryDTO(fixture.permit, deer.getOfficialCode());
                    assertDeerValues(2, dto);
                });
            });
        });
    }

    @Test
    @Transactional
    public void testGetObservationSummary_observationsInOtherPermitNotCounted_whiteTailedDeer() {
        final GameSpecies deer = model().newGameSpeciesWhiteTailedDeer();
        withHuntingGroupFixture(deer, fixture -> {
            withHuntingGroupFixture(fixture.rhy, deer, otherFixture -> {
                final GroupHuntingDay otherFixtureDay = model().newGroupHuntingDay(otherFixture.group, today());
                createObservation(deer, otherFixture.groupMember, otherFixtureDay);

                final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today());
                createObservation(deer, fixture.groupMember, huntingDay);

                onSavedAndAuthenticated(createNewUser("contact", fixture.permit.getOriginalContactPerson()), () -> {
                    final MooselikePermitObservationSummaryDTO dto =
                            service.getObservationSummaryDTO(fixture.permit, deer.getOfficialCode());
                    assertDeerValues(1, dto);
                });
            });
        });
    }

    private Observation createObservation(final GameSpecies species, final Person author,
                                          final GroupHuntingDay huntingDay) {

        final Observation observation = model().newObservation(species, author, huntingDay);
        observation.setMooselikeMaleAmount(1);
        observation.setMooselikeFemaleAmount(2);
        observation.setMooselikeFemale1CalfAmount(3);  // 2x3 -> 6
        observation.setMooselikeFemale2CalfsAmount(4); // 3x4 -> 12
        observation.setMooselikeFemale3CalfsAmount(5); // 4x5 -> 20
        observation.setMooselikeFemale4CalfsAmount(species.isMoose() ? null : 6); // 5x6 -> 30 or null
        observation.setMooselikeCalfAmount(7);
        observation.setMooselikeUnknownSpecimenAmount(8);

        observation.setAmount(species.isMoose() ? 56 : 86);
        return observation;
    }

    private void assertMooseValues(final int observationCount, final MooselikePermitObservationSummaryDTO dto) {
        assertThat(dto.getAdultMale(), equalTo(observationCount * 1));
        assertThat(dto.getAdultFemaleNoCalfs(), equalTo(observationCount * 2));
        assertThat(dto.getAdultFemaleOneCalf(), equalTo(observationCount * 3));
        assertThat(dto.getAdultFemaleTwoCalfs(), equalTo(observationCount * 4));
        assertThat(dto.getAdultFemaleThreeCalfs(), equalTo(observationCount * 5));

        assertThat(dto.getAdultFemaleFourCalfs(), equalTo(observationCount * 0));

        assertThat(dto.getSolitaryCalf(), equalTo(observationCount * 7));
        assertThat(dto.getUnknown(), equalTo(observationCount * 8));
    }

    private void assertDeerValues(final int observationCount, final MooselikePermitObservationSummaryDTO dto) {
        assertThat(dto.getAdultMale(), equalTo(observationCount * 1));
        assertThat(dto.getAdultFemaleNoCalfs(), equalTo(observationCount * 2));
        assertThat(dto.getAdultFemaleOneCalf(), equalTo(observationCount * 3));
        assertThat(dto.getAdultFemaleTwoCalfs(), equalTo(observationCount * 4));
        assertThat(dto.getAdultFemaleThreeCalfs(), equalTo(observationCount * 5));
        assertThat(dto.getAdultFemaleFourCalfs(), equalTo(observationCount * 6));
        assertThat(dto.getSolitaryCalf(), equalTo(observationCount * 7));
        assertThat(dto.getUnknown(), equalTo(observationCount * 8));
    }
}
