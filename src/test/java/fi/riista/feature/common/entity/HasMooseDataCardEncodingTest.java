package fi.riista.feature.common.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javaslang.control.Either;

import org.junit.Test;

import java.util.Optional;

public class HasMooseDataCardEncodingTest {

    private enum TestEnum implements HasMooseDataCardEncoding<TestEnum> {

        A, B, C, D;

        @Override
        public String getMooseDataCardEncoding() {
            switch (this) {
                case A:
                    return "ABC";
                case B:
                    return "def";
                case C:
                    return "GHI";
                default:
                    return null;
            }
        }
    }

    @Test
    public void testEnumOf() {
        assertEquals(Either.right(TestEnum.A), HasMooseDataCardEncoding.enumOf(TestEnum.class, "ABC"));
        assertEquals(Either.right(TestEnum.B), HasMooseDataCardEncoding.enumOf(TestEnum.class, "DEF  "));
        assertEquals(Either.right(TestEnum.C), HasMooseDataCardEncoding.enumOf(TestEnum.class, "  ghi  "));

        assertEquals(Either.left(Optional.empty()), HasMooseDataCardEncoding.enumOf(TestEnum.class, null));
        assertEquals(Either.left(Optional.empty()), HasMooseDataCardEncoding.enumOf(TestEnum.class, ""));
        assertEquals(Either.left(Optional.empty()), HasMooseDataCardEncoding.enumOf(TestEnum.class, "  "));
        assertEquals(Either.left(Optional.of("x")), HasMooseDataCardEncoding.enumOf(TestEnum.class, "x"));
        assertEquals(Either.left(Optional.of("y")), HasMooseDataCardEncoding.enumOf(TestEnum.class, "   y  "));
    }

    @Test
    public void testEqualsMooseDataCardEncoding() {
        assertTrue(TestEnum.A.equalsMooseDataCardEncoding("ABC"));
        assertTrue(TestEnum.B.equalsMooseDataCardEncoding("DEF  "));
        assertTrue(TestEnum.C.equalsMooseDataCardEncoding("  ghi  "));

        assertFalse(TestEnum.A.equalsMooseDataCardEncoding(null));
        assertFalse(TestEnum.D.equalsMooseDataCardEncoding(null));
        assertFalse(TestEnum.D.equalsMooseDataCardEncoding(""));
        assertFalse(TestEnum.D.equalsMooseDataCardEncoding("ABC"));
    }

}
