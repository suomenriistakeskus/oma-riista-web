package fi.riista.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;

public class ContentDispositionUtil {

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static String cleanFileName(final String filename) {
        return StringUtils.replaceChars(filename, "äöåÄÖÅ", "aoaAOA").replaceAll("[^A-Za-z0-9\\.\\-_]", "");
    }

    public static String encodeAttachmentFilename(final String filename) {
        return "attachment; filename=\"" + cleanFileName(filename) + "\"";
    }

    public static String decodeAttachmentFileName(final String contentDisposition) {
        return contentDisposition.split("filename=")[1].replaceAll("[\\[\\]\"]", "");
    }

    public static void addHeader(final HttpServletResponse response, final String filename) {
        response.setHeader(CONTENT_DISPOSITION, encodeAttachmentFilename(filename));
    }

    public static HttpHeaders header(final String filename) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_DISPOSITION, encodeAttachmentFilename(filename));
        return headers;
    }

    private ContentDispositionUtil() {
        throw new AssertionError();
    }
}
