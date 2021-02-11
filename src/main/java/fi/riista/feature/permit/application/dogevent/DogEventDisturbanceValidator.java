package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.gamediary.GameSpecies;

import static java.util.Objects.requireNonNull;

public class DogEventDisturbanceValidator {

    public static void validateContent(final DogEventDisturbance event) {

        if (!event.isSkipped()) {
            validateGameSpecies(event.getGameSpecies());
        }
    }

    private static void validateGameSpecies(final GameSpecies gameSpecies) {

        requireNonNull(gameSpecies);
        if (!GameSpecies.isDogEventDisturbanceSpecies(gameSpecies.getOfficialCode())) {
            throw new IllegalArgumentException("Given game species is not allowed.");
        }
    }
}
