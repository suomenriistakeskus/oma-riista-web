package fi.riista;

import static org.reflections.ReflectionUtils.getAll;
import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import fi.riista.config.Constants;
import fi.riista.util.ResourceUtils;

import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Set;

public final class ClassInventory {

    private static final Set<Class<?>> MAIN_CLASSES;
    private static final Set<Class<?>> TEST_CLASSES;

    static {
        MAIN_CLASSES = ResourceUtils.getClasses(Constants.APPLICATION_ROOT_PACKAGE, "/target/classes/");
        TEST_CLASSES = ResourceUtils.getClasses(Constants.APPLICATION_ROOT_PACKAGE, "/target/test-classes/");
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Set<Class<?>> getMainClasses(@Nonnull final Predicate<Class<?>> predicate) {
        return getAll(MAIN_CLASSES, Objects.requireNonNull(predicate));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Set<Class<?>> getTestClasses(@Nonnull final Predicate<Class<?>> predicate) {
        return getAll(TEST_CLASSES, Objects.requireNonNull(predicate));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Set<Class<?>> getManagedJpaClasses() {
        return getMainClasses(Predicates.or(
                withAnnotation(Entity.class),
                withAnnotation(Embeddable.class),
                withAnnotation(MappedSuperclass.class)));
    }

    @Nonnull
    public static Set<Class<?>> getApiResourceClasses() {
        return getMainClasses(ClassNamePredicate.endsWith("ApiResource"));
    }

    @Nonnull
    public static Set<Class<?>> getClassesFromFeatureAndIntegrationPackage() {
        return getMainClasses(Predicates.or(
                ClassNamePredicate.startsWith(Constants.FEATURE_BASE_PACKAGE),
                ClassNamePredicate.startsWith(Constants.INTEGRATION_BASE_PACKAGE)));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Set<Class<?>> getJUnitTestClasses() {
        return getTestClasses(clazz -> {
            return clazz != null &&
                    !Modifier.isAbstract(clazz.getModifiers()) &&
                    !getAllMethods(clazz, withAnnotation(Test.class)).isEmpty();
        });
    }

    private ClassInventory() {
        throw new AssertionError();
    }

    @FunctionalInterface
    private interface ClassNamePredicate extends Predicate<Class<?>> {

        @Nonnull
        static ClassNamePredicate contains(@Nonnull final String str) {
            Objects.requireNonNull(str);
            return className -> className != null && className.contains(str);
        }

        @Nonnull
        static ClassNamePredicate notContaining(@Nonnull final String str) {
            return not(contains(str));
        }

        @Nonnull
        static ClassNamePredicate startsWith(@Nonnull final String str) {
            Objects.requireNonNull(str);
            return className -> className != null && className.startsWith(str);
        }

        @Nonnull
        static ClassNamePredicate notStartingWith(@Nonnull final String str) {
            return not(startsWith(str));
        }

        @Nonnull
        static ClassNamePredicate endsWith(@Nonnull final String str) {
            Objects.requireNonNull(str);
            return className -> className != null && className.endsWith(str);
        }

        @Nonnull
        static ClassNamePredicate notEndingWith(@Nonnull final String str) {
            return not(endsWith(str));
        }

        @Nonnull
        static ClassNamePredicate not(@Nonnull final ClassNamePredicate predicate) {
            Objects.requireNonNull(predicate);
            return className -> !predicate.apply(className);
        }

        @Override
        default boolean apply(@Nullable final Class<?> clazz) {
            return clazz != null && apply(clazz.getName());
        }

        boolean apply(@Nullable String className);
    }

}
