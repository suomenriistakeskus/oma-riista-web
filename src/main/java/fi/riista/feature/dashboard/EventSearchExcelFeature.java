package fi.riista.feature.dashboard;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.CalendarEventSearchParamsDTO;
import fi.riista.feature.organization.calendar.CalendarEventSearchResultDTO;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.calendar.VenueRepository;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventSearchExcelFeature {

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private VenueRepository venueRepository;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public EventExcelView export(final EventSearchConditionDTO searchCondition,
                                 final Locale locale) {
        final CalendarEventSearchParamsDTO searchParams = searchCondition.toCalendarEventSearchParams();
        final List<CalendarEventSearchResultDTO> events = calendarEventRepository.getCalendarEvents(searchParams);

        final Set<Long> orgIds = F.mapNonNullsToSet(events, CalendarEventSearchResultDTO::getOrganisationId);
        final Map<Long, OrganisationNameDTO> organisationMap = getOrganisationMapping(orgIds);

        final Set<Long> venueIds = F.mapNonNullsToSet(events, CalendarEventSearchResultDTO::getVenueId);
        final List<Venue> venues = !venueIds.isEmpty() ? venueRepository.findAllById(venueIds) : Collections.emptyList();
        final Map<Long, String> venueNameMap = getVenueNameMapping(venues);
        final Map<Long, String> venueAddressMap = getVenueAddressMapping(venues);
        return new EventExcelView(events, searchCondition.getYear(), organisationMap, venueNameMap, venueAddressMap,
                new EnumLocaliser(messageSource, locale));
    }

    private Map<Long, OrganisationNameDTO> getOrganisationMapping(final Set<Long> orgIds) {
        if (orgIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return organisationRepository.findAllById(orgIds).stream()
                .map(OrganisationNameDTO::createWithOfficialCode)
                .collect(Collectors.toMap(OrganisationNameDTO::getId, o -> o));
    }

    private Map<Long, String> getVenueNameMapping(final List<Venue> venues) {
        if (venues.isEmpty()) {
            return Collections.emptyMap();
        }

        return venues.stream().collect(Collectors.toMap(Venue::getId, Venue::getName));
    }

    private Map<Long, String> getVenueAddressMapping(final List<Venue> venues) {
        if (venues.isEmpty()) {
            return Collections.emptyMap();
        }

        return venues.stream().collect(Collectors.toMap(Venue::getId, venue ->
                Optional.ofNullable(venue.getAddress())
                        .map(address -> {
                            final String streetAddress = Optional.ofNullable(address.getStreetAddress()).orElse("");
                            final String postalCode = Optional.ofNullable(address.getPostalCode()).orElse("");
                            final String city = Optional.ofNullable(address.getCity()).orElse("");
                            return streetAddress + " " + postalCode + " " + city;
                        })
                        .orElse("")));
    }
}
