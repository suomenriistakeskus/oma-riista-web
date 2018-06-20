package fi.riista.feature.common;

import com.google.common.collect.ImmutableSet;
import fi.riista.util.MediaTypeExtras;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;

@Component
public class ContentTypeChecker {
    /*
    "If only Tika Core is available, the Default Detector will work only with Mime Magic and Resource Name detection."
    http://tika.apache.org/1.17/detection.html#The_default_Tika_Detector

    If content aware detection is really needed, add tika-parsers dependency to pom, and make sure that latest
    document formats are supported.

    Latest iWork file formats are not fully supported https://issues.apache.org/jira/browse/TIKA-1358
    */

    private static final Set<String> ALLOWED_ATTACHMENT_CONTENT_TYPE = ImmutableSet.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            MediaTypeExtras.IMAGE_TIFF_VALUE,
            MediaType.TEXT_PLAIN_VALUE,
            MediaType.TEXT_HTML_VALUE,
            MediaType.APPLICATION_XHTML_XML_VALUE,
            MediaTypeExtras.TEXT_CSV_VALUE,
            MediaTypeExtras.APPLICATION_PDF_VALUE);

    private static final Set<String> ALLOWED_APPLICATION_ATTACHMENT_CONTENT_TYPE = ImmutableSet.of(
            // https://tika.apache.org/1.14/formats.html

            // org.apache.tika.parser.microsoft.OfficeParser
            "application/x-tika-msoffice-embedded; format=ole10_native",
            "application/msword",
            "application/vnd.visio",
            "application/vnd.ms-project",
            "application/x-tika-msworks-spreadsheet",
            "application/x-mspublisher",
            "application/vnd.ms-powerpoint",
            "application/x-tika-msoffice",
            "application/sldworks",
            "application/x-tika-ooxml-protected",
            "application/vnd.ms-excel",
            "application/vnd.ms-outlook",

            // org.apache.tika.parser.microsoft.ooxml.OOXMLParser
            "application/vnd.ms-word.document.macroenabled.12",
            "application/vnd.ms-excel.addin.macroenabled.12",
            "application/x-tika-ooxml",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "application/vnd.ms-powerpoint.addin.macroenabled.12",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            "application/vnd.ms-powerpoint.slideshow.macroenabled.12",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-powerpoint.presentation.macroenabled.12",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "application/vnd.ms-excel.template.macroenabled.12",
            "application/vnd.ms-excel.sheet.macroenabled.12",
            "application/vnd.ms-word.template.macroenabled.12",

            // org.apache.tika.parser.odf.OpenDocumentParser
            "application/x-vnd.oasis.opendocument.presentation",
            "application/vnd.oasis.opendocument.chart",
            "application/x-vnd.oasis.opendocument.text-web",
            "application/x-vnd.oasis.opendocument.image",
            "application/vnd.oasis.opendocument.graphics-template",
            "application/vnd.oasis.opendocument.text-web",
            "application/x-vnd.oasis.opendocument.spreadsheet-template",
            "application/vnd.oasis.opendocument.spreadsheet-template",
            "application/vnd.sun.xml.writer",
            "application/x-vnd.oasis.opendocument.graphics-template",
            "application/vnd.oasis.opendocument.graphics",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/x-vnd.oasis.opendocument.chart",
            "application/x-vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.image",
            "application/x-vnd.oasis.opendocument.text",
            "application/x-vnd.oasis.opendocument.text-template",
            "application/vnd.oasis.opendocument.formula-template",
            "application/x-vnd.oasis.opendocument.formula",
            "application/vnd.oasis.opendocument.image-template",
            "application/x-vnd.oasis.opendocument.image-template",
            "application/x-vnd.oasis.opendocument.presentation-template",
            "application/vnd.oasis.opendocument.presentation-template",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.text-template",
            "application/vnd.oasis.opendocument.chart-template",
            "application/x-vnd.oasis.opendocument.chart-template",
            "application/x-vnd.oasis.opendocument.formula-template",
            "application/x-vnd.oasis.opendocument.text-master",
            "application/vnd.oasis.opendocument.presentation",
            "application/x-vnd.oasis.opendocument.graphics",
            "application/vnd.oasis.opendocument.formula",
            "application/vnd.oasis.opendocument.text-master",

            // If tika-parsers is added to pom, then IfZipContainerDetector detected file to be iWork,
            // but doesn't know which exact iWork type.
            // org.apache.tika.parser.pkg.ZipContainerDetector.detectIWork13()
            // org.apache.tika.parser.iwork.iwana.IWork13PackageParser
            // "application/vnd.apple.unknown.13",

            "application/vnd.apple.keynote",
            "application/vnd.apple.iwork",
            "application/vnd.apple.numbers",
            "application/vnd.apple.pages"
    );

    private static final Logger LOG = LoggerFactory.getLogger(ContentTypeChecker.class);

    private Tika tika;

    @PostConstruct
    public void init() {
        this.tika = new Tika();
    }

    public boolean isValidAttachmentContent(final MultipartFile file) {
        try (final TikaInputStream stream = TikaInputStream.get(file.getInputStream())) {
            final String contentType = detect(file, stream);

            if (!ALLOWED_ATTACHMENT_CONTENT_TYPE.contains(contentType)) {
                LOG.error("Invalid media type not allowed:" + contentType);
                return false;
            }

            return true;

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidApplicationAttachmentContent(final MultipartFile file) {
        try (final TikaInputStream stream = TikaInputStream.get(file.getInputStream())) {
            final String contentType = detect(file, stream);

            if (!ALLOWED_ATTACHMENT_CONTENT_TYPE.contains(contentType) &&
                    !ALLOWED_APPLICATION_ATTACHMENT_CONTENT_TYPE.contains(contentType)) {
                LOG.error("Invalid media type not allowed:" + contentType);
                return false;
            }

            return true;

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String detect(final MultipartFile file, final TikaInputStream stream) throws IOException {
        final Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, file.getOriginalFilename());
        metadata.set(Metadata.CONTENT_TYPE, file.getContentType());
        return tika.detect(stream, metadata);
    }
}
