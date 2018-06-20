package fi.riista.util;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.ScratchFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFUtil {

    public static String extractAllText(final File file) {
        try (final ScratchFile scratchFile = new ScratchFile(MemoryUsageSetting.setupMainMemoryOnly());
             final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            final PDFParser parser = new PDFParser(randomAccessFile, "", null, null, scratchFile);
            parser.parse();

            try (final PDDocument pdDoc = parser.getPDDocument()) {
                final PDFTextStripper pdfStripper = new PDFTextStripper();
                return pdfStripper.getText(pdDoc);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PDFUtil() {
        throw new AssertionError();
    }
}
