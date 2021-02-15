package fi.riista.feature.gamediary.observation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ObservationLinkableToHuntingDayOnlyWithHuntingCategoryException extends RuntimeException {

    public static void assertWithinMooseHunting(final ObservationCategory category) {
        if (!category.isWithinMooseHunting()) {
            throw new ObservationLinkableToHuntingDayOnlyWithHuntingCategoryException();
        }
    }
}
