package fi.riista.util.jpa;

import javaslang.Function3;
import org.hibernate.jpa.criteria.CriteriaBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JpaSpecsTest {

    @Mock
    private Root<Object> root;

    @Mock
    private CriteriaQuery<?> criteriaQuery;

    private CriteriaBuilder cb;

    @Before
    public void setup() {
        cb = spy(new CriteriaBuilderImpl(null));
    }

    @Test(expected = NullPointerException.class)
    public void testAnd_withNullList() {
        JpaSpecs.and((List<Specification<Object>>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnd_withEmptyVararg() {
        JpaSpecs.and();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnd_withEmptyList() {
        JpaSpecs.and(emptyList());
    }

    @Test
    public void testAnd_withOneItemVararg() {
        final Specification<Object> param = mock(Specification.class);
        final Specification<Object> result = JpaSpecs.and(param);

        assertTrue(param == result);

        result.toPredicate(root, criteriaQuery, cb);

        verify(param).toPredicate(eq(root), eq(criteriaQuery), eq(cb));
        verifyNoMoreInteractions(param);
        verifyZeroInteractions(cb);
    }

    @Test
    public void testAnd_withThreeItemVararg() {
        testAnd_withThreeItems(JpaSpecs::and);
    }

    @Test
    public void testAnd_withThreeItemList() {
        testAnd_withThreeItems((a, b, c) -> JpaSpecs.and(asList(a, b, c)));
    }

    private void testAnd_withThreeItems(
            final Function3<Specification<Object>, Specification<Object>, Specification<Object>, Specification<Object>> targetInvoker) {

        final Predicate x = mock(Predicate.class);
        final Predicate y = mock(Predicate.class);
        final Predicate z = mock(Predicate.class);

        final Specification<Object> a = mock(Specification.class);
        final Specification<Object> b = mock(Specification.class);
        final Specification<Object> c = mock(Specification.class);

        when(a.toPredicate(root, criteriaQuery, cb)).thenReturn(x);
        when(b.toPredicate(root, criteriaQuery, cb)).thenReturn(y);
        when(c.toPredicate(root, criteriaQuery, cb)).thenReturn(z);

        final Specification<Object> result = targetInvoker.apply(a, b, c);
        result.toPredicate(root, criteriaQuery, cb);

        Stream.of(a, b, c).forEach(s -> {
            verify(s).toPredicate(eq(root), eq(criteriaQuery), eq(cb));
            verifyNoMoreInteractions(s);
        });

        verify(cb).and(eq(x), eq(y));
        // First argument is a compound predicate as a result from previous call.
        verify(cb).and(any(Predicate.class), eq(z));

        verifyNoMoreInteractions(cb);

        // Call this again to assert that intermediate Stream object is not tried to consume
        // multiple times.
        result.toPredicate(root, criteriaQuery, cb);
    }

    @Test(expected = NullPointerException.class)
    public void testOr_withNullList() {
        JpaSpecs.or((List<Specification<Object>>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOr_withEmptyVararg() {
        JpaSpecs.or();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOr_withEmptyList() {
        JpaSpecs.or(emptyList());
    }

    @Test
    public void testOr_withOneItemVararg() {
        final Specification<Object> param = mock(Specification.class);
        final Specification<Object> result = JpaSpecs.or(param);

        assertTrue(param == result);

        result.toPredicate(root, criteriaQuery, cb);

        verify(param).toPredicate(eq(root), eq(criteriaQuery), eq(cb));
        verifyNoMoreInteractions(param);
        verifyZeroInteractions(cb);
    }

    @Test
    public void testOr_withThreeItemVararg() {
        testOr_withThreeItems(JpaSpecs::or);
    }

    @Test
    public void testOr_withThreeItemList() {
        testOr_withThreeItems((a, b, c) -> JpaSpecs.or(asList(a, b, c)));
    }

    private void testOr_withThreeItems(
            final Function3<Specification<Object>, Specification<Object>, Specification<Object>, Specification<Object>> targetInvoker) {

        final Predicate x = mock(Predicate.class);
        final Predicate y = mock(Predicate.class);
        final Predicate z = mock(Predicate.class);

        final Specification<Object> a = mock(Specification.class);
        final Specification<Object> b = mock(Specification.class);
        final Specification<Object> c = mock(Specification.class);

        when(a.toPredicate(root, criteriaQuery, cb)).thenReturn(x);
        when(b.toPredicate(root, criteriaQuery, cb)).thenReturn(y);
        when(c.toPredicate(root, criteriaQuery, cb)).thenReturn(z);

        final Specification<Object> result = targetInvoker.apply(a, b, c);
        result.toPredicate(root, criteriaQuery, cb);

        Stream.of(a, b, c).forEach(s -> {
            verify(s).toPredicate(eq(root), eq(criteriaQuery), eq(cb));
            verifyNoMoreInteractions(s);
        });

        verify(cb).or(eq(x), eq(y));
        // First argument is a compound predicate as a result from previous call.
        verify(cb).or(any(Predicate.class), eq(z));

        verifyNoMoreInteractions(cb);

        // Call this again to assert that intermediate Stream object is not tried to consume
        // multiple times.
        result.toPredicate(root, criteriaQuery, cb);
    }

}
