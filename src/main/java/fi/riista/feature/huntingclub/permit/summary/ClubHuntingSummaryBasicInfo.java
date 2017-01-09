package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.util.F;

public interface ClubHuntingSummaryBasicInfo extends HuntingEndStatus {

    HuntingClub getClub();

    @Override
    default Long getClubId() {
        return getClub().getId();
    }

    Integer getTotalHuntingArea();

    Integer getEffectiveHuntingArea();

    Float getEffectiveHuntingAreaPercentage();

    Integer getRemainingPopulationInTotalArea();

    Integer getRemainingPopulationInEffectiveArea();

    // Hunting summary is considered empty if no summary data is present.
    default boolean isEmpty() {
        return !isHuntingFinished() && F.allNull(
                getHuntingEndDate(), getTotalHuntingArea(), getEffectiveHuntingArea(),
                getEffectiveHuntingAreaPercentage(), getRemainingPopulationInTotalArea(),
                getRemainingPopulationInEffectiveArea());
    }

}
