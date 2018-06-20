package fi.riista.feature.account;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountDTOBuilderTest extends EmbeddedDatabaseTest {

    @Resource
    private PersonRepository personRepository;

    @Test
    public void testClubOccupations() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final HuntingClub club1 = model().newHuntingClub(rhy);
        final HuntingClubGroup club1Group1 = model().newHuntingClubGroup(club1);

        final HuntingClub club2 = model().newHuntingClub(rhy);
        final HuntingClubGroup club2Group1 = model().newHuntingClubGroup(club2);
        final HuntingClubGroup club2Group2 = model().newHuntingClubGroup(club2);

        final HuntingClub club3 = model().newHuntingClub(rhy);

        final Person p = model().newPerson();
        final Occupation occupationClub1 = model().newOccupation(club1, p, OccupationType.SEURAN_JASEN);
        final Occupation occupationClub1Group1 = model().newOccupation(club1Group1, p, OccupationType.RYHMAN_JASEN);

        final Occupation occupationClub2 = model().newOccupation(club2, p, OccupationType.SEURAN_YHDYSHENKILO);
        final Occupation occupationClub2Group1 = model().newOccupation(club2Group1, p, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation occupationClub2Group2 = model().newOccupation(club2Group2, p, OccupationType.RYHMAN_JASEN);

        final Occupation occupationClub3 = model().newOccupation(club3, p, OccupationType.SEURAN_YHDYSHENKILO);

        persistInNewTransaction();

        runInTransaction(() -> {
            Person reloadedPerson = personRepository.getOne(p.getId());
            AccountDTO accountDTO = AccountDTOBuilder.create().withPerson(reloadedPerson).build();

            List<MyClubOccupationDTO> clubOccupations = accountDTO.getClubOccupations();
            assertEquals(3, clubOccupations.size());

            Map<Long, MyClubOccupationDTO> clubMap = F.indexById(clubOccupations);
            assertClubOccupation(clubMap.get(occupationClub1.getId()), club1, occupationClub1Group1);
            assertClubOccupation(clubMap.get(occupationClub2.getId()), club2, occupationClub2Group1, occupationClub2Group2);
            assertClubOccupation(clubMap.get(occupationClub3.getId()), club3);
        });
    }


    @Test
    public void testInvitedToClubAndHuntingLeaderOfGroup() {
        final HuntingClub club1 = model().newHuntingClub();
        final HuntingClubGroup club1Group1 = model().newHuntingClubGroup(club1);

        final Person p = model().newPerson();
        model().newOccupation(club1Group1, p, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistInNewTransaction();

        runInTransaction(() -> {
            Person reloadedPerson = personRepository.getOne(p.getId());
            AccountDTO accountDTO = AccountDTOBuilder.create().withPerson(reloadedPerson).build();

            List<MyClubOccupationDTO> clubOccupations = accountDTO.getClubOccupations();
            assertEquals(0, clubOccupations.size());
        });
    }

    private static void assertClubOccupation(MyClubOccupationDTO dto, HuntingClub club, Occupation... groupOccupations) {
        assertEquals(club.getId(), dto.getOrganisation().getId());

        assertNotNull(dto.getGroupOccupations());

        Set<Long> dtoGroupIds = F.getUniqueIds(dto.getGroupOccupations());
        Set<Long> expectedGroupIds = F.getUniqueIds(groupOccupations);
        assertEquals(expectedGroupIds, dtoGroupIds);
    }
}
