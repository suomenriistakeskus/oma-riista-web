package fi.riista.util;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ReflectionTestUtils {

    private static final boolean DEFAULT_INCLUDE_JAVA_LANG_OBJECT = false;

    // Predicates

    public static final Predicate<Class<?>> ABSTRACT_OR_CONCRETE_CLASS = clazz -> clazz != null &&
            !clazz.isInterface() &&
            !clazz.isAnonymousClass() &&
            !clazz.isEnum() &&
            !Throwable.class.isAssignableFrom(clazz);

    private ReflectionTestUtils() {
        throw new AssertionError();
    }

    // Methods returning functions

    @Nonnull
    public static Function<Class<?>, Stream<Field>> fieldsOfClass(final boolean includeSuperTypeFields) {
        return clazz -> clazz == null
                ? Stream.empty()
                : (includeSuperTypeFields ? getAllFields(clazz) : getFields(clazz)).stream();
    }

    @Nonnull
    public static Function<Class<?>, Stream<Method>> methodsOfClass(final boolean includeSuperTypeMethods) {
        return clazz -> clazz == null
                ? Stream.empty()
                : (includeSuperTypeMethods ? getAllMethods(clazz) : getMethods(clazz)).stream();
    }

    //
    // Following (utility) methods are adapted from org.reflections.ReflectionUtils class and
    // migrated to Java8 (under the WTFPL license).
    //

    @Nonnull
    public static Set<Field> getFields(@Nonnull final Class<?> type) {
        return Sets.newHashSet(Objects.requireNonNull(type).getDeclaredFields());
    }

    @Nonnull
    public static Set<Field> getAllFields(@Nullable final Class<?> type) {
        return getAllSuperTypes(type).stream()
                .flatMap(clazz -> getFields(clazz).stream())
                .collect(toSet());
    }

    @Nonnull
    public static Set<Method> getMethods(@Nonnull final Class<?> type) {
        Objects.requireNonNull(type);
        return Sets.newHashSet(type.isInterface() ? type.getMethods() : type.getDeclaredMethods());
    }

    @Nonnull
    public static Set<Method> getAllMethods(@Nullable final Class<?> type) {
        return getAllMethods(type, DEFAULT_INCLUDE_JAVA_LANG_OBJECT);
    }

    @Nonnull
    public static Set<Method> getAllMethods(@Nullable final Class<?> type, final boolean includeJavaLangObject) {
        return getAllSuperTypes(type, includeJavaLangObject).stream()
                .flatMap(clazz -> getMethods(clazz).stream())
                .collect(toSet());
    }

    @Nonnull
    public static Set<Class<?>> getAllSuperTypes(@Nullable final Class<?> type) {
        return getAllSuperTypes(type, DEFAULT_INCLUDE_JAVA_LANG_OBJECT);
    }

    @Nonnull
    public static Set<Class<?>> getAllSuperTypes(@Nullable final Class<?> type, final boolean includeJavaLangObject) {
        if (type == null || !includeJavaLangObject && type.equals(Object.class)) {
            return Collections.emptySet();
        }

        return Stream
                .concat(
                        Stream.concat(
                                Stream.of(type),
                                getAllSuperTypes(type.getSuperclass(), includeJavaLangObject).stream()),
                        Stream.of(type.getInterfaces())
                                .flatMap(ifc -> getAllSuperTypes(ifc, includeJavaLangObject).stream()))
                .collect(toSet());
    }

}
