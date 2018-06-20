package fi.riista.feature.harvestpermit;

import org.joda.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class HarvestPermitSpeciesAmountNotFound extends RuntimeException {
    private final String permitNumber;
    private final int gameSpeciesCode;
    private final LocalDate harvestDate;

    public HarvestPermitSpeciesAmountNotFound(final String permitNumber,
                                              final int gameSpeciesCode,
                                              final LocalDate harvestDate) {
        super(String.format("Species %d is not valid for permit %s on harvest date %s",
                gameSpeciesCode, permitNumber, harvestDate));
        this.permitNumber = permitNumber;
        this.gameSpeciesCode = gameSpeciesCode;
        this.harvestDate = harvestDate;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }
}
