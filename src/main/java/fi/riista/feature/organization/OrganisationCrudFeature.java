package fi.riista.feature.organization;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventDTO;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestEventRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static fi.riista.util.Collect.idSet;

@Service
public class OrganisationCrudFeature extends AbstractCrudFeature<Long, Organisation, OrganisationDTO> {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private ShootingTestEventRepository shootingTestEventRepository;

    @Override
    protected JpaRepository<Organisation, Long> getRepository() {
        return organisationRepository;
    }

    @Override
    protected OrganisationDTO toDTO(@Nonnull final Organisation entity) {
        return OrganisationDTO.create(entity);
    }

    @Override
    protected void updateEntity(final Organisation org, final OrganisationDTO dto) {
    }

    @Transactional(readOnly = true)
    public List<CalendarEventDTO> listEvents(final long organisationId) {
        final Organisation org = requireEntity(organisationId, EntityPermission.READ);
        final List<CalendarEvent> calendarEvents = calendarEventRepository.findByOrganisation(org);
        final Set<Long> idsOfCalendarEventsAssociatedWithShootingTestEvent =
                shootingTestEventRepository.findByCalendarEventIn(calendarEvents)
                        .stream()
                        .map(ShootingTestEvent::getCalendarEvent)
                        .collect(idSet());

        return transformToDTO(calendarEvents, idsOfCalendarEventsAssociatedWithShootingTestEvent);
    }

    private static List<CalendarEventDTO> transformToDTO(final List<CalendarEvent> events,
                                                         final Set<Long> idsOfCalendarEventsAssociatedWithShootingTestEvent) {

        return F.mapNonNullsToList(events, event -> {
            final Venue venue = event.getVenue();
            final boolean hasShootingTestEvent =
                    idsOfCalendarEventsAssociatedWithShootingTestEvent.contains(event.getId());

            return CalendarEventDTO.create(
                    event, event.getOrganisation(), venue, venue.getAddress(), hasShootingTestEvent);
        });
    }

    @Transactional(readOnly = true)
    public List<VenueDTO> listVenues(final long organisationId) {
        final Organisation org = organisationRepository.getOneFetchingVenues(organisationId);
        activeUserService.assertHasPermission(org, EntityPermission.READ);
        return F.mapNonNullsToList(org.getVenues(), venue -> VenueDTO.create(venue, venue.getAddress()));
    }
}
