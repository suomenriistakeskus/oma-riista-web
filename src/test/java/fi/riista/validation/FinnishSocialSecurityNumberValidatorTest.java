package fi.riista.validation;

import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.validation.FinnishSocialSecurityNumberValidator.parseBirthDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FinnishSocialSecurityNumberValidatorTest {
    @Test
    public void testSmoke() {
        assertTrue(Validators.isValidSsn("111111-1023"));
    }

    @Test
    public void testInvalidChecksum() {
        assertFalse(Validators.isValidSsn("111111-1024"));
    }

    @Test
    public void testInvalidDate() {
        assertFalse(Validators.isValidSsn("310499-001T"));
    }

    @Test
    public void testParseBirthDate() {
        assertEquals(new LocalDate(2004, 7, 15), parseBirthDate("150704A7994"));
        assertEquals(new LocalDate(1910, 8, 22), parseBirthDate("220810-981V"));
    }
}
