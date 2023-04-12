package fi.riista.feature.harvestpermit;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Service
public class AnnualRenewalPeriodUpdateFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public void updatePeriods(final @Nonnull AnnualRenewalPeriodUpdateDTO dto) {
        requireNonNull(dto);

        final HarvestPermit permit =
                requireEntityService.requireHarvestPermit(dto.getPermitId(), EntityPermission.UPDATE);

        checkArgument(PermitTypeCode.isAnnualUnprotectedBird(permit.getPermitTypeCode()));

        final Map<Integer, HarvestPermitSpeciesAmount> speciesAmountsBySpeciesId =
                F.index(permit.getSpeciesAmounts(), p -> p.getGameSpecies().getOfficialCode());

        checkArgument(dto.getPeriods().size() == speciesAmountsBySpeciesId.size());

        dto.getPeriods()
                .forEach(s -> {
                    final HarvestPermitSpeciesAmount amount = speciesAmountsBySpeciesId.get(s.getSpeciesCode());
                    amount.setBeginDate(s.getBeginDate());
                    amount.setEndDate(s.getEndDate());
                    amount.setBeginDate2(s.getBeginDate2());
                    amount.setEndDate2(s.getEndDate2());
                });
    }
}
