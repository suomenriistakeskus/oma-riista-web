package fi.riista.feature.harvestpermit.statistics;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.Collection;

import static fi.riista.util.NumberUtils.nullsafeIntSum;
import static fi.riista.util.NumberUtils.ratio;
import static fi.riista.util.NumberUtils.sum;

public class MoosePermitStatisticsAreaAndPopulation {
    @Nonnull
    public static MoosePermitStatisticsAreaAndPopulation create(final int permitAreaSize,
                                                                final @Nonnull Collection<ClubHuntingSummaryBasicInfoDTO> huntingSummaryList) {
        final int remainingPopulationInEffectiveArea = nullsafeIntSum(huntingSummaryList, ClubHuntingSummaryBasicInfoDTO::getRemainingPopulationInEffectiveArea);
        final int remainingPopulationInTotalArea = nullsafeIntSum(huntingSummaryList, ClubHuntingSummaryBasicInfoDTO::getRemainingPopulationInTotalArea);

        return create(permitAreaSize, huntingSummaryList, remainingPopulationInEffectiveArea, remainingPopulationInTotalArea);
    }

    @Nonnull
    public static MoosePermitStatisticsAreaAndPopulation createForClub(final int permitAreaSize,
                                                                       final @Nonnull Collection<ClubHuntingSummaryBasicInfoDTO> huntingSummaryList) {
        // Remaining population is not shown for club members
        final int remainingPopulationInEffectiveArea = 0;
        final int remainingPopulationInTotalArea = 0;

        return create(permitAreaSize, huntingSummaryList, remainingPopulationInEffectiveArea, remainingPopulationInTotalArea);
    }

    @Nonnull
    public static MoosePermitStatisticsAreaAndPopulation createTotal(final @Nonnull Collection<MoosePermitStatisticsAreaAndPopulation> counts) {
        return new MoosePermitStatisticsAreaAndPopulation(
                sum(counts, MoosePermitStatisticsAreaAndPopulation::getTotalAreaSize),
                sum(counts, MoosePermitStatisticsAreaAndPopulation::getEffectiveAreaSize),
                sum(counts, MoosePermitStatisticsAreaAndPopulation::getRemainingPopulationInTotalArea),
                sum(counts, MoosePermitStatisticsAreaAndPopulation::getRemainingPopulationInEffectiveArea));
    }

    private static MoosePermitStatisticsAreaAndPopulation create(final int permitAreaSize,
                                                                 final @Nonnull Collection<ClubHuntingSummaryBasicInfoDTO> huntingSummaryList,
                                                                 final int remainingPopulationInEffectiveArea,
                                                                 final int remainingPopulationInTotalArea) {
        final int effectiveAreaSize = Math.min(permitAreaSize, nullsafeIntSum(huntingSummaryList, summary -> {
            final int totalOrPermitAreaSize = F.coalesceAsInt(summary.getTotalHuntingArea(), permitAreaSize);
            return F.coalesceAsInt(summary.getEffectiveHuntingArea(), totalOrPermitAreaSize);
        }));

        return new MoosePermitStatisticsAreaAndPopulation(permitAreaSize, effectiveAreaSize,
                remainingPopulationInTotalArea, remainingPopulationInEffectiveArea);
    }

    private MoosePermitStatisticsAreaAndPopulation(final int totalAreaSize,
                                                   final int effectiveAreaSize,
                                                   final int remainingPopulationInTotalArea,
                                                   final int remainingPopulationInEffectiveArea) {
        this.totalAreaSize = totalAreaSize;
        this.effectiveAreaSize = effectiveAreaSize;
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }

    private final int totalAreaSize;
    private final int effectiveAreaSize;
    private final int remainingPopulationInTotalArea;
    private final int remainingPopulationInEffectiveArea;

    @JsonGetter
    public double getRemainingPopulationInTotalAreaPer1000ha() {
        return ratio(remainingPopulationInTotalArea, totalAreaSize / 1000.0);
    }

    @JsonGetter
    public double getRemainingPopulationInEffectiveAreaPer1000ha() {
        return ratio(remainingPopulationInEffectiveArea, effectiveAreaSize / 1000.0);
    }

    public int getTotalAreaSize() {
        return totalAreaSize;
    }

    public int getEffectiveAreaSize() {
        return effectiveAreaSize;
    }

    public int getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public int getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }
}
