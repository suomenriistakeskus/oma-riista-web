package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;

import java.util.Objects;

public class MultipleSpecimenNotAllowedException extends IllegalArgumentException {
    private final int givenAmount;

    public static void assertHarvestMultipleSpecimenConstraint(final GameSpecies species, final int totalAmount) {
        Objects.requireNonNull(species);

        if (totalAmount > 1 && !species.isMultipleSpecimenAllowedOnHarvest()) {
            throw new MultipleSpecimenNotAllowedException(species, totalAmount);
        }
    }

    private MultipleSpecimenNotAllowedException(final GameSpecies species, final int givenAmount) {
        super(String.format(
                "Multiple harvest specimens not allowed for species: %s (%s)",
                species.getNameFinnish(),
                species.getOfficialCode()));
        this.givenAmount = givenAmount;
    }

    public int getGivenAmount() {
        return givenAmount;
    }
}
