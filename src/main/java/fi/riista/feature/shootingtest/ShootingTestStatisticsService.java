package fi.riista.feature.shootingtest;

import com.google.common.collect.ImmutableSortedMap;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.shootingtest.ShootingTestStatisticsRowDTO.TestTypeStatisticsDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.Collect.toImmutableSortedMap;
import static fi.riista.util.NumberUtils.sum;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class ShootingTestStatisticsService {

    @Resource
    private JPAQueryFactory queryFactory;

    @Resource
    private ShootingTestCalendarEventDTOTransformer shootingTestCalendarEventDTOTransformer;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ShootingTestStatisticsDTO calculate(final Riistanhoitoyhdistys rhy, final int calendarYear) {
        final List<ShootingTestEvent> events = findEvents(rhy, calendarYear);

        final Map<Long, ShootingTestCalendarEventDTO> calendarEvents = calendarDtos(events);
        final Map<Long, BigDecimal> paid = getSumOfPaymentsGroupedByEvent(events);
        final Map<Long, ImmutableSortedMap<ShootingTestType, TestTypeStatisticsDTO>> groupedTestTypeStats = groupAttempts(events);

        final List<ShootingTestStatisticsRowDTO> eventStatistics = events.stream().map(event -> {

            final long eventId = event.getId();
            final ShootingTestCalendarEventDTO calendarEventDto = calendarEvents.get(eventId);
            final ImmutableSortedMap<ShootingTestType, TestTypeStatisticsDTO> testTypeStats =
                    groupedTestTypeStats.computeIfAbsent(eventId, k -> ImmutableSortedMap.of());
            final BigDecimal paidAmount = paid.getOrDefault(eventId, BigDecimal.ZERO);
            final BigDecimal dueAmount = sum(testTypeStats.values(), TestTypeStatisticsDTO::getDueAmount);

            return new ShootingTestStatisticsRowDTO(calendarEventDto, paidAmount, dueAmount, testTypeStats);

        }).collect(toList());

        return ShootingTestStatisticsDTO.create(eventStatistics);
    }

    private List<ShootingTestEvent> findEvents(final Riistanhoitoyhdistys rhy, final int calendarYear) {
        final Date start = DateUtil.beginOfCalendarYear(calendarYear).toDate();
        final Date end = DateUtil.beginOfCalendarYear(calendarYear + 1).toDate();

        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;

        return queryFactory.selectFrom(EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .where(EVENT.lockedTime.isNotNull())
                .where(CALENDAR_EVENT.organisation.eq(rhy))
                .where(CALENDAR_EVENT.date.goe(start))
                .where(CALENDAR_EVENT.date.lt(end))
                .orderBy(CALENDAR_EVENT.date.desc())
                .fetch();
    }

    private Map<Long, ShootingTestCalendarEventDTO> calendarDtos(final List<ShootingTestEvent> events) {
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;

        final List<CalendarEvent> calendarEvents = queryFactory.select(CALENDAR_EVENT)
                .from(EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .where(EVENT.in(events))
                .fetch();

        return shootingTestCalendarEventDTOTransformer.apply(calendarEvents).stream()
                .collect(indexingBy(ShootingTestCalendarEventDTO::getShootingTestEventId));
    }

    private Map<Long, ImmutableSortedMap<ShootingTestType, TestTypeStatisticsDTO>> groupAttempts(final List<ShootingTestEvent> events) {
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;

        final NumberExpression<Integer> qualifiedCount = Expressions.cases()
                .when(ATTEMPT.result.eq(ShootingTestAttemptResult.QUALIFIED))
                .then(1)
                .otherwise(0)
                .sum();

        return queryFactory.select(EVENT.id, ATTEMPT.type, ATTEMPT.count(), qualifiedCount)
                .from(EVENT)
                .join(EVENT.participants, PARTICIPANT)
                .join(PARTICIPANT.attempts, ATTEMPT)
                .where(EVENT.in(events))
                .where(ATTEMPT.result.ne(ShootingTestAttemptResult.REBATED))
                .groupBy(EVENT.id, ATTEMPT.type)
                .fetch()
                .stream()
                .collect(groupingBy(t -> t.get(EVENT.id), toImmutableSortedMap(
                        t -> t.get(ATTEMPT.type),
                        t -> new TestTypeStatisticsDTO(
                                t.get(ATTEMPT.count()).intValue(),
                                t.get(qualifiedCount).intValue()))));
    }

    private Map<Long, BigDecimal> getSumOfPaymentsGroupedByEvent(final List<ShootingTestEvent> events) {
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;

        final NumberExpression<BigDecimal> sumOfPaidAmounts = PARTICIPANT.paidAmount.sum();

        return queryFactory.select(EVENT.id, sumOfPaidAmounts)
                .from(PARTICIPANT)
                .where(PARTICIPANT.shootingTestEvent.in(events))
                .groupBy(EVENT.id)
                .fetch()
                .stream()
                .collect(toMap(
                        t -> t.get(EVENT.id),
                        t -> F.firstNonNull(t.get(sumOfPaidAmounts), BigDecimal.ZERO)));
    }
}
