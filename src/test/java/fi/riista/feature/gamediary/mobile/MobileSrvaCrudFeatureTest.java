package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.error.RevisionConflictException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.mobile.srva.MobileSrvaCrudFeature;
import fi.riista.feature.gamediary.mobile.srva.MobileSrvaEventDTO;
import fi.riista.feature.gamediary.srva.SrvaApprovedException;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventRepository;
import fi.riista.feature.gamediary.srva.SrvaEventSpecVersion;
import fi.riista.feature.gamediary.srva.SrvaEventStateEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethodDTO;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.TestUtils;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.now;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(Theories.class)
public class MobileSrvaCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileSrvaCrudFeature mobileSrvaCrudFeature;

    @Resource
    private SrvaEventRepository srvaEventRepository;

    @Resource
    private GameDiaryImageRepository gameDiaryImageRepo;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testListSrvaEventsForActiveUser() {
        withPerson(person -> withRhy(rhy -> {

            final int numberOfEvents = nextPositiveIntAtMost(55);

            TestUtils.createList(numberOfEvents, () -> model().newSrvaEvent(person, rhy));

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileSrvaEventDTO> srvaEventDTOs = mobileSrvaCrudFeature.listSrvaEventsForActiveUser(SrvaEventSpecVersion._1);

                assertEquals(numberOfEvents, srvaEventDTOs.size());
            });
        }));
    }

    @Test
    public void testCreateSrvaEvent_duplicate() {
        withPerson(person -> withRhy(rhy -> {
            final MobileSrvaEventDTO inputDto = createMobileSrvaEventDto();

            onSavedAndAuthenticated(createUser(person), () -> {
                inputDto.setRhyId(rhy.getId());
                assertEquals(mobileSrvaCrudFeature.createSrvaEvent(inputDto).getId(),
                        mobileSrvaCrudFeature.createSrvaEvent(inputDto).getId());
            });
        }));
    }

    @Test
    public void testCreateSrvaEvent() {
        final MobileSrvaEventDTO inputDto = createMobileSrvaEventDto();
        _testCreateSrvaEvent(inputDto);
    }

    @Test(expected = MessageExposableValidationException.class)
    public void testCreateSrvaEvent_nullSpecVersion() {
        final MobileSrvaEventDTO inputDto = createMobileSrvaEventDto();
        inputDto.setSrvaEventSpecVersion(null);
        _testCreateSrvaEvent(inputDto);
    }

    @Test(expected = MessageExposableValidationException.class)
    public void testCreateSrvaEvent_nullRefId() {
        final MobileSrvaEventDTO inputDto = createMobileSrvaEventDto();
        inputDto.setMobileClientRefId(null);
        _testCreateSrvaEvent(inputDto);
    }

    private void _testCreateSrvaEvent(final MobileSrvaEventDTO inputDto) {
        withPerson(person -> withRhy(rhy -> onSavedAndAuthenticated(createUser(person), () -> {
            inputDto.setRhyId(rhy.getId());
            final MobileSrvaEventDTO outputDto = mobileSrvaCrudFeature.createSrvaEvent(inputDto);

            assertNotNull(outputDto);

            runInTransaction(() -> {
                final SrvaEvent savedSrvaEvent = srvaEventRepository.findById(outputDto.getId()).orElse(null);
                assertNotNull(savedSrvaEvent);
                assertEquals(Integer.valueOf(0), savedSrvaEvent.getConsistencyVersion());

                assertEquals(person, savedSrvaEvent.getAuthor());

                assertEquals(inputDto.getEventName(), savedSrvaEvent.getEventName());
                assertEquals(inputDto.getEventType(), savedSrvaEvent.getEventType());
                assertEquals(inputDto.getGeoLocation(), savedSrvaEvent.getGeoLocation());
                assertEquals(inputDto.getGameSpeciesCode(), Integer.valueOf(savedSrvaEvent.getSpecies().getOfficialCode()));
                assertEquals(inputDto.getTotalSpecimenAmount(), savedSrvaEvent.getTotalSpecimenAmount());
                assertEquals(rhy.getOfficialCode(), savedSrvaEvent.getRhy().getOfficialCode());

                assertEquals(inputDto.getSrvaEventSpecVersion(), outputDto.getSrvaEventSpecVersion());
                assertEquals(inputDto.getMobileClientRefId(), savedSrvaEvent.getMobileClientRefId());
            });
        })));
    }

    @Test
    public void testUpdateSrvaEvent() {
        withPerson(person -> {
            final GameSpecies gameSpecies = model().newGameSpecies();
            final SrvaEvent srvaEvent = model().newSrvaEvent(person, gameSpecies);

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileSrvaEventDTO updateDto = MobileSrvaEventDTO.create(srvaEvent, SrvaEventSpecVersion._1);
                updateDto.setGameSpeciesCode(gameSpecies.getOfficialCode());
                updateDto.setMethods(Collections.singletonList(new SrvaMethodDTO(SrvaMethodEnum.DOG, true)));
                updateDto.setSpecimens(Collections.emptyList());

                final SrvaEventTypeEnum newEventType = someOtherThan(updateDto.getEventType(), SrvaEventTypeEnum.getBySrvaEvent(updateDto.getEventName()));
                updateDto.setEventType(newEventType);

                final MobileSrvaEventDTO outputDto = mobileSrvaCrudFeature.updateSrvaEvent(updateDto);

                assertEquals(newEventType, outputDto.getEventType());
                assertEquals(srvaEvent.getId(), outputDto.getId());
                assertEquals(srvaEvent.getConsistencyVersion().longValue() + 1, outputDto.getRev().longValue());
            });
        });
    }

    @Test(expected = MessageExposableValidationException.class)
    public void testUpdateSrvaEvent_nullSpecVersion() {
        final SrvaEvent srvaEvent = model().newSrvaEvent();

        onSavedAndAuthenticated(createNewUser(), () -> {
            final MobileSrvaEventDTO updateDto = MobileSrvaEventDTO.create(srvaEvent, SrvaEventSpecVersion._1);
            updateDto.setSrvaEventSpecVersion(null);

            mobileSrvaCrudFeature.updateSrvaEvent(updateDto);
        });
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateSrvaEvent_notFound() {
        withRhy(rhy -> {
            final MobileSrvaEventDTO inputDto = createMobileSrvaEventDto();
            inputDto.setId(111111L);

            onSavedAndAuthenticated(createNewUser(), () -> {
                inputDto.setRhyId(rhy.getId());
                mobileSrvaCrudFeature.updateSrvaEvent(inputDto);
            });
        });
    }

    @Test(expected = RevisionConflictException.class)
    public void testUpdateSrvaEvent_revConflict() {
        withRhy(rhy -> {
            final MobileSrvaEventDTO inputDto = createMobileSrvaEventDto();

            onSavedAndAuthenticated(createUserWithPerson(), () -> {
                inputDto.setRhyId(rhy.getId());

                final MobileSrvaEventDTO savedDto = mobileSrvaCrudFeature.createSrvaEvent(inputDto);
                savedDto.setRev(savedDto.getRev() - 1);
                mobileSrvaCrudFeature.updateSrvaEvent(savedDto);
            });
        });
    }

    @Test
    public void testUpdateSrvaEvent_alreadyApproved() {
        withRhy(rhy -> {
            final MobileSrvaEventDTO inputDto = createMobileSrvaEventDto();

            onSavedAndAuthenticated(createUserWithPerson(), user -> {
                inputDto.setDescription("Original");
                inputDto.setRhyId(rhy.getId());

                final MobileSrvaEventDTO dto = mobileSrvaCrudFeature.createSrvaEvent(inputDto);

                // Change manually state to APPROVED so that updateSrvaEvent throws RunTimeException
                SrvaEvent event = srvaEventRepository.findById(dto.getId()).orElse(null);
                event.setState(SrvaEventStateEnum.APPROVED);
                event.setApproverAsUser(user);
                event = srvaEventRepository.saveAndFlush(event);

                dto.setRev(event.getConsistencyVersion());
                dto.setDescription("Changed");

                // Make sure that update did not happen and update returns current event from DB
                assertEquals(inputDto.getDescription(), mobileSrvaCrudFeature.updateSrvaEvent(dto).getDescription());
            });
        });
    }

    @Test
    public void testDeleteSrvaEvent() {
        withPerson(person -> {

            final SrvaEvent srvaEvent = model().newSrvaEvent(person);

            onSavedAndAuthenticated(createUser(person), () -> {
                mobileSrvaCrudFeature.deleteSrvaEvent(srvaEvent.getId());

                assertFalse(srvaEventRepository.existsById(srvaEvent.getId()));
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

            onSavedAndAuthenticated(user, u -> mobileSrvaCrudFeature.deleteSrvaEvent(srvaEvent.getId()));
        });
    }

    @Test
    public void testAddAndDeleteImage() throws IOException {
        final UUID imageId = UUID.randomUUID();
        final byte[] imageData = Files.readAllBytes(new File("frontend/app/assets/images/select2.png").toPath());
        final MultipartFile file = new MockMultipartFile("test.png", "//test/test.png", "image/png", imageData);

        withPerson(person -> {
            final SrvaEvent srvaEvent = model().newSrvaEvent(person);

            onSavedAndAuthenticated(createUser(person), () -> {
                //add
                try {
                    mobileSrvaCrudFeature.addImage(srvaEvent.getId(), imageId, file);
                    assertEquals(1, gameDiaryImageRepo.findBySrvaEvent(srvaEvent).size());
                    assertEquals(imageId, gameDiaryImageRepo.findBySrvaEvent(srvaEvent).get(0).getFileMetadata().getId());
                } catch (IOException e) {
                    e.printStackTrace();
                    fail();
                }

                //delete
                mobileSrvaCrudFeature.deleteImage(imageId);
                assertEquals(0, gameDiaryImageRepo.findBySrvaEvent(srvaEvent).size());
            });
        });
    }

    @Test
    public void testDeleteImage_notFound() {
        persistAndAuthenticateWithNewUser(true);
        mobileSrvaCrudFeature.deleteImage(UUID.randomUUID());
    }

    private MobileSrvaEventDTO createMobileSrvaEventDto() {

        final MobileSrvaEventDTO mobileSrvaEventDTO = new MobileSrvaEventDTO();

        mobileSrvaEventDTO.setGeoLocation(geoLocation());
        mobileSrvaEventDTO.setPointOfTime(DateUtil.localDateTime());
        mobileSrvaEventDTO.setEventName(some(SrvaEventNameEnum.class));
        mobileSrvaEventDTO.setEventType(some(SrvaEventTypeEnum.getBySrvaEvent(mobileSrvaEventDTO.getEventName())));
        mobileSrvaEventDTO.setGameSpeciesCode(model().newGameSpecies().getOfficialCode());
        mobileSrvaEventDTO.setTotalSpecimenAmount(1);

        mobileSrvaEventDTO.setMethods(Collections.singletonList(new SrvaMethodDTO(some(SrvaMethodEnum.class), true)));
        mobileSrvaEventDTO.setSpecimens(Collections.emptyList());
        mobileSrvaEventDTO.setState(some(SrvaEventStateEnum.class));

        mobileSrvaEventDTO.setSrvaEventSpecVersion(SrvaEventSpecVersion._1);
        mobileSrvaEventDTO.setMobileClientRefId(getNumberGenerator().nextLong());

        return mobileSrvaEventDTO;
    }

    @Theory
    public void testFetchPageForActiveUser(final SrvaEventSpecVersion version) {
        withPerson(person -> withRhy(rhy -> {
            final List<SrvaEvent> list = TestUtils.createList(1, () -> model().newSrvaEvent(person, rhy));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> page =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(0).getModificationTime())));
            });
        }));
    }

    @Theory
    public void testFetchPageForActiveUser_doesNotReturnWithSameTimestamp(final SrvaEventSpecVersion version) {
        withPerson(person -> withRhy(rhy -> {
            final List<SrvaEvent> list = TestUtils.createList(1, () -> model().newSrvaEvent(person, rhy));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> page =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(0).getModificationTime())));

                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> secondPage =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(page.getLatestEntry(), version);
                assertThat(secondPage.getContent(), is(empty()));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), is(nullValue()));
            });
        }));
    }

    @Theory
    public void testFetchPageForActiveUser_modifiedAfterNewestEvent(final SrvaEventSpecVersion version) {
        withPerson(person -> withRhy(rhy -> {
            final List<SrvaEvent> list = TestUtils.createList(1, () -> model().newSrvaEvent(person, rhy));

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> page =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(
                                ldt(list.get(0).getModificationTime().plusSeconds(1)), version);

                assertThat(page.getContent(), is(empty()));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), is(nullValue()));
            });
        }));
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_differentTimeStamp(final SrvaEventSpecVersion version) {
        withPerson(person -> withRhy(rhy -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<SrvaEvent> list = TestUtils.createList(51, () -> {
                MockTimeProvider.advance(Minutes.minutes(1));
                final SrvaEvent event = model().newSrvaEvent(person, rhy);
                persistInNewTransaction();
                return event;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> page =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(50));
                assertThat(page.isHasMore(), is(true));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(49).getModificationTime())));
            });
        }));
    }

    private LocalDateTime ldt(final DateTime modificationTime) {
        return DateUtil.toLocalDateTimeNullSafe(modificationTime);
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_differentTimeStamp_fetchSecondPage(final SrvaEventSpecVersion version) {
        withPerson(person -> withRhy(rhy -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<SrvaEvent> list = TestUtils.createList(51, () -> {
                MockTimeProvider.advance(Minutes.minutes(1));
                final SrvaEvent event = model().newSrvaEvent(person, rhy);
                persistInNewTransaction();
                return event;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                // When searching with timestamp of the second last item, only the last one should be returned
                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> page =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(ldt(list.get(49).getModificationTime()), version);

                assertThat(page.getContent(), hasSize(1));
                assertThat(page.isHasMore(), is(false));
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(50).getModificationTime())));
                assertThat(page.getContent().get(0).getId(), equalTo(list.get(50).getId()));
            });
        }));
    }

    @Theory
    public void testFetchPageForActiveUser_pagingDoesNotReturnDuplicates(final SrvaEventSpecVersion version) {
        withPerson(person -> withRhy(rhy -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());
            final LocalDateTime firstBatchTime = DateUtil.toLocalDateTimeNullSafe(now());

            final List<SrvaEvent> list = TestUtils.createList(50, () -> model().newSrvaEvent(person, rhy));
            persistInNewTransaction();

            MockTimeProvider.advance(1);
            final LocalDateTime secondBatchTime = DateUtil.toLocalDateTimeNullSafe(now());
            final List<SrvaEvent> secondList = TestUtils.createList(1, () -> model().newSrvaEvent(person, rhy));
            persistInNewTransaction();


            onSavedAndAuthenticated(createUser(person), () -> {
                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> page =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(50));
                assertThat(page.isHasMore(), is(true));
                assertThat(page.getLatestEntry(), equalTo(firstBatchTime));


                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> secondPage =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(page.getLatestEntry(), version);

                assertThat(secondPage.getContent(), hasSize(1));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), equalTo(secondBatchTime));
                assertThat(secondPage.getContent().get(0).getId(), equalTo(secondList.get(0).getId()));
            });
        }));
    }

    @Theory
    public void testFetchPageForActiveUser_moreThanOnePage_sameTimeStamp(final SrvaEventSpecVersion version) {
        withPerson(person -> withRhy(rhy -> {
            MockTimeProvider.mockTime(now().minusHours(1).getMillis());

            final List<SrvaEvent> list = TestUtils.createList(51, () -> model().newSrvaEvent(person, rhy));

            onSavedAndAuthenticated(createUser(person), () -> {
                // When more that pageful of entries found with same modification time, extra items are returned
                // in the list
                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> page =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(null, version);

                assertThat(page.getContent(), hasSize(51));
                assertThat(page.isHasMore(), is(true)); // Does not actually have more in this special case
                assertThat(page.getLatestEntry(), equalTo(ldt(list.get(50).getModificationTime())));

                final MobileDiaryEntryPageDTO<MobileSrvaEventDTO> secondPage =
                        mobileSrvaCrudFeature.fetchPageForActiveUser(ldt((list.get(50).getModificationTime())), version);

                assertThat(secondPage.getContent(), is(empty()));
                assertThat(secondPage.isHasMore(), is(false));
                assertThat(secondPage.getLatestEntry(), is(nullValue()));
            });
        }));
    }

    @Test
    public void testGetDeletedEvents() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        model().newDeletedSrvaEvent(now, 1L, user.getPerson().getId());

        onSavedAndAuthenticated(user, () -> {
            final LocalDateTime deletionTime = ldt(now.minusSeconds(1));
            final MobileDeletedDiaryEntriesDTO deletedEvents = mobileSrvaCrudFeature.getDeletedEvents(deletionTime);
            assertThat(deletedEvents.getEntryIds(), hasSize(1));
            assertThat(deletedEvents.getEntryIds().get(0), equalTo(1L));
            assertThat(deletedEvents.getLatestEntry(), equalTo(ldt(now)));
        });
    }

    @Test
    public void testGetDeletedEvents_nullTimeStamp() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        model().newDeletedSrvaEvent(now, 1L, user.getPerson().getId());

        onSavedAndAuthenticated(user, () -> {
            final MobileDeletedDiaryEntriesDTO deletedEvents = mobileSrvaCrudFeature.getDeletedEvents(null);
            assertThat(deletedEvents.getEntryIds(), hasSize(1));
            assertThat(deletedEvents.getEntryIds().get(0), equalTo(1L));
            assertThat(deletedEvents.getLatestEntry(), equalTo(ldt(now)));
        });
    }

    @Test
    public void testGetDeletedEvents_onlyNewerDeletionTimesReturned() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        model().newDeletedSrvaEvent(now, 1L, user.getPerson().getId());

        onSavedAndAuthenticated(user, () -> {
            final MobileDeletedDiaryEntriesDTO deletedEvents = mobileSrvaCrudFeature.getDeletedEvents(ldt(now));
            assertThat(deletedEvents.getEntryIds(), hasSize(0));
            assertThat(deletedEvents.getLatestEntry(), is(nullValue()));
        });
    }

    @Test
    public void testGetDeletedEvents_onlyOwnEventDeletionTimesReturned() {
        final SystemUser user = createUserWithPerson();
        persistInNewTransaction();

        final DateTime now = now();
        model().newDeletedSrvaEvent(now, 1L, user.getPerson().getId() + 1);

        onSavedAndAuthenticated(user, () -> {
            final MobileDeletedDiaryEntriesDTO deletedEvents =
                    mobileSrvaCrudFeature.getDeletedEvents(ldt(now.minusSeconds(1)));
            assertThat(deletedEvents.getEntryIds(), hasSize(0));
            assertThat(deletedEvents.getLatestEntry(), is(nullValue()));
        });
    }
}
