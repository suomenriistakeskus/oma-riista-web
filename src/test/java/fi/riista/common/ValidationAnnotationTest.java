package fi.riista.common;

import fi.riista.ClassInventory;
import fi.riista.config.Constants;
import fi.riista.util.F;
import fi.riista.util.ReflectionTestUtils;
import fi.riista.validation.DoNotValidate;
import fi.riista.validation.XssSafe;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.ReflectionTestUtils.fieldsOfClass;
import static fi.riista.util.ReflectionTestUtils.methodsOfClass;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ValidationAnnotationTest {

    private static final List<Class<? extends Annotation>> VALIDATION_ANNOTATIONS =
            Arrays.asList(Valid.class, Validated.class, DoNotValidate.class);

    private static final List<Class<? extends Annotation>> SAFE_STRING_ANNOTATIONS =
            Arrays.asList(SafeHtml.class, Pattern.class, Email.class);

    /**
     * Tests that public API parameters annotated with either @RequestBody
     * or @ModelAttribute are also annotated with one of: @Valid, @Validated
     * or @DoNotValidate.
     */
    @Test
    public void testThatApiResourceMethodsEnableValidationOnInputParameters() {
        final Stream<Method> failingMethods = streamRequestMappingMethods()
                .filter(method -> {

                    return findTopLevelParametersRequiringValidation(method)
                            .map(Tuple3::_3)
                            .anyMatch(paramAnnotations -> !F.containsAny(paramAnnotations, VALIDATION_ANNOTATIONS));
                });

        assertEmpty(failingMethods,
                "The following methods are missing validation annotation on their @ModelAttribute/@RequestBody annotated parameters: ");
    }

    /**
     * Tests that String fields of validated input parameter types of public API
     * methods are properly annotated to prevent XSS injection.
     */
    @Test
    public void testXssSafetyOfStringFieldsInInputParameterTypeGraph() {
        final Stream<Field> failingFields = streamFieldsFromClassesOfInputParameterTypeGraph()
                .filter(ValidationAnnotationTest::isOfStringTypeAndNotSafelyAnnotated);

        assertEmpty(failingFields, "The following String fields should have XSS injection preventing annotation: ");
    }

    /**
     * Tests that embedded object fields of validated input parameter types of
     * public API methods are annotated with @Valid or @DoNotValidate.
     */
    @Test
    public void testValidationAnnotationPresenceInInputParameterTypeGraph() {
        final List<Class<? extends Annotation>> expectedAnnotations = Arrays.asList(Valid.class, DoNotValidate.class);

        final Stream<Field> failingFields = streamFieldsFromClassesOfInputParameterTypeGraph()
                .filter(ValidationAnnotationTest::isValidateableField)
                .filter(field -> !F.containsAny(getAnnotationTypes(field.getAnnotations()), expectedAnnotations));

        assertEmpty(failingFields,
                "The following fields should be annotated with @Valid or @DoNotValidate because the declaring class " +
                        "is part of the input parameter type graph of public API: ");
    }

    /**
     * Tests that @Embedded object fields in JPA entity types are annotated
     * with @Valid.
     */
    @Test
    public void testEmbeddedObjectsAreValidatedWithinManagedJpaTypes() {
        final Stream<Field> failingFields = ClassInventory.getManagedJpaClasses().stream()
                .flatMap(fieldsOfClass(false))
                .filter(field -> field.isAnnotationPresent(Embedded.class))
                .filter(field -> !field.isAnnotationPresent(Valid.class));

        assertEmpty(failingFields, "The following @Embedded-annotated fields must also be annotated with @Valid: ");
    }

    @Test
    public void testThatAssertTrueAnnotatedMethodsStartWithIsWord() {
        final Stream<Method> failingMethods = ClassInventory.getMainClasses()
                .stream()
                .flatMap(ReflectionTestUtils.methodsOfClass(true))
                .filter(method -> method.isAnnotationPresent(AssertTrue.class))
                .filter(method -> !method.getName().startsWith("is"));

        assertEmpty(failingMethods, "The names of following @AssertTrue annotated methods should start with \"is\": ");
    }

    private static Stream<Method> streamRequestMappingMethods() {
        return ClassInventory.getPublicApiClasses().stream()
                .flatMap(methodsOfClass(true))
                .filter(method -> AnnotationUtils.findAnnotation(method, RequestMapping.class) != null)
                .filter(method -> Modifier.isPublic(method.getModifiers()));
    }

    private static Set<Class<?>> yieldClassesFromInputParameterTypeGraph() {
        final Set<Class<?>> classes = streamRequestMappingMethods()
                .flatMap(method -> {
                    return findTopLevelParametersRequiringValidation(method).flatMap(t -> findCustomTypes(t._1, t._2));
                })
                .flatMap(clazz -> {
                    final Stream.Builder<Class<?>> hierarchy = Stream.builder();
                    Class<?> cur = clazz;

                    while (cur != Object.class) {
                        hierarchy.add(cur);
                        cur = cur.getSuperclass();
                    }

                    return hierarchy.build();
                })
                .collect(toSet());

        classes.addAll(getEmbeddedCustomTypes(classes));

        return classes;
    }

    private static Set<Class<?>> getEmbeddedCustomTypes(final Set<Class<?>> classes) {
        final Set<Class<?>> embeddedCustomTypes = classes.stream()
                .flatMap(fieldsOfClass(true))
                .flatMap(field -> {
                    final Class<?> type = field.getType();

                    if (classes.contains(type) || AnnotationUtils.findAnnotation(field, DoNotValidate.class) != null) {
                        return Stream.empty();
                    }

                    return findCustomTypes(type, field.getGenericType()).filter(cls -> !classes.contains(cls));
                })
                .collect(toSet());

        // Dive one level deeper in class hierarchy.
        if (!embeddedCustomTypes.isEmpty()) {
            embeddedCustomTypes.addAll(getEmbeddedCustomTypes(embeddedCustomTypes));
        }

        return embeddedCustomTypes;
    }

    private static Stream<Field> streamFieldsFromClassesOfInputParameterTypeGraph() {
        return yieldClassesFromInputParameterTypeGraph()
                .stream()
                .flatMap(fieldsOfClass(false))
                .filter(field -> !Modifier.isStatic(field.getModifiers()));
    }

    private static Stream<Tuple3<Class<?>, Type, List<Class<? extends Annotation>>>> findTopLevelParametersRequiringValidation(
            final Method method) {

        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Type[] genericParameterTypes = method.getGenericParameterTypes();
        final Annotation[][] annotations = method.getParameterAnnotations();

        final Stream.Builder<Tuple3<Class<?>, Type, List<Class<? extends Annotation>>>> resultBuilder =
                Stream.builder();

        for (int i = 0, numParameters = parameterTypes.length; i < numParameters; i++) {
            final Class<?> parameterType = parameterTypes[i];
            final Type genericType = genericParameterTypes[i];
            final List<Class<? extends Annotation>> annotationTypes = getAnnotationTypes(annotations[i]);

            if (isValidateableType(parameterType, genericType)
                    && isValidationMandatedByRequestParameterModeling(parameterType, annotationTypes)) {

                resultBuilder.add(Tuple.of(parameterType, genericType, annotationTypes));
            }
        }

        return resultBuilder.build();
    }

    private static List<Class<? extends Annotation>> getAnnotationTypes(final Annotation[] annotations) {
        return Arrays.stream(annotations).map(Annotation::annotationType).collect(toList());
    }

    private static boolean isValidateableField(final Field field) {
        return isValidateableType(field.getType(), field.getGenericType());
    }

    private static boolean isValidateableType(final Class<?> parameterType, final Type genericParameterType) {
        return isValidateableCustomClass(parameterType)
                || isCollectionOfCustomTypes(parameterType, genericParameterType);
    }

    private static boolean isValidateableCustomClass(final Class<?> clazz) {
        return !clazz.isEnum() && clazz.getName().startsWith(Constants.APPLICATION_ROOT_PACKAGE);
    }

    private static boolean isValidateableCustomType(final Type type) {
        return type instanceof Class && isValidateableCustomClass((Class<?>) type);
    }

    private static boolean isCollectionOfCustomTypes(final Class<?> parameterType, final Type genericParameterType) {
        if (!isCollectionOrMap(parameterType)) {
            return false;
        }

        final ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;

        return Arrays
                .stream(parameterizedType.getActualTypeArguments())
                .anyMatch(ValidationAnnotationTest::isValidateableCustomType);
    }

    private static Stream<Class<?>> findCustomTypes(final Class<?> type, final Type genericType) {
        if (isValidateableCustomClass(type)) {
            return Stream.of(type);
        }

        return isCollectionOrMap(type) ? findCustomTypesFromCollectionTypeArguments(genericType) : Stream.empty();
    }

    private static Stream<Class<?>> findCustomTypesFromCollectionTypeArguments(final Type genericParameterType) {
        final ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;

        return Arrays
                .stream(parameterizedType.getActualTypeArguments())
                .filter(ValidationAnnotationTest::isValidateableCustomType)
                .map(typeArg -> (Class<?>) typeArg);
    }

    private static boolean isCollectionOrMap(final Class<?> type) {
        return Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }

    private static boolean isValidationMandatedByRequestParameterModeling(final Class<?> parameterType,
                                                                          final Collection<Class<? extends Annotation>> annotationTypes) {

        return AnnotationUtils.findAnnotation(parameterType, XmlRootElement.class) == null
                && F.containsAny(annotationTypes, ModelAttribute.class, RequestBody.class);
    }

    private static boolean isOfStringTypeAndNotSafelyAnnotated(final Field field) {
        return String.class.equals(field.getType())
                && !F.containsAny(getAnnotationTypes(field.getAnnotations()), SAFE_STRING_ANNOTATIONS)
                && AnnotationUtils.findAnnotation(field, XssSafe.class) == null;
    }
}
