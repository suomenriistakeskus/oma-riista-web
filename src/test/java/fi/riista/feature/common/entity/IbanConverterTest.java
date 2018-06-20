package fi.riista.feature.common.entity;

import org.iban4j.Iban;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IbanConverterTest {

    private static final String TEST_IBAN_FORMATTED = "FI29 5000 0121 5028 75";
    private static final String TEST_IBAN_UNFORMATTED = TEST_IBAN_FORMATTED.replaceAll(" ", "");
    private static final Iban TEST_IBAN = Iban.valueOf(TEST_IBAN_UNFORMATTED);

    private final IbanConverter converter = new IbanConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals(TEST_IBAN_UNFORMATTED, converter.convertToDatabaseColumn(TEST_IBAN));
    }

    @Test
    public void testConvertToEntityAttribute() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(" "));
        assertEquals(TEST_IBAN, converter.convertToEntityAttribute(TEST_IBAN_FORMATTED));
        assertEquals(TEST_IBAN, converter.convertToEntityAttribute(" " + TEST_IBAN_FORMATTED + " "));
        assertEquals(TEST_IBAN, converter.convertToEntityAttribute(" " + TEST_IBAN_UNFORMATTED + " "));
    }
}
