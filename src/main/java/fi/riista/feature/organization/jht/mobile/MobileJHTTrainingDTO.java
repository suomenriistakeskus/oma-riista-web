package fi.riista.feature.organization.jht.mobile;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.feature.organization.occupation.OccupationType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.joda.time.LocalDate;

public class MobileJHTTrainingDTO {

    public static MobileJHTTrainingDTO create(final JHTTraining training) {
        final MobileJHTTrainingDTO dto = new MobileJHTTrainingDTO();

        dto.id = training.getId();
        dto.trainingType = training.getTrainingType();
        dto.setOccupationType(training.getOccupationType());
        dto.date = training.getTrainingDate();
        dto.location = training.getTrainingLocation();

        return dto;
    }

    @NotNull
    private Long id;

    @NotNull
    private TrainingType trainingType;

    @NotNull
    private OccupationType occupationType;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String location;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(final TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }
}
