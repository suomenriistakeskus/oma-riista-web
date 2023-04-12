package fi.riista.util;

import org.junit.Test;

import static fi.riista.util.NumberUtils.isInRange;
import static fi.riista.util.NumberUtils.nullableDoubleSubtraction;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NumberUtilsTest {

    private static final Integer NULL = null;
    private static final Integer ONE = Integer.valueOf(1);

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
    public void testNullableIntSum_consumingVararg() {
        assertNull(nullableIntSum());
        assertNull(nullableIntSum(NULL));
        assertNull(nullableIntSum(NULL, NULL, NULL));

        assertEquals(Integer.valueOf(1), nullableIntSum(ONE));
        assertEquals(Integer.valueOf(1), nullableIntSum(NULL, ONE, NULL));
        assertEquals(Integer.valueOf(3), nullableIntSum(ONE, ONE, ONE));
    }

    @Test
    public void testNullableIntSum_consumingTwoObjectsAndIntegerFunction() {
        assertNull(nullableIntSum(null, null, IntegerHolder::value));
        assertNull(nullableIntSum(null, IntegerHolder.NULL, IntegerHolder::value));
        assertNull(nullableIntSum(IntegerHolder.NULL, null, IntegerHolder::value));
        assertNull(nullableIntSum(IntegerHolder.NULL, IntegerHolder.NULL, IntegerHolder::value));

        assertEquals(ONE, nullableIntSum(null, IntegerHolder.ONE, IntegerHolder::value));
        assertEquals(ONE, nullableIntSum(IntegerHolder.NULL, IntegerHolder.ONE, IntegerHolder::value));

        assertEquals(ONE, nullableIntSum(IntegerHolder.ONE, null, IntegerHolder::value));
        assertEquals(ONE, nullableIntSum(IntegerHolder.ONE, IntegerHolder.NULL, IntegerHolder::value));

        assertEquals(Integer.valueOf(3), nullableIntSum(IntegerHolder.ONE, new IntegerHolder(2), IntegerHolder::value));
    }

    @Test
    public void testNullableIntSum_consumingCollectionOfObjectsAndIntegerFunction() {
        assertNull(nullableIntSum(asList(), IntegerHolder::value));
        assertNull(nullableIntSum(asList(IntegerHolder.NULL), IntegerHolder::value));
        assertNull(nullableIntSum(asList(IntegerHolder.NULL, IntegerHolder.NULL), IntegerHolder::value));

        assertEquals(ONE, nullableIntSum(asList(IntegerHolder.ONE), IntegerHolder::value));
        assertEquals(ONE, nullableIntSum(asList(IntegerHolder.NULL, IntegerHolder.ONE), IntegerHolder::value));
        assertEquals(ONE, nullableIntSum(asList(IntegerHolder.ONE, IntegerHolder.NULL), IntegerHolder::value));

        assertEquals(Integer.valueOf(2), nullableIntSum(asList(IntegerHolder.ONE, IntegerHolder.ONE), IntegerHolder::value));
        assertEquals(Integer.valueOf(3), nullableIntSum(IntegerHolder.ONE, new IntegerHolder(2), IntegerHolder::value));
        assertEquals(Integer.valueOf(3), nullableIntSum(asList(IntegerHolder.ONE, IntegerHolder.ONE, IntegerHolder.ONE), IntegerHolder::value));
    }

    @Test
    public void testNullableDoubleSubtraction() {
        assertNull(nullableDoubleSubtraction(null, null));
        assertNull(nullableDoubleSubtraction(1.0, null));
        assertNull(nullableDoubleSubtraction(null, 1.0));
        assertEquals(Double.valueOf(1.0999999999999996),
                     nullableDoubleSubtraction(Double.valueOf(3.3), Double.valueOf(2.2)));
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
