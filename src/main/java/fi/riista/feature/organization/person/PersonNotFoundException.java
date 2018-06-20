package fi.riista.feature.organization.person;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PersonNotFoundException extends RuntimeException {
    public static PersonNotFoundException create(final PersonWithHunterNumberDTO dto) {
        if (dto.getId() != null) {
            return new PersonNotFoundException(dto.getId());
        } else {
            return new PersonNotFoundException(dto.getHunterNumber());
        }
    }

    private final String hunterNumber;
    private final Long personId;

    public PersonNotFoundException(String hunterNumber) {
        super(String.format("Person not found by hunterNumber: %s", hunterNumber));
        this.hunterNumber = hunterNumber;
        this.personId = null;
    }

    public PersonNotFoundException(long personId) {
        super(String.format("Person not found by personId: %d", personId));
        this.personId = personId;
        this.hunterNumber = null;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public Long getPersonId() {
        return personId;
    }
}
