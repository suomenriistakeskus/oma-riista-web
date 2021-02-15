package fi.riista.feature.harvestpermit.usage;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
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

import static fi.riista.util.F.mapNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.postgresql.shaded.com.ongres.scram.common.util.Preconditions.checkArgument;

@Component
public class PermitUsageFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private PermitUsageRepository permitUsageRepository;

    @Resource
    private PermitUsageLocationRepository permitUsageLocationRepository;

    @Resource
    private LastModifierService lastModifierService;

    @Transactional(readOnly = true)
    public List<PermitUsageDTO> getPermitUsage(final long harvestPermitId) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);
        final Map<Integer, HarvestPermitSpeciesAmount> speciesCodeToAmount =
                harvestPermitSpeciesAmountRepository.findSpeciesCodeToSpeciesAmountByHarvestPermit(permit);
        final Map<Integer, PermitUsage> speciesCodeToUsages = !speciesCodeToAmount.isEmpty() ?
                permitUsageRepository.findByHarvestPermitSpeciesAmountIn(speciesCodeToAmount.values()).stream()
                        .collect(toMap(
                                u -> u.getHarvestPermitSpeciesAmount().getGameSpecies().getOfficialCode(),
                                u -> u)) :
                Collections.emptyMap();
        final Map<PermitUsage, List<PermitUsageLocation>> usageToLocations =
                permitUsageLocationRepository.findByPermitUsageIn(speciesCodeToUsages.values()).stream()
                .collect(groupingBy(location -> location.getPermitUsage(), toList()));

        final Map<PermitUsage, LastModifierDTO> usageToLastModifier =
                lastModifierService.getLastModifiers(speciesCodeToUsages.values());

        return speciesCodeToAmount.keySet().stream()
                .map(speciesCode -> {
                    final HarvestPermitSpeciesAmount speciesAmount = speciesCodeToAmount.get(speciesCode);
                    final PermitUsage usage = speciesCodeToUsages.get(speciesCode);
                    final List<PermitUsageLocation> locationList =
                            Optional.ofNullable(usageToLocations.get(usage)).orElse(Collections.emptyList());
                    final List<PermitUsageLocationDTO> locations = locationList.stream()
                            .map(location -> PermitUsageLocationDTO.create(location))
                            .collect(toList());
                    final LastModifierDTO lastModifier = usageToLastModifier.get(usage);
                    return PermitUsageDTO.create(speciesAmount, usage, locations, lastModifier);
                }).collect(toList());
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

            final Integer permittedSpecimenAmount = F.coalesceAsInt(speciesAmount.getSpecimenAmount(), 0);
            final Integer permittedEggAmount = F.coalesceAsInt(speciesAmount.getEggAmount(), 0);
            final int usedSpecimenAmount = usageDTO.getUsedSpecimenAmount();
            final int usedEggAmount = usageDTO.getUsedEggAmount();
            checkArgument(usedSpecimenAmount <= permittedSpecimenAmount, "Invalid amount for specimen usage");
            checkArgument(usedEggAmount <= permittedEggAmount, "Invalid amount for egg usage");

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
}
