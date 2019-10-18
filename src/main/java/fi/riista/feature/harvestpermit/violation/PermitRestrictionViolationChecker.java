package fi.riista.feature.harvestpermit.violation;

import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class PermitRestrictionViolationChecker {
    private final HarvestPermitSpeciesAmount permitSpeciesAmount;
    private final Collection<HarvestCountDTO> harvestCounts;

    public PermitRestrictionViolationChecker(final HarvestPermitSpeciesAmount permitSpeciesAmount,
                                             final Collection<HarvestCountDTO> harvestCounts) {
        this.permitSpeciesAmount = requireNonNull(permitSpeciesAmount);
        this.harvestCounts = requireNonNull(harvestCounts);
    }

    public boolean isRestrictionViolated() {
        return permitSpeciesAmount.getRestrictionType() != null && getRestrictedHarvestCount() > permitSpeciesAmount.getRestrictionAmount();
    }

    private int getRestrictedHarvestCount() {
        return harvestCounts.stream().mapToInt(h -> {
            switch (permitSpeciesAmount.getRestrictionType()) {
                case AE:
                    return h.getNumberOfAdultMales() + h.getNumberOfAdultFemales() - h.getNumberOfNonEdibleAdults();
                case AU:
                    return h.getNumberOfAdultMales() - h.getNumberOfNonEdibleAdultMales();
                default:
                    throw new IllegalStateException("Unknown restriction type:" + permitSpeciesAmount.getRestrictionType());
            }
        }).sum();
    }
}
