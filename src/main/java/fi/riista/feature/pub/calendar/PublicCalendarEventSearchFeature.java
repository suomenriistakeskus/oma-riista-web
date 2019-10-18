package fi.riista.feature.pub.calendar;

import com.google.common.base.Preconditions;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.CalendarEventSearchParamsDTO;
import fi.riista.feature.organization.calendar.CalendarEventSearchResultDTO;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.calendar.VenueRepository;
import fi.riista.feature.pub.PublicDTOFactory;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Component
public class PublicCalendarEventSearchFeature {

    private static final int MAX_RESULTS = 200;

    @Resource
    private PublicDTOFactory dtoFactory;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private VenueRepository venueRepository;

    @Transactional(readOnly = true)
    public PublicCalendarEventSearchResultDTO findCalendarEvents(final PublicCalendarEventSearchDTO parameters) {
        return findCalendarEvents(parameters, MAX_RESULTS);
    }

    // For testing
    @Transactional(readOnly = true)
    public PublicCalendarEventSearchResultDTO findCalendarEvents(final PublicCalendarEventSearchDTO parameters, final int maxResults) {
        Preconditions.checkArgument(parameters.getPageSize() == null || parameters.getPageSize() <= maxResults,
                "Requested page size must not exceed " + maxResults);

        final int pageSize = Optional.ofNullable(parameters.getPageSize()).orElse(MAX_RESULTS);
        final int pageNumber = Optional.ofNullable(parameters.getPageNumber()).orElse(0);

        final CalendarEventSearchParamsDTO calendarEventSearchParamsDTO =
                new CalendarEventSearchParamsDTO(parameters, pageSize + 1, pageNumber * pageSize);
        final List<CalendarEventSearchResultDTO> result = calendarEventRepository.getCalendarEvents(calendarEventSearchParamsDTO);

        boolean lastPage = true;
        if (result.size() > pageSize) {
            lastPage = false;
            result.remove(result.size() - 1);
        }

        return toCalendarEventDTOs(result, lastPage, maxResults);
    }

    public List<PublicCalendarEventTypeDTO> getCalendarEventTypes() {
        return F.mapNonNullsToList(CalendarEventType.values(), dtoFactory::create);
    }

    private PublicCalendarEventSearchResultDTO toCalendarEventDTOs(
            final List<CalendarEventSearchResultDTO> events, final boolean isLastPage, final int maxResults) {
        if (events.size() > maxResults) {
            return PublicCalendarEventSearchResultDTO.TOO_MANY_RESULTS;
        }

        final HashSet<Long> organisationIds = new HashSet<>();
        final HashSet<Long> venueIds = new HashSet<>();

        events.forEach(event -> {
            venueIds.add(event.getVenueId());
            organisationIds.add(event.getOrganisationId());
        });

        final Map<Long, Organisation> organisationIdToOrganisation = getOrganisationIdToOrganisation(organisationIds);
        final Map<Long, Venue> venueIdToVenue = getVenueIdToVenue(venueIds);

        final List<PublicCalendarEventDTO> eventDTOS = events.stream()
                .map(event -> {
                    final String id = String.format("%d:%d",
                            event.getCalendarEventId(),
                            Optional.ofNullable(event.getAdditionalCalendarEventId()).orElse(Long.valueOf(0)));
                    final PublicCalendarEventTypeDTO eventTypeDTO = dtoFactory.create(event.getCalendarEventType());
                    final Organisation organisation = organisationIdToOrganisation.get(event.getOrganisationId());
                    final Venue venue = venueIdToVenue.get(event.getVenueId());

                    return dtoFactory.create(
                            id,
                            eventTypeDTO,
                            event.getName(),
                            event.getDescription(),
                            DateUtil.toLocalDateNullSafe(event.getDate()),
                            event.getBeginTime(),
                            event.getEndTime(),
                            organisation,
                            venue);
                }).collect(Collectors.toList());

        return new PublicCalendarEventSearchResultDTO(eventDTOS, isLastPage);
    }

    private Map<Long, Organisation> getOrganisationIdToOrganisation(final HashSet<Long> organisationIds) {
        if (organisationIds.isEmpty()) {
            return emptyMap();
        }

        return F.indexById(organisationRepository.findAll(organisationIds));
    }

    private Map<Long, Venue> getVenueIdToVenue(final HashSet<Long> venueIds) {
        if (venueIds.isEmpty()) {
            return emptyMap();
        }

        return F.indexById(venueRepository.findAll(venueIds));
    }
}
