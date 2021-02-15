package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUser.Role;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObservationModifierInfoTest {

    @Test
    public void testAuthorOrObserver_notCarnivoreAuthority() {
        final SystemUser user = newUser(Role.ROLE_USER);

        final ObservationModifierInfo info = ObservationModifierInfo.builder()
                .withActiveUser(user)
                .withAuthorOrObserver(true)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(false)
                .build();

        assertEquals(user, info.getActiveUser());
        assertFalse(info.isModerator());
        assertTrue(info.isAuthorOrObserver());
        assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        assertFalse(info.canUpdateCarnivoreFields());
    }

    @Test
    public void testAuthorOrObserver_asCarnivoreAuthority() {
        final SystemUser user = newUser(Role.ROLE_USER);

        final ObservationModifierInfo info = ObservationModifierInfo.builder()
                .withActiveUser(user)
                .withAuthorOrObserver(true)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(true)
                .build();

        assertEquals(user, info.getActiveUser());
        assertFalse(info.isModerator());
        assertTrue(info.isAuthorOrObserver());
        assertTrue(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        assertTrue(info.canUpdateCarnivoreFields());
    }

    @Test
    public void testCarnivoreAuthority_notAuthorNorObserver() {
        final SystemUser user = newUser(Role.ROLE_USER);

        final ObservationModifierInfo info = ObservationModifierInfo.builder()
                .withActiveUser(user)
                .withAuthorOrObserver(false)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(true)
                .build();

        assertEquals(user, info.getActiveUser());
        assertFalse(info.isModerator());
        assertFalse(info.isAuthorOrObserver());
        assertTrue(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        assertFalse(info.canUpdateCarnivoreFields());
    }

    @Test
    public void testAdmin() {
        testAdminOrModerator(newUser(Role.ROLE_ADMIN));
    }

    @Test
    public void testModerator() {
        testAdminOrModerator(newUser(Role.ROLE_MODERATOR));
    }

    private static void testAdminOrModerator(final SystemUser user) {
        final ObservationModifierInfo info = ObservationModifierInfo.builder()
                .withActiveUser(user)
                .withAuthorOrObserver(false)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(false)
                .build();

        assertEquals(user, info.getActiveUser());
        assertTrue(info.isModerator());
        assertFalse(info.isAuthorOrObserver());
        assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        assertFalse(info.canUpdateCarnivoreFields());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdmin_asAuthorOrObserver() {
        testAdminOrModerator_asAuthorOrObserver(newUser(Role.ROLE_ADMIN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModerator_asAuthorOrObserver() {
        testAdminOrModerator_asAuthorOrObserver(newUser(Role.ROLE_MODERATOR));
    }

    private static void testAdminOrModerator_asAuthorOrObserver(final SystemUser user) {
        ObservationModifierInfo.builder()
                .withActiveUser(user)
                .withAuthorOrObserver(true)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(false)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdmin_asCarnivoreAuthority() {
        testAdminOrModerator_asCarnivoreAuthority(newUser(Role.ROLE_ADMIN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModerator_asCarnivoreAuthority() {
        testAdminOrModerator_asCarnivoreAuthority(newUser(Role.ROLE_MODERATOR));
    }

    private static void testAdminOrModerator_asCarnivoreAuthority(final SystemUser user) {
        ObservationModifierInfo.builder()
                .withActiveUser(user)
                .withAuthorOrObserver(false)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(true)
                .build();
    }

    private static SystemUser newUser(final Role role) {
        final SystemUser user = new SystemUser();
        user.setRole(role);
        return user;
    }
}
