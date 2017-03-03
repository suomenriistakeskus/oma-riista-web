package fi.riista.integration.metsastajarekisteri.input;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

public final class PendingImportFile implements Comparable<PendingImportFile> {
    private final Path sourceFile;
    private Path markerFile;
    private final FileTime creationTime;
    private final FileTime lastModifiedAt;

    public PendingImportFile(Path sourceFile, BasicFileAttributes inputFileAttrs, Path markerFile) {
        Objects.requireNonNull(inputFileAttrs);
        this.sourceFile = Objects.requireNonNull(sourceFile).toAbsolutePath();
        this.markerFile = Objects.requireNonNull(markerFile).toAbsolutePath();
        this.creationTime = inputFileAttrs.creationTime();
        this.lastModifiedAt = inputFileAttrs.lastModifiedTime();
    }

    public Path getInputFile() {
        return sourceFile;
    }

    public FileTime getCreationTime() {
        return creationTime;
    }

    public FileTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void removeMarkerFile() throws IOException {
        if (this.markerFile != null) {
            Files.deleteIfExists(this.markerFile);
            this.markerFile = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingImportFile)) return false;

        PendingImportFile that = (PendingImportFile) o;

        return sourceFile.equals(that.sourceFile);
    }

    @Override
    public int compareTo(@Nonnull final PendingImportFile o) {
        final String f1 = this.sourceFile.getFileName().toString();
        final String f2 = o.sourceFile.getFileName().toString();

        return -1 * f1.compareTo(f2);
    }

    @Override
    public int hashCode() {
        return sourceFile.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sourceFile", sourceFile)
                .append("creationTime", creationTime)
                .append("lastModifiedAt", lastModifiedAt)
                .toString();
    }
}
