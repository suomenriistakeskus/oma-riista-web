package fi.riista.feature.harvestpermit.violation;

public class AmendmentPermitDoesNotMatchHarvestCountException extends IllegalStateException {

    public AmendmentPermitDoesNotMatchHarvestCountException(final String permitNumber,
                                                            final double amendmentAmount,
                                                            final double requiredAmount) {
        super(String.format("Amendment permit amount %.1f does not match required amount %.1f for permit %s",
                amendmentAmount, requiredAmount, permitNumber));
    }
}
