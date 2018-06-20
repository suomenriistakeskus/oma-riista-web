package fi.riista.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FinnishHuntingPermitNumberValidatorTest {

    @Test
    public void testValid() {
        assertTrue(Validators.isValidPermitNumber("2013-3-450-00260-2"));
        assertTrue(Validators.isValidPermitNumber("2013-1-010-12345-7"));
        assertTrue(Validators.isValidPermitNumber("2014-1-123-12345-7"));
    }

    @Test
    public void testInvalid() {
        assertFalse(Validators.isValidPermitNumber("0013-3-450-00260-2"));
        assertFalse(Validators.isValidPermitNumber(" "));
    }

    @Test
    public void testInvalidLength() {
        assertFalse(Validators.isValidPermitNumber("2013-3-450-00260"));
        assertFalse(Validators.isValidPermitNumber("3-450-00260-2"));
    }

    @Test
    public void testInvalidCharacter() {
        assertFalse(Validators.isValidPermitNumber("a013-3-450-00260-2"));
        assertFalse(Validators.isValidPermitNumber("2013-a-450-00260-2"));
    }
}
