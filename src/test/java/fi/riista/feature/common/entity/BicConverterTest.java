package fi.riista.feature.common.entity;

import org.iban4j.Bic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BicConverterTest {
    private static final String TEST_BIC_STRING = "OKOYFIHH";
    private static final Bic TEST_BIC = Bic.valueOf(TEST_BIC_STRING);

    private final BicConverter converter = new BicConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals(TEST_BIC_STRING, converter.convertToDatabaseColumn(TEST_BIC));
    }

    @Test
    public void testConvertToEntityAttribute() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(" "));
        assertEquals(TEST_BIC, converter.convertToEntityAttribute(TEST_BIC_STRING));
        assertEquals(TEST_BIC, converter.convertToEntityAttribute(" " + TEST_BIC_STRING + " "));
    }
}
