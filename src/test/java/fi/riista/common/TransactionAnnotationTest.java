package fi.riista.common;

import com.google.common.collect.Sets;
import fi.riista.ClassInventory;
import fi.riista.util.ClassUtils;
import fi.riista.util.F;
import fi.riista.util.ReflectionTestUtils;
import org.junit.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.ClassUtils.isRuntimeException;
import static fi.riista.util.Collect.mappingTo;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toSet;

public class TransactionAnnotationTest {

    private static final Predicate<Method> MANDATORY_TRANSACTIONAL_PROPAGATION =
            method -> method.getAnnotation(Transactional.class).propagation() == Propagation.MANDATORY;

    private static final Set<String> IMMUTABLE_METHOD_PREFIXES = Sets.newHashSet(
            "assert", "check", "collect", "count", "export", "fetch", "find", "get", "has", "is", "list", "load",
            "parse", "preload", "query", "read", "require", "resolve", "retrieve", "search", "validate", "verify");

    private static final Set<String> MUTABLE_METHOD_PREFIXES = Sets.newHashSet(
            "accept", "add", "assign", "associate", "audit", "cancel", "change", "copy", "create", "deactivate",
            "delete", "execute", "import", "link", "mark", "persist", "process", "purge", "register", "reject",
            "remove", "replace", "revoke", "save", "schedule", "send", "set", "store", "update", "upsert");

    private static final Set<String> WORDS_MARKING_MUTABLE_METHOD = Sets.newHashSet(
            "add", "create", "mark", "revoke", "send");

    private static final Set<String> WORDS_HINTING_IMMUTABLE_METHOD = Sets.newHashSet("exists", "has");

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
        final Set<Method> methodsToTest = streamPublicMethodsFromFeatureAndIntegrationPackage()
                .filter(method -> method.isAnnotationPresent(Transactional.class))
                .filter(MANDATORY_TRANSACTIONAL_PROPAGATION.negate())
                .collect(toSet());

        final Map<Method, Set<Class<? extends Exception>>> rollbackingCheckedExceptions =
                methodsToTest.stream().collect(mappingTo(method -> {

                    final Transactional txAnnot = method.getAnnotation(Transactional.class);

                    return streamCheckedExceptions(txAnnot.rollbackFor()).collect(toSet());
                }));

        final Predicate<Method> failureTest = method -> {
            return streamCheckedExceptions(method.getExceptionTypes()).anyMatch(declaredCheckedException -> {
                return !findClassOrItsAncestor(
                        declaredCheckedException, rollbackingCheckedExceptions.get(method)).isPresent();
            });
        };

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
    public void testTransactionalPublicMethodsWithMandatoryPropagationMustNotRollbackOnRuntimeException() {
        final Stream<Method> failingMethods = streamPublicMethodsFromFeatureAndIntegrationPackage()
                .filter(method -> method.isAnnotationPresent(Transactional.class))
                .filter(MANDATORY_TRANSACTIONAL_PROPAGATION)
                .filter(method -> {

                    return Arrays
                            .stream(method.getAnnotation(Transactional.class).noRollbackFor())
                            .noneMatch(cls -> isRuntimeException(cls) || cls.isAssignableFrom(Exception.class));
                });

        assertEmpty(failingMethods,
                "The following methods should include RuntimeExceptions in noRollbackFor attribute of "
                        + "@Transactional annotation: ");
    }

    @Test
    public void testMethodsOfRepositoryImplClassesMustBeAnnotatedWithTransactional() {
        final Stream<Method> failingMethods = streamPublicMethodsFromFeatureAndIntegrationPackage()
                .filter(method -> {
                    final Class<?> declaringClass = method.getDeclaringClass();

                    return isRepositoryClass(declaringClass) &&
                            !method.getName().equals("setDataSource") &&
                            !method.isAnnotationPresent(Transactional.class) &&
                            !declaringClass.isAnnotationPresent(Transactional.class);
                });

        assertEmpty(failingMethods, "The following methods should be annotated with @Transactional: ");
    }

    @Test
    public void testQueryMethodsMustBeReadOnly() {
        final Stream<Method> failingMethods = streamPublicMethodsFromFeatureAndIntegrationPackage()
                .filter(method -> {

                    final Class<?> clazz = method.getDeclaringClass();

                    final boolean methodAnnotated = method.isAnnotationPresent(Transactional.class);
                    final boolean classAnnotated = clazz.isAnnotationPresent(Transactional.class);

                    if (!methodAnnotated && !classAnnotated) {
                        return false;
                    }

                    final String methodName = method.getName().toLowerCase();

                    if (IMMUTABLE_METHOD_PREFIXES.stream().noneMatch(methodName::startsWith) &&
                            WORDS_HINTING_IMMUTABLE_METHOD.stream().noneMatch(methodName::contains) ||
                            MUTABLE_METHOD_PREFIXES.stream().anyMatch(methodName::startsWith) ||
                            WORDS_MARKING_MUTABLE_METHOD.stream().anyMatch(methodName::contains)) {

                        return false;
                    }

                    if (methodAnnotated) {
                        final Transactional mTx = method.getAnnotation(Transactional.class);
                        return mTx.propagation() != Propagation.MANDATORY && !mTx.readOnly();
                    }

                    final Transactional cTx = clazz.getAnnotation(Transactional.class);
                    return cTx.propagation() != Propagation.MANDATORY && !cTx.readOnly();
                });

        assertEmpty(failingMethods, "The following transactional methods should be marked read-only: ");
    }

    @Test
    public void testStateChangingMethodsMustNotBeExplicityMarkedReadOnly() {
        final Stream<Method> failingMethods = streamPublicMethodsFromFeatureAndIntegrationPackage()
                .filter(method -> {

                    final Class<?> clazz = method.getDeclaringClass();

                    final boolean methodAnnotated = method.isAnnotationPresent(Transactional.class);
                    final boolean classAnnotated = clazz.isAnnotationPresent(Transactional.class);

                    if (!methodAnnotated && !classAnnotated) {
                        return false;
                    }

                    final String methodName = method.getName().toLowerCase();

                    if (MUTABLE_METHOD_PREFIXES.stream().noneMatch(methodName::startsWith) ||
                            IMMUTABLE_METHOD_PREFIXES.stream().anyMatch(methodName::startsWith)) {

                        return false;
                    }

                    return methodAnnotated && method.getAnnotation(Transactional.class).readOnly();
                });

        assertEmpty(failingMethods, "The following transactional methods should not be marked read-only: ");
    }

    private static Stream<Method> streamPublicMethodsFromFeatureAndIntegrationPackage() {
        return ClassInventory.getClassesFromFeatureAndIntegrationPackage().stream()
                .filter(ReflectionTestUtils.ABSTRACT_OR_CONCRETE_CLASS)
                .flatMap(ReflectionTestUtils.methodsOfClass(true))
                .filter(method -> Modifier.isPublic(method.getModifiers()));
    }

    private static Stream<Class<? extends Exception>> streamCheckedExceptions(final Class<?>[] classArray) {
        return Arrays.stream(classArray)
                .filter(ClassUtils::isCheckedException)
                .map(cls -> cls.asSubclass(Exception.class));
    }

    private static <T> Optional<Class<?>> findClassOrItsAncestor(final Class<T> clazz,
                                                                 final Iterable<? extends Class<?>> candidates) {
        return F.stream(candidates)
                .filter(candidate -> candidate.isAssignableFrom(clazz))
                .<Class<?>> map(identity())
                .findFirst();
    }

    private static boolean isRepositoryClass(final Class<?> clazz) {
        final String className = clazz.getSimpleName();
        return !clazz.isInterface() && (className.endsWith("RepositoryImpl") || className.endsWith("Repository"));
    }
}
