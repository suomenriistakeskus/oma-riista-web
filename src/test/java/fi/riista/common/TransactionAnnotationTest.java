package fi.riista.common;

import static fi.riista.util.Asserts.assertEmpty;
import static java.util.stream.Collectors.toSet;

import fi.riista.ClassInventory;
import fi.riista.util.F;
import fi.riista.util.ReflectionTestUtils;

import javaslang.Tuple;
import javaslang.Tuple2;

import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TransactionAnnotationTest {

    private static final Predicate<Method> MANDATORY_TRANSACTIONAL_PROPAGATION =
            method -> method.getAnnotation(Transactional.class).propagation() == Propagation.MANDATORY;

    /**
     * This test asserts that each public method appearing in an abstract or
     * concrete class inside feature package and annotated with @Transactional
     * has the method's declared checked exceptions included in the rollbackFor
     * attribute. This is because, by default, Spring's @Transactional-
     * annotated methods rollback only on RuntimeExceptions, which in most cases
     * is not a desired behavior.
     */
    @Test
    public void testTransactionalPublicMethodsRollbackOnCheckedExceptionIfNoMandatoryPropagation() {
        final Set<Method> methodsToTest = streamPublicTransactionalFeatureMethods()
                .filter(MANDATORY_TRANSACTIONAL_PROPAGATION.negate())
                .collect(toSet());

        final Map<Method, Set<Class<? extends Exception>>> rollbackingCheckedExceptions =
                F.toMapAsKeySet(methodsToTest, method -> Stream.of(method.getAnnotation(Transactional.class))
                        .map(Transactional::rollbackFor)
                        .flatMap(TransactionAnnotationTest::streamCheckedExceptions)
                        .collect(toSet()));

        final Predicate<Method> failureTest =
                method -> streamCheckedExceptions(method.getExceptionTypes()).anyMatch(declaredCheckedException -> {
                    return !findClassOrItsAncestor(
                            declaredCheckedException, rollbackingCheckedExceptions.get(method)).isPresent();
                });

        assertEmpty(methodsToTest.stream().filter(failureTest),
                "The following methods should include checked exceptions in rollbackFor attribute of "
                        + "@Transactional annotation: ");
    }

    /**
     * This test asserts that each public method appearing in an abstract or
     * concrete class inside feature package and annotated with @Transactional
     * such that propagation is set to mandatory includes RuntimeException or
     * Exception class in the noRollbackFor attribute. This is because, by
     * default, Spring's @Transactional- annotated methods rollback on
     * RuntimeExceptions, which in most cases may not be a desired behavior
     * within nested call hierarchies of transactional methods.
     */
    @Test
    public void testTransactionalPublicMethodsWithMandatoryPropagationDoNotRollbackOnRuntimeException() {
        final Stream<Method> failingMethods = streamPublicTransactionalFeatureMethods()
                .filter(MANDATORY_TRANSACTIONAL_PROPAGATION)
                .map(method -> Tuple.of(method, method.getAnnotation(Transactional.class).noRollbackFor()))
                .filter(pair -> Stream.of(pair._2()).noneMatch(
                        cls -> RuntimeException.class.isAssignableFrom(cls) ||
                                cls.isAssignableFrom(Exception.class)))
                .map(Tuple2::_1);

        assertEmpty(
                failingMethods,
                "The following methods should include RuntimeExceptions in noRollbackFor attribute of "
                        + "@Transactional annotation: ");
    }

    private static Stream<Method> streamPublicTransactionalFeatureMethods() {
        return ClassInventory.getClassesFromFeatureAndIntegrationPackage().stream()
                .filter(ReflectionTestUtils.ABSTRACT_OR_CONCRETE_CLASS)
                .flatMap(ReflectionTestUtils.methodsOfClass(true))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(Transactional.class));
    }

    private static Stream<Class<? extends Exception>> streamCheckedExceptions(final Class<?>[] classArray) {
        return Stream.of(classArray)
                .filter(cls -> Exception.class.isAssignableFrom(cls) && !RuntimeException.class.isAssignableFrom(cls))
                .map(cls -> cls.asSubclass(Exception.class));
    }

    private static <T> Optional<Class<?>> findClassOrItsAncestor(
            final Class<T> clazz, final Iterable<? extends Class<?>> candidates) {

        return F.stream(candidates)
                .filter(candidate -> candidate.isAssignableFrom(clazz))
                .<Class<?>> map(cls -> cls)
                .findFirst();
    }

}
