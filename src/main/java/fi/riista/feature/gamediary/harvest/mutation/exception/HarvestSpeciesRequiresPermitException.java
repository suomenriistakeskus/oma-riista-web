package fi.riista.feature.gamediary.harvest.mutation.exception;

public class HarvestSpeciesRequiresPermitException extends RuntimeException {
    private final int gameSpeciesCode;

    public HarvestSpeciesRequiresPermitException(final int gameSpeciesCode) {
        super(String.format("Harvest species=%d requires permit", gameSpeciesCode));
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }
}
