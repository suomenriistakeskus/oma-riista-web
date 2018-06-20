package fi.riista.feature.huntingclub.group;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import fi.riista.util.jpa.JpaSubQuery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupPermitFeature {
    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Transactional(readOnly = true)
    public HarvestPermitSpeciesAmountDTO getGroupPermitSpeciesAmount(final long huntingClubGroupId) {
        final HuntingClubGroup group = requireEntityService
                .requireHuntingGroup(huntingClubGroupId, EntityPermission.READ);

        return harvestPermitSpeciesAmountRepository.findByHuntingClubGroupPermit(group)
                .map(HarvestPermitSpeciesAmountDTO::create).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<HuntingClubGroupDTO.PermitDTO> listAvailablePermits(final long clubId,
                                                                    final int gameSpeciesCode,
                                                                    final int huntingYear) {
        final HuntingClub huntingClub = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);

        final List<HarvestPermit> permits = harvestPermitRepository
                .findAll(spec(huntingClub, huntingYear, gameSpeciesCode))
                .stream()
                .filter(p -> !harvestPermitLockedByDateService.isPermitLockedByDateForHuntingYear(p, huntingYear))
                .collect(Collectors.toList());

        return HuntingClubGroupDTO.PermitDTO.create(permits);
    }

    private static Specifications<HarvestPermit> spec(final HuntingClub club, final int huntingYear, int speciesCode) {
        return Specifications.where(clubIsPartnerPredicate(club))
                .and(HarvestPermitSpecs.validWithinHuntingYear(huntingYear))
                .and(HarvestPermitSpecs.IS_MOOSELIKE_PERMIT)
                .and(HarvestPermitSpecs.withSpeciesCode(speciesCode));
    }

    private static Specification<HarvestPermit> clubIsPartnerPredicate(final HuntingClub club) {
        return JpaSubQuery.of(HarvestPermit_.permitPartners).exists((root, cb) -> cb.equal(root, club));
    }
}
