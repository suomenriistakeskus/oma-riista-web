package fi.riista.util;

import org.junit.Test;

import static fi.riista.util.NumberUtils.isInRange;
import static fi.riista.util.NumberUtils.nullsafeSum;
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NumberUtilsTest {

    @Test
    public void testIsInRange_double() {
        assertFalse(isInRange(0.5, 1.0, 3.0));
        assertTrue(isInRange(1.0, 1.0, 3.0));
        assertTrue(isInRange(3.0, 2.0, 3.0));
        assertTrue(isInRange(3.0, 1.0, 3.0));
        assertFalse(isInRange(3.5, 1.0, 3.0));
    }

    @Test
    public void testIsInRange_doubleTolerance() {
        assertFalse(isInRange(0.9999, 1.0, 3.0));
        assertTrue(isInRange(0.99999, 1.0, 3.0));

        assertFalse(isInRange(3.00001, 1.0, 3.0));
        assertTrue(isInRange(3.000001, 1.0, 3.0));
    }

    @Test
    public void testNullsafeSum_withIntegerVararg() {
        assertNull(nullsafeSum(IntegerHolder::value));
        assertNull(nullsafeSum(IntegerHolder::value, IntegerHolder.NULL));
        assertNull(nullsafeSum(IntegerHolder::value, IntegerHolder.NULL, IntegerHolder.NULL));
        assertNull(nullsafeSum(IntegerHolder::value, IntegerHolder.NULL, IntegerHolder.NULL, IntegerHolder.NULL));

        assertEquals(Integer.valueOf(1), nullsafeSum(IntegerHolder::value, IntegerHolder.NULL, IntegerHolder.ONE));
        assertEquals(Integer.valueOf(1), nullsafeSum(IntegerHolder::value, IntegerHolder.ONE, IntegerHolder.NULL));
        assertEquals(Integer.valueOf(2), nullsafeSum(IntegerHolder::value, IntegerHolder.ONE, IntegerHolder.ONE));

        assertEquals(
                Integer.valueOf(3),
                nullsafeSum(IntegerHolder::value, IntegerHolder.ONE, IntegerHolder.NULL, new IntegerHolder(2)));
    }

    @Test
    public void testNullsafeSumAsInt() {
        assertNull(nullsafeSumAsInt(null, null, IntegerHolder::value));
        assertNull(nullsafeSumAsInt(null, IntegerHolder.NULL, IntegerHolder::value));
        assertNull(nullsafeSumAsInt(IntegerHolder.NULL, null, IntegerHolder::value));
        assertNull(nullsafeSumAsInt(IntegerHolder.NULL, IntegerHolder.NULL, IntegerHolder::value));

        assertEquals(Integer.valueOf(1), nullsafeSumAsInt(null, IntegerHolder.ONE, IntegerHolder::value));
        assertEquals(Integer.valueOf(1), nullsafeSumAsInt(IntegerHolder.NULL, IntegerHolder.ONE, IntegerHolder::value));

        assertEquals(Integer.valueOf(1), nullsafeSumAsInt(IntegerHolder.ONE, null, IntegerHolder::value));
        assertEquals(Integer.valueOf(1), nullsafeSumAsInt(IntegerHolder.ONE, IntegerHolder.NULL, IntegerHolder::value));

        assertEquals(Integer.valueOf(3), nullsafeSumAsInt(IntegerHolder.ONE, new IntegerHolder(2), IntegerHolder::value));
    }

    private static class IntegerHolder {

        private static final IntegerHolder NULL = new IntegerHolder(null);
        private static final IntegerHolder ONE = new IntegerHolder(1);

        private final Integer val;

        IntegerHolder(final Integer val) {
            this.val = val;
        }

        public Integer value() {
            return val;
        }
    }
}
