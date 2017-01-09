package fi.riista.feature.common.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IbanConverterTest {

    IbanConverter converter = new IbanConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        assertEquals(null, converter.convertToDatabaseColumn(null));
        assertEquals(null, converter.convertToDatabaseColumn(" "));
        assertEquals("FI2950000121502875", converter.convertToDatabaseColumn("FI29 5000 0121 5028 75"));
    }

    @Test
    public void testConvertToEntityAttribute() {
        assertEquals(null, converter.convertToEntityAttribute(null));
        assertEquals(null, converter.convertToEntityAttribute(" "));
        assertEquals("FI29 5000 0121 5028 75", converter.convertToEntityAttribute("FI2950000121502875"));
    }
}