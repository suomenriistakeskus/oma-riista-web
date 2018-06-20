package fi.riista.validation;

import org.junit.Test;

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
}
