package fi.riista.feature.organization.rhy.annualstats.audit;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysEmailService;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.annualstats.QRhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsRepository;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.feature.organization.rhy.annualstats.statechange.QRhyAnnualStatisticsStateChangeEvent;
import fi.riista.util.F;
import fi.riista.util.Locales;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.querydsl.core.group.GroupBy.groupBy;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.util.Collect.toImmutableSortedMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.joda.time.Duration.standardHours;
import static org.joda.time.Duration.standardMinutes;

@Component
public class RhyAnnualStatisticsNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(RhyAnnualStatisticsNotificationService.class);

    private static final String SUBMIT_MESSAGE_TEMPLATE = "email_annual_statistics_submitted";
    private static final String SUBMIT_MESSAGE_TEMPLATE_SV = "email_annual_statistics_submitted.sv";

    private static final String MODERATOR_UPDATE_MESSAGE_TEMPLATE = "email_annual_statistics_moderator_update";
    private static final String MODERATOR_UPDATE_MESSAGE_TEMPLATE_SV = "email_annual_statistics_moderator_update.sv";

    private static final String APPROVAL_MESSAGE_TEMPLATE = "email_annual_statistics_approved";
    private static final String APPROVAL_MESSAGE_TEMPLATE_SV = "email_annual_statistics_approved.sv";

    @Resource
    private RhyAnnualStatisticsRepository statisticsRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private RiistanhoitoyhdistysEmailService emailService;

    @Resource
    private MailService mailService;

    @Resource
    private EnumLocaliser localiser;

    @Resource
    private Handlebars handlebars;

    @Transactional(readOnly = true)
    public List<AggregatedAnnualStatisticsModeratorUpdateDTO> findAnnualStatisticGroupsUpdatedByModerator(
            @Nonnull final Interval interval) {

        requireNonNull(interval);

        final QRhyAnnualStatistics STATS = QRhyAnnualStatistics.rhyAnnualStatistics;
        final QRhyAnnualStatisticsModeratorUpdateEvent EVENT =
                QRhyAnnualStatisticsModeratorUpdateEvent.rhyAnnualStatisticsModeratorUpdateEvent;

        final DateTimeExpression<DateTime> lastModified = EVENT.eventTime.max();

        final Map<Tuple2<Integer, Long>, ImmutableSortedMap<AnnualStatisticGroup, DateTime>> dataGroupsByYearAndRhyId =
                jpqlQueryFactory
                        .select(STATS.rhy.id,
                                STATS.year,
                                EVENT.dataGroup,
                                lastModified)
                        .from(EVENT)
                        .join(EVENT.statistics, STATS)
                        .where(EVENT.eventTime.gt(interval.getStart()))
                        .where(EVENT.eventTime.loe(interval.getEnd()))
                        .groupBy(STATS.rhy.id, STATS.year, EVENT.dataGroup)
                        .orderBy(STATS.year.asc(), STATS.rhy.id.asc())
                        .fetch()
                        .stream()
                        .collect(groupingBy(
                                t -> Tuple.of(t.get(STATS.year), t.get(STATS.rhy.id)),
                                toImmutableSortedMap(t -> t.get(EVENT.dataGroup), t -> t.get(lastModified))));

        return dataGroupsByYearAndRhyId.entrySet()
                .stream()
                .sorted(comparing(Map.Entry::getKey))
                .map(entry -> {

                    return entry.getKey().swap()
                            .append(entry.getValue())
                            .apply(AggregatedAnnualStatisticsModeratorUpdateDTO::new);
                })
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<RhyAnnualStatistics> findApprovedAnnualStatistics(@Nonnull final Interval interval) {
        requireNonNull(interval);

        final QRhyAnnualStatistics STATS = QRhyAnnualStatistics.rhyAnnualStatistics;
        final QRhyAnnualStatisticsStateChangeEvent EVENT =
                QRhyAnnualStatisticsStateChangeEvent.rhyAnnualStatisticsStateChangeEvent;

        final DateTimeExpression<DateTime> lastModified = EVENT.eventTime.max();

        final Map<Long, DateTime> statisticsIdToModificationTime =
                jpqlQueryFactory
                        .select(STATS.id, lastModified)
                        .from(EVENT)
                        .join(EVENT.statistics, STATS)
                        .where(EVENT.state.eq(APPROVED))
                        .where(STATS.state.eq(APPROVED))
                        .where(EVENT.eventTime.gt(interval.getStart()))
                        .where(EVENT.eventTime.loe(interval.getEnd()))
                        .groupBy(STATS.id)
                        .transform(groupBy(STATS.id).as(lastModified));

        return statisticsRepository.findAllById(statisticsIdToModificationTime.keySet());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendSubmitConfirmationNotification(@Nonnull final RhyAnnualStatistics statistics) {
        sendEmails(singleton(statistics), stats -> stats.getRhy().getId(), this::sendSubmitConfirmationEmail);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendModeratorUpdateNotifications(@Nonnull final List<AggregatedAnnualStatisticsModeratorUpdateDTO> aggregatedUpdates) {
        sendEmails(aggregatedUpdates, dto -> dto.getRhyId(), this::sendModeratorUpdateEmail);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendApprovalNotifications(@Nonnull final List<RhyAnnualStatistics> statistics) {
        sendEmails(statistics, stats -> stats.getRhy().getId(), this::sendApprovalEmail);
    }

    private <T> void sendEmails(@Nonnull final Collection<T> collection,
                                @Nonnull final Function<? super T, Long> getRhyId,
                                @Nonnull final EmailSender<T> emailSender) {

        requireNonNull(collection);
        requireNonNull(getRhyId);
        requireNonNull(emailSender);

        if (collection.isEmpty()) {
            return;
        }

        final Set<Long> uniqueRhyIds = F.mapNonNullsToSet(collection, getRhyId);
        final List<Riistanhoitoyhdistys> involvedRhys = rhyRepository.findAllById(uniqueRhyIds);
        final Map<Long, Riistanhoitoyhdistys> rhyIndex = F.indexById(involvedRhys);

        final Map<Long, Set<String>> rhyIdToEmails = emailService.resolveEmails(uniqueRhyIds);

        collection.forEach(model -> {

            final Long rhyId = getRhyId.apply(model);
            final Riistanhoitoyhdistys rhy = rhyIndex.get(rhyId);
            final Set<String> rhyContactEmails = rhyIdToEmails.get(rhyId);

            if (rhyContactEmails.isEmpty()) {
                LOG.warn("Could not resolve email for RHY with officialCode={} and name={}",
                        rhy.getOfficialCode(), rhy.getNameFinnish());

            } else {
                emailSender.send(rhy, rhyContactEmails, model);
            }
        });
    }

    private void sendSubmitConfirmationEmail(final Riistanhoitoyhdistys rhy,
                                             final Set<String> rhyContactEmails,
                                             final RhyAnnualStatistics statistics) {

        LOG.info("Sending annual statistics submit notification for rhyCode={} and year {} to email={}",
                rhy.getOfficialCode(), statistics.getYear(), joinToString(rhyContactEmails));

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(rhyContactEmails)
                .withSubject("Suomen riistakeskus on vastaanottanut hakemuksenne riistanhoitoyhdistyksen valtionavustukseksi")
                .withScheduledTimeAfter(standardHours(1))
                .appendHandlebarsBody(handlebars, SUBMIT_MESSAGE_TEMPLATE, emptyMap())
                .appendBody("\n<hr/>\n")
                .appendHandlebarsBody(handlebars, SUBMIT_MESSAGE_TEMPLATE_SV, emptyMap())
                .build());
    }

    private void sendModeratorUpdateEmail(final Riistanhoitoyhdistys rhy,
                                          final Set<String> rhyContactEmails,
                                          final AggregatedAnnualStatisticsModeratorUpdateDTO dto) {

        final int year = dto.getYear();
        final Set<AnnualStatisticGroup> groups = dto.getDataGroups().keySet();

        final Map<String, Object> model = ImmutableMap.<String, Object> builder()
                .put("year", year)
                .put("groupsFI", localise(groups, Locales.FI))
                .put("groupsSV", localise(groups, Locales.SV))
                .build();

        final Set<String> emailToAddresses = new HashSet<>(rhyContactEmails);
        // Send copy of mail to Riistakeskus for archiving purposes
        emailToAddresses.add("noreply@riista.fi");

        LOG.info("Sending annual statistics moderator update notification for rhyCode={} and year {} to email={}",
                rhy.getOfficialCode(), year, joinToString(emailToAddresses));

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(emailToAddresses)
                .withSubject("Toimintatietoja on täydennetty / Verksamhetsuppgifterna har kompletterats")
                .withScheduledTimeAfter(standardHours(1))
                .appendHandlebarsBody(handlebars, MODERATOR_UPDATE_MESSAGE_TEMPLATE, model)
                .appendBody("\n<hr/>\n")
                .appendHandlebarsBody(handlebars, MODERATOR_UPDATE_MESSAGE_TEMPLATE_SV, model)
                .build());
    }

    private void sendApprovalEmail(final Riistanhoitoyhdistys rhy,
                                   final Set<String> rhyContactEmails,
                                   final RhyAnnualStatistics statistics) {

        final int year = statistics.getYear();
        final Map<String, Object> model = ImmutableMap.of("year", year);

        LOG.info("Sending approval notification of annual statistics {} for rhyCode={} to email={}",
                year, rhy.getOfficialCode(), joinToString(rhyContactEmails));

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(rhyContactEmails)
                .withSubject("Toimintatiedot on hyväksytty")
                .withScheduledTimeAfter(standardHours(1).plus(standardMinutes(15)))
                .appendHandlebarsBody(handlebars, APPROVAL_MESSAGE_TEMPLATE, model)
                .appendBody("\n<hr/>\n")
                .appendHandlebarsBody(handlebars, APPROVAL_MESSAGE_TEMPLATE_SV, model)
                .build());
    }

    private List<String> localise(final Collection<? extends AnnualStatisticGroup> groups, final Locale locale) {
        return groups.stream().map(group -> localiser.getTranslation(group, locale)).collect(toList());
    }

    private static String joinToString(final Set<String> strings) {
        return strings.stream().collect(joining(", "));
    }

    private interface EmailSender<T> {
        void send(Riistanhoitoyhdistys rhy, Set<String> emails, T model);
    }
}
