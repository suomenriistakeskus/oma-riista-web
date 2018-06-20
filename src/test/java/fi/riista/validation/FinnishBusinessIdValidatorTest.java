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
}