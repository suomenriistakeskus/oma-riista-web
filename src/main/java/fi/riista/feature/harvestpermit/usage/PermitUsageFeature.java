package fi.riista.feature.harvestpermit.usage;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.postgresql.shaded.com.ongres.scram.common.util.Preconditions.checkArgument;

@Component
public class PermitUsageFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitUsageService permitUsageService;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private PermitUsageRepository permitUsageRepository;

    @Resource
    private PermitUsageLocationRepository permitUsageLocationRepository;

    @Transactional(readOnly = true)
    public List<PermitUsageDTO> getPermitUsage(final long harvestPermitId) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        return permitUsageService.getPermitUsage(permit);
    }

    @Transactional
    public void savePermitUsage(final long harvestPermitId, final List<PermitUsageDTO> permitUsageDTOS) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.UPDATE);
        final Map<Integer, HarvestPermitSpeciesAmount> speciesCodeToSpeciesAmount =
                harvestPermitSpeciesAmountRepository.findSpeciesCodeToSpeciesAmountByHarvestPermit(permit);

        final Collection<HarvestPermitSpeciesAmount> speciesAmounts = speciesCodeToSpeciesAmount.values();
        final List<PermitUsage> usages = Optional.ofNullable(speciesAmounts)
                .map(permitUsageRepository::findByHarvestPermitSpeciesAmountIn)
                .orElseGet(Collections::emptyList);
        final Map<Long, PermitUsage> idToUsage = F.indexById(usages);

        final Set<Long> dtoIds = permitUsageDTOS.stream().map(PermitUsageDTO::getId).collect(Collectors.toSet());
        final List<PermitUsage> removedUsages =
                usages.stream().filter(usage -> !dtoIds.contains(usage.getId())).collect(toList());
        if (!removedUsages.isEmpty()) {
            permitUsageLocationRepository.deleteByPermitUsageIn(removedUsages);
            permitUsageRepository.deleteAll(removedUsages);
        }

        permitUsageDTOS.forEach(usageDTO -> {
            final HarvestPermitSpeciesAmount speciesAmount = speciesCodeToSpeciesAmount.get(usageDTO.getSpeciesCode());
            checkArgument(speciesAmount != null, "Invalid species for usage");

            final Integer permittedSpecimenAmount = F.mapNullable(speciesAmount.getSpecimenAmount(), Float::intValue);
            final Integer usedSpecimenAmount = usageDTO.getUsedSpecimenAmount();
            assertAmount(permittedSpecimenAmount, usedSpecimenAmount);

            final Integer permittedEggAmount = speciesAmount.getEggAmount();
            final Integer usedEggAmount = usageDTO.getUsedEggAmount();
            assertAmount(permittedEggAmount, usedEggAmount);

            final PermitUsage usage = idToUsage.get(usageDTO.getId());
            if (usage == null) {
                final PermitUsage newUsage = new PermitUsage(usedSpecimenAmount, usedEggAmount, speciesAmount);
                permitUsageRepository.save(newUsage);
                final List<PermitUsageLocation> locations = usageDTO.getPermitUsageLocations().stream()
                        .map(locationDTO -> new PermitUsageLocation(newUsage, locationDTO.getGeoLocation()))
                        .collect(toList());
                permitUsageLocationRepository.saveAll(locations);
            } else {
                usage.setSpecimenAmount(usedSpecimenAmount);
                usage.setEggAmount(usedEggAmount);
                permitUsageLocationRepository.deleteByPermitUsage(usage);
                final List<PermitUsageLocation> locations = usageDTO.getPermitUsageLocations().stream()
                        .map(locationDTO -> new PermitUsageLocation(usage, locationDTO.getGeoLocation()))
                        .collect(toList());
                permitUsageLocationRepository.saveAll(locations);
            }
        });
    }

    private void assertAmount(final Integer permitAmount, final Integer usedAmount) {
        if (permitAmount == null && usedAmount != null) {
            throw new IllegalArgumentException("Used amount not null when no permitted amount");
        }

        if (F.allNotNull(permitAmount, usedAmount) && permitAmount < usedAmount) {
            throw new IllegalArgumentException("Used amount more than permitted amount");
        }
    }
}
