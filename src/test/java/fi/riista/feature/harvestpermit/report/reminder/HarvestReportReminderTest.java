package fi.riista.feature.harvestpermit.report.reminder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class HarvestReportReminderTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportReminderFeature harvestReportReminder;

    @Resource
    private HarvestRepository harvestRepository;

    private enum HarvestReportRequired {
        REQUIRED, NOT_REQUIRED;
    }

    private enum AuthorIsRegistered {
        REGISTERED, NOT_REGISTERED, REGISTERED_BUT_INACTIVE
    }

    private enum ShooterIsRegistered {
        REGISTERED, NOT_REGISTERED, REGISTERED_BUT_INACTIVE
    }

    private Map<Long, Set<String>> callSendRemindersAfterInitialDelay() {
        try {
            final DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.FIRST_REMINDER_DELAY).plusHours(1);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            return harvestReportReminder.sendReminders();

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testHarvestReportNotRequired() {
        createHarvest(HarvestReportRequired.NOT_REQUIRED, AuthorIsRegistered.REGISTERED);

        persistInNewTransaction();

        runInTransaction(() -> assertEquals(0, callSendRemindersAfterInitialDelay().size()));
    }

    @Test
    public void testHarvestReportNotRequired_linkedToListPermit() {
        final HarvestPermit permit = model().newHarvestPermit(true);

        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);
        harvest.setRhy(permit.getRhy());

        persistInNewTransaction();

        runInTransaction(() -> assertEquals(0, callSendRemindersAfterInitialDelay().size()));
    }

    @Test
    public void testHarvestReportNotRequired_linkedToSingleHarvestPermit() {
        final HarvestPermit permit = model().newHarvestPermit(false);

        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setRhy(permit.getRhy());

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getAuthor()));
    }

    @Test
    public void testNoShooter() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getAuthor()));
    }

    @Test
    public void testWithShooter() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED, ShooterIsRegistered.REGISTERED);

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getAuthor(), harvest.getActualShooter()));
    }

    @Test
    public void testHarvestReportIsDone() {
        Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        harvest.setHarvestReportAuthor(harvest.getAuthor());
        harvest.setHarvestReportDate(DateUtil.now());

        persistInNewTransaction();

        runInTransaction(() -> assertEquals(0, callSendRemindersAfterInitialDelay().size()));
    }

    @Test
    public void testAuthorIsNotRegistered() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.NOT_REGISTERED, ShooterIsRegistered.REGISTERED);

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getActualShooter()));
    }

    @Test
    public void testAuthorIsRegisteredButNotActive() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED_BUT_INACTIVE, ShooterIsRegistered.REGISTERED);

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getActualShooter()));
    }

    @Test
    public void testShooterIsRegisteredButNotActive() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED, ShooterIsRegistered.REGISTERED_BUT_INACTIVE);

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getAuthor()));
    }

    @Test
    public void testNobodyIsActive() {
        createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED_BUT_INACTIVE, ShooterIsRegistered.REGISTERED_BUT_INACTIVE);

        persistInNewTransaction();

        runInTransaction(() -> assertEquals(0, callSendRemindersAfterInitialDelay().size()));
    }

    @Test
    public void testShooterHasNoEmail() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED, ShooterIsRegistered.REGISTERED);
        harvest.getActualShooter().setEmail(null);

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getAuthor()));
    }

    @Test
    public void testAuthorHasNoEmail() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED, ShooterIsRegistered.REGISTERED);
        harvest.getAuthor().setEmail(null);

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay(), harvest.getActualShooter()));
    }

    @Test
    public void testAuthorAndShooterHasNoEmail() {
        final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED, ShooterIsRegistered.REGISTERED);
        harvest.getAuthor().setEmail("");
        harvest.getActualShooter().setEmail("");

        persistInNewTransaction();

        runInTransaction(() -> assertEmails(callSendRemindersAfterInitialDelay()));
    }

    @Test
    public void testFirstReminderIsNotSentBeforeInitialDelay() {
        try {
            createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);

            persistInNewTransaction();

            runInTransaction(() -> {
                DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.FIRST_REMINDER_DELAY).minusSeconds(1);
                DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

                Map<Long, Set<String>> res = harvestReportReminder.sendReminders();

                assertEquals(0, res.size());
            });

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testFirstReminderIsSentAfterInitialDelay() {
        try {
            final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);

            persistInNewTransaction();

            final DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.FIRST_REMINDER_DELAY).plusSeconds(1);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            runInTransaction(() -> {
                final Map<Long, Set<String>> res = harvestReportReminder.sendReminders();

                assertEquals(1, res.size());
                assertThat(res, hasKey(harvest.getId()));
            });

            runInTransaction(() -> {
                final DateTime reminderSentTime = harvestRepository.getOne(harvest.getId()).getEmailReminderSentTime();
                assertEquals(fakeNow, reminderSentTime);
            });

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testSecondReminderIsNotSentTooEarlyAfterPrevious() {
        try {
            final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
            harvest.setEmailReminderSentTime(getTestStartTime());

            persistInNewTransaction();

            final DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.REMINDER_INTERVAL).minusSeconds(1);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            runInTransaction(() -> {
                final Map<Long, Set<String>> res = harvestReportReminder.sendReminders();
                assertEquals(0, res.size());
            });

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testSecondReminderIsSentAfterDelay() {
        try {
            final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
            harvest.setEmailReminderSentTime(getTestStartTime());

            persistInNewTransaction();

            final DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.REMINDER_INTERVAL).plusSeconds(1);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            runInTransaction(() -> {
                final Map<Long, Set<String>> res = harvestReportReminder.sendReminders();

                assertEquals(1, res.size());
                assertThat(res, hasKey(harvest.getId()));
            });

            runInTransaction(() -> {
                final DateTime reminderSentTime = harvestRepository.getOne(harvest.getId()).getEmailReminderSentTime();
                assertEquals(fakeNow, reminderSentTime);
            });

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testLastReminderIsSent() {
        try {
            final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
            harvest.setEmailReminderSentTime(getTestStartTime());

            persistInNewTransaction();

            final DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.LAST_REMINDER_DELAY).minusSeconds(1);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            runInTransaction(() -> {
                final Map<Long, Set<String>> res = harvestReportReminder.sendReminders();

                assertEquals(1, res.size());
                assertThat(res, hasKey(harvest.getId()));
            });

            runInTransaction(() -> {
                final DateTime reminderSentTime = harvestRepository.getOne(harvest.getId()).getEmailReminderSentTime();
                assertEquals(fakeNow, reminderSentTime);
            });

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testNoMoreRemindersAreSent() {
        try {
            final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
            harvest.setEmailReminderSentTime(getTestStartTime());

            persistInNewTransaction();

            final DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.LAST_REMINDER_DELAY).plusSeconds(1);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            runInTransaction(() -> {
                Map<Long, Set<String>> res = harvestReportReminder.sendReminders();

                assertEquals(0, res.size());
            });

            runInTransaction(() -> {
                final DateTime reminderSentTime = harvestRepository.getOne(harvest.getId()).getEmailReminderSentTime();
                assertEquals(getTestStartTime(), reminderSentTime);
            });

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    public void testUpdateOnlyThoseWhichEmailIsSent() {
        try {
            final Harvest harvest = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.REGISTERED);
            final Harvest harvest2 = createHarvest(HarvestReportRequired.REQUIRED, AuthorIsRegistered.NOT_REGISTERED);

            persistInNewTransaction();

            final DateTime fakeNow = getTestStartTime().plus(HarvestReportReminderFeature.FIRST_REMINDER_DELAY).plusSeconds(1);
            DateTimeUtils.setCurrentMillisFixed(fakeNow.getMillis());

            runInTransaction(() -> {
                final Map<Long, Set<String>> res = harvestReportReminder.sendReminders();

                assertEquals(1, res.size());
                assertThat(res, hasKey(harvest.getId()));
            });

            runInTransaction(() -> assertNull(harvestRepository.getOne(harvest2.getId()).getEmailReminderSentTime()));

        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    private static void assertEmails(Map<Long, Set<String>> res, Person... expectedPersons) {
        final Set<String> expectedEmails = Stream.of(expectedPersons).map(Person::getEmail).collect(toSet());
        final Set<String> emails = Sets.newHashSet(Iterables.concat(res.values()));

        assertEquals(expectedEmails, emails);
    }

    private Harvest createHarvest(HarvestReportRequired isRequired, AuthorIsRegistered authorIsRegistered) {
        SystemUser author = createUserWithPerson();
        author.setActive(authorIsRegistered == AuthorIsRegistered.REGISTERED);

        Harvest harvest = model().newHarvest(author.getPerson(), author.getPerson());
        harvest.setHarvestReportRequired(isRequired == HarvestReportRequired.REQUIRED);
        return harvest;
    }

    private Harvest createHarvest(HarvestReportRequired isRequired, AuthorIsRegistered authorIsRegistered,
                                  ShooterIsRegistered shooterIsRegistered) {

        SystemUser author = createUserWithPerson();
        SystemUser hunter = createUserWithPerson();
        author.setActive(authorIsRegistered == AuthorIsRegistered.REGISTERED);
        hunter.setActive(shooterIsRegistered == ShooterIsRegistered.REGISTERED);

        Harvest harvest = model().newHarvest(author.getPerson(), hunter.getPerson());
        harvest.setHarvestReportRequired(isRequired == HarvestReportRequired.REQUIRED);
        return harvest;
    }
}
