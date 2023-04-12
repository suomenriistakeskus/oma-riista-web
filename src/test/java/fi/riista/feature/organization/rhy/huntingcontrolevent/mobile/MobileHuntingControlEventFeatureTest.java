package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import com.google.common.collect.Sets;
import fi.riista.feature.account.AccountShootingTestService;
import fi.riista.feature.account.mobile.MobileOrganisationDTO;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.huntingcontrolevent.ChangeHistory;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlAttachmentRepository;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlCooperationType;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventRepository;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventType;
import fi.riista.feature.shootingtest.ShootingTestAttempt;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Rule;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;

import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestType.BEAR;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
import static fi.riista.test.TestUtils.ld;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class MobileHuntingControlEventFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileHuntingControlEventFeature feature;

    @Resource
    private HuntingControlEventRepository eventRepository;

    @Resource
    private HuntingControlAttachmentRepository attachmentRepository;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static LocalDateTime DATE_IN_LIMITS = DateUtil.toLocalDateTimeNullSafe(DateUtil.now()).minusYears(1);
    private static LocalDateTime DATE_OVER_LIMITS = DateUtil.toLocalDateTimeNullSafe(DateUtil.now()).minusYears(3);
    private static LocalDateTime SEARCH_TIME = new LocalDateTime(2022, 2, 1, 10, 0);

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Theory
    public void testCreateEvent(final MobileHuntingControlSpecVersion dtoSpecVersion,
                                final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            final SystemUser loggedUser = createUser(gameWarden);

            onSavedAndAuthenticated(loggedUser, () -> {
                final MobileHuntingControlEventDTO dtoIn = createDto(dtoSpecVersion);
                final MobileHuntingControlEventDTO dtoOut = feature.createEvent(rhy.getId(), dtoIn, requestedSpecVersion);

                runInTransaction(() -> {
                    final HuntingControlEvent created = eventRepository.getOne(dtoOut.getId());
                    assertEvent(created, dtoIn, rhy);

                    assertThat(dtoOut.getId(), is(notNullValue()));
                    assertThat(dtoOut.getRev(), equalTo(0));
                    assertThat(dtoOut.getSpecVersion(), equalTo(requestedSpecVersion));
                    assertThat(dtoOut.getInspectors(), hasSize(1)); // If no inspectors, the creator is added as one
                    assertThat(dtoOut.getInspectors().get(0).getId(), equalTo(gameWarden.getId()));

                    final ChangeHistory change = created.getChangeHistory().get(0).getChangeHistory();
                    assertThat(change.getReasonForChange(), is(nullValue()));
                    assertThat(change.getUserId(), equalTo(loggedUser.getId()));
                    assertThat(change.getChangeType(), equalTo(ChangeHistory.ChangeType.CREATE));
                });
            });
        });
    }

    @Theory
    public void testUpdateEvent(final MobileHuntingControlSpecVersion dtoSpecVersion,
                                final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy, gameWarden);
            final SystemUser loggedUser = createUser(gameWarden);

            onSavedAndAuthenticated(loggedUser, () -> {
                final MobileHuntingControlEventDTO dtoIn = mutateDto(createDto(event, dtoSpecVersion));

                final MobileHuntingControlEventDTO dtoOut = feature.updateEvent(dtoIn, requestedSpecVersion);

                runInTransaction(() -> {
                    final HuntingControlEvent updated = eventRepository.getOne(dtoOut.getId());
                    assertEvent(updated, dtoIn, rhy);
                    assertNonMutableFields(updated, event);
                    assertThat(updated.getChangeHistory(), hasSize(1));

                    final ChangeHistory change = updated.getChangeHistory().get(0).getChangeHistory();
                    assertThat(change.getUserId(), equalTo(loggedUser.getId()));
                    assertThat(change.getChangeType(), equalTo(ChangeHistory.ChangeType.MODIFY));
                });
            });
        });
    }

    @Theory
    public void testGetEvents_gameWarden_hasRhyData(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, requestedSpecVersion);
                assertThat(events, hasSize(1));
                final MobileHuntingControlRhyDTO rhyEvents = events.get(0);
                assertThat(rhyEvents.getSpecVersion(), equalTo(requestedSpecVersion));
                assertThat(rhyEvents.getGameWardens(), hasSize(1));
                assertThat(rhyEvents.getGameWardens().get(0).getInspector().getId(), equalTo(gameWarden.getId()));
                assertThat(rhyEvents.getEvents(), hasSize(0));
                assertRhy(rhyEvents.getRhy(), rhy);
            });
        });
    }

    @Theory
    public void testGetEvents_gameWarden_nominationInPast_hasNoRhyData(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWardenOccupation((rhy, gameWardenOccupation) -> {
            gameWardenOccupation.setEndDate(DateUtil.today().minusDays(1));
            final Person gameWarden = gameWardenOccupation.getPerson();
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, requestedSpecVersion);
                assertThat(events, hasSize(0));
            });
        });
    }

    @Theory
    public void testGetEvents_gameWarden_nominationLongTimeAgo_hasRhyData(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            model().newOccupation(rhy, model().newPerson(), OccupationType.METSASTYKSENVALVOJA,
                                  DateUtil.today().minusYears(2),
                                  DateUtil.today().plusYears(2));
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, requestedSpecVersion);
                assertThat(events, hasSize(1));
                assertThat(events.get(0).getGameWardens(), hasSize(2));
            });
        });
    }

    @Theory
    public void testGetEvents_gameWarden_inspectorEndDateIsDeletionDateIfPresent(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            final Occupation softDeletedOccupation = model().newOccupation(rhy, model().newPerson(),
                                                                           OccupationType.METSASTYKSENVALVOJA,
                                                                           DateUtil.today().minusYears(2),
                                                                           DateUtil.today().plusYears(2));
            final DateTime deletionTime = DateUtil.now().minusDays(1);
            softDeletedOccupation.getLifecycleFields().setDeletionTime(deletionTime);
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, requestedSpecVersion);
                assertThat(events, hasSize(1));
                final List<MobileGameWardenDTO> gameWardenDTOs = events.get(0).getGameWardens();
                assertThat(gameWardenDTOs, hasSize(2));
                final List<LocalDate> endDates = events.get(0).getGameWardens()
                        .stream()
                        .map(MobileGameWardenDTO::getEndDate)
                        .collect(Collectors.toList());
                assertThat(endDates, containsInAnyOrder(null, deletionTime.toLocalDate()));
            });
        });
    }

    @Theory
    public void testGetEvents_gameWarden_gameWardenListObeysSoftDelete(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWardenOccupation((rhy, gameWardenOccupation) -> {
            // Requester game warden has nomination starting today
            gameWardenOccupation.setBeginDate(DateUtil.today());
            final Person gameWarden = gameWardenOccupation.getPerson();

            // Another game warden has nomination from 2 years ago, lasting 4 years
            // but the nomination has been (soft) deleted yesterday.
            final Occupation softDeletedGameWarden = model().newOccupation(rhy, model().newPerson(),
                                                                           OccupationType.METSASTYKSENVALVOJA,
                                                                           DateUtil.today().minusYears(2),
                                                                           DateUtil.today().plusYears(2));
            softDeletedGameWarden.getLifecycleFields().setDeletionTime(DateUtil.now().minusDays(1));

            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, requestedSpecVersion);
                assertThat(events, hasSize(1));
                assertThat(events.get(0).getGameWardens(), hasSize(1)); // Only requester listed as active game warden
            });
        });
    }

    @Theory
    public void testGetEvents_gameWarden_nominationInFuture_hasNoRhyData(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWardenOccupation((rhy, gameWardenOccupation) -> {
            gameWardenOccupation.setBeginDate(DateUtil.today().plusDays(1));
            final Person gameWarden = gameWardenOccupation.getPerson();
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> events = feature.getEvents(null, requestedSpecVersion);
                assertThat(events, hasSize(0));
            });
        });
    }

    @Theory
    public void testGetEvents_gameWarden_hasEvent(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy, gameWarden);
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> rhys = feature.getEvents(null, requestedSpecVersion);
                assertThat(rhys, hasSize(1));

                assertRhyEvents(rhys.get(0),
                                requestedSpecVersion,
                                singletonList(gameWarden),
                                singletonList(event),
                                rhy);
            });
        });
    }

    @Theory
    public void testGetEvents_onlyModifiedAfterGivenTime(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            // Create two events in the past
            MockTimeProvider.mockTime(SEARCH_TIME.minusSeconds(1).toDate().getTime());
            model().newHuntingControlEvent(rhy, gameWarden); // event not in search
            final HuntingControlEvent eventIn = model().newHuntingControlEvent(rhy, gameWarden);
            persistInNewTransaction();

            // Update one event
            MockTimeProvider.mockTime(SEARCH_TIME.plusSeconds(1).toDate().getTime());
            runInTransaction(() -> {
                eventIn.setCustomers(100);
                eventRepository.saveAndFlush(eventIn);
            });

            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> rhys = feature.getEvents(SEARCH_TIME, requestedSpecVersion);
                assertThat(rhys, hasSize(1));

                assertRhyEvents(rhys.get(0),
                                requestedSpecVersion,
                                singletonList(gameWarden),
                                singletonList(eventIn),
                                rhy);
            });
        });
    }

    @Theory
    public void testGetEvents_onlyCreatedAfterGivenTime(final MobileHuntingControlSpecVersion requestedSpecVersion) {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            // Create one event in the past
            MockTimeProvider.mockTime(SEARCH_TIME.minusSeconds(1).toDate().getTime());
            model().newHuntingControlEvent(rhy, gameWarden); // event not in search
            persistInNewTransaction();

            // Create one event after
            MockTimeProvider.mockTime(SEARCH_TIME.plusSeconds(1).toDate().getTime());
            final HuntingControlEvent eventIn = model().newHuntingControlEvent(rhy, gameWarden);
            persistInNewTransaction();

            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final List<MobileHuntingControlRhyDTO> rhys = feature.getEvents(SEARCH_TIME, requestedSpecVersion);
                assertThat(rhys, hasSize(1));

                assertRhyEvents(rhys.get(0),
                                requestedSpecVersion,
                                singletonList(gameWarden),
                                singletonList(eventIn),
                                rhy);
            });
        });
    }

    @Test
    public void testGetHunterInfoByHunterNumber() {
        mockDate(ld(2019, 12, 27));

        withRhyAndGameWarden((rhy, gameWarden) -> {
            final Person person = model().newPerson();
            person.setHuntingCardStart(ld(2019, 8, 1));
            person.setHuntingCardEnd(ld(2099, 7, 31));
            person.setHunterExamDate(ld(1996, 4, 12));
            person.setHunterExamExpirationDate(ld(2099, 6, 23));
            person.setHuntingPaymentOneDay(ld(2019, 8, 3));
            person.setHuntingPaymentOneYear(2019);
            createNewUser("user1", person);

            final ShootingTestParticipant participant = model().newShootingTestParticipant();
            participant.setPerson(person);
            final ShootingTestAttempt attempt = model().newShootingTestAttempt(participant, QUALIFIED);
            attempt.setType(BEAR);
            participant.updateTotalDueAmount(1);
            participant.updatePaymentState(1, true);

            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final MobileHuntingControlHunterInfoDTO info = feature.getHunterInfoByHunterNumber(person.getHunterNumber());
                assertThat(info.getName(), is(equalTo(person.getFirstName() + " " + person.getLastName())));
                assertThat(info.getDateOfBirth(), is(equalTo(person.parseDateOfBirth())));
                assertThat(info.getHomeMunicipality(), is(equalTo(person.getHomeMunicipalityName().asMap())));
                assertThat(info.getHunterNumber(), is(equalTo(person.getHunterNumber())));
                assertTrue(info.isHuntingCardActive());
                assertThat(info.getHuntingCardDateOfPayment(), is(equalTo(ld(2019, 8, 3))));
                assertThat(info.getShootingTests(), hasSize(1));
            });
        });
    }

    @Test
    public void testGetHunterInfoBySsn() {
        mockDate(ld(2019, 12, 27));

        withRhyAndGameWarden((rhy, gameWarden) -> {
            final Person person = model().newPerson();
            person.setHuntingCardStart(ld(2019, 8, 1));
            person.setHuntingCardEnd(ld(2099, 7, 31));
            person.setHunterExamDate(ld(1996, 4, 12));
            person.setHunterExamExpirationDate(ld(2099, 6, 23));
            person.setHuntingPaymentOneDay(ld(2019, 8, 3));
            person.setHuntingPaymentOneYear(2019);
            createNewUser("user1", person);

            final ShootingTestParticipant participant = model().newShootingTestParticipant();
            participant.setPerson(person);
            final ShootingTestAttempt attempt = model().newShootingTestAttempt(participant, QUALIFIED);
            attempt.setType(BEAR);
            participant.updateTotalDueAmount(1);
            participant.updatePaymentState(1, true);

            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final MobileHuntingControlHunterInfoDTO info = feature.getHunterInfoBySsn(person.getSsn());
                assertThat(info.getName(), is(equalTo(person.getFirstName() + " " + person.getLastName())));
                assertThat(info.getDateOfBirth(), is(equalTo(person.parseDateOfBirth())));
                assertThat(info.getHomeMunicipality(), is(equalTo(person.getHomeMunicipalityName().asMap())));
                assertThat(info.getHunterNumber(), is(equalTo(person.getHunterNumber())));
                assertTrue(info.isHuntingCardActive());
                assertThat(info.getHuntingCardDateOfPayment(), is(equalTo(ld(2019, 8, 3))));
                assertThat(info.getShootingTests(), hasSize(1));
            });
        });
    }

    @Test(expected = PersonNotFoundException.class)
    public void testHunterInfoNotFoundByHunterNumber() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                feature.getHunterInfoByHunterNumber("12345672");
            });
        });
    }

    @Test(expected = PersonNotFoundException.class)
    public void testHunterInfoNotFoundBySsn() {
        withRhyAndGameWarden((rhy, gameWarden) -> {
            onSavedAndAuthenticated(createUser(gameWarden), () -> {
                final Person personNotSaved = model().newPerson();
                feature.getHunterInfoBySsn(personNotSaved.getSsn());
            });
        });
    }

    private void assertRhyEvents(final MobileHuntingControlRhyDTO rhyEvents,
                                 final MobileHuntingControlSpecVersion requestedSpecVersion,
                                 final List<Person> gameWardens,
                                 final List<HuntingControlEvent> events,
                                 final Riistanhoitoyhdistys rhy) {

        final Map<Long, Person> personMap = F.indexById(gameWardens);
        final Map<Long, HuntingControlEvent> eventMap = F.indexById(events);

        assertThat(rhyEvents.getSpecVersion(), equalTo(requestedSpecVersion));
        assertThat(rhyEvents.getGameWardens(), hasSize(gameWardens.size()));
        rhyEvents.getGameWardens().forEach(
                gameWarden -> assertGameWarden(gameWarden, personMap.get(gameWarden.getInspector().getId())));
        assertThat(rhyEvents.getEvents(), hasSize(events.size()));
        assertRhy(rhyEvents.getRhy(), rhy);

        rhyEvents.getEvents().forEach(dto -> {
            assertThat(dto.getSpecVersion(), equalTo(requestedSpecVersion));
            assertEvent(eventMap.get(dto.getId()), dto, rhy);
        });
    }

    private void assertGameWarden(final MobileGameWardenDTO actual, final Person expected) {
        assertThat(actual.getInspector().getId(), equalTo(expected.getId()));
        assertThat(actual.getInspector().getFirstName(), equalTo(expected.getFirstName()));
        assertThat(actual.getInspector().getLastName(), equalTo(expected.getLastName()));
    }

    // Creation helpers -->

    private MobileHuntingControlEventDTO createDto(final MobileHuntingControlSpecVersion dtoSpecVersion) {
        //
        // When there are more specVersions, do separate creators for each.
        //
        final EntitySupplier es = model();
        final MobileHuntingControlEventDTO dto = new MobileHuntingControlEventDTO();
        //dto.setId(null);
        //dto.setRev(null);
        dto.setSpecVersion(dtoSpecVersion);
        dto.setMobileClientRefId(es.nextLong());
        dto.setEventType(HuntingControlEventType.MOOSELIKE_HUNTING_CONTROL);
        //dto.setStatus(null);
        dto.setInspectors(emptyList());
        dto.setCooperationTypes(emptySet());
        dto.setWolfTerritory(false);
        dto.setGeoLocation(es.geoLocation());
        dto.setLocationDescription("");
        dto.setDate(DateUtil.today().minusDays(1));
        dto.setBeginTime(new LocalTime(10, 00));
        dto.setEndTime(new LocalTime(12, 30));
        dto.setCustomers(1);
        dto.setProofOrders(2);
        dto.setDescription("");
        //dto.setChangeHistory(null);
        //dto.setReasonForChange(null);
        //dto.setCanEdit(null);
        return dto;
    }

    private MobileHuntingControlEventDTO createDto(final HuntingControlEvent event,
                                                   final MobileHuntingControlSpecVersion dtoSpecVersion) {
        return MobileHuntingControlEventDTO.create(dtoSpecVersion,
                                                   event,
                                                   event.getInspectors(),
                                                   event.getCooperationTypes(),
                                                   Collections.emptyList(),
                                                   Collections.emptyList());
    }

    private MobileHuntingControlEventDTO mutateDto(final MobileHuntingControlEventDTO dto) {
        // id
        // rev
        // dto.setMobileClientRefId(model().nextLong()); // Should not be changed
        dto.setEventType(HuntingControlEventType.OTHER);
        dto.setStatus(HuntingControlEventStatus.REJECTED); // Should not be changed
        dto.setCooperationTypes(Sets.newHashSet(HuntingControlCooperationType.RAJAVARTIOSTO));
        dto.setWolfTerritory(!dto.isWolfTerritory());
        dto.setOtherParticipants(dto.getOtherParticipants() + "_mutated");
        dto.setGeoLocation(model().geoLocation());
        dto.setLocationDescription(dto.getLocationDescription() + "_mutated");
        dto.setDate(dto.getDate().minusDays(1));
        dto.setBeginTime(dto.getBeginTime().plusHours(1));
        dto.setEndTime(dto.getBeginTime().plusHours(1));
        dto.setCustomers(dto.getCustomers() + 1);
        dto.setProofOrders(dto.getProofOrders() + 1);
        dto.setDescription(dto.getDescription() + "_mutated");
        // attachments
        // changeHistory
        // reasonForChange
        // canEdit
        return dto;
    }

    // Assert helper -->

    private void assertNonMutableFields(final HuntingControlEvent updated, final HuntingControlEvent original) {
        assertThat(updated.getMobileClientRefId(), equalTo(original.getMobileClientRefId()));
        assertThat(updated.getStatus(), equalTo(original.getStatus()));
    }

    private static void assertEvent(final HuntingControlEvent event,
                                    final MobileHuntingControlEventDTO dto,
                                    final Riistanhoitoyhdistys rhy) {
        // id, rev/consistencyVersion
        assertThat(event.getRhy().getId(), equalTo(rhy.getId()));
        assertThat(event.getMobileClientRefId(), equalTo(dto.getMobileClientRefId()));
        assertThat(event.getEventType(), equalTo(dto.getEventType()));
        // status
        assertThat(event.getTitle(), is(nullValue()));
        assertThat(event.getInspectorCount(), equalTo(dto.getInspectors().size()));
        assertThat(event.getCooperationTypes(), equalTo(dto.getCooperationTypes()));
        assertThat(event.getWolfTerritory(), equalTo(dto.isWolfTerritory()));
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

    private void assertRhy(final MobileOrganisationDTO dto, final Riistanhoitoyhdistys rhy) {
        assertThat(dto.getId(), equalTo(rhy.getId()));
        assertThat(dto.getOfficialCode(), equalTo(rhy.getOfficialCode()));
        assertThat(dto.getName().get("fi"), equalTo(rhy.getNameFinnish()));
        assertThat(dto.getName().get("sv"), equalTo(rhy.getNameSwedish()));
    }

    private static void mockDate(final LocalDate localDate) {
        MockTimeProvider.mockTime(DateUtil.toDateNullSafe(localDate).getTime());
    }
}
