package fi.riista.feature.harvestpermit.usage;

import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class PermitUsageService {

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private PermitUsageRepository permitUsageRepository;

    @Resource
    private PermitUsageLocationRepository permitUsageLocationRepository;

    @Resource
    private LastModifierService lastModifierService;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<PermitUsageDTO> getPermitUsage(final HarvestPermit permit) {
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
}
