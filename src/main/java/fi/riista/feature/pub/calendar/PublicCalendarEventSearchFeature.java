package fi.riista.feature.pub.calendar;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.CalendarEvent_;
import fi.riista.feature.pub.PublicDTOFactory;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.List;

import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class PublicCalendarEventSearchFeature {

    private static final int MAX_RESULTS = 200;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private PublicDTOFactory dtoFactory;

    @Transactional(readOnly = true)
    public PublicCalendarEventSearchResultDTO findCalendarEvents(PublicCalendarEventSearchDTO params) {
        final Specification<CalendarEvent> filter = where(betweenDates(params.getBegin(), params.getEnd()))
                .and(byArea(params.getAreaId()))
                .and(byRhy(params.getRhyId()))
                .and(byEventType(params.getCalendarEventType()));

        final JpaSort sort = new JpaSort(Sort.Direction.ASC, CalendarEvent_.date, CalendarEvent_.beginTime);
        return toCalendarEventDTOs(calendarEventRepository.findAll(filter, sort));
    }

    private static Specification<CalendarEvent> betweenDates(final LocalDate begin, final LocalDate end) {
        return (root, query, cb) -> cb.between(root.get(CalendarEvent_.date), begin.toDate(), end.toDate());
    }

    private static Specification<CalendarEvent> byArea(final String areaId) {
        return (root, query, cb) -> {
            if (areaId == null) {
                return cb.conjunction();
            }

            final Join<CalendarEvent, Organisation> orgJoin = root.join(CalendarEvent_.organisation);
            final Join<Organisation, Organisation> parentOrgJoin = orgJoin.join(Organisation_.parentOrganisation);
            final Predicate thisIsWantedArea = getPredicate(cb, orgJoin, OrganisationType.RKA, areaId);
            final Predicate parentIsWantedArea = getPredicate(cb, parentOrgJoin, OrganisationType.RKA, areaId);
            return cb.or(thisIsWantedArea, parentIsWantedArea);
        };
    }

    private static Specification<CalendarEvent> byRhy(final String rhyId) {
        return (root, query, cb) -> rhyId == null
                ? cb.conjunction()
                : getPredicate(cb, root.join(CalendarEvent_.organisation), OrganisationType.RHY, rhyId);
    }

    private static Predicate getPredicate(
            CriteriaBuilder cb, From<?, Organisation> organisationJoin, OrganisationType orgType, String officialCode) {

        return cb.and(
                cb.equal(organisationJoin.get(Organisation_.organisationType), orgType),
                cb.equal(organisationJoin.get(Organisation_.officialCode), officialCode));
    }

    private static Specification<CalendarEvent> byEventType(final CalendarEventType calendarEventType) {
        return calendarEventType == null
                ? JpaSpecs.conjunction()
                : JpaSpecs.equal(CalendarEvent_.calendarEventType, calendarEventType);
    }

    private PublicCalendarEventSearchResultDTO toCalendarEventDTOs(final List<CalendarEvent> calendarEvents) {
        if (calendarEvents.size() > MAX_RESULTS) {
            return PublicCalendarEventSearchResultDTO.TOO_MANY_RESULTS;
        }
        final List<PublicCalendarEventDTO> events = F.mapNonNullsToList(calendarEvents, calendarEvent ->
                dtoFactory.create(calendarEvent, dtoFactory.create(calendarEvent.getCalendarEventType())));
        return new PublicCalendarEventSearchResultDTO(events);
    }

    public List<PublicCalendarEventTypeDTO> getCalendarEventTypes() {
        return F.mapNonNullsToList(CalendarEventType.values(), dtoFactory::create);
    }

}
