package fi.riista.feature.organization.rhy.mobile;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.training.OccupationTraining;
import javax.validation.constraints.NotNull;
import org.joda.time.LocalDate;

public class MobileOccupationTrainingDTO {

    public static MobileOccupationTrainingDTO create(final OccupationTraining training) {
        MobileOccupationTrainingDTO dto = new MobileOccupationTrainingDTO();

        dto.setId(training.getId());
        dto.setOccupationType(training.getOccupationType());
        dto.setTrainingType(training.getTrainingType());
        dto.setDate(training.getTrainingDate());

        return dto;
    }

    private Long id;

    @NotNull
    private OccupationType occupationType;

    @NotNull
    private TrainingType trainingType;

    @NotNull
    private LocalDate date;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(final TrainingType trainingType) {
        this.trainingType = trainingType;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
