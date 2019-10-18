package fi.riista.feature.organization.calendar;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TimePath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.sql.SQAdditionalCalendarEvent;
import fi.riista.sql.SQCalendarEvent;
import fi.riista.sql.SQOrganisation;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CalendarEventRepositoryImpl implements CalendarEventRepositoryCustom {

    private static final StringPath calendarEventTypePath = Expressions.stringPath("calendar_event_type");
    private static final DateTimePath<Timestamp> datePath = Expressions.dateTimePath(Timestamp.class, "date");
    private static final BooleanPath publicVisibilityPath = Expressions.booleanPath("public_visibility");
    private static final StringPath organisationTypePath = Expressions.stringPath("organisation_type");
    private static final StringPath parentOrganisationTypePath = Expressions.stringPath("parent_organisation_type");
    private static final StringPath organisationOfficialCodePath = Expressions.stringPath("official_code");
    private static final StringPath parentOrganisationOfficialCodePath = Expressions.stringPath("parent_official_code");

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

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
                .where(CALENDAR_EVENT.excludedFromStatistics.eq(false))
                .groupBy(keyCol)
                .transform(GroupBy.groupBy(keyCol).as(valueCol));
    }

    @Transactional(readOnly = true)
    @Override
    public Map<CalendarEventType, Integer> countEventParticipants(Organisation organisation,
                                                               LocalDate beginDate,
                                                               LocalDate endDate) {
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;

        final EnumPath<CalendarEventType> keyCol = CALENDAR_EVENT.calendarEventType;
        final NumberExpression<Integer> valueCol = CALENDAR_EVENT.participants.sum();

        return jpaQueryFactory.select(keyCol, valueCol)
                .from(CALENDAR_EVENT)
                .where(CALENDAR_EVENT.organisation.eq(organisation))
                .where(CALENDAR_EVENT.date.between(beginDate.toDate(), endDate.toDate()))
                .where(CALENDAR_EVENT.excludedFromStatistics.eq(false))
                .where(CALENDAR_EVENT.participants.isNotNull())
                .groupBy(keyCol)
                .transform(GroupBy.groupBy(keyCol).as(valueCol));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CalendarEventSearchResultDTO> getCalendarEvents(final CalendarEventSearchParamsDTO params) {
        final SQCalendarEvent SQEVENT = SQCalendarEvent.calendarEvent;
        final SQAdditionalCalendarEvent SQADDITIONALEVENT = SQAdditionalCalendarEvent.additionalCalendarEvent;
        final SQOrganisation SQORGANISATION = SQOrganisation.organisation;
        final SQOrganisation SQPARENTORGANISATION = new SQOrganisation("parent");

        final NumberPath<Long> calendarEventIdPath = Expressions.numberPath(Long.class, "calendar_event_id");
        final NumberPath<Long> additionalCalendarEventIdPath = Expressions.numberPath(Long.class, "additional_calendar_event_id");
        final TimePath<Time> beginTimePath = Expressions.timePath(Time.class, "begin_time");
        final TimePath<Time> endTimePath = Expressions.timePath(Time.class, "end_time");
        final StringPath namePath = Expressions.stringPath("name");
        final StringPath descriptionPath = Expressions.stringPath("description");
        final NumberPath<Long> organisationIdPath = Expressions.numberPath(Long.class, "organisation_id");
        final NumberPath<Long> venueIdPath = Expressions.numberPath(Long.class, "venue_id");

        final SQLQuery<Tuple> calendarEventQuery = SQLExpressions
                .select(SQEVENT.calendarEventId.as(calendarEventIdPath),
                        Expressions.as(Expressions.nullExpression(), additionalCalendarEventIdPath),
                        SQEVENT.calendarEventType.as(calendarEventTypePath),
                        SQEVENT.date.as(datePath),
                        SQEVENT.beginTime.as(beginTimePath),
                        SQEVENT.endTime.as(endTimePath),
                        SQEVENT.name.as(namePath),
                        SQEVENT.description.as(descriptionPath),
                        SQEVENT.organisationId.as(organisationIdPath),
                        SQEVENT.venueId.as(venueIdPath),
                        SQEVENT.publicVisibility.as(publicVisibilityPath),
                        SQORGANISATION.organisationType.as(organisationTypePath),
                        SQPARENTORGANISATION.organisationType.as(parentOrganisationTypePath),
                        SQORGANISATION.officialCode.as(organisationOfficialCodePath),
                        SQPARENTORGANISATION.officialCode.as(parentOrganisationOfficialCodePath))
                .from(SQEVENT)
                .join(SQORGANISATION).on(SQEVENT.organisationId.eq(SQORGANISATION.organisationId))
                .leftJoin(SQPARENTORGANISATION).on(SQORGANISATION.parentOrganisationId.eq(SQPARENTORGANISATION.organisationId));

        final SQLQuery<Tuple> additionalCalendarEventQuery = SQLExpressions
                .select(SQEVENT.calendarEventId.as(calendarEventIdPath),
                        SQADDITIONALEVENT.additionalCalendarEventId.as(additionalCalendarEventIdPath),
                        SQEVENT.calendarEventType.as(calendarEventTypePath),
                        SQADDITIONALEVENT.date.as(datePath),
                        SQADDITIONALEVENT.beginTime.as(beginTimePath),
                        SQADDITIONALEVENT.endTime.as(endTimePath),
                        SQEVENT.name.as(namePath),
                        SQEVENT.description.as(descriptionPath),
                        SQEVENT.organisationId.as(organisationIdPath),
                        SQADDITIONALEVENT.venueId.as(venueIdPath),
                        SQEVENT.publicVisibility.as(publicVisibilityPath),
                        SQORGANISATION.organisationType.as(organisationTypePath),
                        SQPARENTORGANISATION.organisationType.as(parentOrganisationTypePath),
                        SQORGANISATION.officialCode.as(organisationOfficialCodePath),
                        SQPARENTORGANISATION.officialCode.as(parentOrganisationOfficialCodePath))
                .from(SQADDITIONALEVENT)
                .join(SQEVENT).on(SQADDITIONALEVENT.calendarEventId.eq(SQEVENT.calendarEventId))
                .join(SQORGANISATION).on(SQEVENT.organisationId.eq(SQORGANISATION.organisationId))
                .leftJoin(SQPARENTORGANISATION).on(SQORGANISATION.parentOrganisationId.eq(SQPARENTORGANISATION.organisationId));

        final Predicate predicate = new BooleanBuilder()
                .and(betweenPredicate(params))
                .and(rhyPredicate(params.getRhyId()))
                .and(areaPredicate(params.getAreaId(), params.getRhyId()))
                .and(onlyPublicOrganisationsPredicate(params.getOnlyPublicEvents()))
                .and(typePredicate(params.getCalendarEventType()))
                .and(onlyPubliclyVisiblePredicate(params.getOnlyPubliclyVisible()));

        final SQLQuery<Tuple> query = sqlQueryFactory
                .select(calendarEventIdPath,
                        calendarEventTypePath,
                        namePath,
                        descriptionPath,
                        datePath,
                        beginTimePath,
                        endTimePath,
                        organisationIdPath,
                        venueIdPath,
                        organisationTypePath,
                        parentOrganisationTypePath,
                        organisationOfficialCodePath,
                        parentOrganisationOfficialCodePath,
                        additionalCalendarEventIdPath)
                .from(SQLExpressions.union(calendarEventQuery, additionalCalendarEventQuery).as("u"))
                .where(predicate)
                .orderBy(datePath.asc(), beginTimePath.asc(), namePath.asc(), calendarEventIdPath.asc())
                .limit(params.getLimit())
                .offset(params.getOffset());

        return query.fetch().stream()
                .map(t -> {
                    final Long calendarEventId = t.get(calendarEventIdPath);
                    final Long additionalCalendarEventId = t.get(additionalCalendarEventIdPath);
                    final String calendarEventTypeString = t.get(calendarEventTypePath);
                    final CalendarEventType calendarEventType = CalendarEventType.valueOf(calendarEventTypeString);
                    final String name = t.get(namePath);
                    final String description = t.get(descriptionPath);
                    final Timestamp dateTimestamp = t.get(datePath);
                    final Date date = new Date(dateTimestamp.getTime());
                    final Time begin = t.get(beginTimePath);
                    final LocalTime beginTime = new LocalTime(begin);
                    final Time end = t.get(endTimePath);
                    final LocalTime endTime = end != null ? new LocalTime(end) : null;
                    final Long organisationId = t.get(organisationIdPath);
                    final Long venueId = t.get(venueIdPath);

                    return new CalendarEventSearchResultDTO(
                            calendarEventId,
                            additionalCalendarEventId,
                            calendarEventType,
                            name,
                            description,
                            date,
                            beginTime,
                            endTime,
                            organisationId,
                            venueId);
        }).collect(Collectors.toList());
    }

    private static BooleanExpression betweenPredicate(final CalendarEventSearchParamsDTO parameters) {
        if (parameters.getBegin() == null && parameters.getEnd() == null) {
            return null;
        }

        Timestamp beginTimestamp = parameters.getBegin() == null
                ? null
                : new Timestamp(DateUtil.toDateNullSafe(parameters.getBegin()).getTime());
        Timestamp endTimestamp = parameters.getEnd() == null
                ? null
                : new Timestamp(DateUtil.toDateNullSafe(parameters.getEnd()).getTime());

        return datePath.between(beginTimestamp, endTimestamp);
    }

    private static BooleanExpression areaPredicate(final String areaId, final String rhyId) {
        if (rhyId == null && areaId != null) {
            // If only rka is selected as search criteria, match events for rhys that have
            // the specified rka as their parent organisation.
            BooleanExpression eventUnderRhy = organisationTypePath.eq(OrganisationType.RHY.name())
                    .and(parentOrganisationTypePath.eq(OrganisationType.RKA.name()))
                    .and(parentOrganisationOfficialCodePath.eq(areaId));

            BooleanExpression eventUnderRka = organisationTypePath.eq(OrganisationType.RKA.name())
                    .and(organisationOfficialCodePath.eq(areaId));

            return eventUnderRhy.or(eventUnderRka);
        }

        return null;
    }

    private static BooleanExpression rhyPredicate(final String rhyId) {
        return rhyId == null
                ? null
                : organisationTypePath.eq(OrganisationType.RHY.name())
                .and(organisationOfficialCodePath.eq(rhyId));
    }

    private static BooleanExpression onlyPublicOrganisationsPredicate(boolean onlyPubliclyVisible) {
        return onlyPubliclyVisible
                ? organisationTypePath.in(
                        OrganisationType.RHY.name(),
                        OrganisationType.RKA.name(),
                        OrganisationType.VRN.name(),
                        OrganisationType.RK.name(),
                        OrganisationType.ARN.name())
            : null;
    }

    private static BooleanExpression typePredicate(final CalendarEventType calendarEventType) {
        return calendarEventType == null
                ? null
                : calendarEventTypePath.eq(calendarEventType.name());
    }

    private static BooleanExpression onlyPubliclyVisiblePredicate(boolean onlyPubliclyVisible) {
        return onlyPubliclyVisible
                ? publicVisibilityPath.eq(true)
                : null;
    }


}
