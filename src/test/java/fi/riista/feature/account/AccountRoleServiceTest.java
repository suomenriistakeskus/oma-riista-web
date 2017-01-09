package fi.riista.feature.account;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.AccountRoleDTO;
import fi.riista.feature.account.AccountRoleService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

                final AccountRoleDTO.ContextDTO personContext = roles.get(0).getContext();
                assertEquals(person.getId(), personContext.getPersonId());
                assertNull(personContext.getClubId());

                final AccountRoleDTO.ContextDTO clubContext = roles.get(1).getContext();
                assertEquals(club1.getId(), clubContext.getClubId());
                assertNull(clubContext.getPersonId());
            }));
        }));
    }

    @Test
    public void testInvitedToClubAndMemberOfGroup() {
        withPerson(person -> {
            model().newOccupation(model().newHuntingClubGroup(), person, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

            onSavedAndAuthenticated(createUser(person), tx(user -> {
                final List<AccountRoleDTO> roles = accountRoleService.getRoles(user);
                assertEquals(1, roles.size());

                final AccountRoleDTO.ContextDTO personContext = roles.get(0).getContext();
                assertEquals(person.getId(), personContext.getPersonId());
                assertNull(personContext.getClubId());
            }));
        });
    }

}
