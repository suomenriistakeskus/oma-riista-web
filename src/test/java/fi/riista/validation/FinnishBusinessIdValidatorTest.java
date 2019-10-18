package fi.riista.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FinnishBusinessIdValidatorTest {

    @Test
    public void test() {
        assertTrue(Validators.isValidBusinessId("2222222-9"));
        assertTrue(Validators.isValidBusinessId("3333333-8"));

        assertFalse(Validators.isValidBusinessId("2222222-0"));
        assertFalse(Validators.isValidBusinessId("1111111-1"));
    }

    @Test
    public void testNullOrEmptyIsValid(){
        assertTrue(Validators.isValidBusinessId(null));
        assertTrue(Validators.isValidBusinessId(""));
    }

    @Test
    public void testFormat() {

        // Length
        assertFalse(FinnishBusinessIdValidator.validate("111", false));
        assertFalse(FinnishBusinessIdValidator.validate("1111111-1111", false));


        // Pattern
        assertFalse(FinnishBusinessIdValidator.validate("1111111F1", false));
        assertFalse(FinnishBusinessIdValidator.validate("FFFFFFF-1", false));
        assertFalse(FinnishBusinessIdValidator.validate("1111111-F", false));

    }

    @Test
    public void testChecksum() {
        assertTrue(FinnishBusinessIdValidator.validate("1111111-2", false));
        assertFalse(FinnishBusinessIdValidator.validate("1111111-2", true));
    }
}
