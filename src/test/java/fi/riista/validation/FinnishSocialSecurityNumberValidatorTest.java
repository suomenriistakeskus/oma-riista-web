package fi.riista.validation;

import org.joda.time.LocalDate;
import org.junit.Test;

import static fi.riista.validation.FinnishSocialSecurityNumberValidator.parseBirthDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FinnishSocialSecurityNumberValidatorTest {
    @Test
    public void testSmoke() {
        assertTrue(Validators.isValidSsn("111111-1023"));

        assertTrue(Validators.isValidSsn("010594Y9032"));
        assertTrue(Validators.isValidSsn("010594Y9021"));
        assertTrue(Validators.isValidSsn("020594X903P"));
        assertTrue(Validators.isValidSsn("020594X902N"));
        assertTrue(Validators.isValidSsn("030594W903B"));
        assertTrue(Validators.isValidSsn("030694W9024"));
        assertTrue(Validators.isValidSsn("040594V9030"));
        assertTrue(Validators.isValidSsn("040594V902Y"));
        assertTrue(Validators.isValidSsn("050594U903M"));
        assertTrue(Validators.isValidSsn("050594U902L"));
        assertTrue(Validators.isValidSsn("010516B903X"));
        assertTrue(Validators.isValidSsn("010516B902W"));
        assertTrue(Validators.isValidSsn("020516C903K"));
        assertTrue(Validators.isValidSsn("020516C902J"));
        assertTrue(Validators.isValidSsn("030516D9037"));
        assertTrue(Validators.isValidSsn("030516D9026"));
        assertTrue(Validators.isValidSsn("010501E9032"));
        assertTrue(Validators.isValidSsn("020502E902X"));
        assertTrue(Validators.isValidSsn("020503F9037"));
        assertTrue(Validators.isValidSsn("020504A902E"));
        assertTrue(Validators.isValidSsn("020504B904H"));
        assertTrue(Validators.isValidSsn("241200F9039"));
    }

    @Test
    public void testInvalidChecksum() {
        assertFalse(Validators.isValidSsn("111111-1024"));
    }

    @Test
    public void testInvalidDate() {
        assertFalse(Validators.isValidSsn("310499-001T"));
    }

    @Test
    public void testParseBirthDate() {
        assertEquals(new LocalDate(2004, 7, 15), parseBirthDate("150704A7994"));
        assertEquals(new LocalDate(1910, 8, 22), parseBirthDate("220810-981V"));

        assertEquals(new LocalDate(1994, 5, 1), parseBirthDate("010594Y9032"));
        assertEquals(new LocalDate(1994, 5, 2), parseBirthDate("020594X903P"));
        assertEquals(new LocalDate(1994, 5, 3), parseBirthDate("030594W903B"));
        assertEquals(new LocalDate(1994, 5, 4), parseBirthDate("040594V9030"));
        assertEquals(new LocalDate(1994, 5, 5), parseBirthDate("050594U903M"));
        assertEquals(new LocalDate(2016, 5, 1), parseBirthDate("010516B903X"));
        assertEquals(new LocalDate(2016, 5, 2), parseBirthDate("020516C903K"));
        assertEquals(new LocalDate(2016, 5, 3), parseBirthDate("030516D9037"));
        assertEquals(new LocalDate(2001, 5, 1), parseBirthDate("010501E9032"));
        assertEquals(new LocalDate(2003, 5, 2), parseBirthDate("020503F9037"));
        assertEquals(new LocalDate(2000, 12, 24), parseBirthDate("241200F9039"));
    }
}
