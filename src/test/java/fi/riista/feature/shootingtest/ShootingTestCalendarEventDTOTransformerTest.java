package fi.riista.feature.shootingtest;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.calendar.VenueDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepositoryCustom.ParticipantSummary;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.currency;
import static fi.riista.util.DateUtil.today;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ShootingTestCalendarEventDTOTransformerTest extends EmbeddedDatabaseTest
        implements ShootingTestFixtureMixin {

    @Resource
    private ShootingTestCalendarEventDTOTransformer transformer;

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private ShootingTestEventRepository shootingTestEventRepository;

    @Test
    public void testWithInvalidCalendarEventTypes() {
        withRhy(rhy -> {

            final List<CalendarEvent> calendarEvents = EnumSet
                    .allOf(CalendarEventType.class)
                    .stream()
                    .filter(type -> !type.isShootingTest())
                    .map(type -> model().newCalendarEvent(rhy, type))
                    .collect(toList());

            persistInNewTransaction();

            calendarEvents.forEach(calendarEvent -> {
                try {

                    transformer.apply(calendarEvent);
                    fail("Expected exception for calendar event type: " + calendarEvent.getCalendarEventType().name());

                } catch (final IllegalArgumentException e) {
                    // Exception of this type is expected.
                } catch (final Throwable e) {
                    fail("Unexpected error: " + e);
                }
            });
        });
    }

    @Test
    public void testWhenShootingTestEventNotOpened() {
        withRhy(rhy1 -> withRhy(rhy2 -> {

            final CalendarEvent event1 = model().newCalendarEvent(rhy1, AMPUMAKOE, today());
            final CalendarEvent event2 = model().newCalendarEvent(rhy2, JOUSIAMPUMAKOE, today().minusDays(1));

            persistInNewTransaction();

            final List<ShootingTestCalendarEventDTO> results = transformer.apply(asList(event1, event2));

            assertFieldsWhenShootingTestEventIsNotPresent(event1, results.get(0));
            assertFieldsWhenShootingTestEventIsNotPresent(event2, results.get(1));
        }));
    }

    @Test
    public void testWithShootingTestEvents() {
        withRhy(rhy1 -> withRhy(rhy2 -> {

            final ShootingTestEvent event1 = openEvent(rhy1, today());
            final ShootingTestEvent event2 = openEvent(rhy1, today().minusDays(1));

            // Create three completed participants for event2.
            createParticipantWithOneAttempt(event2);
            createParticipantWithOneAttempt(event2);
            createParticipantWithOneAttempt(event2);

            // Create one uncompleted participant with two attempts for event2.
            final ShootingTestParticipant participant = model().newShootingTestParticipant(event2, model().newPerson());
            model().newShootingTestAttempt(participant);
            model().newShootingTestAttempt(participant);

            // Create two participants with no attempts for event2.
            model().newShootingTestParticipant(event2, model().newPerson());
            model().newShootingTestParticipant(event2, model().newPerson());

            event2.close();

            persistInNewTransaction();

            final List<ShootingTestCalendarEventDTO> results =
                    transformer.apply(asList(event1.getCalendarEvent(), event2.getCalendarEvent()));

            assertFieldsWhenShootingTestEventIsPresent(event1, null, ParticipantSummary.EMPTY, results.get(0));

            final DateTime expectedLockedTimeForEvent2 = DateUtil.toDateTimeNullSafe(event2.getLockedTime());
            final ParticipantSummary participantSummaryForEvent2 = new ParticipantSummary(6, 3, 2, currency(60));

            assertFieldsWhenShootingTestEventIsPresent(
                    event2, expectedLockedTimeForEvent2, participantSummaryForEvent2, results.get(1));
        }));
    }

    private static void assertFieldsWhenShootingTestEventIsNotPresent(final CalendarEvent event,
                                                                      final ShootingTestCalendarEventDTO dto) {
        assertCalendarEventAndVenueBasedFields(event, dto);

        assertNull(dto.getShootingTestEventId());
        assertNull(dto.getLockedTime());

        assertEmpty(dto.getOfficials());

        assertEquals(0, dto.getNumberOfAllParticipants());
        assertEquals(0, dto.getNumberOfCompletedParticipants());
        assertEquals(0, dto.getNumberOfParticipantsWithNoAttempts());
        assertEquals(Constants.ZERO_MONETARY_AMOUNT, dto.getTotalPaidAmount());
    }

    private static void assertFieldsWhenShootingTestEventIsPresent(final ShootingTestEvent sourceEvent,
                                                                   final DateTime expectedLockedTime,
                                                                   final ParticipantSummary expectedParticipantSummary,
                                                                   final ShootingTestCalendarEventDTO dto) {

        assertCalendarEventAndVenueBasedFields(sourceEvent.getCalendarEvent(), dto);

        assertEquals(sourceEvent.getId(), dto.getShootingTestEventId());
        assertEquals(expectedLockedTime, dto.getLockedTime());

        assertEventOfficials(sourceEvent, dto);

        assertEquals(expectedParticipantSummary.numberOfAllParticipants, dto.getNumberOfAllParticipants());
        assertEquals(expectedParticipantSummary.numberOfCompletedParticipants, dto.getNumberOfCompletedParticipants());
        assertEquals(
                expectedParticipantSummary.numberOfParticipantsWithNoAttempts,
                dto.getNumberOfParticipantsWithNoAttempts());
        assertEquals(expectedParticipantSummary.totalPaidAmount, dto.getTotalPaidAmount());
    }

    private static void assertCalendarEventAndVenueBasedFields(final CalendarEvent event,
                                                               final ShootingTestCalendarEventDTO dto) {

        assertEquals(event.getId(), Long.valueOf(dto.getCalendarEventId()));
        assertEquals(event.getOrganisation().getId(), Long.valueOf(dto.getRhyId()));

        assertEquals(event.getCalendarEventType(), dto.getCalendarEventType());
        assertEquals(event.getName(), dto.getName());
        assertEquals(event.getDescription(), dto.getDescription());
        assertEquals(event.getDateAsLocalDate(), dto.getDate());
        assertEquals(event.getBeginTime(), dto.getBeginTime());
        assertEquals(event.getEndTime(), dto.getEndTime());

        final Venue venue = event.getVenue();
        final VenueDTO venueDTO = dto.getVenue();

        assertNotNull(venueDTO);
        assertEquals(VenueDTO.create(venue, venue.getAddress()), venueDTO);
    }

    private static void assertEventOfficials(final ShootingTestEvent shootingTestEvent,
                                             final ShootingTestCalendarEventDTO dto) {

        final List<ShootingTestOfficialDTO> expectedOfficials =
                F.mapNonNullsToList(shootingTestEvent.getOfficials(), official -> {
                    return ShootingTestOfficialDTO.create(official, official.getOccupation().getPerson());
                });

        assertEquals(expectedOfficials, dto.getOfficials());
    }

    // This test verifies that N+1 issue is not present.
    @Test
    public void testThatNumberOfQueriesIsFixedAndSmall() {
        final LocalDate today = today();

        final List<CalendarEvent> calendarEvents = IntStream
                .rangeClosed(1, 30)
                .mapToObj(i -> {

                    final ShootingTestEvent event = openEvent(model().newRiistanhoitoyhdistys(), today);
                    createParticipantWithOneAttempt(event);
                    createParticipantWithOneAttempt(event);

                    return event.getCalendarEvent();
                })
                .collect(toList());

        persistInNewTransaction();

        runInTransaction(() -> {
            final List<CalendarEvent> reloaded = calendarEventRepository.findAll(F.getUniqueIds(calendarEvents));

            // "5" is currently the upper limit of SQL queries needed to be executed. This may
            // need to be raised in case new logic is added to the transformer.
            assertMaxQueryCount(5, () -> transformer.apply(reloaded));
        });
    }

    @Test
    public void testLastModifier_asPerson() {
        final Person person = model().newPerson();

        testLastModifier(createUser(person), (u, modificationTime) -> {
            return LastModifierDTO.createForPerson(person, modificationTime);
        });
    }

    @Test
    public void testLastModifier_asModeratorUser() {
        final SystemUser moderator = createNewModerator();
        moderator.setFirstName("ABC");
        moderator.setLastName("DEF");

        testLastModifier(moderator, LastModifierDTO::createForAdminOrModerator);
    }

    private void testLastModifier(final SystemUser activeUser,
                                  final BiFunction<SystemUser, DateTime, LastModifierDTO> yieldExceptedLastModifier) {
        withRhy(rhy -> {

            final ShootingTestEvent event = openEvent(rhy, today());

            onSavedAndAuthenticated(activeUser, () -> {

                // Trigger update with active user.
                doWithShootingTestEvent(event.getId(), ShootingTestEvent::close);

                // Check resulting last modifier.
                doWithShootingTestEvent(event.getId(), reloaded -> {
                    final DateTime modificationTime = DateUtil.toDateTimeNullSafe(reloaded.getModificationTime());

                    final ShootingTestCalendarEventDTO eventDTO = transformer.apply(reloaded.getCalendarEvent());
                    final LastModifierDTO actualLastModifier = eventDTO.getLastModifier();

                    assertEquals(yieldExceptedLastModifier.apply(activeUser, modificationTime), actualLastModifier);
                });
            });
        });
    }

    private void doWithShootingTestEvent(final long shootingTestEventId, final Consumer<ShootingTestEvent> consumer) {
        runInTransaction(() -> {
            consumer.accept(shootingTestEventRepository.findOne(shootingTestEventId));
        });
    }
}
