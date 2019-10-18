package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MooseHuntingSummaryDTOTransformerTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MooseHuntingSummaryDTOTransformer transformer;

    @Test
    public void testLockedByDate_clubContact_lastUnlockedDate() {
        testLocked(false, lastUnlockedDate(), f -> createUser(f.clubContact));
    }

    @Test
    public void testLockedByDate_clubContact_firstLockedDate() {
        testLocked(true, lastUnlockedDate().plusDays(1), f -> createUser(f.clubContact));
    }

    @Test
    public void testLockedByDate_moderator() {
        testLocked(false, lastUnlockedDate().plusDays(1), f -> createNewModerator());
    }

    private static DateTime lastUnlockedDate() {
        return new DateTime(DateUtil.huntingYear() + 1, 3, 31, 0, 0);
    }

    private void testLocked(final boolean locked, final DateTime dateToTest, final Function<HuntingGroupFixture, SystemUser> createUserFn) {
        withMooseHuntingGroupFixture(fixture -> {
            persistInNewTransaction();

            final MooseHuntingSummary summary = model().newMooseHuntingSummary(fixture.permit, fixture.club, true);
            fixture.group.setFromMooseDataCard(false);

            onSavedAndAuthenticated(createUserFn.apply(fixture), () -> {
                try {
                    long millis = dateToTest.getMillis();
                    DateTimeUtils.setCurrentMillisFixed(millis);

                    runInTransaction(() -> {
                        final MooseHuntingSummaryDTO dto = transformer.transform(summary);
                        assertEquals(locked, dto.isLocked());
                    });

                } finally {
                    DateTimeUtils.setCurrentMillisSystem();
                }
            });
        });
    }

}
