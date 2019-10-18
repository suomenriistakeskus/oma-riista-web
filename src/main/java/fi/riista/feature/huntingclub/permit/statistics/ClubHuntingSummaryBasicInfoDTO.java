package fi.riista.feature.huntingclub.permit.statistics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingEndStatus;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.Optional;

public class ClubHuntingSummaryBasicInfoDTO implements HuntingEndStatus, Serializable {

    static Integer calculatePercentageBasedEffectiveArea(final Integer totalArea,
                                                         final Double effectiveAreaPercentage) {
        if (totalArea == null || effectiveAreaPercentage == null) {
            return null;
        }

        return (int) Math.round(totalArea.doubleValue() * effectiveAreaPercentage / 100.0);
    }

    private final Long permitId;
    private final Long clubId;
    private final int gameSpeciesCode;

    @JsonIgnore
    private final Long moderatorOverrideSummaryId;

    @JsonIgnore
    private final Integer moderatorOverrideSummaryRevision;

    private final boolean huntingFinished;
    private final LocalDate huntingEndDate;
    private final boolean fromMooseDataCard;
    private final boolean huntingFinishedByModeration;

    private final Integer totalHuntingArea;
    private final Integer effectiveHuntingArea;
    private final Integer remainingPopulationInTotalArea;
    private final Integer remainingPopulationInEffectiveArea;

    public ClubHuntingSummaryBasicInfoDTO(final Long permitId,
                                          final Long clubId,
                                          final int gameSpeciesCode,
                                          final Long moderatorOverrideSummaryId,
                                          final Integer moderatorOverrideSummaryRevision,
                                          final boolean huntingFinished,
                                          final LocalDate huntingEndDate,
                                          final boolean fromMooseDataCard,
                                          final boolean huntingFinishedByModeration,
                                          final Integer totalHuntingArea,
                                          final Integer effectiveHuntingArea,
                                          final Double effectiveAreaPercentage,
                                          final Integer remainingPopulationInTotalArea,
                                          final Integer remainingPopulationInEffectiveArea) {
        this.permitId = permitId;
        this.clubId = clubId;
        this.gameSpeciesCode = gameSpeciesCode;
        this.moderatorOverrideSummaryId = moderatorOverrideSummaryId;
        this.moderatorOverrideSummaryRevision = moderatorOverrideSummaryRevision;
        this.huntingFinished = huntingFinished;
        this.huntingEndDate = huntingEndDate;
        this.fromMooseDataCard = fromMooseDataCard;
        this.huntingFinishedByModeration = huntingFinishedByModeration;
        this.totalHuntingArea = totalHuntingArea;
        this.effectiveHuntingArea = Optional
                .ofNullable(effectiveHuntingArea)
                .orElseGet(() -> calculatePercentageBasedEffectiveArea(totalHuntingArea, effectiveAreaPercentage));

        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }

    // Hunting summary is considered empty if no summary data is present.
    public boolean isEmpty() {
        return F.allNull(huntingEndDate,
                totalHuntingArea,
                effectiveHuntingArea,
                remainingPopulationInTotalArea,
                remainingPopulationInEffectiveArea);
    }

    // Accessors -->

    public Long getPermitId() {
        return permitId;
    }

    @Override
    public Long getClubId() {
        return clubId;
    }

    @Override
    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public Long getModeratorOverrideSummaryId() {
        return moderatorOverrideSummaryId;
    }

    public Integer getModeratorOverrideSummaryRevision() {
        return moderatorOverrideSummaryRevision;
    }

    @Override
    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    @Override
    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    public boolean isFromMooseDataCard() {
        return fromMooseDataCard;
    }

    // Returns either stored area size or value calculated from percentage of total area.
    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    public boolean isHuntingFinishedByModeration() {
        return huntingFinishedByModeration;
    }
}
