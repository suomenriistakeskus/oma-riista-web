package fi.riista.feature.huntingclub.hunting.day;

public class NumberOfHoundsMissingException extends RuntimeException {

    public NumberOfHoundsMissingException(String message) {
        super(message);
    }

    public static void assertNumberOfHoundsRequired(Integer numberOfHounds) {
        if (numberOfHounds == null || numberOfHounds < 1) {
            throw new NumberOfHoundsMissingException("Invalid number of hounds:" + numberOfHounds);
        }
    }
}
