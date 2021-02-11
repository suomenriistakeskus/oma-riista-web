package fi.riista.feature.organization.jht.training;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class JHTTrainingSearchDTO {
    private static final int DEFAULT_PAGE_SIZE = 50;

    public enum SearchType {
        PREVIOUS_OCCUPATION,
        TRAINING_LOCATION,
        HOME_RHY,
        PERSON
    }

    @Min(0)
    private int page;
    private int pageSize = DEFAULT_PAGE_SIZE;

    @NotNull
    private SearchType searchType;

    @NotNull
    private OccupationType occupationType;

    private JHTTraining.TrainingType trainingType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String trainingLocation;

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

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(final SearchType searchType) {
        this.searchType = searchType;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public JHTTraining.TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(final JHTTraining.TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public String getTrainingLocation() {
        return trainingLocation;
    }

    public void setTrainingLocation(final String trainingLocation) {
        this.trainingLocation = trainingLocation;
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
