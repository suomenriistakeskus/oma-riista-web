package fi.riista.feature.harvestpermit.search;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson;
import fi.riista.feature.harvestpermit.HarvestPermitContactPersonRepository;
import fi.riista.feature.harvestpermit.HarvestPermitContactPerson_;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount_;
import fi.riista.feature.harvestpermit.HarvestPermitSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.Person_;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys_;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import fi.riista.util.jpa.JpaSpecs;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.common.decision.GrantStatus.RESTRICTED;
import static fi.riista.feature.common.decision.GrantStatus.UNCHANGED;
import static fi.riista.feature.harvestpermit.search.HarvestPermitDecisionOrigin.LUPAHALLINTA;
import static fi.riista.feature.harvestpermit.search.HarvestPermitDecisionOrigin.OMA_RIISTA;
import static fi.riista.util.jpa.JpaSpecs.and;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static fi.riista.util.jpa.JpaSpecs.or;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class HarvestPermitSearchFeature {

    private static final int BATCH_SIZE = 16_000;

    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QHarvestPermitSpeciesAmount AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestPermitContactPersonRepository harvestPermitContactPersonRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public List<HarvestPermitTypeDTO> listPermitTypes() {
        // it is ok to anyone call this

        // Fetch permits separately from LH and OR in order to
        // have items for both origins with same permit type code
        return Stream.concat(
                fetchTypes(LUPAHALLINTA),
                fetchTypes(OMA_RIISTA))
                .collect(toList());
    }

    private Stream<HarvestPermitTypeDTO> fetchTypes(final HarvestPermitDecisionOrigin origin) {
        final BooleanExpression permitOriginExpression =
                origin == LUPAHALLINTA
                        ? PERMIT.permitDecision.id.isNull()
                        : PERMIT.permitDecision.id.isNotNull();

        // Do not return permit types 100, 190
        final Map<String, Group> res = jpqlQueryFactory
                .select(PERMIT.permitType, PERMIT.permitTypeCode, SPECIES.officialCode)
                .from(PERMIT)
                .leftJoin(PERMIT.speciesAmounts, AMOUNT)
                .leftJoin(AMOUNT.gameSpecies, SPECIES)
                .where(PERMIT.isMooselikeOrAmendmentPermit().not(), permitOriginExpression)
                .distinct()
                // Group by concatenating code + origin to get types from LH and OR
                .transform(GroupBy.groupBy(PERMIT.permitTypeCode)
                        .as(PERMIT.permitTypeCode,
                                PERMIT.permitType,
                                GroupBy.set(SPECIES.officialCode)));

        return res.entrySet().stream()
                .map(entry -> {
                    final Group group = entry.getValue();
                    final String permitTypeCode = group.getOne(PERMIT.permitTypeCode);
                    final String permitType = group.getOne(PERMIT.permitType);
                    final Set<Integer> speciesCodes = group.getSet(SPECIES.officialCode);
                    return new HarvestPermitTypeDTO(permitTypeCode, permitType, origin, speciesCodes);
                });
    }

    @Transactional(readOnly = true)
    public HarvestPermitExistsDTO checkHarvestPermitExists(final String permitNumber) {
        // it is ok to anyone call this
        // Do not return permit types for mooselike and nest removal permits as harvests are not linked to permit
        return Optional.ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .filter(permit -> PermitTypeCode.canLinkHarvests(permit.getPermitTypeCode()))
                .map(HarvestPermitExistsDTO::create)
                .orElseThrow(() -> new HarvestPermitNotFoundException(permitNumber));
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
        final List<HarvestPermit> all = harvestPermitRepository.findAll(createSpec(dto),
                sort(dto.getPermitNumberSort()));
        if (dto.getSortingType() == HarvestPermitSearchDTO.SortType.SPECIAL) {
            all.sort(comparePermitYear(dto).thenComparing(comparePermitOrderNumber(dto)));
        }
        return HarvestPermitSearchResultDTO.create(all);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public HarvestPermitSearchExcelView export(final HarvestPermitSearchDTO dto, final Locale locale) {

        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final List<HarvestPermit> permits = harvestPermitRepository.findAll(createSpec(dto), sort(dto.getPermitNumberSort()));

        return new HarvestPermitSearchExcelView(localiser, doWithSingleQueries(permits));
    }

    /* package */ List<HarvestPermitSearchExportDTO> doWithSingleQueries(final List<HarvestPermit> permits) {

        // rhy / rka queries can be done without partitioning due their number is so small
        final Map<Long, Riistanhoitoyhdistys> rhysById = F.indexById(
                rhyRepository.findAll(inCollection(Riistanhoitoyhdistys_.id,
                                                   permits.stream()
                                                           .map(HarvestPermit::getRhy)
                                                           .map(Riistanhoitoyhdistys::getId)
                                                           .collect(toSet()))));

        // getRiistakeskuksenAlue() causes an implicit join to database, so, it can be used directly
        final Map<Long, LocalisedString> rkaNameByRhyId = rhysById.values().stream()
                .collect(toMap(Riistanhoitoyhdistys::getId,
                               rhy -> rhy.getRiistakeskuksenAlue().getNameLocalisation()));

        // Handle permits in smaller patches, so, max number of query parameter will not be exceeded
        final List<HarvestPermitSearchExportDTO> resultList = Lists.newArrayListWithExpectedSize(Math.min(permits.size(), BATCH_SIZE));
        Lists.partition(permits, BATCH_SIZE).forEach(permitBatch -> {

            final Map<Long, List<HarvestPermitContactPerson>> contactPersonsByPermitId = harvestPermitContactPersonRepository
                    .findAll(inCollection(HarvestPermitContactPerson_.harvestPermit, permitBatch))
                    .stream()
                    .collect(groupingBy(c -> c.getHarvestPermit().getId()));
            final Map<Long, Person> personsById = findContactPersonsById(permitBatch, contactPersonsByPermitId);
            final Map<Long, List<HarvestPermitSpeciesAmount>> speciesAmountsByPermitId = harvestPermitSpeciesAmountRepository
                    .findAll(inCollection(HarvestPermitSpeciesAmount_.harvestPermit, permitBatch))
                    .stream()
                    .collect(groupingBy(s -> s.getHarvestPermit().getId()));
            resultList.addAll(HarvestPermitSearchExportDTO.create(permitBatch, contactPersonsByPermitId, personsById, speciesAmountsByPermitId, rkaNameByRhyId));

        });

        return resultList;
    }

    private Map<Long, Person> findContactPersonsById(final List<HarvestPermit> permits,
                                                     final Map<Long, List<HarvestPermitContactPerson>> contactPersonsByPermitId) {

        final Set<Long> personIds = permits.stream()
                .map(HarvestPermit::getOriginalContactPerson)
                .map(Person::getId)
                .collect(toSet());

        personIds.addAll(contactPersonsByPermitId.values().stream()
                                 .flatMap(List::stream)
                                 .map(HarvestPermitContactPerson::getContactPerson)
                                 .map(Person::getId)
                                 .collect(toSet()));

        final Map<Long, Person> result = Maps.newHashMapWithExpectedSize(Math.min(personIds.size(), BATCH_SIZE));

        // Make sure that person query does not exceed parameter limit.
        Iterables.partition(personIds, BATCH_SIZE).forEach(idBatch ->
            personRepository.findAll(inCollection(Person_.id, idBatch))
                    .forEach(p -> result.merge(p.getId(), p, (oldVal, newVal) -> newVal)));

        return result;
    }

    private static Comparator<HarvestPermit> comparePermitOrderNumber(final HarvestPermitSearchDTO dto) {
        return comparing(p -> DocumentNumberUtil.extractOrderNumber(p.getPermitNumber()),
                keyComparator(dto.getOrdinalSort()));
    }

    private static Comparator<HarvestPermit> comparePermitYear(final HarvestPermitSearchDTO dto) {
        return comparing(p -> DocumentNumberUtil.extractYear(p.getPermitNumber()), keyComparator(dto.getYearSort()));
    }

    private static <T extends Comparable<? super T>> Comparator<T> keyComparator(final Sort.Direction direction) {
        return direction == Sort.Direction.DESC ? Comparator.reverseOrder() : Comparator.naturalOrder();
    }

    private static Specification<HarvestPermit> createSpec(final HarvestPermitSearchDTO dto) {
        final List<Specification<HarvestPermit>> specs = new ArrayList<>();

        specs.add(HarvestPermitSpecs.IS_NOT_ANY_MOOSELIKE_PERMIT);

        if (dto.getRhyId() != null) {
            specs.add(HarvestPermitSpecs.withRhyId(dto.getRhyId()));
        }
        if (StringUtils.isNotBlank(dto.getPermitNumber())) {
            specs.add(HarvestPermitSpecs.withPermitNumber(dto.getPermitNumber()));
            return and(specs);
        }
        if (dto.getAreaId() != null) {
            specs.add(HarvestPermitSpecs.withAreaId(dto.getAreaId()));
        }
        if (dto.getPermitType() != null) {
            specs.add(and(
                    HarvestPermitSpecs.withPermitTypeCode(dto.getPermitType().getPermitTypeCode()),
                    HarvestPermitSpecs.withDecisionOrigin(dto.getPermitType().getOrigin())));
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
        final List<GrantStatus> decisionStatuses = dto.getDecisionStatuses();
        if (decisionStatuses != null) {
            final Specification<HarvestPermit> grantStatus =
                    HarvestPermitSpecs.withDecisionGrantStatus(decisionStatuses);

            if (decisionStatuses.contains(UNCHANGED) ||
                    decisionStatuses.contains(RESTRICTED)) {
                // When searching for granted permits, include LH permits
                specs.add(or(
                        HarvestPermitSpecs.withDecisionOrigin(LUPAHALLINTA),
                        grantStatus));
            } else {
                specs.add(grantStatus);
            }
        }
        return and(specs);
    }

    private static JpaSort sort(final Sort.Direction asc) {
        return JpaSort.of(asc, HarvestPermit_.permitNumber);
    }
}
