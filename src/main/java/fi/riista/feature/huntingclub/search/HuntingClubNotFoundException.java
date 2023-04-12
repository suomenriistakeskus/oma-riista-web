package fi.riista.feature.huntingclub.search;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static java.lang.String.format;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class HuntingClubNotFoundException extends RuntimeException {
    public static HuntingClubNotFoundException byOfficialCode(final String officialCode) {
        final String errorMessage = format("HuntingClub not found by officialCode: %s", officialCode);
        return new HuntingClubNotFoundException(errorMessage);
    }

    public static HuntingClubNotFoundException byId(final Long huntingClubId) {
        final String errorMessage = format("HuntingClub not found by id: %s", huntingClubId);
        return new HuntingClubNotFoundException(errorMessage);
    }

    private HuntingClubNotFoundException(final String errorMessage) {
        super(errorMessage);
    }
}
