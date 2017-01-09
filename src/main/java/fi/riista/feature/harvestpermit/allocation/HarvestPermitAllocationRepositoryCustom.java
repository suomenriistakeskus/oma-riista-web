package fi.riista.feature.harvestpermit.allocation;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.permit.allocation.HuntingClubPermitAllocationDTO;

import java.util.List;

public interface HarvestPermitAllocationRepositoryCustom {
    List<HuntingClubPermitAllocationDTO> getAllocationsIncludeMissingPartnerDTO(HarvestPermit permit, GameSpecies species);
}
