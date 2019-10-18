package fi.riista.feature.harvestpermit.violation;

import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.util.NumberUtils;

import java.util.Collection;
import java.util.Map;

public class AmendmentPermitMatchHarvest {
    public static void assertCountMatches(final String permitNumber,
                                          final Collection<HarvestCountDTO> harvestCounts,
                                          final Map<String, Float> amendmentPermits) {
        final double amendmentAmount = countAmendmentPermits(amendmentPermits);
        final double requiredAmendmentAmount = countRequiredAmendmentPermits(harvestCounts);

        if (!NumberUtils.equal(amendmentAmount, requiredAmendmentAmount)) {
            throw new AmendmentPermitDoesNotMatchHarvestCountException(
                    permitNumber, amendmentAmount, requiredAmendmentAmount);
        }
    }

    public static boolean countMatches(final Collection<HarvestCountDTO> harvestCounts,
                                       final Map<String, Float> amendmentPermits) {
        return NumberUtils.equal(countAmendmentPermits(amendmentPermits), countRequiredAmendmentPermits(harvestCounts));
    }

    private static double countRequiredAmendmentPermits(final Collection<HarvestCountDTO> harvestCounts) {
        return harvestCounts.stream().mapToDouble(HarvestCountDTO::getRequiredAmendmentPermits).sum();
    }

    private static double countAmendmentPermits(final Map<String, Float> amendmentPermits) {
        return amendmentPermits.values().stream().mapToDouble(Float::doubleValue).sum();
    }

    private AmendmentPermitMatchHarvest() {
        throw new AssertionError();
    }
}
