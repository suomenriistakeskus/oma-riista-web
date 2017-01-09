package fi.riista.util.jpa;

import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class JpaSpecsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testAndWithEmpty() {
        JpaSpecs.and();
    }

    @Test
    public void testAndWithValues() {
        Root<Object> root = mock(Root.class);
        CriteriaQuery<?> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Specification<Object> a = mock(Specification.class);
        Specification<Object> b = mock(Specification.class);
        JpaSpecs.and(a, b).toPredicate(root, criteriaQuery, criteriaBuilder);
        verify(a).toPredicate(any(Root.class), any(CriteriaQuery.class), any(CriteriaBuilder.class));
        verify(b).toPredicate(any(Root.class), any(CriteriaQuery.class), any(CriteriaBuilder.class));
        verifyNoMoreInteractions(a);
        verifyNoMoreInteractions(b);
    }

    @Test
    public void testAndWithList() {
        Root<Object> root = mock(Root.class);
        CriteriaQuery<?> criteriaQuery = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Specification<Object> a = mock(Specification.class);
        Specification<Object> b = mock(Specification.class);
        JpaSpecs.and(asList(a, b)).toPredicate(root, criteriaQuery, criteriaBuilder);
        verify(a).toPredicate(any(Root.class), any(CriteriaQuery.class), any(CriteriaBuilder.class));
        verify(b).toPredicate(any(Root.class), any(CriteriaQuery.class), any(CriteriaBuilder.class));
        verifyNoMoreInteractions(a);
        verifyNoMoreInteractions(b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAndWithNullList() {
        List<Specification<Object>> list = null;
        JpaSpecs.and(list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAndWithEmptyList() {
        List<Specification<Object>> list = new ArrayList<>();
        JpaSpecs.and(list);
    }
}
