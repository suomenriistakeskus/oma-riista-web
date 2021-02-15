package fi.riista.util.jpa;

import com.google.common.collect.Iterables;
import fi.riista.feature.common.entity.HasID;
import fi.riista.util.F;
import fi.riista.util.Functions;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static fi.riista.util.Collect.nullSafeGroupingBy;
import static fi.riista.util.Collect.nullSafeGroupingByIdOf;
import static fi.riista.util.Functions.forMap;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static fi.riista.util.jpa.JpaSpecs.inIdCollection;

public final class JpaGroupingUtils {

    @Nonnull
    public static <T, U> Map<T, List<U>> groupRelations(@Nonnull final Collection<? extends T> objects,
                                                        @Nonnull final SingularAttribute<? super U, T> singularAttribute,
                                                        @Nonnull final JpaSpecificationExecutor<U> repository) {

        return groupRelations(objects, singularAttribute, repository, null, null);
    }

    @Nonnull
    public static <T, U> Map<T, List<U>> groupRelations(@Nonnull final Collection<? extends T> objects,
                                                        @Nonnull final SingularAttribute<? super U, T> singularAttribute,
                                                        @Nonnull final JpaSpecificationExecutor<U> repository,
                                                        @Nullable final Specification<U> constraint) {

        return groupRelations(objects, singularAttribute, repository, constraint, null);
    }

    @Nonnull
    public static <T, U> Map<T, List<U>> groupRelations(@Nonnull final Collection<? extends T> objects,
                                                        @Nonnull final SingularAttribute<? super U, T> singularAttribute,
                                                        @Nonnull final JpaSpecificationExecutor<U> repository,
                                                        @Nullable final Sort sortCriteria) {

        return groupRelations(objects, singularAttribute, repository, null, sortCriteria);
    }

    @Nonnull
    public static <T, U> Map<T, List<U>> groupRelations(@Nonnull final Collection<? extends T> objects,
                                                        @Nonnull final SingularAttribute<? super U, T> singularAttribute,
                                                        @Nonnull final JpaSpecificationExecutor<U> repository,
                                                        @Nullable final Specification<U> constraint,
                                                        @Nullable final Sort sortCriteria) {

        final Specification<U> inSpec = inCollection(singularAttribute, objects);

        return groupRelationsInternal(
                objects, inSpec, repository, CriteriaUtils.jpaProperty(singularAttribute), constraint, sortCriteria);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<? extends T> objects,
            @Nonnull final SingularAttribute<? super U, T> singularAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository) {

        return groupRelationsById(objects, singularAttribute, repository, null, null);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<? extends T> objects,
            @Nonnull final SingularAttribute<? super U, T> singularAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            @Nullable final Specification<U> constraint) {

        return groupRelationsById(objects, singularAttribute, repository, constraint, null);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<? extends T> objects,
            @Nonnull final SingularAttribute<? super U, T> singularAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            @Nullable final Sort sortCriteria) {

        return groupRelationsById(objects, singularAttribute, repository, null, sortCriteria);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<? extends T> objects,
            @Nonnull final SingularAttribute<? super U, T> singularAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            @Nullable final Specification<U> constraint,
            @Nullable final Sort sortCriteria) {

        final Specification<U> inSpec = inCollection(singularAttribute, objects);

        final Function<U, ID> idFunction = Functions.idOf(CriteriaUtils.jpaProperty(singularAttribute));

        return groupRelationsInternal(objects, inSpec, repository, idFunction, constraint, sortCriteria);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<ID> ids,
            @Nonnull final SingularAttribute<? super U, T> associationAttribute,
            @Nonnull final SingularAttribute<? super T, ID> idAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository) {

        return groupRelationsById(ids, associationAttribute, idAttribute, repository, null, null);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<ID> ids,
            @Nonnull final SingularAttribute<? super U, T> associationAttribute,
            @Nonnull final SingularAttribute<? super T, ID> idAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            @Nullable final Specification<U> constraint) {

        return groupRelationsById(ids, associationAttribute, idAttribute, repository, constraint, null);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<ID> ids,
            @Nonnull final SingularAttribute<? super U, T> associationAttribute,
            @Nonnull final SingularAttribute<? super T, ID> idAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            @Nullable final Sort sortCriteria) {

        return groupRelationsById(ids, associationAttribute, idAttribute, repository, null, sortCriteria);
    }

    @Nonnull
    public static <ID extends Serializable, T extends HasID<ID>, U> Map<ID, List<U>> groupRelationsById(
            @Nonnull final Collection<ID> ids,
            @Nonnull final SingularAttribute<? super U, T> associationAttribute,
            @Nonnull final SingularAttribute<? super T, ID> idAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            @Nullable final Specification<U> constraint,
            @Nullable final Sort sortCriteria) {

        final Specification<U> inSpec = inIdCollection(associationAttribute, idAttribute, ids);

        final Function<U, ID> idFunction = Functions.idOf(CriteriaUtils.jpaProperty(associationAttribute));

        return groupRelationsInternal(ids, inSpec, repository, idFunction, constraint, sortCriteria);
    }

    private static <T, U, V> Map<V, List<U>> groupRelationsInternal(@Nonnull final Collection<? extends T> objects,
                                                                    @Nonnull final Specification<U> inSpec,
                                                                    @Nonnull final JpaSpecificationExecutor<U> repository,
                                                                    @Nonnull final Function<? super U, V> keyMapper,
                                                                    @Nullable final Specification<U> constraint,
                                                                    @Nullable final Sort sortCriteria) {

        Objects.requireNonNull(objects, "objects must not be null");
        Objects.requireNonNull(inSpec, "inSpec must not be null");
        Objects.requireNonNull(repository, "repository must not be null");

        if (objects.isEmpty()) {
            return new HashMap<>(0);
        }

        final Specification<U> compoundSpec =
                constraint == null ? inSpec : Specification.where(inSpec).and(constraint);

        final List<U> list = sortCriteria != null
                ? repository.findAll(compoundSpec, sortCriteria)
                : repository.findAll(compoundSpec);

        return list.stream().collect(nullSafeGroupingBy(keyMapper));
    }

    public static <ID extends Serializable, T extends HasID<ID>, U> Function<T, List<U>> createInverseMappingFunction(
            @Nonnull final Collection<? extends T> objects,
            @Nonnull final SingularAttribute<? super U, T> singularAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            final boolean letFunctionThrowExceptionOnEmptyResult) {

        return createInverseMappingFunction(
                objects, singularAttribute, repository, null, letFunctionThrowExceptionOnEmptyResult);
    }

    public static <ID extends Serializable, T extends HasID<ID>, U> Function<T, List<U>> createInverseMappingFunction(
            @Nonnull final Collection<? extends T> objects,
            @Nonnull final SingularAttribute<? super U, T> singularAttribute,
            @Nonnull final JpaSpecificationExecutor<U> repository,
            @Nullable final Specification<U> constraint,
            final boolean letFunctionThrowExceptionOnEmptyResult) {

        Objects.requireNonNull(objects, "objects must not be null");
        Objects.requireNonNull(singularAttribute, "singularAttribute must not be null");
        Objects.requireNonNull(repository, "repository must not be null");

        if (Iterables.isEmpty(objects)) {
            return input -> Collections.emptyList();
        }

        final Specification<U> relationSpec = inCollection(singularAttribute, objects);
        final Specification<U> compoundSpec =
                constraint != null ? JpaSpecs.and(relationSpec, constraint) : relationSpec;

        final List<U> relatedObjects = repository.findAll(compoundSpec);

        if (relatedObjects.isEmpty()) {
            return input -> Collections.emptyList();
        }

        final Map<ID, List<U>> index =
                relatedObjects.stream().collect(nullSafeGroupingByIdOf(CriteriaUtils.jpaProperty(singularAttribute)));

        final Function<ID, List<U>> indexFunc =
                letFunctionThrowExceptionOnEmptyResult ? forMap(index) : forMap(index, null);

        return indexFunc.compose(F::getId);
    }

    private JpaGroupingUtils() {
        throw new AssertionError();
    }
}
