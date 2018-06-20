package fi.riista.feature.gamediary;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;

import java.util.List;

public class OutOfBoundsSpecimenAmountException extends RuntimeException {
    private final int minimumAmount;
    private final int maximumAmount;
    private final int givenAmount;

    public static void assertHarvestSpecimenAmountWithinBounds(final int totalAmount) {
        if (totalAmount < Harvest.MIN_AMOUNT || totalAmount > Harvest.MAX_AMOUNT) {
            throw new OutOfBoundsSpecimenAmountException(Harvest.MIN_AMOUNT, Harvest.MAX_AMOUNT, totalAmount,
                    String.format(
                            "Total amount of harvest specimens must be between %d and %d",
                            Harvest.MIN_AMOUNT, Harvest.MAX_AMOUNT));
        }
    }

    public static void assertObservationSpecimenAmountWithinBounds(final int totalAmount) {
        if (totalAmount < Observation.MIN_AMOUNT || totalAmount > Observation.MAX_AMOUNT) {
            throw new OutOfBoundsSpecimenAmountException(Observation.MIN_AMOUNT, Observation.MAX_AMOUNT, totalAmount,
                    String.format(
                            "Total amount of observation specimens must be between %d and %d",
                            Observation.MIN_AMOUNT, Observation.MAX_AMOUNT));
        }
    }

    public static void assertDtoCount(final List<?> dtos, final int totalAmount) {
        if (dtos.size() > totalAmount) {
            throw new OutOfBoundsSpecimenAmountException(1, dtos.size(), totalAmount,
                    "Total specimen amount must not be less than number of items in specimen collection");
        }
    }

    private OutOfBoundsSpecimenAmountException(final int minimumAmount,
                                               final int maximumAmount,
                                               final int givenAmount,
                                               final String message) {
        super(message);

        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.givenAmount = givenAmount;
    }

    public int getMinimumAmount() {
        return minimumAmount;
    }

    public int getMaximumAmount() {
        return maximumAmount;
    }

    public int getGivenAmount() {
        return givenAmount;
    }
}
