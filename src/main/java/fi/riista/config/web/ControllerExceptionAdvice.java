package fi.riista.config.web;

import fi.riista.feature.error.RestError;
import fi.riista.feature.error.RestErrorService;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ControllerExceptionAdvice extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionAdvice.class);

    @Resource
    private RestErrorService restErrorService;

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleAll(final Throwable ex) {
        LOG.error("Exception caught", ex);

        Sentry.capture(ex);

        final RestError restError = restErrorService.exposeGeneralException(ex);

        return new ResponseEntity<>(restError, restError.getHttpStatus());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex,
                                                             final Object body,
                                                             final HttpHeaders headers,
                                                             final HttpStatus status,
                                                             final WebRequest request) {
        LOG.error("Spring MVC exception was caught", ex);

        Sentry.capture(ex);

        final RestError restError = restErrorService.exposeOtherSpringError(ex, status, request);

        return new ResponseEntity<>(restError, headers, restError.getHttpStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                  final HttpHeaders headers,
                                                                  final HttpStatus status,
                                                                  final WebRequest request) {
        LOG.error("Spring MVC parameter validation failed", ex);

        Sentry.capture(ex);

        final RestError restError = restErrorService.exposeMethodArgumentError(ex, status);

        return new ResponseEntity<>(restError, headers, restError.getHttpStatus());
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<RestError> handleConstraintViolationException(final ConstraintViolationException cve) {
        LOG.error("Constraint violation failure", cve);

        Sentry.capture(cve);

        final RestError restError = restErrorService.exposeConstraintViolation(cve);

        return new ResponseEntity<>(restError, restError.getHttpStatus());
    }
}
