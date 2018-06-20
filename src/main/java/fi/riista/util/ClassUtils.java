package fi.riista.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

public final class ClassUtils {

    @Nullable
    public static Class<?> resolveGenericType(@Nonnull final Field field) {
        Objects.requireNonNull(field);
        final Type[] actualTypeArgs = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        return actualTypeArgs.length == 1 ? (Class<?>) actualTypeArgs[0] : null;
    }

    @Nonnull
    public static Class<?> getTypeArgumentOfSuperClass(@Nonnull final Object object,
                                                       @Nonnull final Class<?> superClass,
                                                       final int typeArgumentIndex) {

        Objects.requireNonNull(object, "object is null");
        Objects.requireNonNull(superClass, "superClass is null");
        Preconditions.checkArgument(typeArgumentIndex >= 0, "typeArgumentIndex must not be negative");

        final Optional<ParameterizedType> paramType =
                findParameterizedParentTypeOfClass(object.getClass(), superClass);

        if (!paramType.isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "Did not find parameterized interface of class %s for object of class %s",
                    superClass.getName(),
                    object.getClass().getName()));
        }

        final Type[] typeArguments = paramType.get().getActualTypeArguments();

        if (typeArguments.length == 0) {
            throw new IllegalArgumentException("The interface does not have type arguments: " + superClass.getName());
        }

        return (Class<?>) typeArguments[typeArgumentIndex];
    }

    @Nonnull
    public static Optional<ParameterizedType> findParameterizedParentTypeOfClass(@Nonnull final Class<?> clazz,
                                                                                 @Nonnull final Class<?> expectedType) {

        Objects.requireNonNull(clazz, "clazz is null");
        Objects.requireNonNull(expectedType, "expectedType is null");

        final Type genericSuperclass = clazz.getGenericSuperclass();
        final Type[] genericInterfaces = clazz.getGenericInterfaces();

        for (final Type type : Lists.asList(genericSuperclass, genericInterfaces)) {
            if (type != null) {
                if (type instanceof ParameterizedType) {
                    final ParameterizedType parameterizedType = (ParameterizedType) type;
                    final Type rawType = parameterizedType.getRawType();

                    if (rawType instanceof Class) {
                        final Class<?> rawClass = (Class<?>) rawType;

                        if (expectedType.equals(rawClass)) {
                            return Optional.of(parameterizedType);
                        }

                        final Optional<ParameterizedType> match =
                                findParameterizedParentTypeOfClass(rawClass, expectedType);
                        if (match.isPresent()) {
                            return match;
                        }
                    }
                } else if (type instanceof Class) {
                    final Optional<ParameterizedType> match =
                            findParameterizedParentTypeOfClass((Class<?>) type, expectedType);
                    if (match.isPresent()) {
                        return match;
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static boolean isRuntimeException(@Nonnull final Class<?> clazz) {
        return RuntimeException.class.isAssignableFrom(clazz);
    }

    public static boolean isCheckedException(@Nonnull final Class<?> clazz) {
        return Exception.class.isAssignableFrom(clazz) && !isRuntimeException(clazz);
    }

    @Nonnull
    public static <T> Optional<T> cast(@Nullable final Object obj, @Nonnull final Class<T> refClass) {
        Objects.requireNonNull(refClass, "refClass is null");

        return Optional.ofNullable(
                obj != null && refClass.isAssignableFrom(obj.getClass()) ? refClass.cast(obj) : null);
    }

    private ClassUtils() {
        throw new AssertionError();
    }
}
