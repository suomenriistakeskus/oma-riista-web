package fi.riista.feature.error;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.RuntimeEnvironmentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Map;

@Service
public class RestErrorService {
    private static final Logger LOG = LoggerFactory.getLogger(RestErrorService.class);

    private static final ImmutableMap<Class<?>, HttpStatus> exceptionToStatusMapping = ImmutableMap.<Class<?>, HttpStatus>builder()
            .put(ValidationException.class, HttpStatus.BAD_REQUEST)
            .put(NotFoundException.class, HttpStatus.NOT_FOUND)
            .put(EntityNotFoundException.class, HttpStatus.NOT_FOUND)
            .put(RevisionConflictException.class, HttpStatus.CONFLICT)
            .put(OptimisticLockingFailureException.class, HttpStatus.CONFLICT)
            .put(AccessDeniedException.class, HttpStatus.FORBIDDEN)
            .put(IllegalArgumentException.class, HttpStatus.BAD_REQUEST)
            .put(IllegalStateException.class, HttpStatus.INTERNAL_SERVER_ERROR)
            .build();

    private static HttpStatus getHttpStatusCode(final @Nonnull Throwable ex) {
        final ResponseStatus annotationStatusCode = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

        if (annotationStatusCode != null) {
            return annotationStatusCode.value();
        }

        for (final Map.Entry<Class<?>, HttpStatus> entry : exceptionToStatusMapping.entrySet()) {
            if (entry.getKey().isAssignableFrom(ex.getClass())) {
                return entry.getValue();
            }
        }

        LOG.warn("Unknown exception type: " + ex.getClass().getName());

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    public RestError exposeGeneralException(final Throwable ex) {
        final HttpStatus httpStatusCode = getHttpStatusCode(ex);

        final RestError.Builder builder = new RestError.Builder(httpStatusCode);

        if (shouldExposeExceptionMessage(ex) && StringUtils.hasText(ex.getMessage())) {
            builder.setMessage(ex.getMessage());
        } else {
            builder.setMessage(httpStatusCode.getReasonPhrase());
        }

        return builder.build();
    }

    private static boolean shouldExposeExceptionMessage(final @Nonnull Throwable ex) {
        return ex instanceof MessageExposableValidationException ||
                ex instanceof NotFoundException ||
                ex instanceof RevisionConflictException;
    }

    public RestError exposeConstraintViolation(final @Nonnull ConstraintViolationException cve) {
        final RestError.Builder builder = new RestError.Builder(HttpStatus.BAD_REQUEST)
                .setMessage("Constraint violation");

        if (runtimeEnvironmentUtil.isProductionEnvironment()) {
            return builder.build();
        }

        for (final ConstraintViolation<?> violation : cve.getConstraintViolations()) {
            final String fieldName = String.format("%s.%s",
                    violation.getRootBeanClass().getSimpleName(),
                    violation.getPropertyPath());
            final String fieldErrorCode = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            final String fieldErrorMessage;

            if (violation.getMessage() != null && !violation.getMessage().matches("^\\{.+\\}$")) {
                fieldErrorMessage = String.format("%s, was %s",
                        violation.getMessage(),
                        StringUtils.quoteIfString(violation.getInvalidValue()));
            } else {
                fieldErrorMessage = "";
            }

            builder.validationError(fieldName, fieldErrorCode, fieldErrorMessage);
        }

        return builder.build();
    }

    public RestError exposeMethodArgumentError(final @Nonnull MethodArgumentNotValidException ex,
                                               final @Nonnull HttpStatus status) {
        final RestError.Builder builder = new RestError.Builder(status).setMessage("Invalid request");

        for (final ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            builder.validationError(objectError.getObjectName(), objectError.getCode(), objectError.getDefaultMessage());
        }

        for (final FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            builder.validationError(fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
        }

        return builder.build();
    }

    public RestError exposeOtherSpringError(final @Nonnull Exception ex,
                                            final @Nonnull HttpStatus status,
                                            final @Nonnull WebRequest request) {
        return new RestError.Builder(status).setMessage(status.getReasonPhrase()).build();
    }
}
