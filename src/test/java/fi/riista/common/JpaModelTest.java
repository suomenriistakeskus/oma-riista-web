package fi.riista.common;

import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.ReflectionTestUtils.fieldsOfClass;

import com.google.common.base.Strings;

import fi.riista.ClassInventory;
import fi.riista.util.ClassUtils;
import fi.riista.util.jpa.EagerFetch;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Test;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class JpaModelTest {

    /**
     * Classes of bidirectional entity associations must match.
     *
     * An ORM framework catches entity association mapping defects more
     * thoroughly, but this test is here to catch defects without launching
     * application.
     */
    @Test
    public void mappedByParametersMustBeSetCorrectly() {
        final Stream<Field> failedFields = filterFieldsOfManagedJpaTypes(field -> {
            final OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            final OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            final ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);

            String mappedBy = null;
            Class<?> inverseAssociationType = null;

            if (oneToOne != null) {
                mappedBy = oneToOne.mappedBy();
                inverseAssociationType = field.getType();
            } else if (oneToMany != null || manyToMany != null) {

                mappedBy = oneToMany != null
                        ? oneToMany.mappedBy() : manyToMany != null ? manyToMany.mappedBy() : null;
                inverseAssociationType = ClassUtils.resolveGenericType(field);
            }

            if (inverseAssociationType == null || Strings.isNullOrEmpty(mappedBy)) {
                return false;
            }

            final Class<?> expectedType = field.getDeclaringClass();

            while (inverseAssociationType != null) {
                try {
                    final Class<?> mappedByType = resolveType(inverseAssociationType, mappedBy, manyToMany != null);

                    if (expectedType.isAssignableFrom(mappedByType) || mappedByType.isAssignableFrom(expectedType)) {
                        return false;
                    }

                } catch (final NoSuchFieldException e) {}

                inverseAssociationType = inverseAssociationType.getSuperclass();
            }

            return true;
        });

        assertNoFields(failedFields,
                "These entity fields have incorrectly set mappedBy attribute on @OneToOne/@OneToMany/@ManyToMany " +
                        "annotation: ");
    }

    /**
     * Persistent classes must have a no-argument constructor with at least
     * package visibility for runtime proxy generation in Hibernate.
     */
    @Test
    public void allManagedJpaTypesMustHaveNoArgConstructorWithAtLeastPackageVisibility() {
        final Stream<Class<?>> failedTypes = getJpaClassStream()
                .filter(type -> Stream.of(type.getDeclaredConstructors()).noneMatch(ctor -> {
                    return ctor.getParameterTypes().length == 0 && !Modifier.isPrivate(ctor.getModifiers());
                }));

        assertNoClasses(failedTypes,
                "These JPA types should have a no-argument constructor with at least package visibility: ");
    }

    /**
     * Hibernate JPA meta model generator requires field access in order to
     * function properly.
     */
    @Test
    public void allManagedJpaTypesMustHaveFieldAccessType() {
        assertNoClasses(
                getJpaClassStream().filter(type -> !classHasFieldAccessType(type)),
                "These JPA types should be annotated with @Access(AccessType.FIELD): ");
    }

    /**
     * In JPA, @ManyToOne and @OneToOne associations are fetched eagerly by
     * default. That will, in most situations, cause performance problems.
     * Because of that, the default behavior shall be changed to lazy fetching
     * which this test enforces. On some rare occasions, the developer may want
     * to apply eager fetching and for these cases @EagerFetch annotation is
     * available for indicating that the intention is purposeful and should be
     * passed by this test.
     */
    @Test
    public void nonInverseToOneAssociationsMustBeLazilyFetched() {
        final Stream<Field> failedFields = filterFieldsOfManagedJpaTypes(field -> {
            final OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            final ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);

            return !isIdField(field) && !field.isAnnotationPresent(EagerFetch.class) &&
                    (manyToOne != null && manyToOne.fetch() == FetchType.EAGER ||
                            oneToOne != null && "".equals(oneToOne.mappedBy()) && oneToOne.fetch() == FetchType.EAGER);
        });

        assertNoFields(failedFields,
                "These entity fields should either have \"fetch = FetchType.LAZY\" parameter set on @ManyToOne/" +
                        "@OneToOne annotation or alternatively be annotated with @EagerFetch: ");
    }

    /**
     * To differentiate @OneToOne associations from @ManyToOne ones.
     */
    @Test
    public void nonInverseOneToOneAssociationsMustHaveUniqueJoinColumn() {
        final Stream<Field> failedFields = filterFieldsOfManagedJpaTypes(field -> {
            final OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            final JoinColumn joinCol = field.getAnnotation(JoinColumn.class);

            return !isIdField(field) && oneToOne != null && "".equals(oneToOne.mappedBy()) &&
                    (joinCol == null || !joinCol.unique());
        });

        assertNoFields(failedFields, "These entity fields should be annotated with @JoinColumn(unique = true): ");
    }

    @Test
    public void nullableConstraintsMustBeMutuallyConsistent() {
        final Stream<Field> failedFields = filterFieldsOfManagedJpaTypes(field -> {
            if (field.isAnnotationPresent(Version.class) || field.isAnnotationPresent(ElementCollection.class)) {
                return false;
            }

            final boolean isPrimitive = field.getType().isPrimitive();

            final boolean notNull = field.isAnnotationPresent(NotNull.class);
            final boolean notEmpty = field.isAnnotationPresent(NotEmpty.class);
            final boolean notBlank = field.isAnnotationPresent(NotBlank.class);

            final boolean notNullOrEmpty = notNull || notEmpty || notBlank;

            final boolean embedded = field.isAnnotationPresent(Embedded.class);

            final Column column = field.getAnnotation(Column.class);
            final ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
            final OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            final JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

            if (column != null) {
                if (manyToOne != null && column.nullable() != manyToOne.optional() ||
                        oneToOne != null && column.nullable() != oneToOne.optional() ||
                        joinColumn != null && column.nullable() != joinColumn.nullable() ||
                        column.nullable() && (notNullOrEmpty || isPrimitive) ||
                        !isPrimitive && column.insertable() && !column.nullable() && !notNullOrEmpty) {

                    return true;
                }
            }
            if (joinColumn != null) {
                if (manyToOne != null && joinColumn.nullable() != manyToOne.optional() ||
                        oneToOne != null && joinColumn.nullable() != oneToOne.optional() ||
                        joinColumn.nullable() && notNull ||
                        joinColumn.insertable() && !joinColumn.nullable() && !notNull) {

                    return true;
                }
            }
            if (manyToOne != null) {
                if (!isIdField(field) && manyToOne.optional() == notNull) {
                    return true;
                }
            }
            if (oneToOne != null) {
                if (!isIdField(field) && oneToOne.optional() == notNull) {
                    return true;
                }
            }
            if (notNullOrEmpty && !embedded) {
                if (column == null && joinColumn == null && manyToOne == null && oneToOne == null) {
                    return true;
                }
            }

            return false;
        });

        assertNoFields(failedFields,
                "Entity fields should have consistency with regard to:\n" +
                        "    (1) Presence of @NotNull, @NotBlank or @NotEmpty\n" +
                        "    (2) Values of @Column.nullable, @ManyToOne.optional, @OneToOne.optional and " +
                        "@JoinColumn.nullable\n" +
                        "    (3) Whether the type of field is primitive or not\n" +
                        "  These fields fail: ");
    }

    @Test
    public void stringFieldsMustHaveExplicitAndConsistentLengthDefinition() {
        final Stream<Field> failedFields = filterFieldsOfManagedJpaTypes(field -> {
            final int modifiers = field.getModifiers();

            if (String.class.isAssignableFrom(field.getType()) &&
                    !Modifier.isStatic(modifiers) &&
                    !Modifier.isTransient(modifiers) &&
                    !field.isAnnotationPresent(Transient.class) &&
                    !field.isAnnotationPresent(Lob.class)) {

                final Column column = field.getAnnotation(Column.class);
                final Size size = field.getAnnotation(Size.class);

                return column == null && !hasIdGetter(field) ||
                        column != null && size != null && column.length() != size.max();
            }

            return false;
        });

        assertNoFields(failedFields,
                "These entity fields should be explicitly annotated with @Column and @Size with consistency on " +
                        "field's maximum length: ");
    }

    @Test
    public void primitiveTypesShouldNotBeAnnotatedWithNotNullOrEmpty() {
        final Stream<Field> failedFields = filterFieldsOfManagedJpaTypes(field -> {
            final boolean notNull = field.isAnnotationPresent(NotNull.class);
            final boolean notEmpty = field.isAnnotationPresent(NotEmpty.class);
            final boolean notBlank = field.isAnnotationPresent(NotBlank.class);

            final boolean notNullOrEmpty = notNull || notEmpty || notBlank;
            final boolean isPrimitive = field.getType().isPrimitive();

            return isPrimitive && notNullOrEmpty;
        });

        assertNoFields(failedFields,
                "Primitive entity fields should not be annotated with @NotNull, @NotEmpty or @NotBlank:\n" +
                        "  These fields fail: ");
    }

    private static Stream<Field> filterFieldsOfManagedJpaTypes(final Predicate<Field> filter) {
        return getJpaClassStream().flatMap(fieldsOfClass(false)).filter(filter);
    }

    private static Stream<Class<?>> getJpaClassStream() {
        return ClassInventory.getManagedJpaClasses().stream();
    }

    private static boolean isIdField(final Field field) {
        return field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(PrimaryKeyJoinColumn.class);
    }

    private static boolean hasIdGetter(final Field field) {
        try {
            final BeanInfo info = Introspector.getBeanInfo(field.getDeclaringClass());

            for (final PropertyDescriptor pd : info.getPropertyDescriptors()) {
                if (pd.getDisplayName().equals(field.getName()) &&
                        pd.getReadMethod() != null &&
                        pd.getReadMethod().isAnnotationPresent(Id.class) &&
                        methodHasPropertyAccessType(pd.getReadMethod())) {

                    return true;
                }
            }
        } catch (final IntrospectionException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean classHasFieldAccessType(final Class<?> type) {
        final Access access = type.getAnnotation(Access.class);
        return access != null && access.value() == AccessType.FIELD;
    }

    private static boolean methodHasPropertyAccessType(final Method method) {
        final Access access = method.getAnnotation(Access.class);
        return access != null && access.value() == AccessType.PROPERTY;
    }

    private static Class<?> resolveType(Class<?> type, final String propertyPath, final boolean isGeneric)
            throws NoSuchFieldException {

        for (final String path : propertyPath.split("\\.")) {
            final Field field = type.getDeclaredField(path);
            type = isGeneric ? ClassUtils.resolveGenericType(field) : field.getType();
        }

        return type;
    }

    private static void assertNoClasses(final Stream<Class<?>> classes, final String message) {
        assertEmpty(classes.map(Class::getName), message);
    }

    private static void assertNoFields(final Stream<Field> fields, final String message) {
        assertEmpty(
                fields.map(field -> String.format("%s.%s", field.getDeclaringClass().getSimpleName(), field.getName())),
                message);
    }

}
