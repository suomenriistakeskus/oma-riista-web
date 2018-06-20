package fi.riista.feature.gamediary;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class GameSpeciesNotFoundException extends RuntimeException {
    private final int gameSpeciesCode;

    public GameSpeciesNotFoundException(final int gameSpeciesCode) {
        super(String.format("Species with code %d not found", gameSpeciesCode));
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }
}
