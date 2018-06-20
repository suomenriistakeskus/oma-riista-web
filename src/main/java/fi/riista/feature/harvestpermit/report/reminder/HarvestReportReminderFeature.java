package fi.riista.feature.harvestpermit.report.reminder;

import fi.riista.feature.gamediary.GameDiarySpecs;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.util.Collect.mappingTo;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class HarvestReportReminderFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestReportReminderFeature.class);

    static final ReadablePeriod FIRST_REMINDER_DELAY = Hours.hours(24);
    static final ReadablePeriod LAST_REMINDER_DELAY = Hours.hours(5 * 24 + 1);
    static final ReadablePeriod REMINDER_INTERVAL = Hours.hours(2 * 24);

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestReportReminderEmailService emailService;

    private static Interval harvestCreatedDuringInterval(final DateTime now) {
        return new Interval(
                now.minus(HarvestReportReminderFeature.LAST_REMINDER_DELAY),
                now.minus(HarvestReportReminderFeature.FIRST_REMINDER_DELAY));
    }

    private static DateTime reminderMustBeOlderThan(final DateTime now) {
        return now.minus(HarvestReportReminderFeature.REMINDER_INTERVAL);
    }

    private static Set<String> getReceivers(final Harvest harvest) {
        return Stream.of(harvest.getAuthor(), harvest.getActualShooter())
                .filter(Person::isActive)
                .map(Person::getEmail)
                .filter(StringUtils::hasText)
                .collect(toSet());
    }

    @Transactional
    public Map<Long, Set<String>> sendReminders() {
        final DateTime processingStartTime = DateUtil.now();
        final Interval createdDuring = harvestCreatedDuringInterval(processingStartTime);
        final DateTime reminderOlderThan = reminderMustBeOlderThan(processingStartTime);

        // harvest -> emails
        final Map<Harvest, Set<String>> reminders = harvestRepository
                .findAll(spec(createdDuring, reminderOlderThan))
                .stream()
                .collect(mappingTo(HarvestReportReminderFeature::getReceivers));

        // harvestId -> successful email
        return reminders.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(toMap(entry -> F.getId(entry.getKey()), reminder -> {
                    final Harvest harvest = reminder.getKey();
                    final Set<String> emailRecipients = reminder.getValue();

                    try {
                        emailService.sendMails(harvest, emailRecipients);
                        harvest.setEmailReminderSentTime(processingStartTime);
                        return emailRecipients;

                    } catch (RuntimeException ex) {
                        LOG.error("Failed to send notifications for harvestId {} to email recipients {}");
                        return emptySet();
                    }
                }));
    }

    private static Specification<Harvest> spec(final Interval createdDuring, final DateTime reminderOlderThan) {
        return Specifications
                // Reminder needs to be sent if there harvest report is required and there is no harvest report.
                .where(GameDiarySpecs.harvestReportRequiredAndMissing())
                // But list type permits are special. That's why we check that email is sent if there is no link to
                // permit or permit is 'normal' permit (not list type)
                .and(JpaSpecs.or(JpaSpecs.isNull(Harvest_.harvestPermit), GameDiarySpecs.permitHarvestAsList(false)))
                .and(JpaSpecs.creationTimeBetween(createdDuring.getStart().toDate(), createdDuring.getEnd().toDate()))
                .and(JpaSpecs.or(GameDiarySpecs.emailReminderSentTimeIsNull(),
                        GameDiarySpecs.emailReminderSentTimeBefore(reminderOlderThan)));
    }
}
