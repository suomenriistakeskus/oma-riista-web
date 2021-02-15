package fi.riista.feature.harvestpermit.nestremoval;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class HarvestPermitNestRemovalDetailsService {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitNestRemovalUsageRepository harvestPermitNestRemovalUsageRepository;

    @Resource
    private LastModifierService lastModifierService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<HarvestPermitNestRemovalUsageDTO> getPermitUsage(final HarvestPermit permit) {
        final Map<Integer, HarvestPermitSpeciesAmount> speciesCodeToAmount =
                harvestPermitSpeciesAmountRepository.findSpeciesCodeToSpeciesAmountByHarvestPermit(permit);
        final List<HarvestPermitNestRemovalUsage> usages = !speciesCodeToAmount.isEmpty() ?
                harvestPermitNestRemovalUsageRepository.findByHarvestPermitSpeciesAmountIn(speciesCodeToAmount.values()) :
                Collections.emptyList();
        final Map<HarvestPermitNestRemovalUsage, LastModifierDTO> lastModifierMapping =
                lastModifierService.getLastModifiers(usages);

        final boolean canEdit = permit.getHarvestReportState() == null;

        return HarvestPermitNestRemovalUsageDTO.createUsage(speciesCodeToAmount, usages, lastModifierMapping, canEdit);
    }
}
