package fi.riista.feature.harvestpermit.search;

import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.jpa.JpaSpecs;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitSearchFeature {

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public List<HarvestPermitTypeDTO> listPermitTypes() {
        // it is ok to anyone call this
        // Do not return permit types 100, 190
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final Map<String, Group> res = jpqlQueryFactory
                .select(PERMIT.permitType, PERMIT.permitTypeCode, SPECIES.officialCode)
                .from(PERMIT)
                .join(PERMIT.speciesAmounts, AMOUNT)
                .join(AMOUNT.gameSpecies, SPECIES)
                .where(PERMIT.isMooselikeOrAmendmentPermit().not())
                .distinct()
                .transform(GroupBy.groupBy(PERMIT.permitTypeCode).as(PERMIT.permitType, GroupBy.set(SPECIES.officialCode)));

        return res.entrySet().stream()
                .map(entry -> {
                    final String permitTypeCode = entry.getKey();
                    final Group group = entry.getValue();
                    final String permitType = group.getOne(PERMIT.permitType);
                    final Set<Integer> speciesCodes = group.getSet(SPECIES.officialCode);
                    return new HarvestPermitTypeDTO(permitTypeCode, permitType, speciesCodes);
                })
                .collect(toList());
    }

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
    public List<HarvestPermitSearchResultDTO> searchForCoordinator(final HarvestPermitSearchDTO dto) {
        dto.clearAndCheckRhyParams();
        userAuthorizationHelper.assertCoordinatorOrModerator(dto.getRhyId());
        final List<HarvestPermit> all = harvestPermitRepository.findAll(createSpec(dto), sort(Sort.Direction.ASC));
        return HarvestPermitSearchResultDTO.create(all);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<HarvestPermitSearchResultDTO> search(final HarvestPermitSearchDTO dto) {
        final List<HarvestPermit> all = harvestPermitRepository.findAll(createSpec(dto), sort(dto.getPermitNumberSort()));
        if (dto.getSortingType() == HarvestPermitSearchDTO.SortType.SPECIAL) {
            final Comparator<HarvestPermit> c1 = createComparator(p -> p.getPermitNumber().substring(0, 4), dto.getYearSort());
            final Comparator<HarvestPermit> c2 = createComparator(p -> p.getPermitNumber().substring(11, 16), dto.getOrdinalSort());
            all.sort(c1.thenComparing(c2));
        }
        return HarvestPermitSearchResultDTO.create(all);
    }

    private static Comparator<HarvestPermit> createComparator(final Function<HarvestPermit, String> valueExtrator, final Sort.Direction direction) {
        final Comparator<HarvestPermit> c = Comparator.comparing(valueExtrator);
        return direction == Sort.Direction.DESC ? c.reversed() : c;
    }

    private static Specification<HarvestPermit> createSpec(final HarvestPermitSearchDTO dto) {
        final List<Specification<HarvestPermit>> specs = new ArrayList<>();

        specs.add(HarvestPermitSpecs.IS_NOT_ANY_MOOSELIKE_PERMIT);

        if (dto.getRhyId() != null) {
            specs.add(HarvestPermitSpecs.withRhyId(dto.getRhyId()));
        }
        if (StringUtils.isNotBlank(dto.getPermitNumber())) {
            specs.add(HarvestPermitSpecs.withPermitNumber(dto.getPermitNumber()));
            return JpaSpecs.and(specs);
        }
        if (dto.getAreaId() != null) {
            specs.add(HarvestPermitSpecs.withAreaId(dto.getAreaId()));
        }
        if (dto.getPermitType() != null) {
            specs.add(HarvestPermitSpecs.withPermitTypeCode(dto.getPermitType().getPermitTypeCode()));
        }
        if (dto.getSpeciesCode() != null) {
            specs.add(HarvestPermitSpecs.withSpeciesCode(dto.getSpeciesCode()));
        }
        if (StringUtils.isNotBlank(dto.getYear())) {
            specs.add(HarvestPermitSpecs.withYear(dto.getYear()));
        }
        if (dto.getState() != null) {
            specs.add(JpaSpecs.equal(HarvestPermit_.harvestReportState, dto.getState()));
        }
        if (dto.isReportNotDone()) {
            specs.add(JpaSpecs.isNull(HarvestPermit_.harvestReportState));
        }
        if (dto.getValidity() != null) {
            switch (dto.getValidity()) {
                case ACTIVE:
                    specs.add(HarvestPermitSpecs.active(DateUtil.today()));
                    break;
                case PASSED:
                    specs.add(HarvestPermitSpecs.passed(DateUtil.today()));
                    break;
                case FUTURE:
                    specs.add(HarvestPermitSpecs.future(DateUtil.today()));
                    break;
            }
        }
        return JpaSpecs.and(specs);
    }

    private static JpaSort sort(final Sort.Direction asc) {
        return new JpaSort(asc, HarvestPermit_.permitNumber);
    }
}
