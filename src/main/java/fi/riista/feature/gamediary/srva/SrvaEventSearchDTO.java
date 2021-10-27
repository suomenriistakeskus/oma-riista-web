package fi.riista.feature.gamediary.srva;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.List;

import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.OTHER;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.RAILWAY_ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.TRAFFIC_ACCIDENT;

public class SrvaEventSearchDTO implements HasBeginAndEndDate {

    private LocalDate beginDate;

    private LocalDate endDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rkaCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rhyCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String htaCode;

    @NotNull
    private List<SrvaEventStateEnum> states;

    @NotNull
    private List<SrvaEventNameEnum> eventNames;

    private List<SrvaEventTypeEnum> eventTypes;

    private Integer gameSpeciesCode;

    // RhyId based on selected role in UI. If selected role is for example SRVA-contact person of
    // 'Tampereen riistanhoitoyhdistys' currentRhyId is rhyId of 'Tampereen riistanhoitoyhdistys'.
    private Long currentRhyId;

    // set to true from views only for moderator
    private boolean moderatorView;

    @AssertTrue
    public boolean isAccidentCriteriaValid() {
        if (eventNames.contains(ACCIDENT)) {
            return eventTypes.containsAll(EnumSet.of(TRAFFIC_ACCIDENT, RAILWAY_ACCIDENT, OTHER));
        }

        return true;
    }

    // Only accident subtypes allowed in search
    @AssertTrue
    public boolean isEventTypesValid() {
        final EnumSet<SrvaEventTypeEnum> unSupportedTypes =
                EnumSet.complementOf(EnumSet.of(TRAFFIC_ACCIDENT, RAILWAY_ACCIDENT, OTHER));
        return !eventTypes.contains(unSupportedTypes);
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getRkaCode() {
        return rkaCode;
    }

    public void setRkaCode(final String rkaCode) {
        this.rkaCode = rkaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public String getHtaCode() {
        return htaCode;
    }

    public void setHtaCode(final String htaCode) {
        this.htaCode = htaCode;
    }

    public List<SrvaEventStateEnum> getStates() {
        return states;
    }

    public void setStates(List<SrvaEventStateEnum> states) {
        this.states = states;
    }

    public List<SrvaEventNameEnum> getEventNames() {
        return eventNames;
    }

    public void setEventNames(List<SrvaEventNameEnum> eventNames) {
        this.eventNames = eventNames;
    }

    public List<SrvaEventTypeEnum> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(final List<SrvaEventTypeEnum> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Long getCurrentRhyId() {
        return currentRhyId;
    }

    public void setCurrentRhyId(Long currentRhyId) {
        this.currentRhyId = currentRhyId;
    }

    public boolean isModeratorView() {
        return moderatorView;
    }

    public void setModeratorView(boolean moderatorView) {
        this.moderatorView = moderatorView;
    }
}
