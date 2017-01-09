package fi.riista.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PhoneNumberValidatorTest {

    @Test
    public void testFinnishNumberInternationalFormat() throws NumberParseException {
        assertEquals("+358 40 0112233", PhoneNumberValidator.validateAndFormat("+358 400 112233"));
    }

    @Test
    public void testFinnishNumberInternationalFormatWithExtraZero() throws NumberParseException {
        assertEquals("+358 40 0112233", PhoneNumberValidator.validateAndFormat("+358 0400 112233"));
    }

    @Test
    public void testFinnishNumberLocalFormat() throws NumberParseException {
        assertEquals("+358 40 0112233", PhoneNumberValidator.validateAndFormat("0400-112 233"));
    }

    @Test
    public void testOtherInternationalNumberFormat() throws NumberParseException {
        assertEquals("+47 450 11 234", PhoneNumberValidator.validateAndFormat("+47 45011234"));
    }

    @Test(expected = NumberParseException.class)
    public void testInvalidNumber() throws NumberParseException {
        PhoneNumberValidator.validateAndFormat("04001133eivalidi");
    }
}
