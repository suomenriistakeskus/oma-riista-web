package fi.riista.feature.shootingtest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.calendar.QVenue;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepositoryCustom.ParticipantSummary;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.util.jpa.JpaGroupingUtils.groupRelations;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class ShootingTestCalendarEventDTOTransformer
        extends ListTransformer<CalendarEvent, ShootingTestCalendarEventDTO> {

    @Resource
    private ShootingTestEventRepository eventRepo;

    @Resource
    private ShootingTestOfficialRepository officialRepo;

    @Resource
    private ShootingTestParticipantRepository participantRepo;

    @Resource
    private JPAQueryFactory queryFactory;

    @Nonnull
    @Override
    protected List<ShootingTestCalendarEventDTO> transform(@Nonnull final List<CalendarEvent> calendarEvents) {
        if (calendarEvents.isEmpty()) {
            return emptyList();
        }

        final Map<Long, Venue> venueMapping = getVenuesByCalendarEventId(calendarEvents);

        final List<ShootingTestEvent> shootingTestEvents = eventRepo.findByCalendarEventIn(calendarEvents);

        final Map<CalendarEvent, ShootingTestEvent> eventIndex;
        final Map<ShootingTestEvent, List<ShootingTestOfficial>> officialMapping;
        final Map<Long, ParticipantSummary> participantSummaryMapping;

        if (shootingTestEvents.isEmpty()) {
            eventIndex = emptyMap();
            officialMapping = emptyMap();
            participantSummaryMapping = emptyMap();
        } else {
            eventIndex = F.index(shootingTestEvents, ShootingTestEvent::getCalendarEvent);
            officialMapping = groupRelations(shootingTestEvents, ShootingTestOfficial_.shootingTestEvent, officialRepo);
            participantSummaryMapping = participantRepo.getParticipantSummaryByShootingTestEventId(shootingTestEvents);
        }

        return calendarEvents.stream().map(calendarEvent -> {

            final ShootingTestEvent shootingTestEvent = eventIndex.get(calendarEvent);
            final Venue venue = venueMapping.get(calendarEvent.getId());

            final List<ShootingTestOfficialDTO> officialDTOs;
            final ParticipantSummary participantSummary;

            if (shootingTestEvent != null) {
                officialDTOs = Optional
                        .ofNullable(officialMapping.get(shootingTestEvent))
                        .map(officials -> {
                            return officials
                                    .stream()
                                    .map(official -> {
                                        final Person person = official.getOccupation().getPerson();
                                        return ShootingTestOfficialDTOTransformer.create(official, person);
                                    })
                                    .collect(toList());
                        })
                        .orElseGet(Collections::emptyList);

                participantSummary =
                        participantSummaryMapping.getOrDefault(shootingTestEvent.getId(), ParticipantSummary.EMPTY);
            } else {
                officialDTOs = emptyList();
                participantSummary = ParticipantSummary.EMPTY;
            }

            return create(shootingTestEvent, calendarEvent, venue, venue.getAddress(), officialDTOs, participantSummary);

        }).collect(toList());
    }

    private Map<Long, Venue> getVenuesByCalendarEventId(final List<CalendarEvent> events) {
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QVenue VENUE = QVenue.venue;
        final QAddress ADDRESS = QAddress.address;

        return queryFactory
                .select(CALENDAR_EVENT.id, VENUE)
                .from(CALENDAR_EVENT)
                .join(CALENDAR_EVENT.venue, VENUE)
                .join(VENUE.address, ADDRESS).fetchJoin()
                .where(CALENDAR_EVENT.in(events))
                .fetch()
                .stream()
                .collect(toMap(t -> t.get(CALENDAR_EVENT.id), t -> t.get(VENUE)));
    }

    private static ShootingTestCalendarEventDTO create(@Nullable final ShootingTestEvent shootingTestEvent,
                                                       @Nonnull final CalendarEvent calendarEvent,
                                                       @Nonnull final Venue venue,
                                                       @Nonnull final Address venueAddress,
                                                       @Nonnull final List<ShootingTestOfficialDTO> officials,
                                                       @Nonnull final ParticipantSummary participantSummary) {

        final ShootingTestCalendarEventDTO dto = new ShootingTestCalendarEventDTO();

        dto.setRhyId(calendarEvent.getOrganisation().getId());
        dto.setCalendarEventId(calendarEvent.getId());

        if (shootingTestEvent != null) {
            dto.setShootingTestEventId(shootingTestEvent.getId());
            dto.setLockedTime(DateUtil.toDateTimeNullSafe(shootingTestEvent.getLockedTime()));
        }

        dto.setCalendarEventType(calendarEvent.getCalendarEventType());
        dto.setName(calendarEvent.getName());
        dto.setDescription(calendarEvent.getDescription());

        dto.setDate(DateUtil.toLocalDateNullSafe(calendarEvent.getDate()));
        dto.setBeginTime(calendarEvent.getBeginTime());
        dto.setEndTime(calendarEvent.getEndTime());

        dto.setVenue(VenueDTO.create(venue, venueAddress));
        dto.setOfficials(officials);

        dto.setNumberOfAllParticipants(participantSummary.numberOfAllParticipants);
        dto.setNumberOfCompletedParticipants(participantSummary.numberOfCompletedParticipants);
        dto.setNumberOfParticipantsWithNoAttempts(participantSummary.numberOfParticipantsWithNoAttempts);
        dto.setTotalPaidAmount(participantSummary.totalPaidAmount);

        return dto;
    }
}
