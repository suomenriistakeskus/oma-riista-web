package fi.riista.feature.harvestpermit.report.search;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.excel.HarvestReportExcelDTO;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HarvestReportSearchFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestReportSearchRepository harvestReportSearchQueryFactory;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private HarvestDTOTransformer harvestTransformer;

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

    // Coordinator

    @Transactional(readOnly = true)
    public List<HarvestDTO> searchCoordinator(final HarvestReportSearchDTO params) {
        return F.mapNonNullsToList(harvestTransformer.apply(listCoordinator(params), HarvestSpecVersion.CURRENTLY_SUPPORTED),
                dto -> {
                    dto.setActorInfo(null);
                    dto.setAuthorInfo(null);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public List<HarvestReportExcelDTO> searchCoordinatorExcel(final HarvestReportSearchDTO params) {
        return exportExcel(listCoordinator(params), false);
    }

    private List<Harvest> listCoordinator(final HarvestReportSearchDTO dto) {
        userAuthorizationHelper.assertCoordinatorOrModerator(
                Objects.requireNonNull(dto.getRhyId(), "rhyId is required"));

        return harvestReportSearchQueryFactory.queryForList(dto);
    }
}
