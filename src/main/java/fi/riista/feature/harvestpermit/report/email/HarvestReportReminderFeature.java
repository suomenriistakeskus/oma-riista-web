package fi.riista.feature.harvestpermit.report.email;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.GameDiarySpecs;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.DateTime;
import org.joda.time.Hours;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class HarvestReportReminderFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestReportReminderFeature.class);

    public static final ReadablePeriod FIRST_REMINDER_DELAY = Hours.hours(24);
    public static final ReadablePeriod REMINDER_INTERVAL = Hours.hours(48);

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestReportReminderEmailService emailService;

    private static DateTime defaultOlderThan() {
        return DateTime.now().minus(HarvestReportReminderFeature.FIRST_REMINDER_DELAY);
    }

    private static DateTime defaultReminderOlderThan() {
        return DateTime.now().minus(HarvestReportReminderFeature.REMINDER_INTERVAL);
    }

    @Transactional
    public Map<Long, Set<String>> sendReminders() {
        final DateTime processingStartTime = DateTime.now();
        return internalSendReminders(defaultOlderThan(), defaultReminderOlderThan(), processingStartTime);
    }

    /**
     * This is for testing purposes, use the unparametrised method.
     */
    @Transactional
    public Map<Long, Set<String>> sendReminders(DateTime olderThan, DateTime reminderOlderThan) {
        return internalSendReminders(olderThan, reminderOlderThan, DateTime.now());
    }

    private static Set<String> getReceivers(final Harvest harvest) {
        return Stream.of(harvest.getAuthor(), harvest.getActualShooter())
                .filter(Person::isActive)
                .map(Person::getEmail)
                .filter(StringUtils::hasText)
                .collect(toSet());
    }

    private Map<Long, Set<String>> internalSendReminders(final DateTime olderThan,
                                                         final DateTime reminderOlderThan,
                                                         final DateTime processingStartTime) {

        // harvest -> emails
        final Map<Harvest, Set<String>> reminders = harvestRepository.findAll(spec(olderThan, reminderOlderThan)).stream()
                .collect(Collectors.toMap(Function.identity(), HarvestReportReminderFeature::getReceivers));

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

    private static Specification<Harvest> spec(DateTime olderThan, DateTime reminderOlderThan) {
        return Specifications
                // Reminder needs to be sent if there harvest report is required and there is no harvest report.
                .where(GameDiarySpecs.harvestReportRequiredAndMissing())
                // But list type permits are special. That's why we check that email is sent if there is no link to
                // permit or permit is 'normal' permit (not list type)
                .and(JpaSpecs.or(JpaSpecs.isNull(Harvest_.harvestPermit), GameDiarySpecs.permitHarvestAsList(false)))
                .and(JpaSpecs.creationTimeOlderThan(olderThan.toDate()))
                .and(JpaSpecs.or(GameDiarySpecs.emailReminderSentTimeIsNull(),
                        GameDiarySpecs.emailReminderSentTimeGt(reminderOlderThan)));
    }
}
