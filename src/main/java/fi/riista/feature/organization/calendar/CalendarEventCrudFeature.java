package fi.riista.feature.organization.calendar;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.shootingtest.ShootingTestEventRepository;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.DateUtil.streamCurrentAndNextHuntingYear;
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

    @Resource
    private CalendarEventDTOTransformer dtoTransformer;

    @Resource
    private AdditionalCalendarEventService additionalCalendarEventService;

    @Override
    protected JpaRepository<CalendarEvent, Long> getRepository() {
        return calendarEventRepository;
    }

    @Override
    protected void updateEntity(final CalendarEvent entity, final CalendarEventDTO dto) {
        final LocalDate today = today();

        final CalendarEventType eventType = dto.getCalendarEventType();
        final LocalDate date = dto.getDate();
        final boolean canUpdateAllFields;

        RhyEventTimeException.assertEventNotTooFarInPast(date, activeUserService.isModeratorOrAdmin());

        if (entity.isNew()) {
            checkArgument(!eventType.isShootingTest() || date.isAfter(today.plusDays(6)),
                    "For this calendarEventType date must be at least 7 days to future. " + eventType + " " + date);

            entity.setOrganisation(organisationRepository.getOne(dto.getOrganisation().getId()));
            canUpdateAllFields = true;
        } else {
            final CalendarEventType entityType = entity.getCalendarEventType();
            final CalendarEventType DTOType = dto.getCalendarEventType();
            final boolean isDTOTypeActive = CalendarEventType.activeCalendarEventTypes().contains(DTOType);
            checkArgument(isDTOTypeActive || entityType == DTOType,
                    "Event type is not active anymore");

            canUpdateAllFields = !isLockedAsPastCalendarEvent(entity);
            if (canUpdateAllFields) {
                additionalCalendarEventService.updateAdditionalCalendarEvents(dto.getAdditionalCalendarEvents(), entity);
                entity.forceRevisionUpdate();
            }
        }

        if (canUpdateAllFields) {
            entity.setCalendarEventType(eventType);
            entity.setDate(DateUtil.toDateNullSafe(date));
            entity.setBeginTime(dto.getBeginTime());
            entity.setEndTime(dto.getEndTime());
            entity.setVenue(venueRepository.getOne(dto.getVenue().getId()));
            entity.setPublicVisibility(dto.getPublicVisibility());
            entity.setExcludedFromStatistics(dto.getExcludedFromStatistics());
            entity.setNonSubsidizable(dto.isNonSubsidizable());
            entity.setRemoteEvent(dto.isRemoteEvent());
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setParticipants(dto.getParticipants());
    }

    @Override
    protected void afterCreate(CalendarEvent entity, CalendarEventDTO dto) {
        if (dto.getAdditionalCalendarEvents() != null && dto.getAdditionalCalendarEvents().size() > 0) {
            additionalCalendarEventService.addAdditionalCalendarEvents(dto.getAdditionalCalendarEvents(), entity);
        }
    }

    @Override
    protected void delete(final CalendarEvent event) {
        if (isLockedAsPastCalendarEvent(event) || isLockedAsPastStatistics(event)) {
            throw new CannotDeletePastCalendarEventException();
        }

        super.delete(event);
    }

    @Override
    protected CalendarEventDTO toDTO(@Nonnull final CalendarEvent event) {
        return dtoTransformer.apply(event);
    }

    private boolean isLockedAsPastCalendarEvent(final CalendarEvent event) {
        return event.isLockedAsPastCalendarEvent() && !activeUserService.isModeratorOrAdmin()
                || isAssociatedWithOpenedShootingTestEvent(event);
    }

    private boolean isAssociatedWithOpenedShootingTestEvent(final CalendarEvent event) {
        return event.getCalendarEventType().isShootingTest()
                && shootingTestEventRepository.findByCalendarEvent(event).isPresent();
    }

    private boolean isLockedAsPastStatistics(final CalendarEvent event) {
        return event.isLockedAsPastStatistics() && !activeUserService.isModeratorOrAdmin();
    }
}
