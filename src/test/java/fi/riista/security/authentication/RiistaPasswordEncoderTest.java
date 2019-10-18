package fi.riista.security.authentication;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RiistaPasswordEncoderTest {
    private final RiistaPasswordEncoder passwordEncoder = new RiistaPasswordEncoder();

    private final String plainTextShortPassword = "password";
    private final String plainTextLongPassword = "password12345password";

    private String encodedLongPassword;
    private String encodedShortPassword;

    @Before
    public void encodeTestPassword() {
        encodedLongPassword = passwordEncoder.encode(plainTextLongPassword);
        encodedShortPassword = passwordEncoder.encode(plainTextLongPassword);
    }

    @Test
    public void testNullEncodedFails() {
        assertFalse(passwordEncoder.matches(null, null));
        assertFalse(passwordEncoder.matches(null, ""));
        assertFalse(passwordEncoder.matches(null, encodedLongPassword));
        assertFalse(passwordEncoder.matches(null, encodedShortPassword));
        assertFalse(passwordEncoder.matches(null, plainTextLongPassword));
        assertFalse(passwordEncoder.matches(null, plainTextShortPassword));
    }

    @Test
    public void testEmptyEncodedFails() {
        assertFalse(passwordEncoder.matches("", null));
        assertFalse(passwordEncoder.matches("", ""));
        assertFalse(passwordEncoder.matches("", encodedLongPassword));
        assertFalse(passwordEncoder.matches("", encodedShortPassword));
        assertFalse(passwordEncoder.matches("", plainTextLongPassword));
        assertFalse(passwordEncoder.matches("", plainTextShortPassword));
    }

    @Test
    public void testBcryptWrongPaswordDoesNotWork() {
        assertFalse(passwordEncoder.matches("wrong", encodedLongPassword));
    }

    @Test
    public void testBcryptWorks() {
        assertTrue(passwordEncoder.matches(plainTextLongPassword, encodedLongPassword));
    }

    @Test
    public void testBcryptDoesNotWorkAsPlainText() {
        assertFalse(passwordEncoder.matches(encodedLongPassword, encodedLongPassword));
    }

    @Test
    public void testBcryptPrefixDoesNotWorkAsPlainText() {
        final String encoded = "$2aabcdefg";
        assertFalse(passwordEncoder.matches(encoded, encoded));
    }

    @Test
    public void testShortPlaintTextDoesNotWork() {
        assertFalse(passwordEncoder.matches(plainTextShortPassword, "plaintext:" + plainTextShortPassword));
    }

    @Test
    public void testLongPlaintTextWorks() {
        assertTrue(passwordEncoder.matches(plainTextLongPassword, "plaintext:" + plainTextLongPassword));
    }
}
