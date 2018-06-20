package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.config.Constants;
import io.vavr.control.Validation;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.basenameMismatchBetweenXmlAndPdfFile;
import static fi.riista.feature.huntingclub.moosedatacard.exception.MooseDataCardImportFailureReasons.invalidFilename;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MooseDataCardFilenameValidatorTest {

    private static final String VALID_XML_FILENAME = "2015-1-450-00100-9-1234567-20151118084639.xml";
    private static final String VALID_PDF_FILENAME = "2015-1-450-00100-9-1234567-20151118084639.pdf";

    private static final String VALID_XML_FILENAME_2 = "2015-1-450-00100-9-7654321-20151118084641.xml";
    private static final String VALID_PDF_FILENAME_2 = "2015-1-450-00100-9-7654321-20151118084641.pdf";

    private static final String INVALID_XML_FILENAME = VALID_XML_FILENAME.replaceAll("-", "");
    private static final String INVALID_PDF_FILENAME = VALID_PDF_FILENAME.replaceAll("-", "");

    private static final String FILENAME_WITHOUT_EXTENSION = VALID_XML_FILENAME.replaceAll(".xml", "");

    private final MooseDataCardFilenameValidator validator = new MooseDataCardFilenameValidator();

    @Test
    public void testValidate() {
        final Validation<List<String>, MooseDataCardFilenameValidation> result =
                validator.validate(VALID_XML_FILENAME, VALID_PDF_FILENAME);

        assertTrue(result.isValid());
        assertEquals("2015-1-450-00100-9", result.get().permitNumber);
        assertEquals("1234567", result.get().clubCode);

        final DateTime expectedTimestamp = MooseDataCardFilenameValidator.DATE_FORMATTER
                .parseLocalDateTime("20151118084639")
                .toDateTime(Constants.DEFAULT_TIMEZONE);
        assertEquals(expectedTimestamp, result.get().timestamp);
    }

    @Test
    public void testValidate_whenFilenamesGivenInWrongOrder() {
        // XML and PDF file swapped.
        assertInvalidPairOfFilenames(VALID_PDF_FILENAME, VALID_XML_FILENAME, asList(
                invalidFilename(VALID_PDF_FILENAME, "xml"),
                invalidFilename(VALID_XML_FILENAME, "pdf")));
    }

    @Test
    public void testValidate_whenValidFilenameGivenTwice() {
        assertInvalidPairOfFilenames(
                VALID_XML_FILENAME, VALID_XML_FILENAME, invalidFilename(VALID_XML_FILENAME, "pdf"));
    }

    @Test
    public void testValidate_whenInvalidFilenameGivenTwice() {
        assertInvalidPairOfFilenames(INVALID_XML_FILENAME, INVALID_XML_FILENAME, asList(
                invalidFilename(INVALID_XML_FILENAME, "xml"),
                invalidFilename(INVALID_XML_FILENAME, "pdf")));
    }

    @Test
    public void testValidate_whenFilenameWithoutExtensionGivenTwice() {
        assertInvalidPairOfFilenames(FILENAME_WITHOUT_EXTENSION, FILENAME_WITHOUT_EXTENSION, asList(
                invalidFilename(FILENAME_WITHOUT_EXTENSION, "xml"),
                invalidFilename(FILENAME_WITHOUT_EXTENSION, "pdf")));
    }

    @Test
    public void testValidate_withBasenameMismatchBetweenTwoValidFilenames() {
        assertInvalidPairOfFilenames(VALID_XML_FILENAME, VALID_PDF_FILENAME_2, basenameMismatchBetweenXmlAndPdfFile());
        assertInvalidPairOfFilenames(VALID_XML_FILENAME_2, VALID_PDF_FILENAME, basenameMismatchBetweenXmlAndPdfFile());
    }

    @Test
    public void testValidate_withBasenameMismatchBetweenValidAndInvalidFilename() {
        assertInvalidPairOfFilenames(INVALID_XML_FILENAME, VALID_PDF_FILENAME, asList(
                basenameMismatchBetweenXmlAndPdfFile(),
                invalidFilename(INVALID_XML_FILENAME, "xml")));

        assertInvalidPairOfFilenames(VALID_XML_FILENAME, INVALID_PDF_FILENAME, asList(
                basenameMismatchBetweenXmlAndPdfFile(),
                invalidFilename(INVALID_PDF_FILENAME, "pdf")));
    }

    private void assertInvalidPairOfFilenames(
            final String xmlFileName, final String pdfFileName, final String expectedMessage) {

        assertInvalidPairOfFilenames(xmlFileName, pdfFileName, Collections.singletonList(expectedMessage));
    }

    private void assertInvalidPairOfFilenames(
            final String xmlFileName, final String pdfFileName, final List<String> expectedMessages) {

        final Validation<List<String>, MooseDataCardFilenameValidation> result =
                validator.validate(xmlFileName, pdfFileName);

        assertTrue(result.isInvalid());
        assertEquals(result.getError(), expectedMessages);
    }

}
