package fi.riista.feature.organization.calendar;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestEventRepository;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static fi.riista.util.Collect.indexingByIdOf;
import static fi.riista.util.jpa.CriteriaUtils.singleQueryFunction;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.mapping;

@Component
public class CalendarEventDTOTransformer extends ListTransformer<CalendarEvent, CalendarEventDTO> {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private VenueRepository venueRepository;

    @Resource
    private ShootingTestEventRepository shootingTestEventRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private AdditionalCalendarEventRepository additionalCalendarEventRepository;

    @Override
    protected List<CalendarEventDTO> transform(@Nonnull final List<CalendarEvent> events) {

        final Function<CalendarEvent, Organisation> eventToOrganisation = getCalendarEventToOrganisationMapping(events);
        final Function<CalendarEvent, Venue> eventToVenue = getCalendarEventToVenueMapping(events);
        final boolean isModeratorOrAdmin = activeUserService.isModeratorOrAdmin();
        final Predicate<CalendarEvent> isLockedAsPastCalendarEventTester = getCalendarEventToLockingStatusMapping(events, isModeratorOrAdmin);
        final Predicate<CalendarEvent> isLockedAsPastStatisticsTester = getCalendarEventToLockedStatisticsMapping(events, isModeratorOrAdmin);
        final Map<Long, List<AdditionalCalendarEventDTO>> eventIdToAdditionalEventDTOs =
                getEventIdToAdditionalCalendarEventDTOs(events);

        return events.stream().map(event -> {

            final Organisation organisation = eventToOrganisation.apply(event);
            final Venue venue = eventToVenue.apply(event);
            final boolean isLockedAsPastCalendarEvent = isLockedAsPastCalendarEventTester.test(event);
            final boolean isLockedAsPastStatistics = isLockedAsPastStatisticsTester.test(event);

            return CalendarEventDTO.create(event,
                    organisation,
                    venue,
                    venue.getAddress(),
                    isLockedAsPastCalendarEvent,
                    isLockedAsPastStatistics,
                    eventIdToAdditionalEventDTOs.get(event.getId()));
        }).collect(toList());
    }

    @Nonnull
    private Function<CalendarEvent, Organisation> getCalendarEventToOrganisationMapping(final Iterable<CalendarEvent> events) {
        return singleQueryFunction(events, CalendarEvent::getOrganisation, organisationRepository, true);
    }

    @Nonnull
    private Function<CalendarEvent, Venue> getCalendarEventToVenueMapping(final Iterable<CalendarEvent> events) {
        return singleQueryFunction(events, CalendarEvent::getVenue, venueRepository, fetch(Venue_.address), true);
    }

    @Nonnull
    private Predicate<CalendarEvent> getCalendarEventToLockingStatusMapping(final Collection<CalendarEvent> events, boolean isModeratorOrAdmin) {
        final List<CalendarEvent> shootingTestCalendarEvents =
                events.stream().filter(event -> event.getCalendarEventType().isShootingTest()).collect(toList());

        final Map<Long, ShootingTestEvent> calendarEventIdToShootingTestEvent;

        if (shootingTestCalendarEvents.isEmpty()) {
            calendarEventIdToShootingTestEvent = emptyMap();
        } else {
            calendarEventIdToShootingTestEvent = shootingTestEventRepository
                    .findByCalendarEventIn(shootingTestCalendarEvents)
                    .stream()
                    .collect(indexingByIdOf(ShootingTestEvent::getCalendarEvent));
        }

        return event -> {
            return !isModeratorOrAdmin && event.isLockedAsPastCalendarEvent()
                    || calendarEventIdToShootingTestEvent.get(event.getId()) != null;
        };
    }

    @Nonnull
    private Predicate<CalendarEvent> getCalendarEventToLockedStatisticsMapping(final Collection<CalendarEvent> events, boolean isModeratorOrAdmin) {
        return event -> {
            return !isModeratorOrAdmin && event.isLockedAsPastStatistics();
        };
    }

    private Map<Long, List<AdditionalCalendarEventDTO>> getEventIdToAdditionalCalendarEventDTOs(final List<CalendarEvent> events) {
        if (events.size() == 0) {
            return emptyMap();
        }

        final List<AdditionalCalendarEvent> additionalEvents = additionalCalendarEventRepository.findByCalendarEventIn(events);

        final Function<AdditionalCalendarEvent, Venue> additionalEventToVenue =
                singleQueryFunction(additionalEvents, AdditionalCalendarEvent::getVenue, venueRepository, fetch(Venue_.address), true);

        return additionalEvents.stream()
                .collect(groupingBy(
                        a -> a.getCalendarEvent().getId(),
                        mapping(a -> {
                            final Venue venue = additionalEventToVenue.apply(a);
                            return AdditionalCalendarEventDTO.create(a, venue, venue.getAddress());
                        }, toList())));
    }
}
