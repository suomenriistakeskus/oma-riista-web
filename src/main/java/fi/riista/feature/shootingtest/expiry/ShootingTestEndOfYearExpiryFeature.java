package fi.riista.feature.shootingtest.expiry;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysEmailService;
import fi.riista.feature.shootingtest.QShootingTestEvent;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;

@Service
public class ShootingTestEndOfYearExpiryFeature {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    private static final String TIME_FORMAT_FINNISH = "HH:mm";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern(TIME_FORMAT_FINNISH);

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private RiistanhoitoyhdistysEmailService riistanhoitoyhdistysEmailService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Map<Long, List<ShootingTestEndOfYearExpiryDTO>> getOpenShootingTest(final int year) {

        final QShootingTestEvent SHOOTING_TEST_EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QOrganisation RHY = QOrganisation.organisation;

        return jpqlQueryFactory.select(RHY.id, CALENDAR_EVENT)
                .from(SHOOTING_TEST_EVENT)
                .join(SHOOTING_TEST_EVENT.calendarEvent, CALENDAR_EVENT)
                .join(CALENDAR_EVENT.organisation, RHY)
                .where(CALENDAR_EVENT.date.year().eq(year),
                        CALENDAR_EVENT.calendarEventType.in(CalendarEventType.shootingTestTypes()),
                        SHOOTING_TEST_EVENT.lockedTime.isNull())
                .transform(GroupBy.groupBy(RHY.id).as(GroupBy.list(CALENDAR_EVENT)))
                .entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e ->
                    e.getValue().stream()
                            .map(v -> {
                                final String date = DATE_FORMATTER.print(v.getDateAsLocalDate());
                                String time = TIME_FORMATTER.print(v.getBeginTime());
                                if (v.getEndTime() != null) {
                                    time += " - " + TIME_FORMATTER.print(v.getEndTime());
                                }
                                return new ShootingTestEndOfYearExpiryDTO(date, time);
                            })
                            .collect(Collectors.toList())));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Map<Long, Set<String>> resolveEmails(@Nonnull final Set<Long> rhyIds) {
        return riistanhoitoyhdistysEmailService.resolveEmails(rhyIds);
    }

}
