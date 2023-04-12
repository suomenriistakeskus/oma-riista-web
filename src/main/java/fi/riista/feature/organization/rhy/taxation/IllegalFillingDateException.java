package fi.riista.feature.organization.rhy.taxation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalFillingDateException extends RuntimeException {
}
