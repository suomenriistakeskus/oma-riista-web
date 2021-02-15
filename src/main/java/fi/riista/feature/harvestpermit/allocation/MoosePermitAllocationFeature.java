package fi.riista.feature.harvestpermit.allocation;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class MoosePermitAllocationFeature {

    @Resource
    private HarvestPermitAllocationRepository allocationRepository;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private RequireEntityService entityService;

    @Transactional
    public void updateAllocation(final long harvestPermitId,
                                 final int speciesCode,
                                 final List<MoosePermitAllocationDTO> allocations) {
        final GameSpecies species = gameSpeciesService.requireByOfficialCode(speciesCode);
        final HarvestPermit permit = entityService.requireHarvestPermit(harvestPermitId,
                HarvestPermitAuthorization.Permission.UPDATE_ALLOCATIONS);
        assertValidGameSpeciesForPermit(speciesCode, permit);

        // Remove existing allocation for same gameSpecies
        allocationRepository.deleteByHarvestPermitAndGameSpecies(permit, species);
        allocationRepository.flush();

        final Map<Long, HuntingClub> clubIndex = F.indexById(permit.getPermitPartners());

        allocationRepository.saveAll(allocations.stream().map(dto -> {
            Preconditions.checkArgument(clubIndex.containsKey(dto.getHuntingClubId()),
                    "Club " + dto.getHuntingClubId() + " is not associated with permitId " + permit.getId());

            final HarvestPermitAllocation a = new HarvestPermitAllocation();

            a.setHarvestPermit(permit);
            a.setGameSpecies(species);
            a.setHuntingClub(clubIndex.get(dto.getHuntingClubId()));
            a.setTotal(dto.getTotal());
            a.setAdultMales(dto.getAdultMales());
            a.setAdultFemales(dto.getAdultFemales());
            a.setYoung(dto.getYoung());

            return a;
        }).collect(toList()));
    }

    private static void assertValidGameSpeciesForPermit(final int speciesCode, final HarvestPermit permit) {
        final Set<Integer> speciesCodes = permit.getSpeciesAmounts().stream()
                .map(spa -> spa.getGameSpecies().getOfficialCode())
                .collect(toSet());

        Preconditions.checkArgument(speciesCodes.contains(speciesCode),
                "Allocation for species code:"
                        + speciesCode
                        + " which not in permit's species amounts species "
                        + speciesCodes);
    }
}
