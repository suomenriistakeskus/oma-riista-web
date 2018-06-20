package fi.riista.feature.common.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CreditorReferenceConverterTest {

    final CreditorReferenceConverter converter = new CreditorReferenceConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToDatabaseColumn(""));
        assertNull(converter.convertToDatabaseColumn(" "));
        assertNull(converter.convertToDatabaseColumn("   "));

        assertEquals("123456", converter.convertToDatabaseColumn("123456"));
        assertEquals("123456", converter.convertToDatabaseColumn("  123456 "));
        assertEquals("123456", converter.convertToDatabaseColumn("1 23 456"));
        assertEquals("123456", converter.convertToDatabaseColumn(" 1 23456  "));
    }

    @Test
    public void testConvertToEntityAttribute() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
        assertNull(converter.convertToEntityAttribute("   "));

        assertEquals("1 23456", converter.convertToEntityAttribute("123456"));
        assertEquals("1 23456", converter.convertToEntityAttribute("  123456 "));
        assertEquals("1 23456", converter.convertToEntityAttribute("1 23 456"));
        assertEquals("1 23456", converter.convertToEntityAttribute(" 1 23456  "));
    }
}
