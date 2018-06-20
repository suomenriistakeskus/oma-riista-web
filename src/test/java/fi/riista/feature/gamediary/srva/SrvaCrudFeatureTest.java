package fi.riista.feature.gamediary.srva;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.error.RevisionConflictException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.TestUtils;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SrvaCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private SrvaCrudFeature srvaCrudFeature;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Test
    public void testAddSrvaEvent() {
        withPerson(person -> withRhy(rhy -> {
            final GameSpecies species = model().newGameSpecies();

            onSavedAndAuthenticated(createUser(person), () -> {
                int specimenAmount = 1;

                final SrvaEventDTO inputDto = createSrvaEventDTO(some(SrvaEventNameEnum.class),
                        geoLocation(),
                        species,
                        specimenAmount,
                        rhy.getId());

                final SrvaEventDTO outputDto = srvaCrudFeature.createSrvaEvent(inputDto);

                assertNotNull(outputDto);

                runInTransaction(() -> {
                    final SrvaEvent savedSrvaEvent = srvaEventRepository.findOne(outputDto.getId());
                    assertNotNull(savedSrvaEvent);
                    assertEquals(new Integer(0), savedSrvaEvent.getConsistencyVersion());

                    assertEquals(person, savedSrvaEvent.getAuthor());

                    assertEquals(inputDto.getEventName(), savedSrvaEvent.getEventName());
                    assertEquals(inputDto.getEventType(), savedSrvaEvent.getEventType());
                    assertEquals(inputDto.getGeoLocation(), savedSrvaEvent.getGeoLocation());
                    assertEquals(inputDto.getGameSpeciesCode(), Integer.valueOf(savedSrvaEvent.getSpecies().getOfficialCode()));
                    assertEquals(inputDto.getTotalSpecimenAmount(), savedSrvaEvent.getTotalSpecimenAmount());
                    assertEquals(rhy.getOfficialCode(), savedSrvaEvent.getRhy().getOfficialCode());
                    assertEquals(SrvaEventStateEnum.UNFINISHED, savedSrvaEvent.getState());
                });
            });
        }));
    }

    private SrvaEventDTO createSrvaEventDTO(final SrvaEventNameEnum eventName,
                                            final GeoLocation location,
                                            final GameSpecies species,
                                            final int specimenAmount,
                                            final long rhyId) {

        final SrvaEventDTO srvaEventDTO = new SrvaEventDTO();

        srvaEventDTO.setGeoLocation(location);
        srvaEventDTO.setPointOfTime(DateUtil.localDateTime());
        srvaEventDTO.setEventName(eventName);
        srvaEventDTO.setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(eventName)));
        srvaEventDTO.setGameSpeciesCode(species.getOfficialCode());
        srvaEventDTO.setTotalSpecimenAmount(specimenAmount);
        srvaEventDTO.setRhyId(rhyId);

        srvaEventDTO.setMethods(Collections.singletonList(new SrvaMethodDTO(some(SrvaMethodEnum.class), true)));
        srvaEventDTO.setSpecimens(Collections.emptyList());

        return srvaEventDTO;
    }

    @Test
    public void testUpdateSrvaEvent() {
        withPerson(person -> {
            final GameSpecies gameSpecies = model().newGameSpecies();
            final SrvaEvent srvaEvent = model().newSrvaEvent(person, gameSpecies);

            onSavedAndAuthenticated(createUser(person), () -> {
                final SrvaEventDTO updateDto = getSrvaEventDtoForUpdate(srvaEvent);

                final SrvaEventTypeEnum newEventType = someOtherThan(updateDto.getEventType(),
                        SrvaEventTypeEnum.getBySrvaEvent(updateDto.getEventName()));
                updateDto.setEventType(newEventType);

                final SrvaEventDTO outputDto = srvaCrudFeature.updateSrvaEvent(updateDto);

                assertEquals(newEventType, outputDto.getEventType());
                assertEquals(srvaEvent.getId(), outputDto.getId());
                assertEquals(srvaEvent.getConsistencyVersion().longValue() + 1, outputDto.getRev().longValue());
            });
        });
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateSrvaEvent_notFound() {
        final SrvaEvent srvaEvent = model().newSrvaEvent();

        onSavedAndAuthenticated(createNewUser(), () -> {
            srvaEvent.setId(11111L);
            srvaCrudFeature.updateSrvaEvent(getSrvaEventDtoForUpdate(srvaEvent));
        });
    }

    @Test(expected = RevisionConflictException.class)
    public void testUpdateSrvaEvent_revConflict() {
        withPerson(person -> {
            final SrvaEvent srvaEvent = model().newSrvaEvent(person);
            onSavedAndAuthenticated(createUser(person), () -> {
                srvaEvent.setConsistencyVersion(srvaEvent.getConsistencyVersion() - 1);
                srvaCrudFeature.updateSrvaEvent(getSrvaEventDtoForUpdate(srvaEvent));
            });
        });
    }

    @Test(expected = SrvaApprovedException.class)
    public void testUpdateSrvaEvent_alreadyApproved() {
        withPerson(person -> {
            final SrvaEvent srvaEvent = model().newSrvaEvent(person);

            onSavedAndAuthenticated(createUser(person), user -> {
                final SrvaEventDTO updateDto = getSrvaEventDtoForUpdate(srvaEvent);

                // Change manually state to APPROVED so that updateSrvaEvent throws RunTimeException
                srvaEvent.setState(SrvaEventStateEnum.APPROVED);
                srvaEvent.setApproverAsUser(user);
                final SrvaEvent changedEvent = srvaEventRepository.saveAndFlush(srvaEvent);

                updateDto.setRev(changedEvent.getConsistencyVersion());

                srvaCrudFeature.updateSrvaEvent(updateDto).getDescription();
            });
        });
    }

    private static SrvaEventDTO getSrvaEventDtoForUpdate(final SrvaEvent event) {
        final SrvaEventDTO updateDto = SrvaEventDTO.create(event);
        updateDto.setGameSpeciesCode(event.getSpecies().getOfficialCode());
        updateDto.setMethods(Collections.singletonList(new SrvaMethodDTO(SrvaMethodEnum.DOG, true)));
        updateDto.setSpecimens(Collections.emptyList());
        return updateDto;
    }

    @Test
    public void testDeleteSrvaEvent() {
        withPerson(person -> {

            final SrvaEvent srvaEvent = model().newSrvaEvent(person);

            onSavedAndAuthenticated(createUser(person), () -> {
                srvaCrudFeature.deleteSrvaEvent(srvaEvent.getId());

                assertNull(srvaEventRepository.findOne(srvaEvent.getId()));
            });
        });
    }

    @Test(expected = SrvaApprovedException.class)
    public void testDeleteSrvaEvent_alreadyApproved() {
        withPerson(person -> {
            final SystemUser user = createUser(person);
            final SrvaEvent srvaEvent = model().newSrvaEvent(person);
            srvaEvent.setApproverAsPerson(person);
            srvaEvent.setApproverAsUser(user);
            srvaEvent.setState(SrvaEventStateEnum.APPROVED);

            onSavedAndAuthenticated(user, u -> srvaCrudFeature.deleteSrvaEvent(srvaEvent.getId()));
        });
    }

    @Test
    public void testGetSrvaEvent() {
        withPerson(person -> {

            final SrvaEvent srvaEvent = model().newSrvaEvent(person);

            onSavedAndAuthenticated(createUser(person), () -> {
                final SrvaEventDTO outputDto = srvaCrudFeature.getSrvaEvent(srvaEvent.getId());

                assertEquals(srvaEvent.getId(), outputDto.getId());
            });
        });
    }

    @Test
    public void testSearchPage() {
        withPerson(person -> {
            final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

            final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka);
            model().newOccupation(rhy1, person, OccupationType.SRVA_YHTEYSHENKILO);
            person.setRhyMembership(rhy1);
            final List<SrvaEvent> eventsRhy1 = createContentForSearchTest(person, rhy1);

            final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka);
            final List<SrvaEvent> eventsRhy2 = createContentForSearchTest(person, rhy2);
            model().newOccupation(rhy2, person, OccupationType.TOIMINNANOHJAAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                final SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(rhy1, rhy1);
                for (int i = 0; i < eventsRhy1.size(); i++) {
                    final Slice<SrvaEventDTO> slice = srvaCrudFeature.searchPage(searchDTO, new PageRequest(i, 1));
                    assertEquals(1, slice.getNumberOfElements());
                    final boolean shouldHaveNext = i < eventsRhy1.size() - 1;
                    assertEquals(shouldHaveNext ? true : false, slice.hasNext());
                }
                final Slice<SrvaEventDTO> slice = srvaCrudFeature.searchPage(searchDTO, new PageRequest(eventsRhy1.size(), 1));
                assertEquals(0, slice.getNumberOfElements());
                assertFalse(slice.hasNext());
            });
        });
    }

    @Test
    public void testSearchOtherRhy() {
        withPerson(person -> {
            final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

            final Riistanhoitoyhdistys currentRhy = model().newRiistanhoitoyhdistys(rka);
            model().newOccupation(currentRhy, person, OccupationType.SRVA_YHTEYSHENKILO);

            final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka);
            final List<SrvaEvent> eventsRhy2 = createContentForSearchTest(model().newPerson(), rhy2);

            // Some extra data to different rhy
            final Riistanhoitoyhdistys rhy3 = model().newRiistanhoitoyhdistys(rka);
            createContentForSearchTest(model().newPerson(), rhy3);

            onSavedAndAuthenticated(createUser(person), () -> {

                SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(currentRhy, rhy2);
                final List<SrvaEventDTO> search = srvaCrudFeature.search(searchDTO);

                assertTrue(search.stream().allMatch(eventDto -> Objects.equals(eventDto.getEventName(), SrvaEventNameEnum.ACCIDENT)));
                assertEquals(
                        eventsRhy2.stream().filter(srvaEvent -> Objects.equals(srvaEvent.getEventName(), SrvaEventNameEnum.ACCIDENT)).count(),
                        search.size()
                );
            });
        });
    }

    @Test
    public void testSearchOtherRhy_moderatorView() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka);
        createContentForSearchTest(model().newPerson(), rhy2);

        // Some extra data to different rhy
        final Riistanhoitoyhdistys rhy3 = model().newRiistanhoitoyhdistys(rka);
        createContentForSearchTest(model().newPerson(), rhy3);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            final SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(null, rhy2);
            searchDTO.setModeratorView(true);

            final List<SrvaEventDTO> search = srvaCrudFeature.search(searchDTO);
            assertEquals(
                    srvaEventRepository.findAllAsList(QSrvaEvent.srvaEvent.rhy.eq(rhy2)).size(),
                    search.size());
        });
    }

    @Test
    public void testSearchRka() {
        withPerson(person -> {
            final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
            final Riistanhoitoyhdistys currentRhy = model().newRiistanhoitoyhdistys(rka);
            model().newOccupation(currentRhy, person, OccupationType.SRVA_YHTEYSHENKILO);

            // events to rka2 with 2 different rhys
            final RiistakeskuksenAlue rka2 = model().newRiistakeskuksenAlue();
            final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys(rka2);
            final List<SrvaEvent> eventsRhy2 = createContentForSearchTest(model().newPerson(), rhy2);
            final Riistanhoitoyhdistys rhy3 = model().newRiistanhoitoyhdistys(rka2);
            final List<SrvaEvent> eventsRhy3 = createContentForSearchTest(model().newPerson(), rhy3);

            // Some extra data for another rka
            final Riistanhoitoyhdistys rhy4 = model().newRiistanhoitoyhdistys(model().newRiistakeskuksenAlue());
            createContentForSearchTest(model().newPerson(), rhy4);

            onSavedAndAuthenticated(createUser(person), () -> {

                SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(currentRhy, null);
                searchDTO.setRkaCode(rka2.getOfficialCode());
                final List<SrvaEventDTO> search = srvaCrudFeature.search(searchDTO);

                assertTrue(search.stream().allMatch(eventDto -> Objects.equals(eventDto.getEventName(), SrvaEventNameEnum.ACCIDENT)));
                assertEquals(countAccidents(eventsRhy2) + countAccidents(eventsRhy3), search.size());
            });
        });
    }

    private static long countAccidents(final List<SrvaEvent> events) {
        return events.stream().filter(srvaEvent -> Objects.equals(srvaEvent.getEventName(), SrvaEventNameEnum.ACCIDENT)).count();
    }

    @Test
    public void testSearchWithSpecies() {
        withPerson(person -> {
            final GameSpecies species = model().newGameSpecies();
            final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

            final Riistanhoitoyhdistys currentRhy = model().newRiistanhoitoyhdistys(rka);
            model().newOccupation(currentRhy, person, OccupationType.SRVA_YHTEYSHENKILO);

            final List<SrvaEvent> eventsCurrentRhy = createContentForSearchTest(model().newPerson(), currentRhy);

            final List<SrvaEvent> eventsOtherRhy =
                    createContentForSearchTest(model().newPerson(), model().newRiistanhoitoyhdistys(rka));

            //Change data so that there is one event on each RHY that matches by species
            eventsCurrentRhy.get(0).setEventName(SrvaEventNameEnum.DEPORTATION);
            eventsCurrentRhy.get(0).setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(SrvaEventNameEnum.DEPORTATION)));
            eventsCurrentRhy.get(0).setSpecies(species);
            eventsOtherRhy.get(0).setEventName(SrvaEventNameEnum.ACCIDENT);
            eventsOtherRhy.get(0).setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(SrvaEventNameEnum.ACCIDENT)));
            eventsOtherRhy.get(0).setSpecies(species);

            onSavedAndAuthenticated(createUser(person), () -> {

                SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(currentRhy, null);
                searchDTO.setGameSpeciesCode(species.getOfficialCode());
                final List<SrvaEventDTO> search = srvaCrudFeature.search(searchDTO);

                assertTrue(search.stream().allMatch(eventDto -> Objects.equals(eventDto.getGameSpeciesCode(), species.getOfficialCode())));
                assertEquals(2, search.size());
            });
        });
    }

    @Test
    public void testSearchWithOtherSpecies() {
        withPerson(person -> {
            final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

            final Riistanhoitoyhdistys currentRhy = model().newRiistanhoitoyhdistys(rka);
            model().newOccupation(currentRhy, person, OccupationType.SRVA_YHTEYSHENKILO);

            final List<SrvaEvent> eventsCurrentRhy = createContentForSearchTest(model().newPerson(), currentRhy);

            final List<SrvaEvent> eventsOtherRhy =
                    createContentForSearchTest(model().newPerson(), model().newRiistanhoitoyhdistys(rka));

            // Change data so that there is one event on each RHY that matches by species
            eventsCurrentRhy.get(0).setEventName(SrvaEventNameEnum.DEPORTATION);
            eventsCurrentRhy.get(0).setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(SrvaEventNameEnum.DEPORTATION)));
            eventsCurrentRhy.get(0).setSpecies(null);
            eventsCurrentRhy.get(0).setOtherSpeciesDescription("Turtle");
            eventsOtherRhy.get(0).setEventName(SrvaEventNameEnum.ACCIDENT);
            eventsOtherRhy.get(0).setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(SrvaEventNameEnum.ACCIDENT)));
            eventsOtherRhy.get(0).setSpecies(null);
            eventsOtherRhy.get(0).setOtherSpeciesDescription("Turtle");

            onSavedAndAuthenticated(createUser(person), () -> {

                SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(currentRhy, null);
                // In search dto other species is 0
                searchDTO.setGameSpeciesCode(0);
                final List<SrvaEventDTO> search = srvaCrudFeature.search(searchDTO);

                assertTrue(search.stream().allMatch(eventDto -> eventDto.getGameSpeciesCode() == null));
                assertEquals(2, search.size());
            });
        });
    }

    @Test
    public void testSearchWithDates() {
        withPerson(person -> withRhy(currentRhy -> {
            final LocalDate today = DateUtil.today();
            model().newOccupation(currentRhy, person, OccupationType.SRVA_YHTEYSHENKILO);

            final List<SrvaEvent> eventsCurrentRhy = createContentForSearchTest(model().newPerson(), currentRhy);

            //Alter data so that there is one event
            eventsCurrentRhy.get(0).setPointOfTime(today.minusDays(5).toDate());

            onSavedAndAuthenticated(createUser(person), () -> {

                SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(currentRhy, null);

                searchDTO.setBeginDate(today.minusDays(5));
                searchDTO.setEndDate(today.minusDays(5));
                assertEquals(1, srvaCrudFeature.search(searchDTO).size());

                searchDTO.setBeginDate(today.minusDays(6));
                searchDTO.setEndDate(today.minusDays(4));
                assertEquals(1, srvaCrudFeature.search(searchDTO).size());

                searchDTO.setBeginDate(today.minusDays(4));
                searchDTO.setEndDate(null);
                assertEquals(eventsCurrentRhy.size() - 1, srvaCrudFeature.search(searchDTO).size());

                searchDTO.setBeginDate(null);
                searchDTO.setEndDate(today.minusDays(6));
                assertEquals(0, srvaCrudFeature.search(searchDTO).size());
            });
        }));
    }

    @Test
    public void testSearchWithHta() {
        withPerson(person -> withRhy(currentRhy -> {
            final LocalDate today = DateUtil.today();
            model().newOccupation(currentRhy, person, OccupationType.SRVA_YHTEYSHENKILO);

            final List<SrvaEvent> eventsCurrentRhy = createContentForSearchTest(model().newPerson(), currentRhy);

            final SrvaEvent hta1Event = eventsCurrentRhy.get(0);
            final GISHirvitalousalue hta1 = model().newGISHirvitalousalueContaining(hta1Event.getGeoLocation());

            final SrvaEvent hta2Event = eventsCurrentRhy.get(1);
            final GISHirvitalousalue hta2 = model().newGISHirvitalousalueContaining(hta2Event.getGeoLocation());

            onSavedAndAuthenticated(createUser(person), () -> {

                final SrvaEventSearchDTO searchDTO = getSearchDTOWithAllNamesAndStates(currentRhy, null);

                searchDTO.setHtaCode(hta1.getNumber());
                final List<SrvaEventDTO> hta1Result = srvaCrudFeature.search(searchDTO);
                assertEquals(1, hta1Result.size());
                assertEquals(hta1Event.getId(), hta1Result.get(0).getId());

                searchDTO.setHtaCode(hta2.getNumber());
                final List<SrvaEventDTO> hta2Result = srvaCrudFeature.search(searchDTO);
                assertEquals(1, hta2Result.size());
                assertEquals(hta2Event.getId(), hta2Result.get(0).getId());
            });
        }));
    }

    @Test
    public void testChangeState() {
        withPerson(person -> {
            final SrvaEvent srvaEvent = model().newSrvaEvent();
            model().newOccupation(srvaEvent.getRhy(), person, OccupationType.SRVA_YHTEYSHENKILO);

            onSavedAndAuthenticated(createUser(person), user -> {
                assertEquals(SrvaEventStateEnum.UNFINISHED, srvaEvent.getState());

                srvaCrudFeature.changeState(srvaEvent.getId(), srvaEvent.getConsistencyVersion(), SrvaEventStateEnum.APPROVED);
                SrvaEvent updatedEvent = srvaEventRepository.findOne(srvaEvent.getId());
                assertEquals(SrvaEventStateEnum.APPROVED, updatedEvent.getState());
                assertEquals(user.getId(), updatedEvent.getApproverAsUser().getId());
                assertEquals(person.getId(), updatedEvent.getApproverAsPerson().getId());

                srvaCrudFeature.changeState(srvaEvent.getId(), updatedEvent.getConsistencyVersion(), SrvaEventStateEnum.UNFINISHED);
                updatedEvent = srvaEventRepository.findOne(srvaEvent.getId());
                assertEquals(SrvaEventStateEnum.UNFINISHED, updatedEvent.getState());
                assertEquals(null, updatedEvent.getApproverAsUser());
                assertEquals(null, updatedEvent.getApproverAsPerson());
            });
        });
    }

    @Test(expected = RevisionConflictException.class)
    public void testChangeState_revConflict() {
        withPerson(person -> {
            final SrvaEvent srvaEvent = model().newSrvaEvent();
            model().newOccupation(srvaEvent.getRhy(), person, OccupationType.SRVA_YHTEYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                srvaCrudFeature.changeState(srvaEvent.getId(), srvaEvent.getConsistencyVersion(), SrvaEventStateEnum.APPROVED);
                srvaCrudFeature.changeState(srvaEvent.getId(), srvaEvent.getConsistencyVersion(), SrvaEventStateEnum.UNFINISHED);
            });
        });
    }

    private static SrvaEventSearchDTO getSearchDTOWithAllNamesAndStates(final Riistanhoitoyhdistys currentRhy,
                                                                        final Riistanhoitoyhdistys rhy) {
        SrvaEventSearchDTO dto = new SrvaEventSearchDTO();
        dto.setCurrentRhyId(F.getId(currentRhy));
        dto.setRhyCode(rhy != null ? rhy.getOfficialCode() : null);
        dto.setStates(Arrays.asList(SrvaEventStateEnum.values()));
        dto.setEventNames(Arrays.asList(SrvaEventNameEnum.values()));
        return dto;
    }

    private List<SrvaEvent> createContentForSearchTest(final Person person, final Riistanhoitoyhdistys rhy) {
        return TestUtils.createList(5, () -> model().newSrvaEvent(person, rhy));
    }
}
