package fi.riista.feature.common.entity;

import javaslang.control.Either;

import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    public void testEitherInvalidOrValid() {
        assertEquals(Either.right(TestEnum.A), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, "ABC"));
        assertEquals(Either.right(TestEnum.B), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, "DEF  "));
        assertEquals(Either.right(TestEnum.C), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, "  gHi  "));

        assertEquals(Either.left(Optional.empty()), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, null));
        assertEquals(Either.left(Optional.empty()), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, ""));
        assertEquals(Either.left(Optional.empty()), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, "  "));
        assertEquals(Either.left(Optional.of("x")), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, "x"));
        assertEquals(Either.left(Optional.of("y")), HasMooseDataCardEncoding.eitherInvalidOrValid(TestEnum.class, "   y  "));
    }

    @Test
    public void testFindEnum() {
        assertEquals(Optional.of(TestEnum.A), HasMooseDataCardEncoding.findEnum(TestEnum.class, "ABC"));
        assertEquals(Optional.of(TestEnum.B), HasMooseDataCardEncoding.findEnum(TestEnum.class, "DEF  "));
        assertEquals(Optional.of(TestEnum.C), HasMooseDataCardEncoding.findEnum(TestEnum.class, "  gHi  "));

        assertEquals(Optional.empty(), HasMooseDataCardEncoding.findEnum(TestEnum.class, null));
        assertEquals(Optional.empty(), HasMooseDataCardEncoding.findEnum(TestEnum.class, ""));
        assertEquals(Optional.empty(), HasMooseDataCardEncoding.findEnum(TestEnum.class, "  "));
        assertEquals(Optional.empty(), HasMooseDataCardEncoding.findEnum(TestEnum.class, "x"));
        assertEquals(Optional.empty(), HasMooseDataCardEncoding.findEnum(TestEnum.class, "   y  "));
    }

    @Test
    public void testGetEnumOrNull() {
        assertEquals(TestEnum.A, HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, "ABC"));
        assertEquals(TestEnum.B, HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, "DEF  "));
        assertEquals(TestEnum.C, HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, "  gHi  "));

        assertNull(HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, null));
        assertNull(HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, ""));
        assertNull(HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, "  "));
        assertNull(HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, "x"));
        assertNull(HasMooseDataCardEncoding.getEnumOrNull(TestEnum.class, "   y  "));
    }

    @Test
    public void testGetEnum() {
        assertEquals(TestEnum.A, HasMooseDataCardEncoding.getEnum(TestEnum.class, "ABC"));
        assertEquals(TestEnum.B, HasMooseDataCardEncoding.getEnum(TestEnum.class, "DEF  "));
        assertEquals(TestEnum.C, HasMooseDataCardEncoding.getEnum(TestEnum.class, "  gHi  "));

        Stream.of(null, "", "  ", "x", "   y  ").forEach(invalid -> {
            try {
                HasMooseDataCardEncoding.getEnum(TestEnum.class, invalid);
                fail("Should have thrown exception for " +
                        Optional.ofNullable(invalid).map(s -> '"' + s + '"').orElse("null"));
            } catch (final RuntimeException e) {
                // expected
            }
        });
    }

    @Test
    public void testEqualsMooseDataCardEncoding() {
        assertTrue(TestEnum.A.equalsMooseDataCardEncoding("ABC"));
        assertTrue(TestEnum.B.equalsMooseDataCardEncoding("DEF  "));
        assertTrue(TestEnum.C.equalsMooseDataCardEncoding("  gHi  "));

        assertFalse(TestEnum.A.equalsMooseDataCardEncoding(null));
        assertFalse(TestEnum.D.equalsMooseDataCardEncoding(null));
        assertFalse(TestEnum.D.equalsMooseDataCardEncoding(""));
        assertFalse(TestEnum.D.equalsMooseDataCardEncoding("ABC"));
    }

}
