package fi.riista.feature.organization.rhy.huntingcontrolevent;

import static fi.riista.feature.organization.rhy.huntingcontrolevent.ChangeHistory.ChangeType.ADD_ATTACHMENTS;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.ChangeHistory.ChangeType.CREATE;
import static fi.riista.util.DateUtil.currentYear;
import static fi.riista.util.DateUtil.today;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import com.google.common.collect.Sets;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Resource;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;


public class HuntingControlEventCrudFeatureTest extends EmbeddedDatabaseTest {
    final int MAX_QUERIES = 8;

    @Resource
    private HuntingControlEventCrudFeature feature;

    @Resource
    private HuntingControlEventRepository eventRepository;

    @Resource
    private HuntingControlAttachmentRepository attachmentRepository;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static void assertEvent(final HuntingControlEvent event,
                                    final HuntingControlEventDTO dto,
                                    final Riistanhoitoyhdistys rhy) {
        assertThat(event.getRhy().getId(), equalTo(rhy.getId()));
        assertThat(event.getEventType(), equalTo(dto.getEventType()));
        assertThat(event.getTitle(), is(nullValue()));
        assertThat(event.getInspectorCount(), equalTo(dto.getInspectorCount()));
        assertThat(event.getCooperationTypes(), equalTo(dto.getCooperationTypes()));
        assertThat(event.getWolfTerritory(), equalTo(dto.getWolfTerritory()));
        assertThat(event.getOtherParticipants(), equalTo(dto.getOtherParticipants()));
        assertThat(event.getGeoLocation(), equalTo(dto.getGeoLocation()));
        assertThat(event.getDate(), equalTo(dto.getDate()));
        assertThat(event.getBeginTime(), equalTo(dto.getBeginTime()));
        assertThat(event.getEndTime(), equalTo(dto.getEndTime()));
        assertThat(event.getCustomers(), equalTo(dto.getCustomers()));
        assertThat(event.getProofOrders(), equalTo(dto.getProofOrders()));
        assertThat(event.getDescription(), equalTo(dto.getDescription()));
        assertThat(event.getLocationDescription(), equalTo(dto.getLocationDescription()));
    }

    private void assertChangeHistory(final List<HuntingControlEventChange> actual,
                                     final List<ChangeHistory.ChangeType> expectedTypes,
                                     final List<String> expectedReasons) {
        assertThat(actual, hasSize(expectedTypes.size()));
        assertThat("Invalid test parameters", expectedTypes, hasSize(expectedReasons.size()));

        final List<ChangeHistory.ChangeType> actualTypes = actual.stream()
                .sorted(comparing(HasID::getId))
                .map(HuntingControlEventChange::getChangeHistory)
                .map(ChangeHistory::getChangeType)
                .collect(toList());
        final List<String> actualReasons = actual.stream()
                .sorted(comparing(HasID::getId))
                .map(HuntingControlEventChange::getChangeHistory)
                .map(ChangeHistory::getReasonForChange)
                .collect(toList());

        assertThat(actualTypes, equalTo(expectedTypes));
        assertThat(actualReasons, equalTo(expectedReasons));
    }


    private HuntingControlEventDTO createDTO(final Riistanhoitoyhdistys rhy) {
        return createDTO(rhy, today(), new LocalTime(12, 0), new LocalTime(13, 0));
    }

    private HuntingControlEventDTO createDTO(final Riistanhoitoyhdistys rhy,
                                             final LocalDate date) {
        return createDTO(rhy, date, new LocalTime(12, 0), new LocalTime(13, 0));
    }

    private HuntingControlEventDTO createDTO(final Riistanhoitoyhdistys rhy,
                                             final LocalDate date,
                                             final LocalTime beginTime,
                                             final LocalTime endTime) {
        final HuntingControlEventDTO dto = new HuntingControlEventDTO();
        final Person gameWarden = getEntitySupplier().newPerson(rhy);
        getEntitySupplier().newOccupation(rhy, gameWarden, OccupationType.METSASTYKSENVALVOJA);

        dto.setRhy(RiistanhoitoyhdistysDTO.create(rhy));

        dto.setTitle("Title");
        dto.setEventType(HuntingControlEventType.MOOSELIKE_HUNTING_CONTROL);
        dto.setInspectors(Arrays.asList(HuntingControlInspectorDTO.create(gameWarden)));
        dto.setInspectorCount(1);
        dto.setCooperationTypes(Sets.newHashSet(HuntingControlCooperationType.POLIISI));
        dto.setWolfTerritory(true);
        dto.setOtherParticipants("Inspectors");
        dto.setGeoLocation(geoLocation());
        dto.setDate(date);
        dto.setBeginTime(beginTime);
        dto.setEndTime(endTime);
        dto.setCustomers(1);
        dto.setProofOrders(1);
        dto.setDescription("Description");
        dto.setLocationDescription("LocationDescription");

        return dto;
    }

    @Test
    public void testCreate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final HuntingControlEventDTO inputDTO = createDTO(rhy);
                final HuntingControlEventDTO outputDTO = feature.create(inputDTO);

                runInTransaction(() -> {
                    final HuntingControlEvent created = eventRepository.getOne(outputDTO.getId());
                    assertThat(created, is(notNullValue()));
                    assertEvent(created, inputDTO, rhy);
                    assertChangeHistory(created.getChangeHistory(),
                            Arrays.asList(CREATE),
                            Stream.of((String) null).collect(toList())); // null is no no in Arrays.asList
                });
            });
        });
    }

    @Test(expected = RhyEventTimeException.class)
    public void testCreateTwoYearsPastAsCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final HuntingControlEventDTO inputDTO = createDTO(rhy, today().minusYears(2));
                feature.create(inputDTO);
                fail("Should have thrown RhyEventTimeException");
            });
        });
    }

    @Test
    public void testCreateTwoYearsPastAsModerator() {
        withRhy(rhy -> {
            onSavedAndAuthenticated(createNewModerator(), () -> {
                final HuntingControlEventDTO inputDTO = createDTO(rhy, today().minusYears(2));
                final HuntingControlEventDTO outputDTO = feature.create(inputDTO);

                runInTransaction(() -> {
                    final HuntingControlEvent created = eventRepository.getOne(outputDTO.getId());
                    assertThat(created, is(notNullValue()));

                    assertEvent(created, inputDTO, rhy);
                });
            });
        });
    }

    @Test(expected = RhyEventTimeException.class)
    public void testCreateBeginTimeAfterEndTime() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final HuntingControlEventDTO inputDTO = createDTO(rhy, today(), new LocalTime(13, 0), new LocalTime(12, 0));
                feature.create(inputDTO);
                fail("Should have thrown RhyEventTimeException");
            });
        });
    }

    @Test
    public void testCreateWithAttachment() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final HuntingControlEventDTO inputDTO = createDTO(rhy);

                final byte[] attachmentData = new byte[4096];
                new Random().nextBytes(attachmentData);
                final MultipartFile attachment = new MockMultipartFile("test.png", "//test/test.png", "image/png", attachmentData);

                inputDTO.setNewAttachments(Arrays.asList(attachment));

                final HuntingControlEventDTO outputDTO = feature.create(inputDTO);

                runInTransaction(() -> {
                    final HuntingControlEvent created = eventRepository.getOne(outputDTO.getId());
                    assertThat(created, is(notNullValue()));
                    assertThat(created.getAttachments(), hasSize(1));
                    assertThat(created.getAttachments().get(0).getAttachmentMetadata().getContentSize(), equalTo(4096L));
                    assertChangeHistory(created.getChangeHistory(),
                            Arrays.asList(CREATE, ADD_ATTACHMENTS),
                            Stream.of(null, "test.png").collect(toList()));

                });
            });
        });
    }

    @Test
    public void testDeleteEventWithAttachment() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy);
            final HuntingControlAttachment attachment = model().newHuntingControlAttachment(event, model().newPersistentFileMetadata());

            try {
                addFileToMetadata(attachment, "temp.txt");
            } catch (IOException e) {
                fail("Failed to create attachment");
            }

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final long eventId = event.getId();
                final long attachmentId = attachment.getId();
                feature.delete(event.getId());

                runInTransaction(() -> {
                    assertThat(eventRepository.findById(eventId).isPresent(), is(false));
                    assertThat(attachmentRepository.findById(attachmentId).isPresent(), is(false));
                });
            });
        });
    }

    @Test
    public void testUpdate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final HuntingControlEventDTO dto = HuntingControlEventDTO.create(
                        event,
                        rhy,
                        event.getInspectors(),
                        event.getCooperationTypes(),
                        Collections.emptyList(),
                        Collections.emptyList());

                dto.setTitle(dto.getTitle() + "_modified");
                dto.setInspectorCount(dto.getInspectorCount() + 1);
                dto.setCooperationTypes(Sets.newHashSet(HuntingControlCooperationType.RAJAVARTIOSTO));
                dto.setWolfTerritory(!dto.getWolfTerritory());
                dto.setOtherParticipants(dto.getOtherParticipants() + "_mutated");
                dto.setDate(dto.getDate().minusDays(1));
                dto.setBeginTime(dto.getBeginTime().plusHours(1));
                dto.setEndTime(dto.getEndTime().plusHours(1));
                dto.setCustomers(dto.getCustomers() + 1);
                dto.setProofOrders(dto.getProofOrders() + 1);
                dto.setDescription(dto.getDescription() + "_mutated");

                feature.update(dto);

                runInTransaction(() -> {
                    final HuntingControlEvent updated = eventRepository.getOne(dto.getId());
                    assertThat(updated, is(notNullValue()));

                    assertEvent(updated, dto, rhy);
                });
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testListHuntingControlEvents_asGameWardenFails() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                feature.listHuntingControlEvents(rhy.getId(), currentYear());
            });
        });
    }

    @Test
    public void testListHuntingControlEvents_asCoordinatorSucceeds() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                feature.listHuntingControlEvents(rhy.getId(), currentYear());
            });
        });
    }

    @Test
    public void testListHuntingControlEvents_returnOnlyGivenRhyEvents() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            withRhyAndCoordinator((otherRhy, otherCoordinator) -> {
                final HuntingControlEvent event = model().newHuntingControlEvent(rhy);
                model().newHuntingControlEvent(otherRhy); // Other event

                onSavedAndAuthenticated(createUser(coordinator), () -> {
                    final List<HuntingControlEventDTO> events = feature.listHuntingControlEvents(rhy.getId(), currentYear());
                    assertThat(events, hasSize(1));
                    assertThat(events.get(0).getId(), equalTo(event.getId()));
                });
            });
        });
    }

    @Test
    public void testListHuntingControlEvents_returnOnlyGivenYearEvents() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy);
            final HuntingControlEvent oldEvent = model().newHuntingControlEvent(rhy);
            oldEvent.setDate(oldEvent.getDate().minusYears(1));
            final HuntingControlEvent futureEvent = model().newHuntingControlEvent(rhy);
            futureEvent.setDate(futureEvent.getDate().plusYears(1));

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final List<HuntingControlEventDTO> events = feature.listHuntingControlEvents(rhy.getId(), currentYear());
                assertThat(events, hasSize(1));
                assertThat(events.get(0).getId(), equalTo(event.getId()));
            });
        });
    }


    @Test
    public void testListHuntingControlEvents_amountOfQueriesIsReasonable() {
        final int NUM_ENTITIES = 100;

        withRhyAndCoordinator((rhy, coordinator) -> {
            IntStream.range(0, NUM_ENTITIES).forEach(i -> model().newHuntingControlEvent(rhy));

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                assertMaxQueryCount(MAX_QUERIES, () -> {
                    final List<HuntingControlEventDTO> actual = feature.listHuntingControlEvents(rhy.getId(), currentYear());
                    assertThat(actual, hasSize(NUM_ENTITIES));
                });
            });
        });
    }

    @Test
    public void testListHuntingControlEventsForActiveUser_amountOfQueriesIsReasonable() {
        final int NUM_ENTITIES = 100;

        withRhyAndCoordinator((rhy, coordinator) -> {
            IntStream.range(0, NUM_ENTITIES).forEach(i -> model().newHuntingControlEvent(rhy, coordinator));

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                assertMaxQueryCount(MAX_QUERIES, () -> {
                    final List<HuntingControlEventDTO> actual = feature.listHuntingControlEventsForActiveUser(rhy.getId(), currentYear());
                    assertThat(actual, hasSize(NUM_ENTITIES));
                });

            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testListAvailableYears_asGameWardenFails() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                feature.listAvailableYears(rhy.getId());
            });
        });
    }

    @Test
    public void testListAvailableYears_asCoordinatorSucceeds() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                feature.listAvailableYears(rhy.getId());
            });
        });
    }

    @Test
    public void testListAvailableYears_returnOnlyGivenRhyYears() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            withRhyAndCoordinator((otherRhy, otherCoordinator) -> {
                final HuntingControlEvent event = model().newHuntingControlEvent(rhy);
                final HuntingControlEvent otherEvent = model().newHuntingControlEvent(otherRhy);
                otherEvent.setDate(otherEvent.getDate().minusYears(1));

                onSavedAndAuthenticated(createUser(coordinator), () -> {
                    final List<Integer> events = feature.listAvailableYears(rhy.getId());
                    assertThat(events, hasSize(1));
                    assertThat(events.get(0), equalTo(event.getDate().getYear()));
                });
            });
        });
    }

    @Test
    public void testListAllActiveGameWardens_asGameWardenSucceeds() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                feature.listAllActiveGameWardens(rhy.getId(), today());
            });
        });
    }

    @Test
    public void testListAllActiveGameWardens_asCoordinatorSucceeds() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                feature.listAllActiveGameWardens(rhy.getId(), today());
            });
        });
    }

    @Test
    public void testListAllActiveGameWardens_listOnlyGivenRhyGameWardens() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            withRhyAndGameWarden((otherRhy, otherGameWarden) -> {
                onSavedAndAuthenticated(createUser(gameWarden), () -> {
                    final ActiveGameWardensDTO gameWardens = feature.listAllActiveGameWardens(rhy.getId(), today());
                    assertThat(gameWardens, is(notNullValue()));
                    assertThat(gameWardens.isActiveNomination(), is(true));
                    assertThat(gameWardens.getGameWardens(), hasSize(1));
                    assertEquals(gameWardens.getGameWardens().get(0), gameWarden);
                });
            });
        });
    }

    @Test
    public void testListAllActiveGameWardens_listOnlyActiveGameWardens() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final Person current = newGameWarden(rhy, today(), today());
            newGameWarden(rhy, null, today().minusDays(1)); // past
            newGameWarden(rhy, today().plusDays(1), null); // future

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final ActiveGameWardensDTO gameWardens = feature.listAllActiveGameWardens(rhy.getId(), today());
                assertThat(gameWardens, is(notNullValue()));
                assertThat(gameWardens.isActiveNomination(), is(true));
                assertThat(gameWardens.getGameWardens(), hasSize(1));
                assertEquals(gameWardens.getGameWardens().get(0), current);
            });
        });
    }

    @Test
    public void testListAllActiveGameWardens_asGameWarden_noListIfNotActiveOnRequestedDate() {
        withRhyAndGameWardenOccupation((rhy, gameWardenOccupation) -> {
            gameWardenOccupation.setBeginDate(today());
            onSavedAndAuthenticated(createUser(gameWardenOccupation.getPerson()), () -> {
                final ActiveGameWardensDTO gameWardens = feature.listAllActiveGameWardens(rhy.getId(), today().minusDays(1));
                assertThat(gameWardens, is(notNullValue()));
                assertThat(gameWardens.isActiveNomination(), is(false));
                assertThat(gameWardens.getGameWardens(), is(nullValue()));
            });
        });
    }

    @Test
    public void testListAllActiveGameWardens_asCoordinator_listIfNotActiveOnRequestedDate() {
        withRhyAndCoordinatorOccupation((rhy, coordinatorOccupation) -> {
            coordinatorOccupation.setBeginDate(today());

            final Person gameWarden = newGameWarden(rhy, null, null);
            onSavedAndAuthenticated(createUser(coordinatorOccupation.getPerson()), () -> {
                final ActiveGameWardensDTO gameWardens = feature.listAllActiveGameWardens(rhy.getId(), today().minusDays(1));
                assertThat(gameWardens, is(notNullValue()));
                assertThat(gameWardens.isActiveNomination(), is(true));
                assertThat(gameWardens.getGameWardens(), hasSize(1));
                assertEquals(gameWardens.getGameWardens().get(0), gameWarden);
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testChangeStatus_asGameWardenFails() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy);
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                feature.changeStatus(event.getId(), HuntingControlEventStatus.ACCEPTED);
            });
        });
    }

    @Test
    public void testChangeStatus_asCoordinatorSucceeds() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy);
            event.setStatus(HuntingControlEventStatus.PROPOSED);
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final HuntingControlEventDTO actual = feature.changeStatus(event.getId(), HuntingControlEventStatus.ACCEPTED);
                assertThat(actual.getStatus(), equalTo(HuntingControlEventStatus.ACCEPTED));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testChangeStatus_canChangeOnlyOwnRhyEvent() {
        withRhyAndCoordinator((otherRhy, otherCoordinator) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(otherRhy);

            withRhyAndCoordinator((rhy, coordinator) -> {
                onSavedAndAuthenticated(createUser(coordinator), () -> {
                    feature.changeStatus(event.getId(), HuntingControlEventStatus.ACCEPTED);
                });
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testListHuntingControlEventsForActiveUser_canListOnlyOwnRhyUsers() {
        withRhyAndGameWarden((rhy, activeUser) -> {
            withRhyAndGameWarden((otherRhy, otherGameWarden) -> {
                onSavedAndAuthenticated(createUser(activeUser), () -> {
                    feature.listHuntingControlEventsForActiveUser(otherRhy.getId(), currentYear());
                });
            });
        });
    }

    @Test
    public void testListHuntingControlEventsForActiveUser() {
        withRhyAndGameWarden((rhy, activeUser) -> {
            final Person otherGameWarden = newGameWarden(rhy, null, null);
            // activeUser's event
            final HuntingControlEvent expected = model().newHuntingControlEvent(rhy, otherGameWarden);
            expected.setInspectors(Sets.newHashSet(otherGameWarden, activeUser));
            // Other event
            model().newHuntingControlEvent(rhy, otherGameWarden);

            onSavedAndAuthenticated(createUser(activeUser), () -> {
                final List<HuntingControlEventDTO> events = feature.listHuntingControlEventsForActiveUser(rhy.getId(), currentYear());
                assertThat(events, hasSize(1));
                final HuntingControlEventDTO actual = events.get(0);
                assertThat(actual.getId(), equalTo(expected.getId()));
                assertThat(actual.getInspectors(), hasSize(2));
                assertThat(actual.getInspectors().get(0).getId(), isOneOf(activeUser.getId(), otherGameWarden.getId()));
                assertThat(actual.getInspectors().get(1).getId(), isOneOf(activeUser.getId(), otherGameWarden.getId()));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testListAvailableYearsForActiveUser_canListOnlyOwnRhyYears() {
        withRhyAndGameWarden((rhy, activeUser) -> {
            withRhyAndGameWarden((otherRhy, otherGameWarden) -> {
                onSavedAndAuthenticated(createUser(activeUser), () -> {
                    feature.listAvailableYearsForActiveUser(otherRhy.getId());
                });
            });
        });
    }

    @Test
    public void testListAvailableYearsForActiveUser() {
        withRhyAndGameWarden((rhy, activeUser) -> {
            final Person otherGameWarden = newGameWarden(rhy, null, null);
            // activeUser's event
            final HuntingControlEvent expected = model().newHuntingControlEvent(rhy, otherGameWarden);
            expected.setInspectors(Sets.newHashSet(otherGameWarden, activeUser));
            // Other event
            final HuntingControlEvent otherEvent = model().newHuntingControlEvent(rhy, otherGameWarden);
            otherEvent.setDate(otherEvent.getDate().minusYears(1));

            onSavedAndAuthenticated(createUser(activeUser), () -> {
                final List<Integer> years = feature.listAvailableYearsForActiveUser(rhy.getId());
                assertThat(years, hasSize(1));
                assertThat(years.get(0), equalTo(expected.getDate().getYear()));
            });
        });
    }


    @Test
    public void testSearchModeratorAsAdminSucceeds() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.searchModerator(new HuntingControlEventSearchParametersDTO()));
    }

    @Test(expected = AccessDeniedException.class)
    public void testSearchModeratorWithoutPermissionThrowsAccessDeniedException() {
        onSavedAndAuthenticated(createNewModerator(), () -> feature.searchModerator(new HuntingControlEventSearchParametersDTO()));
    }

    @Test
    public void testSearchModerator() {
        withRhy(rhy -> withRhy(rhy2 -> {
            for (final HuntingControlEventType type : HuntingControlEventType.values()) {
                for (final HuntingControlEventStatus status : HuntingControlEventStatus.values()) {
                    for (final HuntingControlCooperationType coop : HuntingControlCooperationType.values()) {
                        final HuntingControlEvent event = model().newHuntingControlEvent(rhy);
                        event.setEventType(type);
                        event.setStatus(status);
                        event.setCooperationTypes(Sets.newHashSet(coop));

                        final HuntingControlEvent event2 = model().newHuntingControlEvent(rhy2);
                        event2.setEventType(type);
                        event2.setStatus(status);
                        event2.setCooperationTypes(Sets.newHashSet(HuntingControlCooperationType.values()));
                    }
                }
            }
            onSavedAndAuthenticated(createNewModerator(SystemUserPrivilege.VIEW_HUNTING_CONTROL_EVENTS), () -> {
                final int expectedCountOfAllEvents = HuntingControlEventType.values().length
                        * HuntingControlEventStatus.values().length
                        * HuntingControlCooperationType.values().length * 2;

                final HuntingControlEventSearchParametersDTO params = new HuntingControlEventSearchParametersDTO();
                assertThat(feature.searchModerator(params), is(empty()));

                // search all
                params.setTypes(Sets.newHashSet(HuntingControlEventType.values()));
                params.setStatuses(Sets.newHashSet(HuntingControlEventStatus.values()));
                params.setCooperationTypes(Sets.newHashSet(HuntingControlCooperationType.values()));
                assertThat(feature.searchModerator(params), hasSize(expectedCountOfAllEvents));

                // search some
                params.setTypes(Sets.newHashSet(HuntingControlEventType.GROUSE_HUNTING_CONTROL));
                params.setStatuses(Sets.newHashSet(HuntingControlEventStatus.ACCEPTED));
                params.setCooperationTypes(Sets.newHashSet(HuntingControlCooperationType.POLIISI, HuntingControlCooperationType.RAJAVARTIOSTO));
                assertThat(feature.searchModerator(params), hasSize(6));

                // search by existing rka
                params.setTypes(Sets.newHashSet(HuntingControlEventType.values()));
                params.setStatuses(Sets.newHashSet(HuntingControlEventStatus.values()));
                params.setCooperationTypes(Sets.newHashSet(HuntingControlCooperationType.values()));
                params.setOrgType(OrganisationType.RKA);
                params.setOrgCode(rhy.getParentOrganisation().getOfficialCode());
                final List<HuntingControlEventDTO> byRka1 = feature.searchModerator(params);
                assertThat(byRka1, hasSize(expectedCountOfAllEvents / 2));

                // ensure that by rka search returns different events
                params.setOrgType(OrganisationType.RKA);
                params.setOrgCode(rhy2.getParentOrganisation().getOfficialCode());
                final List<HuntingControlEventDTO> byRka2 = feature.searchModerator(params);
                assertThat(Sets.intersection(F.getUniqueIds(byRka1), F.getUniqueIds(byRka2)), is(empty()));

                // search by existing rhy
                params.setOrgType(OrganisationType.RHY);
                params.setOrgCode(rhy.getOfficialCode());
                assertThat(feature.searchModerator(params), hasSize(expectedCountOfAllEvents / 2));


                // search by non-existing rhy
                params.setOrgType(OrganisationType.RHY);
                params.setOrgCode("000");
                assertThat(feature.searchModerator(params), hasSize(0));
            });
        }));
    }

    private Person newGameWarden(final Riistanhoitoyhdistys rhy, final LocalDate beginDate, final LocalDate endDate) {
        final Person gameWarden = model().newPerson(rhy);
        model().newOccupation(rhy, gameWarden, OccupationType.METSASTYKSENVALVOJA, beginDate, endDate);
        return gameWarden;
    }

    private void assertEquals(final HuntingControlInspectorDTO actual, final Person expected) {
        assertThat(actual.getId(), equalTo(expected.getId()));
        assertThat(actual.getFirstName(), equalTo(expected.getFirstName()));
        assertThat(actual.getLastName(), equalTo(expected.getLastName()));

    }

    private void addFileToMetadata(final HuntingControlAttachment attachment, final String fileName) throws IOException {
        final File file = folder.newFile(fileName);
        final PersistentFileMetadata attachmentMetadata = attachment.getAttachmentMetadata();
        attachmentMetadata.setResourceUrl(file.toURI().toURL());
        attachmentMetadata.setOriginalFilename(fileName);
    }
}
