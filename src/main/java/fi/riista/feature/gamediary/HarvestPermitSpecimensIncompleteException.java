package fi.riista.feature.gamediary;

public class HarvestPermitSpecimensIncompleteException extends RuntimeException {

    public HarvestPermitSpecimensIncompleteException(final String msg) {
        super(msg);
    }

    public static void assertTrue(boolean value, String errorMessage) {
        if (!value) {
            throw new HarvestPermitSpecimensIncompleteException(errorMessage);
        }
    }
}
