package fi.riista.feature.harvestpermit.report.search;

import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.summary.AdminGameDiarySummaryRequestDTO;
import fi.riista.feature.gamediary.summary.AdminGameSummaryPredicates;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportExcelDTO;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportReviewDTO;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportReviewDTOTransformer;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.Interval;
import org.locationtech.jts.io.InStream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fi.riista.feature.gamediary.summary.AdminGameSummaryPredicates.createHarvestPredicate;
import static fi.riista.util.F.mapNullable;

@Component
public class HarvestReportSearchFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestReportSearchRepository harvestReportSearchQueryFactory;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestDTOTransformer harvestTransformer;

    @Resource
    private HarvestReportReviewDTOTransformer reportReviewDTOTransformer;

    @Resource
    private UserRepository userRepository;

    @Nonnull
    private List<HarvestReportExcelDTO> exportExcel(final List<Harvest> harvestList, final boolean includeDetails) {
        return F.mapNonNullsToList(harvestList, h -> HarvestReportExcelDTO.create(h, enumLocaliser, includeDetails));
    }


    // Contact person

    @Transactional(readOnly = true)
    public List<HarvestReportExcelDTO> listByPermitForExcel(final Long id) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(id, EntityPermission.READ);
        return exportExcel(harvestPermit.getAcceptedHarvestForEndOfHuntingReport(), true);
    }

    // Moderator

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public Slice<HarvestDTO> searchModerator(final HarvestReportSearchDTO params, final Pageable pageRequest) {
        final Slice<Harvest> harvestSlice = harvestReportSearchQueryFactory.queryForSlice(params, pageRequest);

        // TODO Update to currently supported HarvestSpecVersion.
        final List<HarvestDTO> dtoList = harvestTransformer.apply(harvestSlice.getContent(), HarvestSpecVersion._7);

        resolveHarvestCreators(harvestSlice.getContent(), dtoList);

        return new SliceImpl<>(dtoList, pageRequest, harvestSlice.hasNext());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public List<HarvestReportExcelDTO> searchModeratorExcel(final HarvestReportSearchDTO params) {
        return exportExcel(harvestReportSearchQueryFactory.queryForList(params), true);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('REVIEW_HARVEST_REPORT_DELAYS')")
    public List<HarvestReportReviewDTO> moderatorHarvestReportReviewExcel(final AdminGameDiarySummaryRequestDTO dto) {

        final GameSpecies gameSpecies = mapNullable(dto.getSpeciesCode(), gameSpeciesService::requireByOfficialCode);
        final Interval interval = DateUtil.createDateInterval(dto.getBeginDate(), dto.getEndDate());

        final BooleanBuilder harvestPredicate = createHarvestPredicate(
                interval, gameSpecies, dto.getOrganisationType(),
                dto.getOfficialCode(), dto.isHarvestReportOnly(), dto.isOfficialHarvestOnly());
        final ArrayList<HarvestReportReviewDTO> list = Lists.newArrayList();
        int page = 0;

        Slice<Harvest> slice;
        do {
            slice = harvestRepository.findAllAsSlice(harvestPredicate, PageRequest.of(page, 1000));
            list.addAll(reportReviewDTOTransformer.apply(slice.getContent()));
            page++;
        } while (slice.hasNext());

        return list;
    }

    private void resolveHarvestCreators(final List<Harvest> entityList, final List<HarvestDTO> dtoList) {
        if (dtoList.isEmpty() || entityList.isEmpty()) {
            return;
        }

        final Map<Long, String> moderatorIndex = userRepository.getModeratorFullNames(entityList);
        final Map<Long, Long> harvestCreator = entityList.stream()
                .collect(Collectors.toMap(Harvest::getId, Harvest::getCreatedByUserId));

        for (final HarvestDTO h : dtoList) {
            h.setModeratorFullName(Optional.of(h.getId())
                    .map(harvestCreator::get)
                    .map(moderatorIndex::get)
                    .orElse(null));
        }
    }

}
