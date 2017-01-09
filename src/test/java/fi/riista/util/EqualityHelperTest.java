package fi.riista.util;

import fi.riista.feature.common.entity.HasID;
import org.junit.Test;
import java.util.Objects;
import java.util.function.BiFunction;

import static fi.riista.util.EqualityHelper.equal;
import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static fi.riista.util.EqualityHelper.equalNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EqualityHelperTest {

    private static final BiFunction<A, B, Boolean> SAME_CONTENT = (a, b) -> Objects.equals(a.content, b.content);

    private static A a(final Integer id, final String content) {
        return new A(id, content);
    }

    private static B b(final Integer id, final String content) {
        return new B(id, content);
    }

    // equal

    @Test
    public void testEqual_whenBothAreEmpty() {
        assertTrue(equal(emptyList(), emptyList(), Objects::equals));
    }

    @Test
    public void testEqual_whenFirstIsEmpty_andLatterIsSingleton() {
        assertFalse(equal(emptyList(), singletonList("abc"), String::equals));
    }

    @Test
    public void testEqual_whenFirstIsEmpty_andLatterIsNullSingleton() {
        assertFalse(equal(emptyList(), singletonList(null), Objects::equals));
    }

    @Test
    public void testEqual_whenFirstIsEmpty_andLatterContainsMultipleItems() {
        assertFalse(equal(emptyList(), asList(1, 2, 3), Integer::equals));
    }

    @Test
    public void testEqual_whenLatterIsEmpty_andFirstIsSingleton() {
        assertFalse(equal(singletonList("abc"), emptyList(), String::equals));
    }

    @Test
    public void testEqual_whenLatterIsEmpty_andFirstIsNullSingleton() {
        assertFalse(equal(singletonList(null), emptyList(), Objects::equals));
    }

    @Test
    public void testEqual_whenLatterIsEmpty_andFirstContainsMultipleItems() {
        assertFalse(equal(asList(1, 2, 3), emptyList(), Integer::equals));
    }

    @Test
    public void testEqual_withIdenticalSingletons() {
        assertTrue(equal(singletonList("abc"), singletonList("abc"), String::equals));
    }

    @Test
    public void testEqual_withIdenticalSingletons_ofNullItems() {
        assertTrue(equal(singletonList(null), singletonList(null), Objects::equals));
    }

    @Test
    public void testEqual_withNonIdenticalSingletons() {
        assertFalse(equal(singletonList("abc"), singletonList("def"), String::equals));
    }

    @Test
    public void testEqual_withNonIdenticalSingletons_whenFirstIsNull() {
        assertFalse(equal(singletonList(null), singletonList("abc"), Objects::equals));
    }

    @Test
    public void testEqual_withNonIdenticalSingletons_whenLatterIsNull() {
        assertFalse(equal(singletonList("abc"), singletonList(null), String::equals));
    }

    @Test
    public void testEqual_whenFirstIterableSmaller() {
        assertFalse(equal(singletonList(1), asList(1, 2), Integer::equals));
    }

    @Test
    public void testEqual_whenFirstIterableLarger() {
        assertFalse(equal(asList(1, 2), singletonList(1), Integer::equals));
    }

    @Test
    public void testEqual_withIdenticalMultiItemLists() {
        assertTrue(equal(asList(1, 2, 3, 4, 5), asList(1, 2, 3, 4, 5), Integer::equals));
    }

    @Test
    public void testEqual_withSameSizeNullItemLists() {
        assertTrue(equal(asList(null, null, null), asList(null, null, null), Objects::equals));
    }

    @Test
    public void testEqual_whenSameSizeAndIdenticalItems_withSomeNullItems() {
        assertTrue(equal(asList(1, null, 3, null, 5), asList(1, null, 3, null, 5), Objects::equals));
    }

    @Test
    public void testEqual_whenSameSizeButItemsInDifferentOrder() {
        assertFalse(equal(asList(1, 2, 3, 4, 5), asList(5, 4, 3, 2, 1), Integer::equals));
    }

    @Test
    public void testEqual_whenSameSizeButDifferentItems_andAlsoContainingSomeNulls() {
        assertFalse(equal(asList(1, null, 3, null, 5), asList(null, 2, null, 4, null), Objects::equals));
    }

    // equalNotNull

    @Test
    public void testEqualNotNull_withIdenticalSingletons() {
        assertTrue(equalNotNull(singletonList(a(1, "abc")), singletonList(b(1, "abc")), SAME_CONTENT));
    }

    @Test
    public void testEqualNotNull_withIdenticalSingletons_ofNullItems() {
        assertFalse(equalNotNull(singletonList(null), singletonList(null), Objects::equals));
    }

    @Test
    public void testEqualNotNull_withIdenticalMultiItemLists() {
        assertTrue(equalNotNull(
                asList(a(1, "abc"), a(2, "def"), a(3, "ghi")),
                asList(b(1, "abc"), b(2, "def"), b(3, "ghi")),
                SAME_CONTENT));
    }

    @Test
    public void testEqualNotNull_withSameSizeNullItemLists() {
        assertFalse(equalNotNull(asList(null, null, null), asList(null, null, null), Objects::equals));
    }

    @Test
    public void testEqualNotNull_whenSameSizeAndIdenticalItems_andAlsoContainingSomeNulls() {
        assertFalse(equalNotNull(asList(1, null, 3, null, 5), asList(1, null, 3, null, 5), Objects::equals));
    }

    // equalIdAndContent

    @Test
    public void testEqualIdAndContent_withSingletons_whenIdentical() {
        assertTrue(equalIdAndContent(singletonList(a(1, "abc")), singletonList(b(1, "abc")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withSingletons_whenDifferentId() {
        assertFalse(equalIdAndContent(singletonList(a(1, "abc")), singletonList(b(2, "abc")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withSingletons_whenDifferentContent() {
        assertFalse(equalIdAndContent(singletonList(a(1, "abc")), singletonList(b(1, "def")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withSingletons_whenFirstIdIsNull() {
        assertFalse(equalIdAndContent(singletonList(a(null, "abc")), singletonList(b(1, "abc")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withSingletons_whenLatterIdIsNull() {
        assertFalse(equalIdAndContent(singletonList(a(1, "abc")), singletonList(b(null, "abc")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withSingletons_whenBothIdsAreNull() {
        assertTrue(equalIdAndContent(singletonList(a(null, "abc")), singletonList(b(null, "abc")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withSingletons_whenBothIdsAreNull_andContentIsDifferent() {
        assertFalse(equalIdAndContent(singletonList(a(null, "abc")), singletonList(b(null, "def")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withIdenticalMultiItemsLists() {
        assertTrue(equalIdAndContent(asList(a(1, "abc"), a(2, "def")), asList(b(1, "abc"), b(2, "def")), SAME_CONTENT));
    }

    @Test
    public void testEqualIdAndContent_withEqualItemsButInDifferentOrder() {
        assertFalse(
                equalIdAndContent(asList(a(1, "abc"), a(2, "def")), asList(b(2, "def"), b(1, "abc")), SAME_CONTENT));
    }

    // Test classes

    private static class A implements HasID<Integer> {

        // ID is not included in equals and hashCode for testing purposes
        public final Integer id;

        public final String content;

        A(final Integer id, final String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public Integer getId() {
            return id;
        }
    }

    private static class B implements HasID<Integer> {

        // ID is not included in equals and hashCode for testing purposes
        public final Integer id;

        public final String content;

        B(final Integer id, final String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public Integer getId() {
            return id;
        }
    }

}
