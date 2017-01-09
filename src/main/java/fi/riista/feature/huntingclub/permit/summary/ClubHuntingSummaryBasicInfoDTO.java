package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.util.F;

import org.joda.time.LocalDate;

import javax.annotation.Nullable;

import java.io.Serializable;

public class ClubHuntingSummaryBasicInfoDTO implements MutableHuntingEndStatus, Serializable {

    @Nullable
    public static Integer calculatePercentageBasedEffectiveArea(@Nullable final Integer totalArea,
                                                                @Nullable final Float effectiveAreaPercentage) {

        return totalArea == null || effectiveAreaPercentage == null
                ? null
                : Float.valueOf(totalArea.floatValue() * effectiveAreaPercentage.floatValue() / 100.0f).intValue();
    }

    private Long clubId;
    private int gameSpeciesCode;

    private boolean huntingFinished;
    private LocalDate huntingEndDate;
    private boolean fromMooseDataCard;
    private boolean huntingFinishedByModeration;

    private Integer totalHuntingArea;
    private Integer effectiveHuntingArea;
    private Integer remainingPopulationInTotalArea;
    private Integer remainingPopulationInEffectiveArea;

    public boolean isEmpty() {
        return F.allNull(huntingEndDate, totalHuntingArea, effectiveHuntingArea, remainingPopulationInTotalArea,
                remainingPopulationInEffectiveArea);
    }

    // Accessors -->

    @Override
    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    @Override
    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    @Override
    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    @Override
    public void setHuntingFinished(boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
    }

    @Override
    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    @Override
    public void setHuntingEndDate(final LocalDate huntingEndDate) {
        this.huntingEndDate = huntingEndDate;
    }

    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    public void setTotalHuntingArea(final Integer totalHuntingArea) {
        this.totalHuntingArea = totalHuntingArea;
    }

    public boolean isFromMooseDataCard() {
        return fromMooseDataCard;
    }

    public void setFromMooseDataCard(boolean fromMooseDataCard) {
        this.fromMooseDataCard = fromMooseDataCard;
    }

    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    public void setEffectiveHuntingArea(final Integer effectiveHuntingArea) {
        this.effectiveHuntingArea = effectiveHuntingArea;
    }

    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public void setRemainingPopulationInTotalArea(final Integer remainingPopulationInTotalArea) {
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
    }

    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    public void setRemainingPopulationInEffectiveArea(final Integer remainingPopulationInEffectiveArea) {
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }

    public boolean isHuntingFinishedByModeration() {
        return huntingFinishedByModeration;
    }

    public void setHuntingFinishedByModeration(boolean huntingFinishedByModeration) {
        this.huntingFinishedByModeration = huntingFinishedByModeration;
    }
}
