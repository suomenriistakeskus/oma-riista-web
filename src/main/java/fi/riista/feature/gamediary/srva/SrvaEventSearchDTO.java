package fi.riista.feature.gamediary.srva;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;
import java.util.List;

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

    private Integer gameSpeciesCode;

    // RhyId based on selected role in UI. If selected role is for example SRVA-contact person of
    // 'Tampereen riistanhoitoyhdistys' currentRhyId is rhyId of 'Tampereen riistanhoitoyhdistys'.
    private Long currentRhyId;

    // set to true from views only for moderator
    private boolean moderatorView;

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
