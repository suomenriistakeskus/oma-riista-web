package fi.riista.feature.organization.calendar;

import fi.riista.feature.organization.OrganisationDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static fi.riista.feature.organization.calendar.CalendarEventType.KOULUTUSTILAISUUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.NUORISOTAPAHTUMA;
import static fi.riista.feature.organization.calendar.CalendarEventType.nonShootingTestTypes;
import static fi.riista.feature.organization.calendar.CalendarEventType.shootingTestTypes;
import static fi.riista.util.DateUtil.today;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CalendarEventCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private CalendarEventCrudFeature feature;

    @Resource
    private CalendarEventRepository repository;

    @Resource
    private AdditionalCalendarEventRepository additionalCalendarEventRepository;

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
                    assertEquals(inputDto.getDate(), event.getDateAsLocalDate());
                    assertEquals(inputDto.getBeginTime(), event.getBeginTime());
                    assertEquals(inputDto.getEndTime(), event.getEndTime());
                    assertEquals(inputDto.getName(), event.getName());
                    assertEquals(inputDto.getDescription(), event.getDescription());
                });
            });
        });
    }

    @Test
    public void testCreateWithAdditionalEvents() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Venue venue1 = model().newVenue();
            final Venue venue2 = model().newVenue();
            final Venue venue3 = model().newVenue();

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final CalendarEventDTO calendarEventDTO = createDTO(rhy, venue1, false);

                final LocalDate date1 = today().plusDays(1);
                final LocalTime beginTime1 = new LocalTime(18, 0);
                final LocalTime endTime1 = new LocalTime(18, 30);
                final AdditionalCalendarEventDTO additionalCalendarEventDTO1 =
                        createAdditionalCalendarEventDTO(venue2, date1, beginTime1, endTime1);

                final LocalDate date2 = today().plusDays(2);
                final LocalTime beginTime2 = new LocalTime(10, 0);
                final LocalTime endTime2 = new LocalTime(11, 30);
                final AdditionalCalendarEventDTO additionalCalendarEventDTO2 =
                        createAdditionalCalendarEventDTO(venue3, date2, beginTime2, endTime2);

                List<AdditionalCalendarEventDTO> additionalEventDTOs =
                        Arrays.asList(additionalCalendarEventDTO1, additionalCalendarEventDTO2);
                calendarEventDTO.setAdditionalCalendarEvents(additionalEventDTOs);

                final CalendarEventDTO outputDTO = feature.create(calendarEventDTO);

                runInTransaction(() -> {
                    List<AdditionalCalendarEventDTO> outputAdditionalDTOs = outputDTO.getAdditionalCalendarEvents();
                    assertEquals(2, outputAdditionalDTOs.size());

                    final AdditionalCalendarEvent event1 =
                            additionalCalendarEventRepository.findOne(outputAdditionalDTOs.get(0).getId());
                    assertNotNull(event1);
                    assertEquals(date1, event1.getDateAsLocalDate());
                    assertEquals(beginTime1, event1.getBeginTime());
                    assertEquals(endTime1, event1.getEndTime());
                    assertEquals(venue2, event1.getVenue());

                    final AdditionalCalendarEvent event2 =
                            additionalCalendarEventRepository.findOne(outputAdditionalDTOs.get(1).getId());
                    assertNotNull(event2);
                    assertEquals(date2, event2.getDateAsLocalDate());
                    assertEquals(beginTime2, event2.getBeginTime());
                    assertEquals(endTime2, event2.getEndTime());
                    assertEquals(venue3, event2.getVenue());
                });
            });
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTwoYearsPastAsCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Venue venue = model().newVenue();

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final CalendarEventDTO inputDto = createDTO(rhy, venue, false, today().minusYears(2));
                feature.create(inputDto);
            });
        });
    }

    @Test
    public void testCreateTwoYearsPastAsModerator() {
        withRhy(rhy -> {
            final Venue venue = model().newVenue();

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final CalendarEventDTO inputDto = createDTO(rhy, venue, false, today().minusYears(2));
                final CalendarEventDTO outputDto = feature.create(inputDto);

                runInTransaction(() -> {
                    final CalendarEvent event = repository.findOne(outputDto.getId());
                    assertNotNull(event);

                    assertEquals(rhy.getId(), event.getOrganisation().getId());
                    assertEquals(venue.getId(), event.getVenue().getId());

                    assertEquals(inputDto.getCalendarEventType(), event.getCalendarEventType());
                    assertEquals(inputDto.getDate(), event.getDateAsLocalDate());
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
        LocalDate date = today().plusDays(shootingTest ? 7 : 0);

        return createDTO(rhy, venue, shootingTest, date);
    }

    private CalendarEventDTO createDTO(final Riistanhoitoyhdistys rhy, final Venue venue, final boolean shootingTest, LocalDate date) {
        final CalendarEventDTO dto = new CalendarEventDTO();

        dto.setOrganisation(OrganisationDTO.create(rhy));
        dto.setVenue(VenueDTO.create(venue, venue.getAddress()));

        final EnumSet<CalendarEventType> availableEventTypes =
                shootingTest ? shootingTestTypes() : nonShootingTestTypes();

        dto.setCalendarEventType(some(availableEventTypes));
        dto.setDate(date);
        dto.setBeginTime(new LocalTime(18, 0));
        dto.setEndTime(new LocalTime(21, 0));
        dto.setName("Name " + nextPositiveInt());
        dto.setDescription("Description " + nextPositiveInt());

        return dto;
    }

    private AdditionalCalendarEventDTO createAdditionalCalendarEventDTO(final Venue venue,
                                                                        final LocalDate date,
                                                                        final LocalTime beginTime,
                                                                        final LocalTime endTime) {
        final AdditionalCalendarEventDTO dto = new AdditionalCalendarEventDTO();

        dto.setDate(date);
        dto.setBeginTime(beginTime);
        dto.setEndTime(endTime);

        dto.setVenue(VenueDTO.create(venue, venue.getAddress()));

        return dto;
    }

    @Test
    public void testUpdate() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final Venue venue = model().newVenue();
            final Venue venue2 = model().newVenue();

            final CalendarEvent event =
                    model().newCalendarEvent(rhy, some(nonShootingTestTypes()), today(), venue);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation change should be ineffective because it is ignored in update
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                anotherRhy,
                                venue2, venue2.getAddress(),
                                false, false,
                                emptyList());

                mutate(dto);

                feature.update(dto);

                runInTransaction(() -> {
                    final CalendarEvent reloaded = repository.findOne(event.getId());

                    assertEquals(rhy.getId(), reloaded.getOrganisation().getId());
                    assertEquals(venue2.getId(), reloaded.getVenue().getId());

                    assertEquals(dto.getCalendarEventType(), reloaded.getCalendarEventType());
                    assertEquals(dto.getDate(), reloaded.getDateAsLocalDate());
                    assertEquals(dto.getBeginTime(), reloaded.getBeginTime());
                    assertEquals(dto.getEndTime(), reloaded.getEndTime());
                    assertEquals(dto.getName(), reloaded.getName());
                    assertEquals(dto.getDescription(), reloaded.getDescription());
                });
            });
        }));
    }

    @Test
    public void testUpdateParticipantsInOldEventType() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final Venue venue = model().newVenue();
            final Venue venue2 = model().newVenue();

            final CalendarEvent event =
                    model().newCalendarEvent(rhy, NUORISOTAPAHTUMA, today(), venue);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation change should be ineffective because it is ignored in update
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                anotherRhy,
                                venue2, venue2.getAddress(),
                                false, false,
                                emptyList());

                dto.setParticipants(5);

                feature.update(dto);

                runInTransaction(() -> {
                    final CalendarEvent reloaded = repository.findOne(event.getId());

                    assertEquals(rhy.getId(), reloaded.getOrganisation().getId());
                    assertEquals(venue2.getId(), reloaded.getVenue().getId());

                    assertEquals(dto.getCalendarEventType(), reloaded.getCalendarEventType());
                    assertEquals(dto.getDate(), reloaded.getDateAsLocalDate());
                    assertEquals(dto.getBeginTime(), reloaded.getBeginTime());
                    assertEquals(dto.getEndTime(), reloaded.getEndTime());
                    assertEquals(dto.getName(), reloaded.getName());
                    assertEquals(dto.getDescription(), reloaded.getDescription());
                    assertEquals(dto.getParticipants(), reloaded.getParticipants());
                });
            });
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateToOldEventType() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final Venue venue = model().newVenue();
            final Venue venue2 = model().newVenue();

            final CalendarEvent event =
                    model().newCalendarEvent(rhy, some(nonShootingTestTypes()), today(), venue);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation change should be ineffective because it is ignored in update
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                anotherRhy,
                                venue2, venue2.getAddress(),
                                false, false,
                                emptyList());

                dto.setCalendarEventType(KOULUTUSTILAISUUS);

                feature.update(dto);
            });
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateFromOldEventTypeToOldEventType() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final Venue venue = model().newVenue();
            final Venue venue2 = model().newVenue();

            final CalendarEvent event =
                    model().newCalendarEvent(rhy, NUORISOTAPAHTUMA, today(), venue);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation change should be ineffective because it is ignored in update
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                anotherRhy,
                                venue2, venue2.getAddress(),
                                false, false,
                                emptyList());

                dto.setCalendarEventType(KOULUTUSTILAISUUS);

                feature.update(dto);
            });
        }));
    }

    @Test
    public void testUpdateFromOldEventTypeToNewEventType() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final Venue venue = model().newVenue();
            final Venue venue2 = model().newVenue();

            final CalendarEvent event =
                    model().newCalendarEvent(rhy, NUORISOTAPAHTUMA, today(), venue);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation change should be ineffective because it is ignored in update
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                anotherRhy,
                                venue2, venue2.getAddress(),
                                false, false,
                                emptyList());

                dto.setCalendarEventType(some(CalendarEventType.nonShootingTestTypes()));

                feature.update(dto);
            });
        }));
    }

    @Test
    public void testUpdateAndAddAdditionalEvents() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Venue venue = model().newVenue();
            final CalendarEvent event = model().newCalendarEvent(rhy, some(nonShootingTestTypes()), today(), venue);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                rhy,
                                venue, venue.getAddress(),
                                false, false,
                                emptyList());

                final LocalDate date = today().plusDays(1);
                final LocalTime beginTime = new LocalTime(18, 0);
                final LocalTime endTime = new LocalTime(18, 30);
                final AdditionalCalendarEventDTO additionalCalendarEventDTO =
                        createAdditionalCalendarEventDTO(venue, date, beginTime, endTime);

                dto.setAdditionalCalendarEvents(Arrays.asList(additionalCalendarEventDTO));

                feature.update(dto);

                runInTransaction(() -> {
                    final CalendarEvent updatedEvent = repository.findOne(event.getId());

                    List<AdditionalCalendarEvent> reloadedAdditionalEvents =
                            additionalCalendarEventRepository.findByCalendarEvent(updatedEvent);

                    assertNotNull(reloadedAdditionalEvents);
                    assertEquals(1, reloadedAdditionalEvents.size());

                    AdditionalCalendarEvent reloadedAdditionalEvent = reloadedAdditionalEvents.get(0);
                    assertEquals(date, reloadedAdditionalEvent.getDateAsLocalDate());
                    assertEquals(beginTime, reloadedAdditionalEvent.getBeginTime());
                    assertEquals(endTime, reloadedAdditionalEvent.getEndTime());
                    assertEquals(venue, reloadedAdditionalEvent.getVenue());
                });
            });
        });
    }

    @Test
    public void testUpdateOnlyAdditionalEvents() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Venue venue = model().newVenue();

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final CalendarEventDTO calendarEventDTO = createDTO(rhy, venue, false);

                final LocalDate date = today().plusDays(1);
                final LocalTime beginTime = new LocalTime(18, 0);
                final LocalTime endTime = new LocalTime(18, 30);
                final AdditionalCalendarEventDTO additionalCalendarEventDTO =
                        createAdditionalCalendarEventDTO(venue, date, beginTime, endTime);

                calendarEventDTO.setAdditionalCalendarEvents(Arrays.asList(additionalCalendarEventDTO));

                final CalendarEventDTO outputDTO = feature.create(calendarEventDTO);

                outputDTO.getAdditionalCalendarEvents().get(0).setDate(today().plusDays(19));
                CalendarEventDTO updatedDTO = feature.update(outputDTO);
                assertEquals(1, updatedDTO.getRev().longValue());

                runInTransaction(() -> {
                    final CalendarEvent event = repository.findOne(updatedDTO.getId());
                    assertEquals(1, event.getConsistencyVersion().intValue());
                });
            });
        });
    }

    @Test
    public void testUpdateForPastCalendarEvent() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {

            final CalendarEvent event = model().newCalendarEvent(rhy, true, today().minusDays(1));
            final Venue venue = event.getVenue();
            final Venue venue2 = model().newVenue();

            model().newShootingTestEvent(event);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                // Organisation and venue changes should be ineffective because them are ignored in update.
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                anotherRhy,
                                venue2, venue2.getAddress(),
                                true, false,
                                emptyList());

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

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateToTwoYearsPastAsCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final CalendarEvent event = model().newCalendarEvent(rhy, false, today());

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final Venue venue = model().newVenue();

                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                rhy,
                                venue, venue.getAddress(),
                                false, false,
                                emptyList());
                dto.setDate(today().minusYears(2));
                feature.update(dto);
            });
        });
    }

    @Test
    public void testUpdateToTwoYearsPastAsModerator() {
        withRhy(rhy -> {
            final CalendarEvent event = model().newCalendarEvent(rhy, false, today());
            final Venue venue = model().newVenue();

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final CalendarEventDTO dto =
                        CalendarEventDTO.create(event,
                                rhy,
                                venue, venue.getAddress(),
                                false, false,
                                emptyList());
                dto.setDate(event.getDateAsLocalDate().minusYears(2));
                feature.update(dto);

                runInTransaction(() -> {
                    final CalendarEvent reloaded = repository.findOne(event.getId());

                    assertEquals(dto.getDate(), reloaded.getDateAsLocalDate());

                    assertEquals(rhy.getId(), reloaded.getOrganisation().getId());
                    assertEquals(venue.getId(), reloaded.getVenue().getId());
                    assertEquals(event.getCalendarEventType(), reloaded.getCalendarEventType());
                    assertEquals(event.getBeginTime(), reloaded.getBeginTime());
                    assertEquals(event.getEndTime(), reloaded.getEndTime());
                    assertEquals(event.getName(), reloaded.getName());
                    assertEquals(event.getDescription(), reloaded.getDescription());
                });
            });
        });
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
        final LocalDate today = today();

        withRhy(rhy -> {

            final CalendarEvent event = model().newCalendarEvent(rhy, true, today.plusDays(14));
            final Venue venue = event.getVenue();

            onSavedAndAuthenticated(createNewModerator(), () -> {

                final CalendarEventDTO eventDTO =
                        CalendarEventDTO.create(event,
                                rhy,
                                venue, venue.getAddress(),
                                dayOffset >= 0, false,
                                emptyList());
                event.setDate(today.plusDays(dayOffset).toDate());

                feature.update(eventDTO);
            });
        });
    }

    @Test
    public void testDeleteWithAdditionalEvents() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Venue venue1 = model().newVenue();
            final Venue venue2 = model().newVenue();
            final Venue venue3 = model().newVenue();

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final CalendarEventDTO calendarEventDTO = createDTO(rhy, venue1, false);

                final LocalDate date1 = today().plusDays(1);
                final LocalTime beginTime1 = new LocalTime(18, 0);
                final LocalTime endTime1 = new LocalTime(18, 30);
                final AdditionalCalendarEventDTO additionalCalendarEventDTO1 =
                        createAdditionalCalendarEventDTO(venue2, date1, beginTime1, endTime1);

                final LocalDate date2 = today().plusDays(2);
                final LocalTime beginTime2 = new LocalTime(10, 0);
                final LocalTime endTime2 = new LocalTime(11, 30);
                final AdditionalCalendarEventDTO additionalCalendarEventDTO2 =
                        createAdditionalCalendarEventDTO(venue3, date2, beginTime2, endTime2);

                List<AdditionalCalendarEventDTO> additionalEventDTOs =
                        Arrays.asList(additionalCalendarEventDTO1, additionalCalendarEventDTO2);
                calendarEventDTO.setAdditionalCalendarEvents(additionalEventDTOs);

                final CalendarEventDTO outputDTO = feature.create(calendarEventDTO);

                runInTransaction(() -> {
                    CalendarEvent event = repository.findOne(outputDTO.getId());
                    assertEquals(2, event.getAdditionalCalendarEvents().size());

                    feature.delete(event);

                    List<AdditionalCalendarEvent> additionalEvents = additionalCalendarEventRepository.findAll();
                    assertEquals(0, additionalEvents.size());
                });
            });
        });
    }
}
