package fi.riista.feature.huntingclub.permit.statistics;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.joda.time.LocalDateTime;

import java.util.Collection;
import java.util.Objects;

import static fi.riista.util.NumberUtils.sum;

public class HuntingDayStatisticsDTO {
    public static HuntingDayStatisticsDTO zeros(final long huntingClubId) {
        final HuntingDayStatisticsDTO dto = new HuntingDayStatisticsDTO();
        dto.setHuntingClubId(huntingClubId);
        dto.setLatestUpdate(null);
        dto.setDayCount(0);
        dto.setHunterCount(0);
        dto.setHarvestCount(0);
        dto.setObservationCount(0);

        return dto;
    }

    public static HuntingDayStatisticsDTO calculateTotal(final Collection<HuntingDayStatisticsDTO> stats) {
        final HuntingDayStatisticsDTO dto = new HuntingDayStatisticsDTO();

        dto.setLatestUpdate(getLatestUpdate(stats));
        dto.setDayCount(sum(stats, HuntingDayStatisticsDTO::getDayCount));
        dto.setHunterCount(sum(stats, HuntingDayStatisticsDTO::getHunterCount));
        dto.setHarvestCount(sum(stats, HuntingDayStatisticsDTO::getHarvestCount));
        dto.setObservationCount(sum(stats, HuntingDayStatisticsDTO::getObservationCount));

        return dto;
    }

    private static LocalDateTime getLatestUpdate(final Collection<HuntingDayStatisticsDTO> stats) {
        return stats.stream()
                .map(HuntingDayStatisticsDTO::getLatestUpdate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private long huntingClubId;

    private LocalDateTime latestUpdate;

    private int dayCount;
    private int hunterCount;
    private int harvestCount;
    private int observationCount;

    public long getHuntingClubId() {
        return huntingClubId;
    }

    public void setHuntingClubId(long huntingClubId) {
        this.huntingClubId = huntingClubId;
    }

    public LocalDateTime getLatestUpdate() {
        return latestUpdate;
    }

    public void setLatestUpdate(LocalDateTime latestUpdate) {
        this.latestUpdate = latestUpdate;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public int getHunterCount() {
        return hunterCount;
    }

    public void setHunterCount(int hunterCount) {
        this.hunterCount = hunterCount;
    }

    public int getHarvestCount() {
        return harvestCount;
    }

    public void setHarvestCount(int harvestCount) {
        this.harvestCount = harvestCount;
    }

    public int getObservationCount() {
        return observationCount;
    }

    public void setObservationCount(int observationCount) {
        this.observationCount = observationCount;
    }

    @JsonGetter
    public double harvestsPerDay() {
        return dayCount > 0 ? (double) harvestCount / dayCount : 0.0;
    }

    @JsonGetter
    public double observationsPerDay() {
        return dayCount > 0 ? (double) observationCount / dayCount : 0.0;
    }

    @JsonGetter
    public double harvestsPerHunter() {
        return hunterCount > 0 ? (double) harvestCount / hunterCount : 0.0;
    }

    @JsonGetter
    public double observationsPerHunter() {
        return hunterCount > 0 ? (double) observationCount / hunterCount : 0.0;
    }
}
