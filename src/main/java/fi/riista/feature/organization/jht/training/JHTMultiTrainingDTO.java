package fi.riista.feature.organization.jht.training;

import fi.riista.feature.organization.occupation.OccupationType;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class JHTMultiTrainingDTO {


    @NotEmpty
    private List<String> hunterNumbers;

    @NotNull
    private OccupationType occupationType;

    @NotNull
    private JHTTraining.TrainingType trainingType;

    @NotNull
    private LocalDate trainingDate;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String trainingLocation;

    @AssertTrue
    public boolean isJhtOccupationTypeValid() {
        return this.occupationType != null && OccupationType.isValidJhtOccupationType(this.occupationType);
    }

    public List<String> getHunterNumbers() {
        return hunterNumbers;
    }

    public void setHunterNumbers(final List<String> hunterNumbers) {
        this.hunterNumbers = hunterNumbers;
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

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(final LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public String getTrainingLocation() {
        return trainingLocation;
    }

    public void setTrainingLocation(final String trainingLocation) {
        this.trainingLocation = trainingLocation;
    }

}
