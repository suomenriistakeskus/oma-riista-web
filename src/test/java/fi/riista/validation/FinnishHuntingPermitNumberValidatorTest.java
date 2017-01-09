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
public class FinnishHuntingPermitNumberValidatorTest {

    @Mock
    private ConstraintValidatorContext ctx;
    private FinnishHuntingPermitNumberValidator validator;

    @Before
    public void setUp() {
        this.validator = new FinnishHuntingPermitNumberValidator();
    }

    @Test
    public void testValid() {
        assertTrue(validator.isValid("2013-3-450-00260-2", ctx));
        assertTrue(validator.isValid("2013-1-010-12345-7", ctx));
        assertTrue(validator.isValid("2014-1-123-12345-7", ctx));
    }

    @Test
    public void testInvalid() {
        assertFalse(validator.isValid("0013-3-450-00260-2", ctx));
        assertFalse(validator.isValid(" ", ctx));
    }

    @Test
    public void testInvalidLength() {
        assertFalse(validator.isValid("2013-3-450-00260", ctx));
        assertFalse(validator.isValid("3-450-00260-2", ctx));
    }

    @Test
    public void testInvalidCharacter() {
        assertFalse(validator.isValid("a013-3-450-00260-2", ctx));
        assertFalse(validator.isValid("2013-a-450-00260-2", ctx));
    }
}
