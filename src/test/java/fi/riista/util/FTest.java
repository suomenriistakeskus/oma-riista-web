package fi.riista.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FTest {

    @Test
    public void testFirstNonNull() {
        final Integer obj = 1;

        assertNull(F.firstNonNull());
        assertNull(F.firstNonNull((Object[]) null));
        assertNull(F.firstNonNull(null, null));
        assertEquals(obj, F.firstNonNull(obj));
        assertEquals(obj, F.firstNonNull(obj, null));
        assertEquals(obj, F.firstNonNull(null, obj));
        assertEquals(obj, F.firstNonNull(null, obj, null));
    }

    @Test
    public void testAnyNull() {
        final Integer obj = 1;

        assertFalse(F.anyNull());
        assertTrue(F.anyNull((Object[]) null));
        assertTrue(F.anyNull(null, null));
        assertTrue(F.anyNull(null, obj));
        assertTrue(F.anyNull(obj, null));
        assertFalse(F.anyNull(obj, Integer.valueOf(2)));
    }

    @Test
    public void testAnyNonNull() {
        final Integer obj = 1;

        assertFalse(F.anyNonNull());
        assertFalse(F.anyNonNull((Object[]) null));
        assertFalse(F.anyNonNull(null, null));
        assertTrue(F.anyNonNull(null, obj));
        assertTrue(F.anyNonNull(obj, null));
        assertTrue(F.anyNonNull(obj, Integer.valueOf(2)));
    }

    @Test
    public void testAllNull() {
        final Integer obj = 1;

        assertFalse(F.allNull());
        assertTrue(F.allNull((Object[]) null));
        assertTrue(F.allNull(null, null));
        assertFalse(F.allNull(null, obj));
        assertFalse(F.allNull(obj, null));
        assertFalse(F.allNull(obj, Integer.valueOf(2)));
    }

    @Test
    public void testAllNotNull() {
        final Integer obj = 1;

        assertFalse(F.allNotNull());
        assertFalse(F.allNotNull((Object[]) null));
        assertFalse(F.allNotNull(null, null));
        assertFalse(F.allNotNull(null, obj));
        assertFalse(F.allNotNull(obj, null));
        assertTrue(F.allNotNull(obj, Integer.valueOf(2)));
    }

}
