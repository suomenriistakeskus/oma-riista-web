package fi.riista.feature.organization.calendar;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdditionalCalendarEventService {
    @Resource
    private AdditionalCalendarEventRepository additionalCalendarEventRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addAdditionalCalendarEvents(final List<AdditionalCalendarEventDTO> eventDTOs,
                                            final CalendarEvent calendarEvent) {
        final Map<Long, Venue> venueIdToVenue = getVenueIdToVenueMapping(eventDTOs);

        eventDTOs.forEach(eventDTO -> {
                    additionalCalendarEventRepository.save(
                            new AdditionalCalendarEvent(DateUtil.toDateNullSafe(eventDTO.getDate()),
                                    eventDTO.getBeginTime(),
                                    eventDTO.getEndTime(),
                                    calendarEvent,
                                    venueIdToVenue.get(eventDTO.getVenue().getId())));
                });
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateAdditionalCalendarEvents(final List<AdditionalCalendarEventDTO> eventDTOs,
                                               final CalendarEvent calendarEvent) {
        additionalCalendarEventRepository.deleteByCalendarEvent(calendarEvent);
        addAdditionalCalendarEvents(eventDTOs, calendarEvent);
    }

    private Map<Long, Venue> getVenueIdToVenueMapping(final List<AdditionalCalendarEventDTO> additionalCalendarEventDTOS) {
        final QVenue QVENUE = QVenue.venue;
        final Set<Long> venueIds = additionalCalendarEventDTOS.stream()
                .map(additionalCalendarEventDTO -> additionalCalendarEventDTO.getVenue().getId())
                .collect(Collectors.toSet());

        return jpqlQueryFactory
                .from(QVENUE)
                .where(QVENUE.id.in(venueIds))
                .transform(GroupBy.groupBy(QVENUE.id).as(QVENUE));
    }
}
