package fi.riista.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FinnishHunterNumberValidatorTest {

    @Mock
    private ConstraintValidatorContext ctx;
    private FinnishHunterNumberValidator validator;

    @Before
    public void setUp() {
        this.validator = new FinnishHunterNumberValidator();
    }

    @Test
    public void testValid() {
        assertTrue(validator.isValid("90003567", ctx));
        assertTrue(validator.isValid("43145812", ctx));
    }

    @Test
    public void testInvalid() {
        assertFalse(validator.isValid("90003560", ctx));
        assertFalse(validator.isValid("90003561", ctx));
        assertFalse(validator.isValid("90003562", ctx));
        assertFalse(validator.isValid("90003563", ctx));
        assertFalse(validator.isValid("90003564", ctx));
        assertFalse(validator.isValid("90003565", ctx));
        assertFalse(validator.isValid("90003566", ctx));
        assertFalse(validator.isValid("90003568", ctx));
        assertFalse(validator.isValid("90003569", ctx));
    }

    @Test
    public void testInvalidLength() {
        assertFalse(validator.isValid("900035678", ctx));
        assertFalse(validator.isValid("9000356", ctx));
    }

    @Test
    public void testInvalidCharacter() {
        assertFalse(validator.isValid("a0003567", ctx));
        assertFalse(validator.isValid("9000356a", ctx));
    }
}
