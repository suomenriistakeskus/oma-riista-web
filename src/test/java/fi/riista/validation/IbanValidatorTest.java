package fi.riista.validation;

import org.junit.Test;
import org.mockito.Mock;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IbanValidatorTest {

    @Mock
    private ConstraintValidatorContext ctx;

    @Test
    public void testValidFinnish() {
        IbanValidator validator = new IbanValidator();
        assertTrue(validator.isValid("FI2950000121502875", ctx));
        assertTrue(validator.isValid("FI29 5000 0121 5028 75", ctx));
        assertFalse(validator.isValid("DE89370400440532013000", ctx));
        assertFalse(validator.isValid("DE89 3704 0044 0532 0130 00", ctx));
    }

    @Test
    public void testValidForeign() {
        IbanValidator validator = new IbanValidator(false);
        assertTrue(validator.isValid("FI2950000121502875", ctx));
        assertTrue(validator.isValid("FI29 5000 0121 5028 75", ctx));
        assertTrue(validator.isValid("DE89370400440532013000", ctx));
        assertTrue(validator.isValid("DE89 3704 0044 0532 0130 00", ctx));
    }

    @Test
    public void testInvalidLength() {
        IbanValidator validator = new IbanValidator(false);
        assertFalse(validator.isValid("FI29500001215028750", ctx));
        assertFalse(validator.isValid("FI295000012150287", ctx));
        assertFalse(validator.isValid("DE89 3704 0044 0532 0130 0", ctx));
    }
}