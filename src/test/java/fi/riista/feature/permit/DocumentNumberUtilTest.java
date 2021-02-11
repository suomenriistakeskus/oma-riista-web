package fi.riista.feature.permit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DocumentNumberUtilTest {

    @Test
    public void testCreatePermitNumber() {
        assertEquals("2013-1-010-12345-7", DocumentNumberUtil.createDocumentNumber(2013, 1, 1012345));
    }

    @Test
    public void testExtractOrderNumber() {
        assertEquals(98712345, DocumentNumberUtil.extractOrderNumber("2013-1-987-12345-7"));
        assertEquals(12345, DocumentNumberUtil.extractOrderNumber("2013-1-000-12345-7"));
        assertEquals(1, DocumentNumberUtil.extractOrderNumber("2013-1-000-00001-7"));
    }

    @Test
    public void testExtractYear() {
        assertEquals(2013, DocumentNumberUtil.extractYear("2013-1-987-12345-7"));
        assertEquals(2019, DocumentNumberUtil.extractYear("2019-1-987-12345-7"));
    }
}
