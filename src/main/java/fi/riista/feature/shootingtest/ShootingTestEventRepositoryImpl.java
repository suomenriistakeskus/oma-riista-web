package fi.riista.feature.shootingtest;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.querydsl.jpa.JPAExpressions.selectOne;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;

@Repository
public class ShootingTestEventRepositoryImpl implements ShootingTestEventRepositoryCustom {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    @Override
    public long countShootingTestEventsNotProperlyFinished(final Riistanhoitoyhdistys rhy,
                                                           final LocalDate beginDate,
                                                           final LocalDate endDate) {

        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QShootingTestEvent SHOOTING_TEST_EVENT = QShootingTestEvent.shootingTestEvent;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;

        final BooleanExpression noParticipantsRegistered = selectOne()
                .from(PARTICIPANT)
                .where(PARTICIPANT.shootingTestEvent.eq(SHOOTING_TEST_EVENT))
                .notExists();

        final BooleanExpression eventStatePredicate =
                SHOOTING_TEST_EVENT.lockedTime.isNull().or(noParticipantsRegistered);

        return jpaQueryFactory
                .select(CALENDAR_EVENT.id.count())
                .from(SHOOTING_TEST_EVENT)
                .rightJoin(SHOOTING_TEST_EVENT.calendarEvent, CALENDAR_EVENT)
                .where(CALENDAR_EVENT.organisation.eq(rhy),
                        CALENDAR_EVENT.calendarEventType.in(AMPUMAKOE, JOUSIAMPUMAKOE),
                        CALENDAR_EVENT.date.between(beginDate.toDate(), endDate.toDate()),
                        eventStatePredicate)
                .fetchOne();
    }
}
