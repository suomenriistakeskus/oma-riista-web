package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HuntingControlEventAttachmentFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingControlEventAttachmentFeature feature;

    @Resource
    private HuntingControlEventRepository eventRepository;

    @Resource
    private HuntingControlAttachmentRepository attachmentRepository;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Riistanhoitoyhdistys rhy;
    private HuntingControlEvent event;
    private PersistentFileMetadata fileMetadata1;
    private PersistentFileMetadata fileMetadata2;
    private HuntingControlAttachment attachment1;
    private HuntingControlAttachment attachment2;

    private final static String fileName1 = "temp1.txt";
    private final static String fileName2 = "temp2.txt";
    private final static String fileContent1 = "data1";
    private final static String fileContent2 = "data2";

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        event = model().newHuntingControlEvent(rhy);
        fileMetadata1 = model().newPersistentFileMetadata();
        fileMetadata2 = model().newPersistentFileMetadata();
        attachment1 = model().newHuntingControlAttachment(event, fileMetadata1);
        attachment2 = model().newHuntingControlAttachment(event, fileMetadata2);

        try {
            addFileToMetadata(attachment1, fileName1, fileContent1.getBytes());
            addFileToMetadata(attachment2, fileName2, fileContent2.getBytes());
        } catch (final IOException e) {
            fail("Failed to create attachment");
        }
    }

    @Test
    public void testDeleteAttachmentFromEvent() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                feature.deleteAttachment(attachment1.getId());

                runInTransaction(() -> {
                    final HuntingControlEvent createdEvent = eventRepository.getOne(event.getId());
                    final List<HuntingControlAttachment> attachmentList = createdEvent.getAttachments();
                    assertEquals(1, attachmentList.size());

                    final HuntingControlAttachment remainingAttachment = attachmentList.get(0);
                    assertEquals(fileName2, remainingAttachment.getAttachmentMetadata().getOriginalFilename());
                });
            });
        });
    }

    @Test
    public void testListAttachments() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<HuntingControlAttachmentDTO> attachmentDTOS = feature.listAttachments(event.getId());

                assertEquals(attachmentDTOS.size(), 2);
            });
        });
    }

    @Test
    public void testGetAttachment() {
        withPerson(person -> {
            model().newOccupation(rhy, person, TOIMINNANOHJAAJA);

            onSavedAndAuthenticated(createUser(person), () -> {
                try {
                    final ResponseEntity<byte[]> attachmentContent = feature.getAttachment(attachment1.getId());
                    assertArrayEquals(fileContent1.getBytes(), attachmentContent.getBody());
                } catch (final IOException e) {
                    fail("Failed to get attachment data");
                }
            });
        });
    }

    private void addFileToMetadata(final HuntingControlAttachment attachment,
                                   final String fileName,
                                   final byte[] fileContent) throws IOException {

        final File file = folder.newFile(fileName);

        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(fileContent);
        }

        final PersistentFileMetadata attachmentMetadata = attachment.getAttachmentMetadata();
        attachmentMetadata.setResourceUrl(file.toURI().toURL());
        attachmentMetadata.setOriginalFilename(fileName);
        attachmentMetadata.setContentType("text/plain");
    }

}
