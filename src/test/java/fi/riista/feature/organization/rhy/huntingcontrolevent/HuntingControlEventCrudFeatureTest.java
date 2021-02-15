package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class HuntingControlEventCrudFeatureTest extends EmbeddedDatabaseTest {

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
        assertEquals(rhy.getId(), event.getRhy().getId());
        assertEquals(dto.getTitle(), event.getTitle());
        assertEquals(dto.getInspectorCount(), event.getInspectorCount());
        assertEquals(dto.getCooperationType(), event.getCooperationType());
        assertEquals(dto.getWolfTerritory(), event.getWolfTerritory());
        assertEquals(dto.getInspectors(), event.getInspectors());
        assertEquals(dto.getGeoLocation(), event.getGeoLocation());
        assertEquals(dto.getDate(), event.getDate());
        assertEquals(dto.getBeginTime(), event.getBeginTime());
        assertEquals(dto.getEndTime(), event.getEndTime());
        assertEquals(dto.getCustomers(), event.getCustomers());
        assertEquals(dto.getProofOrders(), event.getProofOrders());
        assertEquals(dto.getDescription(), event.getDescription());
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

        dto.setRhy(RiistanhoitoyhdistysDTO.create(rhy));

        dto.setTitle("Title");
        dto.setInspectorCount(1);
        dto.setCooperationType(HuntingControlCooperationType.POLIISI);
        dto.setWolfTerritory(true);
        dto.setInspectors("Inspectors");
        dto.setGeoLocation(geoLocation());
        dto.setDate(date);
        dto.setBeginTime(beginTime);
        dto.setEndTime(endTime);
        dto.setCustomers(1);
        dto.setProofOrders(1);
        dto.setDescription("Description");

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
                    assertNotNull(created);

                    assertEvent(created, inputDTO, rhy);
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
                    assertNotNull(created);

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
                    assertNotNull(created);
                    assertEquals(1, created.getAttachments().size());
                    assertEquals(4096, created.getAttachments().get(0).getAttachmentMetadata().getContentSize());
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
                    assertFalse(eventRepository.findById(eventId).isPresent());
                    assertFalse(attachmentRepository.findById(attachmentId).isPresent());
                });
            });
        });
    }

    @Test
    public void testUpdate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final HuntingControlEvent event = model().newHuntingControlEvent(rhy);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final HuntingControlEventDTO dto = HuntingControlEventDTO.create(event, rhy, Collections.emptyList());

                dto.setTitle(dto.getTitle() + "_modified");
                dto.setInspectorCount(dto.getInspectorCount() + 1);
                dto.setCooperationType(HuntingControlCooperationType.RAJAVARTIOSTO);
                dto.setWolfTerritory(!dto.getWolfTerritory());
                dto.setInspectors(dto.getInspectors() + "_mutated");
                dto.setDate(dto.getDate().minusDays(1));
                dto.setBeginTime(dto.getBeginTime().plusHours(1));
                dto.setEndTime(dto.getEndTime().plusHours(1));
                dto.setCustomers(dto.getCustomers() + 1);
                dto.setProofOrders(dto.getProofOrders() + 1);
                dto.setDescription(dto.getDescription() + "_mutated");

                feature.update(dto);

                runInTransaction(() -> {
                    final HuntingControlEvent updated = eventRepository.getOne(dto.getId());
                    assertNotNull(updated);

                    assertEvent(updated, dto, rhy);
                });
            });
        });
    }

    private void addFileToMetadata(final HuntingControlAttachment attachment, final String fileName) throws IOException {
        final File file = folder.newFile(fileName);
        final PersistentFileMetadata attachmentMetadata = attachment.getAttachmentMetadata();
        attachmentMetadata.setResourceUrl(file.toURI().toURL());
        attachmentMetadata.setOriginalFilename(fileName);
    }
}
