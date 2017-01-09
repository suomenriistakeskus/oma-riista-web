package fi.riista.util;

import org.apache.commons.lang.StringUtils;

public class ContentDispositionUtil {
    public static final String HEADER_NAME = "Content-Disposition";

    public static String cleanFileName(final String filename) {
        return StringUtils.replaceChars(filename, "äöåÄÖÅ", "aoaAOA").replaceAll("[^A-Za-z0-9\\.\\-]", "");
    }

    public static String encodeAttachmentFilename(final String filename) {
        return "attachment; filename=\"" + cleanFileName(filename) + "\"";
    }

    private ContentDispositionUtil() {
        throw new AssertionError();
    }
}
