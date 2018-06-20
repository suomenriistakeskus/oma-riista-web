package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.rules.HibernateStatisticsAssertions;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MooseHuntingSummaryDTOTransformerTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MooseHuntingSummaryDTOTransformer transformer;

    @Resource
    private MooseHuntingSummaryRepository repository;

    @Test
    @HibernateStatisticsAssertions(queryCount = 5)
    public void testQueryCountWithOneSummary() {
        withMooseHuntingGroupFixture(fixture -> {
            persistInNewTransaction();
            createSummary(fixture, false);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<MooseHuntingSummaryDTO> result =
                        callInTransaction(() -> transformer.transform(repository.findAll()));
                assertEquals(1, result.size());
            });
        });
    }

    @Test
    @HibernateStatisticsAssertions(queryCount = 6)
    public void testQueryCountWithMultipleSummaries() {
        withMooseHuntingGroupFixture(fixture -> {
            persistInNewTransaction();
            createSummary(fixture, false);

            for (int i = 0; i < 10; i++) {
                withHuntingGroupFixture(fixture.rhy, fixture.species, fixture2 -> {
                    persistInNewTransaction();
                    createSummary(fixture2, true);
                });
            }

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<MooseHuntingSummaryDTO> result =
                        callInTransaction(() -> transformer.transform(repository.findAll()));

                assertEquals(11, result.size());
            });
        });
    }

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
            createSummary(fixture, false);

            onSavedAndAuthenticated(createUserFn.apply(fixture), () -> {
                try {
                    long millis = dateToTest.getMillis();
                    DateTimeUtils.setCurrentMillisFixed(millis);
                    final List<MooseHuntingSummaryDTO> result =
                            callInTransaction(() -> transformer.transform(repository.findAll()));

                    assertEquals(1, result.size());
                    assertEquals(locked, result.get(0).isLocked());
                } finally {
                    DateTimeUtils.setCurrentMillisSystem();
                }
            });
        });
    }

    private void createSummary(final HuntingGroupFixture fixture, final boolean groupFromMooseDataCard) {
        model().newMooseHuntingSummary(fixture.permit, fixture.club, true);
        fixture.group.setFromMooseDataCard(groupFromMooseDataCard);
    }
}
