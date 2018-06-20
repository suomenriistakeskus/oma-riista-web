package fi.riista.feature.harvestpermit.allocation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;

import java.util.List;

public interface HarvestPermitAllocationRepositoryCustom {
    List<MoosePermitAllocationDTO> getAllocationsIncludeMissingPartnerDTO(HarvestPermit permit, GameSpecies species);
}
