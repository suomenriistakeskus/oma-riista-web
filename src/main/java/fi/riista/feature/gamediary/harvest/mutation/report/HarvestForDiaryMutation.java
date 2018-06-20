package fi.riista.feature.gamediary.harvest.mutation.report;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;

public class HarvestForDiaryMutation implements HarvestMutationForReportType {
    private final boolean harvestReportRequired;

    public HarvestForDiaryMutation(final boolean harvestReportRequired) {
        this.harvestReportRequired = harvestReportRequired;
    }

    @Override
    public void accept(final Harvest harvest) {
        harvest.setHarvestReportRequired(harvestReportRequired);

        clearSeasonFields(harvest);
        clearPermitFields(harvest);
    }

    @Override
    public HarvestReportingType getReportingType() {
        return HarvestReportingType.BASIC;
    }
}
