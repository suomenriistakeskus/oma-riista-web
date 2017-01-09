package fi.riista.util.jpa;

import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import java.util.Objects;

public abstract class JpaSubQuery<P, S> {

    @Nonnull
    public static <P, S> JpaSubQuery<P, S> of(@Nonnull final SingularAttribute<? super P, S> attribute) {
        return new AbstractSubQuery<P, S, SingularAttribute<? super P, S>>(Objects.requireNonNull(attribute)) {
            @Override
            protected From<P, S> join(final Root<P> root) {
                return root.join(attribute);
            }
        };
    }

    @Nonnull
    public static <P, S> JpaSubQuery<P, S> of(@Nonnull final PluralAttribute<P, ?, S> attribute) {
        return new AbstractSubQuery<P, S, PluralAttribute<P, ?, S>>(Objects.requireNonNull(attribute)) {
            @Override
            protected From<P, S> join(final Root<P> root) {
                return CriteriaUtils.join(root, attribute);
            }
        };
    }

    @Nonnull
    public static <P, S> JpaSubQuery<P, S> inverseOf(@Nonnull final SingularAttribute<S, P> attribute) {
        return new AbstractReverseSubQuery<P, S, SingularAttribute<S, P>>(Objects.requireNonNull(attribute)) {
            @Override
            protected Path<P> getPathToParentRoot(final Root<S> root) {
                return root.get(attribute);
            }
        };
    }

    @Nonnull
    public static <P, S> JpaSubQuery<P, S> inverseOf(@Nonnull final PluralAttribute<S, ?, P> attribute) {
        return new AbstractReverseSubQuery<P, S, PluralAttribute<S, ?, P>>(Objects.requireNonNull(attribute)) {
            @Override
            protected Path<P> getPathToParentRoot(final Root<S> root) {
                return CriteriaUtils.join(root, attribute);
            }
        };
    }

    @Nonnull
    public Specification<P> exists(@Nonnull final JpaSubQueryPredicate<S> predicate) {
        Objects.requireNonNull(predicate);

        return (root, query, cb) -> JpaSubQuery.this.exists(root, query, cb, predicate);
    }

    @Nonnull
    public Specification<P> notExists(@Nonnull final JpaSubQueryPredicate<S> predicate) {
        Objects.requireNonNull(predicate);

        return (root, query, cb) -> JpaSubQuery.this.notExists(root, query, cb, predicate);
    }

    @Nonnull
    public abstract Predicate exists(
            @Nonnull final Root<P> root,
            @Nonnull final CriteriaQuery<?> query,
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final JpaSubQueryPredicate<S> predicate);

    @Nonnull
    public final Predicate notExists(
            @Nonnull final Root<P> root,
            @Nonnull final CriteriaQuery<?> query,
            @Nonnull final CriteriaBuilder cb,
            @Nonnull final JpaSubQueryPredicate<S> predicate) {

        return cb.not(exists(root, query, cb, predicate));
    }

    private static abstract class AbstractSubQuery<P, S, A extends Attribute<? super P, ?>> extends JpaSubQuery<P, S> {

        @SuppressWarnings("unused")
        private final A attribute;

        protected AbstractSubQuery(final A attribute) {
            this.attribute = attribute;
        }

        @Nonnull
        @Override
        public final Predicate exists(
                @Nonnull final Root<P> root,
                @Nonnull final CriteriaQuery<?> query,
                @Nonnull final CriteriaBuilder cb,
                @Nonnull final JpaSubQueryPredicate<S> predicate) {

            Objects.requireNonNull(root, "root must not be null");
            Objects.requireNonNull(query, "query must not be null");
            Objects.requireNonNull(cb, "cb must not be null");
            Objects.requireNonNull(predicate, "predicate must not be null");

            final Subquery<Integer> subQuery = query.subquery(Integer.class);
            final Root<P> subRoot = subQuery.correlate(root);

            return cb.exists(subQuery
                    .select(cb.literal(1))
                    .where(predicate.predicate(join(subRoot), cb)));
        }

        protected abstract From<P, S> join(Root<P> root);
    }

    private static abstract class AbstractReverseSubQuery<P, S, A extends Attribute<S, ?>> extends JpaSubQuery<P, S> {

        private final A attribute;

        protected AbstractReverseSubQuery(final A attribute) {
            this.attribute = attribute;
        }

        @Nonnull
        @Override
        public final Predicate exists(
                @Nonnull final Root<P> root,
                @Nonnull final CriteriaQuery<?> query,
                @Nonnull final CriteriaBuilder cb,
                @Nonnull final JpaSubQueryPredicate<S> predicate) {

            Objects.requireNonNull(root, "root must not be null");
            Objects.requireNonNull(query, "query must not be null");
            Objects.requireNonNull(cb, "cb must not be null");
            Objects.requireNonNull(predicate, "predicate must not be null");

            final Subquery<Integer> subQuery = query.subquery(Integer.class);
            final Root<S> subRoot = subQuery.from(attribute.getDeclaringType().getJavaType());

            return cb.exists(subQuery
                    .select(cb.literal(1))
                    .where(cb.and(
                            predicate.predicate(subRoot, cb),
                            cb.equal(getPathToParentRoot(subRoot), subQuery.correlate(root)))));
        }

        protected abstract Path<P> getPathToParentRoot(Root<S> root);
    }

}
