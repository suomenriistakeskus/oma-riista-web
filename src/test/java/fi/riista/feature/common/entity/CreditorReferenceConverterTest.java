package fi.riista.feature.common.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreditorReferenceConverterTest {

    CreditorReferenceConverter converter = new CreditorReferenceConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        assertEquals(null, converter.convertToDatabaseColumn(null));
        assertEquals(null, converter.convertToDatabaseColumn(" "));
        assertEquals("123456", converter.convertToDatabaseColumn("123456"));
        assertEquals("123456", converter.convertToDatabaseColumn("1 23 456"));
    }

    @Test
    public void testConvertToEntityAttribute() {
        assertEquals(null, converter.convertToEntityAttribute(null));
        assertEquals(null, converter.convertToEntityAttribute(" "));
        assertEquals("12345 6", converter.convertToEntityAttribute("123456"));
        assertEquals("12345 6", converter.convertToEntityAttribute("1 23 456"));
    }
}