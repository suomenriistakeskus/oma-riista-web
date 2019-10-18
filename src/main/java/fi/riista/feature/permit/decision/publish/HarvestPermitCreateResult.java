package fi.riista.feature.permit.decision.publish;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;

import java.util.Collections;
import java.util.List;

class HarvestPermitCreateResult {
    private final HarvestPermit permit;
    private final List<HarvestPermitSpeciesAmount> speciesAmounts;

    public HarvestPermitCreateResult() {
        this.permit = null;
        this.speciesAmounts = Collections.emptyList();
    }

    public HarvestPermitCreateResult(final HarvestPermit permit,
                                     final List<HarvestPermitSpeciesAmount> speciesAmounts) {
        this.permit = permit;
        this.speciesAmounts = ImmutableList.copyOf(speciesAmounts);
    }

    public List<HarvestPermit> getPermits() {
        return permit == null ? Collections.emptyList() : Collections.singletonList(permit);
    }

    public List<HarvestPermitSpeciesAmount> getSpeciesAmounts() {
        return speciesAmounts;
    }
}
