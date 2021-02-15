package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class OccupationNominationSearchDTO {
    private static final int DEFAULT_PAGE_SIZE = 50;

    private int page;
    private int pageSize = DEFAULT_PAGE_SIZE;

    @NotNull
    private OccupationType occupationType;

    @NotNull
    private OccupationNomination.NominationStatus nominationStatus;

    @Pattern(regexp = "\\d{3}")
    private String areaCode;

    @Pattern(regexp = "\\d{3}")
    private String rhyCode;

    @FinnishSocialSecurityNumber
    private String ssn;

    @FinnishHunterNumber
    private String hunterNumber;

    private LocalDate beginDate;
    private LocalDate endDate;

    public int getPage() {
        return page;
    }

    public void setPage(final int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public OccupationNomination.NominationStatus getNominationStatus() {
        return nominationStatus;
    }

    public void setNominationStatus(final OccupationNomination.NominationStatus nominationStatus) {
        this.nominationStatus = nominationStatus;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(final String areaCode) {
        this.areaCode = areaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(final String ssn) {
        this.ssn = ssn;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }
}
