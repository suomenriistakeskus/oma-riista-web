package fi.riista.feature.gamediary.srva;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;
import java.util.List;

public class SrvaEventSearchDTO implements HasBeginAndEndDate {

    private LocalDate beginDate;

    private LocalDate endDate;

    private Long rhyId;

    private Long rkaId;

    @NotNull
    private List<SrvaEventStateEnum> states;

    @NotNull
    private List<SrvaEventNameEnum> eventNames;

    private Integer gameSpeciesCode;

    // RhyId based on selected role in UI. If selected role is for example SRVA-contact person of
    // 'Tampereen riistanhoitoyhdistys' currentRhyId is rhyId of 'Tampereen riistanhoitoyhdistys'.
    private Long currentRhyId;

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

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(Long rhyId) {
        this.rhyId = rhyId;
    }

    public Long getRkaId() {
        return rkaId;
    }

    public void setRkaId(Long rkaId) {
        this.rkaId = rkaId;
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
}
