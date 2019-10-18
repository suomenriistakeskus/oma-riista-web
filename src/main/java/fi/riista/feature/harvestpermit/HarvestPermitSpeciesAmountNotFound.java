package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import org.joda.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static java.util.stream.Collectors.joining;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class HarvestPermitSpeciesAmountNotFound extends RuntimeException {

    public static HarvestPermitSpeciesAmountNotFound notFound(final String permitNumber,
                                                              final int gameSpeciesCode) {
        final String errorMessage = String.format("Could not find HarvestPermitSpeciesAmount by { permitNumber: %s, gameSpeciesCode: %d }",
                permitNumber, gameSpeciesCode);

        return new HarvestPermitSpeciesAmountNotFound(errorMessage, permitNumber, gameSpeciesCode, null);
    }

    public static HarvestPermitSpeciesAmountNotFound uniqueHuntingYearNotFound(final String permitNumber,
                                                                               final int gameSpeciesCode,
                                                                               final List<HarvestPermitSpeciesAmount> speciesAmounts) {
        final String huntingYears = Has2BeginEndDates.streamUniqueHuntingYearsSorted(speciesAmounts.stream())
                .mapToObj(String::valueOf)
                .collect(joining(","));

        final String errorMessage = String.format("Cannot resolve HarvestPermitSpeciesAmount unambiguously because multiple instances found for { permitNumber: %s, gameSpeciesCode: %d } for following hunting years: %s",
                permitNumber, gameSpeciesCode, huntingYears);

        return new HarvestPermitSpeciesAmountNotFound(errorMessage, permitNumber, gameSpeciesCode, null);
    }

    public static HarvestPermitSpeciesAmountNotFound harvestNotValidOn(final String permitNumber,
                                                                       final int gameSpeciesCode,
                                                                       final LocalDate harvestDate) {
        final String errorMessage = String.format("Species %d is not valid for permit %s on harvest date %s",
                gameSpeciesCode, permitNumber, harvestDate);
        return new HarvestPermitSpeciesAmountNotFound(errorMessage, permitNumber, gameSpeciesCode, harvestDate);
    }

    private final String permitNumber;
    private final int gameSpeciesCode;
    private final LocalDate harvestDate;

    private HarvestPermitSpeciesAmountNotFound(final String errorMessage,
                                               final String permitNumber,
                                               final int gameSpeciesCode,
                                               final LocalDate harvestDate) {
        super(errorMessage);
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
