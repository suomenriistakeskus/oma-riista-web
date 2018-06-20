package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.gamediary.harvest.Harvest;

import javax.annotation.Nonnull;

public class HarvestReportExistsException extends IllegalStateException {
    public HarvestReportExistsException(@Nonnull final Harvest harvest) {
        super(String.format("Harvest id:%d is locked by harvest report state:%s",
                harvest.getId(), harvest.getHarvestReportState()));
    }
}
