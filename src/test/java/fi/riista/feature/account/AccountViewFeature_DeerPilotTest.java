package fi.riista.feature.account;

import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccountViewFeature_DeerPilotTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    protected AccountViewFeature accountViewFeature;

    private HuntingGroupFixture fixture;

    @Before
    public void setup() {
        fixture = new HuntingGroupFixture(model());
    }

    @Test
    public void testUserNotInDeerPilot() {
        final Person author = fixture.groupLeader;
        final HttpServletRequest request = new MockHttpServletRequest();

        onSavedAndAuthenticated(createUser(author), () -> {
            final AccountDTO accountDTO = accountViewFeature.getActiveAccount(request);
            assertFalse(accountDTO.isDeerPilotUser());
        });
    }

    @Test
    public void testUserIsInDeerPilot() {
        final Person author = fixture.groupLeader;
        final HttpServletRequest request = new MockHttpServletRequest();

        model().newDeerPilot(fixture.permit);

        onSavedAndAuthenticated(createUser(author), () -> {
            final AccountDTO accountDTO = accountViewFeature.getActiveAccount(request);
            assertTrue(accountDTO.isDeerPilotUser());
        });
    }

}
