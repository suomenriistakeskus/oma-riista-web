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
    private String rhyCode;

    @FinnishSocialSecurityNumber
    private String ssn;

    @FinnishHunterNumber
    private String hunterNumber;

    private LocalDate beginDate;
    private LocalDate endDate;

    public SearchType getSearchType() {
        return searchType;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public JHTTraining.TrainingType getTrainingType() {
        return trainingType;
    }

    public String getTrainingLocation() {
        return trainingLocation;
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
