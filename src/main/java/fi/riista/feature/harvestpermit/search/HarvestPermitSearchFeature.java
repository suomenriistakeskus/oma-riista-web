package fi.riista.feature.harvestpermit.search;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitDTO;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.util.jpa.JpaSpecs;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitSearchFeature {

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public HarvestPermitExistsDTO findPermitNumber(final String permitNumber) {
        // it is ok to anyone call this
        // Do not return permit types 100, 190
        return Optional.ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .filter(permit -> !permit.isMooselikePermitType())
                .filter(permit -> !permit.isAmendmentPermit())
                .map(HarvestPermitExistsDTO::create)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitDTO> searchForCoordinator(HarvestPermitRhySearchDTO dto) {
        userAuthorizationHelper.assertCoordinatorOrModerator(dto.getRhyId());

        final SystemUser currentUser = activeUserService.getActiveUser();

        return harvestPermitRepository.findAll(notMoose(createSpec(dto)), sort()).stream()
                .map(permit -> HarvestPermitDTO.create(permit, currentUser,
                        EnumSet.of(HarvestPermitDTO.Inclusion.REPORT_LIST)))
                .collect(toList());
    }

    private static Specifications<HarvestPermit> createSpec(final HarvestPermitRhySearchDTO dto) {
        if (StringUtils.isNotBlank(dto.getPermitNumber())) {
            return Specifications.where(HarvestPermitSpecs.withPermitNumber(dto.getPermitNumber()))
                    .and(HarvestPermitSpecs.withRhyId(dto.getRhyId()));
        }
        Specifications<HarvestPermit> spec = Specifications.where(HarvestPermitSpecs.withRhyId(dto.getRhyId()));
        if (dto.getSpeciesCode() != null) {
            spec = spec.and(HarvestPermitSpecs.withSpeciesCode(dto.getSpeciesCode()));
        }
        if (StringUtils.isNotBlank(dto.getYear())) {
            spec = spec.and(HarvestPermitSpecs.withYear(dto.getYear()));
        }
        return spec;
    }

    private static Specification<HarvestPermit> notMoose(Specification<HarvestPermit> spec) {
        return JpaSpecs.and(spec, HarvestPermitSpecs.IS_NOT_ANY_MOOSELIKE_PERMIT);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<HarvestPermitDTO> search(final HarvestPermitSearchDTO dto) {
        List<HarvestPermit> res = harvestPermitRepository.findAll(notMoose(createSpec(dto)), sort());

        return HarvestPermitDTO.create(res, activeUserService.getActiveUser(),
                EnumSet.of(HarvestPermitDTO.Inclusion.REPORT_LIST));
    }

    private static Specification<HarvestPermit> createSpec(final HarvestPermitSearchDTO dto) {
        if (StringUtils.isNotBlank(dto.getPermitNumber())) {
            return HarvestPermitSpecs.withPermitNumber(dto.getPermitNumber());
        }
        List<Specification<HarvestPermit>> specs = new ArrayList<>();
        if (dto.getAreaId() != null) {
            specs.add(HarvestPermitSpecs.withAreaId(dto.getAreaId()));
        }
        if (dto.getSpeciesCode() != null) {
            specs.add(HarvestPermitSpecs.withSpeciesCode(dto.getSpeciesCode()));
        }
        if (StringUtils.isNotBlank(dto.getYear())) {
            specs.add(HarvestPermitSpecs.withYear(dto.getYear()));
        }
        return JpaSpecs.and(specs);
    }

    private static JpaSort sort() {
        return new JpaSort(Sort.Direction.ASC, HarvestPermit_.permitNumber);
    }
}
