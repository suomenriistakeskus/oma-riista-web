package fi.riista.feature.organization.calendar;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.Organisation;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Repository
public class CalendarEventRepositoryImpl implements CalendarEventRepositoryCustom {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    @Override
    public Map<CalendarEventType, Long> countEventTypes(final Organisation organisation,
                                                        final LocalDate beginDate,
                                                        final LocalDate endDate) {

        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;

        final EnumPath<CalendarEventType> keyCol = CALENDAR_EVENT.calendarEventType;
        final NumberExpression<Long> valueCol = CALENDAR_EVENT.count();

        return jpaQueryFactory.select(keyCol, valueCol)
                .from(CALENDAR_EVENT)
                .where(CALENDAR_EVENT.organisation.eq(organisation))
                .where(CALENDAR_EVENT.date.between(beginDate.toDate(), endDate.toDate()))
                .groupBy(keyCol)
                .transform(GroupBy.groupBy(keyCol).as(valueCol));
    }
}
