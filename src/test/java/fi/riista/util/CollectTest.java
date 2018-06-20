package fi.riista.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import fi.riista.feature.common.entity.HasID;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static fi.riista.util.Collect.greatestAfterGroupingBy;
import static fi.riista.util.Collect.groupingByIdOf;
import static fi.riista.util.Collect.idList;
import static fi.riista.util.Collect.idSet;
import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.Collect.indexingByIdOf;
import static fi.riista.util.Collect.leastAfterGroupingBy;
import static fi.riista.util.Collect.mappingAndCollectingFirst;
import static fi.riista.util.Collect.mappingTo;
import static fi.riista.util.Collect.nullSafeGroupingBy;
import static fi.riista.util.Collect.nullSafeGroupingByIdOf;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CollectTest {

    private static final AtomicInteger A_ID_SEQ = new AtomicInteger(1001);
    private static final AtomicInteger B_ID_SEQ = new AtomicInteger(1);

    @Test
    public void testIdList() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b1);
        final A a4 = new A(null);

        assertEquals(Ints.asList(b1.id, b2.id, b1.id), Stream.of(a1, a2, a3, a4).map(A::getB).collect(idList()));
    }

    @Test
    public void testIdSet() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b1);
        final A a4 = new A(null);

        assertEquals(Sets.newHashSet(b1.id, b2.id), Stream.of(a1, a2, a3, a4).map(A::getB).collect(idSet()));
    }

    @Test
    public void testIndexingBy() {
        final B b1 = new B();
        final B b2 = new B();
        final B b3 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b3);

        assertEquals(ImmutableMap.of(b1, a1, b2, a2, b3, a3), Stream.of(a1, a2, a3).collect(indexingBy(A::getB)));
    }

    @Test
    public void testIndexingByIdOf() {
        final B b1 = new B();
        final B b2 = new B();
        final B b3 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b3);
        final A a3 = new A(b2);

        assertEquals(
                ImmutableMap.of(b1.id, a1, b2.id, a3, b3.id, a2),
                Stream.of(a1, a2, a3).collect(indexingByIdOf(A::getB)));
    }

    @Test
    public void testGroupingByIdOf() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b1);

        assertEquals(
                ImmutableMap.of(b1.id, asList(a1, a3), b2.id, asList(a2)),
                Stream.of(a1, a2, a3).collect(groupingByIdOf(A::getB)));
    }

    @Test
    public void testNullSafeGroupingBy() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b1);
        final A a4 = new A(null);

        assertEquals(
                ImmutableMap.of(b1, asList(a1, a3), b2, asList(a2)),
                Stream.of(a1, a2, a3, a4).collect(nullSafeGroupingBy(A::getB)));
    }

    @Test
    public void testNullSafeGroupingByIdOf() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b1);
        final A a4 = new A(null);

        assertEquals(
                ImmutableMap.of(b1.id, asList(a1, a3), b2.id, asList(a2)),
                Stream.of(a1, a2, a3, a4).collect(nullSafeGroupingByIdOf(A::getB)));
    }

    @Test
    public void testLeastAfterGroupingBy() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b1);
        final A a3 = new A(b2);

        assertEquals(
                ImmutableMap.of(b1, a1, b2, a3),
                Stream.of(a1, a2, a3).collect(leastAfterGroupingBy(A::getB, comparing(A::getId))));
    }

    @Test
    public void testGreatestAfterGroupingBy() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b1);
        final A a3 = new A(b2);

        assertEquals(
                ImmutableMap.of(b1, a2, b2, a3),
                Stream.of(a1, a2, a3).collect(greatestAfterGroupingBy(A::getB, comparing(A::getId))));
    }

    @Test
    public void testMappingTo() {
        final B b1 = new B();
        final B b2 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b1);

        assertEquals(ImmutableMap.of(a1, b1, a2, b2, a3, b1), Stream.of(a1, a2, a3).collect(mappingTo(A::getB)));
    }

    @Test
    public void testMappingAndCollectingFirst() {
        final B b1 = new B();
        final B b2 = new B();
        final B b3 = new B();

        final A a1 = new A(b1);
        final A a2 = new A(b2);
        final A a3 = new A(b3);

        assertEquals(b1, Stream.of(a1).collect(mappingAndCollectingFirst(A::getB)));
        assertEquals(b1, Stream.of(a1, a2, a3).collect(mappingAndCollectingFirst(A::getB)));
        assertEquals(b3, Stream.of(a3, a2, a1).collect(mappingAndCollectingFirst(A::getB)));

        final A a4 = new A(null);

        assertNull(Stream.of(a4).collect(mappingAndCollectingFirst(A::getB)));
    }

    private static class A implements HasID<Integer> {
        final int id;
        final B b;

        A(final B b) {
            this.id = A_ID_SEQ.getAndIncrement();
            this.b = b;
        }

        @Override
        public Integer getId() {
            return id;
        }

        public B getB() {
            return b;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    private static class B implements HasID<Integer> {
        final int id;

        B() {
            this.id = B_ID_SEQ.getAndIncrement();
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
