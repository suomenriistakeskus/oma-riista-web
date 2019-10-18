package fi.riista.feature.permit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PermitNumberUtilTest {

    @Test
    public void testCreatePermitNumber() {
        assertEquals("2013-1-010-12345-7", PermitNumberUtil.createPermitNumber(2013, 1, 1012345));
    }

    @Test
    public void testExtractOrderNumber() {
        assertEquals(98712345, PermitNumberUtil.extractOrderNumber("2013-1-987-12345-7"));
        assertEquals(12345, PermitNumberUtil.extractOrderNumber("2013-1-000-12345-7"));
        assertEquals(1, PermitNumberUtil.extractOrderNumber("2013-1-000-00001-7"));
    }

    @Test
    public void testExtractYear() {
        assertEquals(2013, PermitNumberUtil.extractYear("2013-1-987-12345-7"));
        assertEquals(2019, PermitNumberUtil.extractYear("2019-1-987-12345-7"));
    }
}
