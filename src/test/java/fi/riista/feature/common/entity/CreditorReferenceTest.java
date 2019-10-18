package fi.riista.feature.common.entity;

import org.junit.Test;

import static fi.riista.feature.common.entity.CreditorReference.getDelimitedValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CreditorReferenceTest {

    @Test
    public void testGetDelimitedValue_nullValue() {
        assertNull(getDelimitedValue(null));
    }

    @Test
    public void testGetDelimitedValue_assertTrimmingDone() {
        assertEquals("", getDelimitedValue(""));
        assertEquals("", getDelimitedValue(" "));
        assertEquals("", getDelimitedValue("  "));

        assertDelimiting("1", generateSurroundingWhitespaceVariants("1"));
        assertDelimiting("12", generateSurroundingWhitespaceVariants("12"));
        assertDelimiting("123", generateSurroundingWhitespaceVariants("123"));
        assertDelimiting("1234", generateSurroundingWhitespaceVariants("1234"));
        assertDelimiting("12345", generateSurroundingWhitespaceVariants("12345"));

        assertDelimiting("1 23456", generateSurroundingWhitespaceVariants("123456"));
        assertDelimiting("1 23456", generateSurroundingWhitespaceVariants("1 23456"));
        assertDelimiting("1 23456", generateSurroundingWhitespaceVariants("12345 6"));
    }

    @Test
    public void testGetDelimitedValue_properDelimitingRegardlessOfInputDelimiters() {
        assertDelimiting("1 23456", "123456", "1 23456", "12345 6");
        assertDelimiting("12 34567", "1234567", "12 34567", "12345 67");
        assertDelimiting("123 45678", "12345678", "123 45678", "12345 678");
        assertDelimiting("1234 56789", "123456789", "1234 56789", "12345 6789");
        assertDelimiting("12345 67890", "1234567890", "12345 67890");
        assertDelimiting("1 23456 78901", "12345678901", "1 23456 78901", "12345 67890 1");
        assertDelimiting("12345 67890 12345", "123456789012345", "12345 67890 12345");
        assertDelimiting("1 23456 78901 23456", "1234567890123456", "1 23456 78901 23456", "12345 67890 12345 6");
    }

    @Test
    public void testGetDelimitedValue_properZeroUnpadding() {
        assertEquals("0", getDelimitedValue("0"));
        assertEquals("0", getDelimitedValue("00"));
        assertEquals("0", getDelimitedValue("000"));
        assertEquals("0", getDelimitedValue("0000"));
        assertEquals("0", getDelimitedValue("00000"));
        assertEquals("0", getDelimitedValue("000000"));
        assertEquals("0", getDelimitedValue("0000000"));
        assertEquals("0", getDelimitedValue("00000000"));
        assertEquals("0", getDelimitedValue("000000000"));
        assertEquals("0", getDelimitedValue("0000000000"));
        assertEquals("0", getDelimitedValue("00000000000"));
        assertEquals("0", getDelimitedValue("000000000000"));

        assertEquals("0", getDelimitedValue("0 00000"));
        assertEquals("0", getDelimitedValue("0000 00000"));
        assertEquals("0", getDelimitedValue("000000 00000"));
        assertEquals("0", getDelimitedValue("0 000000 00000"));

        assertEquals("1", getDelimitedValue("01"));
        assertEquals("1", getDelimitedValue("001"));
        assertEquals("1", getDelimitedValue("0001"));
        assertEquals("1", getDelimitedValue("00001"));
        assertEquals("1", getDelimitedValue("000001"));
        assertEquals("1", getDelimitedValue("0000001"));
        assertEquals("1", getDelimitedValue("00000001"));
        assertEquals("1", getDelimitedValue("000000001"));
        assertEquals("1", getDelimitedValue("0000000001"));
        assertEquals("1", getDelimitedValue("00000000001"));

        assertEquals("1234", getDelimitedValue("00000 01234"));
        assertEquals("12345", getDelimitedValue("00000 12345"));
        assertEquals("1 23456", getDelimitedValue("00001 23456"));
        assertEquals("1 23456", getDelimitedValue("0 00001 23456"));
    }

    @Test
    public void testFromNullable() {
        assertNull(CreditorReference.fromNullable(null));
        assertNull(CreditorReference.fromNullable(" "));
        assertNull(CreditorReference.fromNullable("  "));

        assertEquals("1 45077 00161", CreditorReference.fromNullable("14507700161").getValue());
    }

    private static void assertDelimiting(final String expected, final String... inputs) {
        for (final String input : inputs) {
            assertEquals(expected, getDelimitedValue(input));
        }
    }

    private static String[] generateSurroundingWhitespaceVariants(final String str) {
        return new String[] {
                str,
                " " + str,
                "  " + str,
                str + " ",
                str + "  ",
                " " + str + " ",
                "  " + str + "  "
        };
    }
}
