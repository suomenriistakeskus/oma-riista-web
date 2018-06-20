package fi.riista.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FTest {

    private enum TestEnum {
        A, B, C;
    }

    private static class TestEnumHolder {
        private final TestEnum value;

        TestEnumHolder(@Nullable final TestEnum value) {
            this.value = value;
        }

        public TestEnum getValue() {
            return value;
        }
    }

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

    @Test
    public void testFilterToEnumSet() {
        assertEquals(EnumSet.noneOf(TestEnum.class), F.filterToEnumSet(TestEnum.class, t -> false));
        assertEquals(EnumSet.allOf(TestEnum.class), F.filterToEnumSet(TestEnum.class, t -> true));
        assertEquals(EnumSet.of(TestEnum.C), F.filterToEnumSet(TestEnum.class, TestEnum.C::equals));
        assertEquals(EnumSet.of(TestEnum.A, TestEnum.B), F.filterToEnumSet(TestEnum.class, t -> t != TestEnum.C));
    }

    @Test
    public void testMax() {
        final TestEnumHolder holdsA = new TestEnumHolder(TestEnum.A);
        final TestEnumHolder holdsB = new TestEnumHolder(TestEnum.B);
        final TestEnumHolder holdsC = new TestEnumHolder(TestEnum.C);
        final TestEnumHolder holdsNull = new TestEnumHolder(null);

        assertEquals(TestEnum.C, F.max(asList(holdsA, holdsB, holdsC), TestEnumHolder::getValue));
        assertEquals(TestEnum.A, F.max(asList(holdsA, holdsNull, null), TestEnumHolder::getValue));
        assertNull(F.max(asList(holdsNull, null), TestEnumHolder::getValue));
        assertNull(F.max(emptyList(), TestEnumHolder::getValue));
    }

    @Test
    public void testNullsafeMax() {
        final TestEnumHolder holdsA = new TestEnumHolder(TestEnum.A);
        final TestEnumHolder holdsB = new TestEnumHolder(TestEnum.B);
        final TestEnumHolder holdsC = new TestEnumHolder(TestEnum.C);
        final TestEnumHolder holdsNull = new TestEnumHolder(null);

        assertEquals(TestEnum.B, F.nullsafeMax(holdsA, holdsB, TestEnumHolder::getValue));
        assertEquals(TestEnum.C, F.nullsafeMax(holdsC, holdsA, TestEnumHolder::getValue));

        assertEquals(TestEnum.A, F.nullsafeMax(holdsA, holdsNull, TestEnumHolder::getValue));
        assertEquals(TestEnum.B, F.nullsafeMax(holdsNull, holdsB, TestEnumHolder::getValue));

        assertEquals(TestEnum.B, F.nullsafeMax(holdsB, null, TestEnumHolder::getValue));
        assertEquals(TestEnum.C, F.nullsafeMax(null, holdsC, TestEnumHolder::getValue));

        assertNull(F.nullsafeMax(holdsNull, holdsNull, TestEnumHolder::getValue));
        assertNull(F.nullsafeMax(null, holdsNull, TestEnumHolder::getValue));
        assertNull(F.nullsafeMax(holdsNull, null, TestEnumHolder::getValue));
        assertNull(F.nullsafeMax(null, null, TestEnumHolder::getValue));
    }

    @Test
    public void testCountByApplication() {
        final List<String> names = asList("Bob", "Dylan", "James", "Jeff", "Joe", "Leroy");

        assertEquals(ImmutableMap.of(3, 2L, 4, 1L, 5, 3L), F.countByApplication(names.stream(), String::length));
    }
}
