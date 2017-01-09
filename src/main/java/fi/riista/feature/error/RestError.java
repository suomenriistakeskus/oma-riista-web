package fi.riista.feature.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RestError {
    @JsonIgnore
    private final HttpStatus httpStatus;
    private final String status = "ERROR";
    private final String message;
    private final List<RestValidationError> validationErrors;

    public RestError(final Builder builder) {
        this.httpStatus = builder.status;
        this.message = builder.message;
        this.validationErrors = builder.validationErrors;
    }

    @JsonIgnore
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<RestValidationError> getValidationErrors() {
        return validationErrors;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class RestValidationError {
        private final String field;
        private final String fieldErrorCode;
        private final String errorMessage;

        public RestValidationError(final String field,
                                   final String fieldErrorCode,
                                   final String errorMessage) {
            this.field = field;
            this.fieldErrorCode = fieldErrorCode;
            this.errorMessage = errorMessage;
        }

        public String getField() {
            return field;
        }

        public String getFieldErrorCode() {
            return fieldErrorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class Builder {
        private HttpStatus status;
        private String message;
        private List<RestValidationError> validationErrors;

        public Builder(final HttpStatus status) {
            this.status = status;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder validationError(final String field,
                                       final String fieldErrorCode,
                                       final String errorMessage) {
            if (validationErrors == null) {
                this.validationErrors = new ArrayList<>();
            }
            validationErrors.add(new RestValidationError(field, fieldErrorCode, errorMessage));
            return this;
        }

        public RestError build() {
            if (this.status == null) {
                this.status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return new RestError(this);
        }
    }
}
