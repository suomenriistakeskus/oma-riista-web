package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermit;

import java.util.List;

public interface PermitDecisionRepositoryCustom {
    List<String> findCancelledAndIgnoredPermitNumbersByOriginalPermit(HarvestPermit originalPermit);
}
