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
public class FinnishCreditorReferenceValidatorTest {

    @Mock
    private ConstraintValidatorContext ctx;
    private FinnishCreditorReferenceValidator validator;

    @Before
    public void setUp() {
        this.validator = new FinnishCreditorReferenceValidator();
    }

    @Test
    public void testValid() {
        assertTrue(validator.isValid("1570 70010 00012", ctx));
        assertTrue(validator.isValid("15707000790019", ctx));

        assertTrue(validator.isValid("1 23453", ctx));
        assertTrue(validator.isValid("1 23466", ctx));
        assertTrue(validator.isValid("1 23479", ctx));
        assertTrue(validator.isValid("1 23482", ctx));
        assertTrue(validator.isValid("1 23495", ctx));
        assertTrue(validator.isValid("1 23505", ctx));
        assertTrue(validator.isValid("1 23518", ctx));
        assertTrue(validator.isValid("1 23521", ctx));
        assertTrue(validator.isValid("1 23534", ctx));
        assertTrue(validator.isValid("1 23547", ctx));


        assertTrue(validator.isValid("1232", ctx));
        assertTrue(validator.isValid("12344", ctx));
        assertTrue(validator.isValid("123453", ctx));
        assertTrue(validator.isValid("1234561", ctx));
        assertTrue(validator.isValid("12345672", ctx));
        assertTrue(validator.isValid("123456780", ctx));
        assertTrue(validator.isValid("1234567897", ctx));
        assertTrue(validator.isValid("12345678907", ctx));
        assertTrue(validator.isValid("12345678901234567894", ctx));
    }

    @Test
    public void testInvalid() {
        assertFalse(validator.isValid("1570 70010 00011", ctx));
        assertFalse(validator.isValid("1570 70010 00013", ctx));
    }

    @Test
    public void testInvalidLength() {
        assertFalse(validator.isValid("123", ctx));
        assertFalse(validator.isValid("0123456789 01234567890 0", ctx));
    }

    @Test
    public void testInvalidCharacter() {
        assertFalse(validator.isValid("a0003567", ctx));
        assertFalse(validator.isValid("9000356a", ctx));
    }
}
