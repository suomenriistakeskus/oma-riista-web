package fi.riista.validation;

import org.junit.Test;
import org.mockito.Mock;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BicValidatorTest {

    @Mock
    private ConstraintValidatorContext ctx;

    @Test
    public void testValidFinnish() {
        BicValidator validator = new BicValidator();
        assertTrue(validator.isValid("OKOYFIHH", ctx));
        assertFalse(validator.isValid("DEUTDEFF", ctx));
    }

    @Test
    public void testValidForeign() {
        BicValidator validator = new BicValidator(false);
        assertTrue(validator.isValid("OKOYFIHH", ctx));
        assertTrue(validator.isValid("DEUTDEFF", ctx));
    }

    @Test
    public void testInvalidLength() {
        BicValidator validator = new BicValidator(false);
        assertFalse(validator.isValid("OKOYFIH", ctx));
        assertFalse(validator.isValid("OKOYFIHHH", ctx));
        assertFalse(validator.isValid("DEUTDEF", ctx));
        assertFalse(validator.isValid("DEUTDEFFF", ctx));
    }
}