package fi.riista.integration.metsastajarekisteri.input;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class PendingImportFileFilter {

    public static final List<String> DEFAULT_SUFFIX_LIST = Arrays.asList(
            "csv", "csv.enc", "csv.gz", "csv.gz.enc", "csv.enc.gz");
    public static final String DEFAULT_SUFFIX_COMPLETE = ".complete";

    public static PendingImportFileFilter createDefault() {
        return new PendingImportFileFilter(DEFAULT_SUFFIX_LIST, DEFAULT_SUFFIX_COMPLETE);
    }

    private static String createGlobExpression(Iterable<String> suffixes) {
        final StringBuilder sb = new StringBuilder();
        sb.append("*.{");
        Joiner.on(',').appendTo(sb, suffixes);
        sb.append("}");
        return sb.toString();
    }

    private final String globExpression;
    private final String completeSuffix;
    private final Set<PendingImportFile> collectedFiles = Sets.newTreeSet();

    PendingImportFileFilter(List<String> validSuffixes, String completeSuffix) {
        if (!StringUtils.hasText(completeSuffix)) {
            throw new IllegalArgumentException("Invalid complete suffix");
        }

        if (validSuffixes.isEmpty()) {
            throw new IllegalArgumentException("Empty suffix list");
        }

        this.globExpression = createGlobExpression(validSuffixes);
        this.completeSuffix = completeSuffix;
    }

    public Set<PendingImportFile> getCollectedFiles() {
        return ImmutableSet.copyOf(collectedFiles);
    }

    public Set<PendingImportFile> processPath(Path basePath) {
        if (!Files.isDirectory(basePath)) {
            throw new IllegalArgumentException("Input directory does not exist: " + basePath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, globExpression)) {
            for (Path inputFile : stream) {
                final PendingImportFile pending = filterValidFile(inputFile);

                if (pending != null) {
                    collectedFiles.add(pending);
                }
            }

            return getCollectedFiles();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PendingImportFile filterValidFile(Path inputFile) throws IOException {
        final BasicFileAttributes fileAttributes = Files.readAttributes(inputFile, BasicFileAttributes.class);
        final Path markerFile = getMarkerFile(inputFile);

        if (!fileAttributes.isRegularFile() || fileAttributes.size() == 0) {
            return null;
        }

        if (!Files.exists(markerFile)) {
            return null;
        }

        return new PendingImportFile(inputFile, fileAttributes, markerFile);
    }

    private Path getMarkerFile(final Path inputFile) {
        final String markerFileName = inputFile.getFileName().toString() + completeSuffix;

        return inputFile.resolveSibling(markerFileName).normalize();
    }
}
