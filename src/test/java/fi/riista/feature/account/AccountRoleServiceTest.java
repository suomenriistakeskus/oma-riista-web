package fi.riista.feature.account;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AccountRoleServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private AccountRoleService accountRoleService;

    @Test
    public void testClubLeader() {
        withPerson(person -> withRhy(rhy -> {
            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);

            model().newOccupation(club1, person, OccupationType.SEURAN_YHDYSHENKILO);
            final Occupation club2Occupation = model().newOccupation(club2, person, OccupationType.SEURAN_YHDYSHENKILO);
            club2Occupation.softDelete();

            onSavedAndAuthenticated(createUser(person), tx(user -> {
                final List<AccountRoleDTO> roles = accountRoleService.getRoles(user);
                assertEquals(2, roles.size());

                assertEquals(ImmutableMap.of(
                        "personId", person.getId()),
                        roles.get(0).getContext());

                assertEquals(ImmutableMap.<String, Object>of(
                        "clubId", club1.getId(),
                        "nameFI", club1.getNameFinnish(),
                        "nameSV", club1.getNameSwedish()),
                        roles.get(1).getContext());
            }));
        }));
    }

    @Test
    public void testInvitedToClubAndLeaderOfGroup() {
        withPerson(person -> {
            model().newOccupation(model().newHuntingClubGroup(), person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), tx(user -> {
                final List<AccountRoleDTO> roles = accountRoleService.getRoles(user);
                assertEquals(1, roles.size());

                assertEquals(ImmutableMap.<String, Object>of("personId", person.getId()), roles.get(0).getContext());
            }));
        });
    }

    @Test
    public void testDeactivatedClub() {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            club.setActive(false);
            model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

            onSavedAndAuthenticated(createUser(person), tx(user -> {
                final List<AccountRoleDTO> roles = accountRoleService.getRoles(user);
                assertEquals(1, roles.size());
            }));
        });
    }

    @Test
    public void testMemberOfClubAndLeaderOfGroup() {
        doTest(DateUtil.huntingYear(), OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
    }

    @Test
    public void testMemberOfClubAndLeaderOfGroupPreviousYear() {
        doTest(DateUtil.huntingYear() - 1, OccupationType.SEURAN_JASEN);
    }

    @Test
    public void testMemberOfClubAndLeaderOfGroupNextYear() {
        doTest(DateUtil.huntingYear() + 1, OccupationType.SEURAN_JASEN);
    }

    private void doTest(final int huntingYear, final OccupationType expectedOccupationType) {
        withPerson(person -> {
            final HuntingClub club = model().newHuntingClub();
            model().newOccupation(club, person, OccupationType.SEURAN_JASEN);

            final HuntingClubGroup group = model().newHuntingClubGroup(club);
            group.setHuntingYear(huntingYear);
            model().newOccupation(group, person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), tx(user -> {
                final List<AccountRoleDTO> roles = accountRoleService.getRoles(user);
                assertEquals(2, roles.size());

                assertEquals(ImmutableMap.<String, Object>of(
                        "personId", person.getId()),
                        roles.get(0).getContext());

                assertEquals(expectedOccupationType.name(), roles.get(1).getType());
                assertEquals(ImmutableMap.<String, Object>of(
                        "clubId", club.getId(),
                        "nameFI", club.getNameFinnish(),
                        "nameSV", club.getNameSwedish()),
                        roles.get(1).getContext());
            }));
        });
    }
}
