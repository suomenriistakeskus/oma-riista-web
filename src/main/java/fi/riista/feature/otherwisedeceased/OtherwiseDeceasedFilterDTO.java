package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class OtherwiseDeceasedFilterDTO implements HasBeginAndEndDate {

    private Integer gameSpeciesCode;

    @NotNull
    @Valid
    private LocalDate beginDate;

    @NotNull
    @Valid
    private LocalDate endDate;

    @Pattern(regexp = "^\\d{0,3}$")
    private String rkaOfficialCode;

    @Pattern(regexp = "^\\d{0,3}$")
    private String rhyOfficialCode;

    @Valid
    private OtherwiseDeceasedCause cause;

    private boolean showRejected;

    // Accessors

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getRkaOfficialCode() {
        return rkaOfficialCode;
    }

    public void setRkaOfficialCode(final String rkaOfficialCode) {
        this.rkaOfficialCode = rkaOfficialCode;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public void setRhyOfficialCode(final String rhyOfficialCode) {
        this.rhyOfficialCode = rhyOfficialCode;
    }

    public OtherwiseDeceasedCause getCause() {
        return cause;
    }

    public void setCause(final OtherwiseDeceasedCause cause) {
        this.cause = cause;
    }

    public boolean isShowRejected() {
        return showRejected;
    }

    public void setShowRejected(final boolean showRejected) {
        this.showRejected = showRejected;
    }
}
