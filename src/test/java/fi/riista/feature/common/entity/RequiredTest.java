package fi.riista.feature.common.entity;

import fi.riista.feature.error.RequiredFieldMissing;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static fi.riista.feature.error.RequiredFieldMissing.getDefaultMessageForMissingField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RequiredTest {

    private static final String FIELD_NAME = "fieldName";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNullifyIfNeeded_YES() {
        assertEquals("ok", Required.YES.nullifyIfNeeded("ok", FIELD_NAME));
        assertEquals("", Required.YES.nullifyIfNeeded("", FIELD_NAME));

        thrown.expect(RequiredFieldMissing.class);
        thrown.expectMessage(getDefaultMessageForMissingField(FIELD_NAME));

        Required.YES.nullifyIfNeeded(null, FIELD_NAME);
    }

    @Test
    public void testNullifyIfNeeded_VOLUNTARY() {
        assertEquals("vol", Required.VOLUNTARY.nullifyIfNeeded("vol", FIELD_NAME));
        assertEquals("", Required.VOLUNTARY.nullifyIfNeeded("", FIELD_NAME));
        assertNull(Required.VOLUNTARY.nullifyIfNeeded(null, FIELD_NAME));
    }

    @Test
    public void testNullifyIfNeeded_NO() {
        assertNull(Required.NO.nullifyIfNeeded(null, FIELD_NAME));
        assertNull(Required.NO.nullifyIfNeeded("", FIELD_NAME));
        assertNull(Required.NO.nullifyIfNeeded("all values are returned as null", FIELD_NAME));
    }

    @Test
    public void testIsValidValue_YES() {
        assertTrue(Required.YES.isValidValue("ok"));
        assertTrue(Required.YES.isValidValue(""));
        assertFalse(Required.YES.isValidValue(null));
    }

    @Test
    public void testIsValidValue_VOLUNTARY() {
        assertTrue(Required.VOLUNTARY.isValidValue("ok"));
        assertTrue(Required.VOLUNTARY.isValidValue(""));
        assertTrue(Required.VOLUNTARY.isValidValue(null));
    }

    @Test
    public void testIsValidValue_NO() {
        assertFalse(Required.NO.isValidValue("ok"));
        assertFalse(Required.NO.isValidValue(""));
        assertTrue(Required.NO.isValidValue(null));
    }
}
