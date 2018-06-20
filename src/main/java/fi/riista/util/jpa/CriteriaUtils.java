package fi.riista.util.jpa;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.util.F;
import fi.riista.util.Functions;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static fi.riista.util.Collect.idSet;
import static fi.riista.util.Functions.forMap;
import static fi.riista.util.jpa.JpaSpecs.withIds;
import static org.springframework.data.jpa.domain.Specifications.where;

public final class CriteriaUtils {

    private static final LoadingCache<SingularAttribute<?, ?>, Function<?, ?>> JPA_PROPERTY_FUNCTIONS =
            CacheBuilder
                    .newBuilder()
                    .maximumSize(1000) // pretty bogus magic value
                    .build(new CacheLoader<SingularAttribute<?, ?>, Function<?, ?>>() {
                        @Nullable
                        @Override
                        public Function<?, ?> load(@Nullable final SingularAttribute<?, ?> key) {
                            if (key == null) {
                                return null;
                            }

                            @SuppressWarnings("rawtypes")
                            final SingularAttribute attribute = key;
                            @SuppressWarnings("unchecked")
                            final Function<?, ?> result = jpaPropertyInternal(attribute);
                            return result;
                        }
                    });

    private static final LoadingCache<PluralAttribute<?, ?, ?>, Method> ENTITY_COLLECTION_GETTERS =
            CacheBuilder.newBuilder()
                    .weakValues()
                    .build(new CacheLoader<PluralAttribute<?, ?, ?>, Method>() {
                        @Override
                        public Method load(final PluralAttribute<?, ?, ?> attribute) {
                            final Class<?> declaringClass = attribute.getDeclaringType().getJavaType();
                            final String getterName = getGetterName(attribute);
                            final Method readMethod = BeanUtils.findDeclaredMethod(declaringClass, getterName);

                            if (readMethod == null) {
                                throw new IllegalStateException(String.format(
                                        "Class %s does not declare method named '%s'",
                                        declaringClass.getName(),
                                        getterName));
                            }

                            readMethod.setAccessible(true);
                            return readMethod;
                        }

                        private String getGetterName(final PluralAttribute<?, ?, ?> attribute) {
                            return String.format("get%s", StringUtils.capitalize(attribute.getName()));
                        }
                    });

    @Nonnull
    static <T, U> Function<? super T, U> jpaProperty(@Nonnull final SingularAttribute<? super T, U> attribute) {
        Objects.requireNonNull(attribute);

        try {
            @SuppressWarnings("unchecked")
            final Function<? super T, U> result = (Function<? super T, U>) JPA_PROPERTY_FUNCTIONS.get(attribute);
            return result;
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    static <T, U, C extends Collection<U>> Function<T, C> jpaCollection(
            @Nonnull final PluralAttribute<? super T, C, U> attribute) {

        Objects.requireNonNull(attribute);

        final Class<C> collectionType = attribute.getJavaType();

        try {
            final Method readMethod = ENTITY_COLLECTION_GETTERS.get(attribute);

            return obj -> invokeAndCast(readMethod, obj, collectionType);

        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, U, C extends Collection<U>> void updateInverseCollection(
            @Nonnull final PluralAttribute<? super T, C, U> inverseAttribute,
            @Nonnull final U entity,
            @Nullable final T currentAssociation,
            @Nullable final T newAssociation) {
        if (inverseAttribute == null) {
            // Unit-test
            return;
        }

        Objects.requireNonNull(entity, "entity is null");

        final Function<T, C> fn = getCollectionIfInitialized(inverseAttribute);

        final C oldCollection = fn.apply(currentAssociation);

        if (oldCollection != null) {
            oldCollection.remove(entity);
        }

        final C newCollection = fn.apply(newAssociation);

        if (newCollection != null) {
            newCollection.add(entity);
        }
    }

    public static <T, U> boolean isCollectionLoaded(
            @Nonnull final T entity,
            @Nonnull final PluralAttribute<? super T, ? extends Collection<U>, U> pluralAttribute) {

        Objects.requireNonNull(entity, "entity is null");

        return getCollectionIfInitialized(pluralAttribute).apply(entity) != null;
    }

    private static <T, U, C extends Collection<U>> Function<T, C> getCollectionIfInitialized(
            final PluralAttribute<? super T, C, U> attribute) {

        Objects.requireNonNull(attribute);

        return new Function<T, C>() {

            // Cache for lazy-initialized function, not strictly thread-safe
            private Function<T, C> collectionFn;

            @Nullable
            @Override
            public C apply(@Nullable final T entity) {
                if (entity == null || !Hibernate.isInitialized(entity)) {
                    return null;
                }

                if (collectionFn == null) {
                    collectionFn = jpaCollection(attribute);
                }

                final C collection = collectionFn.apply(entity);

                return Hibernate.isInitialized(collection) ? collection : null;
            }
        };
    }

    @Nonnull
    public static <T, ID extends Serializable, U extends HasID<ID> & Persistable<ID>, R extends BaseRepository<U, ID>> Function<T, U> singleQueryFunction(
            @Nonnull final Iterable<? extends T> objects,
            @Nonnull final Function<? super T, U> function,
            @Nonnull final R repository,
            final boolean letFunctionThrowExceptionOnNullResult) {

        return singleQueryFunction(
                objects, function, repository, Optional.empty(), letFunctionThrowExceptionOnNullResult);
    }

    @Nonnull
    public static <T, ID extends Serializable, U extends HasID<ID> & Persistable<ID>, R extends BaseRepository<U, ID>> Function<T, U> singleQueryFunction(
            @Nonnull final Iterable<? extends T> objects,
            @Nonnull final Function<? super T, U> function,
            @Nonnull final R repository,
            @Nonnull final Specification<U> relationConstraint,
            final boolean letFunctionThrowExceptionOnNullResult) {

        return singleQueryFunction(
                objects, function, repository, Optional.of(relationConstraint), letFunctionThrowExceptionOnNullResult);
    }

    private static <T, ID extends Serializable, U extends HasID<ID> & Persistable<ID>, R extends BaseRepository<U, ID>> Function<T, U> singleQueryFunction(
            @Nonnull final Iterable<? extends T> objects,
            @Nonnull final Function<? super T, U> function,
            @Nonnull final R repository,
            @Nonnull final Optional<Specification<U>> relationConstraintOption,
            final boolean letFunctionThrowExceptionOnNullResult) {

        Objects.requireNonNull(objects, "objects is null");
        Objects.requireNonNull(function, "function is null");
        Objects.requireNonNull(repository, "repository is null");
        Objects.requireNonNull(relationConstraintOption, "relationConstraintOption is null");

        final Iterator<? extends T> iter = objects.iterator();

        if (!iter.hasNext()) {
            return t -> null;
        }

        // shortcut for singleton Iterable in which case N+1 issue is not relevant
        iter.next();
        if (!iter.hasNext()) {
            return function::apply;
        }

        final Set<ID> relationIds = F.stream(objects).map(function).collect(idSet());

        if (relationIds.isEmpty()) {
            return t -> null;
        }

        final List<U> relations = relationConstraintOption.isPresent()
                ? repository.findAll(where(relationConstraintOption.get()).and(withIds(relationIds)))
                : repository.findAll(relationIds);

        final Map<ID, U> indexedRelations = F.indexById(relations);

        final Function<ID, U> indexFunc =
                letFunctionThrowExceptionOnNullResult ? forMap(indexedRelations) : forMap(indexedRelations, null);

        return indexFunc.compose(Functions.idOf(function));
    }

    @Nonnull
    public static <ID extends Serializable, T extends Persistable<ID> & HasID<ID>, U> Function<T, Long> createAssociationCountFunction(
            @Nonnull final Iterable<? extends T> collection,
            @Nonnull final Class<U> associatedClass,
            @Nonnull final SingularAttribute<? super U, T> associationAttribute,
            @Nonnull final EntityManager entityManager) {

        Objects.requireNonNull(collection, "collection is null");
        Objects.requireNonNull(associatedClass, "associatedClass is null");
        Objects.requireNonNull(associationAttribute, "associationAttribute is null");
        Objects.requireNonNull(entityManager, "entityManager is null");

        if (Iterables.isEmpty(collection)) {
            return t -> 0L;
        }

        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Object> query = cb.createQuery();

        final Root<U> root = query.from(associatedClass);

        final EntityType<T> targetEntityType = entityManager.getMetamodel().entity(associationAttribute.getJavaType());
        final Path<ID> targetEntityIdPath =
                root.get(associationAttribute).get(getSingularIdAttribute(targetEntityType));

        query.select(cb.construct(Tuple2.class, targetEntityIdPath, cb.count(root)))
                .where(targetEntityIdPath.in(cb.literal(F.getUniqueIds(collection))))
                .groupBy(targetEntityIdPath);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        final List<Tuple2<ID, Long>> pairs = (List) entityManager.createQuery(query).getResultList();

        if (pairs.isEmpty()) {
            return t -> 0L;
        }

        final HashMap<ID, Long> idToCount = HashMap.ofEntries(pairs);

        return t -> idToCount.get(F.getId(t)).getOrElse(0L);
    }

    public static <T, U> BiConsumer<T, U> createSetterInvoker(
            @Nonnull final SingularAttribute<? super T, U> attribute) {

        Objects.requireNonNull(attribute);

        final Class<?> declaringClass = attribute.getDeclaringType().getJavaType();
        final String setterName = getSetterName(attribute.getName());
        final Method setterMethod = BeanUtils.findDeclaredMethod(declaringClass, setterName, attribute.getJavaType());

        if (setterMethod == null) {
            throw new IllegalStateException(String.format(
                    "Class %s does not declare method named '%s'",
                    declaringClass.getName(),
                    setterName));
        }

        setterMethod.setAccessible(true);

        return (object, value) -> {
            try {
                setterMethod.invoke(object, value);
            } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T, U> Join<T, U> join(
            @Nonnull final From<?, T> from, @Nonnull final PluralAttribute<? super T, ?, U> attribute) {

        Objects.requireNonNull(from, "from is null");
        Objects.requireNonNull(attribute, "attribute is null");

        if (attribute instanceof CollectionAttribute) {
            return from.join((CollectionAttribute<T, U>) attribute);
        }
        if (attribute instanceof SetAttribute) {
            return from.join((SetAttribute<T, U>) attribute);
        }
        if (attribute instanceof ListAttribute) {
            return from.join((ListAttribute<T, U>) attribute);
        }
        if (attribute instanceof MapAttribute) {
            return from.join((MapAttribute<T, ?, U>) attribute);
        }

        // Should never end up here.
        throw new IllegalArgumentException();
    }

    private CriteriaUtils() {
        throw new AssertionError();
    }

    @Nonnull
    private static <T, U> Function<T, U> jpaPropertyInternal(@Nonnull final SingularAttribute<? super T, U> attr) {
        Objects.requireNonNull(attr);

        final Method readMethod = BeanUtils.getPropertyDescriptor(attr.getDeclaringType().getJavaType(), attr.getName())
                .getReadMethod();

        return obj -> invokeAndCast(readMethod, obj, attr.getJavaType());
    }

    private static <ID extends Serializable, T extends Persistable<ID>> SingularAttribute<? super T, ID> getSingularIdAttribute(
            final EntityType<T> entityType) {

        if (!entityType.hasSingleIdAttribute()) {
            throw new UnsupportedOperationException("Multi-attribute-ID not supported.");
        }

        @SuppressWarnings("unchecked")
        final Class<ID> idClass = (Class<ID>) entityType.getIdType().getJavaType();
        return entityType.getId(idClass);
    }

    private static <T> T cast(final Object value, final Class<T> expectedType) {
        if (value == null) {
            return null;
        }

        if (expectedType.isPrimitive() && Primitives.wrap(expectedType).equals(value.getClass())) {
            @SuppressWarnings("unchecked")
            final T castValue = (T) value;
            return castValue;
        }

        if (!expectedType.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(String.format(
                    "Expected %s, but was: %s", expectedType.getName(), value.getClass().getName()));
        }

        return expectedType.cast(value);
    }

    private static <T> T invokeAndCast(final Method method, final Object object, final Class<T> expectedType) {
        try {
            return object == null ? null : cast(method.invoke(object), expectedType);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getSetterName(final String propertyName) {
        return String.format("set%s", StringUtils.capitalize(propertyName));
    }

}
