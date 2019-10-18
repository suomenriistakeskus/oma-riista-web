package fi.riista.feature.huntingclub.permit.endofhunting;

import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import org.joda.time.LocalDate;

public class InvalidHuntingEndDateException extends RuntimeException {

    public static void assertDateValid(HarvestPermitSpeciesAmount speciesAmount, LocalDate date) {
        if (date != null && !speciesAmount.containsDate(date)) {
            throw new InvalidHuntingEndDateException();
        }
    }

}
