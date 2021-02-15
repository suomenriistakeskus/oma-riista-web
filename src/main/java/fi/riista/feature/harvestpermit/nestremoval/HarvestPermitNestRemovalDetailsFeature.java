package fi.riista.feature.harvestpermit.nestremoval;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class HarvestPermitNestRemovalDetailsFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitNestRemovalUsageRepository harvestPermitNestRemovalUsageRepository;

    @Resource
    private HarvestPermitNestLocationRepository harvestPermitNestLocationRepository;

    @Resource
    private HarvestPermitNestRemovalDetailsService harvestPermitNestRemovalDetailsService;

    @Transactional(readOnly = true)
    public List<HarvestPermitNestRemovalUsageDTO> getPermitUsage(final long harvestPermitId) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.READ);

        return harvestPermitNestRemovalDetailsService.getPermitUsage(harvestPermit);
    }

    @Transactional
    public void savePermitUsage(final long harvestPermitId,
                                final List<HarvestPermitNestRemovalUsageDTO> dtos) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(harvestPermitId, EntityPermission.UPDATE);
        final HarvestReportState state = harvestPermit.getHarvestReportState();
        Preconditions.checkState(state == null, "Usage cannot be edited at this state: " + state);

        final Map<Integer, HarvestPermitSpeciesAmount> speciesCodeToAmount =
                harvestPermitSpeciesAmountRepository.findSpeciesCodeToSpeciesAmountByHarvestPermit(harvestPermit);
        dtos.forEach(dto -> {
            final Integer speciesCode = dto.getSpeciesCode();
            final HarvestPermitSpeciesAmount speciesAmount = speciesCodeToAmount.get(speciesCode);
            checkArgument(speciesAmount != null, "Invalid species for permit");
            final int permitNestAmount =
                    Optional.ofNullable(speciesAmount.getNestAmount()).orElse(0);
            final int usedNestAmount = Optional.ofNullable(dto.getUsedNestAmount()).orElse(0);
            checkArgument(usedNestAmount <= permitNestAmount, "Used nest amount over permitted amount");
            final int permitEggAmount =
                    Optional.ofNullable(speciesAmount.getEggAmount()).orElse(0);
            final int usedEggAmount = Optional.ofNullable(dto.getUsedEggAmount()).orElse(0);
            checkArgument(usedEggAmount <= permitEggAmount, "Used egg amount over permitted amount");
            final int permitConstructionAmount =
                    Optional.ofNullable(speciesAmount.getConstructionAmount()).orElse(0);
            final int usedConstructionAmount = Optional.ofNullable(dto.getUsedConstructionAmount()).orElse(0);
            checkArgument(usedConstructionAmount <= permitConstructionAmount, "Used construction amount over permitted amount");
        });

        final Collection<HarvestPermitSpeciesAmount> speciesAmounts = speciesCodeToAmount.values();
        final List<HarvestPermitNestRemovalUsage> usages = Optional.ofNullable(speciesAmounts)
                .map(harvestPermitNestRemovalUsageRepository::findByHarvestPermitSpeciesAmountIn)
                .orElseGet(Collections::emptyList);
        if (!usages.isEmpty()) {
            harvestPermitNestLocationRepository.deleteByHarvestPermitNestRemovalUsageIn(usages);
            harvestPermitNestLocationRepository.flush();
            harvestPermitNestRemovalUsageRepository.deleteAll(usages);
            harvestPermitNestRemovalUsageRepository.flush();
        }

        dtos.forEach(dto -> {
            final Integer speciesCode = dto.getSpeciesCode();
            final HarvestPermitNestRemovalUsage usage =
                    new HarvestPermitNestRemovalUsage(speciesCodeToAmount.get(speciesCode), dto.getUsedNestAmount(), dto.getUsedEggAmount(), dto.getUsedConstructionAmount());
            harvestPermitNestRemovalUsageRepository.save(usage);
            final List<HarvestPermitNestLocation> harvestPermitNestLocations = dto.getNestLocations().stream()
                    .map(nestLocation -> new HarvestPermitNestLocation(usage, nestLocation.getGeoLocation(), nestLocation.getNestLocationType()))
                    .collect(Collectors.toList());
            harvestPermitNestLocationRepository.saveAll(harvestPermitNestLocations);
        });
    }
}