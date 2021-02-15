package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportDTO;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticsExportService;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.F.mapNullable;

/**
 * Service for transforming RHY annual statiscics for RHY subsidy calculation. Certain statistics
 * need to be weighted (multiplied) for certain subsidy years.
 */
@Component
public class RhySubsidyStatisticsExportService {

    // Multiplier for winter and summer game triangle counts
    private static final int GAME_TRIANGLE_MULTIPLIER_2021 = 3;

    @Resource
    private AnnualStatisticsExportService service;

    @Transactional(readOnly = true)
    public List<AnnualStatisticsExportDTO> exportWeightedAnnualStatistics(final int subsidyYear,
                                                                          final int statisticsYear,
                                                                          @Nonnull final EnumSet<RhyAnnualStatisticsState> states) {

        checkArgument(!F.isNullOrEmpty(states), "states must not be empty or null");

        return F.mapNonNullsToList(service.exportAnnualStatistics(statisticsYear, states),
                (dto) -> performWeighting(subsidyYear, dto));
    }

    /*package*/ static AnnualStatisticsExportDTO performWeighting(final int subsidyYear, final AnnualStatisticsExportDTO dto) {
        if (subsidyYear >= 2021) {
            final LukeStatistics luke = dto.getLuke();
            luke.setWinterGameTriangles(mapNullable(luke.getWinterGameTriangles(), i -> i * GAME_TRIANGLE_MULTIPLIER_2021));
            luke.setSummerGameTriangles(mapNullable(luke.getSummerGameTriangles(), i -> i * GAME_TRIANGLE_MULTIPLIER_2021));
        }

        return dto;
    }
}
