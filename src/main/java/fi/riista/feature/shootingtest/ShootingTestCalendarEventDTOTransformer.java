package fi.riista.feature.shootingtest;

import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.calendar.QVenue;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepositoryCustom.ParticipantSummary;
import fi.riista.feature.shootingtest.official.QShootingTestOfficial;
import fi.riista.feature.shootingtest.official.ShootingTestOfficial;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialDTO;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

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
    private LastModifierService lastModifierService;

    @Resource
    private JPAQueryFactory queryFactory;

    @Nonnull
    @Override
    protected List<ShootingTestCalendarEventDTO> transform(@Nonnull final List<CalendarEvent> calendarEvents) {
        if (calendarEvents.isEmpty()) {
            return emptyList();
        }

        final Associations assocations = new Associations(calendarEvents);

        return F.mapNonNullsToList(calendarEvents, calendarEvent -> {

            checkArgument(calendarEvent.getCalendarEventType().isShootingTest(), "Event must be a shooting test event");

            final ShootingTestCalendarEventDTO.Builder builder = ShootingTestCalendarEventDTO
                    .builder()
                    .withCalendarEvent(calendarEvent)
                    .withVenue(assocations.getVenue(calendarEvent));

            final ShootingTestEvent shootingTestEvent = assocations.getShootingTestEvent(calendarEvent);

            if (shootingTestEvent == null) {
                return builder.build();
            }

            return builder
                    .withShootingTestEvent(shootingTestEvent)
                    .withOfficials(assocations.getOfficials(shootingTestEvent))
                    .withParticipantSummary(assocations.getParticipantSummary(shootingTestEvent))
                    .withLastModifier(assocations.getLastModifier(shootingTestEvent))
                    .build();
        });
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
                .transform(groupBy(CALENDAR_EVENT.id).as(VENUE));
    }

    private Map<Long, List<ShootingTestOfficial>> getOfficialsByShootingTestEventId(final List<ShootingTestEvent> events) {
        final QShootingTestOfficial OFFICIAL = QShootingTestOfficial.shootingTestOfficial;
        final QOccupation OCCUPATION = QOccupation.occupation;
        final QPerson PERSON = QPerson.person;

        final NumberPath<Long> eventId = OFFICIAL.shootingTestEvent.id;

        return queryFactory
                .select(eventId, OFFICIAL)
                .from(OFFICIAL)
                .join(OFFICIAL.occupation, OCCUPATION).fetchJoin()
                .join(OCCUPATION.person, PERSON).fetchJoin()
                .where(eventId.in(F.getUniqueIds(events)))
                .transform(groupBy(eventId).as(list(OFFICIAL)));
    }

    private class Associations {

        private final Map<Long, Venue> venueByCalendarEventId;
        private final Map<CalendarEvent, ShootingTestEvent> eventIndex;
        private final Map<Long, List<ShootingTestOfficial>> officialsByShootingTestEventId;
        private final Map<Long, ParticipantSummary> participantSummaryMapping;
        private final Map<ShootingTestEvent, LastModifierDTO> lastModifierMapping;

        Associations(final List<CalendarEvent> calendarEvents) {
            final List<ShootingTestEvent> shootingTestEvents = eventRepo.findByCalendarEventIn(calendarEvents);

            this.venueByCalendarEventId = getVenuesByCalendarEventId(calendarEvents);

            if (shootingTestEvents.isEmpty()) {
                this.eventIndex = emptyMap();
                this.officialsByShootingTestEventId = emptyMap();
                this.participantSummaryMapping = emptyMap();
                this.lastModifierMapping = emptyMap();
            } else {
                this.eventIndex = F.index(shootingTestEvents, ShootingTestEvent::getCalendarEvent);
                this.officialsByShootingTestEventId = getOfficialsByShootingTestEventId(shootingTestEvents);
                this.participantSummaryMapping =
                        participantRepo.getParticipantSummaryByShootingTestEventId(shootingTestEvents);
                this.lastModifierMapping = lastModifierService.getLastModifiers(shootingTestEvents);
            }
        }

        ShootingTestEvent getShootingTestEvent(final CalendarEvent calendarEvent) {
            return eventIndex.get(calendarEvent);
        }

        VenueDTO getVenue(final CalendarEvent calendarEvent) {
            final Venue venue = venueByCalendarEventId.get(calendarEvent.getId());
            return VenueDTO.create(venue, venue.getAddress());
        }

        List<ShootingTestOfficialDTO> getOfficials(final ShootingTestEvent shootingTestEvent) {
            final List<ShootingTestOfficial> officials = officialsByShootingTestEventId.get(shootingTestEvent.getId());

            if (F.isNullOrEmpty(officials)) {
                return emptyList();
            }

            return F.mapNonNullsToList(officials, official -> {
                return ShootingTestOfficialDTO.create(official, official.getOccupation().getPerson());
            });
        }

        ParticipantSummary getParticipantSummary(final ShootingTestEvent shootingTestEvent) {
            return participantSummaryMapping.getOrDefault(shootingTestEvent.getId(), ParticipantSummary.EMPTY);
        }

        LastModifierDTO getLastModifier(final ShootingTestEvent shootingTestEvent) {
            return lastModifierMapping.get(shootingTestEvent);
        }
    }
}
