package fi.riista.feature.organization.calendar;

import fi.riista.feature.SimpleAbstractCrudFeature;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.util.DateUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Function;

@Component
public class CalendarEventCrudFeature extends SimpleAbstractCrudFeature<Long, CalendarEvent, CalendarEventDTO> {

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private VenueRepository venueRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Override
    protected JpaRepository<CalendarEvent, Long> getRepository() {
        return calendarEventRepository;
    }

    @Override
    protected void updateEntity(final CalendarEvent entity, final CalendarEventDTO dto) {
        Organisation organisation = organisationRepository.getOne(dto.getOrganisation().getId());
        Venue venue = venueRepository.getOne(dto.getVenue().getId());
        entity.setVenue(venue);
        entity.setOrganisation(organisation);
        entity.setCalendarEventType(dto.getCalendarEventType());
        entity.setName(dto.getName());
        entity.setDate(DateUtil.toDateNullSafe(dto.getDate()));
        entity.setBeginTime(dto.getBeginTime());
        entity.setEndTime(dto.getEndTime());
        entity.setDescription(dto.getDescription());
    }

    @Override
    protected Function<CalendarEvent, CalendarEventDTO> entityToDTOFunction() {
        return CalendarEventDTO::create;
    }
}
