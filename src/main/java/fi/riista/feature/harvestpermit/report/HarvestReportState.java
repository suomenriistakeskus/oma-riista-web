package fi.riista.feature.harvestpermit.report;

import fi.riista.util.LocalisedEnum;

public enum HarvestReportState implements LocalisedEnum {
    // Lähetetty Riistakeskukselle
    SENT_FOR_APPROVAL,

    // Riistakeskus on hyväksynyt
    APPROVED,

    // Riistakeskus on hylännyt
    REJECTED;

    public boolean stateChangeRequiresReasonFromModerator() {
        return this != APPROVED;
    }

    public boolean requiresPropertyIdentifier() {
        return this == HarvestReportState.APPROVED;
    }
}
