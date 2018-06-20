package fi.riista.feature.common.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FinnishBusinessIdConverterTest {

    @Test
    public void testFormat() {
        assertEquals(null, FinnishBusinessIdConverter.format(null));
        assertEquals("", FinnishBusinessIdConverter.format(""));
        assertEquals(" ", FinnishBusinessIdConverter.format(" "));
        assertEquals("1", FinnishBusinessIdConverter.format("1"));
        assertEquals("12", FinnishBusinessIdConverter.format("12"));
        assertEquals("123", FinnishBusinessIdConverter.format("123"));
        assertEquals("1234", FinnishBusinessIdConverter.format("1234"));
        assertEquals("12345", FinnishBusinessIdConverter.format("12345"));
        assertEquals("123456", FinnishBusinessIdConverter.format("123456"));
        assertEquals("1234567", FinnishBusinessIdConverter.format("1234567"));
        assertEquals(" 1234567 ", FinnishBusinessIdConverter.format(" 1234567 "));
        assertEquals("1234567-8", FinnishBusinessIdConverter.format("12345678"));
        assertEquals("1234567-8", FinnishBusinessIdConverter.format(" abcd12345678abcd "));
    }
}