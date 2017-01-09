package fi.riista.util.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public interface JpaSubQueryPredicate<T> {
    public Predicate predicate(From<?, T> root, CriteriaBuilder cb);
}
