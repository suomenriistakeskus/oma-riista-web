package fi.riista.feature.organization.calendar;

import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalTime;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CalendarEventCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private CalendarEventCrudFeature feature;

    @Resource
    private CalendarEventRepository repository;

    @Test
    public void testCreate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Venue venue = model().newVenue();

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                final CalendarEventDTO inputDto = createDTO(rhy, venue, false);
                final CalendarEventDTO outputDto = feature.create(inputDto);

                runInTransaction(() -> {
                    final CalendarEvent event = repository.findOne(outputDto.getId());
                    assertNotNull(event);

                    assertEquals(rhy.getId(), event.getOrganisation().getId());
                    assertEquals(venue.getId(), event.getVenue().getId());

                    assertEquals(inputDto.getCalendarEventType(), event.getCalendarEventType());
                    assertEquals(inputDto.getDate(), DateUtil.toLocalDateNullSafe(event.getDate()));
                    assertEquals(inputDto.getBeginTime(), event.getBeginTime());
                    assertEquals(inputDto.getEndTime(), event.getEndTime());
                    assertEquals(inputDto.getName(), event.getName());
                    assertEquals(inputDto.getDescription(), event.getDescription());
                });
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateShootingTestEvent6DaysIntoFuture() {
        createShootingTestWithDayOffset(6);
    }

    @Test
    public void testCreateShootingTestEvent7DaysIntoFuture() {
        createShootingTestWithDayOffset(7);
    }

    @Test
    public void testCreateShootingTestEvent8DaysIntoFuture() {
        createShootingTestWithDayOffset(8);
    }

    private void createShootingTestWithDayOffset(final int dayOffset) {
        withRhy(rhy -> {
            final Venue venue = model().newVenue();

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final CalendarEventDTO dto = createDTO(rhy, venue, true);
                dto.setDate(today().plusDays(dayOffset));

                feature.create(dto);
            });
        });
    }

    private CalendarEventDTO createDTO(final Riistanhoitoyhdistys rhy, final Venue venue, final boolean shootingTest) {
        final CalendarEventDTO dto = new CalendarEventDTO();

        dto.setOrganisation(OrganisationDTO.create(rhy));
        dto.setVenue(VenueDTO.create(venue, venue.getAddress()));

        dto.setCalendarEventType(some(CalendarEventType.getTypes(shootingTest)));
        dto.setDate(today().plusDays(shootingTest ? 7 : 0));
        dto.setBeginTime(new LocalTime(18, 0));
        dto.setEndTime(new LocalTime(21, 0));
        dto.setName("Name " + nextPositiveInt());
        dto.setDescription("Description " + nextPositiveInt());

        return dto;
    }

    @Test
    public void testUpdate() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final Venue venue = model().newVenue();
            final Venue venue2 = model().newVenue();
            final CalendarEvent event = model().newCalendarEvent(rhy, some(CalendarEventType.class), today(), venue);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation change should be ineffective because it is ignored in update
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event, anotherRhy, venue2, venue2.getAddress(), false);

                mutate(dto);

                feature.update(dto);

                runInTransaction(() -> {
                    final CalendarEvent reloaded = repository.findOne(event.getId());

                    assertEquals(rhy.getId(), reloaded.getOrganisation().getId());
                    assertEquals(venue2.getId(), reloaded.getVenue().getId());

                    assertEquals(dto.getCalendarEventType(), reloaded.getCalendarEventType());
                    assertEquals(dto.getDate(), DateUtil.toLocalDateNullSafe(reloaded.getDate()));
                    assertEquals(dto.getBeginTime(), reloaded.getBeginTime());
                    assertEquals(dto.getEndTime(), reloaded.getEndTime());
                    assertEquals(dto.getName(), reloaded.getName());
                    assertEquals(dto.getDescription(), reloaded.getDescription());
                });
            });
        }));
    }

    @Test
    public void testUpdateAfterShootingTestEventAssociated() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final CalendarEvent event = model().newCalendarEvent(rhy, true, today());
            final Venue venue = event.getVenue();
            final Venue venue2 = model().newVenue();

            model().newShootingTestEvent(event);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation and venue changes should be ineffective because them are ignored in update.
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event, anotherRhy, venue2, venue2.getAddress(), false);

                mutate(dto);

                feature.update(dto);

                runInTransaction(() -> {
                    final CalendarEvent reloaded = repository.findOne(event.getId());

                    // Assert immutable fields.
                    assertEquals(rhy.getId(), reloaded.getOrganisation().getId());
                    assertEquals(venue.getId(), reloaded.getVenue().getId());
                    assertEquals(event.getCalendarEventType(), reloaded.getCalendarEventType());
                    assertEquals(event.getDate(), reloaded.getDate());
                    assertEquals(event.getBeginTime(), reloaded.getBeginTime());
                    assertEquals(event.getEndTime(), reloaded.getEndTime());

                    // Assert mutable fields.
                    assertEquals(dto.getName(), reloaded.getName());
                    assertEquals(dto.getDescription(), reloaded.getDescription());
                });
            });
        }));
    }

    private void mutate(final CalendarEventDTO dto) {
        dto.setCalendarEventType(someOtherThan(dto.getCalendarEventType(), CalendarEventType.class));
        dto.setDate(dto.getDate().plusDays(1));
        dto.setBeginTime(dto.getBeginTime().plusHours(1));
        dto.setEndTime(dto.getEndTime().plusHours(1));
        dto.setName(dto.getName() + "UPDATED");
        dto.setDescription(dto.getDescription() + "UPDATED");
    }

    @Test
    public void testUpdateShootingTestEvent6DaysIntoFuture() {
        testUpdateShootingTestEventIntoFuture(6);
    }

    @Test
    public void testUpdateShootingTestEvent7DaysIntoFuture() {
        testUpdateShootingTestEventIntoFuture(7);
    }

    @Test
    public void testUpdateShootingTestEvent8DaysIntoFuture() {
        testUpdateShootingTestEventIntoFuture(8);
    }

    private void testUpdateShootingTestEventIntoFuture(final int dayOffset) {
        withRhy(rhy -> {

            final CalendarEvent event = model().newCalendarEvent(rhy, true, today().plusDays(14));
            final Venue venue = event.getVenue();

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final CalendarEventDTO eventDTO = CalendarEventDTO.create(event, rhy, venue, venue.getAddress(), false);
                event.setDate(today().plusDays(dayOffset).toDate());

                feature.update(eventDTO);
            });
        });
    }
}
