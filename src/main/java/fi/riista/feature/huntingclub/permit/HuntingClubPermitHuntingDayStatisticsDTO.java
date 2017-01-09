package fi.riista.feature.huntingclub.permit;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.joda.time.LocalDateTime;

public class HuntingClubPermitHuntingDayStatisticsDTO {

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
