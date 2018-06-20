package fi.riista.feature.gamediary.harvest.mutation.exception;

import fi.riista.feature.gamediary.harvest.HarvestReportingType;

public class HarvestReportingTypeChangeForbiddenException extends RuntimeException {
    public HarvestReportingTypeChangeForbiddenException(
            final HarvestReportingType previous,
            final HarvestReportingType next) {
        super(String.format("Cannot change report type from %s to %s", previous, next));
    }
}
