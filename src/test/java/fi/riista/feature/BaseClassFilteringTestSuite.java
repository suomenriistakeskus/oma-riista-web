package fi.riista.feature;

import fi.riista.ClassInventory;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class BaseClassFilteringTestSuite extends Suite {

    private static Class<?>[] getSuiteClasses(final Class<?> clazz) throws InitializationError {
        final BaseTypeFilter baseClassAnnot = clazz.getAnnotation(BaseTypeFilter.class);

        if (baseClassAnnot == null) {
            throw new InitializationError(String.format("Suite class %s must be annotated with: %s",
                    clazz.getSimpleName(), BaseTypeFilter.class.getSimpleName()));
        }

        final boolean hasInclusions = baseClassAnnot.includes().length > 0;

        if (hasInclusions && baseClassAnnot.excludes().length > 0) {
            throw new InitializationError(String.format(
                    "Suite class %s must define either includes or excludes via %s, but not both",
                    clazz.getSimpleName(), BaseTypeFilter.class.getSimpleName()));
        }

        return ClassInventory.getJUnitTestClasses().stream()
                .filter(hasInclusions
                        ? isSubclassOfAny(baseClassAnnot.includes())
                        : isSubclassOfAny(baseClassAnnot.excludes()).negate())
                .toArray(Class<?>[]::new);
    }

    private static Predicate<Class<?>> isSubclassOfAny(@Nonnull final Class<?>[] baseClasses) {
        return clazz -> Optional.ofNullable(clazz)
                .map(c -> Arrays.stream(baseClasses).anyMatch(baseClass -> isFirstSubclassOfSecond(c, baseClass)))
                .orElse(false);
    }

    private static boolean isFirstSubclassOfSecond(final Class<?> first, final Class<?> second) {
        return !first.equals(second) && second.isAssignableFrom(first);
    }

    public BaseClassFilteringTestSuite(final Class<?> clazz, final RunnerBuilder builder) throws InitializationError {
        super(builder, clazz, getSuiteClasses(clazz));
    }

}
