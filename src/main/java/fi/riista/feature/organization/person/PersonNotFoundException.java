package fi.riista.feature.organization.person;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

import static java.lang.String.format;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PersonNotFoundException extends RuntimeException {

    public static PersonNotFoundException create(final PersonWithHunterNumberDTO dto) {
        return Optional
                .ofNullable(dto.getId())
                .map(personId -> byPersonId(personId))
                .orElseGet(() -> byHunterNumber(dto.getHunterNumber()));
    }

    public static PersonNotFoundException byHunterNumber(final String hunterNumber) {
        final String errorMessage = format("Person not found by hunterNumber: %s", hunterNumber);
        return new PersonNotFoundException(errorMessage, hunterNumber, null);
    }

    public static PersonNotFoundException byPersonId(final Long personId) {
        final String errorMessage = format("Person not found by personId: %s", personId);
        return new PersonNotFoundException(errorMessage, null, personId);
    }

    public static PersonNotFoundException bySsn(final String ssn) {
        return new PersonNotFoundException("Person not found by SSN", null, null);
    }

    public static PersonNotFoundException foreignPersonNotEligible(final Person person) {
        final long personId = person.getId();
        final String errorMessage = format("Found person (id=%d) but foreigner is not eligible", personId);
        return new PersonNotFoundException(errorMessage, person.getHunterNumber(), personId);
    }

    private final String hunterNumber;
    private final Long personId;

    private PersonNotFoundException(final String errorMessage, final String hunterNumber, final Long personId) {
        super(errorMessage);
        this.hunterNumber = hunterNumber;
        this.personId = personId;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public Long getPersonId() {
        return personId;
    }
}
