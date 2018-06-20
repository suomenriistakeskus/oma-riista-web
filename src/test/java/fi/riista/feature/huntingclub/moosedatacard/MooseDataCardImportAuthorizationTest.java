package fi.riista.feature.huntingclub.moosedatacard;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.account.user.SystemUser.Role;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class MooseDataCardImportAuthorizationTest extends EmbeddedDatabaseTest {

    @Test
    public void testWithAdmin() {
        testWithUserRole(Role.ROLE_ADMIN);
    }

    @Test
    public void testWithModerator() {
        testWithUserRole(Role.ROLE_MODERATOR);
    }

    private void testWithUserRole(final Role role) {
        final MooseDataCardImport imp = model().newMooseDataCardImport(model().newHuntingClubGroup());

        ImmutableMap.of(CREATE, true, READ, true, UPDATE, false, DELETE, true).forEach((permission, shouldPass) -> {
            onSavedAndAuthenticated(createNewUser(role), () -> {
                onCheckingPermission(permission).expect(shouldPass).expectNumberOfQueriesAtMost(1).apply(imp);
            });
        });
    }

    @Test
    public void testWithGroupMember() {
        testWithGroupOccupation(RYHMAN_JASEN, 7);
    }

    @Test
    public void testWithGroupLeader() {
        testWithGroupOccupation(RYHMAN_METSASTYKSENJOHTAJA, 6);
    }

    private void testWithGroupOccupation(final OccupationType occupationType, final int maxQueries) {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubGroup group = model().newHuntingClubGroup(club);
        final MooseDataCardImport imp = model().newMooseDataCardImport(group);

        ImmutableMap.of(CREATE, false, READ, true, UPDATE, false, DELETE, false)
                .forEach((permission, shouldPass) -> withPerson(person -> {

                    model().newOccupation(club, person, SEURAN_JASEN);
                    model().newOccupation(group, person, occupationType);

                    onSavedAndAuthenticated(createUser(person), () -> onCheckingPermission(permission)
                            .expect(shouldPass)
                            .expectNumberOfQueriesAtMost(maxQueries)
                            .apply(imp));
                }));
    }

    @Test
    public void testWithClubMember() {
        testWithClubOccupation(false, 7);
    }

    @Test
    public void testWithClubContact() {
        testWithClubOccupation(true, 5);
    }

    private void testWithClubOccupation(final boolean isClubContact, final int maxQueries) {
        final HuntingClub club = model().newHuntingClub();
        final MooseDataCardImport imp = model().newMooseDataCardImport(model().newHuntingClubGroup(club));

        ImmutableMap.of(READ, isClubContact, CREATE, false, UPDATE, false, DELETE, false)
                .forEach((permission, shouldPass) -> withPerson(person -> {

                    model().newOccupation(club, person, isClubContact ? SEURAN_YHDYSHENKILO : SEURAN_JASEN);

                    onSavedAndAuthenticated(createUser(person), () -> onCheckingPermission(permission)
                            .expect(shouldPass)
                            .expectNumberOfQueriesAtMost(maxQueries)
                            .apply(imp));
                }));
    }
}
