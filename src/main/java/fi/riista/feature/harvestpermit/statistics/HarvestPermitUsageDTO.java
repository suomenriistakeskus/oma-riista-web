package fi.riista.feature.harvestpermit.statistics;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HarvestPermitUsageDTO extends HarvestPermitSpeciesAmountDTO {
    public static List<HarvestPermitUsageDTO> createUsage(final Collection<HarvestPermitSpeciesAmount> speciesAmounts,
                                                          final Collection<Harvest> acceptedHarvestForEndOfHuntingReport) {
        final Map<Integer, Integer> harvestTotalAmountBySpeciesCode = acceptedHarvestForEndOfHuntingReport.stream()
                .collect(Collectors.groupingBy(h -> h.getSpecies().getOfficialCode(),
                        Collectors.summingInt(Harvest::getAmount)));

        // Species amount list MUST NOT contain duplicates for same species
        return speciesAmounts.stream().map(spa -> {
            final int speciesOfficialCode = spa.getGameSpecies().getOfficialCode();
            int harvestAmount = harvestTotalAmountBySpeciesCode.getOrDefault(speciesOfficialCode, 0);
            return new HarvestPermitUsageDTO(spa.getGameSpecies(), spa, harvestAmount);
        }).collect(Collectors.toList());
    }

    private float reportedAmount;
    private float remainingAmount;

    public HarvestPermitUsageDTO(final GameSpecies species,
                                 final HarvestPermitSpeciesAmount speciesAmount,
                                 final float reportedAmount) {
        super(speciesAmount, species);

        this.reportedAmount = reportedAmount;
        this.remainingAmount = speciesAmount.getSpecimenAmount() - reportedAmount;
    }

    public float getReportedAmount() {
        return reportedAmount;
    }

    public float getRemainingAmount() {
        return remainingAmount;
    }
}
