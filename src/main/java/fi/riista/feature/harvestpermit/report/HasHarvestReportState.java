package fi.riista.feature.harvestpermit.report;

public interface HasHarvestReportState {
    HarvestReportState getHarvestReportState();

    default boolean isHarvestReportDone() {
        return getHarvestReportState() != null;
    }

    default boolean isHarvestReportSentForApproval() {
        return getHarvestReportState() == HarvestReportState.SENT_FOR_APPROVAL;
    }

    default boolean isHarvestReportApproved() {
        return getHarvestReportState() == HarvestReportState.APPROVED;
    }

    default boolean isHarvestReportRejected() {
        return getHarvestReportState() == HarvestReportState.REJECTED;
    }
}
