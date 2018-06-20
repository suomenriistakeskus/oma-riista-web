package fi.riista.feature.gamediary;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GameDiaryImageFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameDiaryImageFeature feature;

    @Resource
    private FileStorageService fileStorageService;

    @Test
    public void testSaveImageTwice() throws IOException {
        final byte[] imageData = Files.readAllBytes(new File("frontend/app/assets/images/select2.png").toPath());

        withPerson(person -> {
            final Harvest harvest = model().newHarvest(person);

            onSavedAndAuthenticated(createUser(person), () -> {

                final long harvestId = harvest.getId();
                final UUID imageId = UUID.randomUUID();

                final MultipartFile file = new MockMultipartFile("test.png", "//test/test.png", "image/png", imageData);

                try {
                    feature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, imageId, file);
                    feature.addGameDiaryImageForDiaryEntry(harvestId, GameDiaryEntryType.HARVEST, imageId, file);

                    assertNotNull(feature.getGameDiaryImageBytes(imageId, false));

                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    @Test
    public void testGetGameDiaryImageBytes_expectNotFoundWhenRequestingNonImage() throws IOException {
        final UUID uuid = storeNonImageFile();
        persistAndAuthenticateWithNewUser(true);
        final ResponseEntity<?> response = feature.getGameDiaryImageBytes(uuid, false);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testGetGameDiaryImageBytesResized_expectNotFoundWhenRequestingNonImage() throws IOException {
        final UUID uuid = storeNonImageFile();
        persistAndAuthenticateWithNewUser(true);
        final ResponseEntity<?> response = feature.getGameDiaryImageBytesResized(uuid, 1024, 768, true);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    private UUID storeNonImageFile() throws IOException {
        return fileStorageService
                .storeFile(
                        UUID.randomUUID(),
                        "foobar".getBytes(),
                        FileType.MOOSE_PERMIT_FINISHED_RECEIPT,
                        MediaType.APPLICATION_PDF_VALUE,
                        "receipt.pdf")
                .getId();
    }
}
