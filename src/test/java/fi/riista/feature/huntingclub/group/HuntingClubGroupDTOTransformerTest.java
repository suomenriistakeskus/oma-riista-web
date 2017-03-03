package fi.riista.feature.huntingclub.group;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.HibernateStatisticsAssertions;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HuntingClubGroupDTOTransformerTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubGroupDTOTransformer transformer;

    @Resource
    private HuntingClubGroupRepository repository;

    @Test
    @HibernateStatisticsAssertions(maxQueries = 8)
    public void testCountMembers_SmokeTest() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation member1 = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_JASEN);
        final Occupation member2 = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        onSavedAndAuthenticated(createUser(member1.getPerson()), () -> {
            final List<HuntingClubGroupDTO> result = findAllGroupsAndThenTransformToDTO();

            assertNotNull(result);
            assertEquals(1, result.size());

            final HuntingClubGroupDTO groupDTO = result.get(0);
            assertEquals(Long.valueOf(2), groupDTO.getMemberCount());
        });
    }

    @Test
    public void testCountMembers_TwoGroups() {
        withRhy(rhy -> {
            final HuntingClubGroup group1 = model().newHuntingClubGroup(model().newHuntingClub(rhy));
            final HuntingClubGroup group2 = model().newHuntingClubGroup(model().newHuntingClub(rhy));
            model().newHuntingClubGroupMember(group1, OccupationType.RYHMAN_JASEN);
            model().newHuntingClubGroupMember(group2, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<HuntingClubGroupDTO> result = findAllGroupsAndThenTransformToDTO();

                assertNotNull(result);
                assertEquals(2, result.size());

                result.forEach(dto -> assertEquals(Long.valueOf(1), dto.getMemberCount()));
            });
        });
    }

    @Test
    @HibernateStatisticsAssertions(maxQueries = 8)
    public void testCountMembers_SamePersonTwoRoles() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Person person = model().newPerson();
        final Occupation member1 = model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_JASEN);
        final Occupation member2 = model().newHuntingClubGroupMember(person, group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        onSavedAndAuthenticated(createUser(person), () -> {
            final List<HuntingClubGroupDTO> result = findAllGroupsAndThenTransformToDTO();

            assertNotNull(result);
            assertEquals(1, result.size());

            final HuntingClubGroupDTO groupDTO = result.get(0);
            assertEquals(Long.valueOf(1), groupDTO.getMemberCount());
        });
    }

    @Test
    public void testCountMembers_HandleSoftDelete() {
        final HuntingClubGroup group = model().newHuntingClubGroup();
        final Occupation member1 = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_JASEN);
        final Occupation member2 = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        member2.softDelete();

        onSavedAndAuthenticated(createUser(member1.getPerson()), () -> {
            final List<HuntingClubGroupDTO> result = findAllGroupsAndThenTransformToDTO();

            assertNotNull(result);
            assertEquals(1, result.size());

            final HuntingClubGroupDTO groupDTO = result.get(0);
            assertEquals(Long.valueOf(1), groupDTO.getMemberCount());
        });
    }

    @Test
    public void testHuntingDaysExists_deerGroupNoHarvests() {
        doTestHuntingDaysExists(false, GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER, false);
    }

    @Test
    public void testHuntingDaysExists_deerGroupHasHarvests() {
        doTestHuntingDaysExists(true, GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER, true);
    }

    @Test
    public void testHuntingDaysExists_mooseGroupNoHarvests() {
        doTestHuntingDaysExists(true, GameSpecies.OFFICIAL_CODE_MOOSE, false);
    }

    @Test
    public void testHuntingDaysExists_mooseGroupHasHarvests() {
        doTestHuntingDaysExists(true, GameSpecies.OFFICIAL_CODE_MOOSE, true);
    }

    private void doTestHuntingDaysExists(final boolean expectedHuntingDaysExist,
                                         final int speciesCode,
                                         final boolean hasHarvests) {

        final GameSpecies species = model().newGameSpecies(speciesCode);
        final HuntingClubGroup group = model().newHuntingClubGroup(species);
        final Occupation member = model().newHuntingClubGroupMember(group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, DateUtil.today());
        if (hasHarvests) {
            model().newHarvest(species, member.getPerson(), huntingDay);
        }

        model().newGroupHuntingDay(group, DateUtil.today().minusDays(1));

        onSavedAndAuthenticated(createUser(member.getPerson()), () -> {
            final List<HuntingClubGroupDTO> result = findAllGroupsAndThenTransformToDTO();

            assertNotNull(result);
            assertEquals(1, result.size());

            final HuntingClubGroupDTO groupDTO = result.get(0);
            assertEquals(expectedHuntingDaysExist, groupDTO.isHuntingDaysExist());
        });
    }

    private List<HuntingClubGroupDTO> findAllGroupsAndThenTransformToDTO() {
        return callInTransaction(() -> transformer.apply(repository.findAll()));
    }

}
