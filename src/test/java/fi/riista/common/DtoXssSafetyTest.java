package fi.riista.common;

import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.ReflectionTestUtils.fieldsOfClass;
import static fi.riista.util.ReflectionTestUtils.methodsOfClass;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import fi.riista.ClassInventory;
import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.common.dto.XssSafe;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.F;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishHuntingPermitNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import fi.riista.validation.PhoneNumber;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.junit.Test;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DtoXssSafetyTest {

    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends Annotation>> XSS_SAFE_MARKER_CLASSES = Sets.newHashSet(
            SafeHtml.class, Pattern.class, Email.class, FinnishHunterNumber.class, FinnishSocialSecurityNumber.class,
            FinnishHuntingPermitNumber.class, PhoneNumber.class, XssSafe.class);

    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends Annotation>> VALID_ANNOTATION_CLASSES =
            Sets.newHashSet(Valid.class, Validated.class);

    private static final Predicate<Class<?>> IS_DTO_CLASS =
            clazz -> BaseEntityDTO.class.isAssignableFrom(clazz) || nameEndsWithDto(clazz);

    private static final Predicate<Method> IS_METHOD_HAVING_UNVALIDATED_DTO_PARAMETER = method -> {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Annotation[][] annotations = method.getParameterAnnotations();

        for (int i = 0, numParameters = parameterTypes.length; i < numParameters; i++) {
            if (IS_DTO_CLASS.test(parameterTypes[i]) &&
                    !F.containsAny(VALID_ANNOTATION_CLASSES, getAnnotationClasses(annotations[i]))) {

                return true;
            }
        }

        return false;
    };

    /**
     * Test that the DTO parameters of public methods of API resource classes
     * are annotated with @Valid/@Validated.
     */
    @Test
    public void testDtoParametersOfApiResourceMethodsAreValidated() {
        final Stream<Method> failingMethods = getRequestMappingMethods()
                .filter(IS_METHOD_HAVING_UNVALIDATED_DTO_PARAMETER)
                .sorted(Ordering.usingToString());

        assertEmpty(failingMethods,
                "The following methods should have their DTO parameters annotated with @Valid or @Validated: ");
    }

    /**
     * Test that the fields of DTO classes declared as input parameters of
     * public API methods are annotated properly to countermeasure XSS
     * injection.
     */
    @Test
    public void testApiResourceParameterDtoFieldsAreXssSafe() {
        final Stream<Field> failingFields = getApiResourceParameterDtoClasses()
                .flatMap(fieldsOfClass(true))
                .filter(DtoXssSafetyTest::isOfStringTypeAndNotSafelyAnnotated)
                .sorted(Ordering.usingToString());

        assertEmpty(failingFields,
                "The following DTO string fields should have XSS injection preventing annotation: ");
    }

    /**
     * Ascertain that the DTO fields of DTO classes declared as input parameters
     * of public API methods are annotated with @Valid in order to
     * countermeasure XSS injection.
     */
    @Test
    public void testDtoFieldsOfDtoClassParametersOfApiResourceMethodsAreValidated() {
        @SuppressWarnings("unchecked")
        final Set<Class<? extends Annotation>> expectedAnnotations = Sets.newHashSet(Valid.class, DoNotValidate.class);

        final Stream<Field> failingFields = getApiResourceParameterDtoClasses()
                .flatMap(fieldsOfClass(true))
                .filter(field -> IS_DTO_CLASS.test(field.getType()))
                .filter(field -> !F.containsAny(expectedAnnotations, getAnnotationClasses(field.getAnnotations())))
                .sorted(Ordering.usingToString());

        assertEmpty(failingFields,
                "The following DTO fields should be annotated with @Valid or @DoNotValidate because the DTO type " +
                        "appears as a field in another DTO class that is used as a parameter type of ApiResource method: ");
    }

    private static Stream<Method> getRequestMappingMethods() {
        return ClassInventory.getApiResourceClasses().stream()
                .flatMap(methodsOfClass(true))
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .filter(method -> Modifier.isPublic(method.getModifiers()));
    }

    private static Stream<Class<?>> getApiResourceParameterDtoClasses() {
        return getRequestMappingMethods().flatMap(DtoXssSafetyTest::getParameterDtoClasses);
    }

    private static Stream<Class<?>> getParameterDtoClasses(final Method method) {
        final Set<Class<?>> ret = Stream.of(method.getParameterTypes())
                .filter(IS_DTO_CLASS)
                .collect(toSet());

        ret.addAll(getContainedDtoClasses(ret));
        return ret.stream();
    }

    private static Set<Class<?>> getContainedDtoClasses(final Set<Class<?>> classes) {
        final Set<Class<?>> containedDtoClasses = classes.stream()
                .flatMap(fieldsOfClass(true))
                .map(Field::getType)
                .filter(clazz -> !classes.contains(clazz))
                .filter(IS_DTO_CLASS)
                .collect(toSet());

        // Dive one step deeper into class hierarchy.
        if (!containedDtoClasses.isEmpty()) {
            containedDtoClasses.addAll(getContainedDtoClasses(containedDtoClasses));
        }

        return containedDtoClasses;
    }

    private static boolean nameEndsWithDto(final Class<?> clazz) {
        final String className = clazz.getSimpleName();
        final int nameLen = className.length();
        return nameLen >= 3 && "DTO".equalsIgnoreCase(className.substring(nameLen - 3, nameLen));
    }

    private static boolean isOfStringTypeAndNotSafelyAnnotated(final Field field) {
        return String.class.equals(field.getType()) &&
                !F.containsAny(XSS_SAFE_MARKER_CLASSES, getAnnotationClasses(field.getAnnotations()));
    }

    private static List<Class<? extends Annotation>> getAnnotationClasses(final Annotation[] annotations) {
        return Stream.of(annotations).map(Annotation::annotationType).collect(toList());
    }

}
