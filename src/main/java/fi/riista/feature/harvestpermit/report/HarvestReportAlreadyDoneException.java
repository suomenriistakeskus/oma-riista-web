package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.gamediary.harvest.Harvest;

import java.util.Objects;

public class HarvestReportAlreadyDoneException extends RuntimeException {

    public HarvestReportAlreadyDoneException(String message) {
        super(message);
    }

    public static void assertHarvestReportNotDone(Harvest harvest, HarvestReport report) {
        if (harvest.isHarvestReportDone() && !Objects.equals(harvest.getHarvestReport().getId(), report.getId())) {
            throw new HarvestReportAlreadyDoneException("Harvest already attached to report id:" +
                    harvest.getHarvestReport().getId());
        }
    }
}
