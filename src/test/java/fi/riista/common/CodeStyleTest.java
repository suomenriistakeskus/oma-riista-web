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
        final Iterator<File> iter = FileUtils.iterateFiles(new File("src"), new CodeStyleFileFilter(), TrueFileFilter.INSTANCE);

        final List<String> filesContainingTabs = new ArrayList<>();

        while (iter.hasNext()) {
            final File file = iter.next();
            Files.asCharSource(file, StandardCharsets.US_ASCII).readLines(new LineProcessor<Void>() {
                @Override
                public boolean processLine(@Nonnull final String line) throws IOException {
                    boolean result = true;
                    if (line.contains("\t")) {
                        filesContainingTabs.add(file.getCanonicalPath());
                        result = false;
                    }
                    return result;
                }

                @Override
                public Void getResult() {
                    return null;
                }
            });
        }

        assertEmpty(filesContainingTabs, "The following files contain tab characters: ");
    }

}
