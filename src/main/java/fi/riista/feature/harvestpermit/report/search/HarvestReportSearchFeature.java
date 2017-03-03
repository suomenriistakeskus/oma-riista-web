package fi.riista.feature.harvestpermit.report.search;

import com.google.common.base.Predicates;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.account.user.UserCrudFeature;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.HarvestReportCrudFeature;
import fi.riista.feature.harvestpermit.report.HarvestReportDTOBase;
import fi.riista.feature.harvestpermit.report.HarvestReportRepository;
import fi.riista.feature.harvestpermit.report.HarvestReportSpecs;
import fi.riista.feature.harvestpermit.report.HarvestReport_;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportExportExcelDTO;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

@Component
public class HarvestReportSearchFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestReportRepository harvestReportRepository;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private UserCrudFeature userCrudFeature;

    @Resource
    private HarvestReportCrudFeature harvestReportCrudFeature;

    @Resource
    private HarvestReportFieldsRepository harvestReportFieldsRepository;

    // HTML table

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public Page<HarvestReportDTOBase> search(final HarvestReportSearchDTO params,
                                             final Pageable pageRequest) {
        final boolean fetchHarvestForExcelView = false;
        final boolean includeReportsViaPermitRhyAssociation = false;

        final Specifications<HarvestReport> spec = constructJpaSpecs(
                params, fetchHarvestForExcelView, includeReportsViaPermitRhyAssociation);

        final Page<HarvestReport> res = loadPage(spec, pageRequest);
        final Map<Long, SystemUser> moderatorCreators = userCrudFeature.getModeratorCreatorsGroupedById(res);

        return DtoUtil.toDTO(res, pageRequest, harvestReportCrudFeature.entityToDTOFunction(moderatorCreators, false, null));
    }

    @Transactional(readOnly = true)
    public List<HarvestReportDTOBase> searchForCoordinator(final HarvestReportSearchDTO params) {
        final Specifications<HarvestReport> spec = constructJpaSpecForRhy(params, false);

        final Tuple2<List<HarvestReport>, Predicate<Harvest>> t = loadAll(spec, params.getFieldsId());
        return F.mapNonNullsToList(t._1, harvestReportCrudFeature.entityToDTOFunction(emptyMap(), true, t._2));
    }

    // Excel

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<HarvestReportExportExcelDTO> searchExcel(final HarvestReportSearchDTO params) {
        final boolean fetchHarvestForExcelView = true;
        final boolean includeReportsViaPermitRhyAssociation = false;

        final Specifications<HarvestReport> spec = constructJpaSpecs(
                params, fetchHarvestForExcelView, includeReportsViaPermitRhyAssociation);

        final Tuple2<List<HarvestReport>, Predicate<Harvest>> t = loadAll(spec, params.getFieldsId());
        return HarvestReportExportExcelDTO.create(t._1, enumLocaliser, t._2);
    }

    @Transactional(readOnly = true)
    public List<HarvestReportExportExcelDTO> searchForCoordinatorExcel(final HarvestReportSearchDTO params) {
        final boolean fetchHarvestForExcelView = true;

        final Specifications<HarvestReport> spec = constructJpaSpecForRhy(params, fetchHarvestForExcelView);

        final Tuple2<List<HarvestReport>, Predicate<Harvest>> t = loadAll(spec, params.getFieldsId());
        return HarvestReportExportExcelDTO.create(t._1, enumLocaliser, t._2);
    }

    private Page<HarvestReport> loadPage(final Specification<HarvestReport> spec,
                                         final Pageable pageRequest) {
        final Page<HarvestReport> page = harvestReportRepository.findAll(spec, pageRequest);
        final List<HarvestReport> list = page.getContent().stream()
                .filter(report -> report.getState() != HarvestReport.State.DELETED)
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageRequest, page.getTotalElements());
    }

    private Tuple2<List<HarvestReport>, Predicate<Harvest>> loadAll(final Specification<HarvestReport> spec, final Long fieldsId) {
        final JpaSort harvestReportSort = new JpaSort(Sort.Direction.ASC, HarvestReport_.id);

        // If fields id is given, then according to fields.usedWithPermit each report.permit needs to be null or not
        // null, and each harvest needs to be match field.species. Without these filters result contains reports which are not
        Predicate<HarvestReport> extraFilter = Predicates.alwaysTrue();
        GameSpecies species = null;
        if (fieldsId != null) {
            final HarvestReportFields fields = harvestReportFieldsRepository.getOne(fieldsId);
            extraFilter = report -> fields.isUsedWithPermit() ? report.getHarvestPermit() != null : report.getHarvestPermit() == null;
            species = fields.getSpecies();
        }
        final List<HarvestReport> reports = harvestReportRepository.findAll(spec, harvestReportSort).stream()
                .filter(report -> report.getState() != HarvestReport.State.DELETED)
                .filter(extraFilter)
                .collect(Collectors.toList());
        return Tuple.of(reports, createSpeciesFilter(species));
    }

    private Predicate<Harvest> createSpeciesFilter(GameSpecies gameSpecies) {
        if (gameSpecies == null) {
            return Predicates.alwaysTrue();
        }
        return harvest -> harvest.getSpecies().equals(gameSpecies);
    }

    private Specifications<HarvestReport> constructJpaSpecForRhy(final HarvestReportSearchDTO params,
                                                                 final boolean fetchHarvestForExcelView) {
        Objects.requireNonNull(params.getRhyId());

        userAuthorizationHelper.assertCoordinatorOrModerator(params.getRhyId());

        final HarvestReportSearchDTO rhyParams = HarvestReportSearchDTO.cloneWithRhyRelevantFields(params);

        final boolean includeReportsViaPermitRhyAssociation = true;

        return constructJpaSpecs(rhyParams, fetchHarvestForExcelView, includeReportsViaPermitRhyAssociation);
    }

    private static Specifications<HarvestReport> constructJpaSpecs(
            final HarvestReportSearchDTO params,
            final boolean fetchHarvestForExcelView,
            final boolean includeReportsViaPermitRhyAssociation) {

        Specifications<HarvestReport> spec = Specifications
                .where(HarvestReportSpecs.withStates(params.getStates()));

        if (fetchHarvestForExcelView) {
            // FIXME we REALLY would like to prefetch harvests, because when generating excel we read data from harvests
        }

        if (params.hasBeginOrEndDate()) {
            spec = spec.and(HarvestReportSpecs.withHarvestBetween(params.getBeginDate(), params.getEndDate()));
        }
        if (params.getSeasonId() != null) {
            spec = spec.and(HarvestReportSpecs.withHarvestSeasonId(params.getSeasonId()));
        }
        if (params.getFieldsId() != null) {
            spec = spec.and(JpaSpecs.or(
                    HarvestReportSpecs.withFieldsId(params.getFieldsId()),
                    HarvestReportSpecs.hasEndOfHuntingReportAndFieldsSpeciesIsPermitSpecies(params.getFieldsId())
            ));
        }
        if (params.getAreaId() != null) {
            spec = spec.and(HarvestReportSpecs.withAreaId(params.getAreaId()));
        }
        if (params.getHarvestAreaId() != null) {
            spec = spec.and(HarvestReportSpecs.withQuotaAreaId(params.getHarvestAreaId()));
        }
        if (StringUtils.isNotBlank(params.getText())) {
            for (String s : params.getText().split(" ")) {
                spec = spec.and(HarvestReportSpecs.withDescriptionLike(s));
            }
        }
        if (StringUtils.isNotBlank(params.getPermitNumber())) {
            spec = spec.and(HarvestReportSpecs.withPermitNumber(params.getPermitNumber()));
        }

        if (params.getRhyId() != null) {
            final long rhyId = params.getRhyId();
            final Specification<HarvestReport> directRhyRelationSpec = HarvestReportSpecs.withRhyId(rhyId);

            final Specification<HarvestReport> compoundRhySpec = includeReportsViaPermitRhyAssociation
                    ? Specifications.where(directRhyRelationSpec).or(HarvestReportSpecs.withPermitRhyId(rhyId))
                    : directRhyRelationSpec;

            spec = spec.and(compoundRhySpec);
        }

        return spec;
    }

    @Transactional(readOnly = true)
    public List<HarvestReportExportExcelDTO> findByPermit(final Long id) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(id, EntityPermission.READ);

        return HarvestReportExportExcelDTO.create(harvestPermit.getUndeletedHarvestReports(), enumLocaliser, Predicates.alwaysTrue());
    }
}
