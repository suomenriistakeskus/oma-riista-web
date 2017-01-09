package fi.riista.feature.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.hash.HashCode;
import com.google.common.io.ByteStreams;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.storage.backend.s3.S3Util;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;

import javax.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileStorageServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private AmazonS3 amazonS3;

    @Test
    public void testDatabase() throws IOException {
        testBackend(FileType.TEST_DB);
    }

    @Test
    public void testLocalFolder() throws IOException {
        testBackend(FileType.TEST_FOLDER);
    }

    @Ignore
    @Test
    public void testAmazonS3() throws IOException {
        testBackend(FileType.TEST_S3);
    }

    @Ignore
    @Test
    public void testAmazonS3BigFile() throws IOException {
        final Path tempPath = Files.createTempFile("aws-s3", ".tmp");
        final File tempFile = tempPath.toFile();

        final int contentSize = 1024 * 1024 * 25;
        final byte asciiCode = 'A';

        try (final FileOutputStream os = new FileOutputStream(tempFile);
             final BufferedOutputStream bos = new BufferedOutputStream(os)) {
            for (int i = 0; i < contentSize; i++) {
                bos.write(asciiCode);
            }
            bos.flush();
        }

        final UUID uuid = UUID.randomUUID();

        fileStorageService.storeFile(uuid, tempFile, FileType.TEST_S3, MediaType.TEXT_PLAIN_VALUE, "test.txt");
        Files.deleteIfExists(tempPath);

        runInTransaction(() -> {
            try {
                final byte[] dbData = fileStorageService.getBytes(uuid);
                assertThat(dbData.length, equalTo(contentSize));
                assertThat(dbData[0],  equalTo(asciiCode));
                assertThat(dbData[contentSize-1], equalTo(asciiCode));

            } catch (final IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                fileStorageService.remove(uuid);
            }
        });
    }

    private void testBackend(final FileType fileType) throws IOException {
        final byte[] data = "Hello world".getBytes("UTF-8");
        final UUID uuid = UUID.randomUUID();

        fileStorageService.storeFile(uuid, data, fileType, MediaType.TEXT_PLAIN_VALUE, "test.txt");

        runInTransaction(() -> {
            final Optional<PersistentFileMetadata> optional = fileStorageService.getMetadata(uuid);
            checkMetadata(fileType, optional.get());

            try {
                final byte[] dbData = fileStorageService.getBytes(uuid);
                assertThat(dbData, equalTo(data));
            } catch (final IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                fileStorageService.remove(uuid);
            }
        });
    }

    private static void checkMetadata(final FileType fileType, final PersistentFileMetadata metadata) {
        assertThat(metadata.getStorageType(), equalTo(fileType.storageType()));
        assertThat(metadata.getOriginalFilename(), equalTo("test.txt"));
        assertThat(metadata.getContentType(), equalTo("text/plain"));
        assertThat(metadata.getContentSize(), equalTo(11L));
        assertThat(metadata.getMd5Hash(), equalTo(HashCode.fromString("3e25960a79dbc69b674cd4ec67a72c62")));
    }

    @Test
    public void testLocalFolderOnRollback() throws IOException {
        final byte[] data = "Hello world".getBytes("UTF-8");
        final UUID uuid = UUID.randomUUID();
        final AtomicReference<Path> resourcePath = new AtomicReference<>();

        try {
            runInTransaction(() -> {
                try {
                    final PersistentFileMetadata metadata = fileStorageService.storeFile(
                            uuid, data, FileType.TEST_FOLDER, MediaType.TEXT_PLAIN_VALUE, "test.txt");
                    resourcePath.set(Paths.get(metadata.getResourceUrl().toURI()));

                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }

                assertTrue("File should exist before rollback", Files.exists(resourcePath.get()));

                throw new IllegalArgumentException("Test exception");
            });
        } catch (final IllegalArgumentException ex) {
            assertEquals("Test exception", ex.getMessage());
            assertFalse(fileStorageService.exists(uuid));
            assertNotNull(resourcePath.get());
            assertFalse("File should be deleted on rollback", Files.exists(resourcePath.get()));

            return;
        }

        fail("Expected exception was not thrown");
    }

    @Ignore
    @Test
    public void testS3OnRollback() throws UnsupportedEncodingException {
        final byte[] data = "Hello world".getBytes("UTF-8");
        final UUID uuid = UUID.randomUUID();
        final AtomicReference<URL> resourceUrl = new AtomicReference<>();

        try {
            runInTransaction(() -> {
                try {
                    final PersistentFileMetadata metadata = fileStorageService.storeFile(
                            uuid, data, FileType.TEST_S3, MediaType.TEXT_PLAIN_VALUE, "test.txt");
                    resourceUrl.set(metadata.getResourceUrl());

                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }

                assertTrue("File should exist before rollback", testS3ObjectExists(resourceUrl.get()));

                throw new IllegalArgumentException("Test exception");
            });
        } catch (final IllegalArgumentException ex) {
            assertEquals("Test exception", ex.getMessage());
            assertFalse(fileStorageService.exists(uuid));
            assertNotNull(resourceUrl.get());
            assertFalse("File should not exist after rollback", testS3ObjectExists(resourceUrl.get()));

            return;
        }

        fail("Expected exception was not thrown");
    }

    private boolean testS3ObjectExists(final URL resourceUrl) {
        final S3Util.BucketObjectPair s3Object = S3Util.parseResourceURL(resourceUrl);

        try (final S3Object object = amazonS3.getObject(s3Object.getBucketName(), s3Object.getKey())) {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteStreams.copy(object.getObjectContent(), bos);

            return bos.size() > 0;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
