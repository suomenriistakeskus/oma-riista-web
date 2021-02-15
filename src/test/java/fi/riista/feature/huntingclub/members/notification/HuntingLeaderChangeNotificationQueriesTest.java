package fi.riista.feature.huntingclub.members.notification;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.times;
import static fi.riista.util.DateUtil.huntingYear;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

public class HuntingLeaderChangeNotificationQueriesTest
        extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin {

    @Resource
    private HuntingLeaderChangeNotificationQueries queries;

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_filteringByOccupationType() {
        withMooseHuntingGroupFixture(f -> {

            persistInNewTransaction();

            final List<LeaderEmailDTO> list = getGroupLeadersOfCurrentAndFutureHuntingYears(f.club);

            assertEquals(asList(newLeaderDTO(f.groupLeaderOccupation)), list);
        });
    }

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_filteringByClubId() {
        final GameSpecies species = model().newGameSpecies();

        withRhy(rhy -> withHuntingGroupFixture(rhy, species, f1 -> withHuntingGroupFixture(rhy, species, f2 -> {

            persistInNewTransaction();

            final List<LeaderEmailDTO> list = getGroupLeadersOfCurrentAndFutureHuntingYears(f1.club);

            assertEquals(asList(newLeaderDTO(f1.groupLeaderOccupation)), list);
        })));
    }

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_withMultipleClubs() {
        final GameSpecies species = model().newGameSpecies();

        withRhy(rhy -> withHuntingGroupFixture(rhy, species, f1 -> withHuntingGroupFixture(rhy, species, f2 -> {

            persistInNewTransaction();

            final List<LeaderEmailDTO> list = getGroupLeadersOfCurrentAndFutureHuntingYears(f1.club, f2.club);

            assertEquals(asList(newLeaderDTO(f1.groupLeaderOccupation), newLeaderDTO(f2.groupLeaderOccupation)), list);
        })));
    }

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_orderingOfLeaders() {
        withMooseHuntingGroupFixture(f -> {

            final Occupation leader2 = model().newHuntingClubGroupMember(f.group, RYHMAN_METSASTYKSENJOHTAJA);
            leader2.setCallOrder(2);

            final Occupation leader3 = model().newHuntingClubGroupMember(f.group, RYHMAN_METSASTYKSENJOHTAJA);
            leader3.setCallOrder(null);

            final Occupation leader4 = model().newHuntingClubGroupMember(f.group, RYHMAN_METSASTYKSENJOHTAJA);
            leader4.setCallOrder(3);

            final Occupation leader5 = model().newHuntingClubGroupMember(f.group, RYHMAN_METSASTYKSENJOHTAJA);
            leader5.setCallOrder(1);

            persistInNewTransaction();

            final List<LeaderEmailDTO> expected = Stream
                    .of(f.groupLeaderOccupation, leader5, leader2, leader4, leader3)
                    .map(HuntingLeaderChangeNotificationQueriesTest::newLeaderDTO)
                    .collect(toList());

            assertEquals(expected, getGroupLeadersOfCurrentAndFutureHuntingYears(f.club));
        });
    }

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_nextHuntingYearIncluded() {
        withHuntingGroupFixture(model().newHarvestPermitSpeciesAmount(huntingYear() + 1), f -> {

            persistInNewTransaction();

            final List<LeaderEmailDTO> list = getGroupLeadersOfCurrentAndFutureHuntingYears(f.club);

            assertEquals(asList(newLeaderDTO(f.groupLeaderOccupation)), list);
        });
    }

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_previousHuntingYearExcluded() {
        withHuntingGroupFixture(model().newHarvestPermitSpeciesAmount(huntingYear() - 1), f -> {

            persistInNewTransaction();

            assertEmpty(getGroupLeadersOfCurrentAndFutureHuntingYears(f.club));
        });
    }

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_filteringByPermitAttachment() {
        withMooseHuntingGroupFixture(f -> {

            f.group.updateHarvestPermit(null);

            persistInNewTransaction();

            assertEmpty(getGroupLeadersOfCurrentAndFutureHuntingYears(f.club));
        });
    }

    @Test
    public void testGetGroupLeadersOfCurrentAndFutureHuntingYears_occupationMustBeActive() {
        withMooseHuntingGroupFixture(f -> {

            f.groupLeaderOccupation.getLifecycleFields().setDeletionTime(DateUtil.now());

            persistInNewTransaction();

            assertEmpty(getGroupLeadersOfCurrentAndFutureHuntingYears(f.club));
        });
    }

    private List<LeaderEmailDTO> getGroupLeadersOfCurrentAndFutureHuntingYears(final HuntingClub... clubs) {
        return queries.getGroupLeadersOfCurrentAndFutureHuntingYears(F.getUniqueIds(clubs));
    }

    @Test
    public void testResolveHuntingGroups() {
        final GameSpecies species = model().newGameSpecies();

        // Create a multitude of species to test that a correct one is picked.
        times(30).run(model()::newGameSpecies);

        withRhy(rhy -> withHuntingGroupFixture(rhy, species,
                f1 -> withHuntingGroupFixture(rhy, species,
                        f2 -> withHuntingGroupFixture(rhy, species, f3 -> {

                            // f3 fixture is created for filtering purposes, it should not be present in results.

                            final Occupation f1Leader2 =
                                    model().newHuntingClubGroupMember(f1.group, RYHMAN_METSASTYKSENJOHTAJA);
                            f1Leader2.setCallOrder(null);

                            final Occupation f2Leader2 =
                                    model().newHuntingClubGroupMember(f2.group, RYHMAN_METSASTYKSENJOHTAJA);
                            f2Leader2.setCallOrder(1);

                            final Occupation f2Leader3 =
                                    model().newHuntingClubGroupMember(f2.group, RYHMAN_METSASTYKSENJOHTAJA);
                            f2Leader3.setCallOrder(null);

                            persistInNewTransaction();

                            final List<LeaderEmailDTO> leaders =
                                    getGroupLeadersOfCurrentAndFutureHuntingYears(f1.club, f2.club);

                            final List<GroupEmailDTO> expected = asList(
                                    newGroupDTO(f1.group, asList(
                                            newLeaderDTO(f1.groupLeaderOccupation), newLeaderDTO(f1Leader2))),
                                    newGroupDTO(f2.group, asList(
                                            newLeaderDTO(f2.groupLeaderOccupation),
                                            newLeaderDTO(f2Leader2),
                                            newLeaderDTO(f2Leader3))));

                            final List<GroupEmailDTO> results = queries.resolveHuntingGroups(leaders);

                            assertEquals(simplify(expected), simplify(results));
                            assertEquals(expected, results);
                        }))));
    }

    private static LeaderEmailDTO newLeaderDTO(final Occupation occupation) {
        assertEquals(RYHMAN_METSASTYKSENJOHTAJA, occupation.getOccupationType());

        final Person person = occupation.getPerson();

        return new LeaderEmailDTO(
                person.getFullName(),
                person.getHunterNumber(),
                occupation.getCallOrder(),
                occupation.getOrganisation().getId(),
                occupation.getModificationTime());
    }

    private static GroupEmailDTO newGroupDTO(final HuntingClubGroup group, final List<LeaderEmailDTO> leaders) {
        final GroupEmailDTO dto = new GroupEmailDTO();

        dto.setId(group.getId());
        dto.setClubId(group.getParentOrganisation().getId());
        dto.setRhyId(group.getHarvestPermit().getRhy().getId());

        dto.setNameFinnish(group.getNameFinnish());
        dto.setNameSwedish(group.getNameSwedish());

        dto.setHuntingYear(group.getHuntingYear());
        dto.setPermitNumber(group.getHarvestPermit().getPermitNumber());

        dto.setSpeciesNameFinnish(group.getSpecies().getNameFinnish());
        dto.setSpeciesNameSwedish(group.getSpecies().getNameSwedish());

        dto.setLeaders(leaders);

        return dto;
    }

    private static Map<Long, List<String>> simplify(final List<GroupEmailDTO> list) {
        return list.stream().collect(toMap(
                GroupEmailDTO::getId,
                group -> group.getLeaders().stream().map(LeaderEmailDTO::getHunterNumber).collect(toList())));
    }
}
