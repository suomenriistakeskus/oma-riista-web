package fi.riista.feature.harvestpermit;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class HarvestPermitNotFoundException extends RuntimeException {
    private final String permitNumber;

    public HarvestPermitNotFoundException(final String permitNumber) {
        super(String.format("Permit %s was not found", permitNumber));
        this.permitNumber = permitNumber;
    }

    public String getPermitNumber() {
        return permitNumber;
    }
}
