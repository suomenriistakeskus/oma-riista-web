package fi.riista.feature.permit.application.fragment;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Component
public class ListApplicationAreaFragmentsFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private MetsahallitusMaterialYear metsahallitusMaterialYear;

    @Resource
    private HarvestPermitAreaFragmentRepository harvestPermitAreaFragmentRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<HarvestPermitAreaFragmentInfoDTO> getFragmentInfo(final HarvestPermitAreaFragmentRequestDTO dto) {
        final HarvestPermitApplication application = getApplication(dto.getApplicationId());

        return listFragments(application, dto, null);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public HarvestPermitAreaFragmentExcelView getFragmentExcel(final long applicationId,
                                                               final HarvestPermitAreaFragmentExcelRequestDTO dto,
                                                               final Locale locale) {
        final HarvestPermitApplication application = getApplication(applicationId);
        final EnumLocaliser enumLocaliser = new EnumLocaliser(messageSource, locale);
        final List<String> fragmentIds = dto.getFragmentIds();

        final HarvestPermitAreaFragmentRequestDTO requestDto =
                new HarvestPermitAreaFragmentRequestDTO();
        requestDto.setFragmentSizeLimit(dto.getFragmentSizeLimitSquareMeters());

        final List<HarvestPermitAreaFragmentInfoDTO> rows = listFragments(application, requestDto, fragmentIds);

        return new HarvestPermitAreaFragmentExcelView(enumLocaliser, application.getApplicationNumber(), rows);
    }

    private List<HarvestPermitAreaFragmentInfoDTO> listFragments(final HarvestPermitApplication application,
                                                                 final HarvestPermitAreaFragmentRequestDTO requestDTO,
                                                                 final List<String> fragmentIds) {
        final HarvestPermitAreaFragmentQueryParams params = createParams(application, requestDTO);

        final Map<String, List<HarvestPermitAreaFragmentPropertyDTO>> fragmentPropertyNumbers =
                harvestPermitAreaFragmentRepository.getFragmentProperty(params);

        // If fragments list is given, search only for the specified fragments
        final List<HarvestPermitAreaFragmentSizeDTO> fragmentSizeList = fragmentIds == null
                ? harvestPermitAreaFragmentRepository.getFragmentSize(params)
                : harvestPermitAreaFragmentRepository.getFragmentSize(params, fragmentIds);

        return F.mapNonNullsToList(
                fragmentSizeList,
                dto -> new HarvestPermitAreaFragmentInfoDTO(
                        dto, fragmentPropertyNumbers.getOrDefault(dto.getHash(), emptyList())));
    }

    private HarvestPermitAreaFragmentQueryParams createParams(final HarvestPermitApplication application,
                                                              final HarvestPermitAreaFragmentRequestDTO dto) {
        return new HarvestPermitAreaFragmentQueryParams(
                requireZoneId(application),
                metsahallitusMaterialYear.getLatestHirviYear(),
                dto.getFragmentSizeLimit(),
                dto.getLocation());
    }

    private HarvestPermitApplication getApplication(final long applicationId) {
        return requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);
    }

    private static long requireZoneId(final HarvestPermitApplication application) {
        return Optional.ofNullable(application.getArea())
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .orElseThrow(() -> new IllegalArgumentException("Permit area is missing"));
    }
}
