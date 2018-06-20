package fi.riista.feature.permit.application.archive;

import com.google.common.base.Preconditions;
import fi.riista.config.Constants;
import fi.riista.util.ContentDispositionUtil;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PermitApplicationArchiveGenerator implements Closeable {
    private static final String ARCHIVE_PREFIX = "application";
    private static final String ARCHIVE_SUFFIX = ".zip";
    private static final int MAX_FILENAME_LENGTH = 255;

    private static class SourceItem {
        final Path sourcePath;
        final String targetFileName;

        private SourceItem(final String targetFileName) throws IOException {
            this.sourcePath = Files.createTempFile("attachment", null);
            this.targetFileName = targetFileName;
        }
    }

    private int counter = 0;
    private final List<SourceItem> sourceItems = new LinkedList<>();

    public Path addAttachment(final String originalFileName) throws IOException {
        return addSourceItem(originalFileName).sourcePath;
    }

    private SourceItem addSourceItem(final String originalFileName) throws IOException {
        final SourceItem item = new SourceItem(getTargetFileName(originalFileName));
        sourceItems.add(item);
        return item;
    }

    @Nonnull
    private String getTargetFileName(final String originalFileName) {
        Preconditions.checkState(counter < 999);

        final String cleanFileName = ContentDispositionUtil.cleanFileName(originalFileName);
        final int trimLength = Math.min(cleanFileName.length(), MAX_FILENAME_LENGTH - 6);
        final String trimmedFileName = cleanFileName.substring(0, trimLength);

        // Add unique prefix to prevent name conflicts
        return String.format("%03d_%s", counter++, trimmedFileName);
    }

    public Path buildArchive() throws Exception {
        final Path archivePath = Files.createTempFile(ARCHIVE_PREFIX, ARCHIVE_SUFFIX);

        try (final FileOutputStream fos = new FileOutputStream(archivePath.toFile());
             final BufferedOutputStream bos = new BufferedOutputStream(fos);
             final ZipOutputStream zipOutputStream = new ZipOutputStream(bos, Constants.DEFAULT_CHARSET)) {

            for (final SourceItem src : sourceItems) {
                zipOutputStream.putNextEntry(new ZipEntry(src.targetFileName));
                Files.copy(src.sourcePath, zipOutputStream);
                zipOutputStream.closeEntry();
            }
            return archivePath;

        } catch (Exception e) {
            Files.deleteIfExists(archivePath);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        for (final SourceItem sourceItem : sourceItems) {
            Files.deleteIfExists(sourceItem.sourcePath);
        }
    }
}
