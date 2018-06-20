package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.harvestpermit.HarvestPermit;

import javax.annotation.Nonnull;

public class HarvestReportNotSupportedException extends IllegalStateException {
    public HarvestReportNotSupportedException(@Nonnull final HarvestPermit permit) {
        super(String.format("Harvest report is not allowed for permit type:%s permitNumber:%s",
                permit.getPermitTypeCode(), permit.getPermitNumber()));
    }
}
