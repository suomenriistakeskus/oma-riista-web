package fi.riista.feature.organization.calendar;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.shootingtest.ShootingTestEventRepository;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;

@Service
public class CalendarEventCrudFeature extends AbstractCrudFeature<Long, CalendarEvent, CalendarEventDTO> {

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private VenueRepository venueRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private ShootingTestEventRepository shootingTestEventRepository;

    @Override
    protected JpaRepository<CalendarEvent, Long> getRepository() {
        return calendarEventRepository;
    }

    @Override
    protected void updateEntity(final CalendarEvent entity, final CalendarEventDTO dto) {
        final CalendarEventType eventType = dto.getCalendarEventType();
        final LocalDate date = dto.getDate();
        final boolean lockedByShootingTestEventPresence;

        if (entity.isNew()) {
            Preconditions.checkArgument(!eventType.isShootingTest() || date.isAfter(today().plusDays(6)),
                    "For this calendarEventType date must be at least 7 days to future. " + eventType + " " + date);

            entity.setOrganisation(organisationRepository.getOne(dto.getOrganisation().getId()));
            lockedByShootingTestEventPresence = false;
        } else {
            lockedByShootingTestEventPresence = shootingTestEventRepository.findByCalendarEvent(entity).isPresent();
        }

        if (!lockedByShootingTestEventPresence) {
            entity.setCalendarEventType(eventType);
            entity.setDate(DateUtil.toDateNullSafe(date));
            entity.setBeginTime(dto.getBeginTime());
            entity.setEndTime(dto.getEndTime());
            entity.setVenue(venueRepository.getOne(dto.getVenue().getId()));
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
    }

    @Override
    protected CalendarEventDTO toDTO(@Nonnull final CalendarEvent event) {
        final boolean hasShootingTestEvent = shootingTestEventRepository.findByCalendarEvent(event).isPresent();

        return CalendarEventDTO.create(
                event, event.getOrganisation(), event.getVenue(), event.getVenue().getAddress(), hasShootingTestEvent);
    }
}
