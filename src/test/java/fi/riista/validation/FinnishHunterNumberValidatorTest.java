package fi.riista.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FinnishHunterNumberValidatorTest {

    @Test
    public void testValid() {
        assertTrue(Validators.isValidHunterNumber("90003567"));
        assertTrue(Validators.isValidHunterNumber("43145812"));
    }

    @Test
    public void testInvalid() {
        assertFalse(Validators.isValidHunterNumber("90003560"));
        assertFalse(Validators.isValidHunterNumber("90003561"));
        assertFalse(Validators.isValidHunterNumber("90003562"));
        assertFalse(Validators.isValidHunterNumber("90003563"));
        assertFalse(Validators.isValidHunterNumber("90003564"));
        assertFalse(Validators.isValidHunterNumber("90003565"));
        assertFalse(Validators.isValidHunterNumber("90003566"));
        assertFalse(Validators.isValidHunterNumber("90003568"));
        assertFalse(Validators.isValidHunterNumber("90003569"));
    }

    @Test
    public void testInvalidLength() {
        assertFalse(Validators.isValidHunterNumber("900035678"));
        assertFalse(Validators.isValidHunterNumber("9000356"));
    }

    @Test
    public void testInvalidCharacter() {
        assertFalse(Validators.isValidHunterNumber("a0003567"));
        assertFalse(Validators.isValidHunterNumber("9000356a"));
    }
}
