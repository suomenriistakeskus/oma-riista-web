package fi.riista.feature.harvestpermit.report;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsRepository;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeasonRepository;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class HarvestReportModeratorFeature {

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private HarvestReportFieldsRepository harvestReportFieldsRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public Map<String, Object> admin() {
        final List<HarvestSeasonDTO> seasonDTOs =
                F.mapNonNullsToList(harvestSeasonRepository.findAll(), HarvestSeasonDTO::create);

        final List<HarvestReportFieldsDTO> fieldDTOs =
                F.mapNonNullsToList(harvestReportFieldsRepository.findAll(), HarvestReportFieldsDTO::create);

        return new ImmutableMap.Builder<String, Object>()
                .put("seasons", seasonDTOs)
                .put("fields", fieldDTOs)
                .build();
    }
}
