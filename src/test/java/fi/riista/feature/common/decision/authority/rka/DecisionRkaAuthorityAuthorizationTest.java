package fi.riista.feature.common.decision.authority.rka;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

public class DecisionRkaAuthorityAuthorizationTest extends EmbeddedDatabaseTest {

    @Test
    public void testModerator() {
        final DecisionRkaAuthority authority = model().newDecisionRkaAuthoriry(model().newRiistakeskuksenAlue());

        onSavedAndAuthenticated(createNewModerator(), () ->{
            testPermission(authority, CREATE, true);
            testPermission(authority, READ, true);
            testPermission(authority, UPDATE, true);
            testPermission(authority, DELETE, true);
        });
    }

    @Test
    public void testAdmin() {
        final DecisionRkaAuthority authority = model().newDecisionRkaAuthoriry(model().newRiistakeskuksenAlue());

        onSavedAndAuthenticated(createNewAdmin(), () ->{
            testPermission(authority, CREATE, true);
            testPermission(authority, READ, true);
            testPermission(authority, UPDATE, true);
            testPermission(authority, DELETE, true);
        });
    }

    @Test
    public void testOrdinaryUser() {
        final DecisionRkaAuthority authority = model().newDecisionRkaAuthoriry(model().newRiistakeskuksenAlue());

        onSavedAndAuthenticated(createNewUser(), () ->{
            testPermission(authority, CREATE, false);
            testPermission(authority, READ, false);
            testPermission(authority, UPDATE, false);
            testPermission(authority, DELETE, false);
        });
    }

    private void testPermission(final DecisionRkaAuthority authority, final Enum<?> permission, final boolean permitted) {
        onCheckingPermission(permission)
                .expect(permitted)
                .expectNumberOfQueriesAtMost(0)
                .apply(authority);
    }
}
