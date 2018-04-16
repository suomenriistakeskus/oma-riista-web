package fi.riista.feature.huntingclub.moosedatacard;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser.Role;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.jpa.HibernateStatisticsAssertions;
import org.junit.Test;

import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class MooseDataCardImportAuthorizationTest extends EmbeddedDatabaseTest {

    @HibernateStatisticsAssertions(maxQueries = 1)
    @Test
    public void testForEntity_withAdmin() {
        testForEntity_withUserRole(Role.ROLE_ADMIN);
    }

    @HibernateStatisticsAssertions(maxQueries = 1)
    @Test
    public void testForEntity_withModerator() {
        testForEntity_withUserRole(Role.ROLE_MODERATOR);
    }

    private void testForEntity_withUserRole(final Role role) {
        final MooseDataCardImport imp = model().newMooseDataCardImport(model().newHuntingClubGroup());

        ImmutableMap.of(CREATE, true, READ, true, UPDATE, false, DELETE, true).forEach((permission, shouldPass) -> {
            onSavedAndAuthenticated(createNewUser(role), tx(() -> assertHasPermission(shouldPass, imp, permission)));
        });
    }

    @HibernateStatisticsAssertions(maxQueries = 7)
    @Test
    public void testForEntity_withGroupMember() {
        testForEntity_withGroupMember(OccupationType.RYHMAN_JASEN);
    }

    @HibernateStatisticsAssertions(maxQueries = 6)
    @Test
    public void testForEntity_withGroupLeader() {
        testForEntity_withGroupMember(OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
    }

    private void testForEntity_withGroupMember(final OccupationType occupationType) {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final MooseDataCardImport imp = model().newMooseDataCardImport(group);

        ImmutableMap.of(CREATE, false, READ, true, UPDATE, false, DELETE, false).forEach((permission, shouldPass) -> {
            withPerson(person -> {

                model().newOccupation(club, person, OccupationType.SEURAN_JASEN);
                model().newOccupation(group, person, occupationType);

                onSavedAndAuthenticated(createUser(person), tx(() -> assertHasPermission(shouldPass, imp, permission)));
            });
        });
    }

    @HibernateStatisticsAssertions(maxQueries = 7)
    @Test
    public void testForEntity_withClubMember() {
        testForEntity_withClubMember(false);
    }

    @HibernateStatisticsAssertions(maxQueries = 5)
    @Test
    public void testForEntity_withClubContact() {
        testForEntity_withClubMember(true);
    }

    private void testForEntity_withClubMember(final boolean isClubContact) {
        final HuntingClub club = model().newHuntingClub();
        final MooseDataCardImport imp = model().newMooseDataCardImport(model().newHuntingClubGroup(club));
        final OccupationType role = isClubContact ? OccupationType.SEURAN_YHDYSHENKILO : OccupationType.SEURAN_JASEN;

        ImmutableMap.of(READ, isClubContact, CREATE, false, UPDATE, false, DELETE, false)
                .forEach((permission, shouldPass) -> withPerson(person -> {

                    model().newOccupation(club, person, role);

                    onSavedAndAuthenticated(createUser(person), tx(() -> {
                        assertHasPermission(shouldPass, imp, permission);
                    }));
                }));
    }

}
