package fi.riista.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static fi.riista.test.Asserts.assertEmpty;

public class CodeStyleTest {

    private static final Set<String> CHECKED_FILE_EXTENSIONS = ImmutableSet.of(
            "java", "xml", "xsd", "properties", "sql", "jsp", "tag", "tld", "vm", "html", "css", "less", "scss");

    private static final Set<String> EXCLUDED_FILES = ImmutableSet.of(
            "PERUSJHHS2.xsd", "VTJHenkilotiedotKatalogi.xsd", "VTJHenkiloVastaussanoma.java");

    private static class CodeStyleFileFilter extends AbstractFileFilter {
        @Override
        public boolean accept(final File dir, final String filename) {
            return shouldCheckFileByExtension(filename) && !isExcludedFile(dir, filename);
        }

        private static boolean shouldCheckFileByExtension(final String filename) {
            return CHECKED_FILE_EXTENSIONS.contains(Files.getFileExtension(filename.toLowerCase()));
        }

        private static boolean isExcludedFile(final File dir, final String filename) {
            return EXCLUDED_FILES.contains(filename) ||
                    filename.endsWith("_.java") ||
                    dir.getAbsolutePath().contains("/src/generated/java/");
        }
    }

    @Test
    public void verifySourceFilesDoNotContainTabCharacters() throws IOException {
        final List<String> filesContainingTabs = find("\t", StandardCharsets.US_ASCII);
        assertEmpty(filesContainingTabs, "The following files contain tab characters: ");
    }

    @Test
    public void verifySourceFilesDoNotContainCombiningDiaeresis() throws IOException {
        final String combiningDiaeresis = "\u0308";
        final List<String> foundFiles = find(combiningDiaeresis, StandardCharsets.UTF_8);
        assertEmpty(foundFiles, "The following files contain combining diaeresis (looks like umlaut but is not): ");
    }

    private List<String> find(final String toFind, final Charset charset) throws IOException {
        final List<String> foundFiles = new ArrayList<>();
        final Iterator<File> iter = FileUtils.iterateFiles(new File("src"), new CodeStyleFileFilter(), TrueFileFilter.INSTANCE);

        while (iter.hasNext()) {
            final File file = iter.next();
            Files.asCharSource(file, charset).readLines(new LineProcessor<Void>() {
                @Override
                public boolean processLine(@Nonnull final String line) throws IOException {
                    if (line.contains(toFind)) {
                        foundFiles.add(file.getCanonicalPath());
                        return false;
                    }
                    return true;
                }

                @Override
                public Void getResult() {
                    return null;
                }
            });
        }
        return foundFiles;
    }
}
