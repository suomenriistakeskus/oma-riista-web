package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;

import static fi.riista.feature.organization.rhy.huntingcontrolevent.mobile.MobileHuntingControlSpecVersion.MOST_RECENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class MobileHuntingControlEventAuthorizationTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileHuntingControlEventFeature feature;

    @Test
    public void testGetEvents_coordinator_hasNoData() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, MOST_RECENT);
                assertThat(events, hasSize(0));
            });
        });
    }

    @Test
    public void testGetEvents_user_hasNoData() {
        withRhy(rhy -> {
            final Person user = model().newPerson();
            onSavedAndAuthenticated(createUser(user), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, MOST_RECENT);
                assertThat(events, hasSize(0));
            });
        });
    }

    @Test
    public void testGetEvents_gameWarden_hasRhyData() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, MOST_RECENT);
                assertThat(events, hasSize(1));
            });
        });
    }

    @Test
    public void testGameWardenCanGetHunterDataByHunterNumber() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            final Person user = model().newPerson();
            createNewUser("user1", user);
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final MobileHuntingControlHunterInfoDTO info = feature.getHunterInfoByHunterNumber(user.getHunterNumber());
                assertThat(info.getHunterNumber(), is(equalTo(user.getHunterNumber())));
            });
        });
    }

    @Test
    public void testGameWardenCanGetHunterDataBySsn() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            final Person user = model().newPerson();
            createNewUser("user1", user);
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final MobileHuntingControlHunterInfoDTO info = feature.getHunterInfoBySsn(user.getSsn());
                assertThat(info.getHunterNumber(), is(equalTo(user.getHunterNumber())));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUserCantGetHunterDataByHunterNumber() {
        withRhy(rhy -> {
            final Person user = model().newPerson();
            onSavedAndAuthenticated(createUser(user), () -> {
                feature.getHunterInfoByHunterNumber(user.getHunterNumber());
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUserCantGetHunterDataBySsn() {
        withRhy(rhy -> {
            final Person user = model().newPerson();
            onSavedAndAuthenticated(createUser(user), () -> {
                feature.getHunterInfoBySsn(user.getSsn());
            });
        });
    }
}
