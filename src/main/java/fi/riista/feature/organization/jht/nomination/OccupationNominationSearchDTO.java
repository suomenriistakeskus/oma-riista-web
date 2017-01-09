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

    public int getPageSize() {
        return pageSize;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public OccupationNomination.NominationStatus getNominationStatus() {
        return nominationStatus;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public String getSsn() {
        return ssn;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }
}
