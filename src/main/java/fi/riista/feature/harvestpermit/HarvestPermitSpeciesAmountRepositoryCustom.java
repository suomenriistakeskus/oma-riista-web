package fi.riista.feature.harvestpermit;

import java.util.Map;

public interface HarvestPermitSpeciesAmountRepositoryCustom {

    Map<Integer, HarvestPermitSpeciesAmount> findSpeciesCodeToSpeciesAmountByHarvestPermit(final HarvestPermit permit);

}
